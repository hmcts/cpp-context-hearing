package uk.gov.moj.cpp.hearing.command.result;

import uk.gov.justice.core.courts.CourtApplicationOutcomeType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@SuppressWarnings({"squid:S2384", "squid:S1067"})
public final class ApplicationDraftResultCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID targetId;
    private UUID hearingId;
    private UUID applicationId;
    private String draftResult;
    private CourtApplicationOutcomeType applicationOutcomeType;
    private LocalDate applicationOutcomeDate;

    public ApplicationDraftResultCommand() {
    }

    @JsonCreator
    public ApplicationDraftResultCommand(@JsonProperty("targetId") final UUID targetId,
                                         @JsonProperty("hearingId") final UUID hearingId,
                                         @JsonProperty("applicationId") final UUID applicationId,
                                         @JsonProperty("draftResult") final String draftResult,
                                         @JsonProperty("applicationOutcomeType") final CourtApplicationOutcomeType applicationOutcomeType,
                                         @JsonProperty("applicationOutcomeDate") final LocalDate applicationOutcomeDate
    ) {
        this.targetId = targetId;
        this.hearingId = hearingId;
        this.applicationId = applicationId;
        this.draftResult = draftResult;
        this.applicationOutcomeType = applicationOutcomeType;
        this.applicationOutcomeDate = applicationOutcomeDate;
    }

    public static ApplicationDraftResultCommand applicationDraftResultCommand() {
        return new ApplicationDraftResultCommand();
    }

    public UUID getTargetId() {
        return targetId;
    }

    public ApplicationDraftResultCommand setTargetId(UUID targetId) {
        this.targetId = targetId;
        return this;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public ApplicationDraftResultCommand setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public ApplicationDraftResultCommand setApplicationId(final UUID applicationId) {
        this.applicationId = applicationId;
        return this;
    }

    public String getDraftResult() {
        return draftResult;
    }

    public ApplicationDraftResultCommand setDraftResult(final String draftResult) {
        this.draftResult = draftResult;
        return this;
    }

    public CourtApplicationOutcomeType getApplicationOutcomeType() {
        return applicationOutcomeType;
    }

    public ApplicationDraftResultCommand setApplicationOutcomeType(final CourtApplicationOutcomeType applicationOutcomeType) {
        this.applicationOutcomeType = applicationOutcomeType;
        return this;
    }

    public LocalDate getApplicationOutcomeDate() {
        return applicationOutcomeDate;
    }

    public ApplicationDraftResultCommand setApplicationOutcomeDate(final LocalDate applicationOutcomeDate) {
        this.applicationOutcomeDate = applicationOutcomeDate;
        return this;
    }
}