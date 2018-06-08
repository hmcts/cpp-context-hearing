package uk.gov.moj.cpp.hearing.event.listener;

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
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpsert;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDate;
import uk.gov.moj.cpp.hearing.persist.entity.ha.AttendeeHearingDate;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DefenceAdvocate;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.repository.AttendeeHearingDateRespository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class DefenceCounselAddedEventListenerTest {

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private AttendeeHearingDateRespository attendeeHearingDateRespository;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @InjectMocks
    private DefenceCounselAddedEventListener defenceCounselAddedEventListener;

    @Captor
    private ArgumentCaptor<Hearing> ahearingArgumentCaptor;

    @Captor
    private ArgumentCaptor<AttendeeHearingDate> attendeeHearingDateArgumentCaptor;

    @Before
    public void setUp() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldStoreDefenceCounselOnAddEvent() {

        DefenceCounselUpsert defenceCounselUpsert = DefenceCounselUpsert.builder()
                .withAttendeeId(randomUUID())
                .withHearingId(randomUUID())
                .withFirstName("David")
                .withLastName("Davidson")
                .withTitle("Colonel")
                .withStatus("QC")
                .withDefendantIds(asList(randomUUID(), randomUUID()))
                .build();

        final Hearing hearing = Hearing.builder()
                .withId(defenceCounselUpsert.getHearingId())
                .withDefendants(asList(
                        Defendant.builder().withId(new HearingSnapshotKey(defenceCounselUpsert.getDefendantIds().get(0), defenceCounselUpsert.getHearingId())).build(),
                        Defendant.builder().withId(new HearingSnapshotKey(defenceCounselUpsert.getDefendantIds().get(1), defenceCounselUpsert.getHearingId())).build()))
                .build();

        hearing.getHearingDays().add(HearingDate.builder()
                .withId(new HearingSnapshotKey(UUID.randomUUID(), hearing.getId()))
                .withDate(LocalDate.now())
                .withDateTime(ZonedDateTime.now())
                .withHearing(hearing)
                .build());

        when(this.hearingRepository.findBy(defenceCounselUpsert.getHearingId())).thenReturn(hearing);

        this.defenceCounselAddedEventListener.defenseCounselAdded(envelopeFrom(metadataWithRandomUUID("hearing.newdefence-counsel-added"),
                objectToJsonObjectConverter.convert(defenceCounselUpsert)));

        verify(this.hearingRepository).saveAndFlush(ahearingArgumentCaptor.capture());
        
        final Hearing savedHearing = ahearingArgumentCaptor.getValue();
        assertThat(savedHearing, is(hearing));
        assertThat(savedHearing.getAttendees().size(), is(1));
        assertThat(savedHearing.getAttendees().get(0), instanceOf(DefenceAdvocate.class));

        final DefenceAdvocate defenceAdvocate = (DefenceAdvocate) hearing.getAttendees().get(0);

        assertThat(defenceAdvocate.getId().getId(), is(defenceCounselUpsert.getAttendeeId()));
        assertThat(defenceAdvocate.getId().getHearingId(), is(defenceCounselUpsert.getHearingId()));
        assertThat(defenceAdvocate.getFirstName(), is(defenceCounselUpsert.getFirstName()));
        assertThat(defenceAdvocate.getLastName(), is(defenceCounselUpsert.getLastName()));
        assertThat(defenceAdvocate.getTitle(), is(defenceCounselUpsert.getTitle()));
        assertThat(defenceAdvocate.getStatus(), is(defenceCounselUpsert.getStatus()));

        assertThat(hearing.getDefendants().get(0).getDefenceAdvocates(), hasItems(defenceAdvocate));
        assertThat(hearing.getDefendants().get(1).getDefenceAdvocates(), hasItems(defenceAdvocate));

        verify(this.attendeeHearingDateRespository).saveAndFlush(attendeeHearingDateArgumentCaptor.capture());

        final AttendeeHearingDate attendeeHearingDate = attendeeHearingDateArgumentCaptor.getValue();

        assertThat(attendeeHearingDate.getId().getId(), instanceOf(UUID.class));
        assertThat(attendeeHearingDate.getId().getHearingId(), is(defenceCounselUpsert.getHearingId()));
        assertThat(attendeeHearingDate.getAttendeeId(), is(defenceCounselUpsert.getAttendeeId()));
        assertThat(attendeeHearingDate.getHearingDateId(), is(savedHearing.getHearingDays().get(0).getId().getId()));
    }

}
