package uk.gov.moj.cpp.hearing.persist.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "hearing_event_definitions")
public class HearingEventDefinitions {

    @Id
    @Column(name = "recorded_label", nullable = false)
    private String recordedLabel;

    @Column(name = "action_label", nullable = false)
    private String actionLabel;

    @Column(nullable = false, name = "sequence_number")
    private Integer sequenceNumber;

    @Column(name = "case_attribute")
    private String caseAttribute;

    public HearingEventDefinitions() {
        // for JPA //NOSONAR
    }

    public HearingEventDefinitions(final String recordedLabel, final String actionLabel, final Integer sequenceNumber,
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

}
