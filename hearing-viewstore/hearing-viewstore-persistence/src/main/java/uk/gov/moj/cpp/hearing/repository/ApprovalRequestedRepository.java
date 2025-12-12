package uk.gov.moj.cpp.hearing.repository;

import uk.gov.justice.core.courts.ApprovalType;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ApprovalRequested;

import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;

@Repository(forEntity = ApprovalRequested.class)
public abstract class ApprovalRequestedRepository extends AbstractEntityRepository<ApprovalRequested, UUID> {

    private static final String REMOVE_ALL_REQUEST_APPROVALS = "delete from ha_request_approval where hearing_id =:hearingId";
    private static final String REMOVE_ALL_REQUEST_APPROVALS_FOR_APPROVAL_TYPE = "delete from ha_request_approval where hearing_id =:hearingId and approval_type = :approvalType";

    @Query(value = "SELECT userId from ApprovalRequested WHERE hearing_id = :hearingId")
    public abstract List<UUID> findUsersByHearingId(@QueryParam("hearingId") final UUID hearingId);

    @Query(value = "select * from ha_request_approval WHERE hearing_id = :hearingId", isNative = true)
    public abstract List<ApprovalRequested> findApprovalsRequestByHearingId(@QueryParam("hearingId") final UUID hearingId);

    public int removeAllRequestApprovals(@QueryParam("hearingId") final UUID hearingId) {
        if (hearingId != null) {
            return entityManager()
                    .createNativeQuery(REMOVE_ALL_REQUEST_APPROVALS)
                    .setParameter("hearingId", hearingId).executeUpdate();
        }
        return 0;
    }

    public int removeAllRequestApprovalsForApprovalType(@QueryParam("hearingId") final UUID hearingId,
                                                        @QueryParam("approvalType") final ApprovalType approvalType) {
        if (hearingId != null) {
            return entityManager()
                    .createNativeQuery(REMOVE_ALL_REQUEST_APPROVALS_FOR_APPROVAL_TYPE)
                    .setParameter("hearingId", hearingId)
                    .setParameter("approvalType", approvalType.toString())
                    .executeUpdate();
        }
        return 0;
    }
}