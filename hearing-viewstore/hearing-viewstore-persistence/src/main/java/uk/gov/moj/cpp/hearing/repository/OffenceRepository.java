package uk.gov.moj.cpp.hearing.repository;

import static org.apache.deltaspike.data.api.SingleResultType.OPTIONAL;

import java.util.UUID;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;

import uk.gov.moj.cpp.hearing.persist.entity.ex.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Offence;

@Repository(forEntity = Offence.class)
public abstract class OffenceRepository extends AbstractEntityRepository<Offence, HearingSnapshotKey> {

    @Query(value = "from Offence o where o.id = :snapshotKey", singleResult = OPTIONAL)
    public abstract Offence findBySnapshotKey(@QueryParam("snapshotKey") final HearingSnapshotKey snapshotKey);
    
    @Query(value = "from Offence o where o.id.id = :offenceId and o.originHearingId = :originHearingId", singleResult = OPTIONAL)
    public abstract Offence findByOffenceIdOriginHearingId(@QueryParam("offenceId") final UUID offenceId, @QueryParam("originHearingId") final UUID originHearingId);
}