package uk.gov.moj.cpp.hearing.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.moj.cpp.hearing.persist.entity.sessiontime.SessionTime;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SessionTimeRepository {

    @PersistenceContext(unitName = "hearing-persistence-unit")
    EntityManager entityManager;

    public SessionTime findBy(final UUID id) {
        return entityManager.find(SessionTime.class, id);
    }

    public SessionTime save(final SessionTime entity) {
        return entityManager.merge(entity);
    }

    public void remove(final SessionTime entity) {
        final SessionTime managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public void attachAndRemove(final SessionTime entity) {
        final SessionTime managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public List<SessionTime> findAll() {
        return entityManager.createQuery("SELECT e FROM SessionTime e", SessionTime.class).getResultList();
    }
}
