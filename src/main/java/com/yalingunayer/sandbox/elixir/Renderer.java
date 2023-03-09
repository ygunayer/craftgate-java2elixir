package com.yalingunayer.sandbox.elixir;

import com.yalingunayer.sandbox.Elixirify;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
public class Renderer {

    private final Configuration cfg;

    private String renderTemplate(String templateName, Map<String, Object> data) throws Exception {
        try (var os = new ByteArrayOutputStream();
             var writer = new OutputStreamWriter(os)
        ) {
            var template = cfg.getTemplate(templateName);
            var beans = new BeansWrapper(Configuration.VERSION_2_3_32);
            var statics = beans.getStaticModels();
            var actualData = new HashMap<>(data);
            actualData.put("statics", statics);
            template.process(actualData, writer);
            return os.toString();
        }
    }

    private String renderClass(ClassModule module) throws Exception {
        return this.renderTemplate("class.ftl", Map.of("class", module));
    }

    private String renderEnum(EnumModule module) throws Exception {
        return this.renderTemplate("enum.ftl", Map.of("enum", module));
    }

    public String render(Namespace namespace) throws Exception {
        return this.renderTemplate("namespace.ftl", Map.of("namespace", namespace));
    }

    public String render(Module module) throws Exception {
        return switch (module.getType()) {
            case ENUM -> this.renderEnum((EnumModule) module);
            case CLASS -> this.renderClass((ClassModule) module);
        };
    }

    public static Renderer createDefault() throws URISyntaxException, IOException {
        var cfg = new Configuration(Configuration.VERSION_2_3_32);

        var rootUri = Objects.requireNonNull(Elixirify.class.getClassLoader().getResource("templates")).toURI();
        cfg.setDirectoryForTemplateLoading(Paths.get(rootUri).toFile());
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        return new Renderer(cfg);
    }
}
