package uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted;

import java.util.Optional;

public enum PleaValue {
    GUILTY("GUILTY"),
    NOT_GUILTY("NOT_GUILTY");

    private final String value;

    PleaValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
