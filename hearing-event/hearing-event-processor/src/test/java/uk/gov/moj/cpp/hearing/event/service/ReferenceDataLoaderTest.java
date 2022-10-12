package uk.gov.moj.cpp.hearing.event.service;

import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.enveloper.EnvelopeFactory.createEnvelope;

import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReferenceDataLoaderTest {
    @Mock
    private Requester requester;

    @InjectMocks
    private ReferenceDataLoader referenceDataLoader;

    @Test
    public void getAllCrownCourtCentresSuccessfully() {

        final List<UUID> expectedCourtCentreIds = Arrays.asList(randomUUID(), randomUUID());

        final JsonEnvelope returnedResponseEnvelope = generateReferenceDataServiceResponse(expectedCourtCentreIds);
        when(requester.requestAsAdmin(any(JsonEnvelope.class))).thenReturn(returnedResponseEnvelope);
        ArgumentCaptor<JsonEnvelope> argumentCaptorForRequestEnvelope = ArgumentCaptor.forClass(JsonEnvelope.class);

        final List<JsonObject> courtCentreIds = referenceDataLoader.getAllCrownCourtCentres();

        verify(requester).requestAsAdmin(argumentCaptorForRequestEnvelope.capture());
        final JsonEnvelope requestEnvelope = argumentCaptorForRequestEnvelope.getValue();
        assertThat(requestEnvelope.metadata().name(), is("referencedata.query.courtrooms"));
        final JsonObject expectedPayload = createObjectBuilder()
                .add("oucodeL1Code", "C")
                .build();
        final JsonObject payloadOfRequestEnvelope = requestEnvelope.payloadAsJsonObject();
        assertThat(payloadOfRequestEnvelope, is(expectedPayload));

        assertThat(fromString(courtCentreIds.get(0).getString("id")), is(expectedCourtCentreIds.get(0)));
        assertThat(fromString(courtCentreIds.get(1).getString("id")), is(expectedCourtCentreIds.get(1)));
    }

    private JsonEnvelope generateReferenceDataServiceResponse(final List<UUID> expectedCourtCentreIds) {
        return createEnvelope(".", createObjectBuilder()
                .add("organisationunits", Json.createArrayBuilder()
                        .add(buildOrgUnit(expectedCourtCentreIds.get(0)))
                        .add(buildOrgUnit(expectedCourtCentreIds.get(1)))
                )
                .build());
    }

    private JsonObject buildOrgUnit(final UUID courtCentreId) {
        return Json.createObjectBuilder()
                .add("id", courtCentreId.toString())
                .build();
    }
}
