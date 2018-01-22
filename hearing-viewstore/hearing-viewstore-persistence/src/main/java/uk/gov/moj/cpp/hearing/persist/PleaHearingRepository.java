package uk.gov.moj.cpp.hearing.persist;

import uk.gov.moj.cpp.hearing.persist.entity.PleaHearing;

import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;

@Repository
public interface PleaHearingRepository extends EntityRepository<PleaHearing, UUID> {
    List<PleaHearing> findByCaseId(final UUID caseId);
}
