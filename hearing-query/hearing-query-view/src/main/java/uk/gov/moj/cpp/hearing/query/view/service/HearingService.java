package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.stream.Collectors.toList;
import static uk.gov.moj.cpp.hearing.query.view.convertor.HearingDetailsResponseConverter.toHearingDetailsResponse;
import static uk.gov.moj.cpp.hearing.query.view.convertor.HearingListResponseConverter.toHearingListResponse;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.moj.cpp.hearing.persist.HearingCaseRepository;
import uk.gov.moj.cpp.hearing.persist.HearingJudgeRepository;
import uk.gov.moj.cpp.hearing.persist.HearingRepository;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.HearingJudge;
import uk.gov.moj.cpp.hearing.query.view.convertor.HearingEntityToHearing;
import uk.gov.moj.cpp.hearing.query.view.response.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.HearingListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.HearingView;
import uk.gov.moj.cpp.hearing.query.view.response.Judge;
import uk.gov.moj.cpp.hearing.repository.AhearingRepository;

public class HearingService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HearingService.class);
    
    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private HearingCaseRepository hearingCaseRepository;

    @Inject
    HearingJudgeRepository hearingJudgeRepository;
    
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
    public HearingListResponse getHearingByStartDateV2(final LocalDate startDate) {
        if (null == startDate) {
            LOGGER.warn("The given startDate parameter was null. Returning an empty HearingListResponse");
            return new HearingListResponse();
        }
        return toHearingListResponse(ahearingRepository.findByStartDateTime(startDate.atStartOfDay()));
    }
    
    @Transactional
    public HearingDetailsResponse getHearingByIdV2(final UUID hearingId) {
        if (null == hearingId) {
            LOGGER.warn("The given hearingId parameter was null. Returning an empty HearingDetailsResponse");
            return new HearingDetailsResponse();
        }
        return toHearingDetailsResponse(ahearingRepository.findById(hearingId));
    }
}
