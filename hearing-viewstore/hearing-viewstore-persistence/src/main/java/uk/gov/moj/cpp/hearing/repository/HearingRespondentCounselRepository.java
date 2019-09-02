package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingRespondentCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;


@Repository(forEntity = HearingRespondentCounsel.class)
public abstract class HearingRespondentCounselRepository extends AbstractEntityRepository<HearingRespondentCounsel, HearingSnapshotKey> {

}