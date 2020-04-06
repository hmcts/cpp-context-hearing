package uk.gov.moj.cpp.hearing.domain.transformation.helper;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Iterables;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.collections.CollectionUtils;

public class JsonPathHelper {

    private JsonPathHelper() {
    }

    public static Optional<String> getFirstValueForJsonPath(final String eventPayload, final String path) {
        if (JsonPath.isPathDefinite(path)) {
            final String value = JsonPath.read(eventPayload, path);
            if (null != value) {
                return Optional.of(value);
            }
        } else {
            final List<String> values = JsonPath.read(eventPayload, path);
            if (CollectionUtils.isNotEmpty(values)) {
                return Optional.of(values.get(0));
            }
        }

        return Optional.empty();
    }

    public static Optional<String> getLastValueForJsonPath(final String eventPayload, final String path) {
        if (JsonPath.isPathDefinite(path)) {
            final String value = JsonPath.read(eventPayload, path);
            if (null != value) {
                return Optional.of(value);
            }
        } else {
            final List<String> values = JsonPath.read(eventPayload, path);
            if (CollectionUtils.isNotEmpty(values)) {
                return Optional.of(Iterables.getLast(values));
            }
        }

        return Optional.empty();
    }
}
