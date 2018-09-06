package uk.gov.moj.cpp.hearing.repository;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;

import java.util.List;
import java.util.UUID;

@Repository(forEntity = Offence.class)
public abstract class OffenceRepository extends AbstractEntityRepository<Offence, HearingSnapshotKey> {

    @Query(value = "from Offence o where o.id.id = :offenceId and o.plea.originatingHearingId = :originatingHearingId")
    public abstract List<Offence> findByOffenceIdAndOriginatingHearingId(
            @QueryParam("offenceId") final UUID offenceId,
            @QueryParam("originatingHearingId") final UUID originatingHearingId);
}