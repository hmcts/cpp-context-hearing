package uk.gov.moj.cpp.hearing.query.view.service;

import uk.gov.moj.cpp.hearing.persist.ProsecutionCounselRepository;
import uk.gov.moj.cpp.hearing.persist.entity.ProsecutionCounsel;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class ProsecutionCounselService {

    @Inject
    private ProsecutionCounselRepository prosecutionCounselRepository;

    @Transactional
    public List<ProsecutionCounsel> getProsecutionCounselsByHearingId(UUID hearingId) {
        return prosecutionCounselRepository.findByHearingId(hearingId);
    }

}
