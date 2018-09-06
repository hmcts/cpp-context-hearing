package uk.gov.moj.cpp.hearing.repository;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;
import uk.gov.moj.cpp.hearing.persist.entity.ui.HearingOutcome;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@Repository
//TODO delete this as part of 5479
public abstract class HearingOutcomeRepository extends AbstractEntityRepository<HearingOutcome, UUID> {

    public abstract List<HearingOutcome> findByHearingId(final UUID hearingId);

}
