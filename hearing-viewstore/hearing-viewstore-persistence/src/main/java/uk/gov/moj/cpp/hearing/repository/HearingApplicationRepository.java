package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingApplication;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingApplicationKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;

import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;


@ApplicationScoped
public class HearingApplicationRepository {

    @PersistenceContext(unitName = "hearing-persistence-unit")
    EntityManager entityManager;

    public HearingApplication findBy(final HearingApplicationKey id) {
        return entityManager.find(HearingApplication.class, id);
    }

    public HearingApplication save(final HearingApplication entity) {
        return entityManager.merge(entity);
    }

    public void remove(final HearingApplication entity) {
        HearingApplication managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public List<HearingApplication> findAll() {
        return entityManager.createQuery("SELECT ha FROM HearingApplication ha", HearingApplication.class).getResultList();
    }

    public long count() {
        return entityManager.createQuery("SELECT COUNT(ha) FROM HearingApplication ha", Long.class).getSingleResult();
    }

    public List<HearingApplication> findByApplicationId(final UUID applicationId) {
        return entityManager.createQuery(
                "SELECT ha FROM HearingApplication ha WHERE ha.id.applicationId = :applicationId",
                HearingApplication.class)
                .setParameter("applicationId", applicationId)
                .getResultList();
    }

}
