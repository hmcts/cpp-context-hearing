package uk.gov.moj.cpp.hearing.persist;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;

import uk.gov.moj.cpp.hearing.domain.HearingStatusEnum;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;

/**
 * Repository for accessing Hearing data.
 */
@Repository
public interface HearingRepository extends EntityRepository<Hearing, UUID> {

    /**
     * Find {@link Hearing} by hearingId.
     *
     * @param hearingId
     *            of the Hearing to retrieve.
     * @return Hearing.
     */
    Hearing findByHearingId(UUID hearingId);

    /**
     * Find all {@link Hearing}s.
     * 
     * @return Hearing.
     */
    @Override
    List<Hearing> findAll();

    /**
     * Find {@link Hearing} by caseId and startDate.
     *
     * @param caseId
     *            of the case to retrieve.
     * @param startDate
     *            of the case to retrieve.
     * @return Hearing.
     */
    List<Hearing> findByCaseIdAndStartDateGreaterThanEquals(UUID caseId, LocalDate startDate);

    /**
     * Find {@link Hearing} by caseid.
     *
     * @param caseId
     *            of the case to retrieve.
     * @param hearingStatus TODO
     * @return Hearing.
     */
    List<Hearing> findByCaseIdAndStatusEqual(UUID caseId, HearingStatusEnum hearingStatus);

}