package uk.gov.moj.cpp.hearing.event;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.hearing.courts.referencedata.CourtCentreOrganisationUnit;
import uk.gov.justice.hearing.courts.referencedata.OuCourtRoomsResult;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher;
import uk.gov.moj.cpp.hearing.domain.DefendantInfoQueryResult;
import uk.gov.moj.cpp.hearing.domain.DefendantOutstandingFineRequests;
import uk.gov.moj.cpp.hearing.domain.DefendantOutstandingFineRequestsResult;
import uk.gov.moj.cpp.hearing.domain.event.OutstandingFinesRequested;
import uk.gov.moj.cpp.hearing.event.service.CourtHouseReverseLookup;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OutstandingFinesRequestedProcessorTest {

    public static final int outstandingFinesBatchSize = 5;

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @Captor
    private ArgumentCaptor<Class> objectTypeArgumentCaptor;

    @Mock
    private Requester requester;

    @Mock
    private CourtHouseReverseLookup courtHouseReverseLookup;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Mock
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;


    @InjectMocks
    private OutstandingFinesRequestedProcessor outstandingFinesRequestedProcessor;

    private UUID courtCentreId1 = randomUUID();
    private UUID courtCentreId2 = randomUUID();
    private UUID defendantId1 = randomUUID();
    private UUID defendantId2 = randomUUID();

    @Before
    public void initMocks() {
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.outstandingFinesRequestedProcessor, "outstandingFinesBatchSize", outstandingFinesBatchSize);
    }


    @Test
    public void publicComputeOutstandingFinesRequested() {
        final JsonObject outstandingFinesQuery = Json.createObjectBuilder().build();
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.outstanding-fines-requested"),
                outstandingFinesQuery);

        final OutstandingFinesRequested outstandingFinesRequested = OutstandingFinesRequested.newBuilder()
                .withHearingDate(LocalDate.parse("2020-05-21"))
                .build();
        when(jsonObjectToObjectConverter.convert(outstandingFinesQuery, OutstandingFinesRequested.class)).thenReturn(outstandingFinesRequested);

        final Envelope<JsonObject> courtBasedDefendantQueryInformation = mock(Envelope.class);
        when(requester.requestAsAdmin(envelopeArgumentCaptor.capture(), objectTypeArgumentCaptor.capture())).thenReturn(courtBasedDefendantQueryInformation);
        final JsonObject courtBasedDefendantQueryJsonObject = Json.createObjectBuilder().build();
        when(courtBasedDefendantQueryInformation.payload()).thenReturn(courtBasedDefendantQueryJsonObject);
        when(jsonObjectToObjectConverter.convert(courtBasedDefendantQueryJsonObject, DefendantOutstandingFineRequestsResult.class)).thenReturn(getDefendantInfoQueryResult());


        when(courtHouseReverseLookup.getCourtRoomsResult(event)).thenReturn(getCourtRoomResults());

        doNothing().when(sender).send(envelopeArgumentCaptor.capture());

        outstandingFinesRequestedProcessor.hearingOutstandingFinesRequested(event);

        final List<JsonEnvelope> allValues = envelopeArgumentCaptor.getAllValues();
        final JsonEnvelope defendantInfoQueryEnvelope = allValues.get(0);
        final JsonEnvelope requestOutstandingFinesEnvelope = allValues.get(1);

        assertThat(defendantInfoQueryEnvelope.metadata(), withMetadataEnvelopedFrom(event)
                .withName("hearing.defendant.outstanding-fine-requests"));
        assertThat(defendantInfoQueryEnvelope.payload(),
                JsonEnvelopePayloadMatcher.payloadIsJson(
                        allOf(
                                withJsonPath("$.hearingDate", is("2020-05-21"))
                        )))
        ;
        assertThat(requestOutstandingFinesEnvelope.metadata(), withMetadataEnvelopedFrom(event)
                .withCausationIds()
                .withName("stagingenforcement.request-outstanding-fine"));
        assertThat(requestOutstandingFinesEnvelope.payload(),
                JsonEnvelopePayloadMatcher.payloadIsJson(
                        allOf(
                                withJsonPath("$.fineRequests.[0].defendantId", is(defendantId1.toString())),
                                withJsonPath("$.fineRequests.[0].ouCode", is("oucode1")),
                                withJsonPath("$.fineRequests.[1].defendantId", is(defendantId2.toString())),
                                withJsonPath("$.fineRequests.[1].ouCode", is("oucode2"))
                        )));


    }


    @Test
    public void publicComputeOutstandingFinesRequestedWithEmptyQuery() {
        final JsonObject outstandingFinesQuery = Json.createObjectBuilder().build();
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.outstanding-fines-requested"),
                outstandingFinesQuery);

        final OutstandingFinesRequested outstandingFinesRequested = OutstandingFinesRequested.newBuilder()
                .withHearingDate(LocalDate.parse("2020-05-21"))
                .build();
        when(jsonObjectToObjectConverter.convert(outstandingFinesQuery, OutstandingFinesRequested.class)).thenReturn(outstandingFinesRequested);

        final JsonEnvelope courtBasedDefendantQueryInformation = mock(JsonEnvelope.class);
        when(requester.requestAsAdmin(envelopeArgumentCaptor.capture(), objectTypeArgumentCaptor.capture())).thenReturn(courtBasedDefendantQueryInformation);
        final JsonObject courtBasedDefendantQueryJsonObject = Json.createObjectBuilder().build();
        when(courtBasedDefendantQueryInformation.payloadAsJsonObject()).thenReturn(courtBasedDefendantQueryJsonObject);

        when(jsonObjectToObjectConverter.convert(courtBasedDefendantQueryJsonObject, DefendantInfoQueryResult.class)).thenReturn(new DefendantInfoQueryResult());

        outstandingFinesRequestedProcessor.hearingOutstandingFinesRequested(event);

        final List<JsonEnvelope> allValues = envelopeArgumentCaptor.getAllValues();
        final JsonEnvelope defendantInfoQueryEnvelope = allValues.get(0);

        assertThat(defendantInfoQueryEnvelope.metadata(), withMetadataEnvelopedFrom(event)
                .withName("hearing.defendant.outstanding-fine-requests"));
        assertThat(defendantInfoQueryEnvelope.payload(),
                JsonEnvelopePayloadMatcher.payloadIsJson(
                        allOf(
                                withJsonPath("$.hearingDate", is("2020-05-21"))
                        )));
        verifyZeroInteractions(sender, courtHouseReverseLookup);

    }

    @Test
    public void publicComputeOutstandingFinesRequestedWithBatches() {
        final JsonObject outstandingFinesQuery = Json.createObjectBuilder().build();
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.outstanding-fines-requested"),
                outstandingFinesQuery);

        final OutstandingFinesRequested outstandingFinesRequested = OutstandingFinesRequested.newBuilder()
                .withHearingDate(LocalDate.parse("2020-05-21"))
                .build();
        when(jsonObjectToObjectConverter.convert(outstandingFinesQuery, OutstandingFinesRequested.class)).thenReturn(outstandingFinesRequested);

        final Envelope<JsonObject> courtBasedDefendantQueryInformation = mock(Envelope.class);
        when(requester.requestAsAdmin(envelopeArgumentCaptor.capture(), objectTypeArgumentCaptor.capture())).thenReturn(courtBasedDefendantQueryInformation);

        final JsonObject courtBasedDefendantQueryJsonObject = Json.createObjectBuilder().build();
        when(courtBasedDefendantQueryInformation.payload()).thenReturn(courtBasedDefendantQueryJsonObject);
        DefendantOutstandingFineRequestsResult randomDefendantInfoQueryResult = getRandomDefendantInfoQueryResult();
        when(jsonObjectToObjectConverter.convert(courtBasedDefendantQueryJsonObject, DefendantOutstandingFineRequestsResult.class)).thenReturn(randomDefendantInfoQueryResult);


        when(courtHouseReverseLookup.getCourtRoomsResult(event)).thenReturn(getCourtRoomResults());

        doNothing().when(sender).send(envelopeArgumentCaptor.capture());

        outstandingFinesRequestedProcessor.hearingOutstandingFinesRequested(event);

        final List<JsonEnvelope> allValues = envelopeArgumentCaptor.getAllValues();
        final JsonEnvelope defendantInfoQueryEnvelope = allValues.get(0);
        final JsonEnvelope requestOutstandingFinesEnvelope = allValues.get(1);

        int numberOfBatches = (int) Math.ceil(randomDefendantInfoQueryResult.getDefendantDetails().size() / (float) outstandingFinesBatchSize) + 1;
        assertThat(allValues.size(), is(numberOfBatches));

        assertThat(defendantInfoQueryEnvelope.metadata(), withMetadataEnvelopedFrom(event)
                .withName("hearing.defendant.outstanding-fine-requests"));
        assertThat(defendantInfoQueryEnvelope.payload(),
                JsonEnvelopePayloadMatcher.payloadIsJson(
                        allOf(
                                withJsonPath("$.hearingDate", is("2020-05-21"))
                        )))
        ;
        assertThat(requestOutstandingFinesEnvelope.metadata(), withMetadataEnvelopedFrom(event)
                .withCausationIds()
                .withName("stagingenforcement.request-outstanding-fine"));

    }

    private DefendantOutstandingFineRequestsResult getRandomDefendantInfoQueryResult() {
        final Integer size = INTEGER.next() % 100 + 10;
        final ArrayList<DefendantOutstandingFineRequests> defendantDetails = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            defendantDetails.add(
                    DefendantOutstandingFineRequests.newBuilder()
                            .withDefendantId(randomUUID())
                            .withCaseId(randomUUID())
                            .withCourtCentreId(courtCentreId2)
                            .build());
        }
        return new DefendantOutstandingFineRequestsResult(defendantDetails);
    }

    private OuCourtRoomsResult getCourtRoomResults() {
        return OuCourtRoomsResult.ouCourtRoomsResult()
                .withOrganisationunits(Arrays.asList(
                        CourtCentreOrganisationUnit.courtCentreOrganisationUnit()
                                .withId(courtCentreId1.toString())
                                .withOucode("oucode1")
                                .build(),
                        CourtCentreOrganisationUnit.courtCentreOrganisationUnit()
                                .withId(courtCentreId2.toString())
                                .withOucode("oucode2")
                                .build(),
                        CourtCentreOrganisationUnit.courtCentreOrganisationUnit()
                                .withId(randomUUID().toString())
                                .withOucode("oucode3")
                                .build()
                ))
                .build();
    }

    private DefendantOutstandingFineRequestsResult getDefendantInfoQueryResult() {
        return new DefendantOutstandingFineRequestsResult(Arrays.asList(
                DefendantOutstandingFineRequests.newBuilder()
                        .withDefendantId(defendantId1)
                        .withCourtCentreId(courtCentreId1)
                        .build(),
                DefendantOutstandingFineRequests.newBuilder()
                        .withDefendantId(defendantId2)
                        .withCourtCentreId(courtCentreId2)
                        .build()
        ));
    }
}
