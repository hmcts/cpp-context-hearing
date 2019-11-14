package uk.gov.moj.cpp.hearing.repository;


import static javax.persistence.EnumType.STRING;

import uk.gov.moj.cpp.hearing.publishing.events.PublishStatus;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Enumerated;

@Embeddable
public class CourtListPK implements Serializable {
    private static final long serialVersionUID = 8137449412662L;

    @Column(name = "court_centre_id", nullable = false)
    private UUID courtCentreId;

    @Enumerated(STRING)
    @Column(name = "publish_status", nullable = false)
    private PublishStatus publishStatus;

    public CourtListPK() {
    }

    public CourtListPK(final UUID courtCentreId, final PublishStatus publishStatus) {
        this.courtCentreId = courtCentreId;
        this.publishStatus = publishStatus;
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
}