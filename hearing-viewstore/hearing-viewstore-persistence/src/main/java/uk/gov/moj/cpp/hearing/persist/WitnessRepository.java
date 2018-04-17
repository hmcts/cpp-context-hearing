package uk.gov.moj.cpp.hearing.persist;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;
import uk.gov.moj.cpp.hearing.persist.entity.ProsecutionCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.ex.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Witness;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@Repository
public abstract class WitnessRepository extends AbstractEntityRepository<Witness, HearingSnapshotKey> {

}
