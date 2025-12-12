package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static io.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingWith2Defendants;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.getPublicTopicInstance;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.sendMessage;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.poll;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.http.HeaderConstants;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForNewOffence;
import uk.gov.moj.cpp.hearing.domain.event.PublicSelectedOffencesRemovedFromExistingHearing;
import uk.gov.moj.cpp.hearing.domain.event.RemoveOffencesFromExistingHearing;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.json.JsonObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class RemoveOffencesFromExistingHearingIT extends AbstractIT {

    private static final String REMOVE_OFFENCES_FROM_EXISTING_HEARING_COMMAND_ENDPOINT = ENDPOINT_PROPERTIES.getProperty("hearing.command.remove-offences-from-existing-hearing");
    private static final String REMOVE_OFFENCES_FROM_EXISTING_HEARING_COMMAND_MEDIA_TYPE = "application/vnd.hearing.remove-offences-from-existing-hearing+json";
    private static final String GET_HEARING_QUERY_ENDPOINT_NAME = "hearing.get.hearing";
    private static final String GET_HEARING_QUERY_ENDPOINT_MEDIA_TYPE = "application/vnd.hearing.get.hearing+json";
    public static final String PUBLIC_PROGRESSION_OFFENCES_REMOVED_FROM_EXISTING_ALLOCATED_HEARING = "public.progression.offences-removed-from-existing-allocated-hearing";
    public static final String PUBLIC_EVENTS_LISTING_OFFENCES_REMOVED_FROM_EXISTING_ALLOCATED_HEARING = "public.events.listing.offences-removed-from-existing-allocated-hearing";
    private final String publicProgressionDefendantOffencesChanged = "public.progression.defendant-offences-changed";

    UUID defendantId1;
    UUID defendantId2;
    UUID offenceId1;
    UUID offenceId2;
    UUID offenceId3;
    UUID prosecutionCaseId;

    @BeforeEach
    public void setUp() {
        defendantId1 = UUID.randomUUID();
        defendantId2 = UUID.randomUUID();
        offenceId1 = UUID.randomUUID();
        offenceId2 = UUID.randomUUID();
        offenceId3 = UUID.randomUUID();
        prosecutionCaseId = UUID.randomUUID();

    }

    @Test
    public void shouldRemove1OffenceOutOf2FromDefendant1ForExistingHearing() {


        final UUID hearingId = initiateHearingAndAssertDefendantAndOffencesInHearing(prosecutionCaseId, defendantId1, defendantId2, offenceId1, offenceId2, offenceId3);

        try (final Utilities.EventListener publicEventResultedListener = listenFor("public.hearing.selected-offences-removed-from-existing-hearing")
                .withFilter(convertStringTo(PublicSelectedOffencesRemovedFromExistingHearing.class, isBean(PublicSelectedOffencesRemovedFromExistingHearing.class)
                        .with(PublicSelectedOffencesRemovedFromExistingHearing::getHearingId, Matchers.is(hearingId))))) {

            commandRemoveOffencesFromExistingHearing(hearingId, singletonList(offenceId1));

            final JsonPath result = publicEventResultedListener.waitFor();
            assertThat(result.getString("offenceIds[0]"), Matchers.is(offenceId1.toString()));

        }


        poll(requestParams(getURL(GET_HEARING_QUERY_ENDPOINT_NAME, hearingId), GET_HEARING_QUERY_ENDPOINT_MEDIA_TYPE)
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(
                                anyOf(allOf(
                                                withJsonPath("$.hearing.prosecutionCases[0].defendants", hasSize(2)),
                                                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].id", is(defendantId1.toString())),
                                                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].offences", hasSize(1)),
                                                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].offences[0].id", is(offenceId2.toString())),
                                                withJsonPath("$.hearing.prosecutionCases[0].defendants[1].id", is(defendantId2.toString())),
                                                withJsonPath("$.hearing.prosecutionCases[0].defendants[1].offences", hasSize(1)),
                                                withJsonPath("$.hearing.prosecutionCases[0].defendants[1].offences[0].id", is(offenceId3.toString()))),

                                        allOf(
                                                withJsonPath("$.hearing.prosecutionCases[0].defendants", hasSize(2)),
                                                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].id", is(defendantId2.toString())),
                                                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].offences", hasSize(1)),
                                                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].offences[0].id", is(offenceId3.toString()))),
                                        withJsonPath("$.hearing.prosecutionCases[0].defendants[1].id", is(defendantId1.toString())),
                                        withJsonPath("$.hearing.prosecutionCases[0].defendants[1].offences", hasSize(1)),
                                        withJsonPath("$.hearing.prosecutionCases[0].defendants[1].offences[0].id", is(offenceId1.toString())))
                        ));
    }

    @Test
    public void shouldRemoveBoth2OffencesFrom1DefendantOutOf2FromExistingHearing() throws IOException {


        final UUID hearingId = initiateHearingAndAssertDefendantAndOffencesInHearing(prosecutionCaseId, defendantId1, defendantId2, offenceId1, offenceId2, offenceId3);

        commandRemoveOffencesFromExistingHearing(hearingId, asList(offenceId1, offenceId2));

        poll(requestParams(getURL(GET_HEARING_QUERY_ENDPOINT_NAME, hearingId), GET_HEARING_QUERY_ENDPOINT_MEDIA_TYPE)
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.prosecutionCases[0].defendants", hasSize(1)),
                                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].id", is(defendantId2.toString())),
                                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].offences", hasSize(1)),
                                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].offences[0].id", is(offenceId3.toString())))
                        ));

        // public.progression.defendant-offences-changed
        final UUID offenceId4 = UUID.randomUUID();

        try (final Utilities.EventListener foundHearingsForNewOffence = listenFor("hearing.events.found-hearings-for-new-offence-v2", "hearing.event")
                .withFilter(convertStringTo(FoundHearingsForNewOffence.class, isBean(FoundHearingsForNewOffence.class)
                        .with(FoundHearingsForNewOffence::getDefendantId, Matchers.is(defendantId1))
                ))) {


            final String eventPayloadForDefendantOffencesChanged = getStringFromResource("public.progression.defendant-offences-changed.json")
                    .replaceAll("CASE_ID", prosecutionCaseId.toString())
                    .replaceAll("DEFENDANT_ID", defendantId1.toString())
                    .replaceAll("OFFENCE_ID", offenceId4.toString());


            sendMessage(getPublicTopicInstance().createProducer(),
                    publicProgressionDefendantOffencesChanged,
                    new StringToJsonObjectConverter().convert(eventPayloadForDefendantOffencesChanged),
                    metadataOf(randomUUID(), publicProgressionDefendantOffencesChanged)
                            .withUserId(randomUUID().toString())
                            .build()
            );

            foundHearingsForNewOffence.expectNone();

        }
    }

    @Test
    public void shouldRemoveBoth2OffencesFrom1DefendantOutOf2FromExistingHearingWhenPublicEventCatch() throws JsonProcessingException {


        final UUID hearingId = initiateHearingAndAssertDefendantAndOffencesInHearing(prosecutionCaseId, defendantId1, defendantId2, offenceId1, offenceId2, offenceId3);

        publicEventRemoveOffencesFromExistingHearing(hearingId, asList(offenceId1, offenceId2));

        poll(requestParams(getURL(GET_HEARING_QUERY_ENDPOINT_NAME, hearingId), GET_HEARING_QUERY_ENDPOINT_MEDIA_TYPE)
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.prosecutionCases[0].defendants", hasSize(1)),
                                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].id", is(defendantId2.toString())),
                                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].offences", hasSize(1)),
                                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].offences[0].id", is(offenceId3.toString())))
                        ));
    }

    @Test
    public void shouldRemove1OffenceForEachDefendantFromExistingHearing() {

        final UUID hearingId = initiateHearingAndAssertDefendantAndOffencesInHearing(prosecutionCaseId, defendantId1, defendantId2, offenceId1, offenceId2, offenceId3);

        commandRemoveOffencesFromExistingHearing(hearingId, asList(offenceId2, offenceId3));

        poll(requestParams(getURL(GET_HEARING_QUERY_ENDPOINT_NAME, hearingId), GET_HEARING_QUERY_ENDPOINT_MEDIA_TYPE)
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                        withJsonPath("$.hearing.prosecutionCases[0].defendants", hasSize(1)),
                                        withJsonPath("$.hearing.prosecutionCases[0].defendants[0].id", is(defendantId1.toString())),
                                        withJsonPath("$.hearing.prosecutionCases[0].defendants[0].offences", hasSize(1)),
                                        withJsonPath("$.hearing.prosecutionCases[0].defendants[0].offences[0].id", is(offenceId1.toString()))
                                )
                        ));
    }

    @Test
    public void shouldNotCallListingContextIfSourceIsListingForRemoveFlow() throws JsonProcessingException {

        final UUID hearingId = initiateHearingAndAssertDefendantAndOffencesInHearing(prosecutionCaseId, defendantId1, defendantId2, offenceId1, offenceId2, offenceId3);

        publicEventRemoveOffencesFromExistingAllocatedHearing(hearingId, singletonList(offenceId1));

        poll(requestParams(getURL(GET_HEARING_QUERY_ENDPOINT_NAME, hearingId), GET_HEARING_QUERY_ENDPOINT_MEDIA_TYPE)
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(
                                anyOf(allOf(
                                                withJsonPath("$.hearing.prosecutionCases[0].defendants", hasSize(2)),
                                                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].id", is(defendantId1.toString())),
                                                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].offences", hasSize(1)),
                                                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].offences[0].id", is(offenceId2.toString())),
                                                withJsonPath("$.hearing.prosecutionCases[0].defendants[1].id", is(defendantId2.toString())),
                                                withJsonPath("$.hearing.prosecutionCases[0].defendants[1].offences", hasSize(1)),
                                                withJsonPath("$.hearing.prosecutionCases[0].defendants[1].offences[0].id", is(offenceId3.toString()))),

                                        allOf(
                                                withJsonPath("$.hearing.prosecutionCases[0].defendants", hasSize(2)),
                                                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].id", is(defendantId2.toString())),
                                                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].offences", hasSize(1)),
                                                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].offences[0].id", is(offenceId3.toString()))),
                                        withJsonPath("$.hearing.prosecutionCases[0].defendants[1].id", is(defendantId1.toString())),
                                        withJsonPath("$.hearing.prosecutionCases[0].defendants[1].offences", hasSize(1)),
                                        withJsonPath("$.hearing.prosecutionCases[0].defendants[1].offences[0].id", is(offenceId1.toString())))
                        ));
    }

    private void commandRemoveOffencesFromExistingHearing(final UUID hearingId, final List<UUID> uuids) {
        final Response response = given().spec(getRequestSpec())
                .and().contentType(REMOVE_OFFENCES_FROM_EXISTING_HEARING_COMMAND_MEDIA_TYPE)
                .and().header(USER_ID, getLoggedInUser())
                .and().body(new RemoveOffencesFromExistingHearing(hearingId, uuids, null))
                .when().post(REMOVE_OFFENCES_FROM_EXISTING_HEARING_COMMAND_ENDPOINT)
                .then().extract().response();

        assertThat(response.getStatusCode(), equalTo(SC_ACCEPTED));
    }

    private void publicEventRemoveOffencesFromExistingHearing(final UUID hearingId, final List<UUID> uuids) throws JsonProcessingException {
        JsonObject commandJson = Utilities.JsonUtil.objectToJsonObject(new RemoveOffencesFromExistingHearing(hearingId, uuids, null));
        sendMessage(getPublicTopicInstance().createProducer(),
                PUBLIC_PROGRESSION_OFFENCES_REMOVED_FROM_EXISTING_ALLOCATED_HEARING,
                commandJson,
                metadataOf(randomUUID(), PUBLIC_PROGRESSION_OFFENCES_REMOVED_FROM_EXISTING_ALLOCATED_HEARING)
                        .withUserId(randomUUID().toString())
                        .build()
        );
    }

    private void publicEventRemoveOffencesFromExistingAllocatedHearing(final UUID hearingId, final List<UUID> uuids) throws JsonProcessingException {
        JsonObject commandJson = Utilities.JsonUtil.objectToJsonObject(new RemoveOffencesFromExistingHearing(hearingId, uuids, true));
        sendMessage(getPublicTopicInstance().createProducer(),
                PUBLIC_EVENTS_LISTING_OFFENCES_REMOVED_FROM_EXISTING_ALLOCATED_HEARING,
                commandJson,
                metadataOf(randomUUID(), PUBLIC_EVENTS_LISTING_OFFENCES_REMOVED_FROM_EXISTING_ALLOCATED_HEARING)
                        .withUserId(randomUUID().toString())
                        .build()
        );
    }

    private UUID initiateHearingAndAssertDefendantAndOffencesInHearing(final UUID prosecutionCaseId, final UUID defendantId1, final UUID defendantId2, final UUID offenceId1, final UUID offenceId2, final UUID offenceId3) {
        final InitiateHearingCommand initiateHearingCommand = initiateHearingWith2Defendants(prosecutionCaseId, defendantId1, offenceId1, offenceId2, defendantId2, offenceId3);

        final UUID hearingId = initiateHearingCommand.getHearing().getId();

        initiateHearing(getRequestSpec(), initiateHearingCommand);

        poll(requestParams(getURL(GET_HEARING_QUERY_ENDPOINT_NAME, hearingId), GET_HEARING_QUERY_ENDPOINT_MEDIA_TYPE)
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(
                                anyOf(allOf(withJsonPath("$.hearing.prosecutionCases[0].defendants[0].id", is(defendantId1.toString())),
                                                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].offences", hasSize(2)),
                                                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].offences[0].id", anyOf(is(offenceId1.toString()), is(offenceId2.toString()))),
                                                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].offences[1].id", anyOf(is(offenceId1.toString()), is(offenceId2.toString()))),
                                                withJsonPath("$.hearing.prosecutionCases[0].defendants[1].id", is(defendantId2.toString())),
                                                withJsonPath("$.hearing.prosecutionCases[0].defendants[1].offences", hasSize(1)),
                                                withJsonPath("$.hearing.prosecutionCases[0].defendants[1].offences[0].id", is(offenceId3.toString()))),
                                        allOf(withJsonPath("$.hearing.prosecutionCases[0].defendants[0].id", is(defendantId2.toString())),
                                                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].offences", hasSize(1)),
                                                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].offences[0].id", is(offenceId3.toString()))),
                                        withJsonPath("$.hearing.prosecutionCases[0].defendants[1].id", is(defendantId1.toString())),
                                        withJsonPath("$.hearing.prosecutionCases[0].defendants[1].offences", hasSize(2)),
                                        withJsonPath("$.hearing.prosecutionCases[0].defendants[1].offences[0].id", anyOf(is(offenceId1.toString()), is(offenceId2.toString()))),
                                        withJsonPath("$.hearing.prosecutionCases[0].defendants[1].offences[1].id", anyOf(is(offenceId1.toString()), is(offenceId2.toString())))
                                )));
        return hearingId;
    }

}
