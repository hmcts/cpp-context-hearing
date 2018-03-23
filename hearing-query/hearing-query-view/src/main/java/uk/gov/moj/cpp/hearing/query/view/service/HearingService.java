package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

import uk.gov.moj.cpp.hearing.persist.HearingCaseRepository;
import uk.gov.moj.cpp.hearing.persist.HearingJudgeRepository;
import uk.gov.moj.cpp.hearing.persist.HearingRepository;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.HearingJudge;
import uk.gov.moj.cpp.hearing.query.view.convertor.HearingDetailsResponseConverter;
import uk.gov.moj.cpp.hearing.query.view.convertor.HearingEntityToHearing;
import uk.gov.moj.cpp.hearing.query.view.convertor.HearingListResponseConverter;
import uk.gov.moj.cpp.hearing.query.view.response.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.HearingListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.HearingView;
import uk.gov.moj.cpp.hearing.query.view.response.Judge;
import uk.gov.moj.cpp.hearing.repository.AhearingRepository;

public class HearingService {
    
    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private HearingCaseRepository hearingCaseRepository;

    @Inject
    HearingJudgeRepository hearingJudgeRepository;

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

    //TODO - GPE-3032 - investigate proper validation.
    @Transactional
    public HearingListResponse getHearingByStartDateV2(final LocalDate startDate) {
        if (null == startDate) {
            return new HearingListResponse();
        }
        return new HearingListResponseConverter().convert(ahearingRepository.findByStartDateTime(startDate.atStartOfDay()));
    }
    
    @Transactional
    public HearingDetailsResponse getHearingByIdV2(final UUID hearingId) {
        if (null == hearingId) {
            return new HearingDetailsResponse();
        }
        return new HearingDetailsResponseConverter().convert(ahearingRepository.findById(hearingId));
    }
}
