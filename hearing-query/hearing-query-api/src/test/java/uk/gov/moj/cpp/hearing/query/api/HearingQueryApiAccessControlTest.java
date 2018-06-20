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
    private static final String ACTION_NAME_GET_HEARINGS_BY_DATE = "hearing.get.hearings-by-date";
    private static final String ACTION_NAME_GET_DRAFT_RESULT = "hearing.get-draft-result";
    private static final String ACTION_NAME_GET_HEARING_EVENT_LOG = "hearing.get-hearing-event-log";
    private static final String ACTION_NAME_GET_HEARING_EVENT_DEFINITIONS = "hearing.get-hearing-event-definitions";
    private static final String ACTION_NAME_GET_HEARING_EVENT_DEFINITION = "hearing.get-hearing-event-definition";
    private static final String ACTION_NAME_GET_NOWS = "hearing.get.nows";
    private static final String HEARING_QUERY_SEARCH_BY_MATERIAL_ID = "hearing.query.search-by-material-id";

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
    public void shouldAllowUserInAuthorisedGroupToGetHearingsByDateV2() {
        assertSuccessfulOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_HEARINGS_BY_DATE, "Listing Officers", "Court Clerks");
    }

    @Test
    public void shouldNotAllowUserInUnauthorisedGroupToGetHearingsByDateV2() {
        assertFailureOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_HEARINGS_BY_DATE, "Listing Officers", "Court Clerks");
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
    public void shouldAllowUserInAuthorisedGroupToGetHearingEventDefinition() {
        assertSuccessfulOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_HEARING_EVENT_DEFINITION, "Listing Officers", "Court Clerks");
    }

    @Test
    public void shouldNotAllowUserInUnauthorisedGroupToGetHearingEventDefinition() {
        assertFailureOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_HEARING_EVENT_DEFINITION, "Listing Officers", "Court Clerks");
    }

    @Test
    public void shouldAllowUserInAuthorisedGroupToGetNows() {
        assertSuccessfulOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_NOWS, "Listing Officers", "Court Clerks","System Users");
    }

    @Test
    public void shouldNotAllowUserInUnauthorisedGroupToGetNows() {
        assertFailureOutcomeOnActionForTheSuppliedGroups(ACTION_NAME_GET_NOWS, "Listing Officers", "Court Clerks","System Users");
    }

    @Test
    public void shouldAllowUserInAuthorisedGroupToSearchByMaterialId() {
        assertSuccessfulOutcomeOnActionForTheSuppliedGroups(HEARING_QUERY_SEARCH_BY_MATERIAL_ID, "System Users");
    }

    @Test
    public void shouldNotAllowUserInUnauthorisedGroupToSearchByMaterialId() {
        assertFailureOutcomeOnActionForTheSuppliedGroups(HEARING_QUERY_SEARCH_BY_MATERIAL_ID, "System Users");
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
