package uk.gov.moj.cpp.hearing.query.view.service;

import uk.gov.moj.cpp.hearing.persist.HearingCaseRepository;
import uk.gov.moj.cpp.hearing.persist.HearingRepository;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.HearingCase;
import uk.gov.moj.cpp.hearing.query.view.convertor.HearingEntityToHearing;
import uk.gov.moj.cpp.hearing.query.view.response.HearingView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class HearingService {

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private HearingCaseRepository hearingCaseRepository;

    @Transactional
    public List<HearingView> getHearingsByStartDate(LocalDate localDate) {
        List<Hearing> caseHearingList = hearingRepository.findByStartdate(localDate);

        List<UUID> hearingIds = caseHearingList.stream()
                .map(hearing -> hearing.geHearingId())
                .collect(Collectors.toList());
        List<HearingCase> hearingCases = hearingCaseRepository.findByHearingIds(hearingIds);

        List<HearingView> hearingViews =
                new ArrayList<>(caseHearingList.stream().map(HearingEntityToHearing::convert).collect(Collectors.toList()));

        Map<String, List<HearingCase>> byHearingId =
                hearingCases
                        .stream()
                        .collect(
                                Collectors.groupingBy(hearingCase -> hearingCase.getHearingId().toString()));
        hearingViews.stream().map(hearingView -> {
            byHearingId.computeIfPresent(hearingView.getHearingId(), (s, hcases) -> {
                hearingView.setCaseIds(hcases.stream()
                        .map(hearingCase -> hearingCase.getCaseId().toString())
                        .collect(Collectors.toList()));
                return hcases;
            });
            return hearingView.getCaseIds();
        }).count();
        return hearingViews;
    }

    @Transactional
    public HearingView getHearingById(UUID hearingId) {
        Optional<Hearing> hearing = hearingRepository.getByHearingId(hearingId);
        HearingView hearingView = new HearingView();
        if (hearing.isPresent()) {
            hearingView = HearingEntityToHearing.convert(hearing.get());
            hearingView.setCaseIds(hearingCaseRepository.findByHearingId(hearing.get().geHearingId()).stream()
                    .map(hearingCase -> hearingCase.getCaseId().toString())
                    .collect(Collectors.toList()));
        }
        return hearingView;
    }

}
