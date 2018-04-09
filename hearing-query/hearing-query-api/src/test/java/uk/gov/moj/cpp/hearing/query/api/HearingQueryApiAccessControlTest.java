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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

public class HearingQueryApiAccessControlTest extends BaseDroolsAccessControlTest {

    private static final String ACTION_NAME_GET_HEARING = "hearing.get.hearing";
    private static final String ACTION_NAME_GET_HEARING_V2 = "hearing.get.hearing.v2";
    private static final String ACTION_NAME_GET_CASE_PLEAS = "hearing.get.case.pleas";
    private static final String ACTION_NAME_GET_HEARINGS_BY_START_DATE = "hearing.get.hearings-by-startdate";
    private static final String ACTION_NAME_GET_HEARINGS_BY_START_DATE_V2 = "hearing.get.hearings-by-startdate.v2";
    private static final String ACTION_NAME_GET_HEARINGS_BY_CASE_ID = "hearing.get.hearings-by-caseid";
    private static final String ACTION_NAME_GET_PROSECUTION_COUNSELS = "hearing.get.prosecution-counsels";
    private static final String ACTION_NAME_GET_DEFENCE_COUNSELS = "hearing.get.defence-counsels";
    private static final String ACTION_NAME_GET_DRAFT_RESULT = "hearing.get-draft-result";
    private static final String ACTION_NAME_GET_HEARING_EVENT_LOG = "hearing.get-hearing-event-log";
    private static final String ACTION_NAME_GET_HEARING_EVENT_DEFINITIONS = "hearing.get-hearing-event-definitions";
    private static final String ACTION_NAME_GET_HEARING_EVENT_DEFINITIONS_VERSION_TWO = "hearing.get-hearing-event-definitions.v2";
    private static final String ACTION_NAME_GET_HEARING_EVENT_DEFINITION = "hearing.get-hearing-event-definition";
    private static final String ACTION_NAME_GET_VERDICTS_BY_CASE_ID = "hearing.get.case.verdicts";
    private static final String ACTION_NAME_GET_OFFENCES_BY_HEARING_ID = "hearing.get.offences";

    @Mock
    private UserAndGroupProvider userAndGroupProvider;

    @Captor
    private ArgumentCaptor<String[]> arrayCaptor;

    @Test
    public void shouldAllowUserInAuthorisedGroupToGetHearing() {
        assertSuccessfulOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_HEARING, "Listing Officers", "Court Clerks", "System Users");
    }
    @Test
    public void shouldAllowUserInAuthorisedGroupToGetHearingV2() {
        assertSuccessfulOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_HEARING_V2, "Listing Officers", "Court Clerks", "System Users");
    }

    @Test
    public void shouldNotAllowUserInUnauthorisedGroupToGetHearing() {
        assertFailureOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_HEARING, "Listing Officers", "Court Clerks", "System Users");
    }
    
    @Test
    public void shouldNotAllowUserInUnauthorisedGroupToGetHearingV2() {
        assertFailureOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_HEARING_V2, "Listing Officers", "Court Clerks", "System Users");
    }

    @Test
    public void shouldAllowUserInAuthorisedGroupToGetCasePleas() {
        assertSuccessfulOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_CASE_PLEAS, "Listing Officers", "Court Clerks", "System Users");
    }

    @Test
    public void shouldNotAllowUserInUnauthorisedGroupToGetCasePleas() {
        assertFailureOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_CASE_PLEAS, "Listing Officers", "Court Clerks", "System Users");
    }


    @Test
    public void shouldAllowUserInAuthorisedGroupToGetHearingsByStartDate() {
        assertSuccessfulOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_HEARINGS_BY_START_DATE, "Listing Officers", "Court Clerks");
    }

    @Test
    public void shouldAllowUserInAuthorisedGroupToGetHearingsByStartDateV2() {
        assertSuccessfulOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_HEARINGS_BY_START_DATE_V2, "Listing Officers", "Court Clerks");
    }

    @Test
    public void shouldNotAllowUserInUnauthorisedGroupToGetHearingsByStartDate() {
        assertFailureOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_HEARINGS_BY_START_DATE, "Listing Officers", "Court Clerks");
    }

    @Test
    public void shouldNotAllowUserInUnauthorisedGroupToGetHearingsByStartDateV2() {
        assertFailureOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_HEARINGS_BY_START_DATE_V2, "Listing Officers", "Court Clerks");
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
    public void shouldAllowUserInAuthorisedGroupToGetHearingEventDefinitionsVersionTwo() {
        assertSuccessfulOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_HEARING_EVENT_DEFINITIONS_VERSION_TWO, "Listing Officers", "Court Clerks");
    }

    @Test
    public void shouldNotAllowUserInUnauthorisedGroupToGetHearingEventDefinitionosVersionTw() {
        assertFailureOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_HEARING_EVENT_DEFINITIONS_VERSION_TWO, "Listing Officers", "Court Clerks");
    }

    @Test
    public void shouldAllowUserInAuthorisedGroupToGetHearingEventDefinition() {
        assertSuccessfulOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_HEARING_EVENT_DEFINITION, "Listing Officers", "Court Clerks");
    }

    @Test
    public void shouldNotAllowUserInUnauthorisedGroupToGetHearingEventDefinition() {
        assertFailureOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_HEARING_EVENT_DEFINITION, "Listing Officers", "Court Clerks");
    }

    @Test
    public void shouldAllowUserInAuthorisedGroupToGetVerdicts() {
        assertSuccessfulOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_VERDICTS_BY_CASE_ID, "Listing Officers", "Court Clerks","System Users");
    }

    @Test
    public void shouldNotAllowUserInUnauthorisedGroupToGetVerdicts() {
        assertFailureOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_VERDICTS_BY_CASE_ID, "Listing Officers", "Court Clerks","System Users");
    }

    @Test
    public void shouldAllowUserInAuthorisedGroupToGetOffences() {
        assertSuccessfulOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_OFFENCES_BY_HEARING_ID, "Listing Officers", "Court Clerks","System Users");
    }

    @Test
    public void shouldNotAllowUserInUnauthorisedGroupToGetOffences() {
        assertFailureOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_OFFENCES_BY_HEARING_ID, "Listing Officers", "Court Clerks","System Users");
    }


    @Override
    protected Map<Class, Object> getProviderMocks() {
        return ImmutableMap.<Class, Object>builder().put(UserAndGroupProvider.class, this.userAndGroupProvider).build();
    }

    private void assertFailureOutcomeOnActionForTheSuppliedGroups(final String actionName, final String... groupNames) {
        final Action action = createActionFor(actionName);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, groupNames))
                .willReturn(false);

        assertFailureOutcome(executeRulesWith(action));

        verify(this.userAndGroupProvider).isMemberOfAnyOfTheSuppliedGroups(eq(action), this.arrayCaptor.capture());
        assertThat(this.arrayCaptor.getAllValues(), containsInAnyOrder(groupNames));
        verifyNoMoreInteractions(this.userAndGroupProvider);
    }

    private void assertSuccessfulOutcomeOnActionForTheSuppliedGroups(final String actionName, final String... groupNames) {
        final Action action = createActionFor(actionName);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, groupNames))
                .willReturn(true);

        assertSuccessfulOutcome(executeRulesWith(action));

        verify(this.userAndGroupProvider).isMemberOfAnyOfTheSuppliedGroups(eq(action), this.arrayCaptor.capture());
        assertThat(this.arrayCaptor.getAllValues(), containsInAnyOrder(groupNames));
        verifyNoMoreInteractions(this.userAndGroupProvider);
    }

}
