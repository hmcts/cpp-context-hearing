package uk.gov.moj.cpp.hearing.repository;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingCompanyRepresentative;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;

@Repository(forEntity = HearingCompanyRepresentative.class)
public abstract class HearingCompanyRepresentativeRepository extends AbstractEntityRepository<HearingCompanyRepresentative, HearingSnapshotKey> {

}