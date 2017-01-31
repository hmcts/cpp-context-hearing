package uk.gov.moj.cpp.hearing.domain;


public class HearingEventDefinition {

    private final String actionLabel;
    private final String recordedLabel;
    private final Integer sequence;
    private final String caseAttribute;

    public HearingEventDefinition(final String actionLabel, final String recordedLabel, final Integer sequence,
                                  final String caseAttribute) {
        this.actionLabel = actionLabel;
        this.recordedLabel = recordedLabel;
        this.sequence = sequence;
        this.caseAttribute = caseAttribute;
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

    public String getCaseAttribute() {
        return caseAttribute;
    }

}
