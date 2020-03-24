package uk.gov.moj.cpp.external.domain.progression.prosecutioncases;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProsecutionCase implements Serializable {
    private static final long serialVersionUID = 6988430365532954752L;

    private List<LinkedApplicationsSummary> linkedApplicationsSummary;

    @JsonCreator
    public ProsecutionCase(@JsonProperty(value = "linkedApplicationsSummary") final List<LinkedApplicationsSummary> linkedApplicationsSummary) {
        this.linkedApplicationsSummary = linkedApplicationsSummary;
    }

    public List<LinkedApplicationsSummary> getLinkedApplicationsSummary() {
        return linkedApplicationsSummary;
    }

    public static ProsecutionCase.Builder prosecutionCase() {
        return new ProsecutionCase.Builder();
    }

    public static class Builder {
        private List<LinkedApplicationsSummary> linkedApplicationsSummary;

        public Builder withLinkedApplicationsSummary(final List<LinkedApplicationsSummary> linkedApplicationsSummary) {
            this.linkedApplicationsSummary = linkedApplicationsSummary;
            return this;
        }

        public ProsecutionCase build() {
            return new ProsecutionCase(linkedApplicationsSummary);
        }
    }
}
