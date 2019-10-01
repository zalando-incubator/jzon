package org.zalando.jzon.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author  Sina Golesorkhi(sina.golesorkhi@zalando.de)
 */
public interface PayloadKeyParser {

    String SEPARATOR = "_";
    boolean AS_PATH_LIST = true;
    boolean AS_VALUE_LIST = false;

    /**
     * Searches for the given {@code key} in the payload and returns the found value (if any). Supported scenarios
     * searching for the key {@code customer_number} are:
     *
     * <pre>
       <code>
       {
        "size": 27,
        "customer_number": "12345"
       }
       </code>
       <code>
       OR
       {
        "size": 27,
         "customer": {
            "customer_number": "12345"
        }
       }
       </code>
       OR
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
     * @param   keys             Set of keys to look for in the {@code payload}. The keys should be {@code SNAKE_CASE}
     *                           and only consist of maximum two words
     * @param   replacementKeys  if provided these values from the map will be used instead of those from {@code keys}
     * @param   jsonPayload      the json payload as Map
     * @param   asPathList       if {@code true} the function will look for the path of the {@code key} otherwise for
     *                           the value
     *
     * @return  an always {@code non-null} map which has the mapping of given keys and their corresponding found values
     *          for them in the payload. The value object which is either always an array containing String value or
     *          counterpart classes of primitive types (e.g. Long) or null if nothing is found for that key.
     *
     * @throws  IllegalArgumentException  if the provided {@code keys} is blank
     */
    Map<String, Object[]> parse(final Set<String> keys, final Map<String, String> replacementKeys,
            final Map<String, Object> jsonPayload, boolean asPathList);

    /**
     * Searches for the given {@code key} in the payload and returns the found value (if any). Supported scenarios
     * searching for the key {@code customer_number} are:
     *
     * <pre>
       <code>
       {
        "size": 27,
        "customer_number": "12345"
       }
       </code>
       <code>
       OR
       {
        "size": 27,
         "customer": {
            "customer_number": "12345"
        }
       }
       </code>
       OR
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
     * @param   keys             Set of keys to look for in the {@code payload}. The keys should be {@code SNAKE_CASE}
     *                           and only consist of maximum two words
     * @param   replacementKeys  if provided these values from the map will be used instead of those from {@code keys}
     * @param   jsonPayload      the json payload as String
     * @param   asPathList       if {@code true} the function will look for the path of the {@code key} otherwise for
     *                           the value
     *
     * @return  an always {@code non-null} map which has the mapping of given keys and their corresponding found values
     *          for them in the payload. The value object which is either always an array containing String value or
     *          counterpart classes of primitive types (e.g. Long) or null if nothing is found for that key.
     *
     * @throws  IllegalArgumentException  if the provided {@code keys} is blank
     */
    Map<String, Object[]> parse(final Set<String> keys, final Map<String, String> replacementKeys,
            final String jsonPayload, final boolean asPathList);

    /**
     * Returns unique results for each provided key.
     *
     * <pre>
       <code>
        { "order_items": [
             { "customer_number": "12" },
             { "customer_number": "12" },
             { "customer_number": "34" }
             ]
        }
       </code>
     * </pre>
     *
     * @param   keys
     * @param   replacementKeys  if provided these values from the map will be used instead of those from {@code keys}
     * @param   jsonPayload
     *
     * @return  a Map where its values are Set of Objects guaranteeing that only unique results are returned.
     *
     * @throws  IllegalArgumentException  if the provided {@code keys} is blank
     */
    Map<String, Set<Object>> parseUnique(final Set<String> keys, final Map<String, String> replacementKeys,
            Map<String, Object> jsonPayload);

    /**
     * Searches for the given {@code groupKey} in the payload and then all other {@code keys} on the same level and
     * returns them as a list of maps for those fields. Only supported scenario is if the key is found exactly as it was
     * sent. Searches for the given {@code groupKey} in the payload and then all other {@code mandatoryKeys} on the same
     * level and returns them as a list of maps for those fields. Only supported scenario is if the key is found exactly
     * as it was sent.
     *
     * @param   groupKey         the key on which level all other mandatoryKeys will be looked up
     * @param   mandatoryKeys    the mandatoryKeys for values to be extracted if found on same level as groupKey
     * @param   optionalKeys     the optionalKeys for values to be extracted if found on same level as groupKey
     * @param   replacementKeys  if provided these values from the map will be used instead of their keys which are from
     *                           {@code groupKey},{@code mandatoryKeys}, {@code optionalKeys}
     * @param   jsonPayload      payload
     *
     * @return  an always {@code non-null} list of of maps containing the mandatoryKeys and values for the sent groupKey
     *          and mandatoryKeys
     *
     * @throws  IllegalArgumentException  if the provided {@code keys} is blank
     */
    List<Map<String, Object>> parseGrouped(final String groupKey, final Set<String> mandatoryKeys,
            final Set<String> optionalKeys, Map<String, String> replacementKeys, final Map<String, Object> jsonPayload);

}
