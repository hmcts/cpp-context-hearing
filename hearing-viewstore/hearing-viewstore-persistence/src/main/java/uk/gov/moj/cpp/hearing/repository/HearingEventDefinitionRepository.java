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

    String FIND_ALL_ACTIVE_ORDER_BY_SEQUENCE_TYPE_SEQUENCE_NUMBER_AND_ACTION_LABEL =
            "from HearingEventDefinition hed " +
                    "where hed.deleted is false " +
                    "order by case when hed.sequenceType = 'SENTENCING' then 1 " +
                    "              when hed.sequenceType = 'PAUSE_RESUME' then 2 " +
                    "              when hed.sequenceType is null then 4 " +
                    "              else 3 " +
                    "         end asc " +
                    ", hed.sequenceNumber asc nulls last, lower(hed.actionLabel) asc ";

    @Query(value = FIND_ALL_ACTIVE_ORDER_BY_SEQUENCE_TYPE_SEQUENCE_NUMBER_AND_ACTION_LABEL)
    List<HearingEventDefinition> findAllActiveOrderBySequenceTypeSequenceNumberAndActionLabel();

    @Query(value = "from HearingEventDefinition hed where hed.deleted is false")
    List<HearingEventDefinition>    findAllActive();
}