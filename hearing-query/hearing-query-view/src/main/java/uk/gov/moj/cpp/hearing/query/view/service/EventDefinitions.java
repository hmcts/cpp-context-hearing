package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.UUID.fromString;

import java.math.BigInteger;
import java.util.UUID;

public enum EventDefinitions {
    FINISHED(fromString("0df93f18-0a21-40f5-9fb3-da4749cd70fe"), BigInteger.valueOf(9)),
    PAUSED(fromString("160ecb51-29ee-4954-bbbf-daab18a24fbb"), BigInteger.valueOf(8)),
    RESUME(fromString("64476e43-2138-46d5-b58b-848582cf9b07"), BigInteger.valueOf(5));

    private final UUID eventDefinitionsId;
    private final BigInteger progressCode;

    private EventDefinitions(final UUID eventDefinitionsId,
                             final BigInteger progressCode) {
        this.eventDefinitionsId = eventDefinitionsId;
        this.progressCode = progressCode;
    }

    public UUID getEventDefinitionsId() {
        return eventDefinitionsId;
    }

    public BigInteger getProgressCode() {
        return progressCode;
    }
}
