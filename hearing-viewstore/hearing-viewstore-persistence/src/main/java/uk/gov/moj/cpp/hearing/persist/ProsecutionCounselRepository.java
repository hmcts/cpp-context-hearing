package uk.gov.moj.cpp.hearing.persist;

import uk.gov.moj.cpp.hearing.persist.entity.ProsecutionCounsel;

import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@Repository
public abstract class ProsecutionCounselRepository extends AbstractEntityRepository<ProsecutionCounsel, UUID> {
    /**
     * Find {@link ProsecutionCounsel}s by hearingId.
     *
     * @param hearingId of the {@link ProsecutionCounsel} retrieve.
     * @return HearingCases.
     */
    public abstract List<ProsecutionCounsel> findByHearingId(UUID hearingId);
}
