package uk.gov.moj.cpp.hearing.query.view.service;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import uk.gov.justice.core.courts.CrackedIneffectiveTrial;
import uk.gov.justice.hearing.courts.GetHearings;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.moj.cpp.hearing.domain.CourtRoom;
import uk.gov.moj.cpp.hearing.domain.DefendantDetail;
import uk.gov.moj.cpp.hearing.domain.DefendantInfoQueryResult;
import uk.gov.moj.cpp.hearing.domain.HearingState;
import uk.gov.moj.cpp.hearing.domain.notification.Subscriptions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialType;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialTypes;
import uk.gov.moj.cpp.hearing.mapping.DraftResultJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.ResultLineJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.TargetJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.application.ApplicationDraftResult;
import uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DraftResult;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingYouthCourtDefendants;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Now;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsMaterial;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Person;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;
import uk.gov.moj.cpp.hearing.persist.entity.heda.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.persist.entity.not.Document;
import uk.gov.moj.cpp.hearing.persist.entity.not.Subscription;
import uk.gov.moj.cpp.hearing.query.view.helper.TimelineHearingSummaryHelper;
import uk.gov.moj.cpp.hearing.query.view.response.Timeline;
import uk.gov.moj.cpp.hearing.query.view.response.TimelineHearingSummary;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.ApplicationTarget;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.ApplicationTargetListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.DraftResultResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.GetShareResultsV2Response;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.NowListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.NowResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.ResultLine;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.TargetListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;
import uk.gov.moj.cpp.hearing.repository.DocumentRepository;
import uk.gov.moj.cpp.hearing.repository.DraftResultRepository;
import uk.gov.moj.cpp.hearing.repository.HearingEventDefinitionRepository;
import uk.gov.moj.cpp.hearing.repository.HearingEventPojo;
import uk.gov.moj.cpp.hearing.repository.HearingEventRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.repository.HearingYouthCourtDefendantsRepository;
import uk.gov.moj.cpp.hearing.repository.NowRepository;
import uk.gov.moj.cpp.hearing.repository.NowsMaterialRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.transaction.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("squid:S1612")
public class HearingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HearingService.class);
    private static final DateTimeFormatter FORMATTER = ofPattern("ddMMyyyy");
    private static final ZoneId ZONE_ID = ZoneId.of(ZoneOffset.UTC.getId());
    @Inject
    private HearingRepository hearingRepository;
    @Inject
    private HearingEventRepository hearingEventRepository;
    @Inject
    private HearingEventDefinitionRepository hearingEventDefinitionRepository;
    @Inject
    HearingYouthCourtDefendantsRepository hearingYouthCourtDefendantsRepository;
    @Inject
    private NowRepository nowRepository;
    @Inject
    private NowsMaterialRepository nowsMaterialRepository;
    @Inject
    private DocumentRepository documentRepository;
    @Inject
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;
    @Inject
    private StringToJsonObjectConverter stringToJsonObjectConverter;
    @Inject
    private HearingJPAMapper hearingJPAMapper;
    @Inject
    private TargetJPAMapper targetJPAMapper;
    @Inject
    private ResultLineJPAMapper resultLineJPAMapper;
    @Inject
    private DraftResultJPAMapper draftResultJPAMapper;
    @Inject
    private GetHearingsTransformer getHearingTransformer;
    @Inject
    private TimelineHearingSummaryHelper timelineHearingSummaryHelper;
    @Inject
    private HearingListXhibitResponseTransformer hearingListXhibitResponseTransformer;
    @Inject
    private FilterHearingsBasedOnPermissions filterHearingsBasedOnPermissions;
    @Inject
    private DraftResultRepository draftResultRepository;
    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    public Optional<CurrentCourtStatus> getHearingsForWebPage(final List<UUID> courtCentreList,
                                                              final LocalDate localDate,
                                                              final Set<UUID> cppHearingEventIds) {

        final List<HearingEventPojo> hearingEventPojos = hearingEventRepository.findLatestHearingsForThatDay(courtCentreList, localDate, cppHearingEventIds);
        final List<HearingEvent> activeHearingEventList = getHearingEvents(hearingEventPojos);
        final List<uk.gov.justice.core.courts.Hearing> hearingList = activeHearingEventList
                .stream()
                .map(hearingEvent -> hearingRepository.findBy(hearingEvent.getHearingId()))
                .map(ha -> hearingJPAMapper.fromJPAWithCourtListRestrictions(ha))
                .collect(toList());


        if (!hearingList.isEmpty()) {
            final HearingEventsToHearingMapper hearingEventsToHearingMapper = new HearingEventsToHearingMapper(activeHearingEventList, hearingList, activeHearingEventList);
            return Optional.of(hearingListXhibitResponseTransformer.transformFrom(hearingEventsToHearingMapper));
        }
        return empty();
    }

    public Optional<CurrentCourtStatus> getHearingsByDate(final List<UUID> courtCentreList,
                                                          final LocalDate localDate,
                                                          final Set<UUID> cppHearingEventIds) {

        final List<HearingEventPojo> hearingEventPojos = hearingEventRepository.findLatestHearingsForThatDay(courtCentreList, localDate, cppHearingEventIds);
        final List<HearingEvent> activeHearingEventList = getHearingEvents(hearingEventPojos);

        final List<Hearing> hearingsForDate = hearingRepository.findHearingsByDateAndCourtCentreList(localDate, courtCentreList);
        final List<uk.gov.justice.core.courts.Hearing> hearingList = hearingsForDate
                .stream()
                .map(ha -> hearingJPAMapper.fromJPAWithCourtListRestrictions(ha))
                .collect(toList());

        final List<HearingEvent> allHearingEvents = hearingEventRepository.findBy(courtCentreList, localDate.atStartOfDay(ZoneOffset.UTC), cppHearingEventIds);

        if (!hearingList.isEmpty()) {
            final HearingEventsToHearingMapper hearingEventsToHearingMapper = new HearingEventsToHearingMapper(activeHearingEventList, hearingList, allHearingEvents);
            final CurrentCourtStatus currentCourtStatus = hearingListXhibitResponseTransformer.transformFrom(hearingEventsToHearingMapper);
            return Optional.of(currentCourtStatus);
        }
        return empty();
    }


    @Transactional
    public GetHearings getHearings(final LocalDate date, final String startTime,
                                   final String endTime, final UUID courtCentreId,
                                   final UUID roomId, final List<UUID> accessibleCasesId,
                                   final boolean isDDJorRecorder) {

        if (null == date || null == courtCentreId) {
            return new GetHearings(null);
        }

        List<Hearing> source;
        if (null == roomId) {
            source = hearingRepository.findHearings(date, courtCentreId);
        } else {
            source = hearingRepository.findByFilters(date, courtCentreId, roomId);
        }

        if (isDDJorRecorder) {
            source = filterHearingsBasedOnPermissions.filterHearings(source, accessibleCasesId);
        }

        if (CollectionUtils.isEmpty(source)) {
            return new GetHearings(null);
        }

        final ZonedDateTime from = getDateWithTime(date, startTime);
        final ZonedDateTime to = getDateWithTime(date, endTime);
        List<Hearing> filteredHearings = filterHearings(source, from, to);
        if (null == roomId) {
            filteredHearings = filterNonEndedHearings(filteredHearings);
        }

        return GetHearings.getHearings()
                .withHearingSummaries(filteredHearings.stream()
                        .map(ha -> hearingJPAMapper.fromJPA(ha))
                        .filter(ha -> isNotEmpty(ha.getProsecutionCases()) || isNotEmpty(ha.getCourtApplications()))
                        .map(h -> getHearingTransformer.summary(h).build())
                        .sorted(comparing(hearingSummaries -> hearingSummaries.getHearingDays().stream()
                                .filter(hd -> date.equals(hd.getSittingDay().toLocalDate()))
                                .findFirst().get()
                                .getSittingDay()))
                        .collect(toList()))
                .build();
    }

    public GetHearings getHearingsForToday(final LocalDate date, final UUID userId) {

        if (null == date || null == userId) {
            return new GetHearings(null);
        }
        final List<Hearing> filteredHearings = hearingRepository.findByUserFilters(date, userId);
        filterForShadowListedOffencesAndCases(filteredHearings);
        if (CollectionUtils.isEmpty(filteredHearings)) {
            return new GetHearings(null);
        }


        filteredHearings.sort(Comparator.nullsFirst(Comparator.comparing(o -> sortListingSequence(date, o))));

        return GetHearings.getHearings()
                .withHearingSummaries(filteredHearings.stream()
                        .map(ha -> hearingJPAMapper.fromJPA(ha))
                        .map(h -> getHearingTransformer.summaryForHearingsForToday(h).build())
                        .collect(toList()))
                .build();
    }

    private void filterForShadowListedOffencesAndCases(final List<Hearing> filteredHearings) {
        filteredHearings.stream().flatMap(x -> x.getProsecutionCases().stream()).flatMap(c -> c.getDefendants().stream()).forEach(d -> d.getOffences().removeIf(Offence::isShadowListed));
        filteredHearings.stream().flatMap(x -> x.getProsecutionCases().stream()).forEach(c -> c.getDefendants().removeIf(d -> d.getOffences().isEmpty()));
        filteredHearings.forEach(x -> x.getProsecutionCases().removeIf(c -> c.getDefendants().isEmpty()));
        filteredHearings.removeIf(x -> x.getProsecutionCases().isEmpty());
    }

    @Transactional
    public DefendantInfoQueryResult getHearingsByCourtRoomList(final LocalDate date, final UUID courtCentreId, final List<UUID> roomId) {

        if (null == date || null == courtCentreId || null == roomId || roomId.isEmpty()) {
            return new DefendantInfoQueryResult(null);
        }
        final List<Hearing> hearings = hearingRepository.findByFilters(date, courtCentreId, roomId);
        if (CollectionUtils.isEmpty(hearings)) {
            return new DefendantInfoQueryResult(null);
        }

        final DefendantInfoQueryResult queryResult = new DefendantInfoQueryResult();
        final HashMap<UUID, CourtRoom> courtRooms = new HashMap<>();


        for (final Hearing hearing : hearings) {
            for (final ProsecutionCase pc : hearing.getProsecutionCases()) {
                final UUID roomUUID = hearing.getCourtCentre().getRoomId();

                courtRooms.computeIfAbsent(roomUUID, room ->
                        courtRooms.put(roomUUID, CourtRoom.courtRoom()
                                .withCourtRoomName(hearing.getCourtCentre().getRoomName())
                                .withDefendantDetails(new ArrayList<>()).build()));
                addDefendantDetailsToQueryResult(courtRooms, pc, roomUUID); //court room needs to be created

            }
        }
        queryResult.getCourtRooms().addAll(courtRooms.values());
        return queryResult;
    }

    private void addDefendantDetailsToQueryResult(final HashMap<UUID, CourtRoom> courtRooms, final ProsecutionCase pc, final UUID roomUUID) {
        pc.getDefendants().forEach(defendant -> {
            final DefendantDetail.Builder builder = DefendantDetail.defendantDetail()
                    .withDefendantId(defendant.getId().getId());
            if (nonNull(defendant.getPersonDefendant())) {
                final Person personDetails = defendant.getPersonDefendant().getPersonDetails();
                if (nonNull(personDetails)) {
                    builder.withFirstName(personDetails.getFirstName())
                            .withLastName(personDetails.getLastName())
                            .withNationalInsuranceNumber(personDetails.getNationalInsuranceNumber());

                    if (nonNull(personDetails.getDateOfBirth())) {
                        builder.withDateOfBirth(personDetails.getDateOfBirth().toString());
                    }
                }
            }
            if (nonNull(defendant.getLegalEntityOrganisation())) {
                builder.withLegalEntityOrganizationName(defendant.getLegalEntityOrganisation().getName());
            }
            courtRooms.get(roomUUID).getDefendantDetails().add(builder.build());
        });
    }

    public Optional<Hearing> getHearingById(final UUID hearingId) {
        final Hearing hearing = hearingRepository.findBy(hearingId);
        return Optional.ofNullable(hearing);
    }

    public Optional<uk.gov.justice.core.courts.Hearing> getHearingDomainById(final UUID hearingId) {
        final Hearing hearing = hearingRepository.findBy(hearingId);
        if (hearing != null) {
            return of(hearingJPAMapper.fromJPA(hearing));
        }
        return empty();
    }

    public List<HearingEvent> getHearingEvents(final UUID courtCentreId, final UUID roomId, LocalDate date) {
        final List<HearingEvent> hearingEvents = hearingEventRepository.findHearingEvents(courtCentreId, roomId, date);
        if (isNull(hearingEvents)) {
            return Collections.emptyList();
        }
        return hearingEvents;
    }

    public List<HearingEvent> getHearingEvents(UUID hearingId, LocalDate date) {
        final List<HearingEvent> hearingEvents = hearingEventRepository.findByHearingIdOrderByEventTimeAsc(hearingId, date);
        if (nonNull(hearingEvents)) {
            return hearingEvents;
        }
        return Collections.emptyList();
    }

    public List<HearingEventDefinition> getHearingEventDefinitions() {
        final List<HearingEventDefinition> hearingEventDefinitions = hearingEventDefinitionRepository.findAllActiveOrderBySequenceTypeSequenceNumberAndActionLabel();
        if (nonNull(hearingEventDefinitions)) {
            return hearingEventDefinitions;
        }
        return Collections.emptyList();
    }

    public Optional<CourtCentre> getCourtCenterByHearingId(UUID hearingId) {
        return Optional.ofNullable(hearingRepository.findCourtCenterByHearingId(hearingId));
    }

    public Optional<HearingEventDefinition> getHearingEventDefinition(UUID definitionId) {
        final HearingEventDefinition hearingEventDefinition = hearingEventDefinitionRepository.findBy(definitionId);
        return Optional.ofNullable(hearingEventDefinition);
    }

    @Transactional
    public HearingDetailsResponse getHearingDetailsResponseById(final UUID hearingId, final CrackedIneffectiveVacatedTrialTypes crackedIneffectiveVacatedTrialTypes,
                                                                final List<UUID> accessibleCasesId,
                                                                final boolean isDDJ) {
        if (null == hearingId) {
            return new HearingDetailsResponse();
        }

        Hearing hearing = hearingRepository.findBy(hearingId);
        if (isDDJ) {
            hearing = filterHearingsBasedOnPermissions.filterHearings(Arrays.asList(hearing), accessibleCasesId).stream().findFirst().orElse(null);
        }

        if (hearing == null) {
            return new HearingDetailsResponse();
        }

        final HearingDetailsResponse hearingDetailsResponse = new HearingDetailsResponse(
                hearingJPAMapper.fromJPA(hearing),
                getHearingState(hearing),
                hearing.getAmendedByUserId()
        );

        if (hearing.getTrialTypeId() != null) {

            final Optional<CrackedIneffectiveVacatedTrialType> crackedIneffectiveTrialType = getCrackedIneffectiveVacatedTrialType(hearing.getTrialTypeId(), crackedIneffectiveVacatedTrialTypes);
            crackedIneffectiveTrialType.map(trialType -> new CrackedIneffectiveTrial(
                    trialType.getReasonCode(),
                    trialType.getDate(),
                    trialType.getReasonFullDescription() == null ? "" : trialType.getReasonFullDescription(),
                    trialType.getId(),
                    trialType.getTrialType()))
                    .ifPresent(trialType -> hearingDetailsResponse
                            .getHearing()
                            .setCrackedIneffectiveTrial(trialType));
            hearingDetailsResponse.getHearing().setIsVacatedTrial(FALSE);

        } else if (isVacatedTrialRequest(hearing)) {

            final Optional<CrackedIneffectiveVacatedTrialType> crackedIneffectiveTrialType = getCrackedIneffectiveVacatedTrialType(hearing.getVacatedTrialReasonId(), crackedIneffectiveVacatedTrialTypes);
            crackedIneffectiveTrialType.map(trialType -> new CrackedIneffectiveTrial(
                    trialType.getReasonCode(),
                    trialType.getDate(),
                    trialType.getReasonFullDescription() == null ? "" : trialType.getReasonFullDescription(),
                    trialType.getId(),
                    trialType.getTrialType()))
                    .ifPresent(trialType -> hearingDetailsResponse
                            .getHearing()
                            .setCrackedIneffectiveTrial(trialType));
            hearingDetailsResponse.getHearing().setIsVacatedTrial(TRUE);

        } else if (nonNull(hearing.getIsEffectiveTrial())) {
            hearingDetailsResponse.getHearing().setIsEffectiveTrial(TRUE);
            hearingDetailsResponse.getHearing().setIsVacatedTrial(FALSE);
        } else {
            hearingDetailsResponse.getHearing().setIsVacatedTrial(FALSE);
        }

        return hearingDetailsResponse;
    }

    private HearingState getHearingState(final Hearing hearing) {
        HearingState hearingState = hearing.getHearingState();
        if (hearing.getHearingState() == null) {
            if (hearing.getHasSharedResults()) {
                hearingState = HearingState.SHARED;
            } else {
                hearingState = HearingState.INITIALISED;
            }
        }
        return hearingState;
    }

    private Optional<CrackedIneffectiveVacatedTrialType> getCrackedIneffectiveVacatedTrialType(final UUID trialTypeId, final CrackedIneffectiveVacatedTrialTypes crackedIneffectiveVacatedTrialTypes) {

        return crackedIneffectiveVacatedTrialTypes
                .getCrackedIneffectiveVacatedTrialTypes()
                .stream()
                .filter(crackedIneffectiveTrial -> trialTypeId.equals(crackedIneffectiveTrial.getId()))
                .findFirst();
    }

    private boolean isVacatedTrialRequest(final Hearing hearing) {
        return hearing.getIsVacatedTrial() != null && hearing.getIsVacatedTrial();
    }

    @Transactional
    public CrackedIneffectiveTrial fetchCrackedIneffectiveTrial(final UUID trialTypeId, final CrackedIneffectiveVacatedTrialTypes crackedIneffectiveVacatedTrialTypes) {

        if (null == trialTypeId) {
            return null;
        }

        if (isNotEmpty(crackedIneffectiveVacatedTrialTypes.getCrackedIneffectiveVacatedTrialTypes())) {

            final Optional<CrackedIneffectiveVacatedTrialType> crackedIneffectiveTrialType = crackedIneffectiveVacatedTrialTypes
                    .getCrackedIneffectiveVacatedTrialTypes()
                    .stream()
                    .filter(crackedIneffectiveTrial -> crackedIneffectiveTrial.getId().equals(trialTypeId))
                    .findFirst();

            return crackedIneffectiveTrialType.map(trialType -> new CrackedIneffectiveTrial(
                    trialType.getReasonCode(),
                    trialType.getDate(),
                    trialType.getReasonFullDescription() == null ? "" : trialType.getReasonFullDescription(),
                    trialType.getId(),
                    trialType.getTrialType()))
                    .orElse(null);
        }

        return null;

    }

    @Transactional
    public NowListResponse getNows(final UUID hearingId) {
        final List<Now> nows = nowRepository.findByHearingId(hearingId);
        final NowListResponse.Builder builder = NowListResponse.builder();

        if (!CollectionUtils.isEmpty(nows)) {
            final List<NowResponse> nowResponses = nows
                    .stream()
                    .map(now -> new NowResponse(now.getId(), now.getHearingId()))
                    .collect(toList());

            builder.withNows(nowResponses);
        }
        return builder.build();
    }

    @Transactional
    public JsonObject getNowsRepository(final String q) {
        LOGGER.debug("Searching for allowed user groups with materialId='{}'", q);
        final JsonObjectBuilder json = Json.createObjectBuilder();
        final JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        final NowsMaterial nowsMaterial = nowsMaterialRepository.findBy(fromString(q));
        if (nowsMaterial != null) {
            nowsMaterial.getUserGroups().stream().sorted().collect(toList()).forEach(jsonArrayBuilder::add);
        } else {
            LOGGER.info("No user groups found with materialId='{}'", q);
        }
        json.add("allowedUserGroups", jsonArrayBuilder.build());
        return json.build();
    }

    @Transactional
    public JsonObject getSubscriptions(final String referenceDateParam, final String nowTypeParam) {

        LOGGER.debug("Get subscriptions for the given reference date and nowTypeId ='{} - {}'", referenceDateParam, nowTypeParam);

        try {

            final LocalDate referenceDate = LocalDate.parse(referenceDateParam, FORMATTER);

            final UUID nowTypeId = fromString(nowTypeParam);

            final List<Document> existingDocuments = documentRepository.findAllByOrderByStartDateAsc();

            final List<Document> documents = existingDocuments
                    .stream()
                    .filter(existingDocument -> (referenceDate.isAfter(existingDocument.getStartDate()) || referenceDate.isEqual(existingDocument.getStartDate())))
                    .filter(existingDocument -> (isNull(existingDocument.getEndDate()) || referenceDate.isBefore(existingDocument.getEndDate()) || (referenceDate.isEqual(existingDocument.getEndDate()))))
                    .collect(toList());

            final List<Subscription> subscriptionList = new ArrayList<>();

            documents.forEach(d -> d.getSubscriptions().forEach(s -> s.getNowTypeIds().forEach(nt -> {
                if (nowTypeId.equals(nt)) {
                    subscriptionList.add(s);
                }
            })));

            final Subscriptions subscriptions = new Subscriptions();
            subscriptions.setSubscriptions(subscriptionList.stream().map(populateHearing()).collect(toList()));

            return objectToJsonObjectConverter.convert(subscriptions);

        } catch (final DateTimeParseException | IllegalArgumentException e) {

            LOGGER.error(format("Exception occurred while retrieve get subscriptions = '%s - %s'", referenceDateParam, nowTypeParam), e);

            return Json.createObjectBuilder().build();
        }
    }

    public DraftResultResponse getDraftResult(final UUID hearingId, final String hearingDay) {
        final List<DraftResult> draftResult = draftResultRepository.findDraftResultByFilter(hearingId, hearingDay);
        if (draftResult.isEmpty()) {
            return new DraftResultResponse(objectToJsonObjectConverter.convert(getTargetsByDate(hearingId,hearingDay)), true);
        }

        final JsonNode payload = draftResult.get(0).getDraftResultPayload();
        return new DraftResultResponse(objectToJsonObjectConverter.convert(payload), true);
    }

    public GetShareResultsV2Response getShareResultsByDate(final UUID hearingId, final String hearingDay) {
        final List<Target> targets = hearingRepository.findTargetsByFilters(hearingId, hearingDay);

        return new GetShareResultsV2Response.Builder().withResultLines(transform(targets)).build();
    }

    private List<ResultLine> transform(final List<Target> targets) {
        final List<ResultLine> extractedResultLines = new ArrayList<>();

        targets.forEach(target -> {
            final List<uk.gov.justice.core.courts.ResultLine2> resultLines = resultLineJPAMapper.fromJPA2(target.getResultLinesJson());
            resultLines.forEach(resultLine -> extractResultLines(extractedResultLines, target, resultLine)
            );
        });

        return extractedResultLines.stream().collect(Collectors.toList());
    }

    private void extractResultLines(final List<ResultLine> extractedResultLines, final Target target, final uk.gov.justice.core.courts.ResultLine2 resultLine) {
        final ResultLine.Builder builder = new ResultLine.Builder()
                .withOrderedDate(resultLine.getOrderedDate())
                .withSharedDate(resultLine.getSharedDate())
                .withResultLineId(resultLine.getResultLineId())
                .withOffenceId(resultLine.getOffenceId())
                .withDefendantId(resultLine.getDefendantId())
                .withMasterDefendantId(resultLine.getMasterDefendantId())
                .withResultDefinitionId(resultLine.getResultDefinitionId())
                .withPrompts(resultLine.getPrompts())
                .withLevel(resultLine.getLevel())
                .withShortCode(resultLine.getShortCode())
                .withDelegatedPowers(resultLine.getDelegatedPowers())
                .withResultLabel(resultLine.getResultLabel())
                .withIsModified(resultLine.getIsModified())
                .withIsComplete(resultLine.getIsComplete())
                .withIsDeleted(resultLine.getIsDeleted())
                .withShadowListed(resultLine.getShadowListed())
                .withDraftResult(target.getDraftResult())
                .withApplicationId(resultLine.getApplicationId())
                .withCaseId(resultLine.getCaseId())
                .withAmendmentDate(resultLine.getAmendmentDate())
                .withAmendmentReasonId(resultLine.getAmendmentReasonId())
                .withAmendmentReason(resultLine.getAmendmentReason())
                .withChildResultLineIds(resultLine.getChildResultLineIds())
                .withParentResultLineIds(resultLine.getParentResultLineIds())
                .withFourEyesApproval(resultLine.getFourEyesApproval())
                .withApprovedDate(resultLine.getApprovedDate())
                .withIsDeleted(resultLine.getIsDeleted());

        extractedResultLines.add(builder.build());
    }

    public TargetListResponse getTargets(final UUID hearingId) {
        final List<Target> listOfTargets = hearingRepository.findTargetsByHearingId(hearingId);
        final List<ProsecutionCase> listOfProsecutionCases = hearingRepository.findProsecutionCasesByHearingId(hearingId);
        return TargetListResponse.builder()
                .withTargets(targetJPAMapper.fromJPA(Sets.newHashSet(listOfTargets), Sets.newHashSet(listOfProsecutionCases))).build();
    }

    public TargetListResponse getTargetsByDate(final UUID hearingId, final String hearingDay) {
        final List<Target> listOfTargets = hearingRepository.findTargetsByFilters(hearingId, hearingDay);
        final List<ProsecutionCase> listOfProsecutionCases = hearingRepository.findProsecutionCasesByHearingId(hearingId);
        return TargetListResponse.builder()
                .withTargets(targetJPAMapper.fromJPA(Sets.newHashSet(listOfTargets), Sets.newHashSet(listOfProsecutionCases))).build();
    }

    @Transactional
    public ApplicationTargetListResponse getApplicationTargets(final UUID hearingId) {
        final List<ApplicationDraftResult> applicationDraftResults = hearingRepository.findApplicationDraftResultsByHearingId(hearingId);
        return ApplicationTargetListResponse.applicationTargetListResponse().setHearingId(hearingId)
                .setTargets(applicationDraftResults.stream().map(dr ->
                        ApplicationTarget.applicationTarget().setDraftResult(dr.getDraftResult())
                                .setApplicationId(dr.getApplicationId())
                                .setTargetId(dr.getId())
                ).collect(toList()));
    }

    @Transactional
    public Timeline getTimeLineByCaseId(final UUID caseId, final CrackedIneffectiveVacatedTrialTypes crackedIneffectiveVacatedTrialTypes, final JsonObject allCourtRooms) {
        final List<TimelineHearingSummary> hearingSummaries = hearingRepository.findByCaseId(caseId)
                .stream()
                .map(e -> this.populateTimeLineHearingSummaries(e, crackedIneffectiveVacatedTrialTypes, allCourtRooms))
                .flatMap(Collection::stream)
                .collect(toList());

        return new Timeline(hearingSummaries);
    }

    @Transactional
    public Timeline getTimeLineByApplicationId(final UUID applicationId, final CrackedIneffectiveVacatedTrialTypes crackedIneffectiveVacatedTrialTypes, final JsonObject allCourtRooms) {

        final List<TimelineHearingSummary> hearingSummaries = hearingRepository.findAllHearingsByApplicationId(applicationId)
                .stream()
                .map(hearing -> populateTimeLineHearingSummariesWithApplicants(hearing, crackedIneffectiveVacatedTrialTypes, allCourtRooms, applicationId))
                .flatMap(Collection::stream)
                .collect(toList());

        return new Timeline(hearingSummaries);
    }

    private List<TimelineHearingSummary> populateTimeLineHearingSummaries(final Hearing hearing, final CrackedIneffectiveVacatedTrialTypes crackedIneffectiveVacatedTrialTypes, final JsonObject allCourtRooms) {
        final CrackedIneffectiveTrial crackedIneffectiveTrial = fetchCrackedIneffectiveTrial(hearing.getTrialTypeId(), crackedIneffectiveVacatedTrialTypes);
        final List<HearingYouthCourtDefendants> hearingYouthCourtDefendants = this.hearingYouthCourtDefendantsRepository.findAllByHearingId(hearing.getId());
        return hearing.getHearingDays()
                .stream()
                .map(hd -> timelineHearingSummaryHelper.createTimeLineHearingSummary(hd, hearing, crackedIneffectiveTrial, allCourtRooms, hearingYouthCourtDefendants))
                .collect(toList());
    }

    private List<TimelineHearingSummary> populateTimeLineHearingSummariesWithApplicants(final Hearing hearing,
                                                                                        final CrackedIneffectiveVacatedTrialTypes crackedIneffectiveVacatedTrialTypes,
                                                                                        final JsonObject allCourtRooms,
                                                                                        final UUID applicationId) {
        final CrackedIneffectiveTrial crackedIneffectiveTrial = fetchCrackedIneffectiveTrial(hearing.getTrialTypeId(), crackedIneffectiveVacatedTrialTypes);
        final List<HearingYouthCourtDefendants> hearingYouthCourtDefendants = this.hearingYouthCourtDefendantsRepository.findAllByHearingId(hearing.getId());
        return hearing.getHearingDays()
                .stream()
                .map(hd -> timelineHearingSummaryHelper.createTimeLineHearingSummary(hd, hearing, crackedIneffectiveTrial, allCourtRooms, hearingYouthCourtDefendants, applicationId))
                .collect(toList());
    }

    private List<HearingEvent> getHearingEvents(final List<HearingEventPojo> hearingEventPojos) {
        final List<HearingEvent> hearingEvents = new ArrayList<>();
        for (final HearingEventPojo hearingEventPojo : hearingEventPojos) {
            final HearingEvent hearingEvent = new HearingEvent();
            hearingEvent.setHearingId(hearingEventPojo.getHearingId());
            hearingEvent.setId(hearingEventPojo.getId());
            hearingEvent.setHearingEventDefinitionId(hearingEventPojo.getHearingEventDefinitionId());
            hearingEvent.setLastModifiedTime(hearingEventPojo.getLastModifiedTime());
            hearingEvent.setRecordedLabel(hearingEventPojo.getRecordedLabel());
            hearingEvent.setEventTime(hearingEventPojo.getEventTime());
            hearingEvent.setEventDate(hearingEventPojo.getEventDate());
            hearingEvent.setDeleted(hearingEventPojo.getDeleted());
            hearingEvent.setDefenceCounselId(hearingEventPojo.getDefenceCounselId());
            hearingEvents.add(hearingEvent);
        }
        return hearingEvents;
    }

    private Function<Subscription, uk.gov.moj.cpp.hearing.domain.notification.Subscription> populateHearing() {
        return s -> {
            final uk.gov.moj.cpp.hearing.domain.notification.Subscription subscription = new uk.gov.moj.cpp.hearing.domain.notification.Subscription();
            subscription.setChannel(s.getChannel());
            subscription.setDestination(s.getDestination());
            subscription.setChannelProperties(s.getChannelProperties());
            subscription.setCourtCentreIds(s.getCourtCentreIds());
            subscription.setUserGroups(s.getUserGroups());
            subscription.setNowTypeIds(s.getNowTypeIds());
            return subscription;
        };
    }

    private ZonedDateTime getDateWithTime(final LocalDate date, final String time) {
        final String[] times = time.split(":");
        final LocalTime localTime = LocalTime.of(Integer.parseInt(times[0]), Integer.parseInt(times[1]));
        return ZonedDateTime.of(date, localTime, ZONE_ID);
    }

    private List<Hearing> filterHearings(final List<Hearing> hearings, final ZonedDateTime from, final ZonedDateTime to) {
        return hearings.stream().filter(hearing -> hasHearingDayMatched(hearing, from, to)).collect(toList());
    }

    private boolean hasHearingDayMatched(final Hearing hearing, final ZonedDateTime from, final ZonedDateTime to) {
        return hearing.getHearingDays().stream().anyMatch(hearingDay -> isBetween(hearingDay.getDateTime(), from, to));
    }

    private List<Hearing> filterNonEndedHearings(final List<Hearing> hearings) {
        //if hearing is not ended yet then only display
        return hearings.stream().filter(hearing -> isHearingNotEndedYet(hearing.getId())).collect(toList());
    }

    private boolean isHearingNotEndedYet(final UUID hearingId) {
        final List<HearingEvent> hearingEvents = hearingEventRepository.findHearingEvents(hearingId, "Hearing ended");
        return hearingEvents.isEmpty();
    }

    private boolean isBetween(final ZonedDateTime sittingDay, final ZonedDateTime from, final ZonedDateTime to) {
        return (sittingDay.isAfter(from) || sittingDay.isEqual(from)) && (sittingDay.isEqual(to) || sittingDay.isBefore(to));
    }

    private Integer sortListingSequence(final LocalDate date, final Hearing o1) {
        return o1.getHearingDays().stream().filter(d -> d.getDate().isEqual(date)).map(HearingDay::getListingSequence).findFirst().orElse(0);
    }

}
