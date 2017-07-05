package uk.gov.moj.cpp.hearing.persist;

import uk.gov.moj.cpp.hearing.persist.entity.Hearing;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.SingleResultType;

/**
 * Repository for accessing Hearing data.
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@Repository
public abstract class HearingRepository extends AbstractEntityRepository<Hearing, UUID>{


    /**
     * Find {@link Hearing} by hearingId.
     *
     * @param hearingId of the Hearing to retrieve.
     * @return Hearing.
     */
    @Query(singleResult = SingleResultType.OPTIONAL)
    abstract Hearing findByHearingId(final UUID hearingId);

    public Optional<Hearing> getByHearingId(final UUID hearingId) {
        final Hearing hearing  = findByHearingId(hearingId);
        return hearing != null ? Optional.of(hearing) :  Optional.empty();
    }

    /**
     * Find {@link Hearing} by caseId and startDate.
     *
     * @param startDate of the case to retrieve.
     * @return Hearing.
     */

    public abstract List<Hearing> findByStartDate(final LocalDate startDate);


   /**
     * Find {@link Hearing} by hearingIds.
     *
     * @param hearingIds list of hearing ids.
     * @return Hearings.
     */
    @Query(value = "FROM Hearing h where h.hearingId in (:hearingIds)")
    public abstract List<Hearing> findByHearingIds(@QueryParam("hearingIds") final List<UUID> hearingIds);

}