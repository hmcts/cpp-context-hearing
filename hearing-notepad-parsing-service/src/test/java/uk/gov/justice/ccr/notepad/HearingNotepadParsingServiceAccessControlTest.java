package uk.gov.justice.ccr.notepad;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.kie.api.runtime.ExecutionResults;
import org.mockito.Mock;
import uk.gov.moj.cpp.accesscontrol.common.providers.UserAndGroupProvider;
import uk.gov.moj.cpp.accesscontrol.drools.Action;
import uk.gov.moj.cpp.accesscontrol.test.utils.BaseDroolsAccessControlTest;

import java.util.Map;

import static org.mockito.BDDMockito.given;

public class HearingNotepadParsingServiceAccessControlTest extends BaseDroolsAccessControlTest {

    private static final String HEARING_NOTEPAD_PARSE_RESULT_DEFINITION = "hearing.notepad.parse-result-definition";
    private static final String HEARING_NOTEPAD_PARSE_RESULT_PROMPT = "hearing.notepad.parse-result-prompt";
    private static final String HEARING_NOTEPAD_RELOAD_RESULT_CACHE = "hearing.notepad.reload-result-cache";


    @Mock
    private UserAndGroupProvider userAndGroupProvider;

    @Test
    public void shouldAllowAuthorisedUserToParseResultDefinition() {
        final Action action = createActionFor(HEARING_NOTEPAD_PARSE_RESULT_DEFINITION);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action,  "Court Clerks"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToParseResultDefinition() {
        final Action action = createActionFor(HEARING_NOTEPAD_PARSE_RESULT_DEFINITION);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToParseResultPrompt() {
        final Action action = createActionFor(HEARING_NOTEPAD_PARSE_RESULT_PROMPT);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action,  "Court Clerks"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToParseResultPrompt() {
        final Action action = createActionFor(HEARING_NOTEPAD_PARSE_RESULT_PROMPT);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToReloadResultCache() {
        final Action action = createActionFor(HEARING_NOTEPAD_RELOAD_RESULT_CACHE);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action,  "Court Clerks","Court Administrators"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToReloadResultCache() {
        final Action action = createActionFor(HEARING_NOTEPAD_RELOAD_RESULT_CACHE);
        given(userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }


    @Override
    protected Map<Class, Object> getProviderMocks() {
        return ImmutableMap.<Class, Object>builder().put(UserAndGroupProvider.class, userAndGroupProvider).build();
    }
}
