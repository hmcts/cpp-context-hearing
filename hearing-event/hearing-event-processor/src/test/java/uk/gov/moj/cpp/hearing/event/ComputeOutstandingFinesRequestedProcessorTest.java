package uk.gov.moj.cpp.hearing.event;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher;
import uk.gov.moj.cpp.hearing.domain.event.OutstandingFinesQueried;

import java.time.LocalDate;
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
public class ComputeOutstandingFinesRequestedProcessorTest {

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @Captor
    private ArgumentCaptor<Class> argumentCaptor;

    @Mock
    private Requester requester;

    @Mock
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;


    @InjectMocks
    private ComputeOutstandingFinesRequestedProcessor computeOutstandingFinesRequestedProcessor;


    @Test
    public void publicComputeOutstandingFinesRequested() {
        final JsonObject outstandingFinesQuery = Json.createObjectBuilder().build();
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.compute-outstanding-fines-requested"),
                outstandingFinesQuery);


        final Envelope<JsonObject> courtBasedDefendantQueryInformation = mock(Envelope.class);
        when(courtBasedDefendantQueryInformation.payload()).thenReturn(Json.createObjectBuilder().build());

        final OutstandingFinesQueried outstandingFinesQueried = OutstandingFinesQueried.newBuilder()
                .withCourtCentreId(UUID.fromString("cb41f33d-9de0-4f49-9f8e-b7b06de4279a"))
                .withCourtRoomIds(Arrays.asList(UUID.fromString("52faed9a-db0f-4430-bdf0-d6672ff7f3fa"),
                        UUID.fromString("d760acf6-fc92-46dd-bf26-80ebf50fbed4")))
                .withHearingDate(LocalDate.parse("2019-12-21"))
                .build();
        when(jsonObjectToObjectConverter.convert(outstandingFinesQuery, OutstandingFinesQueried.class)).thenReturn(
                outstandingFinesQueried
        );

        when(requester.requestAsAdmin(envelopeArgumentCaptor.capture(), argumentCaptor.capture())).thenReturn(courtBasedDefendantQueryInformation);
        final JsonObject courtBasedDefendantQueryJsonObject = mock(JsonObject.class);
        when(courtBasedDefendantQueryInformation.payload()).thenReturn(courtBasedDefendantQueryJsonObject);

        doNothing().when(sender).send(envelopeArgumentCaptor.capture());

        computeOutstandingFinesRequestedProcessor.publicComputeOutstandingFinesRequested(event);

        final List<JsonEnvelope> allValues = envelopeArgumentCaptor.getAllValues();
        final JsonEnvelope outstandingFinesQueryEnvelope = allValues.get(0);
        final Envelope courtBasedDefendantQueryEnvelope = allValues.get(1);

        assertThat(outstandingFinesQueryEnvelope.metadata(), withMetadataEnvelopedFrom(event));
        assertThat(outstandingFinesQueryEnvelope.payload(),
                JsonEnvelopePayloadMatcher.payloadIsJson(
                        allOf(

                                withJsonPath("$.courtCentreId", is("cb41f33d-9de0-4f49-9f8e-b7b06de4279a")),
                                withJsonPath("$.courtRoomIds", is("52faed9a-db0f-4430-bdf0-d6672ff7f3fa,d760acf6-fc92-46dd-bf26-80ebf50fbed4")),
                                withJsonPath("$.hearingDate", is("2019-12-21"))
                        )))
        ;

        assertThat(courtBasedDefendantQueryEnvelope.metadata(), withMetadataEnvelopedFrom(event)
                .withCausationIds()
                .withName("stagingenforcement.court.rooms.outstanding-fines"));
        assertThat(courtBasedDefendantQueryEnvelope.payload(), is(courtBasedDefendantQueryJsonObject));

    }
}
