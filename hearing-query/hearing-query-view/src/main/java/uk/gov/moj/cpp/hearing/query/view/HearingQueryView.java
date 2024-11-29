package uk.gov.moj.cpp.hearing.query.view;

import static java.time.LocalDate.now;
import static java.util.Objects.nonNull;
import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static javax.json.Json.createObjectBuilder;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.justice.services.core.annotation.Component.QUERY_VIEW;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjects.getString;
import static uk.gov.justice.services.messaging.JsonObjects.getUUID;

import uk.gov.justice.core.courts.CrackedIneffectiveTrial;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.hearing.courts.GetHearings;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.LocalDates;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.DefendantInfoQueryResult;
import uk.gov.moj.cpp.hearing.domain.OutstandingFinesQuery;
import uk.gov.moj.cpp.hearing.domain.referencedata.HearingType;
import uk.gov.moj.cpp.hearing.domain.referencedata.HearingTypes;
import uk.gov.moj.cpp.hearing.dto.DefendantSearch;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialTypes;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.query.view.response.Timeline;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.DraftResultResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.GetShareResultsV2Response;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.NowListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.ProsecutionCaseResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.TargetListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;
import uk.gov.moj.cpp.hearing.query.view.service.HearingService;
import uk.gov.moj.cpp.hearing.query.view.service.ReusableInfoService;
import uk.gov.moj.cpp.hearing.query.view.service.ctl.CTLExpiryDateCalculatorService;
import uk.gov.moj.cpp.hearing.repository.CourtListPublishStatusResult;
import uk.gov.moj.cpp.hearing.repository.CourtListRepository;
import uk.gov.moj.cpp.hearing.repository.DefendantRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.NoResultException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SuppressWarnings({"squid:S3655", "squid:CallToDeprecatedMethod"})
public class HearingQueryView {

    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_HEARING_DAY = "hearingDay";
    private static final String FIELD_BAIL_STATUS_CODE = "bailStatusCode";
    private static final String FIELD_DEFENDANT_ID = "defendantId";
    private static final String FIELD_DATE = "date";
    private static final String FIELD_COURT_CENTRE_ID = "courtCentreId";
    private static final String FIELD_COURT_CENTRE_IDS = "courtCentreIds";
    private static final String DATE_OF_HEARING = "dateOfHearing";
    private static final String FIELD_COURT_ROOM_IDS = "courtRoomIds";
    private static final String FIELD_HEARING_DATE = "hearingDate";
    private static final String FIELD_ROOM_ID = "roomId";
    private static final String FIELD_START_TIME = "startTime";
    private static final String FIELD_END_TIME = "endTime";
    private static final String FIELD_QUERY = "q";
    private static final String FIELD_ID = "id";
    private static final String FIELD_OFFENCE_ID = "offenceId";
    private static final String FIELD_CUSTODY_TIME_LIMIT = "custodyTimeLimit";
    private static final String FIELD_CASE_IDS = "caseIds";
    private static final Logger LOGGER = LoggerFactory.getLogger(HearingQueryView.class);

    @Inject
    private HearingService hearingService;


    @Inject
    private DefendantRepository defendantRepository;
    @Inject
    private Enveloper enveloper;
    @Inject
    private ObjectToJsonValueConverter objectToJsonValueConverter;
    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;
    @Inject
    private ObjectMapper mapper;
    @Inject
    private ReusableInfoService reusableInfoService;

    @Inject
    private CTLExpiryDateCalculatorService ctlExpiryDateCalculatorService;

    @Inject
    private CourtListRepository courtListRepository;

    @Inject
    private UtcClock utcClock;

    @Inject
    @ServiceComponent(QUERY_VIEW)
    private Requester requester;

    public Envelope<GetHearings> findHearings(final JsonEnvelope envelope,
                                              final List<UUID> accessibleCasesAndApplicationIds,
                                              final boolean isDDJorRecorder) {
        final JsonObject payload = envelope.payloadAsJsonObject();
        final LocalDate date = LocalDates.from(payload.getString(FIELD_DATE));
        final UUID courtCentreId = UUID.fromString(payload.getString(FIELD_COURT_CENTRE_ID));
        final UUID roomId = payload.containsKey(FIELD_ROOM_ID) ? UUID.fromString(payload.getString(FIELD_ROOM_ID)) : null;
        final String startTime = payload.containsKey(FIELD_START_TIME) ? payload.getString(FIELD_START_TIME) : "00:00";
        final String endTime = payload.containsKey(FIELD_END_TIME) ? payload.getString(FIELD_END_TIME) : "23:59";

        final GetHearings hearingListResponse = hearingService.getHearings(date, startTime, endTime, courtCentreId, roomId, accessibleCasesAndApplicationIds, isDDJorRecorder);
        return envelop(hearingListResponse)
                .withName("hearing.get.hearings")
                .withMetadataFrom(envelope);
    }

    @SuppressWarnings({"squid:S3655"})
    public Envelope<GetHearings> findHearingsForToday(final JsonEnvelope envelope) {
        final GetHearings hearingListResponse = hearingService.getHearingsForToday(now(), fromString(envelope.metadata().userId().get()));

        return envelop(hearingListResponse)
                .withName("hearing.get.hearings-for-today")
                .withMetadataFrom(envelope);
    }

    @SuppressWarnings({"squid:S3655"})
    public Envelope<GetHearings> findHearingsForFuture(final JsonEnvelope envelope, final HearingTypes hearingTypes) {
        final List<HearingType> hearingTypeList = hearingTypes.getHearingTypes().stream().filter(HearingType::getTrialTypeFlag).collect(Collectors.toList());
        final String defendantId = envelope.payloadAsJsonObject().getString(FIELD_DEFENDANT_ID);
        final GetHearings hearingListResponse = hearingService.getHearingsForFuture(now(), UUID.fromString(defendantId), hearingTypeList);

        return envelop(hearingListResponse)
                .withName("hearing.get.hearings-for-future")
                .withMetadataFrom(envelope);
    }

    public Envelope<HearingDetailsResponse> findHearing(final JsonEnvelope envelope,
                                                        final CrackedIneffectiveVacatedTrialTypes crackedIneffectiveVacatedTrialTypes,
                                                        final List<UUID> accessibleCaseAndApplicationIds,
                                                        final boolean isDDJ) {
        final Optional<UUID> hearingId = getUUID(envelope.payloadAsJsonObject(), FIELD_HEARING_ID);
        final HearingDetailsResponse hearingDetailsResponse = hearingService.getHearingDetailsResponseById(hearingId.get(), crackedIneffectiveVacatedTrialTypes, accessibleCaseAndApplicationIds, isDDJ);

        return envelop(hearingDetailsResponse)
                .withName("hearing.get-hearing")
                .withMetadataFrom(envelope);
    }


    public Envelope<TargetListResponse> getDraftResult(final JsonEnvelope envelope) {
        final UUID hearingId = fromString(envelope.payloadAsJsonObject().getString(FIELD_HEARING_ID));
        final TargetListResponse targetListResponse = hearingService.getTargets(hearingId);

        return envelop(targetListResponse)
                .withName("hearing.get-draft-result")
                .withMetadataFrom(envelope);
    }



    public JsonEnvelope getDraftResultV2(final JsonEnvelope envelope) {
        final UUID hearingId = fromString(envelope.payloadAsJsonObject().getString(FIELD_HEARING_ID));
        final String hearingDay = envelope.payloadAsJsonObject().getString(FIELD_HEARING_DAY);
        final DraftResultResponse draftResult = hearingService.getDraftResult(hearingId, hearingDay);

        if (draftResult.isTarget()) {
            return enveloper.withMetadataFrom(envelope, "hearing.results")
                    .apply(draftResult.getPayload());
        } else {
            return enveloper.withMetadataFrom(envelope, "hearing.get-draft-result-v2")
                    .apply(draftResult.getPayload());
        }

    }

    public Envelope<GetShareResultsV2Response> getShareResultsV2(final JsonEnvelope envelope) {
        final UUID hearingId = fromString(envelope.payloadAsJsonObject().getString(FIELD_HEARING_ID));
        final String hearingDay = envelope.payloadAsJsonObject().getString(FIELD_HEARING_DAY);
        final GetShareResultsV2Response response = hearingService.getShareResultsByDate(hearingId, hearingDay);

        return envelop(response)
                .withName("hearing.get-share-result-v2")
                .withMetadataFrom(envelope);
    }

    public Envelope<TargetListResponse> getResults(final JsonEnvelope envelope) {
        final UUID hearingId = fromString(envelope.payloadAsJsonObject().getString(FIELD_HEARING_ID));
        final String hearingDay = envelope.payloadAsJsonObject().getString(FIELD_HEARING_DAY);
        final TargetListResponse targetListResponse = hearingService.getTargetsByDate(hearingId, hearingDay);

        return envelop(targetListResponse)
                .withName("hearing.results")
                .withMetadataFrom(envelope);
    }

    public JsonEnvelope searchByMaterialId(final JsonEnvelope envelope) {
        return enveloper.withMetadataFrom(envelope, "hearing.query.search-by-material-id")
                .apply(hearingService.getNowsRepository(envelope.payloadAsJsonObject().getString(FIELD_QUERY)));
    }


    public JsonEnvelope retrieveSubscriptions(final JsonEnvelope envelope) {

        final String referenceDate = envelope.payloadAsJsonObject().getString("referenceDate");

        final String nowTypeId = envelope.payloadAsJsonObject().getString("nowTypeId");

        return enveloper.withMetadataFrom(envelope, "hearing.retrieve-subscriptions")
                .apply(hearingService.getSubscriptions(referenceDate, nowTypeId));
    }


    public Envelope<NowListResponse> findNows(final JsonEnvelope envelope) {
        final UUID hearingId = fromString(envelope.payloadAsJsonObject().getString(FIELD_HEARING_ID));
        final Optional<Hearing> optionalHearing = hearingService.getHearingById(hearingId);

        if (!optionalHearing.isPresent()) {
            return envelop((NowListResponse) null)
                    .withName("hearing.get-nows")
                    .withMetadataFrom(envelope);
        }
        final NowListResponse nowListResponse = hearingService.getNows(hearingId);

        return envelop(nowListResponse)
                .withName("hearing.get-nows")
                .withMetadataFrom(envelope);
    }


    public Envelope<CrackedIneffectiveTrial> getCrackedIneffectiveTrialReason(final JsonEnvelope envelope, final CrackedIneffectiveVacatedTrialTypes crackedIneffectiveVacatedTrialTypes) {


        final Optional<UUID> trialTypeId = getUUID(envelope.payloadAsJsonObject(), "trialTypeId");
        return envelop(hearingService.fetchCrackedIneffectiveTrial(trialTypeId.get(), crackedIneffectiveVacatedTrialTypes))
                .withName("hearing.get-cracked-ineffective-reason")
                .withMetadataFrom(envelope);
    }


    public Envelope<Timeline> getTimeline(final JsonEnvelope envelope, final CrackedIneffectiveVacatedTrialTypes crackedIneffectiveVacatedTrialTypes, final JsonObject allCourtRooms) {
        final Optional<UUID> caseId = getUUID(envelope.payloadAsJsonObject(), FIELD_ID);

        final Timeline timeline = hearingService.getTimeLineByCaseId(caseId.get(), crackedIneffectiveVacatedTrialTypes, allCourtRooms);


        return envelop(timeline)
                .withName("hearing.timeline")
                .withMetadataFrom(envelope);
    }


    public Envelope<Timeline> getTimelineByApplicationId(final JsonEnvelope envelope, final CrackedIneffectiveVacatedTrialTypes crackedIneffectiveVacatedTrialTypes, final JsonObject allCourtRooms) {
        final Optional<UUID> applicationId = getUUID(envelope.payloadAsJsonObject(), FIELD_ID);

        final Timeline timeline = hearingService.getTimeLineByApplicationId(applicationId.get(), crackedIneffectiveVacatedTrialTypes, allCourtRooms);

        return envelop(timeline)
                .withName("hearing.timeline")
                .withMetadataFrom(envelope);
    }


    public JsonEnvelope getCourtListPublishStatus(final JsonEnvelope query) {
        final String courtCentreId = query.payloadAsJsonObject().getString(FIELD_COURT_CENTRE_ID);

        final Optional<CourtListPublishStatusResult> publishCourtListStatus = courtListRepository.courtListPublishStatuses(fromString(courtCentreId));

        final JsonObjectBuilder builder = createObjectBuilder();
        if (publishCourtListStatus.isPresent()) {
            builder.add(FIELD_COURT_CENTRE_ID, publishCourtListStatus.get().getCourtCentreId().toString())
                    .add("lastUpdated", publishCourtListStatus.get().getLastUpdated().toString())
                    .add("publishStatus", publishCourtListStatus.get().getPublishStatus().toString())
                    .add("errorMessage", defaultIfEmpty(publishCourtListStatus.get().getFailureMessage(), ""));
        }
        return enveloper.withMetadataFrom(query, "hearing.court.list.publish.status").apply(createObjectBuilder().add("publishCourtListStatus", builder.build()).build());
    }


    public JsonEnvelope getLatestHearingsByCourtCentres(final JsonEnvelope envelope,
                                                        final Set<UUID> cppHearingEventIds) {
        final Optional<String> courtCentreIds = getString(envelope.payloadAsJsonObject(), FIELD_COURT_CENTRE_IDS);
        final Optional<String> dateOfHearing = getString(envelope.payloadAsJsonObject(), DATE_OF_HEARING);

        final List<UUID> courtCentreList = Stream.of(courtCentreIds.get().split(",")).map(UUID::fromString).collect(Collectors.toList());

        final Optional<CurrentCourtStatus> currentCourtStatus = hearingService.getHearingsForWebPage(courtCentreList, LocalDate.parse(dateOfHearing.get()), cppHearingEventIds);

        return enveloper.withMetadataFrom(envelope, "hearing.get-latest-hearings-by-court-centres").apply(currentCourtStatus.isPresent() ? currentCourtStatus.get() : createObjectBuilder().build());
    }

    @SuppressWarnings({"squid:CallToDeprecatedMethod", "squid:CallToDeprecatedMethod"})
    public JsonEnvelope getHearingsForCourtCentresForDate(final JsonEnvelope envelope, final Set<UUID> cppHearingEventIds) {
        final Optional<String> courtCentreId = getString(envelope.payloadAsJsonObject(), FIELD_COURT_CENTRE_IDS);
        final Optional<String> dateOfHearing = getString(envelope.payloadAsJsonObject(), DATE_OF_HEARING);

        final List<UUID> courtCentreList = Stream.of(courtCentreId.get().split(",")).map(UUID::fromString).collect(Collectors.toList());

        final Optional<CurrentCourtStatus> currentCourtStatus = hearingService.getHearingsByDate(courtCentreList, LocalDate.parse(dateOfHearing.get()), cppHearingEventIds);

        return enveloper.withMetadataFrom(envelope, "hearing.hearings-court-centres-for-date").apply(currentCourtStatus.isPresent() ? currentCourtStatus.get() : createObjectBuilder().build());
    }

    @SuppressWarnings("squid:S1166")

    public JsonEnvelope getOutstandingFinesQueryFromDefendantId(final JsonEnvelope envelope) {
        final Optional<UUID> defendantId = getUUID(envelope.payloadAsJsonObject(), FIELD_DEFENDANT_ID);
        final JsonEnvelope jsonEnvelopeWithoutPayload = envelopeFrom(envelope.metadata(), Json.createObjectBuilder().build());
        if (defendantId.isPresent()) {
            try {
                final DefendantSearch defendantSearch = defendantRepository.getDefendantDetailsForSearching(defendantId.get());
                final JsonObject build = convertToOutstandingFinesQuery(defendantSearch);
                return envelopeFrom(envelope.metadata(), build);
            } catch (final NoResultException ex) {
                LOGGER.error(String.format("No defendant found with defendantId  ='%s'", defendantId.get()), ex);
                return jsonEnvelopeWithoutPayload;
            }
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("No defendant id found in the payload");
        }
        return jsonEnvelopeWithoutPayload;
    }

    private JsonObject convertToOutstandingFinesQuery(final DefendantSearch defendantSearch) {
        final JsonObjectBuilder objectBuilder = createObjectBuilder();
        objectBuilder.add("defendantId", defendantSearch.getDefendantId().toString());
        if (isEmpty(defendantSearch.getLegalEntityOrganizationName())) {
            objectBuilder.add("firstname", defendantSearch.getForename());
            objectBuilder.add("lastname", defendantSearch.getSurname());
            if (defendantSearch.getDateOfBirth() != null) {
                objectBuilder.add("dob", defendantSearch.getDateOfBirth().toString());
            }
            if (!isEmpty(defendantSearch.getNationalInsuranceNumber())) {
                objectBuilder.add("ninumber", defendantSearch.getNationalInsuranceNumber());
            }
        } else {
            objectBuilder.add("organization", defendantSearch.getLegalEntityOrganizationName());
        }
        final JsonObject build = objectBuilder.build();
        return build;
    }

    @SuppressWarnings("squid:S1166")
    public JsonEnvelope getDefendantInfoFromCourtHouseId(final JsonEnvelope envelope) {

        final JsonObject payload = envelope.payloadAsJsonObject();

        final OutstandingFinesQuery outstandingFinesQuery = OutstandingFinesQuery.newBuilder()
                .withCourtCentreId(UUID.fromString(payload.getString(FIELD_COURT_CENTRE_ID)))
                .withCourtRoomIds(Stream.of(payload.getString(FIELD_COURT_ROOM_IDS).split(","))
                        .map(UUID::fromString)
                        .collect(toList()))
                .withHearingDate(LocalDate.parse(payload.getString(FIELD_HEARING_DATE)))
                .build();

        try {
            final DefendantInfoQueryResult result = hearingService.getHearingsByCourtRoomList(outstandingFinesQuery.getHearingDate(), outstandingFinesQuery.getCourtCentreId(), outstandingFinesQuery.getCourtRoomIds());
            return envelopeFrom(envelope.metadata(), objectToJsonValueConverter.convert(result));


        } catch (final NoResultException nre) {
            LOGGER.error("### No defendant found with {}. Exception: {}", envelope.toObfuscatedDebugString(), nre);
            return envelopeFrom(envelope.metadata(), Json.createObjectBuilder().build());
        }
    }

    public JsonEnvelope getReusableInformation(final JsonEnvelope envelope, final List<Prompt> prompts, final Map<String, String> countryCodesMap) {
        final JsonObject payload = envelope.payloadAsJsonObject();
        final UUID hearingId = UUID.fromString(payload.getString(FIELD_HEARING_ID));
        final Optional<uk.gov.justice.core.courts.Hearing> hearingEntity = hearingService.getHearingDomainById(hearingId);

        if (!hearingEntity.isPresent()) {
            return envelopeFrom(envelope.metadata(), Json.createObjectBuilder().build());
        }

        final uk.gov.justice.core.courts.Hearing hearing = hearingEntity.get();
        final Map<UUID, Defendant> defendants = hearing.getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .collect(toMap(Defendant::getMasterDefendantId, defendant -> defendant, (defendant1, defendant2) -> defendant1));

        final List<JsonObject> reusableCaseDetailPrompts = reusableInfoService.getCaseDetailReusableInformation(hearing.getProsecutionCases(), prompts, countryCodesMap);
        if(hearing.getCourtApplications() != null) {
            reusableCaseDetailPrompts.addAll(reusableInfoService.getApplicationDetailReusableInformation(hearing.getCourtApplications(), prompts));
        }
        final JsonObject reusableViewStorePrompts = reusableInfoService.getViewStoreReusableInformation(defendants.values(), reusableCaseDetailPrompts);
        return envelopeFrom(envelope.metadata(), reusableViewStorePrompts);
    }

    public JsonEnvelope retrieveCustodyTimeLimit(final JsonEnvelope envelope) {

        final JsonObject payload = envelope.payloadAsJsonObject();
        final UUID hearingId = UUID.fromString(payload.getString(FIELD_HEARING_ID));
        final UUID offenceId = UUID.fromString(payload.getString(FIELD_OFFENCE_ID));
        final LocalDate hearingDay = LocalDate.parse(envelope.payloadAsJsonObject().getString(FIELD_HEARING_DAY));
        final String bailStatusCode = envelope.payloadAsJsonObject().getString(FIELD_BAIL_STATUS_CODE);

        final Hearing hearing = hearingService.getHearingById(hearingId)
                .orElseThrow(() -> new RuntimeException("Hearing not found for hearing id: " + hearingId));

        final LocalDate expiryDate = getCTLExpiryDate(hearing, offenceId, hearingDay, bailStatusCode);

        if (nonNull(expiryDate)) {

            final JsonObject ctlExpiryDate = createObjectBuilder()
                    .add(FIELD_CUSTODY_TIME_LIMIT, expiryDate.toString())
                    .build();

            return envelopeFrom(envelope.metadata(), ctlExpiryDate);

        } else {
            return envelopeFrom(envelope.metadata(), Json.createObjectBuilder().build());
        }

    }

    private LocalDate getCTLExpiryDate(final Hearing hearing, final UUID offenceId, final LocalDate hearingDay, final String bailStatusCode) {

        if (ctlExpiryDateCalculatorService.avoidCalculation(hearing, offenceId)) {
            return null;
        }

        final uk.gov.moj.cpp.hearing.persist.entity.ha.Offence offence = hearing.getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .flatMap(defendant -> defendant.getOffences().stream())
                .filter(off -> off.getId().getId().equals(offenceId))
                .findFirst().orElseThrow(() -> new RuntimeException("Offence not found for offence id: " + offenceId));

        return ctlExpiryDateCalculatorService.calculateCTLExpiryDate(offence, hearingDay, bailStatusCode).orElse(null);

    }


    public Envelope<GetHearings> getFutureHearingsByCaseIds(final JsonEnvelope envelope) {
        final Optional<String> caseIds = getString(envelope.payloadAsJsonObject(), FIELD_CASE_IDS);
        final List<UUID> caseIdList = Stream.of(caseIds.get().split(",")).map(UUID::fromString).collect(Collectors.toList());

        final GetHearings hearingListResponse = hearingService.getFutureHearingsByCaseIds(caseIdList);

        return envelop(hearingListResponse)
                .withName("hearing.get.hearings")
                .withMetadataFrom(envelope);
    }

    public Envelope<ProsecutionCaseResponse> getProsecutionCaseForHearing(final JsonEnvelope envelope) {

        final Optional<UUID> hearingId = getUUID(envelope.payloadAsJsonObject(), FIELD_HEARING_ID);

        final ProsecutionCaseResponse prosecutionCaseResponse = hearingService.getProsecutionCaseForHearings(hearingId.get());

        return envelop(prosecutionCaseResponse)
                .withName("hearing.get-prosecutioncase-result")
                .withMetadataFrom(envelope);
    }
}
