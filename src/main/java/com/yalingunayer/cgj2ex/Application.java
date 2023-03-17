package com.yalingunayer.cgj2ex;

import com.yalingunayer.cgj2ex.elixir.Module;
import com.yalingunayer.cgj2ex.elixir.Namespace;
import com.yalingunayer.cgj2ex.elixir.Renderer;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class Application {
    static class Convert implements Callable<Integer> {
        @CommandLine.Option(names = { "-o", "--output" }, description = "Path to the root folder to write output to", defaultValue = "./output")
        Path rootPath;

        @CommandLine.Option(names = "--ignore", description = "Packages to ignore when generating modules")
        Set<String> ignoredPackages = Set.of(
                "io.craftgate.net",
                "io.craftgate.request.common",
                "io.craftgate.response.common"
        );

        @Override
        public Integer call() throws Exception {
            System.out.printf("Using root path %s%n", rootPath);

            var allObjects = Elixirify
                    .getAllModules()
                    .stream()
                    .filter(module -> !ignoredPackages.contains(module.getPackageName()))
                    .collect(Collectors.groupingBy(Module::getPackageName));

            var renderer = Renderer.createDefault();

            allObjects
                    .keySet()
                    .stream()
                    .map(name -> new Namespace(Utils.packageNameToNamespace(name), allObjects.get(name)))
                    .forEach(namespace -> {
                        try {
                            Elixirify.writeNamespace(renderer, rootPath, namespace);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });

            System.out.println("Successfully finished writing modules");
            return 0;
        }
    }

    public static void main(String[] args) {
        var exitCode = new CommandLine(new Convert()).execute(args);
        System.exit(exitCode);
    }
}
