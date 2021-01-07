package uk.gov.moj.cpp.hearing.query.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.core.courts.CrackedIneffectiveTrial;
import uk.gov.justice.hearing.courts.GetHearings;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.dispatcher.EnvelopePayloadTypeConverter;
import uk.gov.justice.services.core.dispatcher.JsonEnvelopeRepacker;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialTypes;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.AccessibleCases;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.DDJChecker;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.UsersAndGroupsService;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.Permissions;
import uk.gov.moj.cpp.hearing.query.api.service.referencedata.ReferenceDataService;
import uk.gov.moj.cpp.hearing.query.api.service.referencedata.XhibitEventMapperCache;
import uk.gov.moj.cpp.hearing.query.view.HearingEventQueryView;
import uk.gov.moj.cpp.hearing.query.view.HearingQueryView;
import uk.gov.moj.cpp.hearing.query.view.OutstandingFineRequestsQueryView;
import uk.gov.moj.cpp.hearing.query.view.SessionTimeQueryView;
import uk.gov.moj.cpp.hearing.query.view.response.SessionTimeResponse;
import uk.gov.moj.cpp.hearing.query.view.response.Timeline;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.ApplicationTargetListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.NowListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.TargetListResponse;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.BadRequestException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static javax.json.Json.createArrayBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;

@ServiceComponent(Component.QUERY_API)
public class HearingQueryApi {
    public static final String STAGINGENFORCEMENT_QUERY_OUTSTANDING_FINES = "stagingenforcement.defendant.outstanding-fines";
    private static final Logger LOGGER = LoggerFactory.getLogger(HearingQueryApi.class);

    @Inject
    private Requester requester;

    @Inject
    private Enveloper enveloper;

    @Inject
    private JsonEnvelopeRepacker jsonEnvelopeRepacker;

    @Inject
    private EnvelopePayloadTypeConverter envelopePayloadTypeConverter;

    @Inject
    private HearingEventQueryView hearingEventQueryView;

    @Inject
    private HearingQueryView hearingQueryView;

    @Inject
    private SessionTimeQueryView sessionTimeQueryView;

    @Inject
    private OutstandingFineRequestsQueryView outstandingFineRequestsQueryView;

    @Inject
    private ReferenceDataService referenceDataService;

    @Inject
    private XhibitEventMapperCache xhibitEventMapperCache;

    @Inject
    private UsersAndGroupsService usersAndGroupsService;

    @Inject
    private AccessibleCases accessibleCasesO;

    @Inject
    private DDJChecker ddjChecker;

    @Handles("hearing.get.hearings")
    public JsonEnvelope findHearings(final JsonEnvelope query) {

        final Optional<String> optionalUserId = query.metadata().userId();
        if (!optionalUserId.isPresent()) {
            throw new BadRequestException("No Logged in UserId found to perform hearings search");
        }
        final String userId = optionalUserId.get();
        final boolean isDDJ = ddjChecker.isDDJ(userId);
        final List<UUID> accessibleCases = getAccessibleCases(userId, isDDJ);
        final Envelope<GetHearings> envelope = this.hearingQueryView.findHearings(query, accessibleCases, isDDJ);
        return getJsonEnvelope(envelope);
    }

    @Handles("hearing.get.hearings-for-today")
    public JsonEnvelope findHearingsForToday(final JsonEnvelope query) {
        final Envelope<GetHearings> envelope = this.hearingQueryView.findHearingsForToday(query);
        return getJsonEnvelope(envelope);
    }

    @Handles("hearing.get.hearing")
    public JsonEnvelope findHearing(final JsonEnvelope query) {
        final Optional<String> optionalUserId = query.metadata().userId();
        if (!optionalUserId.isPresent()) {
            throw new BadRequestException("No Logged in UserId found to perform hearings search");
        }
        final String userId = optionalUserId.get();
        final CrackedIneffectiveVacatedTrialTypes crackedIneffectiveVacatedTrialTypes = referenceDataService.listAllCrackedIneffectiveVacatedTrialTypes();
        final boolean isDDJ = ddjChecker.isDDJ(userId);
        final List<UUID> accessibleCases = getAccessibleCases(userId, isDDJ);
        final Envelope<HearingDetailsResponse> envelope = this.hearingQueryView.findHearing(query, crackedIneffectiveVacatedTrialTypes, accessibleCases, isDDJ);
        return getJsonEnvelope(envelope);
    }

    @Handles("hearing.get-hearing-event-definitions")
    public JsonEnvelope getHearingEventDefinitionsVersionTwo(final JsonEnvelope query) {
        final Envelope<JsonObject> envelope = this.hearingEventQueryView.getHearingEventDefinitions(query);
        return getJsonEnvelope(envelope);
    }

    @Handles("hearing.get-hearing-event-definition")
    public JsonEnvelope getHearingEventDefinition(final JsonEnvelope query) {
        final Envelope<JsonObject> envelope = this.hearingEventQueryView.getHearingEventDefinition(query);
        return getJsonEnvelope(envelope);
    }

    @Handles("hearing.get-hearing-event-log")
    public JsonEnvelope getHearingEventLog(final JsonEnvelope query) {
        final Envelope<JsonObject> envelope = this.hearingEventQueryView.getHearingEventLog(query);
        return getJsonEnvelope(envelope);
    }

    @Handles("hearing.get-draft-result")
    public JsonEnvelope getDraftResult(final JsonEnvelope query) {
        final Envelope<TargetListResponse> envelope = this.hearingQueryView.getDraftResult(query);
        return getJsonEnvelope(envelope);
    }

    @Handles("hearing.get-application-draft-result")
    public JsonEnvelope getApplicationDraftResult(final JsonEnvelope query) {
        final Envelope<ApplicationTargetListResponse> envelope = this.hearingQueryView.getApplicationDraftResult(query);
        return getJsonEnvelope(envelope);
    }

    @Handles("hearing.query.search-by-material-id")
    public JsonEnvelope searchByMaterialId(final JsonEnvelope query) {
        return this.hearingQueryView.searchByMaterialId(query);
    }

    @Handles("hearing.retrieve-subscriptions")
    public JsonEnvelope retrieveSubscriptions(final JsonEnvelope query) {
        return this.hearingQueryView.retrieveSubscriptions(query);
    }

    @Handles("hearing.get.nows")
    public JsonEnvelope findNows(final JsonEnvelope query) {
        final Envelope<NowListResponse> envelope = this.hearingQueryView.findNows(query);
        return getJsonEnvelope(envelope);
    }

    @Handles("hearing.get-active-hearings-for-court-room")
    public JsonEnvelope getActiveHearingsForCourtRoom(final JsonEnvelope query) {
        final Envelope<JsonObject> envelope = this.hearingEventQueryView.getActiveHearingsForCourtRoom(query);
        return getJsonEnvelope(envelope);
    }

    @Handles("hearing.get-cracked-ineffective-reason")
    public JsonEnvelope getCrackedIneffectiveTrialReason(final JsonEnvelope query) {
        final CrackedIneffectiveVacatedTrialTypes crackedIneffectiveVacatedTrialTypes = referenceDataService.listAllCrackedIneffectiveVacatedTrialTypes();
        final Envelope<CrackedIneffectiveTrial> envelope = this.hearingQueryView.getCrackedIneffectiveTrialReason(query, crackedIneffectiveVacatedTrialTypes);
        return getJsonEnvelope(envelope);
    }

    @Handles("hearing.case.timeline")
    public JsonEnvelope getCaseTimeline(final JsonEnvelope query) {
        final CrackedIneffectiveVacatedTrialTypes crackedIneffectiveVacatedTrialTypes = referenceDataService.listAllCrackedIneffectiveVacatedTrialTypes();
        final JsonObject allCourtRooms = referenceDataService.getAllCourtRooms(query);

        final Envelope<Timeline> envelope = this.hearingQueryView.getTimeline(query, crackedIneffectiveVacatedTrialTypes, allCourtRooms);
        return getJsonEnvelope(envelope);
    }

    @Handles("hearing.application.timeline")
    public JsonEnvelope getApplicationTimeline(final JsonEnvelope query) {
        final CrackedIneffectiveVacatedTrialTypes crackedIneffectiveVacatedTrialTypes = referenceDataService.listAllCrackedIneffectiveVacatedTrialTypes();
        final JsonObject allCourtRooms = referenceDataService.getAllCourtRooms(query);

        final Envelope<Timeline> envelope = this.hearingQueryView.getTimelineByApplicationId(query, crackedIneffectiveVacatedTrialTypes, allCourtRooms);
        return getJsonEnvelope(envelope);
    }

    @Handles("hearing.court.list.publish.status")
    public JsonEnvelope publishCourtListStatus(final JsonEnvelope query) {
        return this.hearingQueryView.getCourtListPublishStatus(query);
    }

    @Handles("hearing.latest-hearings-by-court-centres")
    public JsonEnvelope getHeringsByCourtCentre(final JsonEnvelope query) {
        final Set<UUID> cppHearingEventIds = xhibitEventMapperCache.getCppHearingEventIds();
        return this.hearingQueryView.getLatestHearingsByCourtCentres(query, cppHearingEventIds);
    }

    @Handles("hearing.hearings-court-centres-for-date")
    public JsonEnvelope getHearingsForCourtCentreForDate(final JsonEnvelope query) {
        final Set<UUID> cppHearingEventIds = xhibitEventMapperCache.getCppHearingEventIds();
        return this.hearingQueryView.getHearingsForCourtCentresForDate(query, cppHearingEventIds);
    }

    @Handles("hearing.defendant.outstanding-fines")
    public JsonEnvelope getDefendantOutstandingFines(final JsonEnvelope query) {
        final JsonEnvelope viewResponseEnvelope = this.hearingQueryView.getOutstandingFromDefendantId(query);
        final JsonObject viewResponseEnvelopePayload = viewResponseEnvelope.payloadAsJsonObject();
        if (!viewResponseEnvelopePayload.isEmpty()) {
            return requestStagingEnforcementToGetOutstandingFines(query, viewResponseEnvelopePayload);
        }
        return envelopeFrom(query.metadata(),
                Json.createObjectBuilder()
                        .add("outstandingFines",
                                createArrayBuilder()).build());
    }

    @Handles("hearing.defendant.info")
    public JsonEnvelope getHearingDefendantInfo(final JsonEnvelope query) {
        return this.hearingQueryView.getDefendantInfoFromCourtHouseId(query);
    }

    @Handles("hearing.defendant.outstanding-fine-requests")
    public JsonEnvelope getDefendantOutstandingFineRequests(final JsonEnvelope query) {
        return this.outstandingFineRequestsQueryView.getDefendantOutstandingFineRequests(query);
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
        final Envelope<SessionTimeResponse> envelope = this.sessionTimeQueryView.getSessionTime(envelopePayloadTypeConverter.convert(query, JsonObject.class));
        return getJsonEnvelope(envelope);
    }

    private JsonEnvelope getJsonEnvelope(final Envelope<?> getHearingsEnvelope) {
        final Envelope<JsonValue> jsonValueEnvelope = this.envelopePayloadTypeConverter.convert(getHearingsEnvelope, JsonValue.class);
        return jsonEnvelopeRepacker.repack(jsonValueEnvelope);
    }

    private List<UUID> getAccessibleCases(final String userId, final boolean isDDJ){
        List<UUID> accessibleCases = new ArrayList<>();
        if (isDDJ){
            final Permissions permissions = usersAndGroupsService.permissions(userId);
            accessibleCases = accessibleCasesO.findCases(permissions, userId);
        }
        return accessibleCases;
    }
}
