package uk.gov.moj.cpp.hearing.persist;

import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.HearingOutcome;

import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@Repository
public abstract class HearingOutcomeRepository extends AbstractEntityRepository<HearingOutcome, UUID> {

    /**
     * Find {@link DefenceCounsel}s by hearingId.
     *
     * @param hearingId of the {@link HearingOutcome} retrieve.
     * @return HearingOutcomes.
     */
    public abstract List<HearingOutcome> findByHearingId(final UUID hearingId);

}
