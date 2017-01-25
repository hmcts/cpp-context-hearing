package uk.gov.moj.cpp.hearing.command.api;

import static org.mockito.BDDMockito.given;

import uk.gov.moj.cpp.accesscontrol.common.providers.UserAndGroupProvider;
import uk.gov.moj.cpp.accesscontrol.drools.Action;
import uk.gov.moj.cpp.accesscontrol.test.utils.BaseDroolsAccessControlTest;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.kie.api.runtime.ExecutionResults;
import org.mockito.Mock;

public class HearingCommandApiAccessControlTest extends BaseDroolsAccessControlTest {

    private static final String ACTION_NAME_INITIATE_HEARING = "hearing.initiate-hearing";
    private static final String ACTION_NAME_START_HEARING = "hearing.start";
    private static final String ACTION_NAME_ADJOURN_HEARING_DATE = "hearing.adjourn-date";
    private static final String ACTION_NAME_END_HEARING = "hearing.end";
    private static final String ACTION_NAME_BOOK_ROOM = "hearing.book-room";
    private static final String ACTION_NAME_ALLOCATE_COURT = "hearing.allocate-court";
    private static final String ACTION_NAME_ADD_PROSECUTION_COUNSEL = "hearing.add-prosecution-counsel";
    private static final String ACTION_NAME_SAVE_DRAFT_RESULT = "hearing.save-draft-result";
    private static final String ACTION_NAME_LOG_HEARING_EVENT = "hearing.log-hearing-event";
    private static final String ACTION_NAME_CORRECT_HEARING_EVENT = "hearing.correct-hearing-event";

    @Mock
    private UserAndGroupProvider userAndGroupProvider;

    @Test
    public void shouldAllowAuthorisedUserToInitiateHearing() {
        final Action action = createActionFor(ACTION_NAME_INITIATE_HEARING);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Listing Officers", "Court Clerks"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToInitiateHearing() {
        final Action action = createActionFor(ACTION_NAME_INITIATE_HEARING);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToStart() {
        final Action action = createActionFor(ACTION_NAME_START_HEARING);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Listing Officers", "Court Clerks"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToStart() {
        final Action action = createActionFor(ACTION_NAME_START_HEARING);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToAdjournDate() {
        final Action action = createActionFor(ACTION_NAME_ADJOURN_HEARING_DATE);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Listing Officers", "Court Clerks"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToAdjournDate() {
        final Action action = createActionFor(ACTION_NAME_ADJOURN_HEARING_DATE);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToEnd() {
        final Action action = createActionFor(ACTION_NAME_END_HEARING);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Listing Officers", "Court Clerks"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToEnd() {
        final Action action = createActionFor(ACTION_NAME_END_HEARING);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToBookRoom() {
        final Action action = createActionFor(ACTION_NAME_BOOK_ROOM);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Listing Officers", "Court Clerks"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToBookRoom() {
        final Action action = createActionFor(ACTION_NAME_BOOK_ROOM);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToAllocateCourt() {
        final Action action = createActionFor(ACTION_NAME_ALLOCATE_COURT);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Listing Officers", "Court Clerks"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToAllocateCourt() {
        final Action action = createActionFor(ACTION_NAME_ALLOCATE_COURT);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToAddProsecutionCounsel() {
        final Action action = createActionFor(ACTION_NAME_ADD_PROSECUTION_COUNSEL);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Listing Officers", "Court Clerks"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToAddProsecutionCounsel() {
        final Action action = createActionFor(ACTION_NAME_ADD_PROSECUTION_COUNSEL);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }


    @Test
    public void shouldAllowAuthorisedUserToSaveDraftResult() {
        final Action action = createActionFor(ACTION_NAME_SAVE_DRAFT_RESULT);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Court Clerks")).willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToSaveDraftResult() {
        final Action action = createActionFor(ACTION_NAME_SAVE_DRAFT_RESULT);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToLogHearingEvent() {
        final Action action = createActionFor(ACTION_NAME_LOG_HEARING_EVENT);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Listing Officers", "Court Clerks")).willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToLogHearingEvent() {
        final Action action = createActionFor(ACTION_NAME_LOG_HEARING_EVENT);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToCorrectHearingEvent() {
        final Action action = createActionFor(ACTION_NAME_CORRECT_HEARING_EVENT);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Listing Officers", "Court Clerks")).willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToCorrectHearingEvent() {
        final Action action = createActionFor(ACTION_NAME_CORRECT_HEARING_EVENT);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Override
    protected Map<Class, Object> getProviderMocks() {
        return ImmutableMap.<Class, Object>builder().put(UserAndGroupProvider.class, userAndGroupProvider).build();
    }
}
