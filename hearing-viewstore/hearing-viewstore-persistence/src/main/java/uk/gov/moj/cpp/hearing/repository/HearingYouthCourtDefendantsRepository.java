package uk.gov.moj.cpp.hearing.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingYouthCourDefendantsKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingYouthCourtDefendants;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class HearingYouthCourtDefendantsRepository {

    @PersistenceContext(unitName = "hearing-persistence-unit")
    EntityManager entityManager;

    public HearingYouthCourtDefendants findBy(final HearingYouthCourDefendantsKey id) {
        return entityManager.find(HearingYouthCourtDefendants.class, id);
    }

    public HearingYouthCourtDefendants save(final HearingYouthCourtDefendants entity) {
        return entityManager.merge(entity);
    }

    public void remove(final HearingYouthCourtDefendants entity) {
        HearingYouthCourtDefendants managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public List<HearingYouthCourtDefendants> findAll() {
        return entityManager.createQuery("SELECT e FROM HearingYouthCourtDefendants e", HearingYouthCourtDefendants.class).getResultList();
    }

    public long count() {
        return entityManager.createQuery("SELECT COUNT(e) FROM HearingYouthCourtDefendants e", Long.class).getSingleResult();
    }

    public List<HearingYouthCourtDefendants> findAllByHearingId(final UUID hearingId) {
        return entityManager.createQuery("SELECT h FROM HearingYouthCourtDefendants h where h.id.hearingId = :hearingId ", HearingYouthCourtDefendants.class)
                .setParameter("hearingId", hearingId)
                .getResultList();
    }

}
