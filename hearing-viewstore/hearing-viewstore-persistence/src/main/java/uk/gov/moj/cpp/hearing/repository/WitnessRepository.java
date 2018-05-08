package uk.gov.moj.cpp.hearing.repository;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Witness;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@Repository
public abstract class WitnessRepository extends AbstractEntityRepository<Witness, HearingSnapshotKey> {

}
