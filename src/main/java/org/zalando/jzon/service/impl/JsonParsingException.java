package org.zalando.jzon.service.impl;

public class JsonParsingException extends RuntimeException {

    private static final long serialVersionUID = 3934126501742945094L;

    public JsonParsingException() {
        super();
    }

    public JsonParsingException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public JsonParsingException(final String message) {
        super(message);
    }

    public JsonParsingException(final Throwable cause) {
        super(cause);
    }

}
