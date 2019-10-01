package org.zalando.jzon.service.impl;

import org.zalando.jzon.service.PayloadKeyModifier;
import org.zalando.jzon.service.PayloadKeyParser;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class PayloadKeyFactory {

    public PayloadKeyParser getPayloadKeyParser(final ObjectMapper objectMapper) {
        return new DefaultPayloadKeyParser(objectMapper);
    }

    public PayloadKeyModifier getPayloadKeyModifier() {
        return new DefaultKeyModifier();
    }
}
