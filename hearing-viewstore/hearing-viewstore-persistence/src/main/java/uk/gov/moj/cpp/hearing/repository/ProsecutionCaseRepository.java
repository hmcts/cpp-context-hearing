package uk.gov.moj.cpp.hearing.repository;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;

@Repository(forEntity = ProsecutionCase.class)
public abstract class ProsecutionCaseRepository extends AbstractEntityRepository<ProsecutionCase, HearingSnapshotKey> {

}
