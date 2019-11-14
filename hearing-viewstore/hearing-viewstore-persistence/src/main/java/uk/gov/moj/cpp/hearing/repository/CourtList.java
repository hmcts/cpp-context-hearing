package uk.gov.moj.cpp.hearing.repository;


import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "courtlist")
public class CourtList implements Serializable {

    private static final long serialVersionUID = 8137443412665L;

    @EmbeddedId
    @Column(name = "shared_court_list_primary_key", unique = true, nullable = false)
    private CourtListPK courtListPK;

    @Column(name = "court_list_file_id")
    private UUID courtListFileId;

    @Column(name = "court_list_file_name")
    private String courtListFileName;

    @Column(name = "last_updated", nullable = false)
    private ZonedDateTime lastUpdated;

    @Column(name = "error_message")
    private String errorMessage;

    public CourtList() {

    }

    public CourtList(final CourtListPK courtListPK,
                     final UUID courtListFileId,
                     final String courtListFileName,
                     final ZonedDateTime lastUpdated
    ) {
        this.courtListPK = courtListPK;
        this.courtListFileId = courtListFileId;
        this.courtListFileName = courtListFileName;
        this.lastUpdated = lastUpdated;
    }

    public CourtList(final CourtListPK courtListPK,
                     final ZonedDateTime createdTime) {
        this.courtListPK = courtListPK;
        this.lastUpdated = createdTime;
    }

    public CourtListPK getCourtListPK() {
        return courtListPK;
    }

    public void setCourtListPK(CourtListPK courtListPK) {
        this.courtListPK = courtListPK;
    }

    public UUID getCourtListFileId() {
        return courtListFileId;
    }

    public void setCourtListFileId(UUID courtListFileId) {
        this.courtListFileId = courtListFileId;
    }

    public String getCourtListFileName() {
        return courtListFileName;
    }

    public void setCourtListFileName(String courtListFileName) {
        this.courtListFileName = courtListFileName;
    }

    public ZonedDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(ZonedDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
