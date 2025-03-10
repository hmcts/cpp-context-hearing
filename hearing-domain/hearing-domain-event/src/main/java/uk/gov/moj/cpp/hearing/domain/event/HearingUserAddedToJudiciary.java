package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Event("hearing.event.user-added-to-judiciary")
public class HearingUserAddedToJudiciary implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID judiciaryId;
    private String emailId;
    private UUID cpUserId;
    private UUID hearingId;
    private UUID id;

    public HearingUserAddedToJudiciary(final UUID judiciaryId, final String emailId, final UUID cpUserId, final UUID hearingId, final UUID id) {
        this.judiciaryId = judiciaryId;
        this.emailId = emailId;
        this.cpUserId = cpUserId;
        this.hearingId = hearingId;
        this.id = id;
    }

    public UUID getJudiciaryId() {
        return judiciaryId;
    }

    public void setJudiciaryId(final UUID judiciaryId) {
        this.judiciaryId = judiciaryId;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(final String emailId) {
        this.emailId = emailId;
    }

    public UUID getCpUserId() {
        return cpUserId;
    }

    public void setCpUserId(final UUID cpUserId) {
        this.cpUserId = cpUserId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final HearingUserAddedToJudiciary that = (HearingUserAddedToJudiciary) o;
        return Objects.equals(judiciaryId, that.judiciaryId) && Objects.equals(emailId, that.emailId) && Objects.equals(cpUserId, that.cpUserId) && Objects.equals(hearingId, that.hearingId) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(judiciaryId, emailId, cpUserId, hearingId, id);
    }

    @Override
    public String toString() {
        return "HearingUserAddedToJudiciary{" +
                "judiciaryId='" + judiciaryId + '\'' +
                ", emailId='" + emailId + '\'' +
                ", cpUserId='" + cpUserId + '\'' +
                ", hearingId='" + hearingId + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
