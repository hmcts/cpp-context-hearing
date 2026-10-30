package uk.gov.moj.cpp.hearing.persist;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Nows;

import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@ApplicationScoped
public class NowsRepository {

    @PersistenceContext(unitName = "hearing-persistence-unit")
    EntityManager entityManager;

    public Nows findBy(final UUID id) {
        return entityManager.find(Nows.class, id);
    }

    public Nows save(final Nows entity) {
        return entityManager.merge(entity);
    }

    public void remove(final Nows entity) {
        final Nows managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public void attachAndRemove(final Nows entity) {
        final Nows managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public List<Nows> findAll() {
        return entityManager.createQuery("SELECT e FROM Nows e", Nows.class).getResultList();
    }

    public List<Nows> findByHearingId(final UUID hearingId) {
        return entityManager.createQuery("SELECT e FROM Nows e WHERE e.hearingId = :hearingId", Nows.class)
                .setParameter("hearingId", hearingId)
                .getResultList();
    }
}
