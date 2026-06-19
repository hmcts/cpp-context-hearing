package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDefenceCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@ApplicationScoped
public class HearingDefenceCounselRepository {

    @PersistenceContext(unitName = "hearing-persistence-unit")
    EntityManager entityManager;

    public HearingDefenceCounsel findBy(HearingSnapshotKey id) {
        return entityManager.find(HearingDefenceCounsel.class, id);
    }

    public HearingDefenceCounsel save(HearingDefenceCounsel entity) {
        return entityManager.merge(entity);
    }

    public HearingDefenceCounsel saveAndFlush(HearingDefenceCounsel entity) {
        final HearingDefenceCounsel merged = entityManager.merge(entity);
        entityManager.flush();
        return merged;
    }

    public void remove(HearingDefenceCounsel entity) {
        HearingDefenceCounsel managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public void attachAndRemove(HearingDefenceCounsel entity) {
        HearingDefenceCounsel managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public List<HearingDefenceCounsel> findAll() {
        return entityManager.createQuery("SELECT e FROM HearingDefenceCounsel e", HearingDefenceCounsel.class).getResultList();
    }
}
