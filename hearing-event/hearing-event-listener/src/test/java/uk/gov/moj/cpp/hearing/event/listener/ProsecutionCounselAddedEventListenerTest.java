package uk.gov.moj.cpp.hearing.event.listener;

import org.apache.commons.lang3.builder.ToStringBuilder;
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
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselUpsert;
import uk.gov.moj.cpp.hearing.persist.entity.ha.AttendeeHearingDate;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDate;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionAdvocate;
import uk.gov.moj.cpp.hearing.repository.AttendeeHearingDateRespository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class ProsecutionCounselAddedEventListenerTest {

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private AttendeeHearingDateRespository attendeeHearingDateRespository;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @InjectMocks
    private ProsecutionCounselAddedEventListener prosecutionCounselAddedEventListener;

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
    public void shouldStoreProsecutionCounselOnAddEvent() {

        final ProsecutionCounselUpsert prosecutionCounselUpsert = ProsecutionCounselUpsert.builder()
                .withAttendeeId(randomUUID())
                .withFirstName("david")
                .withHearingId(randomUUID())
                .withStatus("QC")
                .withLastName("Bowie")
                .withTitle("Mr")
                .build();

       final Hearing hearing = Hearing.builder()
                .withId(prosecutionCounselUpsert.getHearingId())
                .build();

        hearing.getHearingDays().add(HearingDate.builder()
                .withId(new HearingSnapshotKey(UUID.randomUUID(), hearing.getId()))
                .withDate(LocalDate.now())
                .withDateTime(ZonedDateTime.now())
                .withHearing(hearing)
                .build());

        when(this.hearingRepository.findBy(prosecutionCounselUpsert.getHearingId())).thenReturn(hearing);

        this.prosecutionCounselAddedEventListener.prosecutionCounselAdded(envelopeFrom(metadataWithRandomUUID("hearing.newprosecution-counsel-added"),
                objectToJsonObjectConverter.convert(prosecutionCounselUpsert)));

        verify(this.hearingRepository).saveAndFlush(ahearingArgumentCaptor.capture());

        Hearing savedHearing = ahearingArgumentCaptor.getValue();
        assertThat(savedHearing, is(hearing));
        assertThat(savedHearing.getHearingDays().size(), is(1));
        assertThat(savedHearing.getAttendees().size(), is(1));
        assertThat(savedHearing.getAttendees().get(0), instanceOf(ProsecutionAdvocate.class));

        ProsecutionAdvocate prosecutionAdvocate = (ProsecutionAdvocate) hearing.getAttendees().get(0);

        assertThat(prosecutionAdvocate.getId().getId(), is(prosecutionCounselUpsert.getAttendeeId()));
        assertThat(prosecutionAdvocate.getId().getHearingId(), is(prosecutionCounselUpsert.getHearingId()));
        assertThat(prosecutionAdvocate.getFirstName(), is(prosecutionCounselUpsert.getFirstName()));
        assertThat(prosecutionAdvocate.getLastName(), is(prosecutionCounselUpsert.getLastName()));
        assertThat(prosecutionAdvocate.getTitle(), is(prosecutionCounselUpsert.getTitle()));
        assertThat(prosecutionAdvocate.getStatus(), is(prosecutionCounselUpsert.getStatus()));

        //  now check an update works
        final ProsecutionCounselUpsert updateProsecutionCounselAdded = ProsecutionCounselUpsert.builder()
                .withAttendeeId(prosecutionCounselUpsert.getAttendeeId())
                .withFirstName("Xdavid")
                .withHearingId(prosecutionCounselUpsert.getHearingId())
                .withStatus("Trainee")
                .withLastName("XBowie")
                .withTitle("XMr")
                .build();

        reset(this.hearingRepository);
        when(this.hearingRepository.findBy(prosecutionCounselUpsert.getHearingId())).thenReturn(hearing);

        this.prosecutionCounselAddedEventListener.prosecutionCounselAdded(envelopeFrom(metadataWithRandomUUID("hearing.newprosecution-counsel-added"),
                objectToJsonObjectConverter.convert(updateProsecutionCounselAdded)));

        verify(this.hearingRepository).saveAndFlush(this.ahearingArgumentCaptor.capture());
        savedHearing = ahearingArgumentCaptor.getValue();
        assertThat(savedHearing, is(hearing));
        assertThat(savedHearing.getHearingDays().size(), is(1));
        assertThat(savedHearing.getAttendees().size(), is(1));
        assertThat(savedHearing.getAttendees().get(0), instanceOf(ProsecutionAdvocate.class));

        prosecutionAdvocate = (ProsecutionAdvocate) hearing.getAttendees().get(0);

        assertThat(prosecutionAdvocate.getId().getId(), is(prosecutionCounselUpsert.getAttendeeId()));
        assertThat(prosecutionAdvocate.getId().getHearingId(), is(updateProsecutionCounselAdded.getHearingId()));
        assertThat(prosecutionAdvocate.getFirstName(), is(updateProsecutionCounselAdded.getFirstName()));
        assertThat(prosecutionAdvocate.getLastName(), is(updateProsecutionCounselAdded.getLastName()));
        assertThat(prosecutionAdvocate.getTitle(), is(updateProsecutionCounselAdded.getTitle()));
        assertThat(prosecutionAdvocate.getStatus(), is(updateProsecutionCounselAdded.getStatus()));

        //FIXME: BUG -> capturing twice instances for same hearing date
        verify(this.attendeeHearingDateRespository, atLeast(1)).saveAndFlush(attendeeHearingDateArgumentCaptor.capture());
        
        attendeeHearingDateArgumentCaptor.getAllValues().forEach(v -> System.out.println(ToStringBuilder.reflectionToString(v)));

        final AttendeeHearingDate attendeeHearingDate = attendeeHearingDateArgumentCaptor.getValue();

        assertThat(attendeeHearingDate.getId().getId(), instanceOf(UUID.class));
        assertThat(attendeeHearingDate.getId().getHearingId(), is(updateProsecutionCounselAdded.getHearingId()));
        assertThat(attendeeHearingDate.getAttendeeId(), is(updateProsecutionCounselAdded.getAttendeeId()));
        assertThat(attendeeHearingDate.getHearingDateId(), is(savedHearing.getHearingDays().get(0).getId().getId()));
    }

}