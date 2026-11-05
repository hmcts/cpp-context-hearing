package uk.gov.moj.cpp.hearing.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;

import java.util.List;

@ApplicationScoped
public class ProsecutionCaseRepository {

    @PersistenceContext(unitName = "hearing-persistence-unit")
    EntityManager entityManager;

    public ProsecutionCase findBy(final HearingSnapshotKey id) {
        return entityManager.find(ProsecutionCase.class, id);
    }

    public ProsecutionCase save(final ProsecutionCase entity) {
        return entityManager.merge(entity);
    }

    public void flush() {
        entityManager.flush();
    }

    public void remove(final ProsecutionCase entity) {
        final ProsecutionCase managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public void attachAndRemove(final ProsecutionCase entity) {
        final ProsecutionCase managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public List<ProsecutionCase> findAll() {
        return entityManager.createQuery("SELECT e FROM ProsecutionCase e", ProsecutionCase.class).getResultList();
    }
}
