package uk.gov.moj.cpp.hearing.repository;


import static java.util.Optional.empty;
import static org.apache.deltaspike.data.api.SingleResultType.OPTIONAL;

import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@Repository
public abstract class HearingEventRepository extends AbstractEntityRepository<HearingEvent, UUID> {

    private static final String GET_ACTIVE_HEARINGS_FOR_COURT_ROOM =
            "SELECT hearingEvent FROM uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent hearingEvent, " +
                    "uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing hearing " +
                    "WHERE hearing.id = hearingEvent.hearingId and " +
                    "hearing.courtCentre.id = :courtCentreId and " +
                    "hearing.courtCentre.roomId = :roomId and " +
                    "hearingEvent.eventDate = :date and " +
                    "hearingEvent.deleted is false and " +
                    "hearingEvent.alterable is false";

    private static final String GET_HEARING_LOG_FOR_HEARING_ID_AND_DATE =
            "SELECT hearingEvent FROM uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent hearingEvent " +
                    "WHERE hearingEvent.hearingId = :hearingId and " +
                    "hearingEvent.deleted is false and " +
                    "hearingEvent.eventDate = :date " +
                    "order by hearingEvent.eventTime asc ";

    public Optional<HearingEvent> findOptionalById(final UUID hearingEventId) {
        final HearingEvent hearingEvent = findBy(hearingEventId);
        return hearingEvent == null ? empty() : Optional.of(hearingEvent);
    }

    @Query(value = "from HearingEvent he where he.deleted is false and he.id = :hearingEventId", singleResult = OPTIONAL)
    public abstract HearingEvent findBy(@QueryParam("hearingEventId") final UUID hearingEventId);

    @Query(value = GET_HEARING_LOG_FOR_HEARING_ID_AND_DATE)
    public abstract List<HearingEvent> findByHearingIdOrderByEventTimeAsc(@QueryParam("hearingId") final UUID hearingId,
                                                                          @QueryParam("date") final LocalDate date);

    @Query(value = "from HearingEvent he where he.deleted is false and he.hearingId = :hearingId order by he.eventTime asc")
    public abstract List<HearingEvent> findByHearingIdOrderByEventTimeAsc(@QueryParam("hearingId") final UUID hearingId);

    @Query(value = GET_ACTIVE_HEARINGS_FOR_COURT_ROOM)
    public abstract List<HearingEvent> findHearingEvents(@QueryParam("courtCentreId") final UUID courtCentreId,
                                                         @QueryParam("roomId") final UUID roomId,
                                                         @QueryParam("date") final LocalDate date);

    @Query(value = "from HearingEvent he where he.deleted is false and he.hearingId = :hearingId and he.recordedLabel = :recordedLabel")
    public abstract List<HearingEvent> findHearingEvents(@QueryParam("hearingId") final UUID hearingId, @QueryParam("recordedLabel") final String recordedLabel);
}
