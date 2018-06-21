package uk.gov.moj.cpp.hearing.repository;

import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Modifying;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsMaterial;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@Repository
public abstract class NowsMaterialRepository extends AbstractEntityRepository<NowsMaterial, UUID>{

    public abstract List<NowsMaterial> findByHearingId( final UUID hearingId);

    @Modifying
    @Query("update NowsMaterial nm set nm.status = :status where nm.id = :materialId")
    public abstract int updateStatus(@QueryParam("materialId") final UUID materialId, @QueryParam("status") final String status);
}
