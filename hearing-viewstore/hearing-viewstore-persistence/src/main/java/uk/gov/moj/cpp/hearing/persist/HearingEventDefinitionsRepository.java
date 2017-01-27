package uk.gov.moj.cpp.hearing.persist;


import uk.gov.moj.cpp.hearing.persist.entity.HearingEventDefinitions;

import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Modifying;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@Repository
public interface HearingEventDefinitionsRepository extends EntityRepository<HearingEventDefinitions, UUID> {

    @Modifying
    @Query("delete HearingEventDefinitions")
    void deleteAll();

    @Override
    @Query(value = "from HearingEventDefinitions hed order by hed.sequenceNumber asc")
    List<HearingEventDefinitions> findAll();
}