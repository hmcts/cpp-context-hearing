package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Witness;

import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class WitnessRepository {

    @PersistenceContext(unitName = "hearing-persistence-unit")
    EntityManager entityManager;

    public Witness findBy(final UUID id) {
        return entityManager.find(Witness.class, id);
    }

    public Witness save(final Witness entity) {
        return entityManager.merge(entity);
    }

    public void remove(final Witness entity) {
        final Witness managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public void attachAndRemove(final Witness entity) {
        final Witness managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public List<Witness> findAll() {
        return entityManager.createQuery("SELECT e FROM Witness e", Witness.class).getResultList();
    }
}
