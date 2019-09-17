package org.zalando.jzon.service.util;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;

import java.util.Objects;
import java.util.Set;

/**
 * @author  Sina Golesorkhi(sina.golesorkhi@zalando.de)
 */
public class CollectionUtil {

    private static final String NULL_DEFAULT = null;

    /**
     * Gives the first element in the given array.
     *
     * @param   valueArray
     *
     * @return  the first one if the array is not empty or null, otherwise {@code null}
     */
    public static Object getFirst(final Object[] valueArray) {
        if (isNotEmpty(valueArray)) {
            return valueArray[0];
        }

        return null;
    }

    public static String firstStringValueOf(final Object[] knownValues) {
        return Objects.toString(getFirst(knownValues), NULL_DEFAULT);
    }

    /**
     * @param   knownValues
     *
     * @return  {@code null} if not found
     */
    public static String firstStringValueOf(final Set<Object> knownValues) {
        return Objects.toString(getFirst(knownValues), NULL_DEFAULT);
    }

    private static Object getFirst(final Set<Object> values) {
        if (isNotEmpty(values)) {
            return values.iterator().next();
        }

        return null;
    }
}
