package uk.gov.moj.cpp.hearing.repository;


import static java.util.Optional.empty;
import static org.apache.deltaspike.data.api.SingleResultType.OPTIONAL;

import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;

@SuppressWarnings({"CdiManagedBeanInconsistencyInspection", "squid:S1192"})
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

    private static final String GET_CURRENT_ACTIVE_HEARINGS_FOR_COURT_CENTRE =
            "SELECT hearingEvent FROM uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent hearingEvent, " +
                    "uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing hearing " +
                    "WHERE hearing.id = hearingEvent.hearingId and " +
                    "hearing.courtCentre.id = :courtCentreId and " +
                    "hearingEvent.lastModifiedTime >= :lastModifiedTime and " +
                    "hearingEvent.deleted is false ";

    private static final String GET_CURRENT_ACTIVE_HEARINGS_FOR_COURT_CENTRE_LIST =
            "SELECT hearingEvent FROM uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent hearingEvent, " +
                    "uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing hearing " +
                    "WHERE hearing.id = hearingEvent.hearingId and " +
                    "hearing.courtCentre.id IN (:courtCentreList) and " +
                    "hearingEvent.eventTime >= :lastModifiedTime and " +
                    "hearingEvent.deleted is false and " +
                    "hearingEvent.hearingEventDefinitionId IN (:cppHearingEventIds)";


    private static final String GET_LATEST_HEARINGS_FOR_COURT_CENTRE_LIST =
            "SELECT new uk.gov.moj.cpp.hearing.repository.HearingEventPojo( " +
                    "hearingEvent.defenceCounselId," +
                    "hearingEvent.deleted," +
                    "hearingEvent.eventDate," +
                    "hearingEvent.eventTime," +
                    "hearingEvent.hearingEventDefinitionId," +
                    "hearingEvent.hearingId," +
                    "hearingEvent.id," +
                    "hearingEvent.lastModifiedTime," +
                    "hearingEvent.recordedLabel) " +
                    "FROM uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent hearingEvent, " +
                    "uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing hearing " +
                    "WHERE hearing.id = hearingEvent.hearingId and " +
                    "hearing.courtCentre.id IN (:courtCentreList) and " +
                    "hearingEvent.eventTime = (select max(hearingEvent2.eventTime) " +
                    "from uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent hearingEvent2, " +
                    "uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing hearing2 " +
                    "WHERE hearing2.id = hearingEvent2.hearingId and " +
                    "hearing2.courtCentre.roomId = hearing.courtCentre.roomId and " +
                    "hearingEvent2.hearingEventDefinitionId IN (:cppHearingEventIds) " +
                    "group by hearing2.courtCentre.roomId) and  " +
                    "hearingEvent.eventDate = :eventDate and " +
                    "hearingEvent.deleted is false " +
                    " order by hearingEvent.eventTime desc";

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

    @Query(value = GET_CURRENT_ACTIVE_HEARINGS_FOR_COURT_CENTRE)
    public abstract List<HearingEvent> findBy(@QueryParam("courtCentreId") final UUID courtCentreId, @QueryParam("lastModifiedTime") final ZonedDateTime lastModifiedTime);

    @Query(value = GET_CURRENT_ACTIVE_HEARINGS_FOR_COURT_CENTRE_LIST)
    public abstract List<HearingEvent> findBy(@QueryParam("courtCentreList") final List<UUID> courtCentreList, @QueryParam("lastModifiedTime") final ZonedDateTime lastModifiedTime, @QueryParam("cppHearingEventIds") final Set<UUID> cppHearingEventIds);

    @Query(value = GET_LATEST_HEARINGS_FOR_COURT_CENTRE_LIST)
    public abstract List<HearingEventPojo> findLatestHearingsForThatDay(@QueryParam("courtCentreList") final List<UUID> courtCentreList, @QueryParam("eventDate") final LocalDate eventDate, @QueryParam("cppHearingEventIds") final Set<UUID> cppHearingEventIds);

}
