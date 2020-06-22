package uk.gov.moj.cpp.hearing.steps;

import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsAuthorisedUser;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsAuthorisedUserByGivenGroup;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsSystemUser;

import uk.gov.moj.cpp.hearing.it.AbstractIT;

import java.util.UUID;

@SuppressWarnings("unchecked")
public class HearingStepDefinitions extends AbstractIT {

    public static void givenAUserHasLoggedInAsACourtClerk(final UUID validUserId) {
        setupAsAuthorisedUser(validUserId);
        setLoggedInUser(validUserId);
    }

    public static void givenAUserHasLoggedInAsADefenceCounsel(final UUID validUserId) {
        setupAsAuthorisedUser(validUserId);
        setLoggedInUser(validUserId);
    }

    public static void givenAUserHasLoggedInAsAProsecutionCounsel(final UUID validUserId) {
        setupAsAuthorisedUser(validUserId);
        setLoggedInUser(validUserId);
    }

    public static void givenAUserHasLoggedInAsASystemUser(final UUID validUserId) {
        setupAsSystemUser(validUserId);
        setLoggedInUser(validUserId);
    }


    public static void givenAUserHasLoggedInAsAGivenGroup(final UUID validUserId, final String group) {
        setupAsAuthorisedUserByGivenGroup(validUserId, group);
        setLoggedInUser(validUserId);
    }
}
