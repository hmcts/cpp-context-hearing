package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole;

import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class JudicialRoleRepository {

    @PersistenceContext(unitName = "hearing-persistence-unit")
    EntityManager entityManager;

    public JudicialRole findBy(final HearingSnapshotKey id) {
        return entityManager.find(JudicialRole.class, id);
    }

    public JudicialRole save(final JudicialRole entity) {
        return entityManager.merge(entity);
    }

    public void remove(final JudicialRole entity) {
        JudicialRole managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public List<JudicialRole> findAll() {
        return entityManager.createQuery("SELECT e FROM JudicialRole e", JudicialRole.class).getResultList();
    }

    public long count() {
        return entityManager.createQuery("SELECT COUNT(e) FROM JudicialRole e", Long.class).getSingleResult();
    }

    public List<JudicialRole> findByJudicialId(final UUID judicialId) {
        return entityManager.createQuery(
                "SELECT e FROM JudicialRole e WHERE e.judicialId = :judicialId", JudicialRole.class)
                .setParameter("judicialId", judicialId)
                .getResultList();
    }
}
