package org.zalando.jzon.service.impl;

import static com.jayway.jsonpath.Configuration.builder;
import static com.jayway.jsonpath.JsonPath.compile;
import static com.jayway.jsonpath.JsonPath.using;

import java.util.Map;
import java.util.Set;

import org.zalando.jzon.service.PayloadKeyModifier;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

public final class PayloadKeyModifierImpl implements PayloadKeyModifier {

    private static final Configuration PATH_CONFIGURATION = builder().options(Option.AS_PATH_LIST).build();

    PayloadKeyModifierImpl() {
        // do nothing
    }

    @Override
    public void removeKeysWithPaths(final Set<String> toBeRemovedKeyPaths, final Map<String, Object> payload) {
        for (final String path : toBeRemovedKeyPaths) {
            final JsonPath jsonPath = compile(path);
            using(PATH_CONFIGURATION).parse(payload).delete(jsonPath);
        }
    }
}
