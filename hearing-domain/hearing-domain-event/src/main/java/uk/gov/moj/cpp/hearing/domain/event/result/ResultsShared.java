package uk.gov.moj.cpp.hearing.domain.event.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.initiate.Case;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLine;
import uk.gov.moj.cpp.hearing.command.result.CourtClerk;
import uk.gov.moj.cpp.hearing.command.result.UncompletedResultLine;
import uk.gov.moj.cpp.hearing.domain.Plea;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@SuppressWarnings({"squid:S2384", "pmd:BeanMembersShouldSerialize"})
@Event("hearing.results-shared")
public final class ResultsShared implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID hearingId;

    private final ZonedDateTime sharedTime;

    private final CourtClerk courtClerk;

    private final List<UncompletedResultLine> uncompletedResultLines;

    private final List<CompletedResultLine> completedResultLines;

    private final Hearing hearing;

    private final List<Case> cases;

    private final Map<UUID, ProsecutionCounselUpsert> prosecutionCounsels;

    private final Map<UUID, DefenceCounselUpsert> defenceCounsels;

    private final Map<UUID, VerdictUpsert> verdicts;

    private final Map<UUID, Plea> pleas;

    @JsonCreator
    private ResultsShared(@JsonProperty("hearingId") final UUID hearingId,
                            @JsonProperty("sharedTime") final ZonedDateTime sharedTime,
                            @JsonProperty("courtClerk") final CourtClerk courtClerk,
                            @JsonProperty("uncompletedResultLines") final List<UncompletedResultLine> uncompletedResultLines,
                            @JsonProperty("completedResultLines") final List<CompletedResultLine> completedResultLines,
                            @JsonProperty("hearing") final Hearing hearing,
                            @JsonProperty("cases") final List<Case> cases,
                            @JsonProperty("prosecutionCounsels") final Map<UUID, ProsecutionCounselUpsert> prosecutionCounsels,
                            @JsonProperty("defenceCounsels") final Map<UUID, DefenceCounselUpsert> defenceCounsels,
                            @JsonProperty("pleas") final Map<UUID, Plea> pleas,
                            @JsonProperty("verdicts") final Map<UUID, VerdictUpsert> verdicts) {
        this.hearingId = hearingId;
        this.sharedTime = sharedTime;
        this.courtClerk = courtClerk;
        this.uncompletedResultLines = ofNullable(uncompletedResultLines).orElseGet(ArrayList::new);
        this.completedResultLines = ofNullable(completedResultLines).orElseGet(ArrayList::new);
        this.hearing = hearing;
        this.cases = ofNullable(cases).orElseGet(ArrayList::new);
        this.prosecutionCounsels = ofNullable(prosecutionCounsels).orElseGet(HashMap::new);
        this.defenceCounsels = ofNullable(defenceCounsels).orElseGet(HashMap::new);
        this.pleas = ofNullable(pleas).orElseGet(HashMap::new);
        this.verdicts = ofNullable(verdicts).orElseGet(HashMap::new);
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public ZonedDateTime getSharedTime() {
        return sharedTime;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public List<Case> getCases() {
        return new ArrayList<>(cases);
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

    public CourtClerk getCourtClerk() {
        return courtClerk;
    }

    public List<UncompletedResultLine> getUncompletedResultLines() {
        return uncompletedResultLines;
    }

    public List<CompletedResultLine> getCompletedResultLines() {
        return completedResultLines;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID hearingId;

        private ZonedDateTime sharedTime;

        private CourtClerk courtClerk;

        private List<UncompletedResultLine> uncompletedResultLines;

        private List<CompletedResultLine> completedResultLines;

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

        public Builder withCourtClerk(final CourtClerk courtClerk) {
            this.courtClerk = courtClerk;
            return this;
        }

        public Builder withUncompletedResultLines(final List<UncompletedResultLine> uncompletedResultLines) {
            this.uncompletedResultLines = uncompletedResultLines;
            return this;
        }

        public Builder withCompletedResultLines(final List<CompletedResultLine> completedResultLines) {
            this.completedResultLines = completedResultLines;
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
            return new ResultsShared(
                    hearingId,
                    sharedTime,
                    courtClerk,
                    uncompletedResultLines,
                    completedResultLines,
                    hearing,
                    cases,
                    prosecutionCounsels,
                    defenceCounsels,
                    pleas,
                    verdicts);
        }
    }
}