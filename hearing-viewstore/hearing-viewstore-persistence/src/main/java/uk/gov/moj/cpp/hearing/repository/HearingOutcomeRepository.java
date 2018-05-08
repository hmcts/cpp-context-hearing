package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.ui.HearingOutcome;

import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@Repository
public abstract class HearingOutcomeRepository extends AbstractEntityRepository<HearingOutcome, UUID> {

    public abstract List<HearingOutcome> findByHearingId(final UUID hearingId);

}
