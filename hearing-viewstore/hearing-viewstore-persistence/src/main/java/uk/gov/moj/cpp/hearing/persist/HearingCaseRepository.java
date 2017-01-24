package uk.gov.moj.cpp.hearing.persist;


import uk.gov.moj.cpp.hearing.persist.entity.HearingCase;

import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@Repository
public abstract class HearingCaseRepository extends AbstractEntityRepository<HearingCase, UUID> {
    /**
     * Find {@link HearingCase} by hearing Id.
     *
     * @param hearingid of the case to retrieve.
     * @return HearingCases.
     */
    public abstract List<HearingCase> findByHearingId(UUID hearingid);

    /**
     * Find {@link HearingCase} by hearing Ids.
     *
     * @param hearingids of the case to retrieve.
     * @return HearingCases.
     */
    @Query(value = "FROM HearingCase hc where hc.hearingId in (:hearingids)")
    public abstract List<HearingCase> findByHearingIds(@QueryParam("hearingids") List<UUID> hearingids);

    /**
     * Find {@link HearingCase} by case Id.
     *
     * @param caseId of the case to retrieve.
     * @return HearingCases.
     */
    public abstract List<HearingCase> findByCaseId(UUID caseId);

}
