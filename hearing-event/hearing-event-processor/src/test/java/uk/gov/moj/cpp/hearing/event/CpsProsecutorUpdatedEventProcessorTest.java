package uk.gov.moj.cpp.hearing.event;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.event.Framework5Fix.toJsonEnvelope;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.spi.DefaultEnvelope;

import javax.json.Json;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CpsProsecutorUpdatedEventProcessorTest {

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<DefaultEnvelope> envelopeArgumentCaptor;

    @InjectMocks
    private CpsProsecutorUpdatedEventProcessor processor;

    @Test
    public void shouldUpdateProsecutionCaseWithAssociatedHearings() {

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("public.progression.events.cps-prosecutor-updated"),
                Json.createObjectBuilder()
                        .add("prosecutionCaseId", "34d07e81-9770-4d23-af6f-84f1d7571bd3")
                        .add("hearingIds", Json.createArrayBuilder()
                                .add("a8448a33-68ab-4b9b-84c2-59cee4fe36f4")
                                .add("095d7412-ba76-4a15-942d-566d3aeae7c9")
                                .build())
                        .add("caseURN", "test Case URN")
                        .add("prosecutionAuthorityId", "test prosecutionAuthorityId")
                        .add("prosecutionAuthorityReference", "test prosecutionAuthorityReference")
                        .add("prosecutionAuthorityCode", "test prosecutionAuthorityCode")
                        .add("prosecutionAuthorityName", "test prosecutionAuthorityName")
                        .add("address", Json.createObjectBuilder()
                                .add("address1", "41 Manhattan House")
                                .add("postcode", "MK9 2BQ")
                                .build())
                        .build());

        processor.cpsProsecutorUpdated(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(
                toJsonEnvelope(this.envelopeArgumentCaptor.getValue()), jsonEnvelope(metadata().withName("hearing.command.update-cps-prosecutor-with-associated-hearings"), payloadIsJson(allOf(
                        withJsonPath("$.prosecutionCaseId", is("34d07e81-9770-4d23-af6f-84f1d7571bd3")),
                        withJsonPath("$.hearingIds[0]", is("a8448a33-68ab-4b9b-84c2-59cee4fe36f4")),
                        withJsonPath("$.hearingIds[1]", is("095d7412-ba76-4a15-942d-566d3aeae7c9")),
                        withJsonPath("$.caseURN", is("test Case URN")),
                        withJsonPath("$.prosecutionAuthorityId", is("test prosecutionAuthorityId")),
                        withJsonPath("$.prosecutionAuthorityReference", is("test prosecutionAuthorityReference")),
                        withJsonPath("$.prosecutionAuthorityCode", is("test prosecutionAuthorityCode")),
                        withJsonPath("$.prosecutionAuthorityName", is("test prosecutionAuthorityName")),
                        withJsonPath("$.address.address1", is("41 Manhattan House")),
                        withJsonPath("$.address.postcode", is("MK9 2BQ"))
                )))
        );
    }

    @Test
    public void shouldNotUpdateProsecutionCaseWithoutAssociatedHearings() {

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("public.progression.events.cps-prosecutor-updated"),
                Json.createObjectBuilder()
                        .add("prosecutionCaseId", "34d07e81-9770-4d23-af6f-84f1d7571bd3")
                        .add("hearingIds", Json.createArrayBuilder().build())
                        .add("caseURN", "test Case URN")
                        .add("prosecutionAuthorityId", "test prosecutionAuthorityId")
                        .add("prosecutionAuthorityReference", "test prosecutionAuthorityReference")
                        .add("prosecutionAuthorityCode", "test prosecutionAuthorityCode")
                        .add("prosecutionAuthorityName", "test prosecutionAuthorityName")
                        .add("address", Json.createObjectBuilder()
                                .add("address1", "41 Manhattan House")
                                .add("postcode", "MK9 2BQ")
                                .build())
                        .build());

        processor.cpsProsecutorUpdated(event);

        verify(this.sender, times(0)).send(this.envelopeArgumentCaptor.capture());
    }
}