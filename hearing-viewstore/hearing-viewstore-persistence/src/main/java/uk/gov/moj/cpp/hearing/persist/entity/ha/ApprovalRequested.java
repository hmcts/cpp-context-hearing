package uk.gov.moj.cpp.hearing.persist.entity.ha;


import uk.gov.justice.core.courts.ApprovalType;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "ha_request_approval")
public class ApprovalRequested {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "hearing_id", nullable = false)
    private UUID hearingId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "request_approval_time", nullable = false)
    private ZonedDateTime requestApprovalTime;

    @Column(name = "approval_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ApprovalType approvalType;

    @ManyToOne
    @JoinColumn(name = "hearing_id", insertable = false, updatable = false)
    private Hearing hearing;


    public ApprovalRequested() {
    }


    public ApprovalRequested(final UUID id,
                             final UUID hearingId,
                             final UUID userId,
                             final ZonedDateTime requestApprovalTime,
                             final ApprovalType approvalType) {
        this.id = id;
        this.hearingId = hearingId;
        this.userId = userId;
        this.requestApprovalTime = requestApprovalTime;
        this.approvalType = approvalType;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public ZonedDateTime getRequestApprovalTime() {
        return requestApprovalTime;
    }

    public void setRequestApprovalTime(final ZonedDateTime requestApprovalTime) {
        this.requestApprovalTime = requestApprovalTime;
    }

    public ApprovalType getApprovalType() {
        return approvalType;
    }

    public void setApprovalType(final ApprovalType approvalType) {
        this.approvalType = approvalType;
    }


    @Override
    public String toString() {
        return "ApprovalRequested{" +
                "id=" + id +
                ", hearingId=" + hearingId +
                ", userId=" + userId +
                ", requestApprovalTime=" + requestApprovalTime +
                ", approvalType=" + approvalType +
                '}';
    }

    @SuppressWarnings({"squid:S00121,squid:S1067"})
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ApprovalRequested)) {
            return false;
        }
        final ApprovalRequested that = (ApprovalRequested) o;

        final boolean idSame = Objects.equals(id, that.id);
        final boolean hearingIdSame = Objects.equals(hearingId, that.hearingId);
        final boolean userIdSame = Objects.equals(userId, that.userId);
        final boolean approvalTypeSame = Objects.equals(approvalType, that.approvalType);
        final boolean requestApprovalTimeSame = Objects.equals(requestApprovalTime, that.requestApprovalTime);
        final boolean idsSame = idSame && hearingIdSame && userIdSame;
        return idsSame && approvalTypeSame && requestApprovalTimeSame;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, hearingId, userId, requestApprovalTime, approvalType);
    }

}
