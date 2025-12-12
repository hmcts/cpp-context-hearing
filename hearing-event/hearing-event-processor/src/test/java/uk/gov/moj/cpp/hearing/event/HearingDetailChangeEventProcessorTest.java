package uk.gov.moj.cpp.hearing.event;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;
import static uk.gov.justice.services.messaging.JsonMetadata.CONTEXT;
import static uk.gov.justice.services.messaging.JsonMetadata.ID;
import static uk.gov.justice.services.messaging.JsonMetadata.NAME;
import static uk.gov.justice.services.messaging.JsonMetadata.USER_ID;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;

import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HearingDetailChangeEventProcessorTest {
    public static final String ARBITRARY_TRIAL = RandomGenerator.STRING.next();
    public static final String ARBITRARY_COURT_NAME = RandomGenerator.STRING.next();
    public static final String ARBITRARY_HEARING_DAY = "2016-06-01T10:00:00Z";
    private static final String PUBLIC_PROGRESSION_EVENT_HEARING_DETAIL_CHANGED = "public.hearing-detail-changed";
    private static final String PRIVATE_HEARING_COMMAND_HEARING_DETAIL_CHANGE = "hearing.change-hearing-detail";
    private static final String ARBITRARY_HEARING_ID = UUID.randomUUID().toString();
    private static final String ARBITRARY_HEARING_COURT_ROOM_ID = UUID.randomUUID().toString();
    private static final String ARBITRARY_HEARING_JUDGE_ID = UUID.randomUUID().toString();
    private static final String ARBITRARY_HEARING_JUDGE_TITLE = RandomGenerator.STRING.next();
    private static final String ARBITRARY_HEARING_JUDGE_FIRST_NAME = RandomGenerator.STRING.next();
    private static final String ARBITRARY_HEARING_JUDGE_LAST_NAME = RandomGenerator.STRING.next();

    @Mock
    private Sender sender;

    @Spy
    private Enveloper enveloper = createEnveloper();

    @Captor
    private ArgumentCaptor<JsonEnvelope> senderJsonEnvelopeCaptor;

    @InjectMocks
    private HearingDetailChangeEventProcessor testObj;

    public static Metadata createMetadataWithUserId(final String id, final String name, final String userId) {
        JsonObject jsonObject = Json.createObjectBuilder()
                .add(ID, id)
                .add(NAME, name)
                .add(CONTEXT, Json.createObjectBuilder()
                        .add(USER_ID, userId).build())
                .build();

        return metadataFrom(jsonObject).build();
    }

    @Test
    public void publishHearingDetailChangedPublicEvent() throws Exception {
        //Given
        final String userId = UUID.randomUUID().toString();
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID(PUBLIC_PROGRESSION_EVENT_HEARING_DETAIL_CHANGED),
                publicHearingChangedEvent());

        //when
        testObj.publishHearingDetailChangedPrivateEvent(event);

        //then
        verify(sender).send(senderJsonEnvelopeCaptor.capture());
        assertThat(senderJsonEnvelopeCaptor.getValue(), is(jsonEnvelope(
                withMetadataEnvelopedFrom(event)
                        .withName(PRIVATE_HEARING_COMMAND_HEARING_DETAIL_CHANGE),
                payloadIsJson(allOf(
                        withJsonPath("$.hearing.id", equalTo(ARBITRARY_HEARING_ID)),
                        withJsonPath("$.hearing.type", equalTo(ARBITRARY_TRIAL)),
                        withJsonPath("$.hearing.courtRoomName", equalTo(ARBITRARY_COURT_NAME)),
                        withJsonPath("$.hearing.courtRoomId", equalTo(ARBITRARY_HEARING_COURT_ROOM_ID)),
                        withJsonPath("$.hearing.hearingDays[0]", equalTo(ARBITRARY_HEARING_DAY)),
                        withJsonPath("$.hearing.judge.id", equalTo(ARBITRARY_HEARING_JUDGE_ID)),
                        withJsonPath("$.hearing.judge.firstName", equalTo(ARBITRARY_HEARING_JUDGE_FIRST_NAME)),
                        withJsonPath("$.hearing.judge.lastName", equalTo(ARBITRARY_HEARING_JUDGE_LAST_NAME)),
                        withJsonPath("$.hearing.judge.title", equalTo(ARBITRARY_HEARING_JUDGE_TITLE))
                ))).thatMatchesSchema()
        ));
    }

    private JsonObject publicHearingChangedEvent() {
        return Json.createObjectBuilder()
                .add("hearing", Json.createObjectBuilder()
                        .add("id", ARBITRARY_HEARING_ID)
                        .add("type", ARBITRARY_TRIAL)
                        .add("judge", getJudge())
                        .add("courtRoomId", ARBITRARY_HEARING_COURT_ROOM_ID)
                        .add("courtRoomName", ARBITRARY_COURT_NAME)
                        .add("hearingDays", Json.createArrayBuilder().add(ARBITRARY_HEARING_DAY).add("2016-07-03T10:15:00Z").build())
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