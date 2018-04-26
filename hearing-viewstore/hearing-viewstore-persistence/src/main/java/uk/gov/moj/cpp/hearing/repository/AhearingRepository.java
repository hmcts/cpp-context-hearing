package uk.gov.moj.cpp.hearing.repository;

import static org.apache.deltaspike.data.api.SingleResultType.OPTIONAL;

import uk.gov.moj.cpp.hearing.persist.entity.ex.Ahearing;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;

/**
 * Repository for accessing Ahearing data.
 */
@Repository(forEntity = Ahearing.class)
public abstract class AhearingRepository extends AbstractEntityRepository<Ahearing, UUID> {

    @Query(value = "from Ahearing h where h.id = :id", singleResult = OPTIONAL)
    public abstract Ahearing findById(@QueryParam("id") final UUID id);

    @Query(value = "SELECT ahd.hearing from AhearingDate ahd where date(ahd.date) = date(:dateTime)")
    public abstract List<Ahearing> findByDate(@QueryParam("dateTime") final ZonedDateTime dateTime);

}