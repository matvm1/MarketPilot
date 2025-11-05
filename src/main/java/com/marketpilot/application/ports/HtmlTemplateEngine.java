package com.marketpilot.application.ports;

import java.util.Map;

public interface HtmlTemplateEngine {
    String render(String templateName, Map<String, Object> vars);
}
