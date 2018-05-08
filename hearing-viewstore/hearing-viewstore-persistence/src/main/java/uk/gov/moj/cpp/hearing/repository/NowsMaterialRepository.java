package uk.gov.moj.cpp.hearing.repository;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsMaterial;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@Repository
public abstract class NowsMaterialRepository extends AbstractEntityRepository<NowsMaterial, UUID>{
    public abstract List<NowsMaterial> findByHearingId( final UUID hearingId);
}
