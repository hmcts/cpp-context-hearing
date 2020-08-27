package uk.gov.moj.cpp.hearing.query.view.service;

import java.math.BigInteger;

public enum ProgessStatusCode {
    FINISHED(BigInteger.valueOf(9)),
    INPROGRESS(BigInteger.valueOf(5)),
    STARTED(BigInteger.valueOf(0)),
    ADJOURNED(BigInteger.valueOf(8));

    private final BigInteger progressCode;

    private ProgessStatusCode(final BigInteger progressCode) {
        this.progressCode = progressCode;
    }

    public BigInteger getProgressCode() {
        return progressCode;
    }
}
