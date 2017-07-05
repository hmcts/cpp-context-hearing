package uk.gov.moj.cpp.hearing.domain;


public class HearingEventDefinition {

    private final String actionLabel;
    private final String recordedLabel;
    private final Integer sequence;
    private final String sequenceType;
    private final String caseAttribute;
    private final String groupLabel;
    private final String actionLabelExtension;

    public HearingEventDefinition(final String actionLabel, final String recordedLabel, final Integer sequence,
                                  final String sequenceType, final String caseAttribute, final String groupLabel,
                                  final String actionLabelExtension) {
        this.actionLabel = actionLabel;
        this.recordedLabel = recordedLabel;
        this.sequence = sequence;
        this.sequenceType = sequenceType;
        this.caseAttribute = caseAttribute;
        this.groupLabel = groupLabel;
        this.actionLabelExtension = actionLabelExtension;
    }

    public String getActionLabel() {
        return actionLabel;
    }

    public String getRecordedLabel() {
        return recordedLabel;
    }

    public Integer getSequence() {
        return sequence;
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
