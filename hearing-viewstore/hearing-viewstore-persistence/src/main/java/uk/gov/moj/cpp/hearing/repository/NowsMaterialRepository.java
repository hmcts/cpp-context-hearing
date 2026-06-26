package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsMaterial;

import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@ApplicationScoped
public class NowsMaterialRepository {

    @PersistenceContext(unitName = "hearing-persistence-unit")
    EntityManager entityManager;

    public NowsMaterial findBy(final UUID id) {
        return entityManager.find(NowsMaterial.class, id);
    }

    public NowsMaterial save(final NowsMaterial entity) {
        return entityManager.merge(entity);
    }

    public void remove(final NowsMaterial entity) {
        NowsMaterial managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public List<NowsMaterial> findAll() {
        return entityManager.createQuery("SELECT e FROM NowsMaterial e", NowsMaterial.class).getResultList();
    }

    public long count() {
        return entityManager.createQuery("SELECT COUNT(e) FROM NowsMaterial e", Long.class).getSingleResult();
    }

    public List<NowsMaterial> findByHearingId(final UUID hearingId) {
        return entityManager.createQuery("SELECT e FROM NowsMaterial e WHERE e.hearingId = :hearingId", NowsMaterial.class)
                .setParameter("hearingId", hearingId)
                .getResultList();
    }

    public int updateStatus(final UUID materialId, final String status) {
        return entityManager.createQuery("update NowsMaterial nm set nm.status = :status where nm.id = :materialId")
                .setParameter("materialId", materialId)
                .setParameter("status", status)
                .executeUpdate();
    }
}
