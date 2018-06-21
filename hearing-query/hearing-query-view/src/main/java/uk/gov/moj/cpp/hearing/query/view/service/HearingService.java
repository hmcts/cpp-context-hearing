package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.moj.cpp.hearing.persist.NowsRepository;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Advocate;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Attendee;
import uk.gov.moj.cpp.hearing.persist.entity.ha.AttendeeHearingDate;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DefenceAdvocate;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDate;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsMaterial;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsResult;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionAdvocate;
import uk.gov.moj.cpp.hearing.query.view.convertor.HearingDetailsResponseConverter;
import uk.gov.moj.cpp.hearing.query.view.convertor.HearingListResponseConverter;
import uk.gov.moj.cpp.hearing.query.view.response.HearingListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.nowresponse.Material;
import uk.gov.moj.cpp.hearing.query.view.response.nowresponse.NowResult;
import uk.gov.moj.cpp.hearing.query.view.response.nowresponse.Nows;
import uk.gov.moj.cpp.hearing.query.view.response.nowresponse.NowsResponse;
import uk.gov.moj.cpp.hearing.repository.AttendeeHearingDateRespository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.repository.NowsMaterialRepository;

public class HearingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HearingService.class);

    private final HearingRepository hearingRepository;
    private final NowsRepository nowsRepository;
    private final NowsMaterialRepository nowsMaterialRepository;
    private final AttendeeHearingDateRespository attendeeHearingDateRespository;

    @Inject
    public HearingService(final HearingRepository hearingRepository, final NowsRepository nowsRepository,
            final NowsMaterialRepository nowsMaterialRepository,
            final AttendeeHearingDateRespository attendeeHearingDateRespository) {
        this.hearingRepository = hearingRepository;
        this.nowsRepository = nowsRepository;
        this.nowsMaterialRepository = nowsMaterialRepository;
        this.attendeeHearingDateRespository = attendeeHearingDateRespository;
    }

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
            if (! hearingDaysMap.isEmpty()) {
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
        if(nowsMaterial!=null) {
            nowsMaterial.getUserGroups().forEach(s -> jsonArrayBuilder.add(s));
        }else {
            LOGGER.info("No user groups found with materialId='{}'", q);
        }
        json.add("allowedUserGroups", jsonArrayBuilder.build());
        return json.build();
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
                .withUserGroups(nowsMaterial.getUserGroups()).build() ).collect(toList());
    }
}
