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

    @Query(value = "select pc from ProsecutionCase pc where pc.id.id in (:caseIds)")
    public abstract List<ProsecutionCase> findByCaseIds(@QueryParam("caseIds") final Collection<UUID> caseIds);
}
