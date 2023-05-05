package com.yalingunayer.cgj2ex;

import com.yalingunayer.cgj2ex.elixir.Module;
import com.yalingunayer.cgj2ex.elixir.*;
import com.yalingunayer.cgj2ex.elixir.types.BasicType;
import com.yalingunayer.cgj2ex.elixir.types.ListType;
import com.yalingunayer.cgj2ex.elixir.types.Type;
import com.yalingunayer.cgj2ex.elixir.types.TypeRef;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Elixirify {
    public static List<Module> getAllModules() {
        var models = Stream.of("io.craftgate.request", "io.craftgate.response", "io.craftgate.model")
                .map(packageName -> new Reflections(packageName, new SubTypesScanner(false)))
                .flatMap(reflections -> reflections.getSubTypesOf(Object.class).stream())
                .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()) && !Modifier.isStatic(clazz.getModifiers()))
                .map(Elixirify::parseClass);

        var enums = new Reflections("io.craftgate")
                .getSubTypesOf(Enum.class)
                .stream()
                .map(Elixirify::parseEnum);

        return Stream.concat(models, enums).toList();
    }

    public static void writeNamespace(Renderer renderer, Path rootDir, Namespace namespace) throws Exception {
        var filePathParts = Stream.of(namespace.getName().split("\\."))
                .map(String::toLowerCase)
                .toList()
                .toArray(new String[0]);

        var namespacePath = Paths.get(rootDir.toAbsolutePath().toString(), filePathParts).toAbsolutePath();

        namespacePath.toFile().mkdirs();

        for (var module : namespace.getModules()) {
            var filePath = namespacePath.resolve(Utils.toSnakeCase(module.getName()) + ".ex");
            var file = filePath.toFile();

            var renderedModule = renderer.render(module);
            try (var fos = new FileOutputStream(file);
                 var writer = new OutputStreamWriter(fos)) {
                writer.write(renderedModule);
                System.out.printf("Module %s was written to %s%n", module.getName(), filePath);
            }
        }
    }

    public static Module parseEnum(Class<? extends Enum> clazz) {
        var values = Arrays.stream(clazz.getEnumConstants())
                .map(Enum::toString)
                .collect(Collectors.toSet());

        var packageName = clazz.getPackage().getName();
        var name = clazz.getSimpleName();

        return EnumModule.builder()
                .packageName(packageName)
                .name(name)
                .values(values)
                .build();
    }

    public static Module parseClass(Class<?> clazz) {
        var fields = getAllDeclaredFields(clazz).toList();
        var packageName = clazz.getPackage().getName();
        var name = clazz.getSimpleName();
        return ClassModule.builder()
                .packageName(packageName)
                .name(name)
                .fields(fields)
                .build();
    }

    private static Stream<Field> getAllDeclaredFields(Class clazz) {
        if (Objects.isNull(clazz) || Object.class.equals(clazz)) {
            return Stream.empty();
        }

        var myFields = Stream.of(clazz.getDeclaredFields())
                .filter(field -> !Modifier.isAbstract(field.getModifiers()) && !Modifier.isStatic(field.getModifiers()))
                .map(field -> new Field(field.getName(), parseType(field.getGenericType())));

        var genericSuperclass = clazz.getGenericSuperclass();

        if (genericSuperclass instanceof ParameterizedType parameterizedType) {
            return Stream.concat(myFields, getAllDeclaredFieldsFromParameterizedType(parameterizedType));
        }

        return Stream.concat(myFields, getAllDeclaredFields((Class) genericSuperclass));
    }

    private static Stream<Field> getAllDeclaredFieldsFromParameterizedType(ParameterizedType parameterizedType) {
        var rawType = (Class) parameterizedType.getRawType();
        var typeVariables = rawType.getTypeParameters();
        var typeArguments = parameterizedType.getActualTypeArguments();
        var typesByName = new HashMap<String, Class>();
        for (var i = 0; i < typeArguments.length; i++) {
            typesByName.put(typeVariables[i].getName(), (Class) typeArguments[i]);
        }
        return Stream.of(rawType.getDeclaredFields())
                .filter(field -> !Modifier.isAbstract(field.getModifiers()) && !Modifier.isStatic(field.getModifiers()))
                .flatMap(field -> {
                    var genericType = field.getGenericType();
                    if (genericType instanceof ParameterizedType fieldType) {
                        return Arrays.stream(fieldType.getActualTypeArguments())
                                .map(type -> {
                                    if (type instanceof TypeVariable typeVariable) {
                                        var actualType = typesByName.get(typeVariable.getName());
                                        if (Objects.isNull(actualType)) {
                                            throw new RuntimeException(String.format("Unknown type for type variable %s on field %s", typeVariable.getName(), field.getName()));
                                        }
                                        return new Field(field.getName(), parseType(actualType));
                                    }
                                    return new Field(field.getName(), parseType(field.getGenericType()));
                                });
                    }
                    return Stream.of(new Field(field.getName(), parseType(field.getGenericType())));
                });
    }

    public static Type parseType(java.lang.reflect.Type type) {
        if (type instanceof ParameterizedType parameterizedType) {
            var outerType = parseType(parameterizedType.getRawType());

            if (outerType instanceof ListType listType) {
                var innerTypes = Stream.of(((ParameterizedType) type).getActualTypeArguments())
                        .map(Elixirify::parseType)
                        .toList();

                listType.setInner(innerTypes);

                return outerType;
            }

            return outerType;
        }

        if (type instanceof TypeVariable<?> typeVariable) {
            var generic = typeVariable.getGenericDeclaration();
            return parseType((Class<?>) generic);
        }

        return parseType((Class<?>) type);
    }

    public static Type parseType(Class<?> clazz) {
        if (Objects.nonNull(clazz.getPackage()) && clazz.getPackage().getName().startsWith("io.craftgate")) {
            return new TypeRef(clazz.getPackage().getName(), clazz.getSimpleName()).aliased();
        }

        if (clazz.isAssignableFrom(List.class) || clazz.isAssignableFrom(Set.class)) {
            return new ListType();
        }

        return BasicType.from(clazz);
    }
}
