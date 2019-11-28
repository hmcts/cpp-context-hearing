package uk.gov.moj.cpp.hearing.repository;

import static java.lang.String.*;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.criteria.CriteriaSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("squid:S1166")
@Repository
@ApplicationScoped
public abstract class CourtListRepository implements EntityRepository<CourtListPublishStatus, UUID>, CriteriaSupport<CourtListPublishStatus> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CourtListRepository.class);


    private static final String COURT_LIST_PUBLISH_STATUS_QUERY =
            "SELECT * FROM court_list_publish_status WHERE court_centre_id = :courtCenterId " +
                    "AND publish_status IN ( 'EXPORT_SUCCESSFUL','EXPORT_FAILED' ) " +
                    " ORDER  BY last_updated DESC limit 1";

    @Inject
    private EntityManager entityManager;

    public Optional<CourtListPublishStatusResult> courtListPublishStatuses(final UUID courtCentreId) {
        try {
            final CourtListPublishStatus resultList = (CourtListPublishStatus) entityManager.createNativeQuery(COURT_LIST_PUBLISH_STATUS_QUERY, CourtListPublishStatus.class)
                    .setParameter("courtCenterId", courtCentreId).getSingleResult();

            return of(new CourtListPublishStatusResult(resultList.getCourtCentreId(), resultList.getLastUpdated(), resultList.getPublishStatus()));
        } catch (final NoResultException nre) {
            LOGGER.warn(format("No EXHIBT export status found for courtCentreId: %s", courtCentreId));
            return empty();
        }
    }
}