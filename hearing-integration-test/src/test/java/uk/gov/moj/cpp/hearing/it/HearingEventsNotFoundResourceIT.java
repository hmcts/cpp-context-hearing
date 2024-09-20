package uk.gov.moj.cpp.hearing.it;

import static java.util.UUID.randomUUID;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.waitForNotFoundResponse;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsAuthorisedUser;

import java.util.UUID;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
public class HearingEventsNotFoundResourceIT extends AbstractIT {

    private static final UUID userId = randomUUID();
    @BeforeAll
    public static void setupPerClass() {
        setupAsAuthorisedUser(userId);
    }

    @Test
    public void shouldReturn404ForNonExistingdEventDefinitionResource() {
        waitForNotFoundResponse(getURL("hearing.get-hearing-event-definition", randomUUID(), randomUUID()), "application/vnd.hearing.hearing-event-definition+json", userId.toString());
    }

    @Test
    public void shouldReturn404ForNonExistingNowsResource() {
        waitForNotFoundResponse(getURL("hearing.get.nows", randomUUID()), "application/vnd.hearing.get.nows+json", randomUUID().toString());
    }
}
