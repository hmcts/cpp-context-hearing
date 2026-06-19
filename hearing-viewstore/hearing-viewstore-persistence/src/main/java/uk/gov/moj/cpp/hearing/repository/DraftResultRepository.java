package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.DraftResult;

import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class DraftResultRepository {

    @PersistenceContext(unitName = "hearing-persistence-unit")
    EntityManager entityManager;

    public DraftResult findBy(final String id) {
        return entityManager.find(DraftResult.class, id);
    }

    public DraftResult save(final DraftResult entity) {
        return entityManager.merge(entity);
    }

    public void remove(final DraftResult entity) {
        DraftResult managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public void removeAndFlush(final DraftResult entity) {
        DraftResult managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
        entityManager.flush();
    }

    public List<DraftResult> findAll() {
        return entityManager.createQuery("SELECT e FROM DraftResult e", DraftResult.class).getResultList();
    }

    public long count() {
        return entityManager.createQuery("SELECT COUNT(e) FROM DraftResult e", Long.class).getSingleResult();
    }

    public List<DraftResult> findDraftResultByFilter(final UUID hearingId,
                                                     final String hearingDay) {
        return entityManager.createQuery(
                "SELECT result FROM DraftResult result " +
                "WHERE result.hearingId = :hearingId " +
                "AND (result.hearingDay = :hearingDay OR result.hearingDay IS NULL)",
                DraftResult.class)
                .setParameter("hearingId", hearingId)
                .setParameter("hearingDay", hearingDay)
                .getResultList();
    }

}
