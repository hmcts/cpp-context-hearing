package uk.gov.moj.cpp.hearing.persist.entity;


import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "hearing_case")
public class HearingCase {
    @Id
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;


    @Column(name = "hearingid",nullable = false)
    private UUID hearingId;

    @Column(name = "caseid",nullable = false)
    private UUID caseId;

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

    public UUID getCaseId() {
        return caseId;
    }

    public void setCaseId(UUID caseId) {
        this.caseId = caseId;
    }
}
