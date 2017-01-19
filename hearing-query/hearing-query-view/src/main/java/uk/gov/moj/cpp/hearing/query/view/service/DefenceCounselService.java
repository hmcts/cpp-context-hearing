package uk.gov.moj.cpp.hearing.query.view.service;

import uk.gov.moj.cpp.hearing.persist.DefenceCounselDefendantRepository;
import uk.gov.moj.cpp.hearing.persist.DefenceCounselRepository;
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounselDefendant;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class DefenceCounselService {

    @Inject
    private DefenceCounselRepository defenceCounselRepository;

    @Inject
    private DefenceCounselDefendantRepository defenceCounselDefendantRepository;

    @Transactional
    public List<DefenceCounsel> getDefenceCounselsByHearingId(final UUID hearingId) {
        return defenceCounselRepository.findByHearingId(hearingId);
    }

    @Transactional
    public List<DefenceCounselDefendant> getDefenceCounselDefendantsByDefenceCounselAttendeeId(
            final UUID defenceCounselAttendeeId) {
        return defenceCounselDefendantRepository.findByDefenceCounselAttendeeId(defenceCounselAttendeeId);
    }

}
