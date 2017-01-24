package uk.gov.moj.cpp.hearing.persist.entity;

import java.util.Objects;
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

    @Column(name = "case_attribute")
    private String caseAttribute;

    public HearingEventDefinitionEntity() {
        // for JPA
    }

    public HearingEventDefinitionEntity(final String recordedLabel, final String actionLabel, final Integer sequenceNumber,
                                        final String caseAttribute) {
        this.recordedLabel = recordedLabel;
        this.actionLabel = actionLabel;
        this.sequenceNumber = sequenceNumber;
        this.caseAttribute = caseAttribute;
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

    public String getCaseAttribute() {
        return caseAttribute;
    }

    @Override
    public int compareTo(final HearingEventDefinitionEntity that) {
        return this.sequenceNumber.compareTo(that.sequenceNumber);
    }

    @Override
    public String toString() {
        return "HearingEventDefinitionEntity{" +
                "recordedLabel='" + recordedLabel + '\'' +
                ", actionLabel='" + actionLabel + '\'' +
                ", sequenceNumber=" + sequenceNumber +
                ", caseAttribute='" + caseAttribute + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HearingEventDefinitionEntity that = (HearingEventDefinitionEntity) o;
        return Objects.equals(recordedLabel, that.recordedLabel) &&
                Objects.equals(actionLabel, that.actionLabel) &&
                Objects.equals(sequenceNumber, that.sequenceNumber) &&
                Objects.equals(caseAttribute, that.caseAttribute);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recordedLabel, actionLabel, sequenceNumber, caseAttribute);
    }
}
