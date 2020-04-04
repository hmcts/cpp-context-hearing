package uk.gov.moj.cpp.hearing.xhibit.xmlgenerator;

import static java.math.BigInteger.valueOf;
import static java.time.ZonedDateTime.parse;
import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.XmlProducerType.PUBLIC_DISPLAY;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CaseDetail.caseDetail;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Cases.cases;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Court.court;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtRoom.courtRoom;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtSite.courtSite;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus.currentCourtStatus;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Defendant.defendant;
import static uk.gov.moj.cpp.hearing.xhibit.XmlTestUtils.assertXmlEquals;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingEvent;
import uk.gov.justice.core.courts.JudicialRoleType;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.JudicialRoleTypeEnum;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Currentstatus;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Event;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.ObjectFactory;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CaseDetail;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;
import uk.gov.moj.cpp.hearing.test.FileUtil;
import uk.gov.moj.cpp.hearing.test.HearingFactory;
import uk.gov.moj.cpp.hearing.xhibit.CourtCentreGeneratorParameters;
import uk.gov.moj.cpp.hearing.xhibit.XhibitReferenceDataService;
import uk.gov.moj.cpp.hearing.xhibit.XmlUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import javax.json.JsonObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PublicDisplayCourtCentreXmlGeneratorTest {

    private static final String PUBLIC_PAGE_FILE_PATH = "xhibit/expectedPublicPage.xml";
    private static final String PUBLIC_PAGE_FOR_SUMMER_TIME_FILE_PATH = "xhibit/expectedPublicPageForSummerTime.xml";
    private static final String PUBLIC_PAGE_FOR_ACTIVE_CASES_FILE_PATH = "xhibit/expectedPublicPageForActiveCases.xml";
    private static final String PUBLIC_PAGE_FOR_STANDALONE_APPLICATION_FILE_PATH = "xhibit/expectedPublicPageForStandaloneApplication.xml";
    private static final String PUBLIC_PAGE_WITHOUT_JUDGE_NAME_FILE_PATH = "xhibit/expectedPublicPageWithoutJudgeName.xml";
    private static final String MAGISTRATE_JUDICIAL_ROLE_TYPE = "Magistrate";

    @Spy
    private XmlUtils xmlUtils;

    @Mock
    private PublicDisplayEventGenerator publicDisplayEventGenerator;

    @Mock
    private JsonEnvelope context;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Mock
    private Requester requester;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private Enveloper enveloper;

    @Mock
    private XhibitReferenceDataService xhibitReferenceDataService;

    @InjectMocks
    private PublicDisplayCourtCentreXmlGenerator publicDisplayCourtCentreXmlGenerator;

    @Before
    public void setUp() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldCreatePublicDisplayCourtCentreXml() throws IOException {
        final ZonedDateTime lastUpdatedTime = parse("2019-12-05T13:50:00Z");

        final HearingEvent hearingEvent = HearingEvent.hearingEvent()
                .withId(UUID.randomUUID())
                .withHearingId(UUID.randomUUID())
                .withEventTime(lastUpdatedTime)
                .build();
        final Currentstatus currentstatus = getCurrentStatus();

        final Optional<CurrentCourtStatus> currentCourtStatus = of(getCurrentCourtStatus(hearingEvent));

        final JsonEnvelope jsonEnvelopeMock = mock(JsonEnvelope.class);
        final JsonEnvelope hearingEnvelope = getHearingEnvelope();
        when(enveloper.withMetadataFrom(any(JsonEnvelope.class), anyString()).apply(any(JsonObject.class))).thenReturn(jsonEnvelopeMock);
        when(requester.requestAsAdmin(jsonEnvelopeMock)).thenReturn(hearingEnvelope);

        final JsonObject judiciary = FileUtil.givenPayload("/data/referencedata.query.judiciaries.json");
        when(xhibitReferenceDataService.getJudiciary(any(), any())).thenReturn(judiciary);

        when(publicDisplayEventGenerator.generate(currentCourtStatus.get().getCourt().getCourtSites().get(0).getCourtRooms().get(0).getCases().getCasesDetails().get(0))).thenReturn(currentstatus);

        final CourtCentreGeneratorParameters courtCentreGeneratorParameters = new CourtCentreGeneratorParameters(PUBLIC_DISPLAY, currentCourtStatus, lastUpdatedTime, context);

        final String generatedPublicPageXml = publicDisplayCourtCentreXmlGenerator.generateXml(courtCentreGeneratorParameters);

        assertXmlEquals(generatedPublicPageXml, PUBLIC_PAGE_FILE_PATH);
    }

    @Test
    public void shouldCreatePublicDisplayCourtCentreXmlWithSummerTime() throws IOException {
        final ZonedDateTime lastUpdatedTime = parse("2020-03-30T13:50:00Z");

        final HearingEvent hearingEvent = HearingEvent.hearingEvent()
                .withId(UUID.randomUUID())
                .withHearingId(UUID.randomUUID())
                .withEventTime(lastUpdatedTime)
                .build();
        final Currentstatus currentstatus = getCurrentStatus();

        final Optional<CurrentCourtStatus> currentCourtStatus = of(getCurrentCourtStatusWithSummerTime(hearingEvent));

        final JsonEnvelope jsonEnvelopeMock = mock(JsonEnvelope.class);
        final JsonEnvelope hearingEnvelope = getHearingEnvelope();
        when(enveloper.withMetadataFrom(any(JsonEnvelope.class), anyString()).apply(any(JsonObject.class))).thenReturn(jsonEnvelopeMock);
        when(requester.requestAsAdmin(jsonEnvelopeMock)).thenReturn(hearingEnvelope);

        final JsonObject judiciary = FileUtil.givenPayload("/data/referencedata.query.judiciaries.json");
        when(xhibitReferenceDataService.getJudiciary(any(), any())).thenReturn(judiciary);

        when(publicDisplayEventGenerator.generate(currentCourtStatus.get().getCourt().getCourtSites().get(0).getCourtRooms().get(0).getCases().getCasesDetails().get(0))).thenReturn(currentstatus);

        final CourtCentreGeneratorParameters courtCentreGeneratorParameters = new CourtCentreGeneratorParameters(PUBLIC_DISPLAY, currentCourtStatus, lastUpdatedTime, context);

        final String generatedPublicPageXml = publicDisplayCourtCentreXmlGenerator.generateXml(courtCentreGeneratorParameters);

        assertXmlEquals(generatedPublicPageXml, PUBLIC_PAGE_FOR_SUMMER_TIME_FILE_PATH);
    }

    @Test
    public void shouldCreatePublicDisplayCourtCentreXmlJustWithMagistrate() throws IOException {
        final ZonedDateTime lastUpdatedTime = parse("2019-12-05T13:50:00Z");

        final HearingEvent hearingEvent = HearingEvent.hearingEvent()
                .withId(UUID.randomUUID())
                .withHearingId(UUID.randomUUID())
                .withEventTime(lastUpdatedTime)
                .build();
        final Currentstatus currentstatus = getCurrentStatus();

        final Optional<CurrentCourtStatus> currentCourtStatus = of(getCurrentCourtStatus(hearingEvent));

        final JsonEnvelope jsonEnvelopeMock = mock(JsonEnvelope.class);
        final JsonEnvelope hearingEnvelope = getHearingEnvelopeJustWithMagistrate();
        when(enveloper.withMetadataFrom(any(JsonEnvelope.class), anyString()).apply(any(JsonObject.class))).thenReturn(jsonEnvelopeMock);
        when(requester.requestAsAdmin(jsonEnvelopeMock)).thenReturn(hearingEnvelope);

        final JsonObject judiciary = FileUtil.givenPayload("/data/referencedata.query.judiciaries.json");
        when(xhibitReferenceDataService.getJudiciary(any(), any())).thenReturn(judiciary);

        when(publicDisplayEventGenerator.generate(currentCourtStatus.get().getCourt().getCourtSites().get(0).getCourtRooms().get(0).getCases().getCasesDetails().get(0))).thenReturn(currentstatus);

        final CourtCentreGeneratorParameters courtCentreGeneratorParameters = new CourtCentreGeneratorParameters(PUBLIC_DISPLAY, currentCourtStatus, lastUpdatedTime, context);

        final String generatedPublicPageXml = publicDisplayCourtCentreXmlGenerator.generateXml(courtCentreGeneratorParameters);

        assertXmlEquals(generatedPublicPageXml, PUBLIC_PAGE_WITHOUT_JUDGE_NAME_FILE_PATH);
    }

    @Test
    public void shouldCreatePublicDisplayCourtCentreXmlWithActiveCaseFlagSet() throws IOException {
        final ZonedDateTime lastUpdatedTime = parse("2019-12-05T13:50:00Z");

        final HearingEvent hearingEvent = HearingEvent.hearingEvent()
                .withId(UUID.randomUUID())
                .withHearingId(UUID.randomUUID())
                .withEventTime(lastUpdatedTime)
                .build();
        final Currentstatus currentstatus = getCurrentStatus();

        final Optional<CurrentCourtStatus> currentCourtStatus = of(getCurrentCourtStatusWithMultipleCases(hearingEvent));

        final JsonEnvelope jsonEnvelopeMock = mock(JsonEnvelope.class);
        final JsonEnvelope hearingEnvelope = getHearingEnvelope();
        when(enveloper.withMetadataFrom(any(JsonEnvelope.class), anyString()).apply(any(JsonObject.class))).thenReturn(jsonEnvelopeMock);
        when(requester.requestAsAdmin(jsonEnvelopeMock)).thenReturn(hearingEnvelope);

        final JsonObject judiciary = FileUtil.givenPayload("/data/referencedata.query.judiciaries.json");
        when(xhibitReferenceDataService.getJudiciary(any(), any())).thenReturn(judiciary);

        when(publicDisplayEventGenerator.generate(currentCourtStatus.get().getCourt().getCourtSites().get(0).getCourtRooms().get(0).getCases().getCasesDetails().get(0))).thenReturn(currentstatus);

        final CourtCentreGeneratorParameters courtCentreGeneratorParameters = new CourtCentreGeneratorParameters(PUBLIC_DISPLAY, currentCourtStatus, lastUpdatedTime, context);

        final String generatedPublicPageXml = publicDisplayCourtCentreXmlGenerator.generateXml(courtCentreGeneratorParameters);

        assertXmlEquals(generatedPublicPageXml, PUBLIC_PAGE_FOR_ACTIVE_CASES_FILE_PATH);
    }

    @Test
    public void shouldCreatePublicDisplayCourtCentreXmlWithStandaloneApplication() throws IOException {
        final ZonedDateTime lastUpdatedTime = parse("2019-12-05T13:50:00Z");
        final Currentstatus currentstatus = getCurrentStatus();

        final HearingEvent hearingEvent = HearingEvent.hearingEvent()
                .withId(UUID.randomUUID())
                .withHearingId(UUID.randomUUID())
                .withEventTime(lastUpdatedTime)
                .build();

        final Optional<CurrentCourtStatus> currentCourtStatus = of(getCurrentCourtStatusForStandaloneApplication(hearingEvent));

        final JsonEnvelope jsonEnvelopeMock = mock(JsonEnvelope.class);
        final JsonEnvelope hearingEnvelope = getHearingEnvelope();
        when(enveloper.withMetadataFrom(any(JsonEnvelope.class), anyString()).apply(any(JsonObject.class))).thenReturn(jsonEnvelopeMock);
        when(requester.requestAsAdmin(jsonEnvelopeMock)).thenReturn(hearingEnvelope);

        final JsonObject judiciary = FileUtil.givenPayload("/data/referencedata.query.judiciaries.json");
        when(xhibitReferenceDataService.getJudiciary(any(), any())).thenReturn(judiciary);

        when(publicDisplayEventGenerator.generate(currentCourtStatus.get().getCourt().getCourtSites().get(0).getCourtRooms().get(0).getCases().getCasesDetails().get(0))).thenReturn(currentstatus);

        final CourtCentreGeneratorParameters courtCentreGeneratorParameters = new CourtCentreGeneratorParameters(PUBLIC_DISPLAY, currentCourtStatus, lastUpdatedTime, context);

        final String generatedPublicPageXml = publicDisplayCourtCentreXmlGenerator.generateXml(courtCentreGeneratorParameters);

        assertXmlEquals(generatedPublicPageXml, PUBLIC_PAGE_FOR_STANDALONE_APPLICATION_FILE_PATH);
    }

    private Currentstatus getCurrentStatus() {
        final ObjectFactory objectFactory = new ObjectFactory();
        final Currentstatus currentstatus = objectFactory.createCurrentstatus();
        final Event event = objectFactory.createEvent();

        event.setTime("12:30");
        event.setDate("12/07/19");
        event.setFreeText(EMPTY);
        event.setType("102000");

        currentstatus.setEvent(event);
        return currentstatus;
    }

    private CurrentCourtStatus getCurrentCourtStatusForStandaloneApplication(final HearingEvent hearingEvent) {
        return currentCourtStatus()
                .withCourt(court()
                        .withCourtName("Court 1")
                        .withCourtSites(asList(courtSite()
                                .withCourtSiteName("testCourtSiteName")
                                .withId(UUID.randomUUID())
                                .withCourtRooms(asList(courtRoom()
                                        .withCourtRoomName("courtRoomName")
                                        .withHearingEvent(hearingEvent)
                                        .withCases(cases()
                                                .withCasesDetails(asList(caseDetail()
                                                        .withHearingprogress(BigInteger.ZERO)
                                                        .withCaseType("CROWN")
                                                        .withActivecase(valueOf(0))
                                                        .withCppUrn("FPHNY0K3Q9")
                                                        .withHearingType("Application")
                                                        .withHearingEvent(hearingEvent)
                                                        .withDefendants(asList(defendant().withFirstName("Neil").withLastName("Chukshi").build()))
                                                        .withJudgeName(EMPTY)
                                                        .withNotBeforeTime("2020-04-04T23:09Z")
                                                        .build()))
                                                .build())
                                        .build()))
                                .build()))
                        .build()).build();
    }

    private CurrentCourtStatus getCurrentCourtStatus(final HearingEvent hearingEvent) {
        return currentCourtStatus()
                .withCourt(court()
                        .withCourtName("testCourtName")
                        .withCourtSites(asList(courtSite()
                                .withCourtSiteName("testCourtSiteName")
                                .withCourtRooms(asList(courtRoom()
                                        .withCourtRoomName("courtRoomName")
                                        .withHearingEvent(hearingEvent)
                                        .withCases(cases()
                                                .withCasesDetails(asList(caseDetail()
                                                        .withActivecase(valueOf(0))
                                                        .withCaseNumber("123")
                                                        .withCaseType("caseType")
                                                        .withCppUrn("234")
                                                        .withHearingType("hearingType")
                                                        .withHearingEvent(hearingEvent)
                                                        .withDefendants(asList(defendant().withFirstName("Alexander").withMiddleName("de").withLastName("Jong").build()))
                                                        .withJudgeName("Mr Lampard")
                                                        .withNotBeforeTime("2020-02-09T15:00Z")
                                                        .build()))
                                                .build())
                                        .build()))
                                .build()))
                        .build()).build();
    }

    private CurrentCourtStatus getCurrentCourtStatusWithSummerTime(final HearingEvent hearingEvent) {
        return currentCourtStatus()
                .withCourt(court()
                        .withCourtName("testCourtName")
                        .withCourtSites(asList(courtSite()
                                .withCourtSiteName("testCourtSiteName")
                                .withCourtRooms(asList(courtRoom()
                                        .withCourtRoomName("courtRoomName")
                                        .withHearingEvent(hearingEvent)
                                        .withCases(cases()
                                                .withCasesDetails(asList(caseDetail()
                                                        .withActivecase(valueOf(0))
                                                        .withCaseNumber("123")
                                                        .withCaseType("caseType")
                                                        .withCppUrn("234")
                                                        .withHearingType("hearingType")
                                                        .withHearingEvent(hearingEvent)
                                                        .withDefendants(asList(defendant().withFirstName("Alexander").withMiddleName("de").withLastName("Jong").build()))
                                                        .withJudgeName("Mr Lampard")
                                                        .withNotBeforeTime("2020-03-30T15:00Z")
                                                        .build()))
                                                .build())
                                        .build()))
                                .build()))
                        .build()).build();
    }

    private CurrentCourtStatus getCurrentCourtStatusWithMultipleCases(final HearingEvent hearingEvent) {
        return currentCourtStatus()
                .withCourt(court()
                        .withCourtName("testCourtName")
                        .withCourtSites(asList(courtSite()
                                .withCourtSiteName("testCourtSiteName")
                                .withCourtRooms(asList(courtRoom()
                                        .withCourtRoomName("courtRoomName")
                                        .withHearingEvent(hearingEvent)
                                        .withCases(cases().withCasesDetails(asList(caseDetail2(), caseDetail3())).build())
                                        .build()))
                                .build()))
                        .build()).build();
    }

    private CaseDetail caseDetail2() {
        return caseDetail()
                .withActivecase(valueOf(0))
                .withCaseNumber("1")
                .withHearingprogress(valueOf(5))
                .withCaseType("caseType")
                .withCppUrn("234")
                .withHearingType("hearingType")
                .withDefendants(asList(defendant().withFirstName("Alexander").withMiddleName("de").withLastName("Jong").build()))
                .withJudgeName("Mr Lampard")
                .withNotBeforeTime("2020-02-09T15:00Z")
                .build();
    }

    private CaseDetail caseDetail3() {
        return caseDetail()
                .withActivecase(valueOf(1))
                .withHearingprogress(valueOf(5))
                .withCaseNumber("1")
                .withCaseType("caseType")
                .withCppUrn("235")
                .withHearingType("hearingType")
                .withDefendants(asList(defendant().withFirstName("Alexander").withMiddleName("de").withLastName("Jong").build()))
                .withJudgeName("Mr Lampard")
                .withNotBeforeTime("2020-02-09T15:00Z")
                .build();
    }

    private JsonEnvelope getHearingEnvelope() {
        Hearing hearing = new HearingFactory().createStandaloneApplicationHearing()
                .withJudiciary(Arrays.asList(judicialRoleWithRequestedJudicialRole(JudicialRoleType.judicialRoleType().withJudiciaryType(JudicialRoleTypeEnum.CIRCUIT_JUDGE.toString()).build()),
                                             judicialRoleWithRequestedJudicialRole(JudicialRoleType.judicialRoleType().withJudiciaryType(JudicialRoleTypeEnum.DISTRICT_JUDGE.toString()).build())))
                .build();

        HearingDetailsResponse hearingDetailsResponse = new HearingDetailsResponse();
        hearingDetailsResponse.setHearing(hearing);

        final JsonObject jsonObject = objectToJsonObjectConverter.convert(hearingDetailsResponse);

        return JsonEnvelope.envelopeFrom(
                metadataBuilder().
                        withName("hearing.get.hearing").
                        withId(randomUUID()),
                jsonObject
        );
    }

    private JsonEnvelope getHearingEnvelopeJustWithMagistrate() {
        Hearing hearing = new HearingFactory().createStandaloneApplicationHearing()
                .withJudiciary(Arrays.asList(judicialRoleWithRequestedJudicialRole(JudicialRoleType.judicialRoleType().withJudiciaryType(MAGISTRATE_JUDICIAL_ROLE_TYPE).build()),
                                             judicialRoleWithRequestedJudicialRole(JudicialRoleType.judicialRoleType().withJudiciaryType(MAGISTRATE_JUDICIAL_ROLE_TYPE).build())))
                .build();

        HearingDetailsResponse hearingDetailsResponse = new HearingDetailsResponse();
        hearingDetailsResponse.setHearing(hearing);

        final JsonObject jsonObject = objectToJsonObjectConverter.convert(hearingDetailsResponse);

        return JsonEnvelope.envelopeFrom(
                metadataBuilder().
                        withName("hearing.get.hearing").
                        withId(randomUUID()),
                jsonObject
        );
    }

    public uk.gov.justice.core.courts.JudicialRole judicialRoleWithRequestedJudicialRole(JudicialRoleType judicialRoleType) {
        return uk.gov.justice.core.courts.JudicialRole.judicialRole()
                .withFirstName("Bob")
                .withLastName("Roberts")
                .withJudicialRoleType(judicialRoleType).build();
    }
}