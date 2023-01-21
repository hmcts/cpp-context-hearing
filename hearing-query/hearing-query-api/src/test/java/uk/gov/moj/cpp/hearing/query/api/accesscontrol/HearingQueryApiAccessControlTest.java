package uk.gov.moj.cpp.hearing.query.api.accesscontrol;

import static org.mockito.BDDMockito.given;

import uk.gov.moj.cpp.accesscontrol.common.providers.UserAndGroupProvider;
import uk.gov.moj.cpp.accesscontrol.drools.Action;
import uk.gov.moj.cpp.accesscontrol.hearing.providers.HearingProvider;
import uk.gov.moj.cpp.accesscontrol.test.utils.BaseDroolsAccessControlTest;
import uk.gov.moj.cpp.hearing.query.api.accescontrol.RuleConstants;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.kie.api.runtime.ExecutionResults;
import org.mockito.Mock;

public class HearingQueryApiAccessControlTest extends BaseDroolsAccessControlTest {

    private static final String ACTION_NAME_GET_CASES_BY_PERSON_DEFENDANT = "hearing.get.cases-by-person-defendant";
    private static final String ACTION_NAME_GET_CASES_BY_ORGANISATION_DEFENDANT = "hearing.get.cases-by-organisation-defendant";


    @Mock
    private UserAndGroupProvider userAndGroupProvider;

    @Mock
    private HearingProvider hearingProvider;

    @Test
    public void shouldAllowAuthorisedUserToGetCasesByPersonDefendant() {
        final Action action = createActionFor(ACTION_NAME_GET_CASES_BY_PERSON_DEFENDANT);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, RuleConstants.getQueryForCaseByDefendantUsersGroup()))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUserToGetCasesByPersonDefendant() {
        final Action action = createActionFor(ACTION_NAME_GET_CASES_BY_PERSON_DEFENDANT);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "GROUP_INVALID"))
                .willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToGetCasesByOrganisationDefendant() {
        final Action action = createActionFor(ACTION_NAME_GET_CASES_BY_ORGANISATION_DEFENDANT);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, RuleConstants.getQueryForCaseByDefendantUsersGroup()))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUserToGetCasesByOrganisationDefendant() {
        final Action action = createActionFor(ACTION_NAME_GET_CASES_BY_ORGANISATION_DEFENDANT);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "GROUP_INVALID"))
                .willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }


    @Override
    protected Map<Class, Object> getProviderMocks() {
        return ImmutableMap.<Class, Object>builder()
                .put(UserAndGroupProvider.class, this.userAndGroupProvider)
                .put(HearingProvider.class, this.hearingProvider)
                .build();
    }

}