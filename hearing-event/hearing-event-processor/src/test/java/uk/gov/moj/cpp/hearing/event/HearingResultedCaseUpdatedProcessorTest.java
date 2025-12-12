package uk.gov.moj.cpp.hearing.event;

import com.google.common.io.Resources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.io.IOException;
import java.util.List;

import static com.google.common.io.Resources.getResource;
import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;

public class HearingResultedCaseUpdatedProcessorTest {

    @Spy
    private final Enveloper enveloper = createEnveloper();
    @Mock
    private Sender sender;
    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;
    @InjectMocks
    private HearingResultedCaseUpdatedProcessor hearingResultedCaseUpdatedProcessor;

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testHearing() throws IOException {

        final JsonEnvelope event = getJsonHearingResultedCaseUpdatedEnvelope();

        hearingResultedCaseUpdatedProcessor.handleCaseDefendantUpdate(event);

        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());

        List<JsonEnvelope> events = this.envelopeArgumentCaptor.getAllValues();

        assertThat(events.get(0).metadata().name(), is("hearing.command.update-case-defendants"));

    }

    @Test
    public void testHearingApplication() throws IOException {

        final JsonEnvelope event = getJsonHearingResultedApplicationUpdatedEnvelope();

        hearingResultedCaseUpdatedProcessor.handleApplicationDefendantUpdate(event);

        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());

        List<JsonEnvelope> events = this.envelopeArgumentCaptor.getAllValues();

        assertThat(events.get(0).metadata().name(), is("hearing.command.update-application-defendants"));

    }

    private JsonEnvelope getJsonHearingResultedCaseUpdatedEnvelope() throws IOException {
        final String hearingCasePleaAddOrUpdate = getStringFromResource("public.hearing-resulted-case-updated.json");

        final Metadata metadata = metadataWithDefaults().build();
        return JsonEnvelope.envelopeFrom(metadata, new StringToJsonObjectConverter().convert(hearingCasePleaAddOrUpdate));
    }


    private JsonEnvelope getJsonHearingResultedApplicationUpdatedEnvelope() throws IOException {
        final String hearingApplicationPleaAddOrUpdate = getStringFromResource("public.hearing-resulted-case-updated.json");

        final Metadata metadata = metadataWithDefaults().build();
        return JsonEnvelope.envelopeFrom(metadata, new StringToJsonObjectConverter().convert(hearingApplicationPleaAddOrUpdate));
    }

    private String getStringFromResource(final String path) throws IOException {
        return Resources.toString(getResource(path), defaultCharset());
    }
}
