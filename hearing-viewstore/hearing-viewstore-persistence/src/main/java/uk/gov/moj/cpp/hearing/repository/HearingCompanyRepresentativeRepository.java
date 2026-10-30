package uk.gov.moj.cpp.hearing.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingCompanyRepresentative;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;

import java.util.List;

@ApplicationScoped
public class HearingCompanyRepresentativeRepository {

    @PersistenceContext(unitName = "hearing-persistence-unit")
    EntityManager entityManager;

    public HearingCompanyRepresentative findBy(final HearingSnapshotKey id) {
        return entityManager.find(HearingCompanyRepresentative.class, id);
    }

    public HearingCompanyRepresentative save(final HearingCompanyRepresentative entity) {
        return entityManager.merge(entity);
    }

    public HearingCompanyRepresentative saveAndFlush(final HearingCompanyRepresentative entity) {
        final HearingCompanyRepresentative merged = entityManager.merge(entity);
        entityManager.flush();
        return merged;
    }

    public void remove(final HearingCompanyRepresentative entity) {
        final HearingCompanyRepresentative managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public void attachAndRemove(final HearingCompanyRepresentative entity) {
        final HearingCompanyRepresentative managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public List<HearingCompanyRepresentative> findAll() {
        return entityManager.createQuery("SELECT e FROM HearingCompanyRepresentative e", HearingCompanyRepresentative.class).getResultList();
    }
}
