package uk.gov.moj.cpp.hearing.query.controller;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.dispatcher.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

@ServiceComponent(Component.QUERY_CONTROLLER)
public class HearingQueryController {

    @Inject
    private Requester requester;

    @Handles("hearing.get.hearings-by-startdate")
    public JsonEnvelope findHearingsByStartDate(final JsonEnvelope query) {
        return requester.request(query);
    }

    @Handles("hearing.get.hearings-by-caseid")
    public JsonEnvelope findHearingsByCaseId(final JsonEnvelope query) {
        return requester.request(query);
    }

    @Handles("hearing.get.hearing")
    public JsonEnvelope findHearing(final JsonEnvelope query) {
        return requester.request(query);
    }

    @Handles("hearing.get-hearing-event-definitions")
    public JsonEnvelope findHearingEventDefinitions(final JsonEnvelope query) {
        return requester.request(query);
    }

    @Handles("hearing.get.prosecution-counsels")
    public JsonEnvelope getProsecutionCounsels(final JsonEnvelope query) {
        return requester.request(query);
    }

    @Handles("hearing.get-hearing-event-log")
    public JsonEnvelope getHearingEventLog(final JsonEnvelope query) {
        return requester.request(query);
    }

    @Handles("hearing.get.defence-counsels")
    public JsonEnvelope getDefenceCounsels(final JsonEnvelope query) {
        return requester.request(query);
    }

    @Handles("hearing.get-draft-result")
    public JsonEnvelope getDraftResult(final JsonEnvelope query) {
        return requester.request(query);
    }
}
