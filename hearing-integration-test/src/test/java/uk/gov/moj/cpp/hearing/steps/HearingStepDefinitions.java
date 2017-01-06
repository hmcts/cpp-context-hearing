package uk.gov.moj.cpp.hearing.steps;

import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsAuthorisedUser;

import java.util.UUID;

public class HearingStepDefinitions {

    public static void givenAUserHasLoggedInAsACourtClerk(final UUID validUserId) {
        setupAsAuthorisedUser(validUserId.toString());
    }

}
