package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingCaseNote;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@ApplicationScoped
public class HearingCaseNoteRepository {

    @PersistenceContext(unitName = "hearing-persistence-unit")
    EntityManager entityManager;

    public HearingCaseNote findBy(HearingSnapshotKey id) {
        return entityManager.find(HearingCaseNote.class, id);
    }

    public HearingCaseNote save(HearingCaseNote entity) {
        return entityManager.merge(entity);
    }

    public void remove(HearingCaseNote entity) {
        HearingCaseNote managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public void attachAndRemove(HearingCaseNote entity) {
        HearingCaseNote managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public List<HearingCaseNote> findAll() {
        return entityManager.createQuery("SELECT e FROM HearingCaseNote e", HearingCaseNote.class).getResultList();
    }
}
