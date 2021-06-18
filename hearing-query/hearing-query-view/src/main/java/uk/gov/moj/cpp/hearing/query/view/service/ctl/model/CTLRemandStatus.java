package uk.gov.moj.cpp.hearing.query.view.service.ctl.model;

import java.util.Arrays;

public enum CTLRemandStatus {
    CUSTODY_OR_REMANDED_INTO_CUSTODY("C"),
    REMANDED_INTO_CARE_OF_LOCAL_AUTHORITY("L"),
    SECURE_ACCOMADATION("S"),
    REMANDED_IN_CUSTODY_PENDING_CONDITIONS("P");

    private String code;

    CTLRemandStatus(final String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static CTLRemandStatus getCTLRemandStatusFrom(final String code) {
        return Arrays.stream(CTLRemandStatus.values())
                .filter(ot -> ot.code.equalsIgnoreCase(code)).findFirst().orElse(null);
    }
}