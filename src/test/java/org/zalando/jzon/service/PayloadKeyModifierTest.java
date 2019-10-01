package org.zalando.jzon.service;

import static org.assertj.core.api.Assertions.assertThat;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.LENIENT;

import static org.zalando.jzon.SerializationHelper.convertToMap;
import static org.zalando.jzon.SerializationHelper.toJSON;

import static com.google.common.collect.Sets.newHashSet;

import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.zalando.jzon.service.impl.PayloadKeyFactory;

public class PayloadKeyModifierTest {

    private PayloadKeyModifier payloadKeyModifier;

    @Before
    public void setup() {
        payloadKeyModifier = new PayloadKeyFactory().getPayloadKeyModifier();
    }

    @Test
    public void removeKeysWithPaths_removesTheProvidedKeys_ifTheyAreFoundAndAreAtRoot() throws Exception {
        final Set<String> toBeRemovedKeyPaths = newHashSet("$.['items']", "$.['customer_address']");

        //J-
        final String jsonPayload = "{"
                +"\"size\": \"27\","
                +"\"customer_address\": \"jon.doe@zalando.de\","
                +"\"items\": [{"
                +    "\"simple_sku\": \"JA222H085-C11000S000\""
                +    "},{"
                +    "\"simple_sku\": \"BA112A01B-B11000M000\""
                + "}]"
                +"}";

        //J+
        final Map<String, Object> payload = convertToMap(jsonPayload);

        payloadKeyModifier.removeKeysWithPaths(toBeRemovedKeyPaths, payload);

        assertThat(payload.keySet()).containsOnly("size");
    }

    @Test
    public void removeKeysWithPaths_doesNothing_ifTheKeyIsInThePayloadButUnderAnotherPath() throws Exception {
        final Set<String> toBeRemovedKeyPaths = newHashSet("$.['simple_sku']");

        //J-
        final String jsonPayload = "{"
                +"\"items\": [{"
                +    "\"simple_sku\": \"JA222H085-C11000S000\""
                +    "},{"
                +    "\"simple_sku\": \"BA112A01B-B11000M000\""
                + "}]"
                +"}";

        //J+
        final Map<String, Object> payload = convertToMap(jsonPayload);

        payloadKeyModifier.removeKeysWithPaths(toBeRemovedKeyPaths, payload);

        assertEquals(toJSON(payload), jsonPayload, LENIENT);
    }

    @Test
    public void removeKeysWithPaths_doesNothing_ifThePathDoesNotExistInPayload() throws Exception {
        final Set<String> toBeRemovedKeyPaths = newHashSet("$.['any_non_existent_key']");

        //J-
        final String jsonPayload = "{"
                +"\"size\": \"27\""
                +"}";

        //J+
        final Map<String, Object> payload = convertToMap(jsonPayload);

        payloadKeyModifier.removeKeysWithPaths(toBeRemovedKeyPaths, payload);

        assertThat(payload.keySet()).containsOnly("size");
    }
}
