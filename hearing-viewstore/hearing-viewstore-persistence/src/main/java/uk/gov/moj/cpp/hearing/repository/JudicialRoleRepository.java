package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole;

import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;

@Repository
public interface JudicialRoleRepository extends EntityRepository<JudicialRole, HearingSnapshotKey> {
    List<JudicialRole> findByJudicialId(final UUID judicialId);
}
