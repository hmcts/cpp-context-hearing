package uk.gov.moj.cpp.hearing.repository;


import static javax.persistence.EnumType.STRING;

import uk.gov.moj.cpp.hearing.publishing.events.PublishStatus;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "court_list_publish_status")
public class CourtListPublishStatus {

    @Id
    @Column(name = "court_list_publish_status_id", nullable = false)
    private UUID publishCourtListStatusId;

    @Column(name = "court_centre_id", nullable = false)
    private UUID courtCentreId;

    @Enumerated(STRING)
    @Column(name = "publish_status", nullable = false)
    private PublishStatus publishStatus;

    @Column(name = "last_updated", nullable = false)
    private ZonedDateTime lastUpdated;

    @Column(name = "court_list_file_name")
    private String courtListFileName;

    @Column(name = "error_message")
    private String errorMessage;

    public CourtListPublishStatus() {

    }

    public CourtListPublishStatus(final UUID publishCourtListStatusId,
                                  final UUID courtCentreId,
                                  final PublishStatus publishStatus,
                                  final ZonedDateTime lastUpdated
    ) {
        this.publishCourtListStatusId = publishCourtListStatusId;
        this.courtCentreId = courtCentreId;
        this.publishStatus = publishStatus;
        this.lastUpdated = lastUpdated;
    }

    public CourtListPublishStatus(final UUID publishCourtListStatusId,
                                  final UUID courtCentreId,
                                  final PublishStatus publishStatus,
                                  final ZonedDateTime lastUpdated,
                                  final String courtListFileName,
                                  final String errorMessage
    ) {
        this.publishCourtListStatusId = publishCourtListStatusId;
        this.courtCentreId = courtCentreId;
        this.courtListFileName = courtListFileName;
        this.publishStatus = publishStatus;
        this.lastUpdated = lastUpdated;
        this.errorMessage = errorMessage;

    }

    public UUID getPublishCourtListStatusId() {
        return publishCourtListStatusId;
    }

    public void setPublishCourtListStatusId(UUID publishCourtListStatusId) {
        this.publishCourtListStatusId = publishCourtListStatusId;
    }

    public UUID getCourtCentreId() {
        return courtCentreId;
    }

    public void setCourtCentreId(UUID courtCentreId) {
        this.courtCentreId = courtCentreId;
    }

    public PublishStatus getPublishStatus() {
        return publishStatus;
    }

    public void setPublishStatus(PublishStatus publishStatus) {
        this.publishStatus = publishStatus;
    }

    public ZonedDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(ZonedDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getCourtListFileName() {
        return courtListFileName;
    }

    public void setCourtListFileName(String courtListFileName) {
        this.courtListFileName = courtListFileName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.publishCourtListStatusId);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(this.publishCourtListStatusId, ((CourtListPublishStatus) o).publishCourtListStatusId);
    }
}
