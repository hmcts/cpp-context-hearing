package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantAttendance;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class DefendantAttendanceRepository {

    @PersistenceContext(unitName = "hearing-persistence-unit")
    EntityManager entityManager;

    public DefendantAttendance findBy(final HearingSnapshotKey id) {
        return entityManager.find(DefendantAttendance.class, id);
    }

    public DefendantAttendance save(final DefendantAttendance entity) {
        return entityManager.merge(entity);
    }

    public void remove(final DefendantAttendance entity) {
        final DefendantAttendance managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public List<DefendantAttendance> findAll() {
        return entityManager.createQuery("SELECT e FROM DefendantAttendance e", DefendantAttendance.class).getResultList();
    }

    public Long count() {
        return entityManager.createQuery("SELECT COUNT(e) FROM DefendantAttendance e", Long.class).getSingleResult();
    }

    public DefendantAttendance findByHearingIdDefendantIdAndDate(final UUID hearingId, final UUID defendantId, final LocalDate day) {
        return entityManager.createQuery(
                "SELECT da FROM DefendantAttendance da WHERE da.hearing.id = :hearingId AND da.defendantId = :defendantId AND da.day = :day",
                DefendantAttendance.class)
                .setParameter("hearingId", hearingId)
                .setParameter("defendantId", defendantId)
                .setParameter("day", day)
                .getResultList()
                .stream()
                .findFirst()
                .orElse(null);
    }
}
