package uk.gov.moj.cpp.hearing.repository;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository(forEntity = ProsecutionCase.class)
public abstract class ProsecutionCaseRepository extends AbstractEntityRepository<ProsecutionCase, HearingSnapshotKey> {

    /**
     * A case can exist in ha_case under several hearings; rows are returned most recent hearing
     * first (by latest sitting day) so callers keeping the first row per case id get the latest snapshot.
     */
    @Query(value = "select pc from ProsecutionCase pc join pc.hearing h join h.hearingDays d " +
            "where pc.id.id in (:caseIds) order by d.sittingDay desc")
    public abstract List<ProsecutionCase> findByCaseIds(@QueryParam("caseIds") final Collection<UUID> caseIds);
}
