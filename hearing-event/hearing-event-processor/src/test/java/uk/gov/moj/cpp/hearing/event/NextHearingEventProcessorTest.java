package uk.gov.moj.cpp.hearing.event;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import javax.json.Json;
import javax.json.JsonObject;

@ExtendWith(MockitoExtension.class)
public class NextHearingEventProcessorTest {

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<Envelope<JsonObject>> envelopeArgumentCaptor;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter = new JsonObjectConvertersFactory().objectToJsonObjectConverter();

    @InjectMocks
    private NextHearingEventProcessor processor;

    @Test
    public void processNextHearingDayChangedPublicEvent() {

        final String hearingId = randomUUID().toString();
        final String seedingHearingId = randomUUID().toString();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("public.events.listing.next-hearing-day-changed"),
                Json.createObjectBuilder()
                        .add("hearingId", hearingId)
                        .add("seedingHearingId", seedingHearingId)
                        .add("hearingStartDate", "2021-06-20T00:00:00.000Z")
                        .build());

        processor.processNextHearingDayChangedPublicEvent(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        final Envelope<JsonObject> command = this.envelopeArgumentCaptor.getValue();

        assertThat(command.metadata().name(), is("hearing.command.record-next-hearing-day-updated"));
        assertThat(command.payload().toString(), isJson(allOf(
                withJsonPath("$.hearingId", equalTo(hearingId)),
                withJsonPath("$.seedingHearingId", is(seedingHearingId)),
                withJsonPath("$.hearingStartDate", is("2021-06-20T00:00:00.000Z"))
        )));

    }

}