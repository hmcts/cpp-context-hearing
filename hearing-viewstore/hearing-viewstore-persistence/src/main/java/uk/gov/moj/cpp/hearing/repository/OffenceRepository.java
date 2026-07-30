package uk.gov.moj.cpp.hearing.repository;

import static java.util.UUID.fromString;

import uk.gov.moj.cpp.hearing.domain.OffenceBailStatus;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;

@Repository(forEntity = Offence.class)
public abstract class OffenceRepository extends AbstractEntityRepository<Offence, HearingSnapshotKey> {
    @Inject
    private EntityManager entityManager;

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
            "WHERE d.has_shared_results = true " +
            "AND o.proceedings_concluded <> true " +
            "AND o.defendant_id = :defendantId " +
            "group by o.id) latest ON latest.latest_offence_id = ho.id AND latest.latest_sitting_day = hhd.sitting_day";

    @Query(value = "from Offence o where o.id.id = :offenceId and o.plea.originatingHearingId = :originatingHearingId")
    public abstract List<Offence> findByOffenceIdAndOriginatingHearingId(
            @QueryParam("offenceId") final UUID offenceId,
            @QueryParam("originatingHearingId") final UUID originatingHearingId);

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