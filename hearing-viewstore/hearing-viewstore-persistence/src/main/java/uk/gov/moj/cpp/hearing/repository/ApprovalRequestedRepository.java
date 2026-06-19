package uk.gov.moj.cpp.hearing.repository;

import uk.gov.justice.core.courts.ApprovalType;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ApprovalRequested;

import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class ApprovalRequestedRepository {

    private static final String REMOVE_ALL_REQUEST_APPROVALS = "delete from ha_request_approval where hearing_id =:hearingId";
    private static final String REMOVE_ALL_REQUEST_APPROVALS_FOR_APPROVAL_TYPE = "delete from ha_request_approval where hearing_id =:hearingId and approval_type = :approvalType";

    @PersistenceContext(unitName = "hearing-persistence-unit")
    EntityManager entityManager;

    public List<UUID> findUsersByHearingId(final UUID hearingId) {
        return entityManager.createQuery("SELECT a.userId FROM ApprovalRequested a WHERE a.hearingId = :hearingId", UUID.class)
                .setParameter("hearingId", hearingId)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<ApprovalRequested> findApprovalsRequestByHearingId(final UUID hearingId) {
        return entityManager.createNativeQuery("select * from ha_request_approval WHERE hearing_id = :hearingId", ApprovalRequested.class)
                .setParameter("hearingId", hearingId)
                .getResultList();
    }

    public int removeAllRequestApprovals(final UUID hearingId) {
        if (hearingId != null) {
            return entityManager
                    .createNativeQuery(REMOVE_ALL_REQUEST_APPROVALS)
                    .setParameter("hearingId", hearingId).executeUpdate();
        }
        return 0;
    }

    public int removeAllRequestApprovalsForApprovalType(final UUID hearingId,
                                                        final ApprovalType approvalType) {
        if (hearingId != null) {
            return entityManager
                    .createNativeQuery(REMOVE_ALL_REQUEST_APPROVALS_FOR_APPROVAL_TYPE)
                    .setParameter("hearingId", hearingId)
                    .setParameter("approvalType", approvalType.toString())
                    .executeUpdate();
        }
        return 0;
    }
}
