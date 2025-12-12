package uk.gov.moj.cpp.hearing.event.delegates;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.event.NowsTemplates.resultsSharedTemplate;
import static uk.gov.moj.cpp.hearing.event.NowsTemplates.resultsSharedV2Template;

import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV2;

public class UpdateResultLineStatusDelegateTest {

    @Spy
    private final Enveloper enveloper = createEnveloper();
    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
    @Spy
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new JsonObjectConvertersFactory().objectToJsonObjectConverter();
    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;
    @Mock
    private Sender sender;
    @InjectMocks
    private UpdateResultLineStatusDelegate updateResultLineStatusDelegate;

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void updateResultLineStatus() {

        final ResultsShared resultsShared = resultsSharedTemplate();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));

        updateResultLineStatusDelegate.updateResultLineStatus(sender, event, resultsShared);

        verify(sender).send(envelopeArgumentCaptor.capture());

        final List<JsonEnvelope> outgoingMessages = envelopeArgumentCaptor.getAllValues();

        final JsonEnvelope updatedResultLinesMessage = outgoingMessages.get(0);

        assertThat(updatedResultLinesMessage, jsonEnvelope(
                metadata().withName("hearing.command.update-result-lines-status"),
                payloadIsJson(allOf(
                        withJsonPath("$.hearingId", is(resultsShared.getHearing().getId().toString()))))
        ));
    }

    @Test
    public void shouldIssueUpdateDaysResultLineStatusCommand() {

        final ResultsSharedV2 resultsShared = resultsSharedV2Template();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.events.results-shared-v2"),
                objectToJsonObjectConverter.convert(resultsShared));

        updateResultLineStatusDelegate.updateDaysResultLineStatus(sender, event, resultsShared);

        verify(sender).send(envelopeArgumentCaptor.capture());

        final List<JsonEnvelope> outgoingMessages = envelopeArgumentCaptor.getAllValues();

        final JsonEnvelope updatedResultLinesMessage = outgoingMessages.get(0);

        assertThat(updatedResultLinesMessage, jsonEnvelope(
                metadata().withName("hearing.command.update-days-result-lines-status"),
                payloadIsJson(allOf(
                        withJsonPath("$.hearingId", is(resultsShared.getHearing().getId().toString())),
                        withJsonPath("$.hearingDay", is(resultsShared.getHearingDay().toString())),
                        withJsonPath("$.courtClerk", notNullValue())))));
    }

}