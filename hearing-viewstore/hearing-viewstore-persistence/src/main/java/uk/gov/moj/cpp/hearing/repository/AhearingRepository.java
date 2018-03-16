package uk.gov.moj.cpp.hearing.repository;

import static javax.persistence.LockModeType.PESSIMISTIC_WRITE;
import static org.apache.deltaspike.data.api.SingleResultType.OPTIONAL;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;

import uk.gov.moj.cpp.hearing.persist.entity.ex.Ahearing;

/**
 * Repository for accessing Ahearing data.
 */
@Repository(forEntity = Ahearing.class)
public abstract class AhearingRepository extends AbstractEntityRepository<Ahearing, UUID>
        implements CriteriaSupport<Ahearing> {

    /**
     * Find {@link uk.gov.moj.cpp.hearing.persist.entity.ex.Ahearing} by id.
     *
     * @param id of the Ahearing to retrieve.
     * @return Ahearing.
     */
    // Lock mode for update
    @Query(value = "from Ahearing h where h.id = :id", lock = PESSIMISTIC_WRITE, singleResult = OPTIONAL)
    public abstract Ahearing findById(@QueryParam("id") final UUID id);
    
    /**
     * Find {@link uk.gov.moj.cpp.hearing.persist.entity.ex.Ahearing} by start date time.
     * @param startDateTime of the Ahearing to retrieve.
     * @return a list of Ahearing.
     */
    // Lock mode for update
    @Query(value = "from Ahearing h where h.startDateTime >= :startDateTime", lock = PESSIMISTIC_WRITE)
    public abstract List<Ahearing> findByStartDateTime(@QueryParam("startDateTime") final LocalDateTime startDateTime);

}