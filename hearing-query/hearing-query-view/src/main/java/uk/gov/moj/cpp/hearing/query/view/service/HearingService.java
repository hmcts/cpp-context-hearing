package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.stream.Collectors.toList;

import uk.gov.moj.cpp.hearing.persist.HearingCaseRepository;
import uk.gov.moj.cpp.hearing.persist.HearingJudgeRepository;
import uk.gov.moj.cpp.hearing.persist.HearingRepository;
import uk.gov.moj.cpp.hearing.persist.NowsRepository;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.HearingJudge;
import uk.gov.moj.cpp.hearing.persist.entity.ex.NowsMaterial;
import uk.gov.moj.cpp.hearing.persist.entity.ex.NowsResult;
import uk.gov.moj.cpp.hearing.query.view.response.nowresponse.NowResult;
import uk.gov.moj.cpp.hearing.query.view.response.nowresponse.Nows;
import uk.gov.moj.cpp.hearing.query.view.convertor.HearingDetailsResponseConverter;
import uk.gov.moj.cpp.hearing.query.view.convertor.HearingEntityToHearing;
import uk.gov.moj.cpp.hearing.query.view.convertor.HearingListResponseConverter;
import uk.gov.moj.cpp.hearing.query.view.response.HearingListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.HearingView;
import uk.gov.moj.cpp.hearing.query.view.response.Judge;
import uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.nowresponse.Material;
import uk.gov.moj.cpp.hearing.query.view.response.nowresponse.NowsResponse;
import uk.gov.moj.cpp.hearing.repository.AhearingRepository;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class HearingService {

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private HearingCaseRepository hearingCaseRepository;

    @Inject
    HearingJudgeRepository hearingJudgeRepository;

    @Inject
    NowsRepository nowsRepository;

    //TODO - GPE-3032 - move 3032 functionality to new classes so that cleanup is easier.
    // new repositories for hearing and case
    //-----------------------------------------------------------------------
    @Inject
    private AhearingRepository ahearingRepository;

    @Transactional
    public HearingView getHearingById(final UUID hearingIdP) {
        Optional<Hearing> hearing = hearingRepository.getByHearingId(hearingIdP);
        HearingView hearingView = new HearingView();
        if (hearing.isPresent()) {
            hearingView = HearingEntityToHearing.convert(hearing.get());
            final UUID hearingId = hearing.get().getHearingId();
            hearingView.setCaseIds(hearingCaseRepository.findByHearingId(hearingId).stream()
                    .map(hearingCase -> hearingCase.getCaseId().toString())
                    .collect(toList()));
            HearingJudge hearingJudge = hearingJudgeRepository.findBy(hearingId);
            if (hearingJudge != null) {
                hearingView.setJudge(new Judge(hearingJudge.getId(), hearingJudge.getTitle(), hearingJudge.getFirstName(), hearingJudge.getLastName()));
            }
        }
        return hearingView;
    }

    @Transactional
    public HearingListResponse getHearingByDateV2(final LocalDate date) {
        if (null == date) {
            return new HearingListResponse();
        }
        final ZonedDateTime zonedDateTime = date.atStartOfDay(ZoneOffset.systemDefault());
        return new HearingListResponseConverter().convert(ahearingRepository.findByDate(zonedDateTime));
    }

    @Transactional
    public HearingDetailsResponse getHearingByIdV2(final UUID hearingId) {
        if (null == hearingId) {
            return new HearingDetailsResponse();
        }
        return new HearingDetailsResponseConverter().convert(ahearingRepository.findById(hearingId));
    }

    @Transactional
    public NowsResponse getNows(final UUID hearingId) {

        List<uk.gov.moj.cpp.hearing.persist.entity.ex.Nows> nows = nowsRepository.findByHearingId(hearingId);

        List<Nows> nowsList = nows.stream().map(now -> Nows.builder()
                .withDefendantId(now.getDefendantId().toString())
                .withId(now.getId().toString())
                .withNowsTypeId(now.getNowsTypeId().toString())
                .withMaterial(populateMaterial(now.getMaterial()))
                .withNowResult(populateNowResult(now.getNowResult()))
                .build()).collect(toList());
        return NowsResponse.builder().withNows(nowsList).build();
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
