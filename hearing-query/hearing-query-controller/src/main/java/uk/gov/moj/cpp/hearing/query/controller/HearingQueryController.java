package uk.gov.moj.cpp.hearing.query.controller;



import javax.inject.Inject;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.dispatcher.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;

@ServiceComponent(Component.QUERY_CONTROLLER)
public class HearingQueryController {

    @Inject
    private Requester requester;

    @Handles("hearing.get.hearings-by-startdate")
    public JsonEnvelope findHearingsByStartDate(final JsonEnvelope query) {
        return requester.request(query);
    }

    @Handles("hearing.get.hearing")
    public JsonEnvelope findHearing(final JsonEnvelope query) {
        return requester.request(query);
    }

}
