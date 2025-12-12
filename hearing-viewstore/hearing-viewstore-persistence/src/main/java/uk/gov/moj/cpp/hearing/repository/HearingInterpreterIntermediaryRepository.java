package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingInterpreterIntermediary;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;


@Repository(forEntity = HearingInterpreterIntermediary.class)
public abstract class HearingInterpreterIntermediaryRepository extends AbstractEntityRepository<HearingInterpreterIntermediary, HearingSnapshotKey> {

}