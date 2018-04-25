package uk.gov.moj.cpp.hearing.persist;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ex.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ex.NowsMaterial;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Witness;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@Repository
public abstract class NowsMaterialRepository extends AbstractEntityRepository<NowsMaterial, UUID>{
    public abstract List<NowsMaterial> findByHearingId( final UUID hearingId);
}
