package uk.gov.moj.cpp.hearing.persist.entity.heda;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@SuppressWarnings("squid:S00107")
@Entity
@Table(name = "heda_hearing_event_definition")
public class HearingEventDefinition {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "recorded_label", nullable = false)
    private String recordedLabel;

    @Column(name = "action_label", nullable = false)
    private String actionLabel;

    @Column(name = "action_sequence")
    private Integer actionSequence;

    @Column(name = "group_sequence")
    private Integer groupSequence;

    // todo need to normalize case attribute as it's composed of comma separated values which
    // would force to be parsed for processing
    @Column(name = "case_attribute")
    private String caseAttribute;

    @Column(name = "group_label")
    private String groupLabel;

    @Column(name = "alterable", nullable = false)
    private boolean alterable;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    public HearingEventDefinition() {
        // for JPA
    }

    public HearingEventDefinition(final UUID id, final String recordedLabel, final String actionLabel, final Integer actionSequence,
                                  final String caseAttribute, final String groupLabel, final Integer groupSequence,
                                  final boolean alterable) {
        this(id, recordedLabel, actionLabel, actionSequence, caseAttribute, groupLabel,
                groupSequence, alterable, false);
    }

    private HearingEventDefinition(final UUID id, final String recordedLabel, final String actionLabel, final Integer actionSequence,
                                   final String caseAttribute, final String groupLabel, final Integer groupSequence,
                                   final boolean alterable, final boolean deleted) {
        this.id = id;
        this.recordedLabel = recordedLabel;
        this.actionLabel = actionLabel;
        this.actionSequence = actionSequence;
        this.caseAttribute = caseAttribute;
        this.groupLabel = groupLabel;
        this.groupSequence = groupSequence;
        this.alterable = alterable;
        this.deleted = deleted;
    }

    public UUID getId() {
        return id;
    }

    public String getRecordedLabel() {
        return recordedLabel;
    }

    public String getActionLabel() {
        return actionLabel;
    }

    public Integer getActionSequence() {
        return actionSequence;
    }

    public String getCaseAttribute() {
        return caseAttribute;
    }

    public String getGroupLabel() {
        return groupLabel;
    }

    public Integer getGroupSequence() {
        return groupSequence;
    }

    public boolean isAlterable() {
        return alterable;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public void setRecordedLabel(final String recordedLabel) {
        this.recordedLabel = recordedLabel;
    }

    public void setActionLabel(final String actionLabel) {
        this.actionLabel = actionLabel;
    }

    public void setActionSequence(final Integer actionSequence) {
        this.actionSequence = actionSequence;
    }

    public void setGroupSequence(final Integer groupSequence) {
        this.groupSequence = groupSequence;
    }

    public void setCaseAttribute(final String caseAttribute) {
        this.caseAttribute = caseAttribute;
    }

    public void setGroupLabel(final String groupLabel) {
        this.groupLabel = groupLabel;
    }

    public void setAlterable(final boolean alterable) {
        this.alterable = alterable;
    }

    public void setDeleted(final boolean deleted) {
        this.deleted = deleted;
    }

    public Builder builder() {
        return new Builder(getId(), getRecordedLabel(), getActionLabel(), getActionSequence(),
                getCaseAttribute(), getGroupLabel(), getGroupSequence(), isAlterable(), isDeleted());
    }

    public class Builder {
        private UUID id;
        private String recordedLabel;
        private String actionLabel;
        private Integer actionSequence;
        private String caseAttribute;
        private String groupLabel;
        private Integer groupSequence;
        private boolean alterable;
        private boolean deleted;

        Builder(final UUID id, final String recordedLabel, final String actionLabel, final Integer actionSequence,
                final String caseAttribute, final String groupLabel, final Integer groupSequence,
                final boolean alterable, final boolean deleted) {
            this.id = id;
            this.recordedLabel = recordedLabel;
            this.actionLabel = actionLabel;
            this.actionSequence = actionSequence;
            this.caseAttribute = caseAttribute;
            this.groupLabel = groupLabel;
            this.groupSequence = groupSequence;
            this.alterable = alterable;
            this.deleted = deleted;
        }

        public Builder delete() {
            this.deleted = true;
            return this;
        }

        public HearingEventDefinition build() {
            return new HearingEventDefinition(id, recordedLabel, actionLabel, actionSequence, caseAttribute, groupLabel, groupSequence, alterable, deleted);
        }
    }
}
