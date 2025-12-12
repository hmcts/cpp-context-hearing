package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingCaseNote;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Repository;


@Repository(forEntity = HearingCaseNote.class)
public abstract class HearingCaseNoteRepository extends AbstractEntityRepository<HearingCaseNote, HearingSnapshotKey> {

}