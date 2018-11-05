package uk.gov.moj.cpp.hearing.domain;


import java.io.Serializable;
import java.util.UUID;

@SuppressWarnings("squid:S00107")
public class HearingEventDefinition implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final String actionLabel;
    private final String recordedLabel;
    private final Integer sequence;
    private final String sequenceType;
    private final String caseAttribute;
    private final String groupLabel;
    private final String actionLabelExtension;
    private final boolean alterable;

    public HearingEventDefinition(final UUID id, final String actionLabel, final String recordedLabel, final Integer sequence,
                                  final String sequenceType, final String caseAttribute, final String groupLabel,
                                  final String actionLabelExtension, final boolean alterable) {
        this.id = id;
        this.actionLabel = actionLabel;
        this.recordedLabel = recordedLabel;
        this.sequence = sequence;
        this.sequenceType = sequenceType;
        this.caseAttribute = caseAttribute;
        this.groupLabel = groupLabel;
        this.actionLabelExtension = actionLabelExtension;
        this.alterable = alterable;
    }

    public UUID getId() {
        return id;
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

    public boolean isAlterable() {
        return alterable;
    }
}
