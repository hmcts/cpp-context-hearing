package uk.gov.moj.cpp.hearing.domain.event.result;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.ofNullable;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.initiate.Case;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.command.result.ResultLine;
import uk.gov.moj.cpp.hearing.domain.Plea;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;

@SuppressWarnings({"squid:S2384", "squid:S1067"})
@Event("hearing.results-shared")
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ResultsShared implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID hearingId;
    private final ZonedDateTime sharedTime;
    private final List<ResultLine> resultLines;
    private final Hearing hearing;
    private final List<Case> cases;
    private final Map<UUID, ProsecutionCounselUpsert> prosecutionCounsels;
    private final Map<UUID, DefenceCounselUpsert> defenceCounsels;
    private final Map<UUID, VerdictUpsert> verdicts;
    private final Map<UUID, Plea> pleas;

    @JsonCreator
    protected ResultsShared(@JsonProperty("hearingId") final UUID hearingId, 
            @JsonProperty("sharedTime") final ZonedDateTime sharedTime, 
            @JsonProperty("resultLines") final List<ResultLine> resultLines,
            @JsonProperty("hearing") final Hearing hearing,
            @JsonProperty("cases") final List<Case> cases,
            @JsonProperty("prosecutionCounsels") final Map<UUID, ProsecutionCounselUpsert> prosecutionCounsels,
            @JsonProperty("defenceCounsels") final Map<UUID, DefenceCounselUpsert> defenceCounsels,
            @JsonProperty("pleas") final Map<UUID, Plea> pleas,
            @JsonProperty("verdicts") final Map<UUID, VerdictUpsert> verdicts) {
        this.hearingId = hearingId;
        this.sharedTime = sharedTime;
        this.resultLines = unmodifiableList(ofNullable(resultLines).orElseGet(ArrayList::new));
        this.hearing = hearing;
        this.cases = unmodifiableList(ofNullable(cases).orElseGet(ArrayList::new));
        this.prosecutionCounsels = unmodifiableMap(ofNullable(prosecutionCounsels).orElseGet(HashMap::new));
        this.defenceCounsels = unmodifiableMap(ofNullable(defenceCounsels).orElseGet(HashMap::new));
        this.pleas = unmodifiableMap(ofNullable(pleas).orElseGet(HashMap::new));
        this.verdicts = unmodifiableMap(ofNullable(verdicts).orElseGet(HashMap::new));
    }

    @JsonIgnore
    private ResultsShared(final Builder builder) {
        this.hearingId = builder.hearingId;
        this.sharedTime = builder.sharedTime;
        this.resultLines = unmodifiableList(ofNullable(builder.resultLines).orElseGet(ArrayList::new));
        this.hearing = builder.hearing;
        this.cases = unmodifiableList(ofNullable(builder.cases).orElseGet(ArrayList::new));
        this.prosecutionCounsels = unmodifiableMap(ofNullable(builder.prosecutionCounsels).orElseGet(HashMap::new));
        this.defenceCounsels = unmodifiableMap(ofNullable(builder.defenceCounsels).orElseGet(HashMap::new));
        this.pleas = unmodifiableMap(ofNullable(builder.pleas).orElseGet(HashMap::new));
        this.verdicts = unmodifiableMap(ofNullable(builder.verdicts).orElseGet(HashMap::new));
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public ZonedDateTime getSharedTime() {
        return sharedTime;
    }

    public List<ResultLine> getResultLines() {
        return resultLines;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public List<Case> getCases() {
        return cases;
    }

    public Map<UUID, ProsecutionCounselUpsert> getProsecutionCounsels() {
        return prosecutionCounsels;
    }

    public Map<UUID, DefenceCounselUpsert> getDefenceCounsels() {
        return defenceCounsels;
    }

    public Map<UUID, VerdictUpsert> getVerdicts() {
        return verdicts;
    }

    public Map<UUID, Plea> getPleas() {
        return pleas;
    }

    public static Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("PMD.BeanMembersShouldSerialize")
    public static final class Builder {

        private UUID hearingId;
        private ZonedDateTime sharedTime;
        private List<ResultLine> resultLines;
        private Hearing hearing;
        private List<Case> cases;
        private Map<UUID, ProsecutionCounselUpsert> prosecutionCounsels;
        private Map<UUID, DefenceCounselUpsert> defenceCounsels;
        private Map<UUID, VerdictUpsert> verdicts;
        private Map<UUID, Plea> pleas;
        
        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withSharedTime(final ZonedDateTime sharedTime) {
            this.sharedTime = sharedTime;
            return this;
        }
        public Builder withResultLines(final List<ResultLine> resultLines) {
            this.resultLines = resultLines;
            return this;
        }

        public Builder withHearing(final Hearing hearing) {
            this.hearing = hearing;
            return this;
        }

        public Builder withCases(final List<Case> cases) {
            this.cases = cases;
            return this;
        }

        public Builder withProsecutionCounsels(final Map<UUID, ProsecutionCounselUpsert> prosecutionCounsels) {
            this.prosecutionCounsels = prosecutionCounsels;
            return this;
        }

        public Builder withDefenceCounsels(final Map<UUID, DefenceCounselUpsert> defenceCounsels) {
            this.defenceCounsels = defenceCounsels;
            return this;
        }

        public Builder withVerdicts(final Map<UUID, VerdictUpsert> verdicts) {
            this.verdicts = verdicts;
            return this;
        }

        public Builder withPleas(final Map<UUID, Plea> pleas) {
            this.pleas = pleas;
            return this;
        }

        public ResultsShared build() {
            return new ResultsShared(this);
        }
    }
}