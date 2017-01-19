package uk.gov.moj.cpp.hearing.query.api;

import static org.mockito.BDDMockito.given;

import uk.gov.moj.cpp.accesscontrol.common.providers.UserAndGroupProvider;
import uk.gov.moj.cpp.accesscontrol.drools.Action;
import uk.gov.moj.cpp.accesscontrol.test.utils.BaseDroolsAccessControlTest;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.kie.api.runtime.ExecutionResults;
import org.mockito.Mock;

public class HearingQueryApiAccessControlTest extends BaseDroolsAccessControlTest {

    private static final String ACTION_NAME_GET_HEARING = "hearing.get.hearing";
    private static final String ACTION_NAME_GET_HEARINGS_BY_START_DATE = "hearing.get.hearings-by-startdate";
    private static final String ACTION_NAME_GET_PROSECUTION_COUNSELS = "hearing.get.prosecution-counsels";
    private static final String ACTION_NAME_GET_DRAFT_RESULT = "hearing.get-draft-result";
    private static final String ACTION_NAME_GET_HEARING_EVENT_LOG = "hearing.get-hearing-event-log";

    @Mock
    private UserAndGroupProvider userAndGroupProvider;

    @Test
    public void shouldAllowAuthorisedUserToGetHearing() {
        final Action action = createActionFor(ACTION_NAME_GET_HEARING);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Listing Officers", "Court Clerks"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToGetHearing() {
        final Action action = createActionFor(ACTION_NAME_GET_HEARING);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToGetHearings() {
        final Action action = createActionFor(ACTION_NAME_GET_HEARINGS_BY_START_DATE);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Listing Officers", "Court Clerks"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToGetHearings() {
        final Action action = createActionFor(ACTION_NAME_GET_HEARINGS_BY_START_DATE);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToGetProsecutionCounsels() {
        final Action action = createActionFor(ACTION_NAME_GET_PROSECUTION_COUNSELS);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Listing Officers", "Court Clerks"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToGetProsecutionCounsels() {
        final Action action = createActionFor(ACTION_NAME_GET_PROSECUTION_COUNSELS);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToGetDraftResult() {
        final Action action = createActionFor(ACTION_NAME_GET_DRAFT_RESULT);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Court Clerks"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToGetDraftResult() {
        final Action action = createActionFor(ACTION_NAME_GET_DRAFT_RESULT);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToGetHearingEventLog() {
        final Action action = createActionFor(ACTION_NAME_GET_HEARING_EVENT_LOG);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Listing Officers", "Court Clerks"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToGetHearingEventLog() {
        final Action action = createActionFor(ACTION_NAME_GET_HEARING_EVENT_LOG);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Override
    protected Map<Class, Object> getProviderMocks() {
        return ImmutableMap.<Class, Object>builder().put(UserAndGroupProvider.class, userAndGroupProvider).build();
    }

}
