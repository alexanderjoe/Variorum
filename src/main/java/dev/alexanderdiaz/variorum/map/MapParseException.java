package dev.alexanderdiaz.variorum.map;

import lombok.Getter;

@Getter
public class MapParseException extends RuntimeException {
    private final String section;
    private final String xmlContext;

    public MapParseException(String message) {
        super(message);
        this.section = "unknown";
        this.xmlContext = null;
    }

    public MapParseException(String message, String section) {
        super(message);
        this.section = section;
        this.xmlContext = null;
    }

    public MapParseException(String message, String section, String xmlContext) {
        super(message);
        this.section = section;
        this.xmlContext = xmlContext;
    }

    public MapParseException(String message, String section, String xmlContext, Throwable cause) {
        super(message, cause);
        this.section = section;
        this.xmlContext = xmlContext;
    }

    @Override
    public String getMessage() {
        StringBuilder message = new StringBuilder();
        message.append("Error parsing map section '").append(section).append("': ");
        message.append(super.getMessage());

        if (xmlContext != null && !xmlContext.isEmpty()) {
            message.append("\nProblematic XML: ").append(xmlContext);
        }

        return message.toString();
    }
}
