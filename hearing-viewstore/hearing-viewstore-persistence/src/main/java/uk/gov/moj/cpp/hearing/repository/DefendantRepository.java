package uk.gov.moj.cpp.hearing.repository;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;

import java.util.UUID;

@Repository(forEntity = Defendant.class)
public abstract class DefendantRepository extends AbstractEntityRepository<Defendant, HearingSnapshotKey> {

}
