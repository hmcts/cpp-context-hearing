package uk.gov.moj.cpp.hearing.command.handler.service;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnvelopeFactory.createEnvelope;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReferenceDataServiceTest {

    private Enveloper enveloper = createEnveloper();

    @Mock
    private Requester requester;

    @InjectMocks
    private ReferenceDataService referenceDataService;

    @Before
    public void setup() {
        referenceDataService.setEnveloper(enveloper);
    }

    @Test
    public void getAllCrownCourtCentresSuccessfully() {

        final JsonEnvelope inputEnvelope =
                envelopeFrom(
                        metadataWithDefaults(),
                        createObjectBuilder().build());

        final List<UUID> expectedCourtCentreIds = Arrays.asList(randomUUID(), randomUUID());

        final JsonEnvelope returnedResponseEnvelope = generateReferenceDataServiceResponse(expectedCourtCentreIds);
        when(requester.requestAsAdmin(any(JsonEnvelope.class))).thenReturn(returnedResponseEnvelope);
        ArgumentCaptor<JsonEnvelope> argumentCaptorForRequestEnvelope = ArgumentCaptor.forClass(JsonEnvelope.class);

        final List<UUID> courtCentreIds = referenceDataService.getAllCrownCourtCentres(inputEnvelope);

        verify(requester).requestAsAdmin(argumentCaptorForRequestEnvelope.capture());
        final JsonEnvelope requestEnvelope = argumentCaptorForRequestEnvelope.getValue();
        assertThat(requestEnvelope.metadata().name(), is("referencedata.query.courtrooms"));
        final JsonObject expectedPayload = createObjectBuilder()
                .add("oucodeL1Code", "C")
                .build();
        final JsonObject payloadOfRequestEnvelope = requestEnvelope.payloadAsJsonObject();
        assertThat(payloadOfRequestEnvelope, is(expectedPayload));

        assertThat(courtCentreIds.get(0), is(expectedCourtCentreIds.get(0)));
        assertThat(courtCentreIds.get(1), is(expectedCourtCentreIds.get(1)));
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
