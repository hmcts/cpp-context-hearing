package uk.gov.moj.cpp.hearing.event.listener;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.nows.events.NowsMaterialStatusUpdated;
import uk.gov.moj.cpp.hearing.repository.NowsMaterialRepository;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NowsGeneratedEventListenerTest {

    @Mock
    private NowsMaterialRepository nowsMaterialRepository;

    @InjectMocks
    private NowsGeneratedEventListener nowsGeneratedEventListener;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldUpdateNowsMaterialStatusToGenerated() throws Exception {

        final NowsMaterialStatusUpdated nowsMaterialStatusUpdated = new NowsMaterialStatusUpdated(UUID.randomUUID(), UUID.randomUUID(), "generated");

        when(nowsMaterialRepository.updateStatus(nowsMaterialStatusUpdated.getMaterialId(), "generated")).thenReturn(1);

        nowsGeneratedEventListener.nowsGenerated(envelopeFrom(metadataWithRandomUUID("hearing.events.nows-material-status-updated"),
                objectToJsonObjectConverter.convert(nowsMaterialStatusUpdated)));

        final ArgumentCaptor<UUID> materialIdArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        final ArgumentCaptor<String> nowsMaterialStatusArgumentCaptor = ArgumentCaptor.forClass(String.class);

        verify(this.nowsMaterialRepository).updateStatus(materialIdArgumentCaptor.capture(), nowsMaterialStatusArgumentCaptor.capture());

        assertThat(materialIdArgumentCaptor.getValue(), is(nowsMaterialStatusUpdated.getMaterialId()));
        assertThat(nowsMaterialStatusArgumentCaptor.getValue(), is("generated"));
    }

    @Test
    public void shouldFailureToUpdateNowsMaterialStatusToGenerated() throws Exception {

        final NowsMaterialStatusUpdated nowsMaterialStatusUpdated = new NowsMaterialStatusUpdated(UUID.randomUUID(), UUID.randomUUID(), "generated");

        when(nowsMaterialRepository.updateStatus(nowsMaterialStatusUpdated.getMaterialId(), "generated")).thenReturn(0);

        nowsGeneratedEventListener.nowsGenerated(envelopeFrom(metadataWithRandomUUID("hearing.events.nows-material-status-updated"),
                objectToJsonObjectConverter.convert(nowsMaterialStatusUpdated)));

        final ArgumentCaptor<UUID> materialIdArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        final ArgumentCaptor<String> nowsMaterialStatusArgumentCaptor = ArgumentCaptor.forClass(String.class);

        verify(this.nowsMaterialRepository).updateStatus(materialIdArgumentCaptor.capture(), nowsMaterialStatusArgumentCaptor.capture());

        assertThat(materialIdArgumentCaptor.getValue(), is(nowsMaterialStatusUpdated.getMaterialId()));
        assertThat(nowsMaterialStatusArgumentCaptor.getValue(), is("generated"));
    }
}