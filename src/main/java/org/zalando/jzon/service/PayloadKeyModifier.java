package org.zalando.jzon.service;

import java.util.Map;
import java.util.Set;

public interface PayloadKeyModifier {

    /**
     * Modifies a {@code payload} by removing keys that are provided from it.
     *
     * @param  toBeRemovedKeyPaths  a Set of paths of json keys which should be removed from the provided
     *                              {@code payload}
     * @param  payload              payload to be modified
     */
    void removeKeysWithPaths(Set<String> toBeRemovedKeyPaths, Map<String, Object> payload);

}
