package org.zalando.jzon.service.impl;

import static java.lang.String.format;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;
import static org.apache.commons.lang3.ArrayUtils.toArray;
import static org.apache.commons.lang3.StringUtils.isBlank;

import static org.slf4j.LoggerFactory.getLogger;

import static org.zalando.jzon.service.KeyMapper.mappedKeyOf;
import static org.zalando.jzon.service.KeyMapper.mappedKeysOf;
import static org.zalando.jzon.service.KeyMapper.mappedKnownValuesFrom;
import static org.zalando.jzon.service.KeyMapper.mappedKnownValuesOf;

import static com.google.common.collect.Sets.newHashSet;

import static com.jayway.jsonpath.Configuration.builder;
import static com.jayway.jsonpath.JsonPath.using;
import static com.jayway.jsonpath.Option.SUPPRESS_EXCEPTIONS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;

import org.zalando.jzon.service.PayloadKeyParser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;

import net.minidev.json.JSONArray;

/**
 * @author  Sina Golesorkhi(sina.golesorkhi@zalando.de)
 */
public final class DefaultPayloadKeyParser implements PayloadKeyParser {

    private static final Logger LOGGER = getLogger(DefaultPayloadKeyParser.class);
    private static final String ALL_FROM_ROOT_OP = "$.*";
    private static final String ROOT_ELEMENT = "$";
    private static final String EMPTY_STRING = "";
    private static final Configuration PATH_CONFIGURATION = builder().options(Option.AS_PATH_LIST).build();
    private static final Configuration PATH_CONFIGURATION_SUPPRESS_EXCEPTIONS = builder().options(Option.AS_PATH_LIST,
            SUPPRESS_EXCEPTIONS).build();
    private static final String ROOT_SCAN_OP = "$.";
    private static final String DEEP_SCAN_OP = "$..";
    private static final String IMMEDIATE_OBJECT_SCAN = "scanTheImmediateObject";
    private static final String IMMEDIATE_ARRAY_SCAN = "scanTheImmediateArray";
    private final ObjectMapper objectMapper;
    private static final Pattern ROOT_ELEMENT_PATTERN = Pattern.compile(ROOT_ELEMENT, Pattern.LITERAL);

    DefaultPayloadKeyParser(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, Object[]> parse(final Set<String> keys, final Map<String, String> mappedKnownKeys,
            final Map<String, Object> jsonPayload, final boolean asPathList) {
        try {
            final Set<String> mappedKeys = mappedKeysOf(mappedKnownKeys, keys);
            final Map<String, Object[]> knownKeyValues = parse(mappedKeys, objectMapper.writeValueAsString(jsonPayload),
                    asPathList);
            return mappedKnownValuesOf(mappedKnownKeys, knownKeyValues);
        } catch (final JsonProcessingException e) {
            throw new JsonParsingException(format("Cannot serialize payload [%s]", jsonPayload), e);
        }
    }

    @Override
    public Map<String, Object[]> parse(final Set<String> keys, final Map<String, String> mappedKnownKeys,
            final String jsonPayload, final boolean asPathList) {
        final Set<String> mappedKeys = mappedKeysOf(mappedKnownKeys, keys);
        final Map<String, Object[]> knownKeyValues = parse(mappedKeys, jsonPayload, asPathList);
        return mappedKnownValuesOf(mappedKnownKeys, knownKeyValues);
    }

    Map<String, Object[]> parse(final Set<String> keys, final String jsonPayload, final boolean asPathList) {
        if (isBlank(jsonPayload)) {
            return emptyMap();
        }

        final Map<String, Object[]> resolvedKeyValues = new HashMap<>();

        for (final String key : keys) {
            if (isBlank(key)) {
                throw new IllegalArgumentException("Blank key is provided!");
            }

            try {
                Object result = getPathOrValueForExactKeyAnywhereInJson(key, jsonPayload, asPathList);

                if (result == null) {
                    result = getPathOrValueForKeyNestedAndBrokenInJson(key, jsonPayload, asPathList);
                }

                if (result != null) {
                    if (result instanceof JSONArray) {
                        final JSONArray castedResult = (JSONArray) result;
                        if (!castedResult.isEmpty()) {
                            resolvedKeyValues.put(key, castedResult.toArray());
                        }
                    } else {
                        resolvedKeyValues.put(key, toArray(result));
                    }
                }

            } catch (@SuppressWarnings("unused") final InvalidJsonException e) {
                LOGGER.trace("Cannot find key [{}] or its nested version in payload [{}]", key, jsonPayload);
            }
        }

        return resolvedKeyValues;
    }

    @Override
    public Map<String, Set<Object>> parseUnique(final Set<String> keys, final Map<String, String> mappedKnownKeys,
            final Map<String, Object> payload) {
        try {
            final Set<String> mappedKeys = mappedKeysOf(mappedKnownKeys, keys);
            final Map<String, Object[]> duplicatedResult = parse(mappedKeys, objectMapper.writeValueAsString(payload),
                    false);

            //J-
            final Map<String, Set<Object>> knownKeyValues = duplicatedResult.entrySet().stream()
                                              .collect(toMap(Map.Entry::getKey,
                                                      e -> Stream.of(e.getValue()).collect(toSet())));
            //J+
            return mappedKnownValuesFrom(mappedKnownKeys, knownKeyValues);
        } catch (final JsonProcessingException e) {
            throw new JsonParsingException(format("Cannot serialize payload [%s]", payload), e);
        }
    }

    @Override
    public List<Map<String, Object>> parseGrouped(final String groupKey, final Set<String> mandatoryKeys,
            final Set<String> optionalKeys, final Map<String, String> mappedKnownKeys,
            final Map<String, Object> payload) {
        try {
            final String mappedGroupKey = mappedKeyOf(mappedKnownKeys, groupKey);
            final Set<String> mappedMandatoryKeys = mappedKeysOf(mappedKnownKeys, mandatoryKeys);
            final Set<String> mappedOptionalKeys = mappedKeysOf(mappedKnownKeys, optionalKeys);
            final List<Map<String, Object>> knownKeyValues = parseGrouped(mappedGroupKey, mappedMandatoryKeys,
                    mappedOptionalKeys, objectMapper.writeValueAsString(payload));

            return mappedKnownValuesOf(mappedKnownKeys, knownKeyValues);
        } catch (final JsonProcessingException e) {
            throw new JsonParsingException(format("Cannot serialize payload [%s]", payload), e);
        }

    }

    List<Map<String, Object>> parseGrouped(final String groupKey, final Set<String> mandatoryKeys,
            final Set<String> optionalKeys, final String payload) {
        final List<Map<String, Object>> foundItemsFromValues = new ArrayList<>();
        final Object[] pathsForGroupKey = parse(newHashSet(groupKey), payload, AS_PATH_LIST).getOrDefault(groupKey,
                emptyList().toArray());
        for (final Object path : pathsForGroupKey) {
            final Map<String, Object> valuesForMandatoryKeys = new HashMap<>();
            final Map<String, Object> valuesForOptionalKeys = new HashMap<>();
            for (final String key : mandatoryKeys) {
                final Object keyValue = resolveKeyValueSameLevelAsGroupKey(groupKey, payload, (String) path, key);
                if (keyValue != null) {
                    valuesForMandatoryKeys.put(key, keyValue);
                }
            }

            for (final String key : optionalKeys) {
                final Object keyValue = resolveKeyValueSameLevelAsGroupKey(groupKey, payload, (String) path, key);
                if (keyValue != null) {
                    valuesForOptionalKeys.put(key, keyValue);
                }
            }

            if (valuesForMandatoryKeys.values().size() == mandatoryKeys.size()) {
                valuesForMandatoryKeys.putAll(valuesForOptionalKeys);
                valuesForMandatoryKeys.put(groupKey, JsonPath.parse(payload).read(((String) path)));
                foundItemsFromValues.add(valuesForMandatoryKeys);
            }
        }

        return foundItemsFromValues;
    }

    private Object resolveKeyValueSameLevelAsGroupKey(final String groupKey, final String payload, final String path,
            final String key) {
        final String keyPath = path.replace(groupKey, key);
        Object keyValue = null;
        try {
            keyValue = JsonPath.parse(payload).read(keyPath);
        } catch (@SuppressWarnings("unused") final PathNotFoundException e) {
            LOGGER.debug("Path [{}] was not found", keyPath);
        }

        return keyValue;
    }

    /**
     * Searches for the exact given {@code key} no matter whether it is at the root of the payload or nested somewhere
     * in it. e.g: looking for {@code customer_number} as key
     *
     * <pre>
       <code>
      {
        "size": 27,
        "customer_number": "12345"
      }
       </code>
       OR

      {
        "size": 27,
        "customer": {
            "customer_number": "12345"
        }
      }
       </code>
     * </pre>
     *
     * @param   key
     * @param   jsonPayload
     * @param   asPathList   if {@code true} the function will look for the path of the {@code key} otherwise for the
     *                       value
     *
     * @return  the found value for the given {@code key}
     */
    private Object getPathOrValueForExactKeyAnywhereInJson(final String key, final String jsonPayload,
            final boolean asPathList) {
        final Object result;

        if (asPathList) {
            try {
                result = using(PATH_CONFIGURATION).parse(jsonPayload).read(DEEP_SCAN_OP + key);
            } catch (@SuppressWarnings("unused") final PathNotFoundException e) {
                return null;
            }
        } else {
            result = JsonPath.parse(jsonPayload).read(DEEP_SCAN_OP + key);
        }

        final JSONArray resultArray = (JSONArray) result;
        if (!resultArray.isEmpty()) {
            return resultArray;
        }

        return null;
    }

    /**
     * Looks whether the consists of more than one part. If so it looks for the first part in the payload and then for
     * the second part in the value of first part. e.g: looking for {@code customer_number} as key
     *
     * <pre>
       <code>
      {
        "size": 27,
        "customer": {
            "number": "12345"
        }
      }
       </code>
     * </pre>
     *
     * Note that this method only looks two levels deep in the payload.
     *
     * @param   key
     * @param   jsonPayload
     * @param   asPathList   if {@code true} the function will look for the path of the {@code key} otherwise for the
     *                       value
     *
     * @return
     */
    private Object getPathOrValueForKeyNestedAndBrokenInJson(final String key, final String jsonPayload,
            final boolean asPathList) {
        final String[] splitPayloadKey = key.split(SEPARATOR);

        if (splitPayloadKey.length != 2) {
            if (splitPayloadKey.length == 1 && key.contains(SEPARATOR)) {
                LOGGER.error("The provided key [{}] is malformed", key);
            }

            return null;
        }

        if (isNotEmpty(splitPayloadKey)) {
            if (asPathList) {
                return resolvePath(splitPayloadKey, jsonPayload);
            }

            return resolveValue(splitPayloadKey, jsonPayload);
        }

        return null;
    }

    private Object resolveValue(final String[] splitPayloadKey, final String jsonPayload) {
        final JSONArray result = new JSONArray();
        JSONArray partiallyFoundKeys = null;
        try {
            partiallyFoundKeys = JsonPath.parse(jsonPayload).read(DEEP_SCAN_OP + splitPayloadKey[0]);
            resolveSecondKeyUsing(IMMEDIATE_OBJECT_SCAN, splitPayloadKey, result, partiallyFoundKeys);
        } catch (@SuppressWarnings("unused") final PathNotFoundException e) {
            if (partiallyFoundKeys == null || partiallyFoundKeys.isEmpty()) {
                return result;
            }

            resolveSecondKeyUsing(IMMEDIATE_ARRAY_SCAN, splitPayloadKey, result, partiallyFoundKeys);
        }

        return result;
    }

    private void resolveSecondKeyUsing(final String scanType, final String[] splitPayloadKey, final JSONArray result,
            final JSONArray partiallyFoundKeys) {
        String operation = null;
        if (IMMEDIATE_OBJECT_SCAN.equals(scanType)) {
            operation = ROOT_SCAN_OP;
        }

        if (IMMEDIATE_ARRAY_SCAN.equals(scanType)) {
            operation = ALL_FROM_ROOT_OP;

        }

        for (final Object partiallyFoundKey : partiallyFoundKeys) {
            final Object partialResult = JsonPath.parse(partiallyFoundKey).read(operation + splitPayloadKey[1]);

            if (partialResult instanceof JSONArray) {
                result.addAll((JSONArray) partialResult);
            } else {
                result.add(partialResult);
            }
        }
    }

    private Object resolvePath(final String[] splitPayloadKey, final String jsonPayload) {
        final JSONArray result = new JSONArray();
        JSONArray partiallyFoundKeys = null;
        JSONArray partiallyFoundPath = null;
        try {
            partiallyFoundKeys = JsonPath.parse(jsonPayload).read(DEEP_SCAN_OP + splitPayloadKey[0]);
            partiallyFoundPath = using(PATH_CONFIGURATION).parse(jsonPayload).read(DEEP_SCAN_OP + splitPayloadKey[0]);

            for (int i = 0; i < partiallyFoundKeys.size(); i++) {
                final JSONArray subPath = using(PATH_CONFIGURATION).parse(partiallyFoundKeys.get(i)).read(ROOT_SCAN_OP
                            + splitPayloadKey[1]);

                final Object partialResult = buildFullPathOf(partiallyFoundPath.get(i), subPath);
                if (partialResult instanceof JSONArray) {
                    result.addAll((JSONArray) partialResult);
                } else {
                    result.add(partialResult);
                }
            }
        } catch (@SuppressWarnings("unused") final PathNotFoundException e) {
            if (partiallyFoundKeys == null || partiallyFoundKeys.isEmpty() || partiallyFoundPath == null
                    || partiallyFoundPath.isEmpty()) {
                return result;
            }

            for (int i = 0; i < partiallyFoundKeys.size(); i++) {
                final Object partialResult = buildFullPathOf(partiallyFoundPath.get(i),
                        using(PATH_CONFIGURATION_SUPPRESS_EXCEPTIONS).parse(partiallyFoundKeys.get(i)).read(
                            ALL_FROM_ROOT_OP + splitPayloadKey[1]));
                if (partialResult instanceof JSONArray) {
                    result.addAll((JSONArray) partialResult);
                } else {
                    result.add(partialResult);
                }
            }
        }

        return result;
    }

    private Object buildFullPathOf(final Object partiallyFoundPath, final JSONArray subPathArray) {
        //J-
        final List<String> subPaths = subPathArray.stream()
            .map(subEntry -> String.valueOf(partiallyFoundPath)
                + ROOT_ELEMENT_PATTERN.matcher(String.valueOf(subEntry)).replaceAll(EMPTY_STRING))
            .collect(Collectors.toList());
        //J+

        final JSONArray resultPath = new JSONArray();
        resultPath.addAll(subPaths);
        return resultPath;

    }

}
