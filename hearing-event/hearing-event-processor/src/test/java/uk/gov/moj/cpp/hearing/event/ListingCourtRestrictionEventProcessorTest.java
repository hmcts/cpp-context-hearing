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

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ListingCourtRestrictionEventProcessorTest {

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<Envelope<JsonObject>> envelopeArgumentCaptor;


    @InjectMocks
    private ListingCourtRestrictionEventProcessor processor;

    @Test
    public void processCourtListRestrictedPublicEvent() {
        final UUID hearingId = randomUUID();
        final List<UUID> caseIds = Arrays.asList(randomUUID(), randomUUID());

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("public.listing.court-list-restricted"), Json.createObjectBuilder().add("hearingId", hearingId.toString()).build());

        processor.processRestrictCourtListPublicEvent(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        final Envelope<JsonObject> command = this.envelopeArgumentCaptor.getValue();

        assertThat(command.metadata().name(), is("hearing.command.restrict-court-list"));
        assertThat(command.payload().toString(), isJson(allOf(
                withJsonPath("$.hearingId", equalTo(hearingId.toString()))
        )));
    }
}