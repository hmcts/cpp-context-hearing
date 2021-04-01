package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.poll;

import uk.gov.justice.services.common.converter.LocalDates;
import uk.gov.justice.services.test.utils.core.http.ResponseData;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ArrayUtils;
import org.hamcrest.Matcher;
import org.json.JSONObject;
import org.junit.Test;

public class NotepadHearingIT extends AbstractIT {

    @Test
    public void shouldParseDataThatAreAvailableTodayWithAssociatedPrompts() {
        final String definitionQueryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.notepad.result-definition"), "RESTRAOP", LocalDate.now().toString());
        final String definitionUrl = getBaseUri() + "/" + definitionQueryAPIEndPoint;
        final String definitionMediaType = "application/vnd.hearing.notepad.parse-result-definition+json";

        final Matcher[] matchersForResultDefinition = {
                withJsonPath("$.originalText", is("RESTRAOP")),
                withJsonPath("$.parts[0].value", is("Restraining order for period")),
                withJsonPath("$.parts[0].state", is("RESOLVED")),
                withJsonPath("$.parts[0].type", is("RESULT")),
                withJsonPath("$.excludedFromResults", is(false)),
                withJsonPath("$.conditionalMandatory", is(false)),
                withJsonPath("$.promptChoices", hasSize(7)),
                withJsonPath("$.childResultDefinitions", hasSize(2)),
                withJsonPath("$.childResultDefinitions[?(@.code == 'dada120c-160a-49a9-b040-e8b6b7128d67')].childResultCodes", empty()),
                withJsonPath("$.childResultDefinitions[?(@.code == 'b2dab2b7-3edd-4223-b1be-3819173ec54d')].childResultCodes.*", hasSize(36))
        };

        final ResponseData responseData = poll(requestParams(definitionUrl, definitionMediaType).withHeader(USER_ID, getLoggedInUser().toString()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                ArrayUtils.addAll(matchersForResultDefinition, getMatchersForPromptChoices())
                        )));

        final JSONObject jsonObject = new JSONObject(responseData.getPayload());
        final String resultCode = jsonObject.getString("resultCode");
        final String promptsQueryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.notepad.prompt"), resultCode, LocalDate.now().toString());
        final String promptUrl = getBaseUri() + "/" + promptsQueryAPIEndPoint;
        final String promptMediaType = "application/vnd.hearing.notepad.parse-result-prompt+json";

        poll(requestParams(promptUrl, promptMediaType).withHeader(USER_ID, getLoggedInUser().toString()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                getMatchersForPromptChoices()
                        )));
    }

    @Test
    public void shouldParseDataThatAreAvailableTodayWithUsingSynonyms() {
        final String definitionQueryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.notepad.result-definition"), "Restraining", LocalDate.now().toString());
        final String definitionUrl = getBaseUri() + "/" + definitionQueryAPIEndPoint;
        final String definitionMediaType = "application/vnd.hearing.notepad.parse-result-definition+json";

        final Matcher[] matchersForResultDefinition = {
                withJsonPath("$.originalText", is("Restraining")),
                withJsonPath("$.parts[0].value", is("Restraining")),
                withJsonPath("$.parts[0].state", is("UNRESOLVED")),
                withJsonPath("$.parts[0].resultChoices[*].shortCode", containsInAnyOrder("restrad", "restraop", "restraof", "restrav"))
        };

        poll(requestParams(definitionUrl, definitionMediaType).withHeader(USER_ID, getLoggedInUser().toString()).build())
                .until(
                        status().is(OK),
                        payload().isJson(allOf(matchersForResultDefinition)
                        ));
    }

    @Test
    public void shouldGetUnresolvedResultWhenLookingForFutureDefinition() {
        final LocalDate orderedDate = LocalDate.now().plusDays(1);
        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.notepad.result-definition"), "RESTRAOP", orderedDate.toString());
        final String url = getBaseUri() + "/" + queryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.notepad.parse-result-definition+json";

        poll(requestParams(url, mediaType).withHeader(USER_ID, getLoggedInUser().toString()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.originalText", is("RESTRAOP")),
                                withJsonPath("$.orderedDate", is(LocalDates.to(orderedDate))),
                                withJsonPath("$.parts[0].state", is("UNRESOLVED"))
                        )));

    }

    @Test
    public void shouldParseDataThatAreAvailableTomorrow() {
        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.notepad.result-definition"), "RESTRAOP2", LocalDate.now().plusDays(1).toString());
        final String url = getBaseUri() + "/" + queryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.notepad.parse-result-definition+json";

        poll(requestParams(url, mediaType).withHeader(USER_ID, getLoggedInUser().toString()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.originalText", is("RESTRAOP2")),
                                withJsonPath("$.parts[0].value", is("Restraining order for period")),
                                withJsonPath("$.parts[0].state", is("RESOLVED"))
                        )));

    }

    private Matcher[] getMatchersForPromptChoices() {
        return new Matcher[]{withJsonPath("$.promptChoices[0].code", is("3054909b-15b6-499f-b44f-67b2b1215c76")),
                withJsonPath("$.promptChoices[0].label", is("Protected person's address")),
                withJsonPath("$.promptChoices[0].type", is("TXT")),
                withJsonPath("$.promptChoices[0].required", is(true)),
                withJsonPath("$.promptChoices[0].durationSequence", is(0)),
                withJsonPath("$.promptChoices[0].componentType", is("TXT")),
                withJsonPath("$.promptChoices[0].hidden", is(false)),
                withJsonPath("$.promptChoices[1].code", is("abc9bb61-cb5b-4cf7-be24-8866bcd2fc69")),
                withJsonPath("$.promptChoices[1].label", is("Protected person")),
                withJsonPath("$.promptChoices[1].type", is("TXT")),
                withJsonPath("$.promptChoices[1].required", is(true)),
                withJsonPath("$.promptChoices[1].durationSequence", is(0)),
                withJsonPath("$.promptChoices[1].componentType", is("TXT")),
                withJsonPath("$.promptChoices[1].hidden", is(false)),
                withJsonPath("$.promptChoices[2].code", is("8df0ec7e-5985-4998-af1a-5da293d9cb3c")),
                withJsonPath("$.promptChoices[2].label", is("Order details")),
                withJsonPath("$.promptChoices[2].type", is("TXT")),
                withJsonPath("$.promptChoices[2].required", is(true)),
                withJsonPath("$.promptChoices[2].durationSequence", is(0)),
                withJsonPath("$.promptChoices[2].componentType", is("TXT")),
                withJsonPath("$.promptChoices[2].hidden", is(false)),
                withJsonPath("$.promptChoices[2].minLength", is("1")),
                withJsonPath("$.promptChoices[2].maxLength", is("4000")),
                withJsonPath("$.promptChoices[3].code", is("a20665cc-6877-40f4-b85e-d4c87e62987b")),
                withJsonPath("$.promptChoices[3].label", is("Period of order")),
                withJsonPath("$.promptChoices[3].type", is("DURATION")),
                withJsonPath("$.promptChoices[3].required", is(true)),
                withJsonPath("$.promptChoices[3].durationSequence", is(0)),
                withJsonPath("$.promptChoices[3].componentType", is("DURATION")),
                withJsonPath("$.promptChoices[3].hidden", is(false)),
                withJsonPath("$.promptChoices[3].children[0].type", is("INT")),
                withJsonPath("$.promptChoices[3].children[0].label", is("Years")),
                withJsonPath("$.promptChoices[3].children[0].welshLabel", is("Flynedd")),
                withJsonPath("$.promptChoices[3].children[1].type", is("INT")),
                withJsonPath("$.promptChoices[3].children[1].label", is("Months")),
                withJsonPath("$.promptChoices[3].children[1].welshLabel", is("Mis")),
                withJsonPath("$.promptChoices[3].children[2].type", is("INT")),
                withJsonPath("$.promptChoices[3].children[2].label", is("Weeks")),
                withJsonPath("$.promptChoices[3].children[2].welshLabel", is("Wythnos")),
                withJsonPath("$.promptChoices[3].children[3].type", is("INT")),
                withJsonPath("$.promptChoices[3].children[3].label", is("Days")),
                withJsonPath("$.promptChoices[3].children[3].welshLabel", is("Niwrnod")),
                withJsonPath("$.promptChoices[4].code", is("47337f1c-e343-4093-884f-035ba96c4db0")),
                withJsonPath("$.promptChoices[4].label", is("Conviction / acquittal")),
                withJsonPath("$.promptChoices[4].type", is("FIXL")),
                withJsonPath("$.promptChoices[4].required", is(true)),
                withJsonPath("$.promptChoices[4].durationSequence", is(0)),
                withJsonPath("$.promptChoices[4].componentType", is("FIXL")),
                withJsonPath("$.promptChoices[4].hidden", is(false)),
                withJsonPath("$.promptChoices[5].code", is("6927b6ac-4c85-4532-838d-88ac00ea83f9")),
                withJsonPath("$.promptChoices[5].type", is("NAMEADDRESS")),
                withJsonPath("$.promptChoices[5].required", is(true)),
                withJsonPath("$.promptChoices[5].promptOrder", is(500)),
                withJsonPath("$.promptChoices[5].componentType", is("NAMEADDRESS")),
                withJsonPath("$.promptChoices[5].children[0].label", is("Conveyor / custodian name organisation name")),
                withJsonPath("$.promptChoices[5].children[0].type", is("TXT")),
                withJsonPath("$.promptChoices[5].children[0].promptRef", is("conveyorcustodiannameOrganisationName")),
                withJsonPath("$.promptChoices[5].children[0].sequence", is(500)),
                withJsonPath("$.promptChoices[5].children[1].label", is("Conveyor / custodian name address line 1")),
                withJsonPath("$.promptChoices[5].children[1].type", is("TXT")),
                withJsonPath("$.promptChoices[5].children[1].promptRef", is("conveyorcustodiannameAddress1")),
                withJsonPath("$.promptChoices[5].children[1].sequence", is(500)),
                withJsonPath("$.promptChoices[6].code", is("ea26f773-0a91-4526-b4ad-84d07b5bf940")),
                withJsonPath("$.promptChoices[6].label", is("Reason")),
                withJsonPath("$.promptChoices[6].type", is("TXT")),
                withJsonPath("$.promptChoices[6].required", is(false)),
                withJsonPath("$.promptChoices[6].durationSequence", is(0)),
                withJsonPath("$.promptChoices[6].minLength", is("1")),
                withJsonPath("$.promptChoices[6].maxLength", is("1000")),
                withJsonPath("$.promptChoices[6].componentType", is("TXT"))};
    }
}
