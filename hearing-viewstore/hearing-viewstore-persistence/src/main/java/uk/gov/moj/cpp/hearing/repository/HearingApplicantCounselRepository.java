package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingApplicantCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@ApplicationScoped
public class HearingApplicantCounselRepository {

    @PersistenceContext(unitName = "hearing-persistence-unit")
    EntityManager entityManager;

    public HearingApplicantCounsel findBy(final HearingSnapshotKey id) {
        return entityManager.find(HearingApplicantCounsel.class, id);
    }

    public HearingApplicantCounsel save(final HearingApplicantCounsel entity) {
        return entityManager.merge(entity);
    }

    public HearingApplicantCounsel saveAndFlush(final HearingApplicantCounsel entity) {
        final HearingApplicantCounsel merged = entityManager.merge(entity);
        entityManager.flush();
        return merged;
    }

    public void remove(final HearingApplicantCounsel entity) {
        final HearingApplicantCounsel managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public void attachAndRemove(final HearingApplicantCounsel entity) {
        final HearingApplicantCounsel managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public List<HearingApplicantCounsel> findAll() {
        return entityManager.createQuery("SELECT e FROM HearingApplicantCounsel e", HearingApplicantCounsel.class).getResultList();
    }
}
