package com.marketpilot.adapters.persistence;

import com.marketpilot.application.ports.HtmlTemplateEngine;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.loader.FileLoader;
import io.pebbletemplates.pebble.template.PebbleTemplate;

import java.util.Map;

public class PebbleHtmlTemplateEngine implements HtmlTemplateEngine {
    private static PebbleHtmlTemplateEngine instance;
    private static PebbleEngine pebbleEngine;

    private PebbleHtmlTemplateEngine() {
        FileLoader loader = new FileLoader();
        loader.setPrefix(System.getenv("TEMPLATES_PATH"));
        pebbleEngine = new PebbleEngine.Builder()
                .loader(new FileLoader())
                .build();
    }

    @Override
    public String render(String templateName, Map<String, Object> vars) {
        PebbleTemplate pebbleTemplate = pebbleEngine.getTemplate(templateName);
        return "";
    }

    public static PebbleHtmlTemplateEngine getInstance() {
        if (instance == null)
            instance = new PebbleHtmlTemplateEngine();
        return instance;
    }
}
