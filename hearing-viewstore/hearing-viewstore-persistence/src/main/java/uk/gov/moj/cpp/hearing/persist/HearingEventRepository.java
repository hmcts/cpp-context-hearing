package uk.gov.moj.cpp.hearing.persist;


import static java.util.Optional.empty;
import static org.apache.deltaspike.data.api.SingleResultType.OPTIONAL;

import uk.gov.moj.cpp.hearing.persist.entity.HearingEvent;

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

    public Optional<HearingEvent> findOptionalById(final UUID hearingEventId) {
        final HearingEvent hearingEvent = findBy(hearingEventId);
        return hearingEvent == null ? empty() : Optional.of(hearingEvent);
    }

    @Query(value = "from HearingEvent he where he.deleted is false and he.id = :hearingEventId", singleResult = OPTIONAL)
    public abstract HearingEvent findBy(@QueryParam("hearingEventId") final UUID hearingEventId);

    @Query(value = "from HearingEvent he where he.deleted is false and he.hearingId = :hearingId order by he.eventTime asc")
    public abstract List<HearingEvent> findByHearingIdOrderByEventTimeAsc(@QueryParam("hearingId") final UUID hearingId);
}
