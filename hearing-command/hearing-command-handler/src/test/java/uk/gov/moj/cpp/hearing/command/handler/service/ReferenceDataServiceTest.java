package uk.gov.moj.cpp.hearing.command.handler.service;

import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.messaging.JsonObjects.createArrayBuilder;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnvelopeFactory.createEnvelope;

import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.CourtCentre;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;


import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ReferenceDataServiceTest {
    private static final String FIELD_PLEA_STATUS_TYPES = "pleaStatusTypes";
    private static final String FIELD_PLEA_TYPE_GUILTY_FLAG = "pleaTypeGuiltyFlag";
    private static final String GUILTY_FLAG_YES = "Yes";
    private static final String GUILTY_FLAG_NO = "No";
    private static final String FIELD_PLEA_VALUE = "pleaValue";

    private static final String GUILTY = "GUILTY";
    private static final String NOT_GUILTY = "NOT_GUILTY";

    @Mock
    private Requester requester;

    @InjectMocks
    private ReferenceDataService referenceDataService;

    @Test
    public void getAllCrownCourtCentresSuccessfully() {

        final List<UUID> expectedCourtCentreIds = Arrays.asList(randomUUID(), randomUUID());

        final JsonEnvelope returnedResponseEnvelope = generateReferenceDataServiceResponse(expectedCourtCentreIds);
        when(requester.requestAsAdmin(any(JsonEnvelope.class))).thenReturn(returnedResponseEnvelope);
        ArgumentCaptor<JsonEnvelope> argumentCaptorForRequestEnvelope = ArgumentCaptor.forClass(JsonEnvelope.class);

        final List<JsonObject> courtCentreIds = referenceDataService.getAllCrownCourtCentres();

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

    @Test
    public void shouldRetrievePleaTypeGuiltyFlags(){
        final Envelope envelope = envelopeFrom(metadataBuilder().withId(UUID.randomUUID()).withName("name").build(), buildPleaStatusTypesPayload());
        when(requester.requestAsAdmin(any(), eq(JsonObject.class))).thenReturn(envelope);
        final Set<String> actual = referenceDataService.retrieveGuiltyPleaTypes();
        assertThat(actual.size(), is(1));
        assertThat(actual.contains(GUILTY), is(true));
        assertThat(actual.contains(NOT_GUILTY), is(false));
    }

    private JsonObject buildPleaStatusTypesPayload(){
        return createObjectBuilder().add(FIELD_PLEA_STATUS_TYPES, createArrayBuilder()
                .add(createObjectBuilder().add(FIELD_PLEA_VALUE, GUILTY).add(FIELD_PLEA_TYPE_GUILTY_FLAG, GUILTY_FLAG_YES))
                .add(createObjectBuilder().add(FIELD_PLEA_VALUE, NOT_GUILTY).add(FIELD_PLEA_TYPE_GUILTY_FLAG, GUILTY_FLAG_NO)))
                .build();
    }

    private JsonEnvelope generateReferenceDataServiceResponse(final List<UUID> expectedCourtCentreIds) {
        return createEnvelope(".", createObjectBuilder()
                .add("organisationunits", createArrayBuilder()
                        .add(buildOrgUnit(expectedCourtCentreIds.get(0)))
                        .add(buildOrgUnit(expectedCourtCentreIds.get(1)))
                )
                .build());
    }

    private JsonObject buildOrgUnit(final UUID courtCentreId) {
        return createObjectBuilder()
                .add("id", courtCentreId.toString())
                .build();
    }

    @Test
    public void resolveCourtCentre_shouldReturnFullCourtCentreFromReferenceData() {
        final UUID courtCentreId = randomUUID();
        final UUID courtRoomId = randomUUID();

        final JsonEnvelope responseEnvelope = createEnvelope(".",
                createObjectBuilder()
                        .add("organisationunits", createArrayBuilder()
                                .add(createObjectBuilder()
                                        .add("id", courtCentreId.toString())
                                        .add("oucodeL3Name", "Centre English")
                                        .add("oucodeL3WelshName", "Centre Welsh")
                                        .add("courtrooms", createArrayBuilder()
                                                .add(createObjectBuilder()
                                                        .add("id", randomUUID().toString())
                                                        .add("courtroomName", "Other Room")
                                                        .add("welshCourtroomName", "Other Welsh Room"))
                                                .add(createObjectBuilder()
                                                        .add("id", courtRoomId.toString())
                                                        .add("courtroomName", "Target Room")
                                                        .add("welshCourtroomName", "Welsh Target Room")))))
                        .build());
        when(requester.requestAsAdmin(any(JsonEnvelope.class))).thenReturn(responseEnvelope);
        ArgumentCaptor<JsonEnvelope> captor = ArgumentCaptor.forClass(JsonEnvelope.class);

        final Optional<CourtCentre> result = referenceDataService.resolveCourtCentre(courtCentreId, courtRoomId);

        verify(requester).requestAsAdmin(captor.capture());
        assertThat(captor.getValue().metadata().name(), is("referencedata.query.courtrooms"));
        assertThat(captor.getValue().payloadAsJsonObject().getString("courtCentreId"), is(courtCentreId.toString()));

        assertThat(result.isPresent(), is(true));
        final CourtCentre cc = result.get();
        assertThat(cc.getId(), is(courtCentreId));
        assertThat(cc.getName(), is("Centre English"));
        assertThat(cc.getWelshName(), is("Centre Welsh"));
        assertThat(cc.getRoomId(), is(courtRoomId));
        assertThat(cc.getRoomName(), is("Target Room"));
        assertThat(cc.getWelshRoomName(), is("Welsh Target Room"));
    }

    @Test
    public void resolveCourtCentre_shouldReturnEmpty_whenRoomNotFoundInOU() {
        final UUID courtCentreId = randomUUID();
        final UUID courtRoomId = randomUUID();

        final JsonEnvelope responseEnvelope = createEnvelope(".",
                createObjectBuilder()
                        .add("organisationunits", createArrayBuilder()
                                .add(createObjectBuilder()
                                        .add("id", courtCentreId.toString())
                                        .add("oucodeL3Name", "Centre")
                                        .add("courtrooms", createArrayBuilder()
                                                .add(createObjectBuilder()
                                                        .add("id", randomUUID().toString())
                                                        .add("courtroomName", "Other Room")))))
                        .build());
        when(requester.requestAsAdmin(any(JsonEnvelope.class))).thenReturn(responseEnvelope);

        final Optional<CourtCentre> result = referenceDataService.resolveCourtCentre(courtCentreId, courtRoomId);

        assertThat(result.isPresent(), is(false));
    }

    @Test
    public void resolveCourtCentre_shouldReturnEmpty_whenInputsAreNull() {
        assertThat(referenceDataService.resolveCourtCentre(null, randomUUID()).isPresent(), is(false));
        assertThat(referenceDataService.resolveCourtCentre(randomUUID(), null).isPresent(), is(false));
        assertThat(referenceDataService.resolveCourtCentre(null, null).isPresent(), is(false));
    }

    @Test
    public void resolveCourtCentre_shouldReturnEmpty_whenNoOrganisationUnits() {
        final JsonEnvelope responseEnvelope = createEnvelope(".",
                createObjectBuilder()
                        .add("organisationunits", createArrayBuilder())
                        .build());
        when(requester.requestAsAdmin(any(JsonEnvelope.class))).thenReturn(responseEnvelope);

        final Optional<CourtCentre> result = referenceDataService.resolveCourtCentre(randomUUID(), randomUUID());

        assertThat(result.isPresent(), is(false));
    }
}
