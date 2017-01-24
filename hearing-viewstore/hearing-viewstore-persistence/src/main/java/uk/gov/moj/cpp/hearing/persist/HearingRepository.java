package uk.gov.moj.cpp.hearing.persist;

import org.apache.deltaspike.data.api.*;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    abstract Hearing findByHearingId(UUID hearingId);

    public Optional<Hearing> getByHearingId(UUID hearingId) {
        Hearing hearing  = findByHearingId(hearingId);
        return hearing != null ? Optional.of(hearing) :  Optional.empty();
    }

    /**
     * Find {@link Hearing} by caseId and startDate.
     *
     * @param startdate of the case to retrieve.
     * @return Hearing.
     */

    public abstract List<Hearing> findByStartdate(LocalDate startdate);


   /**
     * Find {@link Hearing} by hearingids.
     *
     * @param hearingids list of hearing ids.
     * @return Hearings.
     */
    @Query(value = "FROM Hearing h where h.hearingId in (:hearingids)")
    public abstract List<Hearing> findByHearingIds(@QueryParam("hearingids") List<UUID> hearingids);

}