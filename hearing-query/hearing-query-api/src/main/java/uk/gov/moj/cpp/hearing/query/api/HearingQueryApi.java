package uk.gov.moj.cpp.hearing.query.api;



import javax.inject.Inject;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.dispatcher.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;

@ServiceComponent(Component.QUERY_API)
public class HearingQueryApi {

    @Inject
    private Requester requester;

    @Handles("hearing.query.hearings")
    public JsonEnvelope findHearings(final JsonEnvelope query) {
        return requester.request(query);
    }

    @Handles("hearing.query.hearing")
    public JsonEnvelope findHearing(final JsonEnvelope query) {
        return requester.request(query);
    }

}
