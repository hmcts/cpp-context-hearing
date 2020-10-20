package uk.gov.moj.cpp.hearing.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.command.initiate.ExtendHearingCommand;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

@RunWith(MockitoJUnitRunner.class)
public class HearingExtendedEventProcessorTest {

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter = new JsonObjectConvertersFactory().objectToJsonObjectConverter();

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @InjectMocks
    private HearingExtendedEventProcessor hearingExtendedEventProcessor;

    @Test
    public void processPublicHearingExtended() {

        final ExtendHearingCommand extendHearingCommand = new ExtendHearingCommand();
        extendHearingCommand.setHearingId(randomUUID());
        extendHearingCommand.setCourtApplication(CourtApplication.courtApplication().withLinkedCaseId(randomUUID()).build());

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("public.progression.events.hearing-extended"),
                objectToJsonObjectConverter.convert(extendHearingCommand));

        hearingExtendedEventProcessor.onPublicProgressionEventsHearingExtended(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(
                this.envelopeArgumentCaptor.getValue(), jsonEnvelope(
                        metadata().withName("hearing.command.extend-hearing"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", is(extendHearingCommand.getHearingId().toString())),
                                withJsonPath("$.courtApplication.linkedCaseId", is(extendHearingCommand.getCourtApplication().getLinkedCaseId().toString()))
                                )
                        )
                )
        );
    }


}
