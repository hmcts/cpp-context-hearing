package uk.gov.moj.cpp.hearing.query.view.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

import uk.gov.moj.cpp.hearing.domain.HearingStatusEnum;
import uk.gov.moj.cpp.hearing.persist.HearingRepository;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;
import uk.gov.moj.cpp.hearing.query.view.convertor.HearingEntityToHearing;
import uk.gov.moj.cpp.hearing.query.view.response.HearingView;

public class HearingService {

    @Inject
    private HearingRepository hearingRepository;

    @Transactional
    public List<HearingView> getHearingsForCase(UUID caseId, Optional<String> fromDate, Optional<String> hearingType) {
        List<Hearing> caseHearingList = this.hearingRepository.findByCaseIdAndStatusEqual(caseId, HearingStatusEnum.BOOKED);
        if (fromDate.isPresent()) {
            caseHearingList = caseHearingList.stream().filter(hearing -> hearing.getStartDate().isEqual(LocalDate.parse(fromDate.get()))
                    || hearing.getStartDate().isAfter(LocalDate.parse(fromDate.get()))).collect(Collectors.toList());
        }
        if (hearingType.isPresent()) {
            caseHearingList = caseHearingList.stream().filter(hearing -> hearing.getHearingType().toString().equalsIgnoreCase(hearingType.get()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<HearingView>(caseHearingList.stream().map(HearingEntityToHearing::convert).collect(Collectors.toList()));
    }

    @Transactional
    public HearingView getHearingById(UUID hearingId) {
        Hearing hearing = this.hearingRepository.findByHearingId(hearingId);
        return HearingEntityToHearing.convert(hearing);
    }

}
