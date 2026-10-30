package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class OffenceRepository {

    @PersistenceContext(unitName = "hearing-persistence-unit")
    EntityManager entityManager;

    public Offence findBy(final HearingSnapshotKey id) {
        return entityManager.find(Offence.class, id);
    }

    public Optional<Offence> findOptionalBy(final HearingSnapshotKey id) {
        return Optional.ofNullable(entityManager.find(Offence.class, id));
    }

    public Offence save(final Offence entity) {
        return entityManager.merge(entity);
    }

    public Offence saveAndFlush(final Offence entity) {
        final Offence merged = entityManager.merge(entity);
        entityManager.flush();
        return merged;
    }

    public void remove(final Offence entity) {
        final Offence managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public List<Offence> findAll() {
        return entityManager.createQuery("SELECT e FROM Offence e", Offence.class).getResultList();
    }

    public long count() {
        return entityManager.createQuery("SELECT COUNT(e) FROM Offence e", Long.class).getSingleResult();
    }

    public List<Offence> findByOffenceIdAndOriginatingHearingId(
            final UUID offenceId,
            final UUID originatingHearingId) {
        return entityManager.createQuery(
                "SELECT o FROM Offence o where o.id.id = :offenceId and o.plea.originatingHearingId = :originatingHearingId",
                Offence.class)
                .setParameter("offenceId", offenceId)
                .setParameter("originatingHearingId", originatingHearingId)
                .getResultList();
    }
}
