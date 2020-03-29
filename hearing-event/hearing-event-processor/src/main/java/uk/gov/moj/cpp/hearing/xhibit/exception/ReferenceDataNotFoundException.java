package uk.gov.moj.cpp.hearing.xhibit.exception;

import static java.lang.String.format;

import uk.gov.justice.services.messaging.JsonEnvelope;

public class ReferenceDataNotFoundException extends RuntimeException {
    public ReferenceDataNotFoundException(final JsonEnvelope jsonEnvelope) {
        super(format("Could not find ReferenceData for %n metadata %n %s %n and payload %n %s %n", jsonEnvelope.metadata().asJsonObject(), jsonEnvelope.payloadAsJsonObject()));
    }
}
