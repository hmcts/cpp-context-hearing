package uk.gov.moj.cpp.hearing.repository;

import static org.apache.deltaspike.data.api.SingleResultType.OPTIONAL;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantAttendance;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import java.time.LocalDate;
import java.util.UUID;

@Repository(forEntity = DefendantAttendance.class)
public abstract class DefendantAttendanceRepository extends AbstractEntityRepository<DefendantAttendance, HearingSnapshotKey> {

    @Query(value = "from DefendantAttendance da where da.hearing.id = :hearingId and da.defendantId = :defendantId and da.day = :day", singleResult = OPTIONAL)
    public abstract DefendantAttendance findByHearingIdDefendantIdAndDate(@QueryParam("hearingId") final UUID hearingId, @QueryParam("defendantId") final UUID defendantId, @QueryParam("day") final LocalDate day);
}
