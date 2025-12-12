package uk.gov.moj.cpp.hearing.constants;

import static java.util.UUID.fromString;

import java.util.UUID;

public enum ApplicationOutcome {

    WITHDRAWN("f62dedad-685b-370f-899b-61e94084dab2"),
    GRANTED("c322f934-6b70-3fdd-b196-8628d5ee68db"),
    REFUSED("f48b2061-84b7-3429-8345-2ea4c3e88a3a");

    private UUID outcomeId;

    ApplicationOutcome(final String outcomeId) {
        this.outcomeId = fromString(outcomeId);
    }

    public UUID getOutcomeId() {
        return outcomeId;
    }
}
