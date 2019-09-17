package org.zalando.jzon;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;

import java.io.IOException;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class SerializationHelper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.setPropertyNamingStrategy(SNAKE_CASE);
        objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setSerializationInclusion(NON_EMPTY);
    }

    private SerializationHelper() {
        super();
    }

    public static String toJSON(final Object object) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }

    public static Map<String, Object> convertToMap(final String json) throws IOException {
        return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() { });
    }

    public static byte[] toByteArray(final Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsBytes(object);
    }

    public static <T> T fromJSON(final String json, final Class<T> clazz) throws IOException {
        return objectMapper.readValue(json, clazz);
    }

    public static <T> T fromByteArray(final byte[] bytes, final Class<T> clazz) throws IOException {
        return objectMapper.readValue(bytes, clazz);
    }

    public static ObjectMapper objectMapperForTest() {
        return objectMapper;
    }
}
