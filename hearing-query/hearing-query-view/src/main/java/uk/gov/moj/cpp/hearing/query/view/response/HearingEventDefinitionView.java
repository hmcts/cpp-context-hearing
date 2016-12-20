package uk.gov.moj.cpp.hearing.query.view.response;

public class HearingEventDefinitionView {
    private String recordedLabel;

    private String actionLabel;

    public HearingEventDefinitionView() {
    }

    public String getRecordedLabel() {
        return recordedLabel;
    }

    public void setRecordedLabel(String recordedLabel) {
        this.recordedLabel = recordedLabel;
    }

    public String getActionLabel() {
        return actionLabel;
    }

    public void setActionLabel(String actionLabel) {
        this.actionLabel = actionLabel;
    }

}
