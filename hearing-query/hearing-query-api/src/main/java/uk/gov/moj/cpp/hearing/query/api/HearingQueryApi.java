package uk.gov.moj.cpp.hearing.query.api;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

@ServiceComponent(Component.QUERY_API)
public class HearingQueryApi {

    @Inject
    private Requester requester;

    @Handles("hearing.get.hearings")
    public JsonEnvelope findHearings(final JsonEnvelope query) {
        return this.requester.request(query);
    }

    @Handles("hearing.get.hearings-for-today")
    public JsonEnvelope findHearingsForToday(final JsonEnvelope query) {
        return this.requester.request(query);
    }

    @Handles("hearing.get.hearing")
    public JsonEnvelope findHearing(final JsonEnvelope query) {
        return this.requester.request(query);
    }

    @Handles("hearing.get-hearing-event-definitions")
    public JsonEnvelope getHearingEventDefinitionsVersionTwo(final JsonEnvelope query) {
        return this.requester.request(query);
    }

    @Handles("hearing.get-hearing-event-definition")
    public JsonEnvelope getHearingEventDefinition(final JsonEnvelope query) {
        return this.requester.request(query);
    }

    @Handles("hearing.get-hearing-event-log")
    public JsonEnvelope getHearingEventLog(final JsonEnvelope query) {
        return this.requester.request(query);
    }

    @Handles("hearing.get-draft-result")
    public JsonEnvelope getDraftResult(final JsonEnvelope query) {
        return this.requester.request(query);
    }

    @Handles("hearing.get-application-draft-result")
    public JsonEnvelope getApplicationDraftResult(final JsonEnvelope query) {
        return this.requester.request(query);
    }

    @Handles("hearing.query.search-by-material-id")
    public JsonEnvelope searchByMaterialId(final JsonEnvelope query) {
        return this.requester.request(query);
    }

    @Handles("hearing.retrieve-subscriptions")
    public JsonEnvelope retrieveSubscriptions(final JsonEnvelope query) {
        return this.requester.request(query);
    }

    @Handles("hearing.get.nows")
    public JsonEnvelope findNows(final JsonEnvelope query) {
        return this.requester.request(query);
    }

    @Handles("hearing.get-active-hearings-for-court-room")
    public JsonEnvelope getActiveHearingsForCourtRoom(final JsonEnvelope query) {
        return this.requester.request(query);
    }

    @Handles("hearing.get-cracked-ineffective-reason")
    public JsonEnvelope getCrackedIneffectiveTrialReason(final JsonEnvelope query) {
        return this.requester.request(query);
    }

    @Handles("hearing.court.list.publish.status")
    public JsonEnvelope publishCourtListStatus(final JsonEnvelope query) {
        return requester.request(query);
    }

    @Handles("hearing.latest-hearings-by-court-centres")
    public JsonEnvelope getHeringsByCourtCentre(final JsonEnvelope query) {
        return requester.request(query);
    }


    @Handles("hearing.hearings-court-centres-for-date")
    public JsonEnvelope getHearingsForCourtCentreForDate(final JsonEnvelope query) {
        return requester.request(query);
    }
}
