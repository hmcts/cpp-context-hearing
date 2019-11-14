package uk.gov.moj.cpp.hearing.repository;

import static java.util.stream.Collectors.toList;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;

@Repository
@ApplicationScoped
public abstract class CourtListRepository implements EntityRepository<CourtList, UUID>, CriteriaSupport<CourtList> {

    private static final String COURT_CENTRE_ID = "courtCentreId";
    private static final String LAST_UPDATED = "lastUpdated";
    private static final String COURTLIST_PK = "courtListPK";
    @Inject
    private EntityManager entityManager;

    public List<CourtListPublishStatus> courtListPublishStatuses(final UUID courtCentreId) {

        final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        final CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(CourtList.class);
        final Root<CourtList> mainQueryRoot = criteriaQuery.from(CourtList.class);

        final Subquery<Timestamp> maxSubQuery = criteriaQuery.subquery(Timestamp.class);
        final Root<CourtList> subQueryRoot = maxSubQuery.from(CourtList.class);
        maxSubQuery.select(criteriaBuilder.greatest(subQueryRoot.<Timestamp>get(LAST_UPDATED)));
        criteriaQuery.where(criteriaBuilder.equal(mainQueryRoot.get(LAST_UPDATED), maxSubQuery));

        final Predicate conditionCourtCentreIdPredicate = criteriaBuilder.equal(mainQueryRoot.get(COURTLIST_PK).get(COURT_CENTRE_ID),
                courtCentreId);

        final Predicate combinedPredicate = criteriaBuilder.and(conditionCourtCentreIdPredicate);
        criteriaQuery.where(combinedPredicate);
        final List resultList = entityManager.createQuery(criteriaQuery).getResultList();
        return courtListPublishStatuses(resultList);
    }

    private List<CourtListPublishStatus> courtListPublishStatuses(final List<CourtList> courtLists) {
        return courtLists.stream().map(x -> {
            final CourtListPublishStatus courtListPublishStatus = new CourtListPublishStatus(
                    x.getCourtListPK().getCourtCentreId(),
                    x.getLastUpdated(),
                    x.getCourtListPK().getPublishStatus());
            courtListPublishStatus.setFailureMessage(x.getErrorMessage());
            return courtListPublishStatus;
        }).collect(toList());
    }

}