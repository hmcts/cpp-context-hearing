package uk.gov.moj.cpp.hearing.persist;


import uk.gov.moj.cpp.hearing.persist.entity.HearingEventDefinitionEntity;

import java.util.UUID;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Modifying;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;

@Repository
public interface HearingDefinitionsRepository extends EntityRepository<HearingEventDefinitionEntity, UUID> {
    @Modifying
    @Query("delete HearingEventDefinitionEntity")
    void deleteAll();
}