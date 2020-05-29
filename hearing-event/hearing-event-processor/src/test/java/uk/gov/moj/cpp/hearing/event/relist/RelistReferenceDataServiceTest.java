package uk.gov.moj.cpp.hearing.event.relist;

import static javax.json.Json.createObjectBuilder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.enveloper.EnvelopeFactory.createEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory;
import uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher;
import uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingResultDefinition;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.json.Json;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RelistReferenceDataServiceTest {

    private static final String REFERENCE_DATA_GET_RESULT_DEFINITION_NEXT_HEARING = "referencedata.get-result-definition-next-hearing";
    private static final String REFERENCE_DATA_GET_RESULT_DEFINITION_WITHDRAWN = "referencedata.get-result-definition-withdrawn";

    @InjectMocks
    private RelistReferenceDataService relistReferenceDataService;

    @Spy
    private Enveloper enveloper = EnveloperFactory.createEnveloper();

    @Mock
    private Requester requester;

    @Captor
    private ArgumentCaptor<JsonEnvelope> captor;

    @Test
    public void shouldGetWithdrawnResultDefinitionUuids() {
        //Given

        final JsonEnvelope command = createEnvelope(REFERENCE_DATA_GET_RESULT_DEFINITION_WITHDRAWN,
                createObjectBuilder().build());
        when(requester.request(captor.capture())).thenReturn(getArbitraryWithdrawnResult());

        List<UUID> withdrawnUuids = relistReferenceDataService.getWithdrawnResultDefinitionUuids(command, LocalDate.now());

        final JsonEnvelope jsonEnvelope = captor.getValue();

        assertThat(jsonEnvelope, new JsonEnvelopeMatcher().withMetadataOf(metadata().withName(REFERENCE_DATA_GET_RESULT_DEFINITION_WITHDRAWN)));
        assertEquals(1, withdrawnUuids.size());
    }

    @Test
    public void shouldGetNextHearingResultDefinitions() {
        //Given

        final JsonEnvelope command = createEnvelope(REFERENCE_DATA_GET_RESULT_DEFINITION_NEXT_HEARING,
                createObjectBuilder()
                        .build());
        when(requester.request(captor.capture())).thenReturn(getArbitraryNextHearingResult());

        Map<UUID, NextHearingResultDefinition> nextHearingResult = relistReferenceDataService.getNextHearingResultDefinitions(command, LocalDate.now());

        final JsonEnvelope jsonEnvelope = captor.getValue();

        assertThat(jsonEnvelope, new JsonEnvelopeMatcher().withMetadataOf(metadata().withName(REFERENCE_DATA_GET_RESULT_DEFINITION_NEXT_HEARING)));
        assertEquals(1, nextHearingResult.size());
    }

    private JsonEnvelope getArbitraryWithdrawnResult() {
        return createEnvelope(REFERENCE_DATA_GET_RESULT_DEFINITION_WITHDRAWN,
                createObjectBuilder()
                        .add("resultDefinitions",
                                Json.createArrayBuilder()
                                        .add(Json.createObjectBuilder()
                                                .add("id", UUID.randomUUID().toString())
                                                .build())
                                        .build())
                        .build());
    }

    private JsonEnvelope getArbitraryNextHearingResult() {
        return createEnvelope(REFERENCE_DATA_GET_RESULT_DEFINITION_WITHDRAWN,
                createObjectBuilder()
                        .add("resultDefinitions",
                                Json.createArrayBuilder()
                                        .add(Json.createObjectBuilder()
                                                .add("id", UUID.randomUUID().toString())
                                                .add("prompts", Json.createArrayBuilder()
                                                        .add(Json.createObjectBuilder()
                                                                .add("id", UUID.randomUUID().toString())
                                                                .add("reference", "FOO")
                                                                .build())
                                                        .build())
                                                .build())
                                        .build())
                        .build());
    }

}
