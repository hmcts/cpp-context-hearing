package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createReader;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;

import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;
import java.util.UUID;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProgressionServiceTest {

    @Mock
    private Requester requester;

    @Mock
    private JsonEnvelope jsonEnvelope;

    @InjectMocks
    private ProgressionService progressionService;

    private final String PROGRESSION_QUERY_APPLICATIONS = "progression.query.application.aaag.json";
    private static final UUID APPLICATION_ID = randomUUID();

    @Test
    public void shouldRetrieveAAAGDetailsByApplicationId() {

        JsonEnvelope jsonEnvelope = getUserEnvelope(PROGRESSION_QUERY_APPLICATIONS);
        when(requester.request(any(JsonEnvelope.class), any(Class.class))).thenReturn(jsonEnvelope);

        Optional<JsonObject> aagResponse = progressionService.getApplication(jsonEnvelope, APPLICATION_ID.toString());

        assertThat(aagResponse.get().getString("applicationId"), is("7270191a-54c2-11ea-a2e3-2e728ce88125"));
        assertThat(aagResponse.get().getJsonObject("applicantDetails").getString("name"), is("IuXmrISMSm zKUPL1pbbN"));
        assertThat(aagResponse.get().getJsonArray("respondentDetails").getJsonObject(0).getString("name"), is("WBHE1n0bUr"));
    }


    private JsonEnvelope getUserEnvelope(String fileName) {
        return envelopeFrom(
                metadataBuilder().
                        withName(PROGRESSION_QUERY_APPLICATIONS).
                        withId(randomUUID()),
                createReader(getClass().getClassLoader().
                        getResourceAsStream(fileName)).
                        readObject()
        );
    }

}