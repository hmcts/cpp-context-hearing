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
    private final Integer actionSequence;
    private final String recordedLabel;
    private final String caseAttribute;
    private final String groupLabel;
    private final Integer groupSequence;
    private final boolean alterable;

    @JsonCreator
    public HearingEventDefinition(@JsonProperty("id") final UUID id,
                                  @JsonProperty("actionLabel") final String actionLabel,
                                  @JsonProperty("actionSequence") final Integer actionSequence,
                                  @JsonProperty("recordedLabel") final String recordedLabel,
                                  @JsonProperty("caseAttribute") final String caseAttribute,
                                  @JsonProperty("groupLabel") final String groupLabel,
                                  @JsonProperty("groupSequence") final Integer groupSequence,
                                  @JsonProperty("alterable") final boolean alterable) {
        this.id = id;
        this.actionLabel = actionLabel;
        this.actionSequence = actionSequence;
        this.recordedLabel = recordedLabel;
        this.caseAttribute = caseAttribute;
        this.groupLabel = groupLabel;
        this.groupSequence = groupSequence;
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

    public Integer getActionSequence() {
        return actionSequence;
    }

    public String getRecordedLabel() {
        return recordedLabel;
    }

    public String getCaseAttribute() {
        return caseAttribute;
    }

    public String getGroupLabel() {
        return groupLabel;
    }

    public Integer getGroupSequence() {
        return groupSequence;
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
                && java.util.Objects.equals(this.actionSequence, that.actionSequence)
                && java.util.Objects.equals(this.alterable, that.alterable)
                && java.util.Objects.equals(this.caseAttribute, that.caseAttribute)
                && java.util.Objects.equals(this.groupLabel, that.groupLabel)
                && java.util.Objects.equals(this.groupSequence, that.groupSequence)
                && java.util.Objects.equals(this.id, that.id)
                && java.util.Objects.equals(this.recordedLabel, that.recordedLabel);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(actionLabel, actionSequence, alterable, caseAttribute, groupLabel, id,
                recordedLabel, groupSequence);
    }

    @Override
    public String toString() {
        return "HearingEventDefinition {" + "actionLabel='" + actionLabel + "'," + "actionSequence='"
                + actionSequence + "'," + "alterable='" + alterable + "'," + "caseAttribute='" + caseAttribute
                + "'," + "groupLabel='" + groupLabel + "'," + "id='" + id + "'," + "recordedLabel='" + recordedLabel
                + "'," + "groupSequence='" + groupSequence + "'}";
    }

    public static class Builder {

        private UUID id;
        private String actionLabel;
        private Integer actionSequence;
        private String recordedLabel;
        private String caseAttribute;
        private String groupLabel;
        private Integer groupSequence;
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

        public Builder withActionSequence(final Integer actionSequence) {
            this.actionSequence = actionSequence;
            return this;
        }

        public Builder withGroupSequence(final Integer groupSequence) {
            this.groupSequence = groupSequence;
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


        public Builder withAlterable(final boolean alterable) {
            this.alterable = alterable;
            return this;
        }

        public HearingEventDefinition build() {
            return new HearingEventDefinition(id, actionLabel, actionSequence, recordedLabel, caseAttribute, groupLabel, groupSequence, alterable);
        }
    }
}