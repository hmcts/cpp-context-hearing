package uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted;

import java.util.Optional;

public enum IndicatedPleaValue {
    INDICATED_GUILTY("INDICATED_GUILTY"),

    NO_INDICATION("NO_INDICATION"),

    INDICATED_NOT_GUILTY("INDICATED_NOT_GUILTY");

    private final String value;

    IndicatedPleaValue(String value) {
        this.value = value;
    }

    public static Optional<IndicatedPleaValue> valueFor(final String value) {
        if (INDICATED_GUILTY.value.equals(value)) { return Optional.of(INDICATED_GUILTY); }
        ;
        if (NO_INDICATION.value.equals(value)) { return Optional.of(NO_INDICATION); }
        ;
        if (INDICATED_NOT_GUILTY.value.equals(value)) { return Optional.of(INDICATED_NOT_GUILTY); }
        ;
        return Optional.empty();
    }

    @Override
    public String toString() {
        return value;
    }
}
