package uk.gov.moj.cpp.hearing.steps;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static java.text.MessageFormat.format;
import static java.util.UUID.fromString;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.publicEvents;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.retrieveMessage;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.mockProgressionCaseDetails;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsAuthorisedUser;

import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.jms.MessageConsumer;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.Response;

import org.hamcrest.Matcher;

import com.jayway.jsonpath.matchers.IsJson;
import com.jayway.restassured.path.json.JsonPath;

import uk.gov.justice.services.test.utils.core.messaging.MessageProducerClient;
import uk.gov.moj.cpp.hearing.it.AbstractIT;
import uk.gov.moj.cpp.hearing.steps.data.DefenceCounselData;

@SuppressWarnings("unchecked")
public class HearingStepDefinitions extends AbstractIT {

    private static final String PUBLIC_EVENT_HEARING_CONFIRMED = "public.hearing-confirmed";
    private static final String PUBLIC_EVENT_HEARING_PLEA_UPDATED = "public.hearing.plea-updated";
    private static final String PUBLIC_EVENT_HEARING_UPDATE_PLEA_IGNORED = "public.hearing.update-plea-ignored";
    private static final String PUBLIC_EVENT_HEARING_VERDICT_UPDATED = "public.hearing.verdict-updated";
    private static final String PUBLIC_EVENT_HEARING_UPDATE_VERDICT_IGNORED = "public.hearing.update-verdict-ignored";

    private static final MessageConsumer CONSUMER_FOR_PUBLIC_EVENT_HEARING_PLEA_UPDATED = publicEvents.createConsumer(PUBLIC_EVENT_HEARING_PLEA_UPDATED);
    private static final MessageConsumer CONSUMER_FOR_PUBLIC_EVENT_HEARING_UPDATE_PLEA_IGNORED = publicEvents.createConsumer(PUBLIC_EVENT_HEARING_UPDATE_PLEA_IGNORED);
    private static final MessageConsumer CONSUMER_FOR_PUBLIC_EVENT_HEARING_VERDICT_UPDATED = publicEvents.createConsumer(PUBLIC_EVENT_HEARING_VERDICT_UPDATED);
    private static final MessageConsumer CONSUMER_FOR_PUBLIC_EVENT_HEARING_UPDATE_VERDICT_IGNORED = publicEvents.createConsumer(PUBLIC_EVENT_HEARING_UPDATE_VERDICT_IGNORED);

    private static final String MEDIA_TYPE_GET_HEARING_DETAILS = "application/vnd.hearing.get.hearing+json";
    private static final String MEDIA_TYPE_ADD_DEFENCE_COUNSEL = "application/vnd.hearing.add-defence-counsel+json";
    private static final String MEDIA_TYPE_SHARE_RESULTS = "application/vnd.hearing.share-results+json";

    private static final String FIELD_GENERIC_ID = "id";
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

    public static void andHearingHasBeenConfirmed(final JsonObject hearingConfirmed) {
        final UUID hearingId = fromString(hearingConfirmed.getJsonObject("hearing").getString("id"));

        try (final MessageProducerClient messageProducer = new MessageProducerClient()) {
            messageProducer.startProducer(PUBLIC_EVENT_TOPIC);

            messageProducer.sendMessage(PUBLIC_EVENT_HEARING_CONFIRMED, hearingConfirmed);
        }

        poll(requestParams(getQueryForHearingDetailsUrl(hearingId), MEDIA_TYPE_GET_HEARING_DETAILS)
                .withHeader(USER_ID, getLoggedInUser()))
                .timeout(30, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(hearingId.toString())),
                                withJsonPath("$.caseIds[0]", is(hearingConfirmed.getString("caseId")))
                        )));
    }

    public static void andHearingHasNotBeenConfirmed(final UUID hearingId) {
        final String queryHearingDetailsUrl = getQueryForHearingDetailsUrl(hearingId);

        poll(requestParams(queryHearingDetailsUrl, MEDIA_TYPE_GET_HEARING_DETAILS)
                .withHeader(USER_ID, getLoggedInUser()))
                .until(
                        status().is(OK),
                        payload().isJson(allOf(
                                withoutJsonPath("$.hearingId")
                        )));
    }

    public static void andProgressionCaseDetailsAreAvailable(final UUID caseId, final String caseUrn) {
        mockProgressionCaseDetails(caseId, caseUrn);
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

    public static void thenHearingPleaUpdatedPublicEventShouldBePublished(final String caseId) {
        final JsonPath message = retrieveMessage(CONSUMER_FOR_PUBLIC_EVENT_HEARING_PLEA_UPDATED);

        assertThat(message.prettify(), new IsJson(anyOf(
                withJsonPath("$._metadata.name", equalTo(PUBLIC_EVENT_HEARING_PLEA_UPDATED)),

                withJsonPath("$.caseId", equalTo(caseId))
        )));
    }

    public static void thenHearingUpdatePleaIgnoredPublicEventShouldBePublished(final String caseId) {
        final JsonPath message = retrieveMessage(CONSUMER_FOR_PUBLIC_EVENT_HEARING_UPDATE_PLEA_IGNORED);

        assertThat(message.prettify(), new IsJson(allOf(
                withJsonPath("$._metadata.name", equalTo(PUBLIC_EVENT_HEARING_UPDATE_PLEA_IGNORED)),

                withJsonPath("$.caseId", equalTo(caseId))
        )));
    }

    public static void thenHearingVerdictUpdatedPublicEventShouldBePublished(final String hearingId) {
        JsonPath message = retrieveMessage(CONSUMER_FOR_PUBLIC_EVENT_HEARING_VERDICT_UPDATED);

        Matcher matcher = isJson(allOf(
                withJsonPath("$._metadata.name", equalTo(PUBLIC_EVENT_HEARING_VERDICT_UPDATED)),
                withJsonPath("$.hearingId", equalTo(hearingId))
        ));

        while (message != null && !matcher.matches(message.prettify())) {
            message = retrieveMessage(CONSUMER_FOR_PUBLIC_EVENT_HEARING_VERDICT_UPDATED);
        }

        assertThat(message, is(not(nullValue())));
    }

    public static void thenHearingUpdateVerdictIgnoredPublicEventShouldBePublished(final String hearingId) {
        final JsonPath message = retrieveMessage(CONSUMER_FOR_PUBLIC_EVENT_HEARING_UPDATE_VERDICT_IGNORED);

        assertThat(message.prettify(), new IsJson(allOf(
                withJsonPath("$._metadata.name", equalTo(PUBLIC_EVENT_HEARING_UPDATE_VERDICT_IGNORED)),

                withJsonPath("$.hearingId", equalTo(hearingId))
        )));
    }

    private static String getQueryForHearingDetailsUrl(final UUID hearingId) {
        final String queryEventLogEndPoint = MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing"), hearingId);
        return String.format("%s/%s", getBaseUri(), queryEventLogEndPoint);
    }
}
