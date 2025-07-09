package uk.gov.moj.cpp.hearing.event;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.test.FileUtil.getPayload;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingWithApplicationTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.createCourtApplicationCases;

import uk.gov.justice.core.courts.ApplicationStatus;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.CourtApplicationType;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.event.service.ProgressionService;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

@SuppressWarnings({"unchecked", "unused"})
public class InitiateHearingEventProcessorTest {
    private static final String APPEARANCE_TO_MAKE_STATUTORY_DECLARATION_CODE = "MC80527";
    private static final String APPEARANCE_TO_MAKE_STATUTORY_DECLARATION_SJP_CASE_CODE = "MC80528";
    private static final String APPLICATION_TO_REOPEN_CASE_CODE = "MC80524";
    private static final String APPEAL_AGAINST_CONVICTION_CODE = "MC80801";
    private static final String APPEAL_AGAINST_SENTENCE_CODE = "MC80803";
    private static final String APPEAL_AGAINST_CONVICTION_AND_SENTENCE_CODE = "MC80802";

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final List<String> APPLICATION_TYPE_LIST = Arrays.asList(APPEARANCE_TO_MAKE_STATUTORY_DECLARATION_CODE, APPEARANCE_TO_MAKE_STATUTORY_DECLARATION_SJP_CASE_CODE,
            APPLICATION_TO_REOPEN_CASE_CODE, APPEAL_AGAINST_CONVICTION_CODE, APPEAL_AGAINST_SENTENCE_CODE, APPEAL_AGAINST_CONVICTION_AND_SENTENCE_CODE);

    @Spy
    private final Enveloper enveloper = createEnveloper();
    @Captor
    protected ArgumentCaptor<JsonEnvelope> jsonEnvelopeArgumentCaptor;
    @Spy
    private StringToJsonObjectConverter stringToJsonObjectConverter = new StringToJsonObjectConverter();
    @Spy
    private JsonObjectToObjectConverter jsonObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();
    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter = new JsonObjectConvertersFactory().objectToJsonObjectConverter();
    @InjectMocks
    private InitiateHearingEventProcessor initiateHearingEventProcessor;
    @Mock
    private Sender sender;
    @Mock
    private HearingRepository hearingRepository;
    @Mock
    private Requester InitiateHearingEventProcessorTestrequester;
    @Mock
    private JsonEnvelope responseEnvelope;
    @Captor
    private ArgumentCaptor<Envelope<JsonObject>> envelopeArgumentCaptor;
    @Mock
    private ProgressionService progressionService;


    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    public static Stream<Arguments> applicationTypes() {
        return Stream.of(
                Arguments.of(APPEARANCE_TO_MAKE_STATUTORY_DECLARATION_CODE, "STAT_DEC"),
                Arguments.of(APPEARANCE_TO_MAKE_STATUTORY_DECLARATION_SJP_CASE_CODE, "STAT_DEC"),
                Arguments.of(APPLICATION_TO_REOPEN_CASE_CODE, "REOPEN"),
                Arguments.of(APPEAL_AGAINST_CONVICTION_CODE, "APPEAL"),
                Arguments.of(APPEAL_AGAINST_SENTENCE_CODE, "APPEAL"),
                Arguments.of(APPEAL_AGAINST_CONVICTION_AND_SENTENCE_CODE, "APPEAL")
        );
    }

    @Test
    public void publishHearingInitiatedEvent() {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        publishHearingInitiatedEvent(initiateHearingCommand, false);
    }

    @Test
    public void publishHearingInitiatedEventNoCases() {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        initiateHearingCommand.getHearing().setProsecutionCases(null);
        publishHearingInitiatedEvent(initiateHearingCommand, false);
        verify(sender, times(1)).send(jsonEnvelopeArgumentCaptor.capture());

        final Metadata metadata = jsonEnvelopeArgumentCaptor.getValue().metadata();
        assertThat(metadata.name(), is("public.hearing.initiated"));

        final JsonObject jsonObject = jsonEnvelopeArgumentCaptor.getValue().asJsonObject();

        final JsonArray applicationDetailsList = jsonObject.getJsonArray("applicationDetails");
        final JsonObject applicationDetails = (JsonObject) applicationDetailsList.get(0);
        final JsonObject subject = applicationDetails.getJsonObject("subject");

        assertThat(jsonObject.getString("hearingId"), notNullValue());
        assertThat(jsonObject.getJsonArray("cases").size(), is(0));
        assertThat(jsonObject.getString("hearingDateTime"), notNullValue());
        assertThat(jsonObject.getJsonArray("caseDetails").size(), is(0));
        assertThat(jsonObject.getString("jurisdictionType"), is("CROWN"));
        assertThat(subject.getString("defendantFirstName"), is("Lauren"));
        assertThat(subject.getString("defendantLastName"), is("Michelle"));
    }

    @ParameterizedTest
    @MethodSource("applicationTypes")
    public void shouldRaiseEventForEmailWhenApplicationTypeMatches(final String applicationTypeId, final String applicationType) {
        final UUID masterDefendantId = randomUUID();
        final UUID applicationId = randomUUID();
        when(progressionService.getApplicationDetails(any(JsonEnvelope.class), eq(applicationId)))
                .thenReturn(Optional.of(createObjectBuilder().add("courtApplication",
                        createObjectBuilder().add("id", applicationId.toString())
                                .add("applicant", createObjectBuilder().add("id", randomUUID().toString()))
                                .add("applicationStatus", "UN_ALLOCATED")
                                .build()).build()));


        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingWithApplicationTemplate(singletonList(CourtApplication.courtApplication()
                .withType(CourtApplicationType.courtApplicationType()
                        .withId(randomUUID())
                        .withCode(applicationTypeId)
                        .build())
                .withCourtApplicationCases(createCourtApplicationCases())
                .withId(applicationId)
                .withSubject(CourtApplicationParty.courtApplicationParty()
                        .withMasterDefendant(MasterDefendant.masterDefendant()
                                .withMasterDefendantId(masterDefendantId)
                                .withPersonDefendant(PersonDefendant.personDefendant()
                                        .withPersonDetails(Person.person()
                                                .withFirstName("John")
                                                .withLastName("Doe")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build()));

        publishHearingInitiatedEvent(initiateHearingCommand, false);
        final Envelope<JsonObject> event = this.envelopeArgumentCaptor.getAllValues().get(4);

        final JsonEnvelope allValues = envelopeFrom(event.metadata(), event.payload());
        assertThat(allValues,
                jsonEnvelope(
                        metadata().withName("public.hearing.nces-email-notification-for-application"),
                        payloadIsJson(allOf(
                                withJsonPath("$.applicationType", is(applicationType)),
                                withJsonPath("$.masterDefendantId", is(masterDefendantId.toString())),
                                withJsonPath("$.listingDate", is(dateTimeFormatter.format(initiateHearingCommand.getHearing().getHearingDays().get(0).getSittingDay()))),
                                withJsonPath("$.caseUrns[0]", is("caseURN1")),
                                withJsonPath("$.caseUrns[1]", is("caseURN2")),
                                withJsonPath("$.hearingCourtCentreName", is(notNullValue()))
                        ))));

    }

    @ParameterizedTest
    @MethodSource("applicationTypes")
    public void shouldNotRaiseEventForEmailWhenApplicationTypeMatchesAndFinalised(final String applicationTypeId, final String applicationType) {
        final UUID masterDefendantId = randomUUID();
        final UUID applicationId = UUID.fromString("020074c0-21cf-4339-ab11-0604e7d527e4");
        final Hearing hearing1 = new Hearing();

        when(progressionService.getApplicationDetails(any(JsonEnvelope.class), eq(applicationId)))
                .thenReturn(Optional.of(createObjectBuilder().add("courtApplication",
                        createObjectBuilder().add("id", applicationId.toString())
                                .add("applicant", createObjectBuilder().add("id", randomUUID().toString()))
                                .add("applicationStatus", "FINALISED")
                                .build()).build()));

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingWithApplicationTemplate(singletonList(CourtApplication.courtApplication()
                .withType(CourtApplicationType.courtApplicationType()
                        .withId(randomUUID())
                        .withCode(applicationTypeId).build())
                .withId(applicationId)
                .withApplicationStatus(ApplicationStatus.UN_ALLOCATED)
                .withCourtApplicationCases(createCourtApplicationCases())
                .withSubject(CourtApplicationParty.courtApplicationParty()
                        .withMasterDefendant(MasterDefendant.masterDefendant()
                                .withMasterDefendantId(masterDefendantId)
                                .withPersonDefendant(PersonDefendant.personDefendant()
                                        .withPersonDetails(Person.person()
                                                .withFirstName("John")
                                                .withLastName("Doe")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build()));

        publishHearingInitiatedEvent(initiateHearingCommand, true);
    }

    @ParameterizedTest
    @MethodSource("applicationTypes")
    public void shouldNotRaiseEventForEmailWhenApplicationTypeMatchesAndListed(final String applicationTypeId, final String applicationType) {
        final UUID masterDefendantId = randomUUID();
        final UUID applicationId = UUID.fromString("020074c0-21cf-4339-ab11-0604e7d527e4");
        final Hearing hearing1 = new Hearing();

        when(progressionService.getApplicationDetails(any(JsonEnvelope.class), eq(applicationId)))
                .thenReturn(Optional.of(createObjectBuilder().add("courtApplication",
                        createObjectBuilder().add("id", applicationId.toString())
                                .add("applicant", createObjectBuilder().add("id", randomUUID().toString()))
                                .add("applicationStatus", "LISTED")
                                .add("judicialResults", createArrayBuilder().add(createObjectBuilder().add(
                                        "nextHearing", createObjectBuilder().build())
                                        .add("resultDefinitionGroup", "Next Hearing")))
                                .build()).build()));

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingWithApplicationTemplate(singletonList(CourtApplication.courtApplication()
                .withType(CourtApplicationType.courtApplicationType()
                        .withId(randomUUID())
                        .withCode(applicationTypeId).build())
                .withId(applicationId)
                .withApplicationStatus(ApplicationStatus.UN_ALLOCATED)
                .withCourtApplicationCases(createCourtApplicationCases())
                .withSubject(CourtApplicationParty.courtApplicationParty()
                        .withMasterDefendant(MasterDefendant.masterDefendant()
                                .withMasterDefendantId(masterDefendantId)
                                .withPersonDefendant(PersonDefendant.personDefendant()
                                        .withPersonDetails(Person.person()
                                                .withFirstName("John")
                                                .withLastName("Doe")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build()));

        publishHearingInitiatedEvent(initiateHearingCommand, true);
    }

    @ParameterizedTest
    @MethodSource("applicationTypes")
    public void shouldRaiseEventForEmailWhenApplicationIsListed(final String applicationTypeId, final String applicationType) { //srivani
        final UUID masterDefendantId = randomUUID();
        final UUID applicationId = UUID.fromString("020074c0-21cf-4339-ab11-0604e7d527e4");
        final Hearing hearing1 = new Hearing();

        final JsonObject courtApplications = new StringToJsonObjectConverter().convert(getPayload("./progression.get-application-details.json"));
        when(progressionService.getApplicationDetails(any(JsonEnvelope.class), eq(applicationId)))
                .thenReturn(Optional.of(courtApplications));

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingWithApplicationTemplate(singletonList(CourtApplication.courtApplication()
                .withType(CourtApplicationType.courtApplicationType()
                        .withCode(applicationTypeId).build())
                .withId(applicationId)
                .withApplicationStatus(ApplicationStatus.UN_ALLOCATED)
                .withCourtApplicationCases(createCourtApplicationCases())
                .withSubject(CourtApplicationParty.courtApplicationParty()
                        .withMasterDefendant(MasterDefendant.masterDefendant()
                                .withMasterDefendantId(masterDefendantId)
                                .withPersonDefendant(PersonDefendant.personDefendant()
                                        .withPersonDetails(Person.person()
                                                .withFirstName("John")
                                                .withLastName("Doe")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build()));

        publishHearingInitiatedEvent(initiateHearingCommand, false);
    }


    @ParameterizedTest
    @MethodSource("applicationTypes")
    public void shouldRaiseEventForEmailWhenApplicationTypeMatchesAndListedWithoutNextHearing(final String applicationTypeId, final String applicationType) {
        final UUID masterDefendantId = randomUUID();
        final UUID applicationId = UUID.fromString("020074c0-21cf-4339-ab11-0604e7d527e4");
        final Hearing hearing1 = new Hearing();

        when(progressionService.getApplicationDetails(any(JsonEnvelope.class), eq(applicationId)))
                .thenReturn(Optional.of(createObjectBuilder().add("courtApplication",
                        createObjectBuilder().add("id", applicationId.toString())
                                .add("applicant", createObjectBuilder().add("id", randomUUID().toString()))
                                .add("applicationStatus", "LISTED")
                                .add("judicialResults", createArrayBuilder().add(createObjectBuilder().add(
                                        "sample", createObjectBuilder().build())))
                                .build()).build()));

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingWithApplicationTemplate(singletonList(CourtApplication.courtApplication()
                .withType(CourtApplicationType.courtApplicationType()
                        .withCode(applicationTypeId).build())
                .withId(applicationId)
                .withApplicationStatus(ApplicationStatus.UN_ALLOCATED)
                .withCourtApplicationCases(createCourtApplicationCases())
                .withSubject(CourtApplicationParty.courtApplicationParty()
                        .withMasterDefendant(MasterDefendant.masterDefendant()
                                .withMasterDefendantId(masterDefendantId)
                                .withPersonDefendant(PersonDefendant.personDefendant()
                                        .withPersonDetails(Person.person()
                                                .withFirstName("John")
                                                .withLastName("Doe")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build()));

        publishHearingInitiatedEvent(initiateHearingCommand, false);
    }


    @ParameterizedTest
    @MethodSource("applicationTypes")
    public void shouldRaiseEventForEmailWhenApplicationTypeMatchesAndListedWithoutNEXH(final String applicationTypeId, final String applicationType) {
        final UUID masterDefendantId = randomUUID();
        final UUID applicationId = UUID.fromString("020074c0-21cf-4339-ab11-0604e7d527e4");
        final Hearing hearing1 = new Hearing();

        when(progressionService.getApplicationDetails(any(JsonEnvelope.class), eq(applicationId)))
                .thenReturn(Optional.of(createObjectBuilder().add("courtApplication",
                        createObjectBuilder().add("id", applicationId.toString())
                                .add("applicant", createObjectBuilder().add("id", randomUUID().toString()))
                                .add("applicationStatus", "LISTED")
                                .build()).build()));

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingWithApplicationTemplate(singletonList(CourtApplication.courtApplication()
                .withType(CourtApplicationType.courtApplicationType()
                        .withCode(applicationTypeId).build())
                .withId(applicationId)
                .withApplicationStatus(ApplicationStatus.UN_ALLOCATED)
                .withCourtApplicationCases(createCourtApplicationCases())
                .withSubject(CourtApplicationParty.courtApplicationParty()
                        .withMasterDefendant(MasterDefendant.masterDefendant()
                                .withMasterDefendantId(masterDefendantId)
                                .withPersonDefendant(PersonDefendant.personDefendant()
                                        .withPersonDetails(Person.person()
                                                .withFirstName("John")
                                                .withLastName("Doe")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build()));

        publishHearingInitiatedEvent(initiateHearingCommand, false);
    }


    @ParameterizedTest
    @MethodSource("applicationTypes")
    public void shouldRaiseEventForEmailWhenApplicationTypeMatch(final String applicationTypeId, final String applicationType) {
        final UUID masterDefendantId = randomUUID();
        final UUID applicationId = UUID.fromString("10b95782-48e4-48c7-b168-fcc00558a3b4");
        when(progressionService.getApplicationDetails(any(JsonEnvelope.class), eq(applicationId)))
                .thenReturn(Optional.of(createObjectBuilder().add("courtApplication",
                        createObjectBuilder().add("id", applicationId.toString())
                                .add("applicant", createObjectBuilder().add("id", randomUUID().toString()))
                                .add("applicationStatus", "UN_ALLOCATED")
                                .build()).build()));


        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingWithApplicationTemplate(singletonList(CourtApplication.courtApplication()
                .withType(CourtApplicationType.courtApplicationType()
                        .withCode(applicationTypeId).build())
                .withId(applicationId)
                .withApplicationStatus(ApplicationStatus.UN_ALLOCATED)
                .withCourtApplicationCases(createCourtApplicationCases())
                .withSubject(CourtApplicationParty.courtApplicationParty()
                        .withMasterDefendant(MasterDefendant.masterDefendant()
                                .withMasterDefendantId(masterDefendantId)
                                .withPersonDefendant(PersonDefendant.personDefendant()
                                        .withPersonDetails(Person.person()
                                                .withFirstName("John")
                                                .withLastName("Doe")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build()));

        publishHearingInitiatedEvent(initiateHearingCommand, false);
        final Envelope<JsonObject> event = this.envelopeArgumentCaptor.getAllValues().get(4);

        final JsonEnvelope allValues = envelopeFrom(event.metadata(), event.payload());
        assertThat(allValues,
                jsonEnvelope(
                        metadata().withName("public.hearing.nces-email-notification-for-application"),
                        payloadIsJson(allOf(
                                withJsonPath("$.applicationType", is(applicationType)),
                                withJsonPath("$.masterDefendantId", is(masterDefendantId.toString())),
                                withJsonPath("$.listingDate", is(dateTimeFormatter.format(initiateHearingCommand.getHearing().getHearingDays().get(0).getSittingDay()))),
                                withJsonPath("$.caseUrns[0]", is("caseURN1")),
                                withJsonPath("$.caseUrns[1]", is("caseURN2")),
                                withJsonPath("$.hearingCourtCentreName", is(notNullValue())),
                                withJsonPath("$.caseOffenceIdList.size()", is(2))
                        ))));

    }

    @Test
    public void shouldRaiseEventForEmailWithCourtOrderOffencesWhenHearingInitiatedForAnApplication() {
        final UUID masterDefendantId = randomUUID();
        final UUID applicationId = UUID.fromString("10b95782-48e4-48c7-b168-fcc00558a3b4");

        when(progressionService.getApplicationDetails(any(JsonEnvelope.class), eq(applicationId)))
                .thenReturn(Optional.of(createObjectBuilder().add("courtApplication",
                        createObjectBuilder().add("id", applicationId.toString())
                                .add("applicant", createObjectBuilder().add("id", randomUUID().toString()))
                                .add("applicationStatus", "UN_ALLOCATED")
                                .build()).build()));


        final List<CourtApplicationCase> courtApplicationCases = createCourtApplicationCases();
        final UUID offenceId1 = courtApplicationCases.get(0).getOffences().get(0).getId();
        final UUID offenceId2 = courtApplicationCases.get(1).getOffences().get(0).getId();
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingWithApplicationTemplate(singletonList(CourtApplication.courtApplication()
                .withType(CourtApplicationType.courtApplicationType()
                        .withCode(APPEARANCE_TO_MAKE_STATUTORY_DECLARATION_CODE).build())
                .withId(applicationId)
                .withApplicationStatus(ApplicationStatus.UN_ALLOCATED)
                .withCourtApplicationCases(courtApplicationCases)
                .withSubject(CourtApplicationParty.courtApplicationParty()
                        .withMasterDefendant(MasterDefendant.masterDefendant()
                                .withMasterDefendantId(masterDefendantId)
                                .withPersonDefendant(PersonDefendant.personDefendant()
                                        .withPersonDetails(Person.person()
                                                .withFirstName("John")
                                                .withLastName("Doe")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build()));

        publishHearingInitiatedEvent(initiateHearingCommand, false);
        final Envelope<JsonObject> event = this.envelopeArgumentCaptor.getAllValues().get(4);

        final JsonEnvelope allValues = envelopeFrom(event.metadata(), event.payload());
        assertThat(allValues,
                jsonEnvelope(
                        metadata().withName("public.hearing.nces-email-notification-for-application"),
                        payloadIsJson(allOf(
                                withJsonPath("$.applicationType", is("STAT_DEC")),
                                withJsonPath("$.masterDefendantId", is(masterDefendantId.toString())),
                                withJsonPath("$.listingDate", is(dateTimeFormatter.format(initiateHearingCommand.getHearing().getHearingDays().get(0).getSittingDay()))),
                                withJsonPath("$.caseUrns[0]", is("caseURN1")),
                                withJsonPath("$.caseUrns[1]", is("caseURN2")),
                                withJsonPath("$.hearingCourtCentreName", is(notNullValue())),
                                withJsonPath("$.caseOffenceIdList[0]", is(offenceId1.toString())),
                                withJsonPath("$.caseOffenceIdList[1]", is(offenceId2.toString()))
                        ))));

    }

    private void publishHearingInitiatedEvent(final InitiateHearingCommand initiateHearingCommand, final Boolean isFinaliseOrListedApplication) {

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.initiated"),
                objectToJsonObjectConverter.convert(initiateHearingCommand));

        int expectedInvocations = 0;
        this.initiateHearingEventProcessor.hearingInitiated(event);

        int caseCount = initiateHearingCommand.getHearing().getProsecutionCases() == null ? 0 : initiateHearingCommand.getHearing().getProsecutionCases().size();
        if(isFinaliseOrListedApplication) {
            expectedInvocations =  (3 * caseCount);
        } else {
             expectedInvocations = 1 + (3 * caseCount);
        }
        if (initiateHearingCommand.getHearing().getCourtApplications() != null && APPLICATION_TYPE_LIST.contains(initiateHearingCommand.getHearing().getCourtApplications().get(0).getType().getCode())) {
            expectedInvocations++;
        }


        verify(this.sender, times(expectedInvocations)).send(this.envelopeArgumentCaptor.capture());

        final List<Envelope<JsonObject>> envelopes = this.envelopeArgumentCaptor.getAllValues();

        final List<UUID> prosecutionCaseIds = new ArrayList<>();
        final List<UUID> defendantIds = new ArrayList<>();
        final List<UUID> offenceIds = new ArrayList<>();

        if (caseCount > 0) {
            initiateHearingCommand.getHearing().getProsecutionCases().forEach(prosecutionCase -> {
                prosecutionCaseIds.add(prosecutionCase.getId());
                prosecutionCase.getDefendants().forEach(defendant -> {
                    defendantIds.add(defendant.getId());
                    defendant.getOffences().forEach(offence -> offenceIds.add(offence.getId()));
                });
            });
        }

        if (caseCount > 0) {
            assertThat(
                    envelopeFrom(envelopes.get(0).metadata(), objectToJsonObjectConverter.convert(envelopes.get(0).payload())), is(jsonEnvelope(
                            metadata().withName("hearing.command.register-hearing-against-defendant"),
                            payloadIsJson(allOf(
                                    withJsonPath("$.defendantId", is(defendantIds.get(0).toString())),
                                    withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString())))))
                    )
            );

            assertThat(
                    envelopeFrom(envelopes.get(1).metadata(), objectToJsonObjectConverter.convert(envelopes.get(1).payload())), jsonEnvelope(
                            metadata().withName("hearing.command.register-hearing-against-offence"),
                            payloadIsJson(allOf(
                                    withJsonPath("$.offenceId", is(offenceIds.get(0).toString())),
                                    withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString()))))));


            assertThat(
                    envelopeFrom(envelopes.get(2).metadata(), objectToJsonObjectConverter.convert(envelopes.get(2).payload())), jsonEnvelope(
                            metadata().withName("hearing.command.register-hearing-against-case"),
                            payloadIsJson(allOf(
                                    withJsonPath("$.caseId", is(prosecutionCaseIds.get(0).toString())),
                                    withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString())))))
                            .thatMatchesSchema()
            );
        }

        assertThat(
                envelopeFrom(envelopes.get(caseCount * 3).metadata(), envelopes.get(caseCount * 3).payload()), jsonEnvelope(
                        metadata().withName("public.hearing.initiated"),
                        payloadIsJson(withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString()))))
                        .thatMatchesSchema()
        );
    }
}