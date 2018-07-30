package uk.gov.moj.cpp.hearing.command.witness;

import uk.gov.moj.cpp.hearing.command.DefendantId;

import java.util.List;

public class DefenceWitness {
    private String id;
    private String type;
    private String classification;
    private String title;
    private String firstName;
    private String lastName;
    private List<DefendantId> defendants;

    public DefenceWitness(Builder builder) {
        this.id = builder.id;
        this.type = builder.type;
        this.classification = builder.classification;
        this.title = builder.title;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.defendants = builder.defendants;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getType() {
        return type;
    }

    public String getClassification() {
        return classification;
    }

    public String getTitle() {
        return title;
    }

    public List<DefendantId> getDefendants() {
        return defendants;
    }

    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {
        private String id;
        private String type;
        private String classification;
        private String title;
        private String firstName;
        private String lastName;
        private List<DefendantId> defendants;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder withtType(String type) {
            this.type = type;
            return this;
        }

        public Builder withClassification(String classification) {
            this.classification = classification;
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withDefendants(List<DefendantId> defendants){
            this.defendants = defendants;
            return this;
        }

        public DefenceWitness build() {
            return new DefenceWitness(this);

        }

    }
}
