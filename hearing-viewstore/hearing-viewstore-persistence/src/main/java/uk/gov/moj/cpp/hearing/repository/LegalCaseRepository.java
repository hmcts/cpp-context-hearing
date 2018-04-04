package uk.gov.moj.cpp.hearing.repository;

import static org.apache.deltaspike.data.api.SingleResultType.OPTIONAL;

import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;

import uk.gov.moj.cpp.hearing.persist.entity.ex.LegalCase;

/**
 * Repository for accessing LegalCase data.
 */
@Repository(forEntity = LegalCase.class)
public abstract class LegalCaseRepository extends AbstractEntityRepository<LegalCase, UUID> {

    @Query(value = "from LegalCase lc where lc.id = :id", singleResult = OPTIONAL)
    public abstract LegalCase findById(@QueryParam("id") final UUID id);

    @Query(value = "from LegalCase lc where lc.id in (:ids)", singleResult = OPTIONAL)
    public abstract List<LegalCase> findByIds(@QueryParam("ids") final List<UUID> ids);
}