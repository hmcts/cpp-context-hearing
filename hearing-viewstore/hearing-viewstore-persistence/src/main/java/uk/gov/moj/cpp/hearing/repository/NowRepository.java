package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Now;

import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class NowRepository {

    @PersistenceContext(unitName = "hearing-persistence-unit")
    EntityManager entityManager;

    public Now findBy(final UUID id) {
        return entityManager.find(Now.class, id);
    }

    public Now save(final Now entity) {
        return entityManager.merge(entity);
    }

    public void remove(final Now entity) {
        Now managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public List<Now> findAll() {
        return entityManager.createQuery("SELECT e FROM Now e", Now.class).getResultList();
    }

    public long count() {
        return entityManager.createQuery("SELECT COUNT(e) FROM Now e", Long.class).getSingleResult();
    }

    public List<Now> findByHearingId(final UUID hearingId) {
        return entityManager.createQuery("SELECT e FROM Now e WHERE e.hearingId = :hearingId", Now.class)
                .setParameter("hearingId", hearingId)
                .getResultList();
    }
}
