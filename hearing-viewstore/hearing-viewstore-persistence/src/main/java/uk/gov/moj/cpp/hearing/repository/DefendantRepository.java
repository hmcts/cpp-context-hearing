package uk.gov.moj.cpp.hearing.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityManager;
import uk.gov.moj.cpp.hearing.dto.DefendantSearch;
import uk.gov.moj.cpp.hearing.mapping.DefendantSearchMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class DefendantRepository {

    @PersistenceContext(unitName = "hearing-persistence-unit")
    EntityManager entityManager;

    public DefendantSearch getDefendantDetailsForSearching(final UUID defendantId) {
        final Defendant defendant = entityManager
                .createQuery("SELECT d FROM Defendant d WHERE d.id.id = :defendantId", Defendant.class)
                .setParameter("defendantId", defendantId)
                .getResultList()
                .stream()
                .findFirst()
                .orElse(null);
        return defendant != null ? new DefendantSearchMapper().toDto(defendant) : null;
    }

    public Defendant findBy(final HearingSnapshotKey id) {
        return entityManager.find(Defendant.class, id);
    }

    public Defendant save(final Defendant entity) {
        return entityManager.merge(entity);
    }

    public Defendant saveAndFlush(final Defendant entity) {
        final Defendant merged = entityManager.merge(entity);
        entityManager.flush();
        return merged;
    }

    public void remove(final Defendant entity) {
        final Defendant managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public List<Defendant> findAll() {
        return entityManager.createQuery("SELECT e FROM Defendant e", Defendant.class).getResultList();
    }

}
