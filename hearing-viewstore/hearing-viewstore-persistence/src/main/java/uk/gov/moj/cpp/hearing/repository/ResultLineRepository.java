package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ResultLine;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;

@Repository(forEntity = ResultLine.class)
public abstract class ResultLineRepository extends AbstractEntityRepository<ResultLine, HearingSnapshotKey> {
}
