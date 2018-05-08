package uk.gov.moj.cpp.hearing.persist;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Nows;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@Repository
public abstract class NowsRepository extends AbstractEntityRepository<Nows, UUID>{
    public abstract List<Nows> findByHearingId( final UUID hearingId);
}
