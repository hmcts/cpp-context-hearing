package uk.gov.moj.cpp.hearing.query.view.service;

import java.math.BigInteger;

public enum CaseStatusCode {
    ACTIVE(BigInteger.valueOf(1)),
    INACTIVE(BigInteger.valueOf(0));

    private final BigInteger statusCode;

    CaseStatusCode(final BigInteger statusCode) {
        this.statusCode = statusCode;
    }

    public BigInteger getStatusCode() {
        return statusCode;
    }
}
