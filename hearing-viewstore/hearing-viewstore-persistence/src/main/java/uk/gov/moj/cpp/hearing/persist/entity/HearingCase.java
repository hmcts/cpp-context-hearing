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
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "hearingid", nullable = false)
    private UUID hearingId;

    @Column(name = "caseid", nullable = false)
    private UUID caseId;

    public HearingCase() {
        // for JPA
    }

    public HearingCase(final UUID id, final UUID hearingId, final UUID caseId) {
        this.id = id;
        this.hearingId = hearingId;
        this.caseId = caseId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getCaseId() {
        return caseId;
    }

}
