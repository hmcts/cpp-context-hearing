package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.not.Document;

import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class DocumentRepository {

    @PersistenceContext(unitName = "hearing-persistence-unit")
    EntityManager entityManager;

    public Document findBy(final UUID id) {
        return entityManager.find(Document.class, id);
    }

    public Document save(final Document entity) {
        return entityManager.merge(entity);
    }

    public Document saveAndFlush(final Document entity) {
        final Document merged = entityManager.merge(entity);
        entityManager.flush();
        return merged;
    }

    public void remove(final Document entity) {
        final Document managed = entityManager.contains(entity) ? entity : entityManager.merge(entity);
        entityManager.remove(managed);
    }

    public List<Document> findAll() {
        return entityManager.createQuery("SELECT e FROM Document e", Document.class).getResultList();
    }

    public long count() {
        return entityManager.createQuery("SELECT COUNT(e) FROM Document e", Long.class).getSingleResult();
    }

    public List<Document> findAllByOrderByStartDateAsc() {
        return entityManager.createQuery(
                "select d from Document d where (d.startDate <= d.endDate or d.endDate is null) order by d.startDate asc",
                Document.class)
                .getResultList();
    }
}
