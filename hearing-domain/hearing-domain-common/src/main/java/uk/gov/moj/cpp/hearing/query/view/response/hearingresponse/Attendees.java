package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public final class Attendees {

    private final List<ProsecutionCounsel> prosecutionCounsels;
    private final List<DefenceCounsel> defenceCounsels;

    @JsonCreator
    public Attendees(@JsonProperty("prosecutionCounsels") final List<ProsecutionCounsel> prosecutionCounsels,
                     @JsonProperty("defenceCounsels") final List<DefenceCounsel> defenceCounsels) {
        this.prosecutionCounsels = prosecutionCounsels;
        this.defenceCounsels = defenceCounsels;
    }

    private Attendees(final Builder builder) {
        this.prosecutionCounsels = builder.prosecutionCounsels;
        this.defenceCounsels = builder.defenceCounsels;
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<ProsecutionCounsel> getProsecutionCounsels() {
        return prosecutionCounsels;
    }

    public List<DefenceCounsel> getDefenceCounsels() {
        return defenceCounsels;
    }

    public static final class Builder {

        private List<ProsecutionCounsel> prosecutionCounsels;
        private List<DefenceCounsel> defenceCounsels;

        public Builder withProsecutionCounsels(final List<ProsecutionCounsel> prosecutionCounsels) {
            this.prosecutionCounsels = prosecutionCounsels;
            return this;
        }

        public Builder withDefenceCounsels(final List<DefenceCounsel> defenceCounsels) {
            this.defenceCounsels = defenceCounsels;
            return this;
        }

        public Attendees build() {
            return new Attendees(this);
        }
    }
}
