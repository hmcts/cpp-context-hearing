package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.stream.Collectors.toList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.moj.cpp.hearing.persist.NowsRepository;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsResult;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsMaterial;
import uk.gov.moj.cpp.hearing.query.view.convertor.HearingDetailsResponseConverter;
import uk.gov.moj.cpp.hearing.query.view.convertor.HearingListResponseConverter;
import uk.gov.moj.cpp.hearing.query.view.response.HearingListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.nowresponse.Material;
import uk.gov.moj.cpp.hearing.query.view.response.nowresponse.NowResult;
import uk.gov.moj.cpp.hearing.query.view.response.nowresponse.Nows;
import uk.gov.moj.cpp.hearing.query.view.response.nowresponse.NowsResponse;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.repository.NowsMaterialRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import static java.util.stream.Collectors.toList;

public class HearingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HearingService.class);


    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private NowsRepository nowsRepository;

    @Inject
    private NowsMaterialRepository nowsMaterialRepository;

    @Transactional
    public HearingListResponse getHearingByDateV2(final LocalDate date) {
        if (null == date) {
            return new HearingListResponse();
        }
        final ZonedDateTime zonedDateTime = date.atStartOfDay(ZoneOffset.systemDefault());
        return new HearingListResponseConverter().convert(hearingRepository.findByDate(zonedDateTime));
    }

    @Transactional
    public HearingDetailsResponse getHearingByIdV2(final UUID hearingId) {
        if (null == hearingId) {
            return new HearingDetailsResponse();
        }
        return new HearingDetailsResponseConverter().convert(hearingRepository.findById(hearingId));
    }

    @Transactional
    public NowsResponse getNows(final UUID hearingId) {

        List<uk.gov.moj.cpp.hearing.persist.entity.ha.Nows> nows = nowsRepository.findByHearingId(hearingId);

        List<Nows> nowsList = nows.stream().map(now -> Nows.builder()
                .withDefendantId(now.getDefendantId().toString())
                .withId(now.getId().toString())
                .withNowsTypeId(now.getNowsTypeId().toString())
                .withMaterial(populateMaterial(now.getMaterial()))
                .withNowResult(populateNowResult(now.getNowResult()))
                .build()).collect(toList());
        return NowsResponse.builder().withNows(nowsList).build();
    }

    @Transactional
    public JsonObject getNowsRepository(final String q) {

        final JsonObjectBuilder json = Json.createObjectBuilder();
        try {
            NowsMaterial nowsMaterial = nowsMaterialRepository.findBy(UUID.fromString(q));
            JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
            nowsMaterial.getUserGroups().forEach(s ->jsonArrayBuilder.add(s));
            json.add("allowedUserGroups", jsonArrayBuilder.build());
        } catch (final NoResultException e) {
            LOGGER.info("No user groups found with materialId='{}'", q, e);
        }

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
                .withStatus(nowsMaterial.getStatus().getDescription())
                .withLanguage(nowsMaterial.getLanguage())
                .withUserGroups(nowsMaterial.getUserGroups()).build() ).collect(toList());
    }
}
