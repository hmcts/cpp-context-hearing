package uk.gov.moj.cpp.hearing.persist;

import uk.gov.moj.cpp.hearing.persist.entity.VerdictHearing;

import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;

@Repository
public interface VerdictHearingRepository extends EntityRepository<VerdictHearing, UUID> {
    List<VerdictHearing> findByCaseId(final UUID caseId);
}
