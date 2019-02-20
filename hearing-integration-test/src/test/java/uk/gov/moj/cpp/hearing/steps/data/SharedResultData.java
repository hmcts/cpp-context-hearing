package uk.gov.moj.cpp.hearing.steps.data;

import static java.util.UUID.randomUUID;

import uk.gov.justice.services.test.utils.core.random.RandomGenerator;

import java.util.UUID;

public class SharedResultData {

    private static final String[] RESULT_LEVELS = {"OFFENCE", "CASE", "DEFENDANT"};

    private final UUID id;
    private final UUID hearingId;
    private final UUID caseId;
    private final UUID personId;
    private final UUID offenceId;
    private final String level;

    private final String resultLabel = "Imprisonment";
    private final String promptValue = "1 year 6 months";
    private final String promptLabel = "Duration";


    public SharedResultData() {
        this.id = randomUUID();
        this.hearingId = randomUUID();
        this.caseId = randomUUID();
        this.personId = randomUUID();
        this.offenceId = randomUUID();

        this.level = RandomGenerator.values(RESULT_LEVELS).next();
    }

    public UUID getId() {
        return id;
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

    public UUID getHearingId() {
        return hearingId;
    }

    public String getPromptValue() {
        return promptValue;
    }

    public String getPromptLabel() {
        return promptLabel;
    }
}
