package uk.gov.moj.cpp.hearing.event.listener;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ChangeHearingDetailCommandHandlerTest {
    public static final String ARBITRARY_TRIAL = RandomGenerator.STRING.next();
    public static final String ARBITRARY_COURT_NAME = RandomGenerator.STRING.next();
    public static final String ARBITRARY_HEARING_DAY = "2016-06-01T10:00:00Z";
    private static final String ARBITRARY_HEARING_ID = UUID.randomUUID().toString();
    private static final String ARBITRARY_HEARING_COURT_ROOM_ID = UUID.randomUUID().toString();
    private static final String ARBITRARY_HEARING_JUDGE_ID = UUID.randomUUID().toString();
    private static final String ARBITRARY_HEARING_JUDGE_TITLE = RandomGenerator.STRING.next();
    private static final String ARBITRARY_HEARING_JUDGE_FIRST_NAME = RandomGenerator.STRING.next();
    private static final String ARBITRARY_HEARING_JUDGE_LAST_NAME = RandomGenerator.STRING.next();
    @Mock
    private HearingRepository hearingRepository;

    @InjectMocks
    private ChangeHearingDetailCommandHandler changeHearingDetailCommandHandler;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Captor
    private ArgumentCaptor<Hearing> ahearingArgumentCaptor;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void hearingDetailChanged() throws Exception {

        //Given
        Hearing hearing = Hearing.builder()
                .withId(UUID.fromString(ARBITRARY_HEARING_ID)).build();


        when(this.hearingRepository.findById(UUID.fromString(ARBITRARY_HEARING_ID))).thenReturn(hearing);
        JsonObject hearingChangedPayload = publicHearingChangedEvent();

        //When
        changeHearingDetailCommandHandler.hearingDetailChanged(envelopeFrom(metadataWithRandomUUID("hearing.event.detail-changed"),
                hearingChangedPayload));

        //then
        verify(this.hearingRepository).save(ahearingArgumentCaptor.capture());

        Hearing toBePersisted = ahearingArgumentCaptor.getValue();
        assertThat(hearingChangedPayload.getString("id"), equalTo(toBePersisted.getId().toString()));
        assertThat(hearingChangedPayload.getString("type"), equalTo(toBePersisted.getHearingType()));
        assertThat(hearingChangedPayload.getString("courtRoomName"), equalTo(toBePersisted.getRoomName()));
        assertThat(hearingChangedPayload.getString("courtRoomId"), equalTo(toBePersisted.getRoomId().toString()));
        assertThat(ZonedDateTimes.toString(ZonedDateTimes.fromString(ARBITRARY_HEARING_DAY)), equalTo(ZonedDateTimes.toString(toBePersisted.getHearingDays().get(0).getDateTime())));
        JsonObject judgeJson = hearingChangedPayload.getJsonObject("judge");
        assertThat(judgeJson.getString("title"), equalTo(toBePersisted.getAttendees().get(0).getTitle()));
        assertThat(judgeJson.getString("firstName"), equalTo(toBePersisted.getAttendees().get(0).getFirstName()));
        assertThat(judgeJson.getString("lastName"), equalTo(toBePersisted.getAttendees().get(0).getLastName()));

    }

    private JsonObject publicHearingChangedEvent() {
        return Json.createObjectBuilder()
                .add("id", ARBITRARY_HEARING_ID)
                .add("type", ARBITRARY_TRIAL)
                .add("judge", getJudge())
                .add("courtRoomId", ARBITRARY_HEARING_COURT_ROOM_ID)
                .add("courtRoomName", ARBITRARY_COURT_NAME)
                .add("hearingDays", Json.createArrayBuilder().add(ARBITRARY_HEARING_DAY).build())
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