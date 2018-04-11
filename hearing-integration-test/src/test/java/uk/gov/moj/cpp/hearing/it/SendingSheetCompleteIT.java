package uk.gov.moj.cpp.hearing.it;

import com.jayway.awaitility.core.ConditionTimeoutException;
import com.jayway.restassured.path.json.JsonPath;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.justice.services.messaging.Metadata;

import javax.jms.MessageProducer;
import javax.json.JsonObject;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.publicEvents;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.sendMessage;

public class SendingSheetCompleteIT extends AbstractIT {

    private static final int EXPECTED_DEFAULT_HEARING_LENGTH = 15;

    @Test
    public void processSendingSheetComplete_shouldProduceNoHearings_givenNoneGuilty() throws IOException {

        final String eventName = "public.progression.events.sending-sheet-completed";

        final UUID caseID = randomUUID();

        final String eventPayloadString = getStringFromResource(eventName + ".noguilty.json").replaceAll("CASE_ID", caseID.toString());

        TestUtilities.EventListener publicEventTopic = listenFor("public.hearing.initiated")
                .withFilter(isJson(withJsonPath("$.cases.[0]", is(caseID))));

        sendMessage(publicEvents.createProducer(),
                eventName,
                new StringToJsonObjectConverter().convert(eventPayloadString),
                JsonObjectMetadata.metadataOf(randomUUID(), eventName)
                        .withUserId(randomUUID().toString())
                        .build()
        );

        publicEventTopic.expectNoneWithin(10000);
    }

    @Test
    public void processSendingSheetComplete_shouldProduceHearings_givenGuiltyPleas() throws IOException {

        final String eventName = "public.progression.events.sending-sheet-completed";

        UUID caseID = randomUUID();
        UUID pleaId = randomUUID();
        UUID courtCentreId = randomUUID();
        UUID offenceId = randomUUID();


        String eventPayloadString = getStringFromResource(eventName + ".json")
                .replaceAll("CASE_ID", caseID.toString())
                .replaceAll("PLEA_ID", pleaId.toString())
                .replaceAll("OFFENCE_ID_1", offenceId.toString())
                .replaceAll("COURT_CENTRE_ID", courtCentreId.toString());


        TestUtilities.EventListener publicEventTopic = listenFor("public.hearing.initiated")
                .withFilter(isJson(withJsonPath("$.cases.[0]", is(caseID.toString()))));

        sendMessage(publicEvents.createProducer(),
                eventName,
                new StringToJsonObjectConverter().convert(eventPayloadString),
                JsonObjectMetadata.metadataOf(randomUUID(), eventName)
                        .withUserId(randomUUID().toString())
                        .build()
        );

        JsonPath initiatedMessage = publicEventTopic.waitFor();
        String hearingId = initiatedMessage.getString("hearingId");


        final String queryAPIEndPoint = MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"), hearingId);
        final String url = getBaseUri() + "/" + queryAPIEndPoint;
        poll(requestParams(url, "application/vnd.hearing.get.hearing.v2+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.cases[0].caseId", is(caseID.toString()))
                        )));

    }


    @Test
    public void processSendingSheetComplete_shouldProduceHearings_given3GuiltyPleas() throws IOException {

        final String eventName = "public.progression.events.sending-sheet-completed";

        UUID caseID = randomUUID();
        UUID pleaId = randomUUID();
        UUID courtCentreId = randomUUID();

        final List<UUID> offenceIDs = new ArrayList<>();
        final List<UUID> pleaIDs = new ArrayList<>();

        String eventPayloadString = getStringFromResource(eventName + ".partialguilty.json")
                .replaceAll("CASE_ID", caseID.toString())
                .replaceAll("COURT_CENTRE_ID", courtCentreId.toString());

        for (int done = 0; done <= 4; done++) {
            final UUID offenceID = randomUUID();
            final UUID pleaID = randomUUID();
            offenceIDs.add(offenceID);
            pleaIDs.add(pleaID);
            eventPayloadString = eventPayloadString.replaceAll("OFFENCE_ID_" + done, offenceID.toString());
            eventPayloadString = eventPayloadString.replaceAll("PLEA_ID_" + done, pleaID.toString());
        }

        System.out.println(eventPayloadString);

        TestUtilities.EventListener publicEventTopic = listenFor("public.hearing.initiated")
                .withFilter(isJson(withJsonPath("$.cases.[0]", is(caseID.toString()))));

        sendMessage(publicEvents.createProducer(),
                eventName,
                new StringToJsonObjectConverter().convert(eventPayloadString),
                JsonObjectMetadata.metadataOf(randomUUID(), eventName)
                        .withUserId(randomUUID().toString())
                        .build()
        );

        IntStream.range(0, 3).forEach((i) -> {
            JsonPath initiatedMessage = publicEventTopic.waitFor();
            String hearingId = initiatedMessage.getString("hearingId");

            final String queryAPIEndPoint = MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"), hearingId);
            final String url = getBaseUri() + "/" + queryAPIEndPoint;
            poll(requestParams(url, "application/vnd.hearing.get.hearing.v2+json")
                    .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                    .until(status().is(OK),
                            print(),
                            payload().isJson(allOf(
                                    withJsonPath("$.cases[0].caseId", is(caseID.toString()))
                            )));
        });
    }
}
