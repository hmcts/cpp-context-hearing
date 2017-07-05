package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.domain.ResultPrompt;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Event("hearing.result-amended")
public class ResultAmended {

    private final UUID id;
    private final UUID lastSharedResultId;
    private final ZonedDateTime sharedTime;
    private final UUID hearingId;
    private final UUID caseId;
    private final UUID personId;
    private final UUID offenceId;
    private final String level;
    private final String resultLabel;
    private final List<ResultPrompt> prompts;

    public ResultAmended(final UUID id, final UUID lastSharedResultId, final ZonedDateTime sharedTime,
                         final UUID hearingId, final UUID caseId, final UUID personId, final UUID offenceId,
                         final String level, final String resultLabel, final List<ResultPrompt> prompts) {
        this.id = id;
        this.lastSharedResultId = lastSharedResultId;
        this.sharedTime = sharedTime;
        this.hearingId = hearingId;
        this.caseId = caseId;
        this.personId = personId;
        this.offenceId = offenceId;
        this.level = level;
        this.resultLabel = resultLabel;
        this.prompts = prompts;
    }

    public UUID getId() {
        return id;
    }

    public UUID getLastSharedResultId() {
        return lastSharedResultId;
    }

    public ZonedDateTime getSharedTime() {
        return sharedTime;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getPersonId() {
        return personId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public String getLevel() {
        return level;
    }

    public String getResultLabel() {
        return resultLabel;
    }

    public List<ResultPrompt> getPrompts() {
        return prompts;
    }
}
