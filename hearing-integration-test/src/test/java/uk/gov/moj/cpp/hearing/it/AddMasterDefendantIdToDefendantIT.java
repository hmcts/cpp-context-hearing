package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.Utilities.makeCommand;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.poll;

import uk.gov.justice.services.common.http.HeaderConstants;
import uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.utils.EventHandler;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.json.JsonObject;
import javax.ws.rs.core.Response;

import org.hamcrest.core.Is;
import org.junit.Test;

@SuppressWarnings({"squid:S2699", "squid:S1607"})
public class AddMasterDefendantIdToDefendantIT extends AbstractIT {

    private EventHandler eventHandler = new EventHandler();


    @Test
    public void shouldAddMasterDefendantIdToDefendantAsSystemUser() {


        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));
        final UUID hearingId = hearingOne.getHearingId();
        final UUID prosecutionCaseId = hearingOne.getHearing().getProsecutionCases().get(0).getId();
        final UUID defendantId = hearingOne.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId();
        final UUID masterDefendantId = randomUUID();

        poll(requestParams(getURL("hearing.get.hearing", hearingId), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()))
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(Response.Status.OK), print(), ResponsePayloadMatcher.payload().isJson(allOf(
                        withJsonPath("$.hearing.id", Is.is(hearingId.toString()))
                )));

        addMasterDefendantIdToDefendantAndVerifyEventHasBeenCreated(hearingId, prosecutionCaseId, defendantId, masterDefendantId);
    }


    private void addMasterDefendantIdToDefendantAndVerifyEventHasBeenCreated(final UUID hearingId, final UUID prosecutionCaseId, final UUID defendantId, final UUID masterDefendantId) {

        final JsonObject payload = createObjectBuilder()
                .add("prosecutionCaseId", prosecutionCaseId.toString())
                .add("defendantId", defendantId.toString())
                .add("masterDefendantId", masterDefendantId.toString())
                .build();

        final String EVENT_MASTER_DEFENDANT_ID_ADDED = "hearing.events.master-defendant-id-added";

        eventHandler.subscribe(EVENT_MASTER_DEFENDANT_ID_ADDED)
                .run(() -> makeCommand(getRequestSpec(), "hearing.master-defendant-id")
                        .ofType("application/vnd.hearing.master-defendant-id+json")
                        .withArgs(hearingId)
                        .withCppUserId(USER_ID_VALUE_AS_ADMIN)
                        .withPayload(payload.toString())
                        .executeSuccessfully());

        assertThat(eventHandler.popEvent(EVENT_MASTER_DEFENDANT_ID_ADDED), is(notNullValue()));


    }


}