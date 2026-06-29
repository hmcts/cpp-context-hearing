package uk.gov.moj.cpp.hearing.command.handler.service.validation;

public class DefendantDto {
    private final String defendantId;
    private final String firstName;
    private final String lastName;
    private final String masterDefendantId;

    private DefendantDto(Builder builder) {
        this.defendantId = builder.defendantId;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.masterDefendantId = builder.masterDefendantId;
    }

    public String getDefendantId() {
        return defendantId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getMasterDefendantId() {
        return masterDefendantId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String defendantId;
        private String firstName;
        private String lastName;
        private String masterDefendantId;

        public Builder withDefendantId(String defendantId) {
            this.defendantId = defendantId;
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

        public Builder withMasterDefendantId(String masterDefendantId) {
            this.masterDefendantId = masterDefendantId;
            return this;
        }

        public DefendantDto build() {
            return new DefendantDto(this);
        }
    }
}
