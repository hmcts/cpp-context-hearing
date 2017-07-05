package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.stream.Collectors.toList;

import uk.gov.moj.cpp.hearing.persist.HearingCaseRepository;
import uk.gov.moj.cpp.hearing.persist.HearingRepository;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;
import uk.gov.moj.cpp.hearing.query.view.convertor.HearingEntityToHearing;
import uk.gov.moj.cpp.hearing.query.view.response.HearingView;

import java.util.Optional;
import java.util.UUID;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class HearingService {

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private HearingCaseRepository hearingCaseRepository;

    @Transactional
    public HearingView getHearingById(UUID hearingId) {
        Optional<Hearing> hearing = hearingRepository.getByHearingId(hearingId);
        HearingView hearingView = new HearingView();
        if (hearing.isPresent()) {
            hearingView = HearingEntityToHearing.convert(hearing.get());
            hearingView.setCaseIds(hearingCaseRepository.findByHearingId(hearing.get().getHearingId()).stream()
                    .map(hearingCase -> hearingCase.getCaseId().toString())
                    .collect(toList()));
        }
        return hearingView;
    }
}
