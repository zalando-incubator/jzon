package org.zalando.jzon.service.util;

import static java.util.Collections.emptySet;

import static org.assertj.core.api.Assertions.assertThat;

import static com.google.common.collect.Sets.newHashSet;

import org.junit.Test;

public class CollectionUtilTest {

    @Test
    public void getFirst_returnsNull_ifEmptyArrayIsProvided() {
        final Object firstItem = CollectionUtil.getFirst(null);

        assertThat(firstItem).isNull();
    }

    @Test
    public void getFirst_returnsTheFirstValue_onMultipleValues() {
        final String firstValue = "first";
        final String[] valueArray = {firstValue, "second"};

        final Object firstItem = CollectionUtil.getFirst(valueArray);

        assertThat(firstItem).isEqualTo(firstValue);
    }

    @Test
    public void firstStringValueOf_returnsTheFirstValue_onMultipleValues() {
        final String firstValue = "first";
        final Object[] valueArray = {firstValue, "second"};

        final Object firstItem = CollectionUtil.firstStringValueOf(valueArray);

        assertThat(firstItem).isEqualTo(firstValue);
    }

    @Test
    public void firstStringValueOf_returnsNull_onMultipleValuesWithFirstOneAsNull() {
        final Object[] valueArray = {null, "second"};

        final Object firstItem = CollectionUtil.firstStringValueOf(valueArray);

        assertThat(firstItem).isNull();
    }

    @Test
    public void firstStringValueOf_returnsTheFirstValue_onMultipleValuesForASet() {
        final String firstValue = "first";
        final String firstItem = CollectionUtil.firstStringValueOf(newHashSet(firstValue, "second"));

        assertThat(firstItem).isEqualTo(firstValue);
    }

    @Test
    public void firstStringValueOf_returnsNull_onMultipleValuesWithFirstOneAsNullInASet() {
        final Object firstItem = CollectionUtil.firstStringValueOf(newHashSet(null, "second"));

        assertThat(firstItem).isNull();
    }

    @Test
    public void firstStringValueOf_returnsNull_onEmptySets() {
        final Object firstItem = CollectionUtil.firstStringValueOf(emptySet());

        assertThat(firstItem).isNull();
    }
}
