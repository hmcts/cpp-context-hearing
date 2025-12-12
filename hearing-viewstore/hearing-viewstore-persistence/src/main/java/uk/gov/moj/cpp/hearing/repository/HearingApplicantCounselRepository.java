package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingApplicantCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;


@Repository(forEntity = HearingApplicantCounsel.class)
public abstract class HearingApplicantCounselRepository extends AbstractEntityRepository<HearingApplicantCounsel, HearingSnapshotKey> {

}