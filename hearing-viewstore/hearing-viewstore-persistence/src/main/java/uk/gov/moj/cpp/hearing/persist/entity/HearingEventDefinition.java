package uk.gov.moj.cpp.hearing.persist.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "hearing_event_definition")
public class HearingEventDefinition {

    @Id
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

    public HearingEventDefinition() {
        // for JPA
    }

    public HearingEventDefinition(final String recordedLabel, final String actionLabel, final Integer sequenceNumber,
                                  final String sequenceType, final String caseAttribute, final String groupLabel,
                                  final String actionLabelExtension) {
        this.recordedLabel = recordedLabel;
        this.actionLabel = actionLabel;
        this.sequenceNumber = sequenceNumber;
        this.sequenceType = sequenceType;
        this.caseAttribute = caseAttribute;
        this.groupLabel = groupLabel;
        this.actionLabelExtension = actionLabelExtension;
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
}
