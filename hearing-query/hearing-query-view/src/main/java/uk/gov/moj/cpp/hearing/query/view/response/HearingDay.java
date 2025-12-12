package uk.gov.moj.cpp.hearing.query.view.response;

import java.util.UUID;

public class HearingDay {

    private UUID id;

    private String urn;

    public HearingDay(Builder builder) {
        this.id = builder.id;
        this.urn = builder.urn;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public String getUrn() {
        return urn;
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

        public HearingDay build() {
            return new HearingDay(this);
        }
    }

}
