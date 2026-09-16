package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.PtphDetail;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class PtphDetailRepository {

    @PersistenceContext(unitName = "hearing-persistence-unit")
    EntityManager entityManager;

    public PtphDetail findBy(final UUID hearingId) {
        return entityManager.find(PtphDetail.class, hearingId);
    }

    public PtphDetail save(final PtphDetail entity) {
        return entityManager.merge(entity);
    }

    public void removeAndFlush(final PtphDetail entity) {
        final PtphDetail managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
        entityManager.flush();
    }
}
