package uk.gov.moj.cpp.hearing.query.api;

import static javax.json.Json.createArrayBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(Component.QUERY_API)
public class HearingQueryApi {
    public static final String STAGINGENFORCEMENT_QUERY_OUTSTANDING_FINES = "stagingenforcement.defendant.outstanding-fines";
    private static final Logger LOGGER = LoggerFactory.getLogger(HearingQueryApi.class);

    @Inject
    private Requester requester;

    @Inject
    private Enveloper enveloper;

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

    @Handles("hearing.case.timeline")
    public JsonEnvelope getCaseTimeline(final JsonEnvelope query) {
        return this.requester.request(query);
    }

    @Handles("hearing.application.timeline")
    public JsonEnvelope getApplicationTimeline(final JsonEnvelope query) {
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


    @Handles("hearing.defendant.outstanding-fines")
    public JsonEnvelope getDefendantOutstandingFines(final JsonEnvelope query) {
        final JsonEnvelope viewResponseEnvelope = this.requester.request(query);
        final JsonObject viewResponseEnvelopePayload = viewResponseEnvelope.payloadAsJsonObject();
        if (!viewResponseEnvelopePayload.isEmpty()) {
            return requestStagingEnforcementToGetOutstandingFines(query, viewResponseEnvelopePayload);
        }
        return envelopeFrom(query.metadata(),
                Json.createObjectBuilder()
                        .add("outstandingFines",
                                createArrayBuilder()).build());
    }

    @SuppressWarnings("squid:S2629")
    private JsonEnvelope requestStagingEnforcementToGetOutstandingFines(final JsonEnvelope query, final JsonObject viewResponseEnvelopePayload) {
        final JsonEnvelope enforcementResultEnvelope;
        final JsonEnvelope enforcementRequestEnvelope = enveloper.withMetadataFrom(query, STAGINGENFORCEMENT_QUERY_OUTSTANDING_FINES)
                .apply(viewResponseEnvelopePayload);

        enforcementResultEnvelope = requester.requestAsAdmin(enforcementRequestEnvelope);
        final JsonObject outstandingFines = enforcementResultEnvelope.payloadAsJsonObject();
        LOGGER.info(String.format("outstandingFines  : %s", outstandingFines));
        return enforcementResultEnvelope;
    }

    @Handles("hearing.query.session-time")
    public JsonEnvelope sessionTime(final JsonEnvelope query) {
        return requester.request(query);
    }
}
