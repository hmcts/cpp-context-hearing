package uk.gov.moj.cpp.hearing.steps;

import static com.google.common.collect.Lists.newArrayList;
import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.text.MessageFormat.format;
import static java.util.stream.Collectors.toList;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.CombinableMatcher.both;
import static org.hamcrest.core.IsEqual.equalTo;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.publicEvents;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.retrieveMessage;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsAuthorisedUser;

import uk.gov.moj.cpp.hearing.domain.ResultPrompt;
import uk.gov.moj.cpp.hearing.it.AbstractIT;
import uk.gov.moj.cpp.hearing.steps.data.DefenceCounselData;
import uk.gov.moj.cpp.hearing.steps.data.ResultLineData;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import javax.jms.MessageConsumer;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.Response;

import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.matchers.IsJson;
import com.jayway.restassured.path.json.JsonPath;
import org.hamcrest.Matcher;

@SuppressWarnings("unchecked")
public class HearingStepDefinitions extends AbstractIT {

    private static final String HEARING_RESULTED_PUBLIC_EVENT = "public.hearing.resulted";
    private static final String HEARING_RESULT_AMENDED_EVENT = "public.hearing.result-amended";
    private static final MessageConsumer PUBLIC_HEARING_RESULTED_EVENTS_CONSUMER = publicEvents.createConsumer(HEARING_RESULTED_PUBLIC_EVENT);
    private static final MessageConsumer PUBLIC_HEARING_RESULT_AMENDED_EVENTS_CONSUMER = publicEvents.createConsumer(HEARING_RESULT_AMENDED_EVENT);

    private static final String MEDIA_TYPE_ADD_DEFENCE_COUNSEL = "application/vnd.hearing.add-defence-counsel+json";
    private static final String MEDIA_TYPE_SHARE_RESULTS = "application/vnd.hearing.share-results+json";

    private static final String FIELD_GENERIC_ID = "id";
    private static final String FIELD_LAST_SHARED_RESULT_ID = "lastSharedResultId";
    private static final String FIELD_PERSON_ID = "personId";
    private static final String FIELD_CASE_ID = "caseId";
    private static final String FIELD_LEVEL = "level";
    private static final String FIELD_OFFENCE_ID = "offenceId";
    private static final String FIELD_RESULT_LINES = "resultLines";
    private static final String FIELD_RESULT_LABEL = "resultLabel";
    private static final String FIELD_COURT = "court";
    private static final String FIELD_COURT_ROOM = "courtRoom";
    private static final String FIELD_CLERK_OF_THE_COURT_ID = "clerkOfTheCourtId";
    private static final String FIELD_CLERK_OF_THE_COURT_FIRST_NAME = "clerkOfTheCourtFirstName";
    private static final String FIELD_CLERK_OF_THE_COURT_LAST_NAME = "clerkOfTheCourtLastName";
    private static final String FIELD_PROMPTS = "prompts";
    private static final String FIELD_LABEL = "label";
    private static final String FIELD_VALUE = "value";
    private static final String FIELD_ATTENDEE_ID = "attendeeId";
    private static final String FIELD_DEFENDANT_IDS = "defendantIds";
    private static final String FIELD_DEFENDANT_ID = "defendantId";
    private static final String FIELD_STATUS = "status";

    public static void givenAUserHasLoggedInAsACourtClerk(final UUID validUserId) {
        setupAsAuthorisedUser(validUserId);
        setLoggedInUser(validUserId);
    }

    public static void whenHearingHasDefendantsWithDefenceCounsels(final UUID hearingId, final List<DefenceCounselData> defenceCounsels) {
        final String initiateHearingUrl = String.format("%s/%s", baseUri, format(ENDPOINT_PROPERTIES.getProperty("hearing.initiate-hearing"), hearingId));

        defenceCounsels.forEach(defenceCounsel -> {
                    final JsonArrayBuilder defendantIdsBuilder = createArrayBuilder();
                    defenceCounsel.getMapOfDefendantIdToNames().keySet()
                            .forEach(defendantId -> defendantIdsBuilder
                                    .add(
                                            createObjectBuilder().add(FIELD_DEFENDANT_ID, defendantId.toString())
                                    ));

                    final JsonObjectBuilder defenceCounselPayloadJson = createObjectBuilder()
                            .add(FIELD_ATTENDEE_ID, defenceCounsel.getAttendeeId().toString())
                            .add(FIELD_PERSON_ID, defenceCounsel.getPersonId().toString())
                            .add(FIELD_DEFENDANT_IDS, defendantIdsBuilder)
                            .add(FIELD_STATUS, defenceCounsel.getStatus());

                    final Response response = restClient.postCommand(initiateHearingUrl, MEDIA_TYPE_ADD_DEFENCE_COUNSEL, defenceCounselPayloadJson.build().toString(), getLoggedInHeader());

                    assertThat(response.getStatus(), equalTo(SC_ACCEPTED));
                }
        );
    }

    public static void whenTheUserSharesResultsForAHearing(final UUID hearingId, final List<ResultLineData> resultLines) {
        final String shareResultsUrl = String.format("%s/%s", baseUri, format(ENDPOINT_PROPERTIES.getProperty("hearing.share-results"), hearingId));

        final JsonObjectBuilder resultLinesJson = prepareJsonForResultLines(resultLines);

        final Response response = restClient.postCommand(shareResultsUrl, MEDIA_TYPE_SHARE_RESULTS,
                resultLinesJson.build().toString(), getLoggedInHeader());

        assertThat(response.getStatus(), equalTo(SC_ACCEPTED));
    }

    public static void whenTheUserSharesAmendedResultsForTheHearing(final UUID hearingId, final List<ResultLineData> resultLines) {
        whenTheUserSharesResultsForAHearing(hearingId, resultLines);
    }

    public static void andHearingResultsHaveBeenShared(final UUID hearingId, final List<ResultLineData> resultLines) {
        whenTheUserSharesResultsForAHearing(hearingId, resultLines);
    }

    public static void thenHearingResultedPublicEventShouldBePublished(final UUID hearingId, final List<ResultLineData> resultLines) {
        final JsonPath message = retrieveMessage(PUBLIC_HEARING_RESULTED_EVENTS_CONSUMER);

        assertThat(message.prettify(), new IsJson(allOf(
                withJsonPath("$._metadata.name", equalTo(HEARING_RESULTED_PUBLIC_EVENT)),

                withJsonPath("$.hearingId", equalTo(hearingId.toString())),
                withJsonPath("$.sharedTime", is(notNullValue())),
                withJsonPath("$.resultLines", hasSize(3)),

                withJsonPath("$.resultLines[0].id", equalTo(resultLines.get(0).getId().toString())),
                withJsonPath("$.resultLines[0].caseId", equalTo(resultLines.get(0).getCaseId().toString())),
                withJsonPath("$.resultLines[0].personId", equalTo(resultLines.get(0).getPersonId().toString())),
                withJsonPath("$.resultLines[0].offenceId", equalTo(resultLines.get(0).getOffenceId().toString())),
                withJsonPath("$.resultLines[0].level", equalTo(resultLines.get(0).getLevel().name())),
                withJsonPath("$.resultLines[0].resultLabel", equalTo(resultLines.get(0).getResultLabel())),
                withJsonPath("$.resultLines[0].prompts[*].label", hasItems(resultLines.get(0).getPrompts().stream().map(ResultPrompt::getLabel).collect(toList()).toArray())),
                withJsonPath("$.resultLines[0].prompts[*].value", hasItems(resultLines.get(0).getPrompts().stream().map(ResultPrompt::getValue).collect(toList()).toArray())),

                withJsonPath("$.resultLines[1].id", equalTo(resultLines.get(1).getId().toString())),
                withJsonPath("$.resultLines[1].caseId", equalTo(resultLines.get(1).getCaseId().toString())),
                withJsonPath("$.resultLines[1].personId", equalTo(resultLines.get(1).getPersonId().toString())),
                withJsonPath("$.resultLines[1].offenceId", equalTo(resultLines.get(1).getOffenceId().toString())),
                withJsonPath("$.resultLines[1].level", equalTo(resultLines.get(1).getLevel().name())),
                withJsonPath("$.resultLines[1].resultLabel", equalTo(resultLines.get(1).getResultLabel())),
                withJsonPath("$.resultLines[1].prompts[*].label", hasItems(resultLines.get(1).getPrompts().stream().map(ResultPrompt::getLabel).collect(toList()).toArray())),
                withJsonPath("$.resultLines[1].prompts[*].value", hasItems(resultLines.get(1).getPrompts().stream().map(ResultPrompt::getValue).collect(toList()).toArray())),

                withJsonPath("$.resultLines[2].id", equalTo(resultLines.get(2).getId().toString())),
                withJsonPath("$.resultLines[2].caseId", equalTo(resultLines.get(2).getCaseId().toString())),
                withJsonPath("$.resultLines[2].personId", equalTo(resultLines.get(2).getPersonId().toString())),
                withJsonPath("$.resultLines[2].offenceId", equalTo(resultLines.get(2).getOffenceId().toString())),
                withJsonPath("$.resultLines[2].level", equalTo(resultLines.get(2).getLevel().name())),
                withJsonPath("$.resultLines[2].resultLabel", equalTo(resultLines.get(2).getResultLabel())),
                withJsonPath("$.resultLines[2].prompts[*].label", hasItems(resultLines.get(2).getPrompts().stream().map(ResultPrompt::getLabel).collect(toList()).toArray())),
                withJsonPath("$.resultLines[2].prompts[*].value", hasItems(resultLines.get(2).getPrompts().stream().map(ResultPrompt::getValue).collect(toList()).toArray()))
        )));
    }

    public static void thenHearingAmendedPublicEventShouldBePublished(final UUID hearingId, final ResultLineData amendedResult) {
        final JsonPath message = retrieveMessage(PUBLIC_HEARING_RESULT_AMENDED_EVENTS_CONSUMER);

        final Matcher<ReadContext> amendedEventExceptResultPrompts = allOf(
                withJsonPath("$._metadata.name", equalTo(HEARING_RESULT_AMENDED_EVENT)),

                withJsonPath("$.hearingId", equalTo(hearingId.toString())),
                withJsonPath("$.id", equalTo(amendedResult.getId().toString())),
                withJsonPath("$.lastSharedResultId", equalTo(amendedResult.getLastSharedResultId().toString())),
                withJsonPath("$.sharedTime", is(notNullValue())),
                withJsonPath("$.personId", equalTo(amendedResult.getPersonId().toString())),
                withJsonPath("$.caseId", equalTo(amendedResult.getCaseId().toString())),
                withJsonPath("$.offenceId", equalTo(amendedResult.getOffenceId().toString())),
                withJsonPath("$.level", equalTo(amendedResult.getLevel().name())),
                withJsonPath("$.resultLabel", equalTo(amendedResult.getResultLabel()))
        );

        assertThat(message.prettify(), new IsJson(both(amendedEventExceptResultPrompts).and(withResultPrompts(amendedResult.getPrompts()))));
    }

    private static Matcher<? super ReadContext> withResultPrompts(final List<ResultPrompt> resultPrompts) {
        final List<Matcher<? super ReadContext>> resultPromptsMatcher = newArrayList();

        IntStream.range(0, resultPrompts.size()).forEach(idx -> {
            resultPromptsMatcher.add(withJsonPath(String.format("$.prompts[%s].label", idx), equalTo(resultPrompts.get(idx).getLabel())));
            resultPromptsMatcher.add(withJsonPath(String.format("$.prompts[%s].value", idx), equalTo(resultPrompts.get(idx).getValue())));
        });

        return allOf(resultPromptsMatcher);
    }

    private static JsonObjectBuilder createResultLineFor(final ResultLineData resultLine) {
        final JsonObjectBuilder resultLineJson = createObjectBuilder();
        if (resultLine.getLastSharedResultId() != null) {
            resultLineJson.add(FIELD_LAST_SHARED_RESULT_ID, resultLine.getLastSharedResultId().toString());
        }
        return resultLineJson
                .add(FIELD_GENERIC_ID, resultLine.getId().toString())
                .add(FIELD_PERSON_ID, resultLine.getPersonId().toString())
                .add(FIELD_CASE_ID, resultLine.getCaseId().toString())
                .add(FIELD_OFFENCE_ID, resultLine.getOffenceId().toString())
                .add(FIELD_LEVEL, resultLine.getLevel().name())
                .add(FIELD_RESULT_LABEL, resultLine.getResultLabel())
                .add(FIELD_COURT, resultLine.getCourt())
                .add(FIELD_COURT_ROOM, resultLine.getCourtRoom())
                .add(FIELD_CLERK_OF_THE_COURT_ID, resultLine.getClerkOfTheCourtId().toString())
                .add(FIELD_CLERK_OF_THE_COURT_FIRST_NAME, resultLine.getClerkOfTheCourtFirstName())
                .add(FIELD_CLERK_OF_THE_COURT_LAST_NAME, resultLine.getClerkOfTheCourtLastName())
                .add(FIELD_PROMPTS, resultLine.getPrompts().stream()
                        .map(prompt -> createObjectBuilder().add(FIELD_LABEL, prompt.getLabel()).add(FIELD_VALUE, prompt.getValue()))
                        .collect(Json::createArrayBuilder, JsonArrayBuilder::add, JsonArrayBuilder::add)
                );
    }

    private static JsonObjectBuilder prepareJsonForResultLines(final List<ResultLineData> resultLines) {
        final JsonArrayBuilder resultLinesJson = createArrayBuilder();

        resultLines.forEach(result -> resultLinesJson.add(createResultLineFor(result)));

        return createObjectBuilder().add(FIELD_RESULT_LINES, resultLinesJson);
    }

}
