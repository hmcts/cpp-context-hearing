package uk.gov.moj.cpp.hearing.it;


import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.makeCommand;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.initiateHearingCommandTemplate;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.publicEvents;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.sendMessage;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;

import java.text.MessageFormat;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Test;

public class ChangeHearingDetailIT extends AbstractIT {
    public static final String ARBITRARY_TRIAL = RandomGenerator.STRING.next();
    public static final String ARBITRARY_COURT_ROOM_NAME = RandomGenerator.STRING.next();
    public static final String ARBITRARY_HEARING_DAY = "2016-06-01T10:00:00Z";
    private static final String ARBITRARY_HEARING_COURT_ROOM_ID = UUID.randomUUID().toString();
    private static final String ARBITRARY_HEARING_JUDGE_ID = UUID.randomUUID().toString();
    private static final String ARBITRARY_HEARING_JUDGE_TITLE = RandomGenerator.STRING.next();
    private static final String ARBITRARY_HEARING_JUDGE_FIRST_NAME = RandomGenerator.STRING.next();
    private static final String ARBITRARY_HEARING_JUDGE_LAST_NAME = RandomGenerator.STRING.next();

    @Test
    public void should_update_hearing_when_public_hearing_detail_changed_event_received() {

        //Given Hearing is already created
        final String hearingId = createHearing().getId().toString();
        //new hearing changes payload
        JsonObject hearingChangeDetails = publicHearingChangedEvent(hearingId);


        //When hearing received  'public.hearing-detail-changed' should apply changes accordingly
        sendPublicHearingDetailChangedNotification(hearingChangeDetails);


        //then
        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"), hearingId);
        final String url = getBaseUri() + "/" + queryAPIEndPoint;

        final String responseType = "application/vnd.hearing.get.hearing.v2+json";

        poll(requestParams(url, responseType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(hearingId)),
                                withJsonPath("$.hearingType", is(ARBITRARY_TRIAL)),
                                withJsonPath("$.roomName", is(ARBITRARY_COURT_ROOM_NAME)),
                                withJsonPath("$.roomId", is(ARBITRARY_HEARING_COURT_ROOM_ID.toString())),
                                withJsonPath("$.judge.id", is(ARBITRARY_HEARING_JUDGE_ID.toString())),
                                withJsonPath("$.judge.title", is(ARBITRARY_HEARING_JUDGE_TITLE)),
                                withJsonPath("$.judge.firstName", is(ARBITRARY_HEARING_JUDGE_FIRST_NAME)),
                                withJsonPath("$.judge.lastName", is(ARBITRARY_HEARING_JUDGE_LAST_NAME)),
                                withJsonPath("$.hearingDays[0]", is(ISO_INSTANT.format(ZonedDateTimes.fromString(ARBITRARY_HEARING_DAY))))

                        )));

    }

    private void sendPublicHearingDetailChangedNotification(final JsonObject jsonObject) {
        final String eventName = "public.hearing-detail-changed";


        sendMessage(publicEvents.createProducer(),
                eventName,
                jsonObject,
                metadataOf(randomUUID(), eventName)
                        .withUserId(randomUUID().toString())
                        .build()
        );
    }

    private Hearing createHearing() {
        {

            InitiateHearingCommand initiateHearing = initiateHearingCommandTemplate().build();

            final Hearing hearing = initiateHearing.getHearing();

            TestUtilities.EventListener publicEventTopic = listenFor("public.hearing.initiated")
                    .withFilter(isJson(withJsonPath("$.hearingId", is(hearing.getId().toString()))));

            makeCommand(requestSpec, "hearing.initiate")
                    .ofType("application/vnd.hearing.initiate+json")
                    .withPayload(initiateHearing)
                    .executeSuccessfully();

            publicEventTopic.waitFor();

            final String queryAPIEndPoint = MessageFormat
                    .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"), initiateHearing.getHearing().getId());
            final String url = getBaseUri() + "/" + queryAPIEndPoint;

            final String responseType = "application/vnd.hearing.get.hearing.v2+json";

            poll(requestParams(url, responseType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                    .until(status().is(OK),
                            print(),
                            payload().isJson(allOf(
                                    withJsonPath("$.hearingId", is(initiateHearing.getHearing().getId().toString()))

                            )));

            return hearing;
        }

    }


    private JsonObject publicHearingChangedEvent(final String hearingId) {
        return Json.createObjectBuilder()
                .add("hearing", Json.createObjectBuilder()
                        .add("id", hearingId)
                        .add("type", ARBITRARY_TRIAL)
                        .add("judge", getJudge())
                        .add("courtRoomId", ARBITRARY_HEARING_COURT_ROOM_ID)
                        .add("courtRoomName", ARBITRARY_COURT_ROOM_NAME)
                        .add("hearingDays", Json.createArrayBuilder().add(ARBITRARY_HEARING_DAY).build())
                        .build())
                .build();
    }

    private JsonObject getJudge() {
        final JsonObject judgeJsonObject = Json.createObjectBuilder()
                .add("id", ARBITRARY_HEARING_JUDGE_ID)
                .add("firstName", ARBITRARY_HEARING_JUDGE_FIRST_NAME)
                .add("lastName", ARBITRARY_HEARING_JUDGE_LAST_NAME)
                .add("title", ARBITRARY_HEARING_JUDGE_TITLE)
                .build();
        return judgeJsonObject;
    }

}
