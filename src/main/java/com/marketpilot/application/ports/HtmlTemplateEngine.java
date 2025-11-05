package com.marketpilot.application.ports;

import java.io.IOException;
import java.util.Map;

public interface HtmlTemplateEngine {
    String render(String templateName, Map<String, Object> vars) throws IOException;
}
