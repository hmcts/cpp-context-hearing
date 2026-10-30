package uk.gov.moj.cpp.hearing.xhibit;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.xhibit.pojo.PublishCourtListRequestParameters;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.JsonObject;

@ApplicationScoped
public class PublishCourtListRequestParametersParser {

    public PublishCourtListRequestParameters parse(final JsonEnvelope envelope) {

        final JsonObject payload = envelope.payloadAsJsonObject();

        return new PublishCourtListRequestParameters(payload.getString("courtCentreId"), payload.getString("createdTime"));
    }
}
