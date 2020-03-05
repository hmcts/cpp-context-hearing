package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.time.LocalDate;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class LaaApplnReference {

    @Column(name = "laa_application_reference")
    private String applicationReference;
    @Column(name = "laa_effective_end_date")
    private LocalDate effectiveEndDate;
    @Column(name = "laa_effective_start_date")
    private LocalDate effectiveStartDate;
    @Column(name = "laa_status_code")
    private String statusCode;
    @Column(name = "laa_status_date")
    private LocalDate statusDate;
    @Column(name = "laa_status_description")
    private String statusDescription;
    @Column(name = "laa_status_id")
    private UUID statusId;

    public String getApplicationReference() {
        return applicationReference;
    }

    public LocalDate getEffectiveEndDate() {
        return effectiveEndDate;
    }

    public LocalDate getEffectiveStartDate() {
        return effectiveStartDate;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public LocalDate getStatusDate() {
        return statusDate;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public UUID getStatusId() {
        return statusId;
    }

    public void setApplicationReference(final String applicationReference) {
        this.applicationReference = applicationReference;
    }

    public void setEffectiveEndDate(final LocalDate effectiveEndDate) {
        this.effectiveEndDate = effectiveEndDate;
    }

    public void setEffectiveStartDate(final LocalDate effectiveStartDate) {
        this.effectiveStartDate = effectiveStartDate;
    }

    public void setStatusCode(final String statusCode) {
        this.statusCode = statusCode;
    }

    public void setStatusDate(final LocalDate statusDate) {
        this.statusDate = statusDate;
    }

    public void setStatusDescription(final String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public void setStatusId(final UUID statusId) {
        this.statusId = statusId;
    }
}
