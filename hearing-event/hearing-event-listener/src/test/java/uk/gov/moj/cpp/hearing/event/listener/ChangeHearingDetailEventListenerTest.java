package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;
import uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingType;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class ChangeHearingDetailEventListenerTest {
    private static final String ARBITRARY_TRIAL = RandomGenerator.STRING.next();
    private static final String ARBITRARY_COURT_NAME = RandomGenerator.STRING.next();
    private static final String ARBITRARY_HEARING_DAY = "2016-06-01T10:00:00Z";
    private static final String ARBITRARY_HEARING_ID = randomUUID().toString();
    private static final String ARBITRARY_HEARING_COURT_ROOM_ID = randomUUID().toString();
    private static final String ARBITRARY_HEARING_JUDGE_ID = randomUUID().toString();
    private static final String ARBITRARY_HEARING_JUDGE_TITLE = RandomGenerator.STRING.next();
    private static final String ARBITRARY_HEARING_JUDGE_FIRST_NAME = RandomGenerator.STRING.next();
    private static final String ARBITRARY_HEARING_JUDGE_LAST_NAME = RandomGenerator.STRING.next();
    @Mock
    private HearingRepository hearingRepository;

    @InjectMocks
    private ChangeHearingDetailEventListener changeHearingDetailEventListener;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Captor
    private ArgumentCaptor<Hearing> ahearingArgumentCaptor;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void hearingDetailChanged() {

        //Given
        final CourtCentre courtCentre = new CourtCentre();
        courtCentre.setWelshRoomName(STRING.next());
        courtCentre.setWelshName(STRING.next());
        courtCentre.setRoomName(STRING.next());
        courtCentre.setRoomId(randomUUID());
        courtCentre.setName(STRING.next());
        courtCentre.setId(randomUUID());

        final HearingType type = new HearingType();
        type.setId(randomUUID());
        type.setDescription(STRING.next());

        final Hearing hearing = new Hearing();
        hearing.setId(UUID.fromString(ARBITRARY_HEARING_ID));
        hearing.setCourtCentre(courtCentre);
        hearing.setHearingType(type);

        when(this.hearingRepository.findBy(UUID.fromString(ARBITRARY_HEARING_ID))).thenReturn(hearing);

        final JsonObject hearingChangedPayload = publicHearingChangedEvent();

        //When
        changeHearingDetailEventListener.hearingDetailChanged(envelopeFrom(metadataWithRandomUUID("hearing.event.detail-changed"),
                hearingChangedPayload));

        //then
        verify(this.hearingRepository).save(ahearingArgumentCaptor.capture());

        final Hearing toBePersisted = ahearingArgumentCaptor.getValue();

        assertThat(hearingChangedPayload.getString("id"), equalTo(toBePersisted.getId().toString()));
        assertThat(hearingChangedPayload.getString("type"), equalTo(toBePersisted.getHearingType().getDescription()));
        assertThat(hearingChangedPayload.getString("courtRoomName"), equalTo(toBePersisted.getCourtCentre().getRoomName()));
        assertThat(hearingChangedPayload.getString("courtRoomId"), equalTo(toBePersisted.getCourtCentre().getRoomId().toString()));
        assertThat(ZonedDateTimes.toString(ZonedDateTimes.fromString(ARBITRARY_HEARING_DAY)), equalTo(ZonedDateTimes.toString(toBePersisted.getHearingDays().iterator().next().getSittingDay())));
        JsonObject judgeJson = hearingChangedPayload.getJsonObject("judge");
        assertThat(judgeJson.getString("title"), equalTo(toBePersisted.getJudicialRoles().iterator().next().getTitle()));
        assertThat(judgeJson.getString("firstName"), equalTo(toBePersisted.getJudicialRoles().iterator().next().getFirstName()));
        assertThat(judgeJson.getString("lastName"), equalTo(toBePersisted.getJudicialRoles().iterator().next().getLastName()));

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
        return Json.createObjectBuilder()
                .add("id", ARBITRARY_HEARING_JUDGE_ID)
                .add("firstName", ARBITRARY_HEARING_JUDGE_FIRST_NAME)
                .add("lastName", ARBITRARY_HEARING_JUDGE_LAST_NAME)
                .add("title", ARBITRARY_HEARING_JUDGE_TITLE)
                .build();
    }
}