package uk.gov.moj.cpp.hearing.repository;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("squid:S1166")
@ApplicationScoped
public class CourtListRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(CourtListRepository.class);


    private static final String COURT_LIST_PUBLISH_STATUS_QUERY =
            "SELECT * FROM court_list_publish_status WHERE court_centre_id = :courtCenterId " +
                    "AND publish_status ='EXPORT_SUCCESSFUL' " +
                    " ORDER  BY last_updated DESC limit 1";

    @PersistenceContext(unitName = "hearing-persistence-unit")
    private EntityManager entityManager;

    public CourtListPublishStatus save(final CourtListPublishStatus entity) {
        return entityManager.merge(entity);
    }

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
