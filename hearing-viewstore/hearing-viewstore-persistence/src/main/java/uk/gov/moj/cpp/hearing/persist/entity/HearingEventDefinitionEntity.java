package uk.gov.moj.cpp.hearing.persist.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "hearing_event_definitions")
public class HearingEventDefinitionEntity implements Comparable<HearingEventDefinitionEntity> {

    @Id
    @Column(name = "recorded_label", nullable = false)
    private String recordedLabel;

    @Column(name = "action_label", nullable = false)
    private String actionLabel;

    @Column(nullable = false, name = "sequence_number")
    private Integer sequenceNumber;

    public HearingEventDefinitionEntity() {
        // for JPA
    }

    public HearingEventDefinitionEntity(final String recordedLabel, final String actionLabel, final Integer sequenceNumber) {
        this.recordedLabel = recordedLabel;
        this.actionLabel = actionLabel;
        this.sequenceNumber = sequenceNumber;
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

    @Override
    public int compareTo(HearingEventDefinitionEntity other) {
        return this.sequenceNumber.compareTo(other.sequenceNumber);
    }

    @Override
    public String toString() {
        return "HearingEventDefinitionEntity{" +
                "recordedLabel='" + recordedLabel + '\'' +
                ", actionLabel='" + actionLabel + '\'' +
                ", sequenceNumber=" + sequenceNumber +
                '}';
    }
}
