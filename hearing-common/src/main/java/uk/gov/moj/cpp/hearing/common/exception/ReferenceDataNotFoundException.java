package uk.gov.moj.cpp.hearing.common.exception;

import static java.lang.String.format;

import uk.gov.justice.services.messaging.JsonEnvelope;

public class ReferenceDataNotFoundException extends RuntimeException {
    public ReferenceDataNotFoundException(final JsonEnvelope jsonEnvelope, final String message) {
        super(format("Could not find ReferenceData for %n metadata %n %s %n and payload %n %s %n. Message : %s", jsonEnvelope.metadata().asJsonObject(), jsonEnvelope.payloadAsJsonObject(), message));
    }
}
