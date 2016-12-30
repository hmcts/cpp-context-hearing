package uk.gov.moj.cpp.hearing.domain;


public class HearingEventDefinition {
    private final String actionLabel;
    private final String recordedLabel;
    private final Integer sequence;

    public HearingEventDefinition(String actionLabel, String recordedLabel, Integer sequence) {
        this.actionLabel = actionLabel;
        this.recordedLabel = recordedLabel;
        this.sequence = sequence;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HearingEventDefinition that = (HearingEventDefinition) o;

        if (actionLabel != null ? !actionLabel.equals(that.actionLabel) : that.actionLabel != null)
            return false;
        if (recordedLabel != null ? !recordedLabel.equals(that.recordedLabel) : that.recordedLabel != null)
            return false;
        return sequence != null ? sequence.equals(that.sequence) : that.sequence == null;

    }

    @Override
    public int hashCode() {
        int result = actionLabel != null ? actionLabel.hashCode() : 0;
        result = 31 * result + (recordedLabel != null ? recordedLabel.hashCode() : 0);
        result = 31 * result + (sequence != null ? sequence.hashCode() : 0);
        return result;
    }
}
