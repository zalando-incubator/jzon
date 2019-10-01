package org.zalando.jzon.service;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;

public class KeyMapper {
    private static final Logger LOGGER = getLogger(KeyMapper.class);

    public static Set<String> mappedKeysOf(final Map<String, String> mappedKnownKeys, final Set<String> keys) {
        final Set<String> mappedKeys = new HashSet<>();
        for (final String key : keys) {
            mappedKeys.add(mappedKeyOf(mappedKnownKeys, key));
        }

        return mappedKeys;
    }

    /**
     * @param   mappedKnownKeys
     * @param   key
     *
     * @return  the value of provided key from the given map. If not found the provided {@code key} will be returned
     */
    public static String mappedKeyOf(final Map<String, String> mappedKnownKeys, final String key) {
        final String mappedKey = mappedKnownKeys.getOrDefault(key, key);
        if (Objects.equals(mappedKey, key)) {
            LOGGER.trace("No mapping was defined for [{}], hence using its value as default", key);
        } else {
            LOGGER.trace("Using the mapped key [{}] for the key [{}]", mappedKey, key);
        }

        return mappedKey;
    }

    /**
     * Goes through the knownKeyValues and for each key in that map it looks up in {@code the mappedKnownKeys} then from
     * the value set of this map it finds the key and replaces this key with the one that is in the
     * {@code mappedKnownKeys} map.
     *
     * @param   mappedKnownKeys
     * @param   knownKeyValues
     *
     * @return  the replaced map of knownKeys
     */
    public static Map<String, Object[]> mappedKnownValuesOf(final Map<String, String> mappedKnownKeys,
            final Map<String, Object[]> knownKeyValues) {
        if (knownKeyValues == null) {
            return knownKeyValues;
        }

        final Map<String, Object[]> mappedKnownKeyValues = new HashMap<>();
        for (final Entry<String, Object[]> knownEntry : knownKeyValues.entrySet()) {
            mappedKnownKeyValues.put(getKeyByMappedKnownKey(mappedKnownKeys, knownEntry.getKey()),
                knownEntry.getValue());
        }

        return mappedKnownKeyValues;
    }

    /**
     * Goes through the knownKeyValues and for each key in that map it looks up in {@code the mappedKnownKeys} then from
     * the value set of this map it finds the key and replaces this key with the one that is in the
     * {@code mappedKnownKeys} map.
     *
     * @param   mappedKnownKeys
     * @param   knownKeyValues
     *
     * @return  the replaced map of known values
     */
    public static List<Map<String, Object>> mappedKnownValuesOf(final Map<String, String> mappedKnownKeys,
            final List<Map<String, Object>> knownKeyValues) {
        if (knownKeyValues == null) {
            return knownKeyValues;
        }

        final List<Map<String, Object>> mappedKnownKeyValues = new ArrayList<>();
        for (final Map<String, Object> groupItem : knownKeyValues) {
            final Map<String, Object> mappedKnownEntry = new HashMap<>();
            for (final Entry<String, Object> knownEntry : groupItem.entrySet()) {
                mappedKnownEntry.put(getKeyByMappedKnownKey(mappedKnownKeys, knownEntry.getKey()),
                    knownEntry.getValue());
            }

            mappedKnownKeyValues.add(mappedKnownEntry);
        }

        return mappedKnownKeyValues;
    }

    /**
     * Goes through the knownKeyValues and for each key in that map it looks up in {@code the mappedKnownKeys} then from
     * the value set of this map it finds the key and replaces this key with the one that is in the
     * {@code mappedKnownKeys} map.
     *
     * @param   mappedKnownKeys
     * @param   knownKeyValues
     *
     * @return  the map of known values
     */
    public static Map<String, Set<Object>> mappedKnownValuesFrom(final Map<String, String> mappedKnownKeys,
            final Map<String, Set<Object>> knownKeyValues) {
        if (knownKeyValues == null) {
            return knownKeyValues;
        }

        final Map<String, Set<Object>> mappedKnownKeyValues = new HashMap<>();
        for (final Entry<String, Set<Object>> knownEntry : knownKeyValues.entrySet()) {
            mappedKnownKeyValues.put(getKeyByMappedKnownKey(mappedKnownKeys, knownEntry.getKey()),
                knownEntry.getValue());
        }

        return mappedKnownKeyValues;
    }

    /**
     * Returns the key of the provided {@code value} from the {@code mappedKnownKeys}. The mapping should be one-one
     * otherwise it just returns the first found key of a value.
     *
     * @param   mappedKnownKeys  the map to look from
     * @param   value            from map
     *
     * @return  key of the provided payload from the map.
     */
    static String getKeyByMappedKnownKey(final Map<String, String> mappedKnownKeys, final String value) {
        for (final Entry<String, String> entry : mappedKnownKeys.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                LOGGER.trace("Found the key [{}] looked up by its value [{}]", entry.getKey(), value);
                return entry.getKey();
            }
        }

        LOGGER.trace("No mapping was provided for the value [{}], hence returning this as the key", value);
        return value;
    }

}
