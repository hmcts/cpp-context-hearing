package uk.gov.moj.cpp.hearing.persist.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "hearing_event_definitions")
public class HearingEventDefinitionEntity implements Comparable<HearingEventDefinitionEntity>{

    @Id
    @Column(name = "recorded_label", unique = true, nullable = false)
    private String recordedLabel;

    @Column(name = "action_label", nullable = false)
    private String actionLabel;

    @Column(nullable = false, name = "sequence_number")
    private Integer sequenceNumber;

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

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public int compareTo(HearingEventDefinitionEntity other) {
        return this.sequenceNumber.compareTo(other.sequenceNumber);
    }
}
