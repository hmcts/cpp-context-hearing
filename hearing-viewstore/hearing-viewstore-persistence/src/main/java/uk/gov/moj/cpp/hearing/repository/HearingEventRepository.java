package uk.gov.moj.cpp.hearing.repository;


import static java.util.Optional.empty;

import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SuppressWarnings({"CdiManagedBeanInconsistencyInspection", "squid:S1192"})
@ApplicationScoped
public class HearingEventRepository {

    @PersistenceContext(unitName = "hearing-persistence-unit")
    private EntityManager entityManager;

    public static final String REPLACE_WITH_HEARING_EVENT_DEF_IDS = "REPLACE_WITH_HEARING_EVENT_DEF_IDS";

    public static final String REPLACE_WITH_COURT_CENTRE_IDS = "REPLACE_WITH_COURT_CENTRE_IDS";

    private static final String GET_ACTIVE_HEARINGS_FOR_COURT_ROOM =
            "SELECT hearingEvent FROM HearingEvent hearingEvent, " +
                    "Hearing hearing " +
                    "LEFT JOIN hearing.hearingDays hd WITH hd.date = :date " +
                    "WHERE hearing.id = hearingEvent.hearingId and " +
                    "hearing.courtCentre.id = :courtCentreId and " +
                    "COALESCE(hd.courtRoomId, hearing.courtCentre.roomId) = :roomId and " +
                    "hearingEvent.eventDate = :date and " +
                    "hearingEvent.deleted is false and " +
                    "hearingEvent.alterable is false";

    private static final String GET_HEARING_LOG_FOR_HEARING_ID_AND_DATE =
            "SELECT hearingEvent FROM HearingEvent hearingEvent " +
                    "WHERE hearingEvent.hearingId = :hearingId and " +
                    "hearingEvent.deleted is false and " +
                    "hearingEvent.eventDate = :date " +
                    "order by hearingEvent.eventTime asc ";

    private static final String GET_CURRENT_ACTIVE_HEARINGS_FOR_COURT_CENTRE =
            "SELECT hearingEvent FROM HearingEvent hearingEvent, " +
                    "Hearing hearing " +
                    "WHERE hearing.id = hearingEvent.hearingId and " +
                    "hearing.courtCentre.id = :courtCentreId and " +
                    "hearingEvent.lastModifiedTime >= :lastModifiedTime and " +
                    "hearingEvent.deleted is false ";

    private static final String GET_CURRENT_ACTIVE_HEARINGS_FOR_COURT_CENTRE_LIST =
            "SELECT hearingEvent FROM HearingEvent hearingEvent, " +
                    "Hearing hearing " +
                    "WHERE hearing.id = hearingEvent.hearingId and " +
                    "hearing.courtCentre.id IN (:courtCentreList) and " +
                    "hearingEvent.eventTime >= :lastModifiedTime and " +
                    "hearingEvent.deleted is false and " +
                    "hearingEvent.hearingEventDefinitionId IN (:cppHearingEventIds)";

    private static final String GET_LATEST_HEARINGS_FOR_COURT_CENTRE_LIST =
            "WITH sub_qry " +
                    "AS ( " +
                    "   SELECT max(hearingeve2_.event_time) evtme " +
                    "   FROM ha_hearing_event hearingeve2_ " +
                    "       INNER JOIN ha_hearing hearing3_ ON hearing3_.id = hearingeve2_.hearing_id " +
                    "       LEFT JOIN ha_hearing_day day_ ON day_.hearing_id = hearing3_.id AND day_.date = :eventDate " +
                    "   WHERE hearingeve2_.event_date = :eventDate " +
                    "       AND hearingeve2_.hearing_event_definition_id IN (" + REPLACE_WITH_HEARING_EVENT_DEF_IDS + ") " +
                    "       AND hearingeve2_.deleted = false " +
                    "       AND hearing3_.court_centre_id IN (" + REPLACE_WITH_COURT_CENTRE_IDS + ") " +
                    "   GROUP BY coalesce(day_.court_room_id, hearing3_.room_id) " +
                    "   ) " +
                    "SELECT CAST(defence_counsel_id AS VARCHAR) AS defence_counsel_id, " +
                    "   deleted, " +
                    "   event_date, " +
                    "   event_time, " +
                    "   CAST(hearing_event_definition_id AS VARCHAR) AS hearing_event_definition_id, " +
                    "   CAST(hearing_id AS VARCHAR) AS hearing_id, " +
                    "   CAST(id AS VARCHAR) AS id, " +
                    "   last_modified_time, " +
                    "   recorded_label " +
                    "FROM ha_hearing_event, " +
                    "   sub_qry " +
                    "WHERE event_date = :eventDate " +
                    "   AND hearing_event_definition_id IN (" + REPLACE_WITH_HEARING_EVENT_DEF_IDS + ") " +
                    "   AND deleted = false " +
                    "   AND event_time = sub_qry.evtme " +
                    "ORDER BY event_time DESC";

    private static final String LATEST_HEARINGS_FOR_COURT_CENTRE_LIST =
            "WITH sub_qry " +
            "AS ( " +
                    "   SELECT max(hearingeve2_.event_time) evtme " +
                    "   FROM ha_hearing_event hearingeve2_ " +
                    "       INNER JOIN ha_hearing hearing3_ ON hearing3_.id = hearingeve2_.hearing_id " +
                    "       LEFT JOIN ha_hearing_day day_ ON day_.hearing_id = hearing3_.id AND day_.date = :eventDate " +
                    "   WHERE hearingeve2_.event_date = :eventDate " +
                    "       AND hearingeve2_.hearing_event_definition_id IN (" + REPLACE_WITH_HEARING_EVENT_DEF_IDS + ") " +
                    "       AND hearingeve2_.deleted = false " +
                    "       AND hearing3_.court_centre_id = :courtCentreId " +
                    "   GROUP BY coalesce(day_.court_room_id, hearing3_.room_id) " +
                    "   ) " +
            "SELECT CAST(defence_counsel_id AS VARCHAR) AS defence_counsel_id, " +
                    "   deleted, " +
                    "   event_date, " +
                    "   event_time, " +
                    "   CAST(hearing_event_definition_id AS VARCHAR) AS hearing_event_definition_id, " +
                    "   CAST(hearing_id AS VARCHAR) AS hearing_id, " +
                    "   CAST(id AS VARCHAR) AS id, " +
                    "   last_modified_time, " +
                    "   recorded_label " +
            "FROM ha_hearing_event, " +
                    "   sub_qry " +
            "WHERE event_date = :eventDate " +
                    "   AND hearing_event_definition_id IN (" + REPLACE_WITH_HEARING_EVENT_DEF_IDS + ") " +
                    "   AND deleted = false " +
                    "   AND event_time = sub_qry.evtme " +
            "ORDER BY event_time DESC";


    public HearingEvent save(final HearingEvent entity) {
        return entityManager.merge(entity);
    }

    public List<HearingEvent> findAll() {
        return entityManager.createQuery("SELECT e FROM HearingEvent e", HearingEvent.class).getResultList();
    }

    public Optional<HearingEvent> findOptionalById(final UUID hearingEventId) {
        final HearingEvent hearingEvent = findBy(hearingEventId);
        return hearingEvent == null ? empty() : Optional.of(hearingEvent);
    }

    public HearingEvent findBy(final UUID hearingEventId) {
        return entityManager.createQuery(
                        "SELECT he FROM HearingEvent he where he.deleted is false and he.id = :hearingEventId",
                        HearingEvent.class)
                .setParameter("hearingEventId", hearingEventId)
                .getResultList()
                .stream()
                .findFirst()
                .orElse(null);
    }

    public List<HearingEvent> findByHearingIdOrderByEventTimeAsc(final UUID hearingId, final LocalDate date) {
        return entityManager.createQuery(GET_HEARING_LOG_FOR_HEARING_ID_AND_DATE, HearingEvent.class)
                .setParameter("hearingId", hearingId)
                .setParameter("date", date)
                .getResultList();
    }

    public List<HearingEvent> findByHearingIdOrderByEventTimeAsc(final UUID hearingId) {
        return entityManager.createQuery(
                        "SELECT he FROM HearingEvent he where he.deleted is false and he.hearingId = :hearingId order by he.eventTime asc",
                        HearingEvent.class)
                .setParameter("hearingId", hearingId)
                .getResultList();
    }

    public List<HearingEvent> findHearingEvents(final UUID courtCentreId, final UUID roomId, final LocalDate date) {
        return entityManager.createQuery(GET_ACTIVE_HEARINGS_FOR_COURT_ROOM, HearingEvent.class)
                .setParameter("courtCentreId", courtCentreId)
                .setParameter("roomId", roomId)
                .setParameter("date", date)
                .getResultList();
    }

    public List<HearingEvent> findHearingEvents(final UUID hearingId, final String recordedLabel) {
        return entityManager.createQuery(
                        "SELECT he FROM HearingEvent he where he.deleted is false and he.hearingId = :hearingId and he.recordedLabel = :recordedLabel",
                        HearingEvent.class)
                .setParameter("hearingId", hearingId)
                .setParameter("recordedLabel", recordedLabel)
                .getResultList();
    }

    public List<HearingEvent> findBy(final UUID courtCentreId, final ZonedDateTime lastModifiedTime) {
        return entityManager.createQuery(GET_CURRENT_ACTIVE_HEARINGS_FOR_COURT_CENTRE, HearingEvent.class)
                .setParameter("courtCentreId", courtCentreId)
                .setParameter("lastModifiedTime", lastModifiedTime)
                .getResultList();
    }

    public List<HearingEvent> findBy(final List<UUID> courtCentreList, final ZonedDateTime lastModifiedTime, final Set<UUID> cppHearingEventIds) {
        return entityManager.createQuery(GET_CURRENT_ACTIVE_HEARINGS_FOR_COURT_CENTRE_LIST, HearingEvent.class)
                .setParameter("courtCentreList", courtCentreList)
                .setParameter("lastModifiedTime", lastModifiedTime)
                .setParameter("cppHearingEventIds", cppHearingEventIds)
                .getResultList();
    }

    public Long findEventLogCountByHearingIdAndEventDate(final UUID hearingId, final LocalDate hearingDate) {
        return entityManager.createQuery(
                        "SELECT COUNT(he.id) FROM HearingEvent he WHERE he.hearingId = :hearingId AND he.eventDate = :hearingDate AND he.deleted = false AND he.recordedLabel = 'Hearing started'",
                        Long.class)
                .setParameter("hearingId", hearingId)
                .setParameter("hearingDate", hearingDate)
                .getSingleResult();
    }

    public Long findEventLogCountByHearingId(final UUID hearingId) {
        return entityManager.createQuery(
                        "SELECT COUNT(he.id) FROM HearingEvent he WHERE he.hearingId = :hearingId AND he.deleted = false AND he.recordedLabel = 'Hearing started'",
                        Long.class)
                .setParameter("hearingId", hearingId)
                .getSingleResult();
    }

    public List<Object[]> findLatestHearingsForThatDayByCourt(final UUID courtCentreId, final LocalDate eventDate, final Set<UUID> cppHearingEventIds) {
        final String queryString = getSQLNativeQuery(cppHearingEventIds);
        jakarta.persistence.Query query = entityManager.createNativeQuery(queryString);
        query.setParameter("courtCentreId", courtCentreId);
        query.setParameter("eventDate", eventDate);
        return query.getResultList();
    }

    public List<Object[]> findLatestHearingsForThatDayByCourts(final List<UUID> courtCentreIds, final LocalDate eventDate, final Set<UUID> cppHearingEventIds) {
        final String queryStringWithHearingEventDefAndCourtCentreIds = getSQLNativeQueryWithHearingEventAndCourtCentreIds(new HashSet<>(courtCentreIds), cppHearingEventIds);
        jakarta.persistence.Query query = entityManager.createNativeQuery(queryStringWithHearingEventDefAndCourtCentreIds);
        query.setParameter("eventDate", eventDate);
        return query.getResultList();
    }

    private String getSQLNativeQueryWithHearingEventAndCourtCentreIds(final Set<UUID> courtCentreIds, final Set<UUID> cppHearingEventIds) {
        final String HearingEventDefIds = toSqlInClause(cppHearingEventIds);
        final String courtCentreStrIds = toSqlInClause(courtCentreIds);
        final String sqlNativeQuery = GET_LATEST_HEARINGS_FOR_COURT_CENTRE_LIST.replace(REPLACE_WITH_HEARING_EVENT_DEF_IDS, HearingEventDefIds).replace(REPLACE_WITH_COURT_CENTRE_IDS, courtCentreStrIds);
        return sqlNativeQuery;
    }

    private String getSQLNativeQuery(final Set<UUID> cppHearingEventIds) {
        final String HearingDefIds = toSqlInClause(cppHearingEventIds);
        final String sqlNativeQuery = LATEST_HEARINGS_FOR_COURT_CENTRE_LIST.replace(REPLACE_WITH_HEARING_EVENT_DEF_IDS, HearingDefIds);
        return sqlNativeQuery;
    }

    private static String toSqlInClause(Set<UUID> uuids) {
        return uuids.stream()
                .map(uuid -> "'" + uuid.toString() + "'")
                .collect(Collectors.joining(", "));
    }
}
