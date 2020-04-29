package uk.gov.moj.cpp.hearing.repository;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.mapping.MappingConfig;
import uk.gov.moj.cpp.hearing.dto.DefendantSearch;
import uk.gov.moj.cpp.hearing.mapping.DefendantSearchMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;

import java.util.UUID;

@Repository(forEntity = Defendant.class)
public abstract class DefendantRepository extends AbstractEntityRepository<Defendant, HearingSnapshotKey> {

    @MappingConfig(DefendantSearchMapper.class)
    @Query(value = "select * from ha_defendant where id = :defendantId LIMIT 1;", isNative = true)
    public abstract DefendantSearch getDefendantDetailsForSearching(@QueryParam("defendantId") final UUID defendantId);

}
