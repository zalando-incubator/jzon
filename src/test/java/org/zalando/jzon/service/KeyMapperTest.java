package org.zalando.jzon.service;

import static java.util.Collections.emptyMap;

import static org.assertj.core.api.Assertions.assertThat;

import static org.zalando.jzon.service.KeyMapper.getKeyByMappedKnownKey;
import static org.zalando.jzon.service.KeyMapper.mappedKeyOf;
import static org.zalando.jzon.service.KnownKeys.KEY_CUSTOMER_NUMBER;
import static org.zalando.jzon.service.KnownKeys.KEY_SIMPLE_SKU;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class KeyMapperTest {

    private static final String KEY_PRODUCT_ID = "product_id";
    private static final String KEY_SIZE = "size";

    @Test
    public void mappedKeyOf_returnsTheKeyItself_ifTheKnownMappedKeysDoesNotContainTheProvidedKey() {
        final String mappedKey = mappedKeyOf(emptyMap(), KEY_CUSTOMER_NUMBER);

        assertThat(mappedKey).isEqualTo(KEY_CUSTOMER_NUMBER);
    }

    @Test
    public void mappedKeyOf_returnsTheMappedValue_ifThereExistsAnEntryForTheProvidedKey() {
        final Map<String, String> mappedKnownKeys = new HashMap<>();
        mappedKnownKeys.put(KEY_SIMPLE_SKU, KEY_PRODUCT_ID);

        final String mappedKey = mappedKeyOf(mappedKnownKeys, KEY_SIMPLE_SKU);

        assertThat(mappedKey).isEqualTo(KEY_PRODUCT_ID);
    }

    @Test
    public void getKeyByValue_returnsTheCorrectKey_ifThereExistsAMapping() {
        final Map<String, String> mappedKnownKeys = new HashMap<>();
        mappedKnownKeys.put(KEY_SIMPLE_SKU, KEY_PRODUCT_ID);

        final String mappedKey = getKeyByMappedKnownKey(mappedKnownKeys, KEY_PRODUCT_ID);

        assertThat(mappedKey).isEqualTo(KEY_SIMPLE_SKU);
    }

    @Test
    public void getKeyByValue_returnsTheProvidedValue_ifNoMappingIsProvidedForThatValue() {
        final Map<String, String> mappedKnownKeys = new HashMap<>();
        mappedKnownKeys.put(KEY_SIMPLE_SKU, KEY_PRODUCT_ID);

        final String mappedKey = getKeyByMappedKnownKey(mappedKnownKeys, KEY_SIZE);

        assertThat(mappedKey).isEqualTo(KEY_SIZE);

    }
}
