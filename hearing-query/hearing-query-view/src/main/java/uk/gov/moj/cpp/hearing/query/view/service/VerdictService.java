package uk.gov.moj.cpp.hearing.query.view.service;


import uk.gov.moj.cpp.hearing.persist.VerdictHearingRepository;
import uk.gov.moj.cpp.hearing.persist.entity.VerdictHearing;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class VerdictService {
    @Inject
    private VerdictHearingRepository verdictHearingRepository;

    @Transactional
    public List<VerdictHearing> getVerdictHearingByCaseId(final UUID caseId) {
        return this.verdictHearingRepository.findByCaseId(caseId);
    }
}
