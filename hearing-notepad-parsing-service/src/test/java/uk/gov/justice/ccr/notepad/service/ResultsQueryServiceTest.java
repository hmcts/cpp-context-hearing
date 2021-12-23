package uk.gov.justice.ccr.notepad.service;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.time.LocalDate;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ResultsQueryServiceTest {

    private static final String REFERENCEDATA_GET_ALL_FIXED_LIST = "referencedata.get-all-fixed-list";
    private static final String REFERENCEDATA_GET_ALL_RESULT_DEFINITIONS = "referencedata.get-all-result-definitions";
    private static final String REFERENCEDATA_GET_ALL_RESULT_WORD_SYNONYMS = "referencedata.get-all-result-word-synonyms";
    private static final String REFERENCEDATA_RESULT_GET_ALL_PROMPT_KEYWORD_SYNONYMS = "referencedata.get-all-prompt-synonyms";
    private static final String REFERENCEDATA_GET_ALL_RESULT_PROMPT_WORD_SYNONYMS = "referencedata.get-all-result-prompt-word-synonyms";
    private final LocalDate hearingDate = LocalDate.parse("2018-05-01");

    @InjectMocks
    private ResultsQueryService resultsQueryService;

    @Mock
    private Requester requester;

    @Captor
    private ArgumentCaptor<Envelope<JsonObject>> captor;

    @Test
    public void shouldGetAllResultDefinitionForAGivenDate() {
        //Given
        final JsonEnvelope envelope = buildDummyJsonRequestEnvelopeWithName(REFERENCEDATA_GET_ALL_RESULT_DEFINITIONS);

        resultsQueryService.getAllDefinitions(envelope, hearingDate);

        verify(requester).requestAsAdmin(captor.capture(), Mockito.eq(JsonObject.class));

        final Envelope<JsonObject> jsonEnvelope = captor.getValue();

        assertThat(JsonEnvelope.envelopeFrom(jsonEnvelope.metadata(), jsonEnvelope.payload()), is(jsonEnvelope(
                withMetadataEnvelopedFrom(envelope)
                        .withName(REFERENCEDATA_GET_ALL_RESULT_DEFINITIONS),
                payloadIsJson(allOf(
                        withJsonPath("$.on", equalTo("2018-05-01"))
                )))
        ));
    }

    @Test
    public void shouldGetAllResultDefinitionSynonyms() {
        //Given

        final JsonEnvelope envelope = buildDummyJsonRequestEnvelopeWithName(REFERENCEDATA_GET_ALL_RESULT_WORD_SYNONYMS);

        resultsQueryService.getAllDefinitionWordSynonyms(envelope, hearingDate);

        verify(requester).requestAsAdmin(captor.capture(), Mockito.eq(JsonObject.class));

        final Envelope<JsonObject> jsonEnvelope = captor.getValue();

        assertThat(JsonEnvelope.envelopeFrom(jsonEnvelope.metadata(), jsonEnvelope.payload()), is(jsonEnvelope(
                withMetadataEnvelopedFrom(envelope)
                        .withName(REFERENCEDATA_GET_ALL_RESULT_WORD_SYNONYMS),
                payloadIsJson(allOf(
                        withJsonPath("$.on", equalTo("2018-05-01"))
                )))
        ));
    }

    @Test
    public void shouldGetAllResultPromptSynonyms() {
        //Given

        final JsonEnvelope envelope = buildDummyJsonRequestEnvelopeWithName(REFERENCEDATA_RESULT_GET_ALL_PROMPT_KEYWORD_SYNONYMS);

        resultsQueryService.getAllResultPromptWordSynonyms(envelope, hearingDate);

        verify(requester).requestAsAdmin(captor.capture(), Mockito.eq(JsonObject.class));

        final Envelope<JsonObject> jsonEnvelope = captor.getValue();

        assertThat(JsonEnvelope.envelopeFrom(jsonEnvelope.metadata(), jsonEnvelope.payload()), is(jsonEnvelope(
                withMetadataEnvelopedFrom(envelope)
                        .withName(REFERENCEDATA_GET_ALL_RESULT_PROMPT_WORD_SYNONYMS),
                payloadIsJson(allOf(
                        withJsonPath("$.on", equalTo("2018-05-01"))
                )))
        ));
    }

    @Test
    public void shouldGetFixedLists() {
        //Given
        final JsonEnvelope envelope = buildDummyJsonRequestEnvelopeWithName(REFERENCEDATA_GET_ALL_FIXED_LIST);

        resultsQueryService.getAllFixedLists(envelope, hearingDate);

        verify(requester).requestAsAdmin(captor.capture(), Mockito.eq(JsonObject.class));

        final Envelope<JsonObject> jsonEnvelope = captor.getValue();

        assertThat(JsonEnvelope.envelopeFrom(jsonEnvelope.metadata(), jsonEnvelope.payload()), is(jsonEnvelope(
                withMetadataEnvelopedFrom(envelope)
                        .withName(REFERENCEDATA_GET_ALL_FIXED_LIST),
                payloadIsJson(allOf(
                        withJsonPath("$.on", equalTo("2018-05-01"))
                )))
        ));
    }

    @Test
    public void shouldGetAllCourtCentres() {
        final JsonEnvelope envelope = buildDummyJsonRequestEnvelopeWithName(REFERENCEDATA_GET_ALL_FIXED_LIST);

        resultsQueryService.getAllCourtCentre(envelope);

        verify(requester).requestAsAdmin(captor.capture(), Mockito.eq(JsonObject.class));
        assertThat(captor.getValue().metadata().name(), is("referencedata.query.courtrooms"));
    }

    @Test
    public void shouldGetLocalJusticeAreas() {
        final JsonEnvelope envelope = buildDummyJsonRequestEnvelope();

        resultsQueryService.getLocalJusticeAreas(envelope);

        verify(requester).requestAsAdmin(captor.capture(), Mockito.eq(JsonObject.class));
        assertThat(captor.getValue().metadata().name(), is("referencedata.query.local-justice-areas"));
    }

    @Test
    public void shouldGetCountriesNames() {
        final JsonEnvelope envelope = buildDummyJsonRequestEnvelope();

        resultsQueryService.getCountriesNames(envelope);

        verify(requester).requestAsAdmin(captor.capture(), Mockito.eq(JsonObject.class));
        assertThat(captor.getValue().metadata().name(), is("referencedata.query.country-nationality"));
    }

    @Test
    public void shouldGetLanguages() {
        final JsonEnvelope envelope = buildDummyJsonRequestEnvelope();

        resultsQueryService.getLanguages(envelope);

        verify(requester).requestAsAdmin(captor.capture(), Mockito.eq(JsonObject.class));
        assertThat(captor.getValue().metadata().name(), is("referencedata.query.languages"));
    }

    @Test
    public void shouldGetPrisonNameAddress() {
        final JsonEnvelope envelope = buildDummyJsonRequestEnvelope();

        resultsQueryService.getPrisonNameAddress(envelope);

        verify(requester).requestAsAdmin(captor.capture(), Mockito.eq(JsonObject.class));
        assertThat(captor.getValue().metadata().name(), is("referencedata.query.prisons"));
    }

    @Test
    public void shouldGetRegionalOrganisationNameAddress() {
        final JsonEnvelope envelope = buildDummyJsonRequestEnvelope();

        resultsQueryService.getRegionalOrganisationNameAddress(envelope);

        verify(requester).requestAsAdmin(captor.capture(), Mockito.eq(JsonObject.class));
        assertThat(captor.getValue().metadata().name(), is("referencedata.query.regional-organisations"));
    }

    @Test
    public void shouldGetYOTSNameAddress() {
        final JsonEnvelope envelope = buildDummyJsonRequestEnvelope();

        resultsQueryService.getYOTSNameAddress(envelope);

        verify(requester).requestAsAdmin(captor.capture(), Mockito.eq(JsonObject.class));
        assertThat(captor.getValue().metadata().name(), is("referencedata.query.youth-offending-teams"));
    }

    @Test
    public void shouldGetScottishCourtAddress() {
        final JsonEnvelope envelope = buildDummyJsonRequestEnvelope();

        resultsQueryService.getScottishCourtAddress(envelope);

        verify(requester).requestAsAdmin(captor.capture(), Mockito.eq(JsonObject.class));
        assertThat(captor.getValue().metadata().name(), is("referencedata.query.scottish-ni-courts"));
    }

    @Test
    public void shouldGetYouthCourtAddress() {
        final JsonEnvelope envelope = buildDummyJsonRequestEnvelope();

        resultsQueryService.getYouthCourtAddress(envelope);

        verify(requester).requestAsAdmin(captor.capture(), Mockito.eq(JsonObject.class));
        assertThat(captor.getValue().metadata().name(), is("referencedata.query.youth-courts"));
    }

    @Test
    public void shouldGetHearingTypes() {
        final JsonEnvelope envelope = buildDummyJsonRequestEnvelope();

        resultsQueryService.getHearingTypes(envelope);

        verify(requester).requestAsAdmin(captor.capture(), Mockito.eq(JsonObject.class));
        assertThat(captor.getValue().metadata().name(), is("referencedata.query.hearing-types"));
    }

    @Test
    public void shouldGetCrownCourtsNameAddress() {
        final JsonEnvelope envelope = buildDummyJsonRequestEnvelope();

        resultsQueryService.getCrownCourtsNameAddress(envelope);

        verify(requester).requestAsAdmin(captor.capture(), Mockito.eq(JsonObject.class));

        final Envelope<JsonObject> capturedEnvelope = captor.getValue();
        assertThat(JsonEnvelope.envelopeFrom(capturedEnvelope.metadata(), capturedEnvelope.payload()), is(jsonEnvelope(
                withMetadataEnvelopedFrom(envelope)
                        .withName("referencedata.query.organisationunits"),
                payloadIsJson(allOf(
                        withJsonPath("$.oucodeL1Code", equalTo("C"))
                )))
        ));
    }

    @Test
    public void shouldGetMagistrateCourtsNameAddress() {
        final JsonEnvelope envelope = buildDummyJsonRequestEnvelope();

        resultsQueryService.getMagistrateCourtsNameAddress(envelope);

        verify(requester).requestAsAdmin(captor.capture(), Mockito.eq(JsonObject.class));

        final Envelope<JsonObject> capturedEnvelope = captor.getValue();
        assertThat(JsonEnvelope.envelopeFrom(capturedEnvelope.metadata(), capturedEnvelope.payload()), is(jsonEnvelope(
                withMetadataEnvelopedFrom(envelope)
                        .withName("referencedata.query.organisationunits"),
                payloadIsJson(allOf(
                        withJsonPath("$.oucodeL1Code", equalTo("B"))
                )))
        ));
    }

    @Test
    public void shouldGetBASSProviderNameAddress() {
        final JsonEnvelope envelope = buildDummyJsonRequestEnvelope();

        resultsQueryService.getBASSProviderNameAddress(envelope);

        verify(requester).requestAsAdmin(captor.capture(), Mockito.eq(JsonObject.class));

        final Envelope<JsonObject> capturedEnvelope = captor.getValue();
        assertThat(JsonEnvelope.envelopeFrom(capturedEnvelope.metadata(), capturedEnvelope.payload()), is(jsonEnvelope(
                withMetadataEnvelopedFrom(envelope)
                        .withName("referencedata.query.organisation.byorgtype"),
                payloadIsJson(allOf(
                        withJsonPath("$.orgType", equalTo("BASS"))
                )))
        ));
    }

    @Test
    public void shouldGetEMCOrganisationNameAddress() {
        final JsonEnvelope envelope = buildDummyJsonRequestEnvelope();

        resultsQueryService.getEMCOrganisationNameAddress(envelope);

        verify(requester).requestAsAdmin(captor.capture(), Mockito.eq(JsonObject.class));

        final Envelope<JsonObject> capturedEnvelope = captor.getValue();
        assertThat(JsonEnvelope.envelopeFrom(capturedEnvelope.metadata(), capturedEnvelope.payload()), is(jsonEnvelope(
                withMetadataEnvelopedFrom(envelope)
                        .withName("referencedata.query.organisation.byorgtype"),
                payloadIsJson(allOf(
                        withJsonPath("$.orgType", equalTo("EMC"))
                )))
        ));
    }

    @Test
    public void shouldGetLocalAuthorityNameAddress() {
        final JsonEnvelope envelope = buildDummyJsonRequestEnvelope();

        resultsQueryService.getLocalAuthorityNameAddress(envelope);

        verify(requester).requestAsAdmin(captor.capture(), Mockito.eq(JsonObject.class));

        final Envelope<JsonObject> capturedEnvelope = captor.getValue();
        assertThat(JsonEnvelope.envelopeFrom(capturedEnvelope.metadata(), capturedEnvelope.payload()), is(jsonEnvelope(
                withMetadataEnvelopedFrom(envelope)
                        .withName("referencedata.query.organisation.byorgtype"),
                payloadIsJson(allOf(
                        withJsonPath("$.orgType", equalTo("DESLA"))
                )))
        ));
    }

    @Test
    public void shouldGetNCESNameAddress() {
        final JsonEnvelope envelope = buildDummyJsonRequestEnvelope();

        resultsQueryService.getNCESNameAddress(envelope);

        verify(requester).requestAsAdmin(captor.capture(), Mockito.eq(JsonObject.class));

        final Envelope<JsonObject> capturedEnvelope = captor.getValue();
        assertThat(JsonEnvelope.envelopeFrom(capturedEnvelope.metadata(), capturedEnvelope.payload()), is(jsonEnvelope(
                withMetadataEnvelopedFrom(envelope)
                        .withName("referencedata.query.organisation.byorgtype"),
                payloadIsJson(allOf(
                        withJsonPath("$.orgType", equalTo("NCESCOST"))
                )))
        ));
    }

    @Test
    public void shouldGetDrinkDrivingCourseProvidersAddress() {
        final JsonEnvelope envelope = buildDummyJsonRequestEnvelope();

        resultsQueryService.getDrinkDrivingCourseProvidersAddress(envelope);

        verify(requester).requestAsAdmin(captor.capture(), Mockito.eq(JsonObject.class));

        final Envelope<JsonObject> capturedEnvelope = captor.getValue();
        assertThat(JsonEnvelope.envelopeFrom(capturedEnvelope.metadata(), capturedEnvelope.payload()), is(jsonEnvelope(
                withMetadataEnvelopedFrom(envelope)
                        .withName("referencedata.query.organisation.byorgtype"),
                payloadIsJson(allOf(
                        withJsonPath("$.orgType", equalTo("DDRP"))
                )))
        ));
    }

    @Test
    public void shouldGetProbationNameAddress() {
        final JsonEnvelope envelope = buildDummyJsonRequestEnvelope();

        resultsQueryService.getProbationNameAddress(envelope);

        verify(requester).requestAsAdmin(captor.capture(), Mockito.eq(JsonObject.class));

        final Envelope<JsonObject> capturedEnvelope = captor.getValue();
        assertThat(JsonEnvelope.envelopeFrom(capturedEnvelope.metadata(), capturedEnvelope.payload()), is(jsonEnvelope(
                withMetadataEnvelopedFrom(envelope)
                        .withName("referencedata.query.organisation.byorgtype"),
                payloadIsJson(allOf(
                        withJsonPath("$.orgType", equalTo("NPS"))
                )))
        ));
    }

    @Test
    public void shouldGetAttendanceCenterNameAddress() {
        final JsonEnvelope envelope = buildDummyJsonRequestEnvelope();

        resultsQueryService.getAttendanceCenterNameAddress(envelope);

        verify(requester).requestAsAdmin(captor.capture(), Mockito.eq(JsonObject.class));

        final Envelope<JsonObject> capturedEnvelope = captor.getValue();
        assertThat(JsonEnvelope.envelopeFrom(capturedEnvelope.metadata(), capturedEnvelope.payload()), is(jsonEnvelope(
                withMetadataEnvelopedFrom(envelope)
                        .withName("referencedata.query.organisation.byorgtype"),
                payloadIsJson(allOf(
                        withJsonPath("$.orgType", equalTo("ATTC"))
                )))
        ));
    }

    @Test
    public void shouldGetCrackedIneffectiveVacatedTrialTypes() {

        final JsonEnvelope envelope = buildDummyJsonRequestEnvelope();

        resultsQueryService.getOtherFixedValues(envelope);

        verify(requester).requestAsAdmin(captor.capture(), Mockito.eq(JsonObject.class));
        assertThat(captor.getValue().metadata().name(), is("referencedata.query.cracked-ineffective-vacated-trial-types"));

    }
    private JsonEnvelope buildDummyJsonRequestEnvelope() {
        return buildDummyJsonRequestEnvelopeWithName("name");
    }

    private JsonEnvelope buildDummyJsonRequestEnvelopeWithName(final String name) {
        return envelopeFrom(metadataWithRandomUUID(name).withCausation(randomUUID())
                        .build(),
                createObjectBuilder()
                        .build());
    }
}
