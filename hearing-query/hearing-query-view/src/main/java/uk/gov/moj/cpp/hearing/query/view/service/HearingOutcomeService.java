package uk.gov.moj.cpp.hearing.query.view.service;

import uk.gov.moj.cpp.hearing.repository.HearingOutcomeRepository;
import uk.gov.moj.cpp.hearing.persist.entity.ui.HearingOutcome;

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
