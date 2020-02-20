package uk.gov.moj.cpp.hearing.query.view.service;

import static java.lang.Boolean.TRUE;
import static java.time.ZoneOffset.UTC;
import static java.time.ZoneOffset.of;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import uk.gov.justice.core.courts.CrackedIneffectiveTrial;
import uk.gov.justice.hearing.courts.GetHearings;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.moj.cpp.hearing.domain.notification.Subscriptions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialType;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialTypes;
import uk.gov.moj.cpp.hearing.mapping.HearingDayJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingTypeJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.ProsecutionCaseIdentifierJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.TargetJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsMaterial;
import uk.gov.moj.cpp.hearing.persist.entity.not.Document;
import uk.gov.moj.cpp.hearing.persist.entity.not.Subscription;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.ApplicationTarget;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.ApplicationTargetListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.NowListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.NowResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.TargetListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;
import uk.gov.moj.cpp.hearing.repository.DocumentRepository;
import uk.gov.moj.cpp.hearing.repository.HearingEventPojo;
import uk.gov.moj.cpp.hearing.repository.HearingEventRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.transaction.Transactional;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HearingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HearingService.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
    @Inject
    private HearingRepository hearingRepository;
    @Inject
    private HearingEventRepository hearingEventRepository;
    @Inject
    private NowRepository nowRepository;
    @Inject
    private NowsMaterialRepository nowsMaterialRepository;
    @Inject
    private DocumentRepository documentRepository;
    @Inject
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;
    @Inject
    private HearingJPAMapper hearingJPAMapper;
    @Inject
    private HearingTypeJPAMapper hearingTypeJPAMapper;
    @Inject
    private HearingDayJPAMapper hearingDayJPAMapper;
    @Inject
    private TargetJPAMapper targetJPAMapper;
    @Inject
    private ProsecutionCaseIdentifierJPAMapper prosecutionCaseIdentifierJPAMapper;
    @Inject
    private GetHearingsTransformer getHearingTransformer;

    @Inject
    private ReferenceDataService referenceDataService;
    @Inject
    private HearingListXhibitResponseTransformer hearingListXhibitResponseTransformer;

    private final ZoneId zid = of(UTC.getId());

    public Optional<CurrentCourtStatus> getLatestHearings(final List<UUID> courtCentreList, final LocalDate localDate) {
        final List<HearingEventPojo> hearingEventPojos = hearingEventRepository.findLatestHearingsForThatDay(courtCentreList, localDate);
        final List<HearingEvent> hearingEvents = getHearingEvents(hearingEventPojos);
        final List<uk.gov.justice.core.courts.Hearing> hearingList = hearingEvents
                .stream()
                .map(hearingEvent -> hearingRepository.findBy(hearingEvent.getHearingId()))
                .map(ha -> hearingJPAMapper.fromJPA(ha))
                .collect(toList());


        if (!hearingList.isEmpty()) {
            final HearingEventsToHearingMapper hearingEventsToHearingMapper = new HearingEventsToHearingMapper(hearingEvents, hearingList);
            return Optional.of(hearingListXhibitResponseTransformer.transformFrom(hearingEventsToHearingMapper));
        }
        return empty();
    }

    private List<HearingEvent> getHearingEvents(final List<HearingEventPojo> hearingEventPojos) {
        final List<HearingEvent> hearingEvents  = new ArrayList();
        for(final HearingEventPojo hearingEventPojo: hearingEventPojos){
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

    public Optional<CurrentCourtStatus> getHearingsByDate(final List<UUID> courtCentreList, final LocalDate localDate) {
        final List<Hearing> hearingsForDate = hearingRepository.findHearingsByDateAndCourtCentreList(localDate, courtCentreList);
        final List<uk.gov.justice.core.courts.Hearing> hearingList = hearingsForDate
                .stream()
                .map(ha -> hearingJPAMapper.fromJPA(ha))
                .collect(toList());

        final List<HearingEvent> hearingEvents = hearingEventRepository.findBy(courtCentreList, localDate.atStartOfDay(ZoneOffset.UTC));

        if (!hearingList.isEmpty()) {
            final HearingEventsToHearingMapper hearingEventsToHearingMapper = new HearingEventsToHearingMapper(hearingEvents, hearingList);
            final CurrentCourtStatus currentCourtStatus = hearingListXhibitResponseTransformer.transformFrom(hearingEventsToHearingMapper);
            return Optional.of(currentCourtStatus);
        }
        return empty();
    }

    @Transactional
    public GetHearings getHearings(final LocalDate date, final String startTime, final String endTime, final UUID courtCentreId, final UUID roomId) {

        if (null == date || null == courtCentreId ) {
            return new GetHearings(null);
        }

        List<Hearing> source;
        if(null == roomId){
            source = hearingRepository.findHearings(date, courtCentreId);
        } else {
            source = hearingRepository.findByFilters(date, courtCentreId, roomId);
        }
        if (CollectionUtils.isEmpty(source)) {
            return new GetHearings(null);
        }

        final ZonedDateTime from = getDateWithTime(date, startTime);
        final ZonedDateTime to = getDateWithTime(date, endTime);
        List<Hearing> filteredHearings = filterHearings(source, from, to);
        if(null == roomId){
            filteredHearings = filterNonEndedHearings(filteredHearings);
        }

        //sorting listSequence for hearing day
        filteredHearings.sort(Comparator.nullsFirst(Comparator.comparing(o -> sortListingSequence(date, o))));

        return GetHearings.getHearings()
                .withHearingSummaries(filteredHearings.stream()
                        .map(ha -> hearingJPAMapper.fromJPA(ha))
                        .filter(ha -> isNotEmpty(ha.getProsecutionCases()) || isNotEmpty(ha.getCourtApplications()))
                        .map(h -> getHearingTransformer.summary(h).build())
                        .collect(toList()))
                .build();
    }


    @Transactional
    public HearingDetailsResponse getHearingById(final UUID hearingId) {
        if (null == hearingId) {
            return new HearingDetailsResponse();
        }

        final Hearing hearing = hearingRepository.findBy(hearingId);
        final HearingDetailsResponse hearingDetailsResponse = new HearingDetailsResponse(hearingJPAMapper.fromJPA(hearing));

        if (hearing.getTrialTypeId() != null) {

            final CrackedIneffectiveVacatedTrialTypes crackedIneffectiveVacatedTrialTypes = referenceDataService.getCrackedIneffectiveVacatedTrialTypes();

            if (isNotEmpty(crackedIneffectiveVacatedTrialTypes.getCrackedIneffectiveVacatedTrialTypes())) {

                final Optional<CrackedIneffectiveVacatedTrialType> crackedIneffectiveTrialType = crackedIneffectiveVacatedTrialTypes
                        .getCrackedIneffectiveVacatedTrialTypes()
                        .stream()
                        .filter(crackedIneffectiveTrial -> crackedIneffectiveTrial.getId().equals(hearing.getTrialTypeId()))
                        .findFirst();

                crackedIneffectiveTrialType.map(trialType -> new CrackedIneffectiveTrial(
                        trialType.getReasonCode(),
                        trialType.getReasonFullDescription(),
                        trialType.getId(),
                        trialType.getTrialType()))
                        .ifPresent(trialType -> hearingDetailsResponse
                                .getHearing()
                                .setCrackedIneffectiveTrial(trialType));

            }
        } else if (nonNull(hearing.getIsEffectiveTrial())) {
            hearingDetailsResponse.getHearing().setIsEffectiveTrial(TRUE);
        }

        return hearingDetailsResponse;
    }

    @Transactional
    public CrackedIneffectiveTrial getCrackedIneffectiveTrial(final UUID trailTypeId) {

        final CrackedIneffectiveVacatedTrialTypes crackedIneffectiveVacatedTrialTypes = referenceDataService.getCrackedIneffectiveVacatedTrialTypes();

        if (isNotEmpty(crackedIneffectiveVacatedTrialTypes.getCrackedIneffectiveVacatedTrialTypes())) {

            final Optional<CrackedIneffectiveVacatedTrialType> crackedIneffectiveTrialType = crackedIneffectiveVacatedTrialTypes
                    .getCrackedIneffectiveVacatedTrialTypes()
                    .stream()
                    .filter(crackedIneffectiveTrial -> crackedIneffectiveTrial.getId().equals(trailTypeId))
                    .findFirst();

            return crackedIneffectiveTrialType.map(trialType -> new CrackedIneffectiveTrial(
                    trialType.getReasonCode(),
                    trialType.getReasonFullDescription(),
                    trialType.getId(),
                    trialType.getTrialType()))
                    .orElse(null);
        }

        return null;

    }

    @Transactional
    public NowListResponse getNows(final UUID hearingId) {
        List<uk.gov.moj.cpp.hearing.persist.entity.ha.Now> nows = nowRepository.findByHearingId(hearingId);
        List<NowResponse> nowList = nows.stream()
                .map(now -> new NowResponse(now.getId(), now.getHearingId()))
                .collect(toList());

        return NowListResponse.builder().withNows(nowList).build();
    }

    @Transactional
    public JsonObject getNowsRepository(final String q) {
        LOGGER.debug("Searching for allowed user groups with materialId='{}'", q);
        final JsonObjectBuilder json = Json.createObjectBuilder();
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        NowsMaterial nowsMaterial = nowsMaterialRepository.findBy(UUID.fromString(q));
        if (nowsMaterial != null) {
            nowsMaterial.getUserGroups().stream().sorted().collect(Collectors.toList()).forEach(jsonArrayBuilder::add);
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

                final LocalDate referenceDate = LocalDate.parse(referenceDateParam, formatter);

                final UUID nowTypeId = UUID.fromString(nowTypeParam);

                final List<Document> existingDocuments = documentRepository.findAllByOrderByStartDateAsc();

                final List<Document> documents = existingDocuments
                        .stream()
                        .filter(existingDocument -> (referenceDate.isAfter(existingDocument.getStartDate()) || referenceDate.isEqual(existingDocument.getStartDate())))
                        .filter(existingDocument -> (isNull(existingDocument.getEndDate()) || referenceDate.isBefore(existingDocument.getEndDate()) || (referenceDate.isEqual(existingDocument.getEndDate()))))
                        .collect(Collectors.toList());

                final List<Subscription> subscriptionList = new ArrayList<>();

                documents.forEach(d -> d.getSubscriptions().forEach(s -> s.getNowTypeIds().forEach(nt -> {
                    if (nowTypeId.equals(nt)) {
                        subscriptionList.add(s);
                    }
                })));

                final Subscriptions subscriptions = new Subscriptions();
                subscriptions.setSubscriptions(subscriptionList.stream().map(populateHearing()).collect(Collectors.toList()));

                return objectToJsonObjectConverter.convert(subscriptions);

            } catch (DateTimeParseException | IllegalArgumentException e) {

            LOGGER.error(String.format("Exception occurred while retrieve get subscriptions = '%s - %s'", referenceDateParam, nowTypeParam), e);

            return Json.createObjectBuilder().build();
        }
    }

    @Transactional
    public TargetListResponse getTargets(UUID hearingId) {
        final Hearing hearing = hearingRepository.findBy(hearingId);
        return TargetListResponse.builder()
                .withTargets(targetJPAMapper.fromJPA(hearing.getTargets())).build();
    }

    @Transactional
    public ApplicationTargetListResponse getApplicationTargets(UUID hearingId) {
        final Hearing hearing = hearingRepository.findBy(hearingId);

        return ApplicationTargetListResponse.applicationTargetListResponse().setHearingId(hearing.getId())
                .setTargets(hearing.getApplicationDraftResults().stream().map(dr ->
                        ApplicationTarget.applicationTarget().setDraftResult(dr.getDraftResult())
                                .setApplicationId(dr.getApplicationId())
                                .setTargetId(dr.getId())
                ).collect(toList()));
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
        return ZonedDateTime.of(date, localTime, zid);
    }

    private List<Hearing> filterHearings(final List<Hearing> hearings, final ZonedDateTime from, final ZonedDateTime to) {
        return hearings.stream().filter(hearing -> hasHearingDayMatched(hearing, from, to)).collect(Collectors.toList());
    }

    private boolean hasHearingDayMatched(final Hearing hearing, final ZonedDateTime from, final ZonedDateTime to) {
        return hearing.getHearingDays().stream().anyMatch(hearingDay -> isBetween(hearingDay.getDateTime(), from, to));
    }

    private List<Hearing> filterNonEndedHearings(final List<Hearing> hearings) {
        //if hearing is not ended yet then only display
        return hearings.stream().filter( hearing -> isHearingNotEndedYet(hearing.getId())).collect(Collectors.toList());
    }

    private boolean isHearingNotEndedYet(final UUID hearingId) {
        final List<HearingEvent>  hearingEvents = hearingEventRepository.findHearingEvents(hearingId, "Hearing ended");
        return hearingEvents.isEmpty();
    }

    private boolean isBetween(final ZonedDateTime sittingDay, final ZonedDateTime from, final ZonedDateTime to) {
        return (sittingDay.isAfter(from) || sittingDay.isEqual(from)) && (sittingDay.isEqual(to) || sittingDay.isBefore(to));
    }

    private Integer sortListingSequence(LocalDate date, Hearing o1) {
        return o1.getHearingDays().stream().filter(d -> d.getDate().isEqual(date)).map(HearingDay::getListingSequence).findFirst().orElse(0);
    }
}
