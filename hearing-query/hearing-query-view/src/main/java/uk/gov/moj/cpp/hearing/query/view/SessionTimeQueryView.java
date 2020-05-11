package uk.gov.moj.cpp.hearing.query.view;

import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.hearing.query.view.response.SessionTimeResponse;
import uk.gov.moj.cpp.hearing.query.view.service.SessionTimeService;

import javax.inject.Inject;
import javax.json.JsonObject;

@ServiceComponent(Component.QUERY_VIEW)
public class SessionTimeQueryView {

    @Inject
    private SessionTimeService sessionTimeService;

    @Handles("hearing.query.session-time")
    public Envelope<SessionTimeResponse> getSessionTime(final Envelope<JsonObject> sessionTimeEnvelope) {
        final JsonObject payload = sessionTimeEnvelope.payload();
        final SessionTimeResponse sessionTimeResponse = sessionTimeService.getSessionTime(payload);

        return envelop(sessionTimeResponse)
                .withName("hearing.query.session-time")
                .withMetadataFrom(sessionTimeEnvelope);
    }

}
