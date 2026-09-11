package uk.gov.moj.cpp.hearing.repository;

import static java.util.UUID.fromString;

import uk.gov.moj.cpp.hearing.domain.OffenceBailStatus;
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

    private static final String GET_OFFENCE_BAIL_STATUS = "SELECT CAST(ho.id AS VARCHAR(64)) AS offence_id, " +
            " CAST(COALESCE(ho.bail_status_id, hd.bail_status_id) AS VARCHAR(64)) AS bail_status_id, " +
            " COALESCE(ho.bail_status_code, hd.bail_status_code) AS bail_status_code, " +
            " COALESCE(ho.bail_status_description, hd.bail_status_desc) AS bail_status_desc " +
            "FROM ha_defendant hd " +
            "JOIN ha_offence ho ON hd.hearing_id = ho.hearing_id AND hd.id = ho.defendant_id " +
            "JOIN ha_hearing_day hhd ON hd.hearing_id = hhd.hearing_id " +
            "JOIN (" +
            "SELECT o.id as latest_offence_id, MAX(d.sitting_day) AS latest_sitting_day " +
            "FROM ha_offence o " +
            "JOIN ha_hearing_day d ON o.hearing_id = d.hearing_id " +
            "WHERE o.proceedings_concluded <> true " +
            "AND o.defendant_id = :defendantId " +
            "group by o.id) latest ON latest.latest_offence_id = ho.id AND latest.latest_sitting_day = hhd.sitting_day";


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

    public List<OffenceBailStatus> offenceBailStatuses(final UUID defendantId) {
        final List<Object[]> results =  entityManager.createNativeQuery(GET_OFFENCE_BAIL_STATUS)
                .setParameter("defendantId", defendantId)
                .getResultList();

        return  results.stream()
                .map(OffenceRepository::extractOffenceBailStatus).toList();
    }

    private static OffenceBailStatus extractOffenceBailStatus(final Object[] bailStatusResult) {
        return new OffenceBailStatus(
                toUUID(bailStatusResult[0]),
                toUUID(bailStatusResult[1]),
                bailStatusResult[2] == null ? null : bailStatusResult[2].toString().trim(),
                bailStatusResult[3] == null ? null : bailStatusResult[3].toString().trim());
    }

    private static UUID toUUID(final Object value) {
        if (value == null) {
            return null;
        }
        final String raw = value.toString().trim();
        // some JDBC drivers (e.g. H2) render a cast uuid column as an unhyphenated 32-char hex string
        if (raw.length() == 32 && raw.indexOf('-') < 0) {
            return fromString(raw.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
        }
        return fromString(raw);
    }
}
