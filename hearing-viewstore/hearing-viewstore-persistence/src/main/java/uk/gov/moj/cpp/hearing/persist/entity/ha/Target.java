package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "ha_target")
public class Target {

    @EmbeddedId
    private HearingSnapshotKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hearing_id", insertable = false, updatable = false)
    private Hearing hearing;

    @Column(name = "defendant_id")
    private UUID defendantId;

    @Column(name = "master_defendant_id")
    private UUID masterDefendantId;

    @Column(name = "hearing_day")
    private String hearingDay;

    @Column(name = "draft_result")
    private String draftResult;

    @Column(name = "offence_id")
    private UUID offenceId;

    @Column(name = "application_id")
    private UUID applicationId;

    @Column(name = "case_id")
    private UUID caseId;

    @Transient
    private Set<ResultLine> resultLines = new HashSet<>();

    @Column(name = "result_lines")
    private String resultLinesJson;

    @Column(name = "shadow_listed")
    private Boolean shadowListed;

    public Target() {
        //For JPA
    }

    public static Target target() {
        return new Target();
    }

    public HearingSnapshotKey getId() {
        return id;
    }

    public Target setId(HearingSnapshotKey id) {
        this.id = id;
        return this;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public Target setHearing(final Hearing hearing) {
        this.hearing = hearing;
        return this;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public Target setDefendantId(final UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public String getHearingDay() {
        return hearingDay;
    }

    public Target setHearingDay(final String hearingDay) {
        this.hearingDay = hearingDay;
        return this;
    }

    public String getDraftResult() {
        return draftResult;
    }

    public Target setDraftResult(final String draftResult) {
        this.draftResult = draftResult;
        return this;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public Target setOffenceId(final UUID offenceId) {
        this.offenceId = offenceId;
        return this;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public Target setApplicationId(final UUID applicationId) {
        this.applicationId = applicationId;
        return this;
    }

    @SuppressWarnings("squid:S2384")
    public Set<ResultLine> getResultLines() {
        return resultLines;
    }

    @SuppressWarnings("squid:S2384")
    public Target setResultLines(Set<ResultLine> resultLines) {

        this.resultLines = resultLines;
        return this;
    }

    public Boolean getShadowListed() {
        return shadowListed;
    }

    public void setShadowListed(final Boolean shadowListed) {
        this.shadowListed = shadowListed;
    }

    public UUID getMasterDefendantId() {
        return masterDefendantId;
    }

    public void setMasterDefendantId(final UUID masterDefendantId) {
        this.masterDefendantId = masterDefendantId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public void setCaseId(final UUID caseId) {
        this.caseId = caseId;
    }


    public String getResultLinesJson() {
        return resultLinesJson;
    }

    public void setResultLinesJson(final String resultLinesJson) {
        this.resultLinesJson = resultLinesJson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Target target = (Target) o;
        return Objects.equals(id, target.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id.hashCode());
    }

    public UUID getTargetId() {
        if(this.id != null ) {
            return this.id.getId();
        }
        return null;
    }
}
