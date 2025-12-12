package uk.gov.moj.cpp.hearing.repository;


import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;

@Repository
public interface TargetRepository extends EntityRepository<Target, HearingSnapshotKey> {
}
