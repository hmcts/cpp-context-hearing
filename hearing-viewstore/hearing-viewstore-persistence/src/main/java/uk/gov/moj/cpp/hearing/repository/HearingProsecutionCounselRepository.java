package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingProsecutionCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@ApplicationScoped
public class HearingProsecutionCounselRepository {

    @PersistenceContext(unitName = "hearing-persistence-unit")
    EntityManager entityManager;

    public HearingProsecutionCounsel findBy(final HearingSnapshotKey id) {
        return entityManager.find(HearingProsecutionCounsel.class, id);
    }

    public HearingProsecutionCounsel save(final HearingProsecutionCounsel entity) {
        return entityManager.merge(entity);
    }

    public HearingProsecutionCounsel saveAndFlush(final HearingProsecutionCounsel entity) {
        final HearingProsecutionCounsel merged = entityManager.merge(entity);
        entityManager.flush();
        return merged;
    }

    public void remove(final HearingProsecutionCounsel entity) {
        final HearingProsecutionCounsel managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public void attachAndRemove(final HearingProsecutionCounsel entity) {
        final HearingProsecutionCounsel managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public List<HearingProsecutionCounsel> findAll() {
        return entityManager.createQuery("SELECT e FROM HearingProsecutionCounsel e", HearingProsecutionCounsel.class).getResultList();
    }
}
