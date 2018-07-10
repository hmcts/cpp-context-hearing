package uk.gov.moj.cpp.hearing.repository;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;
import uk.gov.moj.cpp.hearing.persist.entity.not.Document;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends EntityRepository<Document, UUID> {

    @Query(value = "select d from Document d where (d.startDate <= d.endDate or d.endDate is null) order by d.startDate asc")
    List<Document> findAllByOrderByStartDateAsc();
}
