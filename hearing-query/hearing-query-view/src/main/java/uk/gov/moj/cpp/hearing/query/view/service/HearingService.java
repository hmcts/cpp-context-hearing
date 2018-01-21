package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.stream.Collectors.toList;

import uk.gov.moj.cpp.hearing.persist.HearingCaseRepository;
import uk.gov.moj.cpp.hearing.persist.HearingJudgeRepository;
import uk.gov.moj.cpp.hearing.persist.HearingRepository;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.HearingJudge;
import uk.gov.moj.cpp.hearing.query.view.convertor.HearingEntityToHearing;
import uk.gov.moj.cpp.hearing.query.view.response.HearingView;
import uk.gov.moj.cpp.hearing.query.view.response.Judge;

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
}
