package uk.gov.moj.cpp.hearing.repository;

import static org.apache.deltaspike.data.api.SingleResultType.OPTIONAL;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;


@Repository(forEntity = Hearing.class)
public abstract class HearingRepository extends AbstractEntityRepository<Hearing, UUID> {

    @Query(value = "from Hearing h where h.id = :id", singleResult = OPTIONAL)
    public abstract Hearing findById(@QueryParam("id") final UUID id);

    @Query(value = "SELECT ahd.hearing from HearingDate ahd where ahd.date = :date")
    public abstract List<Hearing> findByDate(@QueryParam("date") final LocalDate date);

}