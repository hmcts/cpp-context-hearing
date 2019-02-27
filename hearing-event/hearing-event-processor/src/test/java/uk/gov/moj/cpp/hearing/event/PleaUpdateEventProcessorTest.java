package uk.gov.moj.cpp.hearing.event;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.PleaValue;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.PleaUpsert;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;


public class PleaUpdateEventProcessorTest {

    @Spy
    private final Enveloper enveloper = createEnveloper();
    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
    @Spy
    @InjectMocks
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter();
    @Spy
    @InjectMocks
    private final ObjectToJsonValueConverter objectToJsonValueConverter = new ObjectToJsonValueConverter(this.objectMapper);
    @Spy
    @InjectMocks
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();
    @Mock
    private Sender sender;
    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;
    @InjectMocks
    private PleaUpdateEventProcessor pleaUpdateEventProcessor;


    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void offencePleaUpdate() {
        final PleaUpsert pleaUpsert = PleaUpsert.pleaUpsert()
                .setHearingId(randomUUID())
                .setPlea(Plea.plea()
                        .withOffenceId(randomUUID())
                        .withPleaDate(PAST_LOCAL_DATE.next())
                        .withPleaValue(PleaValue.GUILTY)
                        .build());

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.hearing-offence-plea-updated"),
                objectToJsonObjectConverter.convert(pleaUpsert));

        this.pleaUpdateEventProcessor.offencePleaUpdate(event);

        verify(this.sender, times(2)).send(this.envelopeArgumentCaptor.capture());

        List<JsonEnvelope> events = this.envelopeArgumentCaptor.getAllValues();

        assertThat(events.get(0).metadata().name(), is("hearing.command.update-plea-against-offence"));
        assertThat(asPojo(events.get(0), PleaUpsert.class), isBean(PleaUpsert.class)
                .with(PleaUpsert::getHearingId, is(pleaUpsert.getHearingId()))
                .with(PleaUpsert::getPlea, isBean(Plea.class)
                        .with(Plea::getOffenceId, is(pleaUpsert.getPlea().getOffenceId()))
                        .with(Plea::getPleaDate, is(pleaUpsert.getPlea().getPleaDate()))
                        .with(Plea::getPleaValue, is(pleaUpsert.getPlea().getPleaValue()))));

        assertThat(events.get(1).metadata().name(), is("public.hearing.plea-updated"));
        assertThat(asPojo(events.get(1), Plea.class), isBean(Plea.class)
                .with(Plea::getOffenceId, is(pleaUpsert.getPlea().getOffenceId())));
    }
}