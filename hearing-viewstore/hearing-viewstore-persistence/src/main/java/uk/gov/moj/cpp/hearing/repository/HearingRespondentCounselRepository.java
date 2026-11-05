package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingRespondentCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@ApplicationScoped
public class HearingRespondentCounselRepository {

    @PersistenceContext(unitName = "hearing-persistence-unit")
    EntityManager entityManager;

    public HearingRespondentCounsel findBy(final HearingSnapshotKey id) {
        return entityManager.find(HearingRespondentCounsel.class, id);
    }

    public HearingRespondentCounsel save(final HearingRespondentCounsel entity) {
        return entityManager.merge(entity);
    }

    public HearingRespondentCounsel saveAndFlush(final HearingRespondentCounsel entity) {
        final HearingRespondentCounsel merged = entityManager.merge(entity);
        entityManager.flush();
        return merged;
    }

    public void remove(final HearingRespondentCounsel entity) {
        final HearingRespondentCounsel managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public void attachAndRemove(final HearingRespondentCounsel entity) {
        final HearingRespondentCounsel managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public List<HearingRespondentCounsel> findAll() {
        return entityManager.createQuery("SELECT e FROM HearingRespondentCounsel e", HearingRespondentCounsel.class).getResultList();
    }
}
