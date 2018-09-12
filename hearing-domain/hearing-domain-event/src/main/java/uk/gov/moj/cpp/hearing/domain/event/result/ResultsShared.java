package uk.gov.moj.cpp.hearing.domain.event.result;

import static java.util.Optional.ofNullable;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.json.schemas.core.CourtClerk;
import uk.gov.justice.json.schemas.core.Hearing;
import uk.gov.justice.json.schemas.core.Target;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLine;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings({"squid:S2384", "pmd:BeanMembersShouldSerialize"})
@Event("hearing.results-shared")
public class ResultsShared implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;

    private ZonedDateTime sharedTime;

    private CourtClerk courtClerk;

    private Hearing hearing;

    private Map<UUID, ProsecutionCounselUpsert> prosecutionCounsels;

    private Map<UUID, DefenceCounselUpsert> defenceCounsels;

    private Map<UUID, VerdictUpsert> verdicts;

    private Map<UUID, Plea> pleas;

    private List<Variant> variantDirectory;

    private Map<UUID, CompletedResultLineStatus> completedResultLinesStatus;

    public ResultsShared() {
    }

    @JsonCreator
    private ResultsShared(@JsonProperty("hearingId") final UUID hearingId,
                          @JsonProperty("sharedTime") final ZonedDateTime sharedTime,
                          @JsonProperty("courtClerk") final CourtClerk courtClerk,
                          @JsonProperty("uncompletedResultLines") final List<UncompletedResultLine> uncompletedResultLines,
                          @JsonProperty("completedResultLines") final List<CompletedResultLine> completedResultLines,
                          @JsonProperty("hearing") final Hearing hearing,
                          @JsonProperty("prosecutionCounsels") final Map<UUID, ProsecutionCounselUpsert> prosecutionCounsels,
                          @JsonProperty("defenceCounsels") final Map<UUID, DefenceCounselUpsert> defenceCounsels,
                          @JsonProperty("pleas") final Map<UUID, Plea> pleas,
                          @JsonProperty("verdicts") final Map<UUID, VerdictUpsert> verdicts,
                          @JsonProperty("variantDirectory") final List<Variant> variantDirectory,
                          @JsonProperty("completedResultLinesStatus") final Map<UUID, CompletedResultLineStatus> completedResultLinesStatus) {
        this.hearingId = hearingId;
        this.sharedTime = sharedTime;
        this.courtClerk = courtClerk;
        this.hearing = hearing;
        this.prosecutionCounsels = ofNullable(prosecutionCounsels).orElseGet(HashMap::new);
        this.defenceCounsels = ofNullable(defenceCounsels).orElseGet(HashMap::new);
        this.pleas = ofNullable(pleas).orElseGet(HashMap::new);
        this.verdicts = ofNullable(verdicts).orElseGet(HashMap::new);
        this.variantDirectory = ofNullable(variantDirectory).orElseGet(ArrayList::new);
        this.completedResultLinesStatus = ofNullable(completedResultLinesStatus).orElseGet(HashMap::new);
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

    public List<Variant> getVariantDirectory() {
        return variantDirectory;
    }

    public Map<UUID, CompletedResultLineStatus> getCompletedResultLinesStatus() {
        return completedResultLinesStatus;
    }

    public static Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("pmd:BeanMembersShouldSerialize")
    public static final class Builder {

        private UUID hearingId;

        private ZonedDateTime sharedTime;

        private CourtClerk courtClerk;

        private List<UncompletedResultLine> uncompletedResultLines;

        private List<CompletedResultLine> completedResultLines;

        private Hearing hearing;

        private Map<UUID, ProsecutionCounselUpsert> prosecutionCounsels;

        private Map<UUID, DefenceCounselUpsert> defenceCounsels;

        private Map<UUID, VerdictUpsert> verdicts;

        private Map<UUID, Plea> pleas;

        private List<Variant> variantDirectory;

        private Map<UUID, CompletedResultLineStatus> completedResultLinesStatus;

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

        public Builder withCompletedResultLinesStatus(final Map<UUID, CompletedResultLineStatus> completedResultLinesStatus) {
            this.completedResultLinesStatus = completedResultLinesStatus;
            return this;
        }

        public Builder withPleas(final Map<UUID, Plea> pleas) {
            this.pleas = pleas;
            return this;
        }

        public Builder withVariantDirectory(final List<Variant> variantDirectory) {
            this.variantDirectory = new ArrayList<>(variantDirectory);
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
                    prosecutionCounsels,
                    defenceCounsels,
                    pleas,
                    verdicts,
                    variantDirectory,
                    completedResultLinesStatus);
        }
    }
}