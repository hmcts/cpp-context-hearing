package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;

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
}