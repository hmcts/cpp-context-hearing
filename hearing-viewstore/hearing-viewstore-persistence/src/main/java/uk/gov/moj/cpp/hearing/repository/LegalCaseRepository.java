package uk.gov.moj.cpp.hearing.repository;

import static javax.persistence.LockModeType.PESSIMISTIC_WRITE;
import static org.apache.deltaspike.data.api.SingleResultType.OPTIONAL;

import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;

import uk.gov.moj.cpp.hearing.persist.entity.ex.LegalCase;

/**
 * Repository for accessing LegalCase data.
 */
@Repository(forEntity = LegalCase.class)
public abstract class LegalCaseRepository extends AbstractEntityRepository<LegalCase, UUID>
        implements CriteriaSupport<LegalCase> {

    /**
     * Find {@link uk.gov.moj.cpp.hearing.persist.entity.ex.LegalCase} by id.
     *
     * @param id of the Ahearing to retrieve.
     * @return Ahearing.
     */
    // Lock mode for update
    @Query(value = "from LegalCase lc where lc.id = :id", lock = PESSIMISTIC_WRITE, singleResult = OPTIONAL)
    public abstract LegalCase findById(@QueryParam("id") final UUID id);
    
    /**
     * Find {@link uk.gov.moj.cpp.hearing.persist.entity.ex.LegalCase} by ids.
     *
     * @param ids of the LegalCase list to retrieve.
     * @return a list of LegalCase.
     */
    // Lock mode for update
    @Query(value = "from LegalCase lc where lc.id in (:ids)", lock = PESSIMISTIC_WRITE, singleResult = OPTIONAL)
    public abstract List<LegalCase> findByIds(@QueryParam("ids") final List<UUID> ids);
}