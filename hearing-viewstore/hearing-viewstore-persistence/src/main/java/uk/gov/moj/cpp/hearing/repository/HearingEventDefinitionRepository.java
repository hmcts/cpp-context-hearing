package uk.gov.moj.cpp.hearing.repository;


import uk.gov.moj.cpp.hearing.persist.entity.heda.HearingEventDefinition;

import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@Repository
public interface HearingEventDefinitionRepository extends EntityRepository<HearingEventDefinition, UUID> {

    String FIND_ALL_ACTIVE_ORDER_BY_SEQUENCE_TYPE_SEQUENCE_NUMBER_AND_ACTION_LABEL = "SELECT hed FROM HearingEventDefinition hed WHERE hed.deleted is false ORDER BY hed.groupSequence ASC, hed.actionSequence ASC";

    @Query(value = FIND_ALL_ACTIVE_ORDER_BY_SEQUENCE_TYPE_SEQUENCE_NUMBER_AND_ACTION_LABEL)
    List<HearingEventDefinition> findAllActiveOrderBySequenceTypeSequenceNumberAndActionLabel();

    @Query(value = "from HearingEventDefinition hed where hed.deleted is false")
    List<HearingEventDefinition> findAllActive();
}