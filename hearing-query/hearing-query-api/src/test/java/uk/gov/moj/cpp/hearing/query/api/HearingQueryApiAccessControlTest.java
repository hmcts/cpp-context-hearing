package uk.gov.moj.cpp.hearing.query.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import uk.gov.moj.cpp.accesscontrol.common.providers.UserAndGroupProvider;
import uk.gov.moj.cpp.accesscontrol.drools.Action;
import uk.gov.moj.cpp.accesscontrol.test.utils.BaseDroolsAccessControlTest;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.kie.api.runtime.ExecutionResults;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

public class HearingQueryApiAccessControlTest extends BaseDroolsAccessControlTest {

    private static final String ACTION_NAME_GET_HEARING = "hearing.get.hearing";
    private static final String ACTION_NAME_GET_HEARINGS_BY_START_DATE = "hearing.get.hearings-by-startdate";
    private static final String ACTION_NAME_GET_HEARINGS_BY_CASE_ID = "hearing.get.hearings-by-caseid";
    private static final String ACTION_NAME_GET_PROSECUTION_COUNSELS = "hearing.get.prosecution-counsels";
    private static final String ACTION_NAME_GET_DEFENCE_COUNSELS = "hearing.get.defence-counsels";
    private static final String ACTION_NAME_GET_DRAFT_RESULT = "hearing.get-draft-result";
    private static final String ACTION_NAME_GET_HEARING_EVENT_LOG = "hearing.get-hearing-event-log";
    private static final String ACTION_NAME_GET_HEARING_EVENT_DEFINITIONS = "hearing.get-hearing-event-definitions";

    @Mock
    private UserAndGroupProvider userAndGroupProvider;

    @Captor
    private ArgumentCaptor<String[]> arrayCaptor;

    @Test
    public void shouldAllowUserInAuthorisedGroupToGetHearing() {
        assertSuccessfulOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_HEARING, "Listing Officers", "Court Clerks", "System Users");
    }

    @Test
    public void shouldNotAllowUserInUnauthorisedGroupToGetHearing() {
        assertFailureOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_HEARING, "Listing Officers", "Court Clerks", "System Users");
    }

    @Test
    public void shouldAllowUserInAuthorisedGroupToGetHearingsByStartDate() {
        assertSuccessfulOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_HEARINGS_BY_START_DATE, "Listing Officers", "Court Clerks");
    }

    @Test
    public void shouldNotAllowUserInUnauthorisedGroupToGetHearingsByStartDate() {
        assertFailureOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_HEARINGS_BY_START_DATE, "Listing Officers", "Court Clerks");
    }

    @Test
    public void shouldAllowUserInAuthorisedGroupToGetHearingsByCaseId() {
        assertSuccessfulOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_HEARINGS_BY_CASE_ID, "Listing Officers", "Court Clerks");
    }

    @Test
    public void shouldNotAllowUserInUnauthorisedGroupToGetHearingsByCaseId() {
        assertFailureOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_HEARINGS_BY_CASE_ID, "Listing Officers", "Court Clerks");
    }

    @Test
    public void shouldAllowUserInAuthorisedGroupToGetProsecutionCounsels() {
        assertSuccessfulOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_PROSECUTION_COUNSELS, "Listing Officers", "Court Clerks", "System Users");
    }

    @Test
    public void shouldNotAllowUserInUnauthorisedGroupToGetProsecutionCounsels() {
        assertFailureOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_PROSECUTION_COUNSELS, "Listing Officers", "Court Clerks", "System Users");
    }

    @Test
    public void shouldAllowUserInAuthorisedGroupToGetDefenceCounsels() {
        assertSuccessfulOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_DEFENCE_COUNSELS, "Listing Officers", "Court Clerks", "System Users");
    }

    @Test
    public void shouldNotAllowUserInUnauthorisedGroupToGetDefenceCounsels() {
        assertFailureOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_DEFENCE_COUNSELS, "Listing Officers", "Court Clerks", "System Users");
    }

    @Test
    public void shouldAllowUserInAuthorisedGroupToGetDraftResult() {
        assertSuccessfulOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_DRAFT_RESULT, "Court Clerks");
    }

    @Test
    public void shouldNotAllowUserInUnauthorisedGroupToGetDraftResult() {
        assertFailureOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_DRAFT_RESULT, "Court Clerks");
    }

    @Test
    public void shouldAllowUserInAuthorisedGroupToGetHearingEventLog() {
        assertSuccessfulOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_HEARING_EVENT_LOG, "Listing Officers", "Court Clerks");
    }

    @Test
    public void shouldNotAllowUserInUnauthorisedGroupToGetHearingEventLog() {
        assertFailureOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_HEARING_EVENT_LOG, "Listing Officers", "Court Clerks");
    }

    @Test
    public void shouldAllowUserInAuthorisedGroupToGetHearingEventDefinitions() {
        assertSuccessfulOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_HEARING_EVENT_DEFINITIONS, "Listing Officers", "Court Clerks");
    }

    @Test
    public void shouldNotAllowUserInUnauthorisedGroupToGetHearingEventDefinitions() {
        assertFailureOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_HEARING_EVENT_DEFINITIONS, "Listing Officers", "Court Clerks");
    }

    @Test
    public void shouldAllowAuthorisedUserToGetHearingEventDefinitions() {
        final Action action = createActionFor(ACTION_NAME_GET_HEARING_EVENT_LOG);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Listing Officers", "Court Clerks"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToGetHearingEventDefinitions() {
        final Action action = createActionFor(ACTION_NAME_GET_HEARING_EVENT_DEFINITIONS);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Override
    protected Map<Class, Object> getProviderMocks() {
        return ImmutableMap.<Class, Object>builder().put(UserAndGroupProvider.class, userAndGroupProvider).build();
    }

    private void assertFailureOutcomeOnActionForTheSuppliedGroups(final String actionName, final String... groupNames) {
        final Action action = createActionFor(actionName);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, groupNames))
                .willReturn(false);

        assertFailureOutcome(executeRulesWith(action));

        verify(userAndGroupProvider).isMemberOfAnyOfTheSuppliedGroups(eq(action), arrayCaptor.capture());
        assertThat(arrayCaptor.getAllValues(), containsInAnyOrder(groupNames));
        verifyNoMoreInteractions(userAndGroupProvider);
    }

    private void assertSuccessfulOutcomeOnActionForTheSuppliedGroups(final String actionName, final String... groupNames) {
        final Action action = createActionFor(actionName);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, groupNames))
                .willReturn(true);

        assertSuccessfulOutcome(executeRulesWith(action));

        verify(userAndGroupProvider).isMemberOfAnyOfTheSuppliedGroups(eq(action), arrayCaptor.capture());
        assertThat(arrayCaptor.getAllValues(), containsInAnyOrder(groupNames));
        verifyNoMoreInteractions(userAndGroupProvider);
    }

}
