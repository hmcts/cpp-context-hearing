package uk.gov.moj.cpp.hearing.domain.event.result;

import static java.util.Optional.ofNullable;

import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Target2;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.command.result.NewAmendmentResult;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings({"squid:S2384", "PMD.BeanMembersShouldSerialize"})
@Event("hearing.events.results-shared-v3")
public class ResultsSharedV3 implements Serializable {

    private static final long serialVersionUID = -7078961436932712740L;

    private UUID hearingId;

    private ZonedDateTime sharedTime;

    private DelegatedPowers courtClerk;

    private Hearing hearing;

    private List<Variant> variantDirectory;

    private Map<UUID, CompletedResultLineStatus> completedResultLinesStatus;

    private List<Target2> targets;

    private List<Target2> savedTargets;

    private List<UUID> defendantDetailsChanged;

    private Boolean isReshare;

    private List<NewAmendmentResult> newAmendmentResults;

    private LocalDate hearingDay;

    public ResultsSharedV3() {
    }

    @JsonCreator
    private ResultsSharedV3(@JsonProperty("hearingId") final UUID hearingId,
                            @JsonProperty("sharedTime") final ZonedDateTime sharedTime,
                            @JsonProperty("courtClerk") final DelegatedPowers courtClerk,
                            @JsonProperty("hearing") final Hearing hearing,
                            @JsonProperty("variantDirectory") final List<Variant> variantDirectory,
                            @JsonProperty("completedResultLinesStatus") final Map<UUID, CompletedResultLineStatus> completedResultLinesStatus,
                            @JsonProperty("targets") final List<Target2> targets,
                            @JsonProperty("savedTargets") final List<Target2> savedTargets,
                            @JsonProperty("defendantDetailsChanged") final List<UUID> defendantDetailsChanged,
                            @JsonProperty("isReshare") final Boolean isReshare,
                            @JsonProperty("newAmendmentResults") final List<NewAmendmentResult> newAmendmentResults,
                            @JsonProperty("hearingDay") final LocalDate hearingDay) {
        this.hearingId = hearingId;
        this.sharedTime = sharedTime;
        this.courtClerk = courtClerk;
        this.hearing = hearing;
        this.variantDirectory = ofNullable(variantDirectory).orElseGet(ArrayList::new);
        this.completedResultLinesStatus = ofNullable(completedResultLinesStatus).orElseGet(HashMap::new);
        this.targets = ofNullable(targets).orElseGet(ArrayList::new);
        this.savedTargets = ofNullable(savedTargets).orElseGet(ArrayList::new);
        this.defendantDetailsChanged = ofNullable(defendantDetailsChanged).orElseGet(ArrayList::new);
        this.isReshare = isReshare;
        this.newAmendmentResults = newAmendmentResults;
        this.hearingDay = hearingDay;
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

    public List<Target2> getSavedTargets() {
        return savedTargets;
    }

    public List<Target2> getTargets() {
        return targets;
    }

    public void setTargets(final List<Target2> targets) {
        this.targets = targets;
    }

    public List<UUID> getDefendantDetailsChanged() {
        return defendantDetailsChanged;
    }

    public void setDefendantDetailsChanged(final List<UUID> defendantDetailsChanged) {
        this.defendantDetailsChanged = defendantDetailsChanged;
    }

    public Boolean getIsReshare() {
        return isReshare;
    }

    public List<NewAmendmentResult> getNewAmendmentResults() {
        return newAmendmentResults;
    }

    public LocalDate getHearingDay() {
        return hearingDay;
    }

    @SuppressWarnings("PMD:BeanMembersShouldSerialize")
    public static final class Builder {

        private UUID hearingId;

        private ZonedDateTime sharedTime;

        private DelegatedPowers courtClerk;

        private Hearing hearing;

        private List<Variant> variantDirectory;

        private Map<UUID, CompletedResultLineStatus> completedResultLinesStatus;

        private List<Target2> targets;

        private List<Target2> savedTargets;

        private List<UUID> defendantDetailsChanged;

        private boolean isReshare;

        private List<NewAmendmentResult> newAmendmentResults;

        private LocalDate hearingDay;

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
            if (completedResultLinesStatus != null && !completedResultLinesStatus.isEmpty()) {
                this.completedResultLinesStatus = new HashMap<>(completedResultLinesStatus);
            }
            return this;
        }

        public Builder withVariantDirectory(final List<Variant> variantDirectory) {
            this.variantDirectory = new ArrayList<>(variantDirectory);
            return this;
        }

        public Builder withTargets(final List<Target2> targets) {
            this.targets = targets;
            return this;
        }

        public Builder withSavedTargets(final List<Target2> savedTargets) {
            this.savedTargets = savedTargets;
            return this;
        }

        public Builder withDefendantDetailsChanged(final List<UUID> defendantDetailsChanged) {
            this.defendantDetailsChanged = defendantDetailsChanged;
            return this;
        }

        public Builder withIsReshare(final Boolean isReshare) {
            this.isReshare = isReshare;
            return this;
        }

        public Builder withNewAmendmentResults(final List<NewAmendmentResult> newAmendmentResults) {
            this.newAmendmentResults = newAmendmentResults;
            return this;
        }

        public Builder withHearingDay(final LocalDate hearingDay) {
            this.hearingDay = hearingDay;
            return this;
        }

        public ResultsSharedV3 build() {
            return new ResultsSharedV3(
                    hearingId,
                    sharedTime,
                    courtClerk,
                    hearing,
                    variantDirectory,
                    completedResultLinesStatus,
                    targets,
                    savedTargets,
                    defendantDetailsChanged,
                    isReshare,
                    newAmendmentResults,
                    hearingDay);
        }
    }
}