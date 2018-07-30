package uk.gov.moj.cpp.hearing.command.verdict;

import static java.util.Collections.unmodifiableList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HearingUpdateVerdictCommand implements Serializable {

    private static final long serialVersionUID = 1L;
    private final UUID hearingId;
    private final UUID caseId;
    private final List<Defendant> defendants;

    @JsonCreator
    public HearingUpdateVerdictCommand(@JsonProperty("hearingId") final UUID hearingId,
                                       @JsonProperty("caseId") final UUID caseId,
                                       @JsonProperty("defendants") final List<Defendant> defendants) {
        this.caseId = caseId;
        this.hearingId = hearingId;
        this.defendants = (null == defendants) ? new ArrayList<>() : new ArrayList<>(defendants);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public List<Defendant> getDefendants() {
        return defendants;
    }

    public static class Builder {

        private UUID caseId;
        private UUID hearingId;
        private List<Defendant.Builder> defendants = new ArrayList<>();

        private Builder() {
        }

        public UUID getCaseId() {
            return caseId;
        }

        public UUID getHearingId() {
            return hearingId;
        }

        public Builder withCaseId(UUID caseId) {
            this.caseId = caseId;
            return this;
        }

        public Builder withHearingId(UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder addDefendant(Defendant.Builder defendant) {
            this.defendants.add(defendant);
            return this;
        }

        public List<Defendant.Builder> getDefendants() {
            return defendants;
        }

        public HearingUpdateVerdictCommand build() {
            return new HearingUpdateVerdictCommand(hearingId, caseId,
                    unmodifiableList(defendants.stream().map(Defendant.Builder::build).collect(Collectors.toList()))
            );
        }
    }

    public static HearingUpdateVerdictCommand.Builder builder() {
        return new HearingUpdateVerdictCommand.Builder();
    }

    public static HearingUpdateVerdictCommand.Builder from(HearingUpdateVerdictCommand hearingUpdateVerdictCommand) {
        HearingUpdateVerdictCommand.Builder builder = builder()
                .withCaseId(hearingUpdateVerdictCommand.getCaseId())
                .withHearingId(hearingUpdateVerdictCommand.getHearingId());

        hearingUpdateVerdictCommand.getDefendants().forEach(defendant -> builder.addDefendant(Defendant.from(defendant)));
        return builder;

    }
}
