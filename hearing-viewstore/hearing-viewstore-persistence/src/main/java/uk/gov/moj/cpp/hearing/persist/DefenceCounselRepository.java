package uk.gov.moj.cpp.hearing.persist;

import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounsel;

import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;

@Repository
public abstract class DefenceCounselRepository extends AbstractEntityRepository<DefenceCounsel, UUID> {
    /**
     * Find {@link DefenceCounsel}s by hearingId.
     *
     * @param hearingId of the {@link DefenceCounsel} retrieve.
     * @return HearingCases.
     */
    public abstract List<DefenceCounsel> findByHearingId(final UUID hearingId);


}
