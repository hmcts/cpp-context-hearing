package uk.gov.moj.cpp.hearing.command.api.accesscontrol;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;

import uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder;
import uk.gov.moj.cpp.accesscontrol.common.providers.UserAndGroupProvider;
import uk.gov.moj.cpp.accesscontrol.drools.Action;
import uk.gov.moj.cpp.accesscontrol.test.utils.BaseDroolsAccessControlTest;
import uk.gov.moj.cpp.hearing.command.api.accescontrol.RuleConstants;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.ExecutionResults;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@RunWith(MockitoJUnitRunner.class)
public class RuleConstantTest extends BaseDroolsAccessControlTest {

    private Action action;

    @Mock
    private UserAndGroupProvider userAndGroupProvider;

    @Test
    public void shouldAllowAuthorisedUserToInitiateHearing() throws JsonProcessingException {
        final Map<String, String> metadata = new HashMap();
        metadata.putIfAbsent("id", UUID.randomUUID().toString());
        metadata.putIfAbsent("name", "hearing.initiate");
        action = createActionFor(metadata);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, RuleConstants.getUsersForInitiateHearing())).willReturn(true);
        given(userAndGroupProvider.hasPermission(action, RuleConstants.expectedPermissionsForCase())).willReturn(true);
        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
        verify(userAndGroupProvider, times(1)).isMemberOfAnyOfTheSuppliedGroups(action, RuleConstants.getUsersForInitiateHearing());
        verify(userAndGroupProvider, times(1)).hasPermission(action, RuleConstants.expectedPermissionsForCase());

    }

    @Test
    public void shouldAllowAuthorisedUserWithPermissionToInitiateHearing() throws JsonProcessingException {
        final Map<String, String> metadata = new HashMap();
        metadata.putIfAbsent("id", UUID.randomUUID().toString());
        metadata.putIfAbsent("name", "hearing.initiate");
        action = createActionFor(metadata);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, RuleConstants.getUsersForInitiateHearing())).willReturn(false);
        given(userAndGroupProvider.hasPermission(action, RuleConstants.expectedPermissionsForCase())).willReturn(true);
        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
        verify(userAndGroupProvider, times(1)).isMemberOfAnyOfTheSuppliedGroups(action, RuleConstants.getUsersForInitiateHearing());
        verify(userAndGroupProvider, times(1)).hasPermission(action, RuleConstants.expectedPermissionsForCase());

    }

    @Test
    public void shouldNotAllowUnauthorisedUserToInitiateHearing() throws JsonProcessingException {
        final Map<String, String> metadata = new HashMap();
        metadata.putIfAbsent("id", UUID.randomUUID().toString());
        metadata.putIfAbsent("name", "hearing.initiate");
        action = createActionFor(metadata);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, RuleConstants.getUsersForInitiateHearing())).willReturn(false);
        given(userAndGroupProvider.hasPermission(action, RuleConstants.expectedPermissionsForCase())).willReturn(false);
        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
        verify(userAndGroupProvider, times(1)).isMemberOfAnyOfTheSuppliedGroups(action, RuleConstants.getUsersForInitiateHearing());
        verify(userAndGroupProvider, times(1)).hasPermission(action, RuleConstants.expectedPermissionsForCase());

    }

    @Override
    protected Map<Class, Object> getProviderMocks() {
        return ImmutableMap.<Class, Object>builder().put(UserAndGroupProvider.class, userAndGroupProvider).build();
    }

    @Override
    protected Action createActionFor(final Map<String, String> metadata) {
        JsonEnvelopeBuilder jsonEnvelopeBuilder = JsonEnvelopeBuilder.envelope().withPayloadOf(UUID.randomUUID().toString(), "caseId");
        return new Action(jsonEnvelopeBuilder.with(metadataOf(UUID.randomUUID().toString(), metadata.get("name"))).build());
    }
}