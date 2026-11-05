package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingInterpreterIntermediary;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@ApplicationScoped
public class HearingInterpreterIntermediaryRepository {

    @PersistenceContext(unitName = "hearing-persistence-unit")
    EntityManager entityManager;

    public HearingInterpreterIntermediary findBy(final HearingSnapshotKey id) {
        return entityManager.find(HearingInterpreterIntermediary.class, id);
    }

    public HearingInterpreterIntermediary save(final HearingInterpreterIntermediary entity) {
        return entityManager.merge(entity);
    }

    public HearingInterpreterIntermediary saveAndFlush(final HearingInterpreterIntermediary entity) {
        final HearingInterpreterIntermediary merged = entityManager.merge(entity);
        entityManager.flush();
        return merged;
    }

    public void remove(final HearingInterpreterIntermediary entity) {
        final HearingInterpreterIntermediary managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public void attachAndRemove(final HearingInterpreterIntermediary entity) {
        final HearingInterpreterIntermediary managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public List<HearingInterpreterIntermediary> findAll() {
        return entityManager.createQuery("SELECT e FROM HearingInterpreterIntermediary e", HearingInterpreterIntermediary.class).getResultList();
    }
}
