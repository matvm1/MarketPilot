package com.marketpilot.adapters;

import com.marketpilot.application.ports.HtmlTemplateEngine;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.loader.FileLoader;
import io.pebbletemplates.pebble.template.PebbleTemplate;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

public class PebbleHtmlTemplateEngine implements HtmlTemplateEngine {
    private static PebbleHtmlTemplateEngine instance;
    private static PebbleEngine pebbleEngine;

    private PebbleHtmlTemplateEngine() {
        FileLoader loader = new FileLoader();
        loader.setPrefix(System.getenv("TEMPLATES_PATH"));
        loader.setSuffix(".html");
        pebbleEngine = new PebbleEngine.Builder()
                .loader(loader)
                .build();
    }

    @Override
    public String render(String templateName, Map<String, Object> vars) throws IOException {
        PebbleTemplate template = pebbleEngine.getTemplate(templateName);
        Writer writer = new StringWriter();
        template.evaluate(writer, vars);
        return writer.toString();
    }

    public static PebbleHtmlTemplateEngine getInstance() {
        if (instance == null)
            instance = new PebbleHtmlTemplateEngine();
        return instance;
    }
}
