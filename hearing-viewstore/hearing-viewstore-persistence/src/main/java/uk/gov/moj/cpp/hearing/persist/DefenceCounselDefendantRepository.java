package uk.gov.moj.cpp.hearing.persist;

import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounselDefendant;
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounselDefendantCompositeKey;

import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;

@Repository
public abstract class DefenceCounselDefendantRepository extends AbstractEntityRepository<DefenceCounselDefendant, DefenceCounselDefendantCompositeKey> {
    /**
     * Find {@link DefenceCounselDefendant}s by defenceCounselAttendeeId.
     *
     * @param defenceCounselAttendeeId of the {@link DefenceCounselDefendant} retrieve.
     *
     * @return List of DefenceCounselDefendant.
     */
    public abstract List<DefenceCounselDefendant> findByDefenceCounselAttendeeId(final UUID defenceCounselAttendeeId);

}
