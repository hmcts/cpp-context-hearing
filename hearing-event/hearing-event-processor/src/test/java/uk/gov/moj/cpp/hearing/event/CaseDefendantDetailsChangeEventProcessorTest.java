package uk.gov.moj.cpp.hearing.event;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantDetailsChangedCommandTemplates.caseDefendantDetailsChangedCommandTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.progression.events.CaseDefendantDetails;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class CaseDefendantDetailsChangeEventProcessorTest {

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @InjectMocks
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @InjectMocks
    private CaseDefendantDetailsChangeEventProcessor caseDefendantDetailsChangeEventProcessor;

    @Test
    public void processPublicCaseDefendantChanged() {

        final CaseDefendantDetails defendantDetailsChangedPublicEvent = caseDefendantDetailsChangedCommandTemplate();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("public.progression.case-defendant-changed"),
                objectToJsonObjectConverter.convert(defendantDetailsChangedPublicEvent));

        caseDefendantDetailsChangeEventProcessor.processPublicCaseDefendantChanged(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(this.envelopeArgumentCaptor.getValue(),
                jsonEnvelope(metadata().withName("hearing.update-case-defendant-details"),
                        payloadIsJson(allOf(withJsonPath("$.defendants[0].id", is(defendantDetailsChangedPublicEvent.getDefendants().get(0).getId().toString()))))));
    }

    @Test
    public void enrichDefendantDetails() {

        CaseDefendantDetails caseDefendantDetailsWithHearings = caseDefendantDetailsChangedCommandTemplate();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.update-case-defendant-details-enriched-with-hearing-ids"), objectToJsonObjectConverter.convert(caseDefendantDetailsWithHearings));

        caseDefendantDetailsChangeEventProcessor.enrichDefendantDetails(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(this.envelopeArgumentCaptor.getValue(),
                jsonEnvelope(metadata().withName("hearing.update-case-defendant-details-against-hearing-aggregate"),
                        payloadIsJson(allOf(withJsonPath("$.defendants[0].id", is(caseDefendantDetailsWithHearings.getDefendants().get(0).getId().toString()))))));
    }
}
