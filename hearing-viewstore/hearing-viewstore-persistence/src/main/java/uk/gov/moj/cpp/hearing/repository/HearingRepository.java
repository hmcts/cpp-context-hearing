package uk.gov.moj.cpp.hearing.repository;

import static org.apache.deltaspike.data.api.SingleResultType.OPTIONAL;

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

    @Query(value = "with hearing_days as (select hd.hearing_id, coalesce(hd.court_room_id, h.room_id) as court_room_id, " +
            "coalesce(hd.court_centre_id, h.court_centre_id) as court_centre_id from ha_hearing_day hd " +
            "inner join ha_hearing h on h.id = hd.hearing_id where hd.date = :date " +
            "AND (hd.is_cancelled is null or hd.is_cancelled != true)) " +
            "select distinct hh.* from ha_hearing hh inner join hearing_days hd on hd.hearing_id = hh.id " +
            "where hd.court_centre_id = :courtCentreId and hd.court_room_id = :roomId " +
            "AND (hh.is_box_hearing IS null OR hh.is_box_hearing != true) " +
            "AND (hh.is_vacated_trial IS null OR hh.is_vacated_trial != true) ", isNative = true)
    public abstract List<Hearing> findByFilters(@QueryParam("date") final LocalDate date,
                                                @QueryParam("courtCentreId") final UUID courtCentreId,
                                                @QueryParam("roomId") final UUID roomId);

    @Query(value = "SELECT hearing FROM Hearing hearing INNER JOIN hearing.hearingDays day " +
            "WHERE hearing.courtCentre.id IN (:courtCentreList) " +
            "AND " +
            "day.date = :date ")
    public abstract List<Hearing> findHearingsByDateAndCourtCentreList(@QueryParam("date") final LocalDate date,
                                                                       @QueryParam("courtCentreList") List<UUID> courtCentreList);

    @Query(value = "with hearing_days as (select hd.hearing_id, coalesce(hd.court_room_id, h.room_id) as court_room_id, " +
            "coalesce(hd.court_centre_id, h.court_centre_id) as court_centre_id from ha_hearing_day hd " +
            "inner join ha_hearing h on h.id = hd.hearing_id where hd.date = :date " +
            "AND (hd.is_cancelled is null or hd.is_cancelled != true)) " +
            "select distinct hh.* from ha_hearing hh inner join hearing_days hd on hd.hearing_id = hh.id " +
            "where hd.court_centre_id = :courtCentreId " +
            "AND (hh.is_box_hearing IS null OR hh.is_box_hearing != true) " +
            "AND (hh.is_vacated_trial IS null OR hh.is_vacated_trial != true) ", isNative = true)
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

    @Query(value = "SELECT hearing FROM Hearing hearing INNER JOIN hearing.prosecutionCases prosecutionCase " +
            "WHERE prosecutionCase.id.id = :caseId)")
    public abstract List<Hearing> findByCaseId(@QueryParam("caseId") final UUID caseId);

    @Query(value = "SELECT hearing FROM Hearing hearing INNER JOIN hearing.hearingApplications hearingApplication " +
            "WHERE hearingApplication.id.applicationId = :applicationId)")
    public abstract List<Hearing> findAllHearingsByApplicationId(@QueryParam("applicationId") final UUID applicationId);


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

    @Query(value = "SELECT hearing.targets FROM Hearing hearing " +
            "WHERE hearing.id = :hearingId")
    public abstract List<Target> findTargetsByHearingId(@QueryParam("hearingId") final UUID hearingId);

    @Query(value = "SELECT hearing.applicationDraftResults FROM Hearing hearing " +
            "WHERE hearing.id = :hearingId")
    public abstract List<ApplicationDraftResult> findApplicationDraftResultsByHearingId(@QueryParam("hearingId") final UUID hearingId);

    @Query(value = "SELECT hearing.prosecutionCases FROM Hearing hearing " +
            "WHERE hearing.id = :hearingId")
    public abstract List<ProsecutionCase> findProsecutionCasesByHearingId(@QueryParam("hearingId") final UUID hearingId);
}