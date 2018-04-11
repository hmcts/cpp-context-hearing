package uk.gov.moj.cpp.hearing.query.view.response;

import java.util.UUID;

public class LegalCase {

    private UUID id;

    private String urn;

    public UUID getId() {
        return id;
    }

    public String getUrn() {
        return urn;
    }

    public LegalCase(Builder builder) {
        this.id = builder.id;
        this.urn = builder.urn;
    }

    public static class Builder {

        private UUID id;

        private String urn;


        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withUrn(String urn) {
            this.urn = urn;
            return this;
        }

        public LegalCase build() {
            return new LegalCase(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

}
