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
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselUpsert;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Ahearing;
import uk.gov.moj.cpp.hearing.persist.entity.ex.ProsecutionAdvocate;
import uk.gov.moj.cpp.hearing.repository.AhearingRepository;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;

@RunWith(MockitoJUnitRunner.class)
public class ProsecutionCounselAddedEventListenerTest {

    @Mock
    private AhearingRepository ahearingRepository;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @InjectMocks
    private ProsecutionCounselAddedEventListener prosecutionCounselAddedEventListener;

    @Captor
    private ArgumentCaptor<Ahearing> ahearingArgumentCaptor;

    @Before
    public void setUp() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldStoreProsecutionCounselOnAddEvent() {

        ProsecutionCounselUpsert prosecutionCounselUpsert = ProsecutionCounselUpsert.builder()
                .withAttendeeId(randomUUID())
                .withFirstName("david")
                .withHearingId(randomUUID())
                .withStatus("QC")
                .withLastName("Bowie")
                .withTitle("Mr")
                .build();

        Ahearing ahearing = Ahearing.builder().withId(prosecutionCounselUpsert.getHearingId()).build();
        when(this.ahearingRepository.findBy(prosecutionCounselUpsert.getHearingId())).thenReturn(ahearing);

        this.prosecutionCounselAddedEventListener.prosecutionCounselAdded(envelopeFrom(metadataWithRandomUUID("hearing.newprosecution-counsel-added"),
                objectToJsonObjectConverter.convert(prosecutionCounselUpsert)));

        verify(this.ahearingRepository).save(ahearingArgumentCaptor.capture());
        Ahearing savedHearing = ahearingArgumentCaptor.getValue();
        assertThat(savedHearing, is(ahearing));
        assertThat(savedHearing.getAttendees().size(), is(1));
        assertThat(savedHearing.getAttendees().get(0), instanceOf(ProsecutionAdvocate.class));

        ProsecutionAdvocate prosecutionAdvocate = (ProsecutionAdvocate) ahearing.getAttendees().get(0);

        assertThat(prosecutionAdvocate.getId().getId(), is(prosecutionCounselUpsert.getAttendeeId()));
        assertThat(prosecutionAdvocate.getId().getHearingId(), is(prosecutionCounselUpsert.getHearingId()));
        assertThat(prosecutionAdvocate.getFirstName(), is(prosecutionCounselUpsert.getFirstName()));
        assertThat(prosecutionAdvocate.getLastName(), is(prosecutionCounselUpsert.getLastName()));
        assertThat(prosecutionAdvocate.getTitle(), is(prosecutionCounselUpsert.getTitle()));
        assertThat(prosecutionAdvocate.getStatus(), is(prosecutionCounselUpsert.getStatus()));
        
        //  now check an update works
        ProsecutionCounselUpsert updateProsecutionCounselAdded = ProsecutionCounselUpsert.builder()
                .withAttendeeId(prosecutionCounselUpsert.getAttendeeId())
                .withFirstName("Xdavid")
                .withHearingId(prosecutionCounselUpsert.getHearingId())
                .withStatus("Trainee")
                .withLastName("XBowie")
                .withTitle("XMr")
                .build();

        reset(this.ahearingRepository);
        when(this.ahearingRepository.findBy(prosecutionCounselUpsert.getHearingId())).thenReturn(ahearing);

        this.prosecutionCounselAddedEventListener.prosecutionCounselAdded(envelopeFrom(metadataWithRandomUUID("hearing.newprosecution-counsel-added"),
                objectToJsonObjectConverter.convert(updateProsecutionCounselAdded)));


        verify(this.ahearingRepository).save(this.ahearingArgumentCaptor.capture());
        savedHearing = ahearingArgumentCaptor.getValue();
        assertThat(savedHearing, is(ahearing));
        assertThat(savedHearing.getAttendees().size(), is(1));
        assertThat(savedHearing.getAttendees().get(0), instanceOf(ProsecutionAdvocate.class));

        prosecutionAdvocate = (ProsecutionAdvocate) ahearing.getAttendees().get(0);

        assertThat(prosecutionAdvocate.getId().getId(), is(prosecutionCounselUpsert.getAttendeeId()));
        assertThat(prosecutionAdvocate.getId().getHearingId(), is(updateProsecutionCounselAdded.getHearingId()));
        assertThat(prosecutionAdvocate.getFirstName(), is(updateProsecutionCounselAdded.getFirstName()));
        assertThat(prosecutionAdvocate.getLastName(), is(updateProsecutionCounselAdded.getLastName()));
        assertThat(prosecutionAdvocate.getTitle(), is(updateProsecutionCounselAdded.getTitle()));
        assertThat(prosecutionAdvocate.getStatus(), is(updateProsecutionCounselAdded.getStatus()));
    }

}