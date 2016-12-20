package uk.gov.moj.cpp.hearing.domain;


public class HearingEventDefinition {
    private String actionLabel;
    private String recordedLabel;

    public HearingEventDefinition(String actionLabel, String recordedLabel) {
        this.actionLabel = actionLabel;
        this.recordedLabel = recordedLabel;
    }

    public String getActionLabel() {
        return actionLabel;
    }

    public String getRecordedLabel() {
        return recordedLabel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HearingEventDefinition that = (HearingEventDefinition) o;

        if (actionLabel != null ? !actionLabel.equals(that.actionLabel) : that.actionLabel != null)
            return false;
        return recordedLabel != null ? recordedLabel.equals(that.recordedLabel) : that.recordedLabel == null;

    }

    @Override
    public int hashCode() {
        int result = actionLabel != null ? actionLabel.hashCode() : 0;
        result = 31 * result + (recordedLabel != null ? recordedLabel.hashCode() : 0);
        return result;
    }
}
