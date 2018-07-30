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

    @Column(name = "sequence_number")
    private Integer sequenceNumber;

    @Column(name = "sequence_type")
    private String sequenceType;

    // todo need to normalize case attribute as it's composed of comma separated values which
    // would force to be parsed for processing
    @Column(name = "case_attribute")
    private String caseAttribute;

    @Column(name = "group_label")
    private String groupLabel;

    @Column(name = "action_label_extension")
    private String actionLabelExtension;

    @Column(name = "alterable", nullable = false)
    private boolean alterable;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    public HearingEventDefinition() {
        // for JPA
    }

    public HearingEventDefinition(final UUID id, final String recordedLabel, final String actionLabel, final Integer sequenceNumber,
                                  final String sequenceType, final String caseAttribute, final String groupLabel,
                                  final String actionLabelExtension, final boolean alterable) {
        this(id, recordedLabel, actionLabel, sequenceNumber, sequenceType, caseAttribute, groupLabel,
                actionLabelExtension, alterable, false);
    }

    private HearingEventDefinition(final UUID id, final String recordedLabel, final String actionLabel, final Integer sequenceNumber,
                                   final String sequenceType, final String caseAttribute, final String groupLabel,
                                   final String actionLabelExtension, final boolean alterable, final boolean deleted) {
        this.id = id;
        this.recordedLabel = recordedLabel;
        this.actionLabel = actionLabel;
        this.sequenceNumber = sequenceNumber;
        this.sequenceType = sequenceType;
        this.caseAttribute = caseAttribute;
        this.groupLabel = groupLabel;
        this.actionLabelExtension = actionLabelExtension;
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

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public String getSequenceType() {
        return sequenceType;
    }

    public String getCaseAttribute() {
        return caseAttribute;
    }

    public String getGroupLabel() {
        return groupLabel;
    }

    public String getActionLabelExtension() {
        return actionLabelExtension;
    }

    public boolean isAlterable() {
        return alterable;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Builder builder() {
        return new Builder(getId(), getRecordedLabel(), getActionLabel(), getSequenceNumber(), getSequenceType(),
                getCaseAttribute(), getGroupLabel(), getActionLabelExtension(), isAlterable(), isDeleted());
    }

    public class Builder {
        private UUID id;
        private String recordedLabel;
        private String actionLabel;
        private Integer sequenceNumber;
        private String sequenceType;
        private String caseAttribute;
        private String groupLabel;
        private String actionLabelExtension;
        private boolean alterable;
        private boolean deleted;

        Builder(final UUID id, final String recordedLabel, final String actionLabel, final Integer sequenceNumber,
                final String sequenceType, final String caseAttribute, final String groupLabel,
                final String actionLabelExtension, final boolean alterable, final boolean deleted) {
            this.id = id;
            this.recordedLabel = recordedLabel;
            this.actionLabel = actionLabel;
            this.sequenceNumber = sequenceNumber;
            this.sequenceType = sequenceType;
            this.caseAttribute = caseAttribute;
            this.groupLabel = groupLabel;
            this.actionLabelExtension = actionLabelExtension;
            this.alterable = alterable;
            this.deleted = deleted;
        }

        public Builder delete() {
            this.deleted = true;
            return this;
        }

        public HearingEventDefinition build() {
            return new HearingEventDefinition(id, recordedLabel, actionLabel, sequenceNumber, sequenceType, caseAttribute, groupLabel, actionLabelExtension, alterable, deleted);
        }
    }

}
