package uk.gov.moj.cpp.hearing.repository;

import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.moj.cpp.hearing.persist.entity.application.ApplicationDraftResult;
import uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;


@ApplicationScoped
public class HearingRepository {

    @PersistenceContext(unitName = "hearing-persistence-unit")
    EntityManager entityManager;

    public Hearing findBy(final UUID id) {
        return entityManager.find(Hearing.class, id);
    }

    public Optional<Hearing> findOptionalBy(final UUID id) {
        return Optional.ofNullable(entityManager.find(Hearing.class, id));
    }

    public Hearing save(final Hearing entity) {
        return entityManager.merge(entity);
    }

    public Hearing saveAndFlush(final Hearing entity) {
        final Hearing merged = entityManager.merge(entity);
        entityManager.flush();
        return merged;
    }

    public void flush() {
        entityManager.flush();
    }

    public void remove(final Hearing entity) {
        final Hearing managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public void attachAndRemove(final Hearing entity) {
        final Hearing managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public List<Hearing> findAll() {
        return entityManager.createQuery("SELECT e FROM Hearing e", Hearing.class).getResultList();
    }

    public long count() {
        return entityManager.createQuery("SELECT COUNT(e) FROM Hearing e", Long.class).getSingleResult();
    }

    @SuppressWarnings("unchecked")
    public List<Hearing> findByFilters(final LocalDate date,
                                       final UUID courtCentreId,
                                       final UUID roomId) {
        return entityManager.createNativeQuery(
                "select  h.*" +
                "from ha_hearing_day d ,ha_hearing h   where h.id = d.hearing_id and d.date = :date and coalesce(d.is_cancelled,false) !=true " +
                "and coalesce(d.court_centre_id,h.court_centre_id) = :courtCentreId and " +
                "coalesce(d.court_room_id,h.room_id) = :roomId and " +
                "coalesce(h.is_box_hearing,false) != true and " +
                "coalesce(h.is_vacated_trial,false) != true",
                Hearing.class)
                .setParameter("date", date)
                .setParameter("courtCentreId", courtCentreId)
                .setParameter("roomId", roomId)
                .getResultList();
    }

    public List<Hearing> findHearingsByDateAndCourtCentreList(final LocalDate date,
                                                              final List<UUID> courtCentreList) {
        return entityManager.createQuery(
                "SELECT hearing FROM Hearing hearing INNER JOIN hearing.hearingDays hd " +
                "WHERE hearing.courtCentre.id IN (:courtCentreList) " +
                "AND " +
                "hd.date = :date ",
                Hearing.class)
                .setParameter("courtCentreList", courtCentreList)
                .setParameter("date", date)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Hearing> findHearings(final LocalDate date,
                                      final UUID courtCentreId) {
        return entityManager.createNativeQuery(
                "select  h.*" +
                "from ha_hearing_day d ,ha_hearing h   where h.id = d.hearing_id and d.date = :date and coalesce(d.is_cancelled,false) !=true " +
                "and coalesce(d.court_centre_id,h.court_centre_id) = :courtCentreId and " +
                "coalesce(h.is_box_hearing,false) != true " +
                "and coalesce(h.is_vacated_trial,false) != true",
                Hearing.class)
                .setParameter("date", date)
                .setParameter("courtCentreId", courtCentreId)
                .getResultList();
    }

    public List<Hearing> findByUserFilters(final LocalDate date,
                                           final UUID userId) {
        return entityManager.createQuery(
                "SELECT distinct hearing " +
                "FROM Hearing hearing INNER JOIN hearing.hearingDays hd INNER JOIN hearing.judicialRoles role " +
                "WHERE role.userId = :userId " +
                "AND hd.date = :date " +
                "AND (hearing.isBoxHearing IS null OR hearing.isBoxHearing != true) " +
                "AND (hearing.isVacatedTrial IS null OR hearing.isVacatedTrial != true) ",
                Hearing.class)
                .setParameter("userId", userId)
                .setParameter("date", date)
                .getResultList();
    }

    public List<Hearing> findByDefendantAndHearingType(final LocalDate date,
                                                       final UUID defendantId) {
        return entityManager.createQuery(
                "SELECT hearing " +
                "FROM Hearing hearing " +
                "inner join hearing.prosecutionCases prosecutionCase " +
                "inner join prosecutionCase.defendants defendant " +
                "inner join hearing.hearingDays hd " +
                "where defendant.id.id = :defendantId " +
                "and hd.date > :date ",
                Hearing.class)
                .setParameter("date", date)
                .setParameter("defendantId", defendantId)
                .getResultList();
    }

    public Hearing findByHearingIdAndJurisdictionType(final UUID hearingId, final JurisdictionType jurisdictionType) {
        return entityManager.createQuery(
                "SELECT hearing FROM Hearing hearing  " +
                "WHERE hearing.id = :hearingId " +
                "AND  hearing.jurisdictionType = :jurisdictionType",
                Hearing.class)
                .setParameter("hearingId", hearingId)
                .setParameter("jurisdictionType", jurisdictionType)
                .getSingleResult();
    }

    public List<Hearing> findByCaseId(final UUID caseId) {
        return entityManager.createQuery(
                "SELECT hearing FROM Hearing hearing INNER JOIN hearing.prosecutionCases prosecutionCase " +
                "WHERE prosecutionCase.id.id = :caseId",
                Hearing.class)
                .setParameter("caseId", caseId)
                .getResultList();
    }

    public List<Hearing> findByCaseIdAndJurisdictionType(final UUID caseId, final JurisdictionType jurisdictionType) {
        return entityManager.createQuery(
                "SELECT hearing FROM Hearing hearing INNER JOIN hearing.prosecutionCases prosecutionCase " +
                "WHERE prosecutionCase.id.id = :caseId " +
                "AND  hearing.jurisdictionType = :jurisdictionType",
                Hearing.class)
                .setParameter("caseId", caseId)
                .setParameter("jurisdictionType", jurisdictionType)
                .getResultList();
    }

    public List<Hearing> findAllHearingsByApplicationId(final UUID applicationId) {
        return entityManager.createQuery(
                "SELECT hearing FROM Hearing hearing INNER JOIN hearing.hearingApplications hearingApplication " +
                "WHERE hearingApplication.id.applicationId = :applicationId",
                Hearing.class)
                .setParameter("applicationId", applicationId)
                .getResultList();
    }

    public List<Hearing> findAllHearingsByApplicationIdAndJurisdictionType(final UUID applicationId, final JurisdictionType jurisdictionType) {
        return entityManager.createQuery(
                "SELECT hearing FROM Hearing hearing INNER JOIN hearing.hearingApplications hearingApplication " +
                "WHERE hearingApplication.id.applicationId = :applicationId " +
                "AND  hearing.jurisdictionType = :jurisdictionType",
                Hearing.class)
                .setParameter("applicationId", applicationId)
                .setParameter("jurisdictionType", jurisdictionType)
                .getResultList();
    }

    public List<Hearing> findByFilters(final LocalDate date,
                                       final UUID courtCentreId,
                                       final List<UUID> roomIds) {
        return entityManager.createQuery(
                "SELECT hearing " +
                "FROM Hearing hearing INNER JOIN hearing.hearingDays hd " +
                "WHERE hearing.courtCentre.id = :courtCentreId " +
                "AND hearing.courtCentre.roomId in :roomIds " +
                "AND hd.date = :date " +
                "AND (hearing.isBoxHearing IS null OR hearing.isBoxHearing != true) ",
                Hearing.class)
                .setParameter("date", date)
                .setParameter("courtCentreId", courtCentreId)
                .setParameter("roomIds", roomIds)
                .getResultList();
    }

    public List<Hearing> findByHearingDate(final LocalDate date) {
        return entityManager.createQuery(
                "SELECT hearing " +
                "FROM Hearing hearing INNER JOIN hearing.hearingDays hd " +
                "WHERE hd.date = :date " +
                "AND (hearing.isBoxHearing IS null OR hearing.isBoxHearing != true) ",
                Hearing.class)
                .setParameter("date", date)
                .getResultList();
    }

    public CourtCentre findCourtCenterByHearingId(final UUID hearingId) {
        return entityManager.createQuery(
                "SELECT hearing.courtCentre FROM Hearing hearing " +
                "WHERE hearing.id = :hearingId",
                CourtCentre.class)
                .setParameter("hearingId", hearingId)
                .getResultList().stream().findFirst().orElse(null);
    }

    public HearingDay findHearingDayByHearingIdAndDate(final UUID hearingId,
                                                       final LocalDate date) {
        return entityManager.createQuery(
                "SELECT hd FROM Hearing hearing INNER JOIN hearing.hearingDays hd " +
                "WHERE hearing.id = :hearingId AND hd.date = :date",
                HearingDay.class)
                .setParameter("hearingId", hearingId)
                .setParameter("date", date)
                .getResultList().stream().findFirst().orElse(null);
    }

    public List<Target> findTargetsByHearingId(final UUID hearingId) {
        return entityManager.createQuery(
                "SELECT target FROM Target target " +
                "WHERE target.hearing.id = :hearingId",
                Target.class)
                .setParameter("hearingId", hearingId)
                .getResultList();
    }

    /**
     * <code>hearingDay</code> is introduced with DD-3426.
     * For the results saved before DD-3426 feature, <code>hearingDay</code> field will be empty. To
     * enable backward compatibility, null values also included in the filter
     *
     * @param hearingId  The id of the hearing
     * @param hearingDay The hearing day that the results are entered for
     * @return A list of targets against the given hearing id and hearing day. If no result is
     * found, returns empty list.
     */
    public List<Target> findTargetsByFilters(final UUID hearingId,
                                             final String hearingDay) {
        return entityManager.createQuery(
                "SELECT target FROM Target target " +
                "WHERE target.hearing.id = :hearingId " +
                "AND (target.hearingDay = :hearingDay OR target.hearingDay IS NULL)",
                Target.class)
                .setParameter("hearingId", hearingId)
                .setParameter("hearingDay", hearingDay)
                .getResultList();
    }

    public List<ApplicationDraftResult> findApplicationDraftResultsByHearingId(final UUID hearingId) {
        return entityManager.createQuery(
                "SELECT hearing.applicationDraftResults FROM Hearing hearing " +
                "WHERE hearing.id = :hearingId",
                ApplicationDraftResult.class)
                .setParameter("hearingId", hearingId)
                .getResultList();
    }

    public List<ProsecutionCase> findProsecutionCasesByHearingId(final UUID hearingId) {
        return entityManager.createQuery(
                "SELECT prosecutionCase FROM ProsecutionCase prosecutionCase " +
                "WHERE prosecutionCase.hearing.id = :hearingId",
                ProsecutionCase.class)
                .setParameter("hearingId", hearingId)
                .getResultList();
    }

    public List<Hearing> findHearingsByCaseIdsLaterThan(final List<UUID> caseIds,
                                                        final LocalDate date) {
        return entityManager.createQuery(
                "SELECT distinct hearing " +
                "FROM Hearing hearing INNER JOIN hearing.hearingDays hd INNER JOIN hearing.prosecutionCases prosecutionCase " +
                "WHERE (hearing.isBoxHearing IS null OR hearing.isBoxHearing != true) " +
                "AND (hearing.isVacatedTrial IS null OR hearing.isVacatedTrial != true) " +
                "AND hd.date > :date " +
                "AND prosecutionCase.id.id IN (:caseIds)",
                Hearing.class)
                .setParameter("caseIds", caseIds)
                .setParameter("date", date)
                .getResultList();
    }
}
