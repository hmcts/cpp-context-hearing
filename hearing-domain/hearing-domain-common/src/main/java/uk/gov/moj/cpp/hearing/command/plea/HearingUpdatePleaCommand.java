package uk.gov.moj.cpp.hearing.command.plea;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableList;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public final class HearingUpdatePleaCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID hearingId;
    private final UUID caseId;
    private final List<Defendant> defendants;

    //TODO - GPE-3032 CLEANUP - the update command here needs to change.
    // It should not carry the hearingId as the BE determines which hearing to apply the plea to. (which is always the latest)
    // On the other hand the FE should never allow a plea to be set on a hearing that is not the latest.
    // This is probably not much of a problem, since the FE will take the hearingId that it is currently looking at and specify.
    // But there is a very small chance that a new hearing will arrive while the page is being viewed.

    // I think the update plea should only contain offences.  Each offence is uniquely associated to a defendant, so the defendant info is redundant.

    @JsonCreator
    public HearingUpdatePleaCommand(@JsonProperty("hearingId") final UUID hearingId,
            @JsonProperty("caseId") final UUID caseId, @JsonProperty("defendants") final List<Defendant> defendants) {
        this.caseId = caseId;
        this.hearingId = hearingId;
        this.defendants = (null == defendants) ? new ArrayList<>() : new ArrayList<>(defendants);
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public List<Defendant> getDefendants() {
        return defendants;
    }

    public static final class Builder {

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

        public HearingUpdatePleaCommand build() {
            return new HearingUpdatePleaCommand(hearingId, caseId,
                    unmodifiableList(defendants.stream().map(Defendant.Builder::build).collect(Collectors.toList()))
            );
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder from(HearingUpdatePleaCommand hearingUpdatePleaCommand) {
        Builder builder = builder()
                .withCaseId(hearingUpdatePleaCommand.getCaseId())
                .withHearingId(hearingUpdatePleaCommand.getHearingId());

        hearingUpdatePleaCommand.getDefendants().forEach(defendant -> builder.addDefendant(Defendant.from(defendant)));
        return builder;
    }
}