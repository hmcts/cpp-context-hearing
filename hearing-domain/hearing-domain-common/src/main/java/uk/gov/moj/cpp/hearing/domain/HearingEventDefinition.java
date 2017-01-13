package uk.gov.moj.cpp.hearing.domain;


import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HearingEventDefinition that = (HearingEventDefinition) o;
        return Objects.equals(getActionLabel(), that.getActionLabel()) &&
                Objects.equals(getRecordedLabel(), that.getRecordedLabel()) &&
                Objects.equals(getSequence(), that.getSequence()) &&
                Objects.equals(getCaseAttribute(), that.getCaseAttribute());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getActionLabel(), getRecordedLabel(), getSequence(), getCaseAttribute());
    }

    @Override
    public String toString() {
        return "HearingEventDefinition{" +
                "actionLabel='" + actionLabel + '\'' +
                ", recordedLabel='" + recordedLabel + '\'' +
                ", sequence=" + sequence +
                ", caseAttribute='" + caseAttribute + '\'' +
                '}';
    }
}
