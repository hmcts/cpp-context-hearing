package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("defendant")
public class HearingListResponseDefendant {

    private UUID id;

    private String name;

    @JsonCreator
    public HearingListResponseDefendant(@JsonProperty(value = "id") final UUID id,
                                        @JsonProperty(value = "name") final String name) {
        this.id = id;
        this.name = name;
    }

    private HearingListResponseDefendant(final Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static final class Builder {

        private UUID id;

        private String name;

        public Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public Builder withName(final String name) {
            this.name = name;
            return this;
        }

        public HearingListResponseDefendant build() {
            return new HearingListResponseDefendant(this);
        }

    }

}