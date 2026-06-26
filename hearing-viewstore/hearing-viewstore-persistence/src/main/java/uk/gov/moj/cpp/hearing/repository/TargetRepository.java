package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@ApplicationScoped
public class TargetRepository {

    @PersistenceContext(unitName = "hearing-persistence-unit")
    EntityManager entityManager;

    public Target findBy(final HearingSnapshotKey id) {
        return entityManager.find(Target.class, id);
    }

    public Target save(final Target entity) {
        return entityManager.merge(entity);
    }

    public void remove(final Target entity) {
        final Target managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public void attachAndRemove(final Target entity) {
        final Target managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public List<Target> findAll() {
        return entityManager.createQuery("SELECT e FROM Target e", Target.class).getResultList();
    }
}
