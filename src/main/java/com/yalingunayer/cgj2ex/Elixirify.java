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

        var lastIdx = filePathParts.length - 1;
        filePathParts[lastIdx] = filePathParts[lastIdx] + ".ex";

        var targetPath = Paths.get(rootDir.toAbsolutePath().toString(), filePathParts).toAbsolutePath();
        var file = targetPath.toFile();

        file.getParentFile().mkdirs();

        var renderedNamespace = renderer.render(namespace);
        try (var fos = new FileOutputStream(file);
            var writer = new OutputStreamWriter(fos)) {
            writer.write(renderedNamespace);
        }

        System.out.printf("Namespace %s was written to %s%n", namespace.getName(), targetPath);
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
        var fields = Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> !Modifier.isAbstract(field.getModifiers()) && !Modifier.isStatic(field.getModifiers()))
                .map(field -> new Field(field.getName(), parseType(field.getGenericType())))
                .toList();

        var packageName = clazz.getPackage().getName();
        var name = clazz.getSimpleName();
        return ClassModule.builder()
                .packageName(packageName)
                .name(name)
                .fields(fields)
                .build();
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
        if (String.class.equals(clazz)) {
            return BasicType.STRING;
        }

        if (Integer.class.equals(clazz) || Long.class.equals(clazz) ||
                int.class.equals(clazz) || long.class.equals(clazz)) {
            return BasicType.INTEGER;
        }

        if (Double.class.equals(clazz) || Float.class.equals(clazz) || BigDecimal.class.equals(clazz) ||
                double.class.equals(clazz) || float.class.equals(clazz)) {
            return BasicType.FLOAT;
        }

        if (Boolean.class.equals(clazz) || boolean.class.equals(clazz)) {
            return BasicType.BOOLEAN;
        }

        if (clazz.isAssignableFrom(Map.class)) {
            return BasicType.MAP;
        }

        if (LocalDateTime.class.equals(clazz) || ZonedDateTime.class.equals(clazz)) {
            // TODO
            return BasicType.STRING;
        }

        if (LocalDate.class.equals(clazz) || Date.class.equals(clazz)) {
            // TODO
            return BasicType.STRING;
        }

        if (Objects.nonNull(clazz.getPackage()) && clazz.getPackage().getName().startsWith("io.craftgate")) {
            return new TypeRef(clazz.getPackage().getName(), clazz.getSimpleName()).aliased();
        }

        if (clazz.isAssignableFrom(List.class) || clazz.isAssignableFrom(Set.class)) {
            return new ListType();
        }

        return BasicType.TERM;
    }
}
