package uk.gov.moj.cpp.hearing.persist;

import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounselToDefendant;

import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@Repository
public abstract class DefenceCounselRepository extends AbstractEntityRepository<DefenceCounsel, UUID> {
    /**
     * Find {@link DefenceCounsel}s by hearingId.
     *
     * @param hearingId of the {@link DefenceCounsel} retrieve.
     * @return HearingCases.
     */
    public abstract List<DefenceCounsel> findByHearingId(final UUID hearingId);

    @Query(value = "select new uk.gov.moj.cpp.hearing.persist.entity.DefenceCounselToDefendant(dc.personId, dcd.defendantId) from DefenceCounsel dc, DefenceCounselDefendant dcd where dcd.defenceCounselAttendeeId = dc.attendeeId and dc.hearingId = :hearingId")
    public abstract List<DefenceCounselToDefendant> findDefenceCounselAndDefendantByHearingId(@QueryParam("hearingId") final UUID hearingId);

}
