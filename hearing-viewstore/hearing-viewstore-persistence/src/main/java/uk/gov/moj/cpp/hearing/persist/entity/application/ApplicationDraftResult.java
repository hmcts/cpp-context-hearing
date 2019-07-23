package uk.gov.moj.cpp.hearing.persist.entity.application;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "application_draft_result")
public class ApplicationDraftResult {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "hearing_id")
    private Hearing hearing;

    @Column(name = "application_id")
    private UUID applicationId;

    @Column(name = "draft_result")
    private String draftResult;

    public ApplicationDraftResult() {
        //For JPA
    }

    public static ApplicationDraftResult applicationDraftResult() {
        return new ApplicationDraftResult();
    }

    public UUID getId() {
        return id;
    }

    public ApplicationDraftResult setId(UUID id) {
        this.id = id;
        return this;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public ApplicationDraftResult setHearing(final Hearing hearing) {
        this.hearing = hearing;
        return this;
    }

    public String getDraftResult() {
        return draftResult;
    }

    public ApplicationDraftResult setDraftResult(final String draftResult) {
        this.draftResult = draftResult;
        return this;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public ApplicationDraftResult setApplicationId(final UUID applicationId) {
        this.applicationId = applicationId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ApplicationDraftResult target = (ApplicationDraftResult) o;
        return Objects.equals(id, target.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}