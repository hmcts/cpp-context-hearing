package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.ReusableInfo;

import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;


@ApplicationScoped
public class ReusableInfoRepository {

    @PersistenceContext(unitName = "hearing-persistence-unit")
    EntityManager entityManager;

    public ReusableInfo findBy(final UUID id) {
        return entityManager.find(ReusableInfo.class, id);
    }

    public ReusableInfo save(final ReusableInfo entity) {
        return entityManager.merge(entity);
    }

    public void remove(final ReusableInfo entity) {
        final ReusableInfo managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public List<ReusableInfo> findAll() {
        return entityManager.createQuery("SELECT e FROM ReusableInfo e", ReusableInfo.class).getResultList();
    }

    public long count() {
        return entityManager.createQuery("SELECT COUNT(e) FROM ReusableInfo e", Long.class).getSingleResult();
    }

    public List<ReusableInfo> findReusableInfoByMasterDefendantIds(final List<UUID> masterDefendantIdList) {
        return entityManager.createQuery(
                "SELECT reusableinfo FROM ReusableInfo reusableInfo " +
                "WHERE reusableinfo.masterDefendantId IN (:masterDefendantIdList) ",
                ReusableInfo.class)
                .setParameter("masterDefendantIdList", masterDefendantIdList)
                .getResultList();
    }
}
