package uk.gov.moj.cpp.hearing.event.listener;


import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.domain.event.CustodyTimeLimitClockStopped;
import uk.gov.moj.cpp.hearing.domain.event.CustodyTimeLimitExtended;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CustodyTimeLimitEventListenerTest {

    @Mock
    private OffenceRepository offenceRepository;

    @InjectMocks
    private CustodyTimeLimitEventListener custodyTimeLimitEventListener;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Before
    public void setup() {
        final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
        setField(this.objectToJsonObjectConverter, "mapper", objectMapper);
        setField(this.jsonObjectToObjectConverter, "objectMapper", objectMapper);
    }

    @Test
    public void shouldHandleCustodyTimeLimitClockStopped() {

        final UUID hearingId = randomUUID();
        final UUID offenceId = randomUUID();
        final CustodyTimeLimitClockStopped custodyTimeLimitClockStopped = new CustodyTimeLimitClockStopped(hearingId, Arrays.asList(offenceId));

        final Offence offence = new Offence();
        final HearingSnapshotKey snapshotKey = new HearingSnapshotKey(offenceId, hearingId);
        offence.setId(snapshotKey);
        offence.setCtlDaysSpent(5);
        offence.setCtlTimeLimit(LocalDate.now());
        when(offenceRepository.findBy(snapshotKey)).thenReturn(offence);
        custodyTimeLimitEventListener.custodyTimeLimitClockStopped(envelopeFrom(metadataWithRandomUUID("hearing.event.custody-time-limit-clock-stopped"),
                objectToJsonObjectConverter.convert(custodyTimeLimitClockStopped)));

        final ArgumentCaptor<Offence> offenceCaptor = ArgumentCaptor.forClass(Offence.class);
        verify(offenceRepository).save(offenceCaptor.capture());


        assertThat(offenceCaptor.getValue().getCtlTimeLimit(), nullValue());
        assertThat(offenceCaptor.getValue().getCtlTimeLimit(), nullValue());
        assertThat(offenceCaptor.getValue().isCtlClockStopped(), is(true));


    }

    @Test
    public void shouldHandleCustodyTimeLimitExtendedEvent() {

        final UUID hearingId = randomUUID();
        final UUID offenceId = randomUUID();
        final LocalDate extendedTimeLimit = LocalDate.now();
        final CustodyTimeLimitExtended custodyTimeLimitExtended = new CustodyTimeLimitExtended(hearingId, offenceId, extendedTimeLimit);

        final Offence offence = new Offence();
        final HearingSnapshotKey snapshotKey = new HearingSnapshotKey(offenceId, hearingId);
        offence.setId(snapshotKey);
        when(offenceRepository.findBy(snapshotKey)).thenReturn(offence);
        custodyTimeLimitEventListener.custodyTimeLimitExtended(envelopeFrom(metadataWithRandomUUID("hearing.event.custody-time-limit-extended"),
                objectToJsonObjectConverter.convert(custodyTimeLimitExtended)));

        final ArgumentCaptor<Offence> offenceCaptor = ArgumentCaptor.forClass(Offence.class);
        verify(offenceRepository).save(offenceCaptor.capture());

        assertThat(offenceCaptor.getValue().getCtlTimeLimit(), is(extendedTimeLimit));
        assertThat(offenceCaptor.getValue().isCtlExtended(), is(true));

    }

}
