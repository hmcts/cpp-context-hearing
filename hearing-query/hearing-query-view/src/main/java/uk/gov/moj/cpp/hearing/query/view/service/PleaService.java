package uk.gov.moj.cpp.hearing.query.view.service;


import uk.gov.moj.cpp.hearing.persist.PleaHearingRepository;
import uk.gov.moj.cpp.hearing.persist.entity.PleaHearing;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class PleaService {
    @Inject
    private PleaHearingRepository pleaHearingRepository;

    @Transactional
    public List<PleaHearing> getPleaHearingByCaseId(UUID caseId) {
        return pleaHearingRepository.findByCaseId(caseId);
    }

}
