package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.lang.Integer.valueOf;
import static java.lang.System.lineSeparator;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.NextHearing;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.hearing.courts.referencedata.CourtCentreOrganisationUnit;
import uk.gov.justice.hearing.courts.referencedata.Courtrooms;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.service.CourtHouseReverseLookup;
import uk.gov.moj.cpp.hearing.event.service.CourtRoomOuCodeReverseLookup;
import uk.gov.moj.cpp.hearing.event.service.HearingTypeReverseLookup;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataClientTestBase;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NextHearingHelperTest extends ReferenceDataClientTestBase {

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

    private final int COURT_ROOM_ID = 54321;
    private final int PSA_CODE = 3255;
    private final String courtRoomName = "ROOM A";
    private final String courtName = "Wimbledon Magistrates Court";
    private final String hearingTypeDescription = "Plea & Trial Preparation";
    private final String expectedAdjournmentReason = "Adjournment reason: At request of the prosecution" + lineSeparator() +
            "Additional information Adjournment reason prompt 1" + lineSeparator() +
            "Additional information Adjournment reason prompt 2" + lineSeparator() +
            lineSeparator() +
            "Adjournment reason: At request of the prosecution" + lineSeparator() +
            "Additional information Second Reason prompt 1";

    private final Courtrooms expectedCourtRoomResult = Courtrooms.courtrooms()
            .withCourtroomId(COURT_ROOM_ID)
            .withCourtroomName(courtRoomName)
            .withId(randomUUID())
            .build();
    private final CourtCentreOrganisationUnit expectedCourtHouseByNameResult = CourtCentreOrganisationUnit.courtCentreOrganisationUnit()
            .withId(randomUUID().toString())
            .withLja("3255")
            .withOucodeL3Name(courtName)
            .withOucode("B47GL")
            .withCourtrooms(asList(expectedCourtRoomResult))
            .withAddress1("Address1")
            .withAddress2("Address2")
            .withAddress3("Address3")
            .withAddress4("Address4")
            .withAddress5("Address5")
            .withPostcode("UB10 0HB")
            .build();

    private final HearingType hearingType = new HearingType(hearingTypeDescription, randomUUID(), hearingTypeDescription);

    private final ResultDefinition adjournmentReasonsResultDefinition = ResultDefinition.resultDefinition().setResultDefinitionGroup("Adjournment Reasons");

    @Test
    public void test_Populate_NextHearing_For_CrownCourt_Hearing() {
        final JsonEnvelope event = getJsonEnvelop("fbed768b-ee95-4434-87c8-e81cbc8d24c8");

        setupMocks(event);

        final ResultDefinition resultDefinition = jsonObjectToObjectConverter
                .convert(givenPayload("/data/result-definition-fbed768b-ee95-4434-87c8-e81cbc8d24c8.json"), ResultDefinition.class);

        final Optional<NextHearing> nextHearing = nextHearingHelper.getNextHearing(event, resultDefinition, getResultLines(event), getPrompts(event, resultDefinition));

        assertValid(nextHearing, JurisdictionType.CROWN);
    }

    @Test
    public void test_Populate_NextHearing_For_MagistrateCourt_Hearing() {
        final JsonEnvelope event = getJsonEnvelop("70c98fa6-804d-11e8-adc0-fa7ae01bbebc");
        setupMocks(event);

        final ResultDefinition resultDefinition = jsonObjectToObjectConverter
                .convert(givenPayload("/data/result-definition-70c98fa6-804d-11e8-adc0-fa7ae01bbebc.json"), ResultDefinition.class);

        final Optional<NextHearing> nextHearing = nextHearingHelper.getNextHearing(event, resultDefinition, getResultLines(event), getPrompts(event, resultDefinition));

        assertValid(nextHearing, JurisdictionType.MAGISTRATES);
    }

    private void setupMocks(final JsonEnvelope event) {
        when(courtHouseReverseLookup.getCourtCentreByName(event, courtName)).thenReturn(ofNullable(expectedCourtHouseByNameResult));
        when(courtHouseReverseLookup.getCourtRoomByRoomName(expectedCourtHouseByNameResult, courtRoomName)).thenReturn(ofNullable(expectedCourtRoomResult));
        when(courtRoomOuCodeReverseLookup.getcourtRoomOuCode(event, 291, "B47GL")).thenReturn("B47GL00");
        when(hearingTypeReverseLookup.getHearingTypeByName(event, hearingTypeDescription)).thenReturn(hearingType);
        when(referenceDataService.getResultDefinitionById(any(), any(), eq(fromString("1d55fdeb-7dbc-46ec-b3ff-7b15fe08a476")))).thenReturn(adjournmentReasonsResultDefinition);
    }

    private JsonEnvelope getJsonEnvelop(final String resultDefinitionId) {
        String filePath = "/data/hearing.results-shared-with-nexthearing-magistratescourt.json";

        if (resultDefinitionId.equals("fbed768b-ee95-4434-87c8-e81cbc8d24c8")) {
            filePath = "/data/hearing.results-shared-with-nexthearing-crowncourt.json";
        }

        return JsonEnvelope.envelopeFrom(
                Envelope.metadataBuilder().withId(randomUUID()).withName("hearing.results-shared").build(),
                givenPayload(filePath));
    }

    private List<ResultLine> getResultLines(final JsonEnvelope event) {
        final ResultsShared resultsShared = jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), ResultsShared.class);

        return resultsShared.getTargets().get(0).getResultLines();
    }

    private List<JudicialResultPrompt> getPrompts(final JsonEnvelope event, final ResultDefinition resultDefinition) {
        final ResultsShared resultsShared = jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), ResultsShared.class);

        final Optional<ResultLine> resultLine = resultsShared.getTargets().get(0)
                .getResultLines().stream()
                .filter(rl -> resultDefinition.getId().equals(rl.getResultDefinitionId()))
                .findFirst();

        if (resultLine.isPresent()) {
            final List<JudicialResultPrompt> judicialResultPrompts = resultLine.get().getPrompts().stream().map(prompt -> {
                        final Optional<String> referenceOptional = resultDefinition.getPrompts().stream().filter(p -> p.getId().equals(prompt.getId())).findFirst().map(
                                Prompt::getReference);
                        return JudicialResultPrompt.judicialResultPrompt()
                                .withLabel(prompt.getLabel())
                                .withValue(prompt.getValue())
                                .withPromptReference(referenceOptional.isPresent() ? referenceOptional.get() : DEFAULT_VALUE
                                )
                                .build();
                    }
            ).collect(toList());
            return judicialResultPrompts;
        }

        return emptyList();
    }

    private void assertValid(Optional<NextHearing> nextHearingResult, final JurisdictionType jurisdictionType) {

        assertTrue(nextHearingResult.isPresent());

        final NextHearing nextHearing = nextHearingResult.get();

        assertThat(nextHearing.getEstimatedMinutes(), is(valueOf(8793)));
        assertThat(nextHearing.getListedStartDateTime(), is(ZonedDateTimes.fromString("2019-02-02T22:22Z")));
        assertCourtCentre(nextHearing.getCourtCentre());

        assertThat(nextHearing.getAdjournmentReason(), is(expectedAdjournmentReason));
        assertThat(nextHearing.getType(), is(hearingType));
        assertThat(nextHearing.getJurisdictionType(), is(jurisdictionType));
    }

    private void assertCourtCentre(final CourtCentre courtCentre) {
        final Address address = courtCentre.getAddress();
        assertThat(courtCentre.getName(), is(courtName));
        assertThat(courtCentre.getPsaCode(), is(PSA_CODE));
        assertThat(courtCentre.getRoomName(), is(courtRoomName));
        assertThat(address.getAddress1(), is("Address1"));
        assertThat(address.getAddress2(), is("Address2"));
        assertThat(address.getAddress3(), is("Address3"));
        assertThat(address.getAddress4(), is("Address4"));
        assertThat(address.getAddress5(), is("Address5"));
        assertThat(address.getPostcode(), is("UB10 0HB"));
    }

    private static JsonObject givenPayload(final String filePath) {
        try (InputStream inputStream = NextHearingHelperTest.class.getResourceAsStream(filePath)) {
            final JsonReader jsonReader = Json.createReader(inputStream);
            return jsonReader.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
