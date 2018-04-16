package uk.gov.moj.cpp.hearing.command.initiate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;

import java.io.Serializable;

public class InitiateHearingCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Case> cases;
    private final Hearing hearing;

    @JsonCreator
    public InitiateHearingCommand(@JsonProperty("cases") List<Case> cases,
                                  @JsonProperty("hearing") final Hearing hearing) {
        this.cases = cases;
        this.hearing = hearing;
    }

    public List<Case> getCases() {
        return cases;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public static class Builder {

        private List<Case.Builder> cases = new ArrayList<>();

        private Hearing.Builder hearing;

        private Builder() {

        }

        public List<Case.Builder> getCases() {
            return cases;
        }

        public Hearing.Builder getHearing() {
            return hearing;
        }

        public Builder addCase(Case.Builder legalCase) {
            cases.add(legalCase);
            return this;
        }

        public Builder withHearing(Hearing.Builder hearing) {
            this.hearing = hearing;
            return this;
        }

        public InitiateHearingCommand build() {
            return new InitiateHearingCommand(
                    unmodifiableList(cases.stream().map(Case.Builder::build).collect(Collectors.toList())),
                    ofNullable(hearing).map(Hearing.Builder::build).orElse(null));
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder from(InitiateHearingCommand initiateHearingCommand) {
        Builder builder = builder()
                .withHearing(Hearing.from(initiateHearingCommand.getHearing()));

        initiateHearingCommand.getCases().forEach(legalCase -> builder.addCase(Case.from(legalCase)));

        return builder;
    }
}
