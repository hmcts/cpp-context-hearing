package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared;

import java.math.BigDecimal;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class TypeUtils {

    private TypeUtils() {
    }

    public static Boolean getBooleanValue(final Boolean originalValue, final Boolean defaultValue) {
        return isNull(originalValue) ? defaultValue : originalValue;
    }

    public static BigDecimal getBigDecimal(final Integer originalValue, final BigDecimal defaultValue) {
        return isNull(originalValue) ? defaultValue : BigDecimal.valueOf(originalValue);
    }

    public static String getString(final String originalValue, final String defaultValue) {
        return nonNull(originalValue) && !originalValue.isEmpty() ? originalValue : defaultValue;
    }

    public static String convertBooleanPromptValue(final String originalValue) {
        return "true".equalsIgnoreCase(originalValue) ? "Yes" : "No";
    }
}
