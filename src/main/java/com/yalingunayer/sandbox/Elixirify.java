package com.yalingunayer.sandbox;

import com.yalingunayer.sandbox.elixir.Module;
import com.yalingunayer.sandbox.elixir.*;
import com.yalingunayer.sandbox.elixir.types.BasicType;
import com.yalingunayer.sandbox.elixir.types.ListType;
import com.yalingunayer.sandbox.elixir.types.Type;
import com.yalingunayer.sandbox.elixir.types.TypeRef;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
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
    public static void main(String[] args) throws Exception {
        final Path rootPath;
        if (args.length > 0) {
            rootPath = new File(args[0]).toPath().toAbsolutePath();
        } else {
            rootPath = Paths.get(System.getProperty("user.dir"), "output").toAbsolutePath();
        }

        System.out.printf("Using root path %s%n", rootPath);

        var enums = getEnums();
        var models = getModels();

        var ignoredPackages = List.of(
                "io.craftgate.net",
                "io.craftgate.request.common"
        );

        var allObjects = Stream
                .concat(enums.stream(), models.stream())
                .filter(module -> !ignoredPackages.contains(module.getPackageName()))
                .collect(Collectors.groupingBy(Module::getPackageName));

        var renderer = Renderer.createDefault();

        allObjects
                .keySet()
                .stream()
                .map(name -> new Namespace(Utils.packageNameToNamespace(name), allObjects.get(name)))
                .forEach(namespace -> {
                    try {
                        writeNamespace(renderer, rootPath, namespace);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        System.out.println("Successfully finished writing modules");
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

    public static List<Module> getEnums() {
        return new Reflections("io.craftgate")
                .getSubTypesOf(Enum.class)
                .stream()
                .map(Elixirify::parseEnum)
                .toList();
    }

    public static List<Module> getModels() {
        return Stream.of("io.craftgate.request", "io.craftgate.response")
                .map(packageName -> new Reflections(packageName, new SubTypesScanner(false)))
                .flatMap(reflections -> reflections.getSubTypesOf(Object.class).stream())
                .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()) && !Modifier.isStatic(clazz.getModifiers()))
                .map(Elixirify::parseClass)
                .toList();
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
                .collect(Collectors.toMap(Field::getName, field -> parseType(field.getGenericType())));

        var packageName = clazz.getPackage().getName();
        var name = clazz.getSimpleName();
        return ClassModule.builder()
                .packageName(packageName)
                .name(name)
                .exposedFields(fields)
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
            return new TypeRef(clazz.getPackage().getName(), clazz.getSimpleName());
        }

        if (clazz.isAssignableFrom(List.class) || clazz.isAssignableFrom(Set.class)) {
            return new ListType();
        }

        return BasicType.TERM;
    }
}
