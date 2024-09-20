package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.lang.System.lineSeparator;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static uk.gov.justice.hearing.courts.referencedata.CourtCentreOrganisationUnit.courtCentreOrganisationUnit;
import static uk.gov.justice.hearing.courts.referencedata.Courtrooms.courtrooms;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.RESULT_DEFINITIONS_JSON;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.NextHearing;
import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.justice.hearing.courts.referencedata.CourtCentreOrganisationUnit;
import uk.gov.justice.hearing.courts.referencedata.Courtrooms;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV3;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.service.CourtHouseReverseLookup;
import uk.gov.moj.cpp.hearing.event.service.CourtRoomOuCodeReverseLookup;
import uk.gov.moj.cpp.hearing.event.service.HearingTypeReverseLookup;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;
import uk.gov.moj.cpp.hearing.test.FileResourceObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NextHearingHelperV3Test  {

    private static final String DEFAULT_VALUE = "DefaultValue";

    @Spy
    private final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();

    @Mock
    private HearingTypeReverseLookup hearingTypeReverseLookup;

    @Mock
    private CourtHouseReverseLookup courtHouseReverseLookup;

    @Mock
    private CourtRoomOuCodeReverseLookup courtRoomOuCodeReverseLookup;

    @Mock
    private ReferenceDataService referenceDataService;

    @InjectMocks
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @InjectMocks
    private NextHearingHelper nextHearingHelper;

    @InjectMocks
    private NextHearingHelperV3 nextHearingHelperV3;

    private static final int COURT_ROOM_ID = 54321;
    private static final int PSA_CODE = 3255;
    private static final String COURT_ROOM_NAME = "Courtroom 01";
    private static final String COURT_NAME = "Southwark Crown Court";
    private static final String HEARING_TYPE_DESCRIPTION = "Plea & Trial Preparation";
    private static final String FIRST_HEARING_TYPE_DESCRIPTION = "First hearing";

    private static final String EXPECTED_ADJOURNMENT_REASON = "Adjournment reason: At request of the prosecution" + lineSeparator() +
            "Additional information Adjournment reason prompt 1" + lineSeparator() +
            "Adjournment reason: At request of the prosecution" + lineSeparator() +
            "Additional information Second Reason prompt 1";

    private final Courtrooms expectedCourtRoomResult = courtrooms()
            .withCourtroomId(COURT_ROOM_ID)
            .withCourtroomName(COURT_ROOM_NAME)
            .withId(randomUUID())
            .build();

    private final HearingType hearingType = new HearingType(HEARING_TYPE_DESCRIPTION, randomUUID(), HEARING_TYPE_DESCRIPTION);
    private final HearingType firstHearingType = new HearingType(FIRST_HEARING_TYPE_DESCRIPTION, randomUUID(), FIRST_HEARING_TYPE_DESCRIPTION);

    private final ResultDefinition adjournmentReasonsResultDefinition = ResultDefinition.resultDefinition().setResultDefinitionGroup("Adjournment Reasons");
    protected List<ResultDefinition> resultDefinitions;
    protected static final FileResourceObjectMapper fileResourceObjectMapper = new FileResourceObjectMapper();

    @Before
    public void setUp()  throws IOException {
        initMocks(this);
        resultDefinitions = fileResourceObjectMapper.convertFromFile(RESULT_DEFINITIONS_JSON, AllResultDefinitions.class).getResultDefinitions();
    }
    @Test
    public void shouldPopulateNextHearingForCrownCourtHearing() {
        final JsonEnvelope event = getJsonEnvelop("/data/hearing.results-shared-with-nexthearingv3-crowncourt.json");

        setupMocks(event, null);

        final ResultDefinition resultDefinition = jsonObjectToObjectConverter
                .convert(givenPayload("/data/resultdefinition-fbed768b-ee95-4434-87c8-e81cbc8d24c8.json"), ResultDefinition.class);

        final ResultsSharedV3 resultsSharedV3 = jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), ResultsSharedV3.class);

        final List<ResultLine2> resultLine2s = getResultLines(resultsSharedV3);

        final ResultLine2 resultLine = resultsSharedV3.getTargets().get(0).getResultLines().stream().filter(rl3 -> rl3.getResultLabel().equalsIgnoreCase("Next hearing in Crown Court")).findFirst().get();

        final Optional<NextHearing> nextHearing = nextHearingHelperV3.getNextHearing(event, resultDefinition, resultLine2s, resultLine, getPrompts(resultsSharedV3, resultDefinition), resultsSharedV3, resultDefinitions);

        assertEquals("B47GL", nextHearing.get().getCourtCentre().getCode());

        assertValid(nextHearing, JurisdictionType.CROWN, ZonedDateTimes.fromString("2024-04-29T09:00Z"), "1 HOURS", hearingType, true);
}

    @Test
    public void shouldPopulateNextHearingForApplicationReviewHearing(){
        final JsonEnvelope event = getJsonEnvelop("/data/hearing.results-shared-with-nexthearingv3-susps-drnrr.json");

        setupMocks(event, null);

        final ResultDefinition resultDefinition = jsonObjectToObjectConverter
                .convert(givenPayload("/data/result-definition-b3ed14c1-d921-459c-90fd-400a5d8d0076.json"), ResultDefinition.class);

        final ResultsSharedV3 resultsSharedV3 = jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), ResultsSharedV3.class);

        final List<ResultLine2> resultLine2s = getResultLines(resultsSharedV3);

        final ResultLine2 resultLine = resultsSharedV3.getTargets().get(0).getResultLines().stream().filter(rl3 -> rl3.getResultLabel().equalsIgnoreCase("First Review Hearing – Drug Rehab")).findFirst().get();

        final Optional<NextHearing> nextHearing = nextHearingHelperV3.getNextHearing(event, resultDefinition, resultLine2s, resultLine, getPrompts(resultsSharedV3, resultDefinition), resultsSharedV3, resultDefinitions);

        assertValid(nextHearing, ZonedDateTimes.fromString("2024-05-18T09:30Z"), true);

        final ResultLine2 resultLineForCOEW = resultsSharedV3.getTargets().get(2).getResultLines().stream().filter(rl3 -> rl3.getResultLabel().equalsIgnoreCase("Drug rehabilitation residential")).findFirst().get();

        final Optional<NextHearing> nextHearingForCOEW = nextHearingHelperV3.getNextHearing(event, resultDefinition, resultLine2s, resultLineForCOEW, getPrompts(resultsSharedV3, resultDefinition), resultsSharedV3, resultDefinitions);
        assertThat(nextHearingForCOEW.get().getProbationTeamName(), is("Wales Division NPS"));


    }

    private List<ResultLine2> getResultLines(final ResultsSharedV3 resultsSharedV3) {
        return resultsSharedV3.getTargets().stream().flatMap(t->t.getResultLines().stream()).collect(toList());
    }

    @Test
    public void shouldPopulateNextHearingForCrownCourtHearingFixedDate() {
        final JsonEnvelope event = getJsonEnvelop("/data/hearing.results-shared-v3-with-nexthearing-crowncourt-fixed-date.json");

        setupMocks(event, null);

        final ResultDefinition resultDefinition = jsonObjectToObjectConverter
                .convert(givenPayload("/data/resultdefinition-fbed768b-ee95-4434-87c8-e81cbc8d24c8.json"), ResultDefinition.class);

        final ResultsSharedV3 resultsSharedV3 = jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), ResultsSharedV3.class);

        final List<ResultLine2> resultLine2s = getResultLines(resultsSharedV3);

        final ResultLine2 resultLine = resultsSharedV3.getTargets().get(0).getResultLines().stream().filter(rl3 -> rl3.getResultLabel().equalsIgnoreCase("Next hearing in Crown Court")).findFirst().get();

        final Optional<NextHearing> nextHearing = nextHearingHelperV3.getNextHearing(event, resultDefinition, resultLine2s, resultLine, getPrompts(resultsSharedV3, resultDefinition), resultsSharedV3, resultDefinitions);

        assertValid(nextHearing, JurisdictionType.CROWN, ZonedDateTimes.fromString("2024-04-30T09:00Z"), "1 HOURS", hearingType, true);
    }

    @Test
    public void shouldPopulateNextHearingForCrownCourtHearingFixedDateWithoutTime() {
        final JsonEnvelope event = getJsonEnvelop("/data/hearing.results-shared-v3-with-nexthearing-crowncourt-fixed-date_without_time.json");

        setupMocks(event, null);

        final ResultsSharedV3 resultsSharedV3 = jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), ResultsSharedV3.class);

        final List<ResultLine2> resultLine2s = getResultLines(resultsSharedV3);

        final ResultLine2 resultLine = resultsSharedV3.getTargets().get(0).getResultLines().stream().filter(rl3 -> rl3.getResultLabel().equalsIgnoreCase("Next hearing in Crown Court")).findFirst().get();

        final ResultDefinition resultDefinition = jsonObjectToObjectConverter
                .convert(givenPayload("/data/resultdefinition-fbed768b-ee95-4434-87c8-e81cbc8d24c8.json"), ResultDefinition.class);

        final Optional<NextHearing> nextHearing = nextHearingHelperV3.getNextHearing(event, resultDefinition, resultLine2s, resultLine, getPrompts(resultsSharedV3, resultDefinition), resultsSharedV3, resultDefinitions);

        assertValid(nextHearing, JurisdictionType.CROWN, ZonedDateTimes.fromString("2024-04-30T00:00Z"),"1 HOURS", hearingType, true);
    }

    @Test
    public void shouldPopulateNextHearingForMagistrateCourtHearing() {
        final JsonEnvelope event = getJsonEnvelop("/data/hearing.results-shared-v3-with-nexthearing-magistratescourt.json");
        setupMagsMocks(event, null);

        final ResultsSharedV3 resultsSharedV3 = jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), ResultsSharedV3.class);

        final List<ResultLine2> resultLine2s = getResultLines(resultsSharedV3);


        final ResultDefinition resultDefinition = jsonObjectToObjectConverter
                .convert(givenPayload("/data/resultdefinition-70c98fa6-804d-11e8-adc0-fa7ae01bbebc.json"), ResultDefinition.class);

        final ResultLine2 resultLine = resultsSharedV3.getTargets().get(0).getResultLines().stream().filter(rl3 -> rl3.getResultLabel().equalsIgnoreCase("Next hearing in magistrates' court")).findFirst().get();

        final Optional<NextHearing> nextHearing = nextHearingHelperV3.getNextHearing(event, resultDefinition, resultLine2s, resultLine, getPrompts(resultsSharedV3, resultDefinition), resultsSharedV3, resultDefinitions);

        assertValid(nextHearing, JurisdictionType.MAGISTRATES, ZonedDateTimes.fromString("2024-04-30T22:30Z"), "20 MINUTES", firstHearingType, true);
    }

    @Test
    public void shouldPopulateNextHearingForUnscheduled() {
        final JsonEnvelope event = getJsonEnvelop("/data/hearing.results-shared-v3-with-nexthearing-crowncourt-date-tobe-fixed.json");

        setupMocks(event, null);

        final ResultDefinition resultDefinition = jsonObjectToObjectConverter
                .convert(givenPayload("/data/resultdefinition-fbed768b-ee95-4434-87c8-e81cbc8d24c8.json"), ResultDefinition.class);

        final ResultsSharedV3 resultsSharedV3 = jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), ResultsSharedV3.class);

        final List<ResultLine2> resultLine2s = getResultLines(resultsSharedV3);

        final ResultLine2 resultLine = resultsSharedV3.getTargets().get(0).getResultLines().stream().filter(rl3 -> rl3.getResultLabel().equalsIgnoreCase("Next hearing in Crown Court")).findFirst().get();

        final Optional<NextHearing> nextHearing = nextHearingHelperV3.getNextHearing(event, resultDefinition, resultLine2s, resultLine, getPrompts(resultsSharedV3, resultDefinition), resultsSharedV3, resultDefinitions);

        assertValid(nextHearing, JurisdictionType.CROWN, null, "1 HOURS", hearingType, false);
        assertThat(nextHearing.get().getDateToBeFixed(), is(true));

    }
    @Test
    public void shouldPopulateNextHearingForCrownCourtHearingWithNoListedTime() {
        final JsonEnvelope event = getJsonEnvelop("/data/hearing.results-shared-with-nexthearing-v3-crowncourt-no-listing-time.json");

        setupMocks(event,  null);

        final ResultDefinition resultDefinition = jsonObjectToObjectConverter
                .convert(givenPayload("/data/resultdefinition-fbed768b-ee95-4434-87c8-e81cbc8d24c8.json"), ResultDefinition.class);

        final ResultsSharedV3 resultsSharedV3 = jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), ResultsSharedV3.class);

        final List<ResultLine2> resultLine2s = getResultLines(resultsSharedV3);

        final ResultLine2 resultLine = resultsSharedV3.getTargets().get(0).getResultLines().stream().filter(rl3 -> rl3.getResultLabel().equalsIgnoreCase("Next hearing in Crown Court")).findFirst().get();

        final Optional<NextHearing> nextHearing = nextHearingHelperV3.getNextHearing(event, resultDefinition, resultLine2s, resultLine, getPrompts(resultsSharedV3, resultDefinition), resultsSharedV3, resultDefinitions);

        assertValid(nextHearing, JurisdictionType.CROWN, null, "1 HOURS", hearingType, false);
        assertThat(nextHearing.get().getWeekCommencingDate(), is(LocalDate.of(2024, 5, 6)));
    }


    private void setupMocks(final JsonEnvelope event, final String defaultStartTimeForCourt) {
        final CourtCentreOrganisationUnit courtCentre = getCourtCentre(defaultStartTimeForCourt);
        when(courtHouseReverseLookup.getCourtCentreByName(event, COURT_NAME)).thenReturn(ofNullable(courtCentre));
        when(courtHouseReverseLookup.getCourtRoomByRoomName(courtCentre, COURT_ROOM_NAME)).thenReturn(ofNullable(expectedCourtRoomResult));
        when(courtRoomOuCodeReverseLookup.getcourtRoomOuCode(event, 291, "B47GL")).thenReturn("B47GL00");
        when(hearingTypeReverseLookup.getHearingTypeByName(event, HEARING_TYPE_DESCRIPTION)).thenReturn(hearingType);
        when(referenceDataService.getResultDefinitionById(any(), any(), eq(fromString("1d55fdeb-7dbc-46ec-b3ff-7b15fe08a476")))).thenReturn(adjournmentReasonsResultDefinition);
    }

    private void setupMagsMocks(final JsonEnvelope event, final String defaultStartTimeForCourt) {
        final CourtCentreOrganisationUnit courtCentre = getMagsCourtCentre(null);
        when(courtHouseReverseLookup.getCourtCentreByName(event, "Lavender Hill Magistrates' Court")).thenReturn(ofNullable(courtCentre));
        when(courtHouseReverseLookup.getCourtRoomByRoomName(courtCentre, "Courtroom 01")).thenReturn(ofNullable(expectedCourtRoomResult));
        when(courtRoomOuCodeReverseLookup.getcourtRoomOuCode(event, 295, "B01GL")).thenReturn("B01LY01");
        when(hearingTypeReverseLookup.getHearingTypeByName(event, "First hearing")).thenReturn(firstHearingType);
        when(referenceDataService.getResultDefinitionById(any(), any(), eq(fromString("1d55fdeb-7dbc-46ec-b3ff-7b15fe08a476")))).thenReturn(adjournmentReasonsResultDefinition);
    }

    private JsonEnvelope getJsonEnvelop(final String filePath) {
        return JsonEnvelope.envelopeFrom(
                Envelope.metadataBuilder().withId(randomUUID()).withName("hearing.results-shared-v3").build(),
                givenPayload(filePath));
    }

    private List<JudicialResultPrompt> getPrompts(final ResultsSharedV3 resultsSharedV3, final ResultDefinition resultDefinition) {

        final Optional<ResultLine2> resultLine = resultsSharedV3.getTargets().get(0)
                .getResultLines().stream()
                .filter(rl -> resultDefinition.getId().equals(rl.getResultDefinitionId()))
                .findFirst();

        return resultLine.map(line -> line.getPrompts().stream().map(prompt -> {
                    final Optional<String> referenceOptional = resultDefinition.getPrompts().stream().filter(p -> p.getId().equals(prompt.getId()) && (isNull(prompt.getPromptRef()) || prompt.getPromptRef().equals(p.getReference()))).findFirst().map(
                            Prompt::getReference);
                    return JudicialResultPrompt.judicialResultPrompt()
                            .withLabel(prompt.getLabel())
                            .withValue(prompt.getValue())
                            .withPromptReference(referenceOptional.orElse(DEFAULT_VALUE)
                            )
                            .build();
                }
        ).collect(toList())).orElse(emptyList());

    }

    private void assertValid(final Optional<NextHearing> nextHearingResult, final JurisdictionType jurisdictionType, final ZonedDateTime expectedListedStartDateTime, final String estimatedDuration, final HearingType hearingType, final Boolean roomFlag) {

        assertTrue(nextHearingResult.isPresent());

        final NextHearing nextHearing = nextHearingResult.get();
        assertThat(nextHearing.getListedStartDateTime(), is(expectedListedStartDateTime));
        assertThat(nextHearing.getEstimatedDuration(), is(estimatedDuration));
        assertCourtCentre(nextHearing.getCourtCentre(), roomFlag);
        assertThat(nextHearing.getType(), is(hearingType));
        assertThat(nextHearing.getJurisdictionType(), is(jurisdictionType));
    }

    private void assertValid(final Optional<NextHearing> nextHearingResult, final ZonedDateTime expectedListedStartDateTime, final Boolean roomFlag) {

        assertTrue(nextHearingResult.isPresent());
        final NextHearing nextHearing = nextHearingResult.get();
        assertThat(nextHearing.getListedStartDateTime(), is(expectedListedStartDateTime));
        assertCourtCentre(nextHearing.getCourtCentre(), roomFlag);
        assertThat(nextHearing.getApplicationTypeCode(), is("SE20510"));
        assertThat(nextHearing.getIsFirstReviewHearing(), is(true));
        assertThat(nextHearing.getProbationTeamName(), is("South East and Eastern Division NPS"));
        assertThat(nextHearing.getSuspendedPeriod(), is("12 Months"));
        assertThat(nextHearing.getTotalCustodialPeriod(), is("15 Months"));
        assertThat(nextHearing.getOrderName(), is("Suspended sentence order - imprisonment"));
    }

    private void assertCourtCentre(final CourtCentre courtCentre, final Boolean roomFlag) {
        final Address address = courtCentre.getAddress();
        assertThat(courtCentre.getName(), is(COURT_NAME));
        assertThat(courtCentre.getPsaCode(), is(PSA_CODE));
        if(roomFlag) {
            assertThat(courtCentre.getRoomName(), is(COURT_ROOM_NAME));
        }
        assertThat(address.getAddress1(), is("Address1"));
        assertThat(address.getAddress2(), is("Address2"));
        assertThat(address.getAddress3(), is("Address3"));
        assertThat(address.getAddress4(), is("Address4"));
        assertThat(address.getAddress5(), is("Address5"));
        assertThat(address.getPostcode(), is("UB10 0HB"));
    }

    private static JsonObject givenPayload(final String filePath) {
        try (InputStream inputStream = NextHearingHelperV3Test.class.getResourceAsStream(filePath)) {
            final JsonReader jsonReader = Json.createReader(inputStream);
            return jsonReader.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CourtCentreOrganisationUnit getCourtCentre(final String defaultStartTime) {
        return courtCentreOrganisationUnit()
                .withId(randomUUID().toString())
                .withLja("3255")
                .withOucodeL3Name(COURT_NAME)
                .withOucodeL1Code("C")
                .withOucode("B47GL")
                .withCourtrooms(asList(expectedCourtRoomResult))
                .withAddress1("Address1")
                .withAddress2("Address2")
                .withAddress3("Address3")
                .withAddress4("Address4")
                .withAddress5("Address5")
                .withPostcode("UB10 0HB")
                .withDefaultStartTime(defaultStartTime)
                .build();
    }

    private CourtCentreOrganisationUnit getMagsCourtCentre(final String defaultStartTime) {
        return courtCentreOrganisationUnit()
                .withId(randomUUID().toString())
                .withLja("3255")
                .withOucodeL3Name(COURT_NAME)
                .withOucodeL1Code("B")
                .withOucode("B01GL")
                .withCourtrooms(asList(expectedCourtRoomResult))
                .withAddress1("Address1")
                .withAddress2("Address2")
                .withAddress3("Address3")
                .withAddress4("Address4")
                .withAddress5("Address5")
                .withPostcode("UB10 0HB")
                .withDefaultStartTime(defaultStartTime)
                .build();
    }
}
