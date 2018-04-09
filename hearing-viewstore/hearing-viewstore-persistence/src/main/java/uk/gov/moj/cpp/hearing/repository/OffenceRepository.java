package uk.gov.moj.cpp.hearing.repository;

import static org.apache.deltaspike.data.api.SingleResultType.OPTIONAL;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;

import uk.gov.moj.cpp.hearing.persist.entity.ex.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Offence;

@Repository(forEntity = Offence.class)
public abstract class OffenceRepository extends AbstractEntityRepository<Offence, HearingSnapshotKey> {

    @Query(value = "from Offence o where o.id = :id", singleResult = OPTIONAL)
    public abstract Offence findById(@QueryParam("id") final HearingSnapshotKey id);

}