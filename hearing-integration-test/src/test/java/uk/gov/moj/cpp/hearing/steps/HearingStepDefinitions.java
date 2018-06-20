package uk.gov.moj.cpp.hearing.steps;

import uk.gov.moj.cpp.hearing.it.AbstractIT;

import java.util.UUID;

import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsAuthorisedUser;

@SuppressWarnings("unchecked")
public class HearingStepDefinitions extends AbstractIT {

    public static void givenAUserHasLoggedInAsACourtClerk(final UUID validUserId) {
        setupAsAuthorisedUser(validUserId);
        setLoggedInUser(validUserId);
    }
}
