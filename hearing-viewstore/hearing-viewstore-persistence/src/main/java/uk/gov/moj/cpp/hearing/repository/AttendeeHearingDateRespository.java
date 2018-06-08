package uk.gov.moj.cpp.hearing.repository;

import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Modifying;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.AttendeeHearingDate;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@Repository
public abstract class AttendeeHearingDateRespository extends AbstractEntityRepository<AttendeeHearingDate, HearingSnapshotKey> {

    @Modifying
    @Query("delete from AttendeeHearingDate ahd where ahd.id.hearingId = :hearingId and ahd.attendeeId = :attendeeId and ahd.hearingDateId = :hearingDateId)")
    public abstract int delete(@QueryParam("hearingId") final UUID hearingId, @QueryParam("attendeeId") final UUID attendeeId, @QueryParam("hearingDateId") final UUID hearingDateId);

    @Query("from AttendeeHearingDate ahd where ahd.attendeeId = :attendeeId and ahd.id.hearingId = :hearingId")
    public abstract List<AttendeeHearingDate> findByAttendeeIdAndHearingId(@QueryParam("attendeeId") final UUID attendeeId, final @QueryParam("hearingId") UUID hearingId);
}