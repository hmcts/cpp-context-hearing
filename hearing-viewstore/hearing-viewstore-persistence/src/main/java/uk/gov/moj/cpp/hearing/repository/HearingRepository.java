package uk.gov.moj.cpp.hearing.repository;

import static org.apache.deltaspike.data.api.SingleResultType.OPTIONAL;

import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.moj.cpp.hearing.persist.entity.application.ApplicationDraftResult;
import uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;


@Repository(forEntity = Hearing.class)
public abstract class HearingRepository extends AbstractEntityRepository<Hearing, UUID> {

    @Query(value = "select  h.*" +
            "from ha_hearing_day d ,ha_hearing h   where h.id = d.hearing_id and d.date = :date and coalesce(d.is_cancelled,false) !=true " +
            "and coalesce(d.court_centre_id,h.court_centre_id) = :courtCentreId and " +
            "coalesce(d.court_room_id,h.room_id) = :roomId and " +
            "coalesce(h.is_box_hearing,false) != true and "  +
            "coalesce(h.is_vacated_trial,false) != true", isNative = true)
    public abstract List<Hearing> findByFilters(@QueryParam("date") final LocalDate date,
                                                @QueryParam("courtCentreId") final UUID courtCentreId,
                                                @QueryParam("roomId") final UUID roomId);

    @Query(value = "SELECT hearing FROM Hearing hearing INNER JOIN hearing.hearingDays day " +
            "WHERE hearing.courtCentre.id IN (:courtCentreList) " +
            "AND " +
            "day.date = :date ")
    public abstract List<Hearing> findHearingsByDateAndCourtCentreList(@QueryParam("date") final LocalDate date,
                                                                       @QueryParam("courtCentreList") List<UUID> courtCentreList);

    @Query(value = "select  h.*" +
            "from ha_hearing_day d ,ha_hearing h   where h.id = d.hearing_id and d.date = :date and coalesce(d.is_cancelled,false) !=true " +
            "and coalesce(d.court_centre_id,h.court_centre_id) = :courtCentreId and " +
            "coalesce(h.is_box_hearing,false) != true " +
            "and coalesce(h.is_vacated_trial,false) != true", isNative = true)
    public abstract List<Hearing> findHearings(@QueryParam("date") final LocalDate date,
                                               @QueryParam("courtCentreId") final UUID courtCentreId);

    @Query(value = "SELECT distinct hearing " +
            "FROM Hearing hearing INNER JOIN hearing.hearingDays day INNER JOIN hearing.judicialRoles role " +
            "WHERE role.userId = :userId " +
            "AND day.date = :date " +
            "AND (hearing.isBoxHearing IS null OR hearing.isBoxHearing != true) " +
            "AND (hearing.isVacatedTrial IS null OR hearing.isVacatedTrial != true) ")
    public abstract List<Hearing> findByUserFilters(@QueryParam("date") final LocalDate date,
                                                    @QueryParam("userId") final UUID userId);

    @Query(value = "SELECT hearing " +
            "FROM Hearing hearing " +
            "inner join hearing.prosecutionCases prosecutionCase " +
            "inner join prosecutionCase.defendants defendant " +
            "inner join hearing.hearingDays day " +
            "where defendant.id.id = :defendantId " +
            "and day.date > :date "

    )
    public abstract List<Hearing> findByDefendantAndHearingType(@QueryParam("date") final LocalDate date,
                                                                @QueryParam("defendantId") final UUID defendantId);

    @Query(value = "SELECT hearing FROM Hearing hearing  " +
            "WHERE hearing.id = :hearingId " +
            "AND  hearing.jurisdictionType = :jurisdictionType")
    public abstract Hearing findByHearingIdAndJurisdictionType(@QueryParam("hearingId") final UUID hearingId, @QueryParam("jurisdictionType") final JurisdictionType jurisdictionType);

    @Query(value = "SELECT hearing FROM Hearing hearing INNER JOIN hearing.prosecutionCases prosecutionCase " +
            "WHERE prosecutionCase.id.id = :caseId")
    public abstract List<Hearing> findByCaseId(@QueryParam("caseId") final UUID caseId);

    @Query(value = "SELECT hearing FROM Hearing hearing INNER JOIN hearing.prosecutionCases prosecutionCase " +
            "WHERE prosecutionCase.id.id = :caseId " +
            "AND  hearing.jurisdictionType = :jurisdictionType")
    public abstract List<Hearing> findByCaseIdAndJurisdictionType(@QueryParam("caseId") final UUID caseId, @QueryParam("jurisdictionType") final JurisdictionType jurisdictionType);

    @Query(value = "SELECT hearing FROM Hearing hearing INNER JOIN hearing.hearingApplications hearingApplication " +
            "WHERE hearingApplication.id.applicationId = :applicationId")
    public abstract List<Hearing> findAllHearingsByApplicationId(@QueryParam("applicationId") final UUID applicationId);

    @Query(value = "SELECT hearing FROM Hearing hearing INNER JOIN hearing.hearingApplications hearingApplication " +
            "WHERE hearingApplication.id.applicationId = :applicationId " +
            "AND  hearing.jurisdictionType = :jurisdictionType")
    public abstract List<Hearing> findAllHearingsByApplicationIdAndJurisdictionType(@QueryParam("applicationId") final UUID applicationId, @QueryParam("jurisdictionType") final JurisdictionType jurisdictionType);


    @Query(value = "SELECT hearing " +
            "FROM Hearing hearing INNER JOIN hearing.hearingDays day " +
            "WHERE hearing.courtCentre.id = :courtCentreId " +
            "AND hearing.courtCentre.roomId in :roomIds " +
            "AND day.date = :date " +
            "AND (hearing.isBoxHearing IS null OR hearing.isBoxHearing != true) ")
    public abstract List<Hearing> findByFilters(@QueryParam("date") final LocalDate date,
                                                @QueryParam("courtCentreId") final UUID courtCentreId,
                                                @QueryParam("roomIds") final List<UUID> roomIds);

    @Query(value = "SELECT hearing " +
            "FROM Hearing hearing INNER JOIN hearing.hearingDays day " +
            "WHERE day.date = :date " +
            "AND (hearing.isBoxHearing IS null OR hearing.isBoxHearing != true) ")
    public abstract List<Hearing> findByHearingDate(@QueryParam("date") final LocalDate date);

    @Query(value = "SELECT hearing.courtCentre FROM Hearing hearing " +
            "WHERE hearing.id = :hearingId", singleResult = OPTIONAL)
    public abstract CourtCentre findCourtCenterByHearingId(@QueryParam("hearingId") final UUID hearingId);

    @Query(value = "SELECT target FROM Target target " +
            "WHERE target.hearing.id = :hearingId")
    public abstract List<Target> findTargetsByHearingId(@QueryParam("hearingId") final UUID hearingId);

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
    @Query(value = "SELECT target FROM Target target " +
            "WHERE target.hearing.id = :hearingId " +
            "AND (target.hearingDay = :hearingDay OR target.hearingDay IS NULL)")
    public abstract List<Target> findTargetsByFilters(@QueryParam("hearingId") final UUID hearingId,
                                                      @QueryParam("hearingDay") final String hearingDay);

    @Query(value = "SELECT hearing.applicationDraftResults FROM Hearing hearing " +
            "WHERE hearing.id = :hearingId")
    public abstract List<ApplicationDraftResult> findApplicationDraftResultsByHearingId(@QueryParam("hearingId") final UUID hearingId);

    @Query(value = "SELECT prosecutionCase FROM ProsecutionCase prosecutionCase " +
            "WHERE prosecutionCase.hearing.id = :hearingId")
    public abstract List<ProsecutionCase> findProsecutionCasesByHearingId(@QueryParam("hearingId") final UUID hearingId);

    @Query(value = "SELECT distinct hearing " +
            "FROM Hearing hearing INNER JOIN hearing.hearingDays day INNER JOIN hearing.prosecutionCases prosecutionCase " +
            "WHERE (hearing.isBoxHearing IS null OR hearing.isBoxHearing != true) " +
            "AND (hearing.isVacatedTrial IS null OR hearing.isVacatedTrial != true) " +
            "AND day.date > :date " +
            "AND prosecutionCase.id.id IN (:caseIds)")
    public abstract List<Hearing> findHearingsByCaseIdsLaterThan(@QueryParam("caseIds") final List<UUID> caseIds,
                                                                 @QueryParam("date") final LocalDate date);
}
