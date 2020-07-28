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

    @Query(value = "SELECT hearing from Hearing hearing inner join hearing.hearingDays day " +
            "WHERE hearing.courtCentre.id = :courtCentreId and " +
            "hearing.courtCentre.roomId = :roomId and " +
            "day.date = :date and " +
            "( hearing.isBoxHearing is null or hearing.isBoxHearing != true)")
    public abstract List<Hearing> findByFilters(@QueryParam("date") final LocalDate date,
                                                @QueryParam("courtCentreId") final UUID courtCentreId,
                                                @QueryParam("roomId") final UUID roomId);

    @Query(value = "SELECT hearing from Hearing hearing inner join hearing.hearingDays day " +
            "WHERE hearing.courtCentre.id IN (:courtCentreList) " +
            "and " +
            "day.date = :date ")
    public abstract List<Hearing> findHearingsByDateAndCourtCentreList(@QueryParam("date") final LocalDate date,
                                                                       @QueryParam("courtCentreList") List<UUID> courtCentreList);

    @Query(value = "SELECT hearing from Hearing hearing inner join hearing.hearingDays day " +
            "WHERE hearing.courtCentre.id = :courtCentreId and " +
            "day.date = :date and " +
            "( hearing.isBoxHearing is null or hearing.isBoxHearing != true)")
    public abstract List<Hearing> findHearings(@QueryParam("date") final LocalDate date,
                                                @QueryParam("courtCentreId") final UUID courtCentreId);


    @Query(value = "SELECT distinct hearing from Hearing hearing inner join hearing.hearingDays day inner join hearing.judicialRoles role " +
            "WHERE role.userId = :userId and " +
            "day.date = :date and " +
            "( hearing.isBoxHearing is null or hearing.isBoxHearing != true)")
    public abstract List<Hearing> findByUserFilters(@QueryParam("date") final LocalDate date,
                                                    @QueryParam("userId") final UUID userId);

    @Query(value = "SELECT hearing from Hearing hearing inner join hearing.prosecutionCases prosecutionCase " +
            "WHERE prosecutionCase.id.id = :caseId)")
    public abstract List<Hearing> findByCaseId(@QueryParam("caseId") final UUID caseId);

    @Query(value = "SELECT hearing from Hearing hearing inner join hearing.hearingApplications hearingApplication " +
            "WHERE hearingApplication.id.applicationId = :applicationId)")
    public abstract List<Hearing> findAllHearingsByApplicationId(@QueryParam("applicationId") final UUID applicationId);


    @Query(value = "SELECT hearing from Hearing hearing inner join hearing.hearingDays day " +
            "WHERE hearing.courtCentre.id = :courtCentreId and " +
            "hearing.courtCentre.roomId in :roomIds and " +
            "day.date = :date and " +
            "( hearing.isBoxHearing is null or hearing.isBoxHearing != true)")
    public abstract List<Hearing> findByFilters(@QueryParam("date") final LocalDate date,
                                                @QueryParam("courtCentreId") final UUID courtCentreId,
                                                @QueryParam("roomIds") final List<UUID> roomIds);

    @Query(value = "SELECT hearing from Hearing hearing inner join hearing.hearingDays day " +
            "WHERE day.date = :date and " +
            "( hearing.isBoxHearing is null or hearing.isBoxHearing != true)")
    public abstract List<Hearing> findByHearingDate(@QueryParam("date") final LocalDate date);

    @Query(value = "SELECT hearing.courtCentre from Hearing hearing " +
            "WHERE hearing.id = :hearingId", singleResult = OPTIONAL)
    public abstract CourtCentre findCourtCenterByHearingId(@QueryParam("hearingId") final UUID hearingId);

    @Query(value = "SELECT hearing.targets from Hearing hearing " +
            "WHERE hearing.id = :hearingId")
    public abstract List<Target> findTargetsByHearingId(@QueryParam("hearingId") final UUID hearingId);

    @Query(value = "SELECT hearing.applicationDraftResults from Hearing hearing " +
            "WHERE hearing.id = :hearingId")
    public abstract List<ApplicationDraftResult> findApplicationDraftResultsByHearingId(@QueryParam("hearingId") final UUID hearingId);

    @Query(value = "SELECT hearing.prosecutionCases from Hearing hearing " +
            "WHERE hearing.id = :hearingId")
    public abstract List<ProsecutionCase> findProsecutionCasesByHearingId(@QueryParam("hearingId") final UUID hearingId);
}