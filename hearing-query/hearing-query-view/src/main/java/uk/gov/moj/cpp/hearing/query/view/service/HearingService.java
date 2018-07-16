package uk.gov.moj.cpp.hearing.query.view.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.moj.cpp.hearing.domain.notification.Subscriptions;
import uk.gov.moj.cpp.hearing.persist.NowsRepository;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Advocate;
import uk.gov.moj.cpp.hearing.persist.entity.ha.AttendeeHearingDate;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDate;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsMaterial;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsResult;
import uk.gov.moj.cpp.hearing.persist.entity.not.Document;
import uk.gov.moj.cpp.hearing.persist.entity.not.Subscription;
import uk.gov.moj.cpp.hearing.query.view.convertor.HearingDetailsResponseConverter;
import uk.gov.moj.cpp.hearing.query.view.convertor.HearingListResponseConverter;
import uk.gov.moj.cpp.hearing.query.view.response.HearingListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.nowresponse.Material;
import uk.gov.moj.cpp.hearing.query.view.response.nowresponse.NowResult;
import uk.gov.moj.cpp.hearing.query.view.response.nowresponse.Nows;
import uk.gov.moj.cpp.hearing.query.view.response.nowresponse.NowsResponse;
import uk.gov.moj.cpp.hearing.repository.AttendeeHearingDateRespository;
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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class HearingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HearingService.class);

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private NowsRepository nowsRepository;

    @Inject
    private NowsMaterialRepository nowsMaterialRepository;

    @Inject
    private AttendeeHearingDateRespository attendeeHearingDateRespository;

    @Inject
    private DocumentRepository documentRepository;

    @Inject
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");

    @Transactional
    public HearingListResponse getHearingByDateV2(final LocalDate date) {
        if (null == date) {
            return new HearingListResponse();
        }
        return new HearingListResponseConverter().convert(hearingRepository.findByDate(date));
    }

    @Transactional
    public HearingDetailsResponse getHearingByIdV2(final UUID hearingId) {
        if (null == hearingId) {
            return new HearingDetailsResponse();
        }
        final Hearing hearing = hearingRepository.findById(hearingId);
        if (null != hearing) {
            final Map<UUID, LocalDate> hearingDaysMap = hearing.getHearingDays().stream().collect(toMap(h -> h.getId().getId(), HearingDate::getDate));
            if (!hearingDaysMap.isEmpty()) {
                hearing.getAttendees().stream()
                        .filter(attendee -> attendee instanceof Advocate)
                        .map(Advocate.class::cast)
                        .forEach(attendee -> {
                            final List<AttendeeHearingDate> attendeDays = findAttendeeDates(attendee.getId().getId(), hearing.getId());
                            attendeDays.forEach(attendeDay -> attendee.addHearingDate(hearingDaysMap.get(attendeDay.getHearingDateId())));
                        });
            }
        }
        return new HearingDetailsResponseConverter().convert(hearing);
    }

    @Transactional
    public List<AttendeeHearingDate> findAttendeeDates(final UUID attendeeId, final UUID hearingID) {
        return this.attendeeHearingDateRespository.findByAttendeeIdAndHearingId(attendeeId, hearingID);
    }

    @Transactional
    public NowsResponse getNows(final UUID hearingId) {

        List<uk.gov.moj.cpp.hearing.persist.entity.ha.Nows> nows = nowsRepository.findByHearingId(hearingId);

        List<Nows> nowsList = nows.stream().map(now -> Nows.builder()
                .withDefendantId(now.getDefendantId().toString())
                .withId(now.getId().toString())
                .withNowsTypeId(now.getNowsTypeId().toString())
                .withMaterial(populateMaterial(now.getMaterial()))

                .build()).collect(toList());
        return NowsResponse.builder().withNows(nowsList).build();
    }

    @Transactional
    public JsonObject getNowsRepository(final String q) {
        LOGGER.debug("Searching for allowed user groups with materialId='{}'", q);
        final JsonObjectBuilder json = Json.createObjectBuilder();
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        NowsMaterial nowsMaterial = nowsMaterialRepository.findBy(UUID.fromString(q));
        if (nowsMaterial != null) {
            nowsMaterial.getUserGroups().forEach(s -> jsonArrayBuilder.add(s));
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
            subscriptions.setSubscriptions(subscriptionList.stream().map(convert()).collect(Collectors.toList()));

            return objectToJsonObjectConverter.convert(subscriptions);

        } catch (DateTimeParseException | IllegalArgumentException e) {

            LOGGER.error(String.format("Exception occurred while retrieve get subscriptions = '%s - %s'", referenceDateParam, nowTypeParam), e);

            return Json.createObjectBuilder().build();
        }
    }

    private List<NowResult> populateNowResult(List<NowsResult> nowResult) {
        return nowResult.stream().map(result -> NowResult.builder()
                .withSequence(result.getSequence())
                .withSharedResultId(result.getSharedResultId().toString())
                .build()).collect(toList());
    }

    private List<Material> populateMaterial(List<NowsMaterial> nowsMaterials) {
        return nowsMaterials.stream().map(nowsMaterial -> Material.builder()
                .withId(nowsMaterial.getId().toString())
                .withStatus(nowsMaterial.getStatus())
                .withLanguage(nowsMaterial.getLanguage())
                .withNowResult(populateNowResult(nowsMaterial.getNowResult()))
                .withUserGroups(nowsMaterial.getUserGroups()).build()).collect(toList());
    }

    private Function<Subscription, uk.gov.moj.cpp.hearing.domain.notification.Subscription> convert() {
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
}
