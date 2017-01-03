package uk.gov.moj.cpp.hearing.query.view.service;

import uk.gov.moj.cpp.hearing.persist.DefenceCounselDefendantRepository;
import uk.gov.moj.cpp.hearing.persist.DefenceCounselRepository;
import uk.gov.moj.cpp.hearing.persist.HearingOutcomeRepository;
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounselDefendant;
import uk.gov.moj.cpp.hearing.persist.entity.HearingOutcome;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class HearingOutcomeService {

    @Inject
    private HearingOutcomeRepository hearingOutcomeRepository;

    @Transactional
    public List<HearingOutcome> getHearingOutcomeByHearingId(final UUID hearingId) {
        return hearingOutcomeRepository.findByHearingId(hearingId);
    }
}
