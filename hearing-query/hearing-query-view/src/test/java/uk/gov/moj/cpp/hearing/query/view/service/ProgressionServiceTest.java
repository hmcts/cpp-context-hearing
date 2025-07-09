package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createReader;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;

import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;
import java.util.UUID;

import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProgressionServiceTest {

    @Mock
    private Requester requester;

    @Mock
    private JsonEnvelope jsonEnvelope;

    @InjectMocks
    private ProgressionService progressionService;

    private final String PROGRESSION_QUERY_APPLICATIONS = "progression.query.application.aaag";
    private final String PROGRESSION_QUERY_APPLICATION_ONLY = "progression.query.application-only";
    private static final UUID APPLICATION_ID_AAAG = UUID.fromString("7270191a-54c2-11ea-a2e3-2e728ce88125");
    private static final UUID APPLICATION_ID_ONLY = UUID.fromString("12615f6e-b1de-485c-ae69-e989445b988e");

    @Test
    public void shouldRetrieveAAAGDetailsByApplicationId() {

        JsonEnvelope jsonEnvelope = getUserEnvelope(PROGRESSION_QUERY_APPLICATIONS);
        when(requester.request(any(), any(Class.class))).thenReturn(jsonEnvelope);

        Optional<JsonObject> aagResponse = progressionService.getApplication(jsonEnvelope, APPLICATION_ID_AAAG.toString());

        assertThat(aagResponse.get().getString("applicationId"), is("7270191a-54c2-11ea-a2e3-2e728ce88125"));
        assertThat(aagResponse.get().getJsonObject("applicantDetails").getString("name"), is("IuXmrISMSm zKUPL1pbbN"));
        assertThat(aagResponse.get().getJsonArray("respondentDetails").getJsonObject(0).getString("name"), is("WBHE1n0bUr"));
    }

    @Test
    public void shouldRetrieveApplicationOnly() {
        JsonEnvelope jsonEnvelope = getUserEnvelope(PROGRESSION_QUERY_APPLICATION_ONLY);
        when(requester.request(any(), any(Class.class))).thenReturn(jsonEnvelope);

        Optional<JsonObject> applicationOnlyResponse = progressionService.getApplicationOnly(jsonEnvelope, APPLICATION_ID_ONLY.toString());

        assertThat(applicationOnlyResponse.get().getJsonObject("courtApplication")
                .getString("id"), is("12615f6e-b1de-485c-ae69-e989445b988e"));
        assertThat(applicationOnlyResponse.get().getJsonObject("courtApplication")
                .getString("parentApplicationId"), is("6823d502-a0ec-4861-a39f-438e28d3af13"));
    }

    private JsonEnvelope getUserEnvelope(final String name) {
        return envelopeFrom(
                metadataBuilder().
                        withName(name).
                        withId(randomUUID()),
                createReader(getClass().getClassLoader().
                        getResourceAsStream(name.concat(".json"))).
                        readObject()
        );
    }

}