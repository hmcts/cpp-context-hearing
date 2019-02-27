package uk.gov.moj.cpp.hearing.domain;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings({"squid:S1067"})
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

    @JsonCreator
    public HearingEventDefinition(@JsonProperty("id") final UUID id,
                                  @JsonProperty("actionLabel") final String actionLabel,
                                  @JsonProperty("recordedLabel") final String recordedLabel,
                                  @JsonProperty("sequence") final Integer sequence,
                                  @JsonProperty("sequenceType") final String sequenceType,
                                  @JsonProperty("caseAttribute") final String caseAttribute,
                                  @JsonProperty("groupLabel") final String groupLabel,
                                  @JsonProperty("actionLabelExtension") final String actionLabelExtension,
                                  @JsonProperty("alterable") final boolean alterable) {
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

    public static Builder builder() {
        return new Builder();
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

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final HearingEventDefinition that = (HearingEventDefinition) obj;

        return java.util.Objects.equals(this.actionLabel, that.actionLabel)
                && java.util.Objects.equals(this.actionLabelExtension, that.actionLabelExtension)
                && java.util.Objects.equals(this.alterable, that.alterable)
                && java.util.Objects.equals(this.caseAttribute, that.caseAttribute)
                && java.util.Objects.equals(this.groupLabel, that.groupLabel)
                && java.util.Objects.equals(this.id, that.id)
                && java.util.Objects.equals(this.recordedLabel, that.recordedLabel)
                && java.util.Objects.equals(this.sequence, that.sequence)
                && java.util.Objects.equals(this.sequenceType, that.sequenceType);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(actionLabel, actionLabelExtension, alterable, caseAttribute, groupLabel, id,
                recordedLabel, sequence, sequenceType);
    }

    @Override
    public String toString() {
        return "HearingEventDefinition {" + "actionLabel='" + actionLabel + "'," + "actionLabelExtension='"
                + actionLabelExtension + "'," + "alterable='" + alterable + "'," + "caseAttribute='" + caseAttribute
                + "'," + "groupLabel='" + groupLabel + "'," + "id='" + id + "'," + "recordedLabel='" + recordedLabel
                + "'," + "sequence='" + sequence + "'," + "sequenceType='" + sequenceType + "'" + "}";
    }

    public static class Builder {

        private UUID id;
        private String actionLabel;
        private String recordedLabel;
        private Integer sequence;
        private String sequenceType;
        private String caseAttribute;
        private String groupLabel;
        private String actionLabelExtension;
        private boolean alterable;

        public Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public Builder withActionLabel(final String actionLabel) {
            this.actionLabel = actionLabel;
            return this;
        }

        public Builder withRecordedLabel(final String recordedLabel) {
            this.recordedLabel = recordedLabel;
            return this;
        }

        public Builder withSequence(final Integer sequence) {
            this.sequence = sequence;
            return this;
        }

        public Builder withSequenceType(final String sequenceType) {
            this.sequenceType = sequenceType;
            return this;
        }

        public Builder withCaseAttribute(final String caseAttribute) {
            this.caseAttribute = caseAttribute;
            return this;
        }

        public Builder withGroupLabel(final String groupLabel) {
            this.groupLabel = groupLabel;
            return this;
        }

        public Builder withActionLabelExtension(final String actionLabelExtension) {
            this.actionLabelExtension = actionLabelExtension;
            return this;
        }

        public Builder withAlterable(final boolean alterable) {
            this.alterable = alterable;
            return this;
        }

        public HearingEventDefinition build() {
            return new HearingEventDefinition(id, actionLabel, recordedLabel, sequence, sequenceType, caseAttribute, groupLabel, actionLabelExtension, alterable);
        }
    }
}