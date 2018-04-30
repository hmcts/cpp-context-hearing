package uk.gov.justice.progression.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.defendant.Address;
import uk.gov.moj.cpp.hearing.command.defendant.Defendant;
import uk.gov.moj.cpp.hearing.command.defendant.Interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@SuppressWarnings({"squid:S1188"})
@Event("public.progression.events.public-case-defendant-changed")
public class CaseDefendantDetails {

    private final UUID caseId;

    private final String caseUrn;

    private final List<Defendant> defendants;

    public CaseDefendantDetails(@JsonProperty("caseId") UUID caseId, @JsonProperty("caseUrn") String caseUrn, @JsonProperty("defendants") final List<Defendant> defendants) {
        this.caseId = caseId;
        this.caseUrn = caseUrn;
        this.defendants = new ArrayList<>(defendants);
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getCaseId() {
        return caseId;
    }

    public String getCaseUrn() {
        return caseUrn;
    }

    public List<Defendant> getDefendants() {
        return new ArrayList<>(defendants);
    }

    public static class Builder {

        private UUID caseId;

        private String caseUrn;

        private List<Defendant> defendants = new ArrayList<>();

        private Builder() {
        }

        public Builder withCaseId(UUID caseId) {
            this.caseId = caseId;
            return this;
        }

        public Builder withCaseUrn(String caseUrn) {
            this.caseUrn = caseUrn;
            return this;
        }

        public Builder addDefendant(Defendant.Builder defendantBuilder) {
            final Defendant defendant = ofNullable(defendantBuilder).map(Defendant.Builder::build).orElse(null);

            if (Objects.nonNull(defendant)) {
                defendants.add(defendant);
            }

            return this;
        }

        public Builder addDefendants(List<Defendant> defendants) {

            defendants.forEach(defendant -> {
                final Address address = defendant.getAddress();
                final Interpreter interpreter = defendant.getInterpreter();
                final uk.gov.moj.cpp.hearing.command.defendant.Defendant.Builder builder = Defendant.builder()
                        .withId(defendant.getId())
                        .withPersonId(defendant.getPersonId())
                        .withFirstName(defendant.getFirstName())
                        .withLastName(defendant.getLastName())
                        .withNationality(defendant.getNationality())
                        .withGender(defendant.getGender())
                        .withAddress(Address.address()
                                .withAddress1(address.getAddress1())
                                .withAddress2(address.getAddress2())
                                .withAddress3(address.getAddress3())
                                .withAddress4(address.getAddress4())
                                .withPostcode(address.getPostCode()))
                        .withDateOfBirth(defendant.getDateOfBirth())
                        .withBailStatus(defendant.getBailStatus())
                        .withCustodyTimeLimitDate(defendant.getCustodyTimeLimitDate())
                        .withDefenceOrganisation(defendant.getDefenceOrganisation())
                        .withInterpreter(Interpreter.interpreter()
                                .withLanguage(interpreter.getLanguage())
                                .withNeeded(interpreter.getNeeded()));
                addDefendant(builder);
            });

            return this;
        }

        public CaseDefendantDetails build() {
            return new CaseDefendantDetails(caseId, caseUrn, defendants);
        }
    }

}