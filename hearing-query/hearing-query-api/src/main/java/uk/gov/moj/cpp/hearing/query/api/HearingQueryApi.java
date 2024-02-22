package uk.gov.moj.cpp.hearing.query.api;

import static java.util.UUID.fromString;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjects.getString;
import static uk.gov.justice.services.messaging.JsonObjects.getUUID;

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
import uk.gov.moj.cpp.external.domain.progression.prosecutioncases.ProsecutionCase;
import uk.gov.moj.cpp.hearing.domain.referencedata.HearingTypes;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialTypes;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.query.api.service.usergroups.UserGroupQueryService;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.AccessibleApplications;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.AccessibleCases;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.DDJChecker;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.RecorderChecker;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.UsersAndGroupsService;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.Permissions;
import uk.gov.moj.cpp.hearing.query.api.service.progression.ProgressionService;
import uk.gov.moj.cpp.hearing.query.api.service.referencedata.PIEventMapperCache;
import uk.gov.moj.cpp.hearing.query.api.service.referencedata.ReferenceDataService;
import uk.gov.moj.cpp.hearing.query.api.service.referencedata.XhibitEventMapperCache;
import uk.gov.moj.cpp.hearing.query.view.HearingEventQueryView;
import uk.gov.moj.cpp.hearing.query.view.HearingQueryView;
import uk.gov.moj.cpp.hearing.query.view.SessionTimeQueryView;
import uk.gov.moj.cpp.hearing.query.view.response.SessionTimeResponse;
import uk.gov.moj.cpp.hearing.query.view.response.Timeline;
import uk.gov.moj.cpp.hearing.query.view.response.TimelineHearingSummary;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.GetShareResultsV2Response;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.NowListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.ProsecutionCaseResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.TargetListResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.BadRequestException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ServiceComponent(Component.QUERY_API)
public class HearingQueryApi {
    public static final String STAGINGENFORCEMENT_QUERY_OUTSTANDING_FINES = "stagingenforcement.defendant.outstanding-fines";
    private static final Logger LOGGER = LoggerFactory.getLogger(HearingQueryApi.class);
    private static final String FIELD_HEARING_EVENT_DEFINITION_ID = "hearingEventDefinitionId";
    private static final String EVENT_LOG_COUNT_FAILED = "Hearing Event Log Count failed due to Organisation ID mismatch";
    private static final String EVENT_LOG_FAILED = "Hearing Event Log For Document failed due to Organisation ID mismatch";
    private static final String REASON = "reason";
    private static final String GET_HEARING_EVENT_LOG_COUNT = "hearing.get-hearing-event-log-count";
    private static final String GET_HEARING_EVENT_LOG_FOR_DOCUMENTS= "hearing.get-hearing-event-log-for-documents";

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
    private ReferenceDataService referenceDataService;

    @Inject
    private XhibitEventMapperCache xhibitEventMapperCache;

    @Inject
    private PIEventMapperCache piEventMapperCache;

    @Inject
    private UsersAndGroupsService usersAndGroupsService;

    @Inject
    private AccessibleCases accessibleCases;

    @Inject
    private UserGroupQueryService userGroupQueryService;

    @Inject
    private AccessibleApplications accessibleApplications;

    @Inject
    private DDJChecker ddjChecker;

    @Inject
    private RecorderChecker recorderChecker;

    @Inject
    private ProgressionService progressionService;

    @Handles("hearing.get.hearings")
    public JsonEnvelope findHearings(final JsonEnvelope query) {

        final Optional<String> optionalUserId = query.metadata().userId();
        if (!optionalUserId.isPresent()) {
            throw new BadRequestException("No Logged in UserId found to perform hearings search");
        }
        final String userId = optionalUserId.get();
        final Permissions permissions = usersAndGroupsService.permissions(userId);
        final boolean isDDJorRecorder = isDDJorRecorder(permissions);
        final List<UUID> accessibleCasesAndApplications = getAccessibleCasesAndApplications(userId, isDDJorRecorder, permissions);
        final Envelope<GetHearings> envelope = this.hearingQueryView.findHearings(query, accessibleCasesAndApplications, isDDJorRecorder);
        return getJsonEnvelope(envelope);
    }

    @Handles("hearing.get.hearings-for-today")
    public JsonEnvelope findHearingsForToday(final JsonEnvelope query) {
        final Envelope<GetHearings> envelope = this.hearingQueryView.findHearingsForToday(query);
        return getJsonEnvelope(envelope);
    }

    @Handles("hearing.get.hearings-for-future")
    public JsonEnvelope findHearingsForFuture(final JsonEnvelope query) {
        final HearingTypes hearingTypes = referenceDataService.getAllHearingTypes();

        final Envelope<GetHearings> envelope = this.hearingQueryView.findHearingsForFuture(query, hearingTypes);
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
        final Permissions permissions = usersAndGroupsService.permissions(userId);

        final boolean ddJorRecorder = isDDJorRecorder(permissions);

        final List<UUID> accessibleCasesAndApplications = getAccessibleCasesAndApplications(userId, ddJorRecorder, permissions);
        final Envelope<HearingDetailsResponse> envelope = this.hearingQueryView.findHearing(query, crackedIneffectiveVacatedTrialTypes, accessibleCasesAndApplications, ddJorRecorder);
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

    @SuppressWarnings("squid:S3655")
    @Handles(GET_HEARING_EVENT_LOG_FOR_DOCUMENTS)
    public JsonEnvelope getHearingEventLogForDocuments(final JsonEnvelope query) {
        final UUID userId = query.metadata().userId().isPresent() ? fromString(query.metadata().userId().get()) : null;
        final boolean flag = userGroupQueryService.doesUserBelongsToHmctsOrganisation(userId);
        if(flag) {
            return getJsonEnvelope(this.hearingEventQueryView.getHearingEventLogForDocuments(query));
        } else {
            return getJsonEnvelope(envelop(createObjectBuilder().add(REASON, EVENT_LOG_FAILED).build())
                    .withName(GET_HEARING_EVENT_LOG_FOR_DOCUMENTS)
                    .withMetadataFrom(query));
        }
    }

    @SuppressWarnings("squid:S3655")
    @Handles(GET_HEARING_EVENT_LOG_COUNT)
    public JsonEnvelope getHearingEventLogCount(final JsonEnvelope query) {
        final UUID userId = query.metadata().userId().isPresent() ? fromString(query.metadata().userId().get()) : null;
        final boolean flag = userGroupQueryService.doesUserBelongsToHmctsOrganisation(userId);
        if(flag) {
            return getJsonEnvelope(this.hearingEventQueryView.getHearingEventLogCount(query));
        } else {
            return getJsonEnvelope(envelop(createObjectBuilder().add(REASON, EVENT_LOG_COUNT_FAILED).build())
                    .withName(GET_HEARING_EVENT_LOG_COUNT)
                    .withMetadataFrom(query));
        }
    }

    @Handles("hearing.get-draft-result")
    public JsonEnvelope getDraftResult(final JsonEnvelope query) {
        final Envelope<TargetListResponse> envelope = this.hearingQueryView.getDraftResult(query);
        return getJsonEnvelope(envelope);
    }

    @Handles("hearing.get-draft-result-v2")
    public JsonEnvelope getDraftResultV2(final JsonEnvelope query) {
        return this.hearingQueryView.getDraftResultV2(query);
    }

    @Handles("hearing.get-share-result-v2")
    public JsonEnvelope getShareResultV2(final JsonEnvelope query) {
        final Envelope<GetShareResultsV2Response> envelope = this.hearingQueryView.getShareResultsV2(query);
        return getJsonEnvelope(envelope);
    }

    @Handles("hearing.get-results")
    public JsonEnvelope getResults(final JsonEnvelope query) {
        final Envelope<TargetListResponse> envelope = this.hearingQueryView.getResults(query);
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
        final Optional<UUID> caseId = getUUID(query.payloadAsJsonObject(), "id");
        final Envelope<Timeline> timelineForCase = this.hearingQueryView.getTimeline(query, crackedIneffectiveVacatedTrialTypes, allCourtRooms);

        final List<TimelineHearingSummary> summaryToRemove = new ArrayList<>();
        timelineForCase.payload().getHearingSummaries().stream().filter(timelineHearingSummary -> "Review".equals(timelineHearingSummary.getHearingType())).findFirst().ifPresent(rev -> {
            final ProsecutionCase prosecutionCaseDetails = progressionService.getProsecutionCaseDetails(caseId.get());
            if (prosecutionCaseDetails.getLinkedApplicationsSummary() != null && !prosecutionCaseDetails.getLinkedApplicationsSummary().isEmpty()) {
                summaryToRemove.add(rev);
            }
        });

        final List<TimelineHearingSummary> allTimelineHearingSummaries = new ArrayList<>(timelineForCase.payload().getHearingSummaries());

        summaryToRemove.stream().forEach(summary -> {
            final Optional<TimelineHearingSummary> summaryOptional = allTimelineHearingSummaries.stream().filter(qualifiedSummary -> qualifiedSummary.getHearingId().equals(summary.getHearingId())).findFirst();
            summaryOptional.ifPresent(allTimelineHearingSummaries::remove);
        });


        final Timeline timeline = new Timeline(allTimelineHearingSummaries);

        return getJsonEnvelope(envelop(timeline)
                .withName("hearing.timeline")
                .withMetadataFrom(query));
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
        final JsonEnvelope viewResponseEnvelope = this.hearingQueryView.getOutstandingFinesQueryFromDefendantId(query);
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



    @Handles("hearing.query.reusable-info")
    public JsonEnvelope getReusableInfo(final JsonEnvelope query) {
        final JsonObject payload = query.payloadAsJsonObject();
        final Map<String, String> countryCodesMap = referenceDataService.getCountryCodesMap();
        final List<Prompt> prompts = referenceDataService.getCacheableResultPrompts(getString(payload, "orderedDate"));
        return this.hearingQueryView.getReusableInformation(query, prompts, countryCodesMap);
    }

    @Handles("hearing.custody-time-limit")
    public JsonEnvelope retrieveCustodyTimeLimit(final JsonEnvelope query) {
        return this.hearingQueryView.retrieveCustodyTimeLimit(query);
    }

    @SuppressWarnings({"squid:S2629", "squid:CallToDeprecatedMethod"})
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

    private List<UUID> getAccessibleCasesAndApplications(final String userId, final boolean isDDJorRecorder, final Permissions permissions) {
        final List<UUID> accessibleCasesAndApplications = new ArrayList<>();
        if (isDDJorRecorder) {
            accessibleCasesAndApplications.addAll(accessibleCases.findCases(permissions, userId));
            accessibleCasesAndApplications.addAll(accessibleApplications.findApplications(permissions, userId));
        }
        return accessibleCasesAndApplications;
    }

    private boolean isDDJorRecorder(final Permissions permissions) {
        final boolean isDDJ = ddjChecker.isDDJ(permissions);
        final boolean isRecorder = recorderChecker.isRecorder(permissions);
        return isDDJ || isRecorder;
    }

    @Handles("hearing.get.future-hearings")
    public JsonEnvelope getFutureHearingsByCaseIds(final JsonEnvelope query) {
        final Envelope<GetHearings> envelope = this.hearingQueryView.getFutureHearingsByCaseIds(query);
        return getJsonEnvelope(envelope);
    }

    @Handles("hearing.prosecution-case-by-hearingid")
    public JsonEnvelope getProsecutionCaseForHearing(final JsonEnvelope query) {

        final Optional<UUID> hearingEventDefinitionId = getUUID(query.payloadAsJsonObject(), FIELD_HEARING_EVENT_DEFINITION_ID);
        final Set<UUID> cppHearingEventIds = piEventMapperCache.getCppHearingEventIds();
        if ((hearingEventDefinitionId.isPresent()) && cppHearingEventIds.contains(hearingEventDefinitionId.get())) {
            final Envelope<ProsecutionCaseResponse> envelope = this.hearingQueryView.getProsecutionCaseForHearing(query);
            return getJsonEnvelope(envelope);
        }
        return query;
    }
}
