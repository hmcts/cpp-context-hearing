package uk.gov.moj.cpp.hearing.domain.event.result;

import static java.util.Optional.ofNullable;

import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;

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

    private static final long serialVersionUID = 2L;

    private UUID hearingId;

    private ZonedDateTime sharedTime;

    private DelegatedPowers courtClerk;

    private Hearing hearing;

    private List<Variant> variantDirectory;

    private Map<UUID, CompletedResultLineStatus> completedResultLinesStatus;

    private List<Target> targets;

    private List<Target> savedTargets;

    public ResultsShared() {
    }

    @JsonCreator
    private ResultsShared(@JsonProperty("hearingId") final UUID hearingId,
                          @JsonProperty("sharedTime") final ZonedDateTime sharedTime,
                          @JsonProperty("courtClerk") final DelegatedPowers courtClerk,
                          @JsonProperty("hearing") final Hearing hearing,
                          @JsonProperty("variantDirectory") final List<Variant> variantDirectory,
                          @JsonProperty("completedResultLinesStatus") final Map<UUID, CompletedResultLineStatus> completedResultLinesStatus,
                          @JsonProperty("targets") final List<Target> targets,
                          @JsonProperty("savedTargets") final List<Target> savedTargets) {
        this.hearingId = hearingId;
        this.sharedTime = sharedTime;
        this.courtClerk = courtClerk;
        this.hearing = hearing;
        this.variantDirectory = ofNullable(variantDirectory).orElseGet(ArrayList::new);
        this.completedResultLinesStatus = ofNullable(completedResultLinesStatus).orElseGet(HashMap::new);
        this.targets = ofNullable(targets).orElseGet(ArrayList::new);
        this.savedTargets = ofNullable(savedTargets).orElseGet(ArrayList::new);
    }

    public static Builder builder() {
        return new Builder();
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

    public DelegatedPowers getCourtClerk() {
        return courtClerk;
    }

    public List<Variant> getVariantDirectory() {
        return variantDirectory;
    }

    public void setVariantDirectory(List<Variant> variantDirectory) {
        this.variantDirectory = variantDirectory;
    }

    public Map<UUID, CompletedResultLineStatus> getCompletedResultLinesStatus() {
        return completedResultLinesStatus;
    }

    public List<Target> getSavedTargets() {
        return savedTargets;
    }

    public List<Target> getTargets() {
        return targets;
    }

    public void setTargets(final List<Target> targets) {
        this.targets = targets;
    }

    @SuppressWarnings("pmd:BeanMembersShouldSerialize")
    public static final class Builder {

        private UUID hearingId;

        private ZonedDateTime sharedTime;

        private DelegatedPowers courtClerk;

        private Hearing hearing;

        private List<Variant> variantDirectory;

        private Map<UUID, CompletedResultLineStatus> completedResultLinesStatus;

        private List<Target> targets;

        private List<Target> savedTargets;

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withSharedTime(final ZonedDateTime sharedTime) {
            this.sharedTime = sharedTime;
            return this;
        }

        public Builder withCourtClerk(final DelegatedPowers courtClerk) {
            this.courtClerk = courtClerk;
            return this;
        }

        public Builder withHearing(final Hearing hearing) {
            this.hearing = hearing;
            return this;
        }

        public Builder withCompletedResultLinesStatus(final Map<UUID, CompletedResultLineStatus> completedResultLinesStatus) {
            this.completedResultLinesStatus = new HashMap<>(completedResultLinesStatus);
            return this;
        }

        public Builder withVariantDirectory(final List<Variant> variantDirectory) {
            this.variantDirectory = new ArrayList<>(variantDirectory);
            return this;
        }

        public Builder withTargets(final List<Target> targets) {
            this.targets = targets;
            return this;
        }

        public Builder withSavedTargets(final List<Target> savedTargets) {
            this.savedTargets = savedTargets;
            return this;
        }

        public ResultsShared build() {
            return new ResultsShared(
                    hearingId,
                    sharedTime,
                    courtClerk,
                    hearing,
                    variantDirectory,
                    completedResultLinesStatus,
                    targets,
                    savedTargets);
        }
    }
}