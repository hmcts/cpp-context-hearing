package uk.gov.moj.cpp.hearing.event;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import javax.json.Json;

@ExtendWith(MockitoExtension.class)
public class CaseMarkerEventProcessorTest {

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @InjectMocks
    private CaseMarkerEventProcessor processor;

    @Test
    public void processPublicEventCaseMarkersUpdated() {
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("public.progression.case-markers-updated"),
                Json.createObjectBuilder()
                        .add("prosecutionCaseId", "34d07e81-9770-4d23-af6f-84f1d7571bd3")
                        .add("hearingId", "581767a1-22af-408a-92f0-20837846cc6f")
                        .add("caseMarkers", Json.createArrayBuilder()
                            .add(Json.createObjectBuilder()
                                .add("id", "3789ab16-0bb7-4ef1-87ef-c936bf0364f1")
                                .add("markerTypeid", "3789ab16-0bb7-4ef1-87ef-c936bf0364f1")
                                .add("markerTypeCode", "WP")
                                .add("markerTypeDescription", "Prohibited Weapons"))
                            .build())
                        .build());

        processor.processPublicEventCaseMarkerUpdated(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(envelopeArgumentCaptor.getValue(),
                jsonEnvelope(metadata().withName("hearing.command.update-case-markers"), payloadIsJson(allOf(
                        withJsonPath("$.prosecutionCaseId", is("34d07e81-9770-4d23-af6f-84f1d7571bd3")),
                        withJsonPath("$.hearingId", is("581767a1-22af-408a-92f0-20837846cc6f")),
                        withJsonPath("$.caseMarkers[0].id", is("3789ab16-0bb7-4ef1-87ef-c936bf0364f1")),
                        withJsonPath("$.caseMarkers[0].markerTypeid", is("3789ab16-0bb7-4ef1-87ef-c936bf0364f1")),
                        withJsonPath("$.caseMarkers[0].markerTypeCode", is("WP")),
                        withJsonPath("$.caseMarkers[0].markerTypeDescription", is("Prohibited Weapons"))))));
    }

    @Test
    public void processEnrichUpdateCaseMarkersWithAssociatedHearings() {
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.events.case-markers-enriched-with-associated-hearings"),
                Json.createObjectBuilder()
                        .add("prosecutionCaseId", "34d07e81-9770-4d23-af6f-84f1d7571bd3")
                        .add("hearingIds", Json.createArrayBuilder()
                                .add("a8448a33-68ab-4b9b-84c2-59cee4fe36f4")
                                .add("095d7412-ba76-4a15-942d-566d3aeae7c9")
                                .build())
                        .add("caseMarkers", Json.createArrayBuilder()
                                .add(Json.createObjectBuilder()
                                        .add("id", "3789ab16-0bb7-4ef1-87ef-c936bf0364f1")
                                        .add("markerTypeid", "3789ab16-0bb7-4ef1-87ef-c936bf0364f1")
                                        .add("markerTypeCode", "WP")
                                        .add("markerTypeDescription", "Prohibited Weapons"))
                                .build())
                        .build());

        processor.enrichUpdateCaseMarkersWithAssociatedHearings(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(envelopeArgumentCaptor.getValue(),
                jsonEnvelope(metadata().withName("hearing.command.update-case-markers-with-associated-hearings"), payloadIsJson(allOf(
                        withJsonPath("$.prosecutionCaseId", is("34d07e81-9770-4d23-af6f-84f1d7571bd3")),
                        withJsonPath("$.hearingIds[0]", is("a8448a33-68ab-4b9b-84c2-59cee4fe36f4")),
                        withJsonPath("$.hearingIds[1]", is("095d7412-ba76-4a15-942d-566d3aeae7c9")),
                        withJsonPath("$.caseMarkers[0].id", is("3789ab16-0bb7-4ef1-87ef-c936bf0364f1")),
                        withJsonPath("$.caseMarkers[0].markerTypeid", is("3789ab16-0bb7-4ef1-87ef-c936bf0364f1")),
                        withJsonPath("$.caseMarkers[0].markerTypeCode", is("WP")),
                        withJsonPath("$.caseMarkers[0].markerTypeDescription", is("Prohibited Weapons"))))));
    }
}