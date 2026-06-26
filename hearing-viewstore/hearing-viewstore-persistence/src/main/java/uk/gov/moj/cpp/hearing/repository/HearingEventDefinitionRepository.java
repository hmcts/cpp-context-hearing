package uk.gov.moj.cpp.hearing.repository;


import uk.gov.moj.cpp.hearing.persist.entity.heda.HearingEventDefinition;

import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class HearingEventDefinitionRepository {

    static final String FIND_ALL_ACTIVE_ORDER_BY_SEQUENCE_TYPE_SEQUENCE_NUMBER_AND_ACTION_LABEL = "SELECT hed FROM HearingEventDefinition hed WHERE hed.deleted is false ORDER BY hed.groupSequence ASC, hed.actionSequence ASC";

    @PersistenceContext(unitName = "hearing-persistence-unit")
    EntityManager entityManager;

    public HearingEventDefinition findBy(final UUID id) {
        return entityManager.find(HearingEventDefinition.class, id);
    }

    public HearingEventDefinition save(final HearingEventDefinition entity) {
        return entityManager.merge(entity);
    }

    public void remove(final HearingEventDefinition entity) {
        final HearingEventDefinition managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public List<HearingEventDefinition> findAll() {
        return entityManager.createQuery("SELECT e FROM HearingEventDefinition e", HearingEventDefinition.class).getResultList();
    }

    public long count() {
        return entityManager.createQuery("SELECT COUNT(e) FROM HearingEventDefinition e", Long.class).getSingleResult();
    }

    public List<HearingEventDefinition> findAllActiveOrderBySequenceTypeSequenceNumberAndActionLabel() {
        return entityManager.createQuery(FIND_ALL_ACTIVE_ORDER_BY_SEQUENCE_TYPE_SEQUENCE_NUMBER_AND_ACTION_LABEL, HearingEventDefinition.class)
                .getResultList();
    }

    public List<HearingEventDefinition> findAllActive() {
        return entityManager.createQuery("SELECT hed FROM HearingEventDefinition hed WHERE hed.deleted is false", HearingEventDefinition.class)
                .getResultList();
    }
}
