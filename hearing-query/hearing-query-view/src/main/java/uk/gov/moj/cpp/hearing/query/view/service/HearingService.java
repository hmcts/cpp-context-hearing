package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.json.schemas.core.JurisdictionType;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.moj.cpp.hearing.domain.notification.Subscriptions;
import uk.gov.moj.cpp.hearing.mapping.HearingDayJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingTypeJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.ProsecutionCaseIdentifierJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.TargetJPAMapper;
import uk.gov.moj.cpp.hearing.persist.NowsRepository;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsMaterial;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsResult;
import uk.gov.moj.cpp.hearing.persist.entity.not.Document;
import uk.gov.moj.cpp.hearing.persist.entity.not.Subscription;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingListResponseDefendant;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingListResponseHearing;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.ProsecutionCase;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.TargetListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.nowresponse.Material;
import uk.gov.moj.cpp.hearing.query.view.response.nowresponse.NowResult;
import uk.gov.moj.cpp.hearing.query.view.response.nowresponse.Nows;
import uk.gov.moj.cpp.hearing.query.view.response.nowresponse.NowsResponse;
import uk.gov.moj.cpp.hearing.repository.DocumentRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.repository.NowsMaterialRepository;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HearingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HearingService.class);

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private NowsRepository nowsRepository;

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

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");

    @Transactional
    public HearingListResponse getHearingByDateV2(final LocalDate date, final String startTime, final String endTime, final UUID courtCentreId, final UUID roomId) {

        if (null == date || null == courtCentreId || null == roomId) {
            return new HearingListResponse();
        }
        final List<Hearing> source = hearingRepository.findByFilters(date, courtCentreId, roomId);
        if (CollectionUtils.isEmpty(source)) {
            return new HearingListResponse();
        }

        final LocalDateTime from = getDateWithTime(date, startTime);
        final LocalDateTime to = getDateWithTime(date, endTime);
        final List<Hearing> filteredHearings = filterHearings(source, from, to);

        return HearingListResponse.builder()
                .withHearings(filteredHearings.stream().map(this::populateHearing).collect(toList()))
                .build();
    }

    @Transactional
    public HearingDetailsResponse getHearingById(final UUID hearingId) {
        if (null == hearingId) {
            return new HearingDetailsResponse();
        }
        return new HearingDetailsResponse(hearingJPAMapper.fromJPA(hearingRepository.findBy(hearingId)));
    }

    @Transactional
    public NowsResponse getNows(final UUID hearingId) {

        List<uk.gov.moj.cpp.hearing.persist.entity.ha.Nows> nows = nowsRepository.findByHearingId(hearingId);

        List<Nows> nowsList = nows.stream().map(now -> Nows.builder()
                .withDefendantId(now.getDefendantId().toString())
                .withId(now.getId().toString())
                .withNowsTypeId(now.getNowsTypeId().toString())
                .withMaterial(populateMaterial(now.getMaterial()))
                .build())
                .collect(toList());
        return NowsResponse.builder().withNows(nowsList).build();
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

    private List<NowResult> populateNowResult(Set<NowsResult> nowResult) {
        return nowResult.stream()
                .sorted(Comparator.comparing(NowsResult::getSequence))
                .map(result -> NowResult.builder()
                        .withSequence(result.getSequence())
                        .withSharedResultId(result.getSharedResultId().toString())
                        .build()
                )
                .collect(toList());
    }

    private List<Material> populateMaterial(Set<NowsMaterial> nowsMaterials) {
        return nowsMaterials.stream().map(nowsMaterial -> Material.builder()
                .withId(nowsMaterial.getId().toString())
                .withStatus(nowsMaterial.getStatus())
                .withLanguage(nowsMaterial.getLanguage())
                .withNowResult(populateNowResult(nowsMaterial.getNowResult()))
                .withUserGroups(new ArrayList<>(nowsMaterial.getUserGroups())).build()).collect(toList());
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

    private HearingListResponseHearing populateHearing(final Hearing source) {
        if (null == source || null == source.getId()) {
            return null;
        }

        final List<ProsecutionCase> prosecutionCases = source.getProsecutionCases().stream()
                .map(prosecutionCase -> ProsecutionCase.builder()
                        .withId(prosecutionCase.getId().getId())
                        .withProsecutionCaseIdentifier(prosecutionCaseIdentifierJPAMapper.fromJPA(prosecutionCase.getProsecutionCaseIdentifier()))
                        .withDefendants(prosecutionCase.getDefendants().stream()
                                .map(defendant -> HearingListResponseDefendant.builder()
                                        .withId(defendant.getId().getId())
                                        .withName(defendant.getPersonDefendant().getPersonDetails().getFirstName()
                                                + " " + defendant.getPersonDefendant().getPersonDetails().getMiddleName()
                                                + " " + defendant.getPersonDefendant().getPersonDetails().getLastName()).build()).collect(toList()))
                        .build()).collect(toList());

        return HearingListResponseHearing.builder()
                .withId(source.getId())
                .withType(hearingTypeJPAMapper.fromJPA(source.getHearingType()))
                .withJurisdictionType(JurisdictionType.valueOf(source.getJurisdictionType().name()))
                .withReportingRestrictionReason(source.getReportingRestrictionReason())
                .withHearingLanguage(source.getHearingLanguage().name())
                .withHearingDays(hearingDayJPAMapper.fromJPA(source.getHearingDays()))
                .withProsecutionCases(prosecutionCases)
                .withHasSharedResults(source.getHasSharedResults())
                .build();
    }

    private LocalDateTime getDateWithTime(final LocalDate date, final String time) {
        final String[] times = time.split(":");
        final LocalTime localTime = LocalTime.of(Integer.parseInt(times[0]), Integer.parseInt(times[1]));
        return LocalDateTime.of(date, localTime);
    }

    private List<Hearing> filterHearings(final List<Hearing> hearings, final LocalDateTime from, final LocalDateTime to) {
        return hearings.stream().filter(hearing -> hasHearingDayMatched(hearing, from, to)).collect(Collectors.toList());
    }

    private boolean hasHearingDayMatched(final Hearing hearing, final LocalDateTime from, final LocalDateTime to) {
        return hearing.getHearingDays().stream().anyMatch(hearingDay -> isBetween(hearingDay.getDateTime(), from, to));
    }

    private boolean isBetween(final LocalDateTime sittingDay, final LocalDateTime from, final LocalDateTime to) {
        return sittingDay.isAfter(from) && sittingDay.isBefore(to);
    }
}
