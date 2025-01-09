package uk.gov.moj.cpp.hearing.domain.event.result;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.domain.HearingState;
import uk.gov.moj.cpp.hearing.domain.ResultsError;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@SuppressWarnings({"squid:S2384", "squid:S1067"})
@Event("hearing.manage-results-failed")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ManageResultsFailed implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID hearingId;
    private final HearingState hearingState;
    private final ResultsError resultsError;
    private final LocalDate hearingDay;
    private final UUID lastUpdatedByUserId;
    private final Integer lastUpdatedVersion;
    private final UUID userId;
    private Integer version;

    @JsonCreator
    public ManageResultsFailed(final UUID hearingId, final HearingState hearingState, final ResultsError resultsError, final LocalDate hearingDay,
                               final Integer amendedResultVersion, final UUID amendedByUserId, final Integer version, final UUID userId) {
        this.hearingId = hearingId;
        this.hearingState = hearingState;
        this.resultsError = resultsError;
        this.hearingDay = hearingDay;
        this.lastUpdatedVersion = amendedResultVersion;
        this.lastUpdatedByUserId = amendedByUserId;
        this.version = version;
        this.userId = userId;
    }

    public ManageResultsFailed(final UUID hearingId, final HearingState hearingState, final ResultsError resultsError, final LocalDate hearingDay,
                               final Integer amendedResultVersion, final UUID amendedByUserId, final UUID userId) {
        this.hearingId = hearingId;
        this.hearingState = hearingState;
        this.resultsError = resultsError;
        this.hearingDay = hearingDay;
        this.lastUpdatedVersion = amendedResultVersion;
        this.lastUpdatedByUserId = amendedByUserId;
        this.userId = userId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public HearingState getHearingState() {
        return hearingState;
    }

    public ResultsError getResultsError() {
        return resultsError;
    }

    public LocalDate getHearingDay() {
        return hearingDay;
    }

    public Integer getLastUpdatedVersion() {
        return lastUpdatedVersion;
    }

    public UUID getLastUpdatedByUserId() {
        return lastUpdatedByUserId;
    }

    public Integer getVersion() {
        return version;
    }

    public UUID getUserId() {
        return userId;
    }
}