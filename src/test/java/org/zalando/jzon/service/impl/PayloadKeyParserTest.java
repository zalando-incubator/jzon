package org.zalando.jzon.service.impl;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

import static org.apache.commons.lang3.ArrayUtils.toArray;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.zalando.jzon.SerializationHelper.convertToMap;
import static org.zalando.jzon.service.KnownKeys.KEY_CUSTOMER_ADDRESS;
import static org.zalando.jzon.service.KnownKeys.KEY_CUSTOMER_EMAIL;
import static org.zalando.jzon.service.KnownKeys.KEY_CUSTOMER_HASH;
import static org.zalando.jzon.service.KnownKeys.KEY_CUSTOMER_NUMBER;
import static org.zalando.jzon.service.KnownKeys.KEY_LAST_NAME;
import static org.zalando.jzon.service.KnownKeys.KEY_SIMPLE_SKU;
import static org.zalando.jzon.service.PayloadKeyParser.AS_PATH_LIST;
import static org.zalando.jzon.service.PayloadKeyParser.AS_VALUE_LIST;
import static org.zalando.jzon.service.util.CollectionUtil.firstStringValueOf;

import static com.google.common.collect.Sets.newHashSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.springframework.boot.test.rule.OutputCapture;

import org.zalando.jzon.service.PayloadKeyParser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.common.collect.ImmutableMap;

/**
 * @author  Sina Golesorkhi(sina.golesorkhi@zalando.de)
 */
public class PayloadKeyParserTest {

    private static final String KEY_SIZE = "size";
    private PayloadKeyParserImpl payloadKeyParserImpl;
    private PayloadKeyParser payloadKeyParser;
    private final String expectedCustomerNumber = "12345";
    private final String expectedCustomerHash = "12345anyStupidHash";
    private ObjectMapper objectMapper = new ObjectMapper();
    @Rule
    public final OutputCapture capture = new OutputCapture();

    @Before
    public void setup() {
        payloadKeyParserImpl = new PayloadKeyParserImpl(objectMapper);
        payloadKeyParser = new PayloadKeyFactory().getPayloadKeyParser(objectMapper);
    }

    @Test(expected = JsonParsingException.class)
    public void parse_throwsJsonParsingExceptionn_ifPayloadCannotBeSerialized() throws Exception {
        objectMapper = mock(ObjectMapper.class);
        payloadKeyParser = new PayloadKeyParserImpl(objectMapper);

        final ImmutableMap<String, Object> payload = ImmutableMap.of("customer_number", "2");

        when(objectMapper.writeValueAsString(payload)).thenThrow(JsonProcessingException.class);

        payloadKeyParser.parse(emptySet(), emptyMap(), payload, AS_VALUE_LIST);
    }

    @Test
    public void parse_returnsCorrectValues_ifTheKeyIsAtRootOfTheJson() {
        //J-
        final String jsonPayload = "{"
                +"\"customer_number\": \"12345\","
                +"\"customer_hash\": \"12345anyStupidHash\""
                +"}";
        //J+

        final Map<String, Object[]> resultMap = payloadKeyParserImpl.parse(newHashSet(KEY_CUSTOMER_NUMBER,
                    KEY_CUSTOMER_HASH), jsonPayload, AS_VALUE_LIST);

        assertEquals(expectedCustomerNumber, resultMap.get(KEY_CUSTOMER_NUMBER)[0]);
    }

    @Test
    public void parse_usesMappedKeysForLookup_ifProvidedOnMapAsPayload() throws Exception {
        //J-
        final String jsonPayload = "{"
                +"\"size\": \"27\","
                +"\"customer\": {"
                +    "\"id\": \"12345\","
                +    "\"home\": \"jon.doe@zalando.de\""
                +    "}"
                +"}";
        //J+

        final Map<String, Object> payload = convertToMap(jsonPayload);
        final Map<String, String> mappedKnownKeys = new HashMap<>();
        mappedKnownKeys.put(KEY_CUSTOMER_NUMBER, "customer_id");
        mappedKnownKeys.put(KEY_CUSTOMER_EMAIL, "customer_home");

        final Map<String, Object[]> resultMap = payloadKeyParser.parse(newHashSet(KEY_CUSTOMER_NUMBER,
                    KEY_CUSTOMER_EMAIL, KEY_SIZE), mappedKnownKeys, payload, AS_VALUE_LIST);

        assertThat(firstStringValueOf(resultMap.get(KEY_SIZE))).isEqualTo("27");
        assertThat(firstStringValueOf(resultMap.get(KEY_CUSTOMER_NUMBER))).isEqualTo("12345");
        assertThat(firstStringValueOf(resultMap.get(KEY_CUSTOMER_EMAIL))).isEqualTo("jon.doe@zalando.de");
    }

    @Test
    public void parse_usesMappedKeysForLookup_ifProvidedOnStringAsPayload() {
        //J-
        final String jsonPayload = "{"
                +"\"size\": \"27\","
                +"\"customer\": {"
                +    "\"id\": \"12345\","
                +    "\"home\": \"jon.doe@zalando.de\""
                +    "}"
                +"}";
        //J+

        final Map<String, String> mappedKnownKeys = new HashMap<>();
        mappedKnownKeys.put(KEY_CUSTOMER_NUMBER, "customer_id");
        mappedKnownKeys.put(KEY_CUSTOMER_EMAIL, "customer_home");

        final Map<String, Object[]> resultMap = payloadKeyParser.parse(newHashSet(KEY_CUSTOMER_NUMBER,
                    KEY_CUSTOMER_EMAIL, KEY_SIZE), mappedKnownKeys, jsonPayload, AS_VALUE_LIST);

        assertThat(firstStringValueOf(resultMap.get(KEY_SIZE))).isEqualTo("27");
        assertThat(firstStringValueOf(resultMap.get(KEY_CUSTOMER_NUMBER))).isEqualTo("12345");
        assertThat(firstStringValueOf(resultMap.get(KEY_CUSTOMER_EMAIL))).isEqualTo("jon.doe@zalando.de");
    }

    @Test
    public void parse_returnsCorrectValues_ifTheKeyIsWrappedInAnObjectOnTheSecondLevel() {
        //J-
        final String jsonPayload = "{"
                                    +"\"size\": \"27\","
                                    +"\"customer\": {"
                                    +    "\"customer_number\": \"12345\","
                                    +    "\"customer_hash\": \"12345anyStupidHash\""
                                    +    "}"
                                  +"}";
        //J+

        final Map<String, Object[]> resultMap = payloadKeyParserImpl.parse(newHashSet(KEY_CUSTOMER_NUMBER,
                    KEY_CUSTOMER_HASH), jsonPayload, AS_VALUE_LIST);

        assertEquals(expectedCustomerNumber, resultMap.get(KEY_CUSTOMER_NUMBER)[0]);
        assertEquals(expectedCustomerHash, resultMap.get(KEY_CUSTOMER_HASH)[0]);
    }

    @Test
    public void parse_returnsNullForTheKey_ifTheKeyIsNotFound() {
        //J-
        final String jsonPayload = "{"
                +"\"size\": \"27\","
                +"\"customer\": {"
                +    "\"customer_hash\": \"12345anyStupidHash\""
                +    "}"
                +"}";
        //J+

        final Map<String, Object[]> resultMap = payloadKeyParserImpl.parse(newHashSet(KEY_CUSTOMER_NUMBER,
                    KEY_CUSTOMER_HASH), jsonPayload, AS_VALUE_LIST);

        assertNull(resultMap.get(KEY_CUSTOMER_NUMBER));
        assertEquals(expectedCustomerHash, resultMap.get(KEY_CUSTOMER_HASH)[0]);
    }

    @Test
    public void parse_returnsEmptyMap_forEmptyPayload() {
        final String jsonPayload = " ";

        final Map<String, Object[]> resultMap = payloadKeyParserImpl.parse(newHashSet(KEY_CUSTOMER_NUMBER), jsonPayload,
                AS_VALUE_LIST);

        assertEquals(emptyMap(), resultMap);
    }

    @Test
    public void parse_returnsAnArray_ifMoreThanOneOccurrenceOfAKeyIsFound() {
        final Set<String> expectedSimpleSkus = newHashSet("JA222H085-C11000S000", "BA112A01B-B11000M000");
        //J-
        final String jsonPayload = "{"
                +"\"size\": \"27\","
                +"\"items\": [{"
                +    "\"simple_sku\": \"JA222H085-C11000S000\""
                +    "},{"
                +    "\"simple_sku\": \"BA112A01B-B11000M000\""
                + "}]"
                +"}";
        //J+

        final Map<String, Object[]> resultMap = payloadKeyParserImpl.parse(newHashSet(KEY_SIMPLE_SKU), jsonPayload,
                AS_VALUE_LIST);

        final Set<String> actualSimpleSkus = new HashSet<>();
        for (final Object simpleSku : resultMap.get(KEY_SIMPLE_SKU)) {
            actualSimpleSkus.add(Objects.toString(simpleSku));
        }

        assertEquals(expectedSimpleSkus, actualSimpleSkus);
    }

    @Test
    public void parse_returnsAnArray_ifMoreThanOneOccurrenceOfAKeyIsFoundOnTheSecondLevel() {
        final Set<String> expectedSimpleSkus = newHashSet("JA222H085-C11000S000", "BA112A01B-B11000M000");
        //J-
        final String jsonPayload = "{"
                +"\"size\": \"27\","
                +"\"simple\": [{"
                +    "\"sku\": \"JA222H085-C11000S000\""
                +    "},{"
                +    "\"sku\": \"BA112A01B-B11000M000\""
                + "}]"
                +"}";
        //J+

        final Map<String, Object[]> resultMap = payloadKeyParserImpl.parse(newHashSet(KEY_SIMPLE_SKU), jsonPayload,
                AS_VALUE_LIST);

        final Set<String> actualSimpleSkus = new HashSet<>();
        for (final Object simpleSku : resultMap.get(KEY_SIMPLE_SKU)) {
            actualSimpleSkus.add(Objects.toString(simpleSku));
        }

        assertEquals(expectedSimpleSkus, actualSimpleSkus);
    }

    @Test
    public void parse_resolvesTheValuesUsingDeepScanForTheFirstKeyPart_ifTheNestedKeyDoesNotStartFromRoot() {
        final Set<String> expectedSimpleSkus = newHashSet("JA222H085-C11000S000", "JA222H085-C11000S002",
                "BA112A01B-B11000M000", "BA112A01B-B11000M002");
        //J-
        final String jsonPayload = "{"
                +"\"size\": \"27\","
                +"\"orders\": [{"
                    +"\"simple\": [{"
                    +    "\"sku\": \"JA222H085-C11000S000\""
                    +    "},{"
                    +    "\"sku\": \"BA112A01B-B11000M000\""
                    + "}]"
                    +"},{"
                    +"\"simple\": [{"
                    +    "\"sku\": \"JA222H085-C11000S002\""
                    +    "},{"
                    +    "\"sku\": \"BA112A01B-B11000M002\""
                    + "}]"
                + "}]"
                +"}";
        //J+

        final Map<String, Object[]> resultMap = payloadKeyParserImpl.parse(newHashSet(KEY_SIMPLE_SKU), jsonPayload,
                AS_VALUE_LIST);

        final Set<String> actualSimpleSkus = new HashSet<>();
        for (final Object simpleSku : resultMap.get(KEY_SIMPLE_SKU)) {
            actualSimpleSkus.add(Objects.toString(simpleSku));
        }

        assertEquals(expectedSimpleSkus, actualSimpleSkus);
    }

    @Test
    public void parse_resolvesThePathsCorrectly_ifTheNestedKeyDoesNotStartFromRoot() {
        //J-
        final String jsonPayload = "{"
                +"\"size\": \"27\","
                +"\"orders\": [{"
                    +"\"simple\": [{"
                    +    "\"sku\": \"JA222H085-C11000S000\""
                    +    "},{"
                    +    "\"sku\": \"BA112A01B-B11000M000\""
                    + "}]"
                    +"},{"
                    +"\"simple\": [{"
                    +    "\"sku\": \"JA222H085-C11000S002\""
                    +    "},{"
                    +    "\"sku\": \"BA112A01B-B11000M002\""
                    + "}]"
                + "}]"
                +"}";
        //J+

        final Map<String, Object[]> resultMap = payloadKeyParserImpl.parse(newHashSet(KEY_SIMPLE_SKU), jsonPayload,
                AS_PATH_LIST);

        final Set<String> actualSimpleSkus = new HashSet<>();
        for (final Object simpleSku : resultMap.get(KEY_SIMPLE_SKU)) {
            actualSimpleSkus.add(Objects.toString(simpleSku));
        }

        //J-
        assertThat(actualSimpleSkus).containsExactlyInAnyOrder(
                "$['orders'][0]['simple'][0]['sku']",
                "$['orders'][0]['simple'][1]['sku']",
                "$['orders'][1]['simple'][0]['sku']",
                "$['orders'][1]['simple'][1]['sku']");
        //J+
    }

    @Test
    public void parse_onlySearchesInTheImmediateLevelForTheSecondPartOfTheKeyAfterSplit_ifTheKeyIsNestedAndItFoundTheFirstPartOfTheKey() {
        final Set<String> expectedOrderNumbers = newHashSet("55555555555", "44444444444");
        //J-
        final String jsonPayload = "{"
                +"\"size\": \"27\","
                +"\"orders\": [{"
                    +"\"customer\": [{"
                    +    "\"number\": \"1234\""
                    +    "},{"
                    +    "\"number\": \"5678\""
                    + "}],"
                    + "\"number\": \"55555555555\""
                    +"},{"
                    + "\"number\": \"44444444444\""
                    + "}]"
                +"}";
        //J+

        final Map<String, Object[]> resultMap = payloadKeyParserImpl.parse(newHashSet("orders_number"), jsonPayload,
                AS_VALUE_LIST);

        final Set<String> actualOrderNumbers = new HashSet<>();
        for (final Object simpleSku : resultMap.get("orders_number")) {
            actualOrderNumbers.add(Objects.toString(simpleSku));
        }

        assertEquals(expectedOrderNumbers, actualOrderNumbers);
    }

    @Test
    public void parse_resolvesThePathsCorrectly_ifTheKeyIsNestedAndItFoundTheFirstPartOfTheKey() {
        //J-
        final String jsonPayload = "{"
                +"\"size\": \"27\","
                +"\"orders\": [{"
                    +"\"customer\": [{"
                    +    "\"number\": \"1234\""
                    +    "},{"
                    +    "\"number\": \"5678\""
                    + "}],"
                    + "\"number\": \"55555555555\""
                    +"},{"
                    + "\"number\": \"44444444444\""
                    + "}]"
                +"}";
        //J+

        final Map<String, Object[]> resultMap = payloadKeyParserImpl.parse(newHashSet("orders_number"), jsonPayload,
                AS_PATH_LIST);

        final Set<String> actualOrderNumbers = new HashSet<>();
        for (final Object simpleSku : resultMap.get("orders_number")) {
            actualOrderNumbers.add(Objects.toString(simpleSku));
        }

        //J-
        assertThat(actualOrderNumbers).containsExactlyInAnyOrder(
                "$['orders'][0]['number']",
                "$['orders'][1]['number']");
        //J+
    }

    @Test
    public void parse_logsWithErrorAndDoesNotReturnTheKey_ifAMalformedKeyIsProvided() {
        //J-
        final String jsonPayload = "{"
                +"\"size\": \"27\","
                +"\"customer\": {"
                +    "\"customer_number\": \"12345\""
                + "}"
                +"}";
        //J+

        final String malformedKey = "customer_";
        final Map<String, Object[]> returnedMap = payloadKeyParserImpl.parse(newHashSet(malformedKey), jsonPayload,
                AS_VALUE_LIST);
        assertThat(capture.toString()).contains("The provided key [customer_] is malformed");
        assertEquals(emptyMap(), returnedMap);
    }

    @Test
    public void parse_doesNotLogErrorAndDoesNotSearchForKey_ifTheKeyHasMoreThanTwoWordsSeparatedByUnderscore() {
        //J-
        final String jsonPayload = "{"
                +"\"size\": \"27\","
                +"\"customer\": {"
                +    "\"customer_number\": \"12345\""
                + "}"
                +"}";
        //J+

        final String keyWithMoreThanTwoWordsSnakeCased = "three_words_key";
        final Map<String, Object[]> returnedMap = payloadKeyParserImpl.parse(newHashSet(
                    keyWithMoreThanTwoWordsSnakeCased), jsonPayload, AS_VALUE_LIST);

        assertThat(capture.toString()).doesNotContain("ERROR");
        assertEquals(emptyMap(), returnedMap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parse_throwsIllegalArgumentException_ifBlankKeyIsProvided() {
        //J-
        final String jsonPayload = "{"
                +"\"size\": \"27\","
                +"\"customer\": {"
                +    "\"customer_number\": \"12345\""
                + "}"
                +"}";
        //J+

        final String malformedKey = " ";
        payloadKeyParserImpl.parse(newHashSet(malformedKey), jsonPayload, AS_VALUE_LIST);
    }

    @Test
    public void parse_returnsCorrectValues_ifTheKeyHasTwoPartsAndTheValueIsWrappedInAnotherObjectUnderSecondPartOfTheKey() {
        //J-
        final String jsonPayload = "{"
                +"\"size\": \"27\","
                +"\"customer\": {"
                +    "\"number\": \"12345\","
                +    "\"hash\": \"12345anyStupidHash\""
                +    "}"
                +"}";
        //J+

        final Map<String, Object[]> resultMap = payloadKeyParserImpl.parse(newHashSet(KEY_CUSTOMER_NUMBER,
                    KEY_CUSTOMER_HASH), jsonPayload, AS_VALUE_LIST);

        assertEquals(expectedCustomerNumber, resultMap.get(KEY_CUSTOMER_NUMBER)[0]);
        assertEquals(expectedCustomerHash, resultMap.get(KEY_CUSTOMER_HASH)[0]);
    }

    @Test
    public void parse_returnsThePath_ifTheKeyIsAtTheRootAndPathIsAsked() {
        //J-
        final String jsonPayload = "{"
                +"\"customer_number\": \"12345\","
                +"\"customer_hash\": \"12345anyStupidHash\""
                +"}";
        //J+

        final Map<String, Object[]> resultMap = payloadKeyParserImpl.parse(newHashSet(KEY_CUSTOMER_NUMBER,
                    KEY_CUSTOMER_HASH), jsonPayload, AS_PATH_LIST);

        assertEquals("$['" + KEY_CUSTOMER_NUMBER + "']", resultMap.get(KEY_CUSTOMER_NUMBER)[0]);
        assertEquals("$['" + KEY_CUSTOMER_HASH + "']", resultMap.get(KEY_CUSTOMER_HASH)[0]);
    }

    @Test
    public void parse_returnsCorrectValues_ifTheKeyHasTwoPartsAndTheValueIsWrappedInAnotherObjectUnderSecondPartOfTheKeyAndPathIsAsked() {
        //J-
        final String jsonPayload = "{"
                +"\"size\": \"27\","
                +"\"customer\": {"
                +    "\"number\": \"12345\","
                +    "\"hash\": \"12345anyStupidHash\""
                +    "}"
                +"}";
        //J+

        final Map<String, Object[]> resultMap = payloadKeyParserImpl.parse(newHashSet(KEY_CUSTOMER_NUMBER,
                    KEY_CUSTOMER_HASH), jsonPayload, AS_PATH_LIST);

        assertEquals("$['customer']['number']", resultMap.get(KEY_CUSTOMER_NUMBER)[0]);
        assertEquals("$['customer']['hash']", resultMap.get(KEY_CUSTOMER_HASH)[0]);
    }

    @Test
    public void parse_returnsAnArray_ifMoreThanOneOccurrenceIsFoundForAKeyOnTheSecondLevelAndPathIsAsked() {
        //J-
        final String jsonPayload = "{"
                +"\"size\": \"27\","
                +"\"simple\": [{"
                +    "\"sku\": \"JA222H085-C11000S000\""
                +    "},{"
                +    "\"sku\": \"BA112A01B-B11000M000\""
                + "}]"
                +"}";
        //J+

        final Map<String, Object[]> resultMap = payloadKeyParserImpl.parse(newHashSet(KEY_SIMPLE_SKU), jsonPayload,
                AS_PATH_LIST);

        final Set<String> actualSimpleSkus = new HashSet<>();
        for (final Object simpleSku : resultMap.get(KEY_SIMPLE_SKU)) {
            actualSimpleSkus.add(Objects.toString(simpleSku));
        }

        assertEquals(newHashSet("$['simple'][0]['sku']", "$['simple'][1]['sku']"), actualSimpleSkus);

    }

    @Test
    public void parseGrouped_returnsEmptyList_ifTheGroupKeyIsNotFound() {
        //J-
        final String jsonPayload = "{"
                +"\"customer_email\": \"test@email.com\","
                +"\"customer_address\": \"dummyAddress\","
                +"\"customer_hash\": \"12345anyStupidHash\""
                +"}";
        //J+

        final List<Map<String, Object>> resultMap = payloadKeyParserImpl.parseGrouped(KEY_CUSTOMER_NUMBER,
                newHashSet(KEY_CUSTOMER_EMAIL, KEY_CUSTOMER_HASH), newHashSet(), jsonPayload);

        assertThat(resultMap).isEmpty();
    }

    @Test
    public void parseGrouped_returnsCorrectValues_ifTheKeyIsAtRootOfTheJson() {
        final String expectedCustomerEmail = "test@email.com";

        //J-
        final String jsonPayload = "{"
                +"\"customer_number\": \"12345\","
                +"\"customer_email\": \"test@email.com\","
                +"\"customer_address\": \"dummyAddress\","
                +"\"customer_hash\": \"12345anyStupidHash\""
                +"}";
        //J+

        final List<Map<String, Object>> resultMap = payloadKeyParserImpl.parseGrouped(KEY_CUSTOMER_NUMBER,
                newHashSet(KEY_CUSTOMER_EMAIL, KEY_CUSTOMER_HASH), newHashSet(), jsonPayload);

        assertThat(resultMap.size()).isEqualTo(1);

        final Map<String, Object> firstOrderItem = resultMap.get(0);
        assertEquals(expectedCustomerNumber, firstOrderItem.get(KEY_CUSTOMER_NUMBER));
        assertEquals(expectedCustomerHash, firstOrderItem.get(KEY_CUSTOMER_HASH));
        assertEquals(expectedCustomerEmail, firstOrderItem.get(KEY_CUSTOMER_EMAIL));
        assertNull(firstOrderItem.get(KEY_CUSTOMER_ADDRESS));
    }

    @Test
    public void parseGrouped_returnsCorrectValues_ifTheKeyIsInListJson() {
        //J-
        final String jsonPayload = "{"
                +   "order_items: ["
                +       "{"
                +           "\"customer_number\": \"12\","
                +           "\"customer_email\": \"test1@email.com\","
                +           "\"customer_address\": \"dummyAddress1\","
                +           "\"customer_hash\": \"12anyStupidHash\""
                +       "},"
                +       "{"
                +           "\"customer_number\": \"23\","
                +           "\"customer_email\": \"test2@email.com\","
                +           "\"customer_address\": \"dummyAddress2\","
                +           "\"customer_hash\": \"45anyStupidHash\""
                +       "},"
                +       "{"
                +           "\"customer_number\": \"34\","
                +           "\"customer_email\": \"test3@email.com\","
                +           "\"customer_address\": \"dummyAddress3\""
                +       "}"
                +   "]"
                +"}";
        //J+

        final List<Map<String, Object>> resultMap = payloadKeyParserImpl.parseGrouped(KEY_CUSTOMER_NUMBER,
                newHashSet(KEY_CUSTOMER_ADDRESS, KEY_CUSTOMER_HASH), emptySet(), jsonPayload);

        assertThat(resultMap.size()).isEqualTo(2);

        final Map<String, Object> firstOrderItem = resultMap.get(0);
        assertEquals("12", firstOrderItem.get(KEY_CUSTOMER_NUMBER));
        assertEquals("12anyStupidHash", firstOrderItem.get(KEY_CUSTOMER_HASH));
        assertEquals("dummyAddress1", firstOrderItem.get(KEY_CUSTOMER_ADDRESS));
        assertNull(firstOrderItem.get(KEY_CUSTOMER_EMAIL));

        final Map<String, Object> secondOrderItem = resultMap.get(1);
        assertEquals("23", secondOrderItem.get(KEY_CUSTOMER_NUMBER));
        assertEquals("45anyStupidHash", secondOrderItem.get(KEY_CUSTOMER_HASH));
        assertEquals("dummyAddress2", secondOrderItem.get(KEY_CUSTOMER_ADDRESS));
        assertNull(secondOrderItem.get(KEY_CUSTOMER_EMAIL));
    }

    @Test
    public void parseGrouped_returnsEmptyValues_ifAllMandatoryKeysWereNotFound() {

        //J-
        final String jsonPayload = "{"
                +"\"customer_number\": \"12345\","
                +"\"customer_email\": \"test@email.com\","
                +"\"customer_address\": \"dummyAddress\","
                +"\"customer_hash\": \"12345anyStupidHash\""
                +"}";
        //J+

        final List<Map<String, Object>> resultMap = payloadKeyParserImpl.parseGrouped(KEY_CUSTOMER_NUMBER,
                newHashSet(KEY_CUSTOMER_EMAIL, KEY_LAST_NAME), newHashSet(KEY_CUSTOMER_ADDRESS), jsonPayload);

        assertThat(resultMap).isEmpty();
    }

    @Test
    public void parseGrouped_returnsValues_ifNotAllOptionalKeysWereNotFound() {
        final String expectedCustomerEmail = "test@email.com";
        final String expectedCustomerAddress = "dummyAddress";

        //J-
        final String jsonPayload = "{"
                +"\"customer_number\": \"12345\","
                +"\"customer_email\": \"test@email.com\","
                +"\"customer_address\": \"dummyAddress\","
                +"\"customer_hash\": \"12345anyStupidHash\""
                +"}";
        //J+

        final List<Map<String, Object>> resultMap = payloadKeyParserImpl.parseGrouped(KEY_CUSTOMER_NUMBER,
                newHashSet(KEY_CUSTOMER_EMAIL), newHashSet(KEY_CUSTOMER_ADDRESS, KEY_LAST_NAME), jsonPayload);

        assertThat(resultMap.size()).isEqualTo(1);

        final Map<String, Object> firstOrderItem = resultMap.get(0);
        assertEquals(expectedCustomerNumber, firstOrderItem.get(KEY_CUSTOMER_NUMBER));
        assertEquals(expectedCustomerEmail, firstOrderItem.get(KEY_CUSTOMER_EMAIL));
        assertEquals(expectedCustomerAddress, firstOrderItem.get(KEY_CUSTOMER_ADDRESS));
    }

    @Test
    public void parseGrouped_usesMappedKeysForLookupForMandatoryFields_ifProvided() throws Exception {
        final String expectedCustomerEmail = "test@email.com";

        //J-
        final String jsonPayload = "{"
                +"\"customer_id\": \"12345\","
                +"\"customer_electronic\": \"test@email.com\","
                +"\"customer_address\": \"dummyAddress\","
                +"\"customer_hashcode\": \"12345anyStupidHash\""
                +"}";
        //J+

        final Map<String, Object> payload = convertToMap(jsonPayload);
        final Map<String, String> mappedKnownKeys = new HashMap<>();
        mappedKnownKeys.put(KEY_CUSTOMER_NUMBER, "customer_id");
        mappedKnownKeys.put(KEY_CUSTOMER_EMAIL, "customer_electronic");
        mappedKnownKeys.put(KEY_CUSTOMER_HASH, "customer_hashcode");

        final List<Map<String, Object>> resultMap = payloadKeyParser.parseGrouped(KEY_CUSTOMER_NUMBER,
                newHashSet(KEY_CUSTOMER_EMAIL, KEY_CUSTOMER_HASH), newHashSet(), mappedKnownKeys, payload);

        assertThat(resultMap.size()).isEqualTo(1);

        final Map<String, Object> firstOrderItem = resultMap.get(0);
        assertEquals(expectedCustomerNumber, firstOrderItem.get(KEY_CUSTOMER_NUMBER));
        assertEquals(expectedCustomerHash, firstOrderItem.get(KEY_CUSTOMER_HASH));
        assertEquals(expectedCustomerEmail, firstOrderItem.get(KEY_CUSTOMER_EMAIL));
        assertNull(firstOrderItem.get(KEY_CUSTOMER_ADDRESS));
    }

    @Test
    public void parseGrouped_usesMappedKeysForLookupForMandatoryAndOptionalFields_ifProvided() throws Exception {
        final String expectedCustomerEmail = "test@email.com";
        final String expectedCustomerAddress = "dummyAddress";

        //J-
        final String jsonPayload = "{"
                +"\"customer_id\": \"12345\","
                +"\"customer_email\": \"test@email.com\","
                +"\"customer_mail\": \"dummyAddress\","
                +"\"customer_hashcode\": \"12345anyStupidHash\""
                +"}";
        //J+

        final Map<String, Object> payload = convertToMap(jsonPayload);
        final Map<String, String> mappedKnownKeys = new HashMap<>();
        mappedKnownKeys.put(KEY_CUSTOMER_NUMBER, "customer_id");
        mappedKnownKeys.put(KEY_CUSTOMER_ADDRESS, "customer_mail");
        mappedKnownKeys.put(KEY_CUSTOMER_HASH, "customer_hashcode");

        final List<Map<String, Object>> resultMap = payloadKeyParser.parseGrouped(KEY_CUSTOMER_NUMBER,
                newHashSet(KEY_CUSTOMER_EMAIL), newHashSet(KEY_CUSTOMER_ADDRESS, KEY_LAST_NAME), mappedKnownKeys,
                payload);

        assertThat(resultMap.size()).isEqualTo(1);

        final Map<String, Object> firstOrderItem = resultMap.get(0);
        assertEquals(expectedCustomerNumber, firstOrderItem.get(KEY_CUSTOMER_NUMBER));
        assertEquals(expectedCustomerEmail, firstOrderItem.get(KEY_CUSTOMER_EMAIL));
        assertEquals(expectedCustomerAddress, firstOrderItem.get(KEY_CUSTOMER_ADDRESS));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void parseUnique_returnsOnlyUniqueValues() {

        //J-
        final ImmutableMap<String, Object> payload =
                ImmutableMap.of("order_items",
                        toArray(ImmutableMap.of("customer_number", "12"),
                                ImmutableMap.of("customer_number", "12"),
                                ImmutableMap.of("customer_number", "34"))
                        );
        //J+

        final Map<String, Set<Object>> resultMap = payloadKeyParser.parseUnique(newHashSet(KEY_CUSTOMER_NUMBER),
                emptyMap(), payload);

        assertThat(resultMap.get(KEY_CUSTOMER_NUMBER)).containsOnly("12", "34");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void parseUnique_usesMappedKeysForLookup_ifProvided() {
        //J-
        final ImmutableMap<String, Object> payload =
                ImmutableMap.of("order_items",
                        toArray(ImmutableMap.of("customer_id", "12"),
                                ImmutableMap.of("customer_id", "12"),
                                ImmutableMap.of("customer_id", "34")),
                                KEY_SIZE, "27"
                        );
        //J+

        final Map<String, String> mappedKnownKeys = new HashMap<>();
        mappedKnownKeys.put(KEY_CUSTOMER_NUMBER, "customer_id");

        final Map<String, Set<Object>> resultMap = payloadKeyParser.parseUnique(newHashSet(KEY_CUSTOMER_NUMBER,
                    KEY_SIZE), mappedKnownKeys, payload);

        assertThat(resultMap.get(KEY_CUSTOMER_NUMBER)).containsOnly("12", "34");
        assertThat(resultMap.get(KEY_SIZE)).containsOnly("27");
    }

    @Test
    public void parse_returnsCorrectValues_ifKnownKeyIsSplitAndNotInPayloadAsPathList() {
        //J-
        final String jsonPayload = "{"
                +"\"size\": \"27\","
                +"\"customer\": {"
                +    "\"number\": \"12345\","
                +    "\"address\": \"12345anyStupidHash\""
                +    "}"
                +"}";
        //J+

        final Map<String, Object[]> resultMap = payloadKeyParserImpl.parse(newHashSet(KEY_CUSTOMER_NUMBER,
                    KEY_CUSTOMER_EMAIL), jsonPayload, AS_PATH_LIST);

        assertEquals("$['customer']['number']", resultMap.get(KEY_CUSTOMER_NUMBER)[0]);
        assertEquals(1, resultMap.size());
    }

}
