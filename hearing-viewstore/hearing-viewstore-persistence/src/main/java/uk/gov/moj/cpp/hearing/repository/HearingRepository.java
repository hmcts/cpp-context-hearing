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
}