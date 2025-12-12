package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingProsecutionCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;


@Repository(forEntity = HearingProsecutionCounsel.class)
public abstract class HearingProsecutionCounselRepository extends AbstractEntityRepository<HearingProsecutionCounsel, HearingSnapshotKey> {

}