package uk.gov.moj.cpp.hearing.command.result;

import uk.gov.justice.core.courts.DelegatedPowers;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.moj.cpp.hearing.domain.HearingState;

@SuppressWarnings("squid:S2384")
public final class ShareDaysResultsCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;

    private List<SharedResultsCommandResultLineV2> resultLines;

    private DelegatedPowers courtClerk;

    private HearingState newHearingState;

    private LocalDate hearingDay;

    public ShareDaysResultsCommand() {
    }

    @JsonCreator
    private ShareDaysResultsCommand(
            @JsonProperty("hearingId") final UUID hearingId,
            @JsonProperty("courtClerk") final DelegatedPowers courtClerk,
            @JsonProperty("resultLines") final List<SharedResultsCommandResultLineV2> resultLines,
            @JsonProperty("newHearingState") final HearingState newHearingState,
            @JsonProperty("hearingDay") final LocalDate hearingDay) {
        this.hearingId = hearingId;
        this.courtClerk = courtClerk;
        this.resultLines = resultLines;
        this.newHearingState = newHearingState;
        this.hearingDay = hearingDay;
    }

    public static ShareDaysResultsCommand shareResultsCommand() {
        return new ShareDaysResultsCommand();
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public ShareDaysResultsCommand setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public List<SharedResultsCommandResultLineV2> getResultLines() {
        return resultLines;
    }

    public ShareDaysResultsCommand setResultLines(final List<SharedResultsCommandResultLineV2> resultLines) {
        this.resultLines = resultLines;
        return this;
    }

    public DelegatedPowers getCourtClerk() {
        return courtClerk;
    }

    public ShareDaysResultsCommand setCourtClerk(final DelegatedPowers courtClerk) {
        this.courtClerk = courtClerk;
        return this;
    }

    public HearingState getNewHearingState() {
        return newHearingState;
    }

    public void setNewHearingState(final HearingState newHearingState) {
        this.newHearingState = newHearingState;
    }

    public LocalDate getHearingDay() {
        return hearingDay;
    }

    public void setHearingDay(final LocalDate hearingDay) {
        this.hearingDay = hearingDay;
    }
}