package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.Arrays.asList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.query.view.service.CaseStatusCode.ACTIVE;
import static uk.gov.moj.cpp.hearing.query.view.service.CaseStatusCode.INACTIVE;
import static uk.gov.moj.cpp.hearing.query.view.service.ProgessStatusCode.ADJOURNED;
import static uk.gov.moj.cpp.hearing.query.view.service.ProgessStatusCode.FINISHED;
import static uk.gov.moj.cpp.hearing.query.view.service.ProgessStatusCode.INPROGRESS;
import static uk.gov.moj.cpp.hearing.query.view.service.ProgessStatusCode.STARTED;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.HearingEvent;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.JudicialRole;
import uk.gov.justice.core.courts.JudicialRoleType;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.mapping.CourtApplicationsSerializer;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CaseDetail;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtRoom;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;
import uk.gov.moj.cpp.hearing.test.FileUtil;
import uk.gov.moj.cpp.listing.common.xhibit.CommonXhibitReferenceDataService;
import uk.gov.moj.cpp.listing.domain.referencedata.CourtRoomMapping;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingListXhibitResponseTransformerTest {
    private static final String COURT_NAME = "Court 1";

    private UUID hearingTypeId;

    @Mock
    private HearingDay hearingDay;

    @Mock
    private Person person;

    @Mock
    private PersonDefendant personDefendant;

    @Mock
    private Defendant defendant;

    @Mock
    private ProsecutionCase prosecutionCase;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private CommonXhibitReferenceDataService commonXhibitReferenceDataService;

    @Mock
    private HearingEventsToHearingMapper hearingEventsToHearingMapper;

    @Mock
    private Hearing hearing;

    @Mock
    private HearingType hearingType;

    @Mock
    private CourtRoomMapping courtRoomMapping;

    @Mock
    private HearingEvent hearingEvent;

    @InjectMocks
    private HearingListXhibitResponseTransformer hearingListXhibitResponseTransformer;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @InjectMocks
    private CourtApplicationsSerializer courtApplicationsSerializer;


    @Before
    public void setUp() {
        setField(this.courtApplicationsSerializer, "jsonObjectToObjectConverter", jsonObjectToObjectConverter);
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldTransformFrom() {
        final UUID courtCentreId = randomUUID();
        final UUID courtRoomId = randomUUID();
        final UUID hearingId = randomUUID();
        final HearingEvent hearingEvent = HearingEvent.hearingEvent().build();
        final List<Hearing> hearingList = asList(hearing);
        final List<ProsecutionCase> prosecutionCases = asList(prosecutionCase);
        final List<Defendant> defendantList = asList(defendant);
        final List<HearingDay> hearingDays = asList(hearingDay);
        final ProsecutionCaseIdentifier prosecutionCaseIdentifier = ProsecutionCaseIdentifier
                .prosecutionCaseIdentifier().withCaseURN("caseURN").build();

        final Set<UUID> activeHearingIds = new HashSet<>();

        final Map<UUID, UUID> eventDefinitionsIds = new HashMap<>();
        final UUID eventDefinitionsId = randomUUID();
        eventDefinitionsIds.putIfAbsent(hearingId, eventDefinitionsId);
        when(hearingEventsToHearingMapper.getActiveHearingIds()).thenReturn(activeHearingIds);
        when(hearingEventsToHearingMapper.getHearingIdAndEventDefinitionIds()).thenReturn(eventDefinitionsIds);

        when(prosecutionCase.getProsecutionCaseIdentifier()).thenReturn(prosecutionCaseIdentifier);
        when(prosecutionCase.getDefendants()).thenReturn(defendantList);
        when(defendant.getPersonDefendant()).thenReturn(personDefendant);
        when(personDefendant.getPersonDetails()).thenReturn(person);
        when(person.getFirstName()).thenReturn("firstName");
        when(person.getMiddleName()).thenReturn("middleName");
        when(person.getLastName()).thenReturn("lastName");
        when(hearing.getHearingDays()).thenReturn(hearingDays);
        when(hearingDay.getSittingDay()).thenReturn(ZonedDateTime.now());
        when(hearing.getId()).thenReturn(hearingId);
        when(hearing.getType()).thenReturn(HearingType.hearingType().withDescription("hearingTypeDescription").build());
        when(hearing.getProsecutionCases()).thenReturn(prosecutionCases);
        when(hearing.getCourtCentre()).thenReturn(CourtCentre.courtCentre().withName(COURT_NAME).withRoomId(courtRoomId).withId(courtCentreId).build());
        when(hearingEventsToHearingMapper.getHearingList()).thenReturn(hearingList);
        when(hearingEventsToHearingMapper.getAllHearingEventBy(hearingId)).thenReturn(Optional.of(hearingEvent));
        when(commonXhibitReferenceDataService.getCourtRoomMappingBy(any(), any())).thenReturn(courtRoomMapping);
        when(courtRoomMapping.getCrestCourtRoomName()).thenReturn("x");
        when(courtRoomMapping.getCrestCourtSiteUUID()).thenReturn(randomUUID());

        mockHearingTypeId();

        when(commonXhibitReferenceDataService.getXhibitHearingType(hearingTypeId).getExhibitHearingDescription()).thenReturn("Plea");

        final CurrentCourtStatus currentCourtStatus = hearingListXhibitResponseTransformer.transformFrom(hearingEventsToHearingMapper);
        final CourtRoom courtRoom = currentCourtStatus.getCourt().getCourtSites().get(0).getCourtRooms().get(0);
        final CaseDetail caseDetail = courtRoom.getCases().getCasesDetails().get(0);
        final String courtRoomName = courtRoom.getCourtRoomName();

        assertThat(currentCourtStatus.getCourt().getCourtName(), is(COURT_NAME));
        assertThat(caseDetail.getActivecase(), is(INACTIVE.getStatusCode()));
        assertThat(caseDetail.getHearingprogress(), is(STARTED.getProgressCode()));
        assertThat(courtRoomName, is("x"));
        assertThat(currentCourtStatus.getCourt().getCourtSites().size(), is(1));
    }

    @Test
    public void shouldTransformFromWithInProgressEventAndActiveCase() {
        final UUID courtCentreId = randomUUID();
        final UUID courtRoomId = randomUUID();
        final UUID hearingId = randomUUID();
        final List<Hearing> hearingList = asList(hearing);
        final List<ProsecutionCase> prosecutionCases = asList(prosecutionCase);
        final List<Defendant> defendantList = asList(defendant);
        final List<HearingDay> hearingDays = asList(hearingDay);
        final ProsecutionCaseIdentifier prosecutionCaseIdentifier = ProsecutionCaseIdentifier
                .prosecutionCaseIdentifier().withCaseURN("caseURN").build();

        when(prosecutionCase.getProsecutionCaseIdentifier()).thenReturn(prosecutionCaseIdentifier);
        when(prosecutionCase.getDefendants()).thenReturn(defendantList);
        when(defendant.getPersonDefendant()).thenReturn(personDefendant);
        when(personDefendant.getPersonDetails()).thenReturn(person);
        when(person.getFirstName()).thenReturn("firstName");
        when(person.getMiddleName()).thenReturn("middleName");
        when(person.getLastName()).thenReturn("lastName");
        when(hearing.getHearingDays()).thenReturn(hearingDays);
        when(hearingDay.getSittingDay()).thenReturn(ZonedDateTime.now());
        when(hearing.getId()).thenReturn(hearingId);
        when(hearing.getType()).thenReturn(HearingType.hearingType().withDescription("hearingTypeDescription").build());
        when(hearing.getProsecutionCases()).thenReturn(prosecutionCases);
        when(hearing.getCourtCentre()).thenReturn(CourtCentre.courtCentre().withName(COURT_NAME).withRoomId(courtRoomId).withId(courtCentreId).build());
        when(hearingEventsToHearingMapper.getHearingList()).thenReturn(hearingList);
        when(hearingEventsToHearingMapper.getAllHearingEventBy(hearingId)).thenReturn(Optional.of(hearingEvent));
        final Set<UUID> activeHearingIds = new HashSet<>();
        activeHearingIds.add(hearingId);

        final Map<UUID, UUID> eventDefinitionsIds = new HashMap<>();
        final UUID eventDefinitionsId = randomUUID();
        eventDefinitionsIds.putIfAbsent(hearingId, eventDefinitionsId);
        when(hearingEventsToHearingMapper.getHearingIdAndEventDefinitionIds()).thenReturn(eventDefinitionsIds);
        when(hearingEventsToHearingMapper.getActiveHearingIds()).thenReturn(activeHearingIds);
        when(commonXhibitReferenceDataService.getCourtRoomMappingBy(any(), any())).thenReturn(courtRoomMapping);
        when(courtRoomMapping.getCrestCourtRoomName()).thenReturn("x");
        when(courtRoomMapping.getCrestCourtSiteUUID()).thenReturn(randomUUID());

        mockHearingTypeId();

        when(commonXhibitReferenceDataService.getXhibitHearingType(hearingTypeId).getExhibitHearingDescription()).thenReturn("Plea");

        final CurrentCourtStatus currentCourtStatus = hearingListXhibitResponseTransformer.transformFrom(hearingEventsToHearingMapper);
        final CourtRoom courtRoom = currentCourtStatus.getCourt().getCourtSites().get(0).getCourtRooms().get(0);
        final CaseDetail caseDetail = courtRoom.getCases().getCasesDetails().get(0);
        final String courtRoomName = courtRoom.getCourtRoomName();

        assertThat(currentCourtStatus.getCourt().getCourtName(), is(COURT_NAME));
        assertThat(caseDetail.getActivecase(), is(ACTIVE.getStatusCode()));
        assertThat(caseDetail.getHearingprogress(), is(INPROGRESS.getProgressCode()));
        assertThat(courtRoomName, is("x"));
        assertThat(currentCourtStatus.getCourt().getCourtSites().size(), is(1));
    }

    @Test
    public void shouldTransformFromWithStartedEventAndInActiveCase() {
        final UUID eventDefinitionId = fromString("7df93f18-0a21-40f5-9fb3-da4749cd70ff");//could be any random uuid
        final UUID courtCentreId = randomUUID();
        final UUID courtRoomId = randomUUID();
        final UUID hearingId = randomUUID();
        final HearingEvent hearingEvent = HearingEvent.hearingEvent().withHearingId(hearingId).withHearingEventDefinitionId(eventDefinitionId).build();
        final List<Hearing> hearingList = asList(hearing);
        final List<ProsecutionCase> prosecutionCases = asList(prosecutionCase);
        final List<Defendant> defendantList = asList(defendant);
        final List<HearingDay> hearingDays = asList(hearingDay);
        final ProsecutionCaseIdentifier prosecutionCaseIdentifier = ProsecutionCaseIdentifier
                .prosecutionCaseIdentifier().withProsecutionAuthorityReference("ProsecutionAuthorityReference").build();
        final Set<UUID> activeHearingIds = new HashSet<>();

        final Map<UUID, UUID> eventDefinitionsIds = new HashMap<>();
        final UUID eventDefinitionsId = randomUUID();
        eventDefinitionsIds.putIfAbsent(hearingId, eventDefinitionsId);

        when(prosecutionCase.getProsecutionCaseIdentifier()).thenReturn(prosecutionCaseIdentifier);
        when(prosecutionCase.getDefendants()).thenReturn(defendantList);
        when(defendant.getPersonDefendant()).thenReturn(personDefendant);
        when(personDefendant.getPersonDetails()).thenReturn(person);
        when(person.getFirstName()).thenReturn("firstName");
        when(person.getMiddleName()).thenReturn("middleName");
        when(person.getLastName()).thenReturn("lastName");
        when(hearing.getHearingDays()).thenReturn(hearingDays);
        when(hearingDay.getSittingDay()).thenReturn(ZonedDateTime.now());
        when(hearing.getId()).thenReturn(hearingId);
        when(hearing.getType()).thenReturn(HearingType.hearingType().withDescription("hearingTypeDescription").build());
        when(hearing.getProsecutionCases()).thenReturn(prosecutionCases);
        when(hearing.getCourtCentre()).thenReturn(CourtCentre.courtCentre().withName(COURT_NAME).withRoomId(courtRoomId).withId(courtCentreId).build());
        when(hearingEventsToHearingMapper.getHearingList()).thenReturn(hearingList);
        when(hearingEventsToHearingMapper.getActiveHearingIds()).thenReturn(activeHearingIds);
        when(hearingEventsToHearingMapper.getHearingIdAndEventDefinitionIds()).thenReturn(eventDefinitionsIds);

        when(hearingEventsToHearingMapper.getAllHearingEventBy(hearingId)).thenReturn(Optional.of(hearingEvent));
        when(commonXhibitReferenceDataService.getCourtRoomMappingBy(any(), any())).thenReturn(courtRoomMapping);
        when(courtRoomMapping.getCrestCourtRoomName()).thenReturn("x");
        when(courtRoomMapping.getCrestCourtSiteUUID()).thenReturn(randomUUID());

        mockHearingTypeId();

        when(commonXhibitReferenceDataService.getXhibitHearingType(hearingTypeId).getExhibitHearingDescription()).thenReturn("Plea");

        final CurrentCourtStatus currentCourtStatus = hearingListXhibitResponseTransformer.transformFrom(hearingEventsToHearingMapper);
        final CourtRoom courtRoom = currentCourtStatus.getCourt().getCourtSites().get(0).getCourtRooms().get(0);
        final CaseDetail caseDetail = courtRoom.getCases().getCasesDetails().get(0);
        final String courtRoomName = courtRoom.getCourtRoomName();

        assertThat(currentCourtStatus.getCourt().getCourtName(), is(COURT_NAME));
        assertThat(caseDetail.getActivecase(), is(INACTIVE.getStatusCode()));
        assertThat(caseDetail.getHearingprogress(), is(STARTED.getProgressCode()));
        assertThat(caseDetail.getCppUrn(), is("ProsecutionAuthorityReference"));
        assertThat(courtRoomName, is("x"));
        assertThat(currentCourtStatus.getCourt().getCourtSites().size(), is(1));
    }

    @Test
    public void shouldTransformFromWithFinishedEventWithInactiveCaseStatus() {
        final UUID eventDefinitionsId = EventDefinitions.FINISHED.getEventDefinitionsId();

        final UUID courtCentreId = randomUUID();
        final UUID courtRoomId = randomUUID();
        final UUID hearingId = randomUUID();
        final HearingEvent hearingEvent = HearingEvent.hearingEvent().withHearingId(hearingId).withHearingEventDefinitionId(eventDefinitionsId).build();
        final List<Hearing> hearingList = asList(hearing);
        final List<ProsecutionCase> prosecutionCases = asList(prosecutionCase);
        final List<Defendant> defendantList = asList(defendant);
        final List<HearingDay> hearingDays = asList(hearingDay);
        final ProsecutionCaseIdentifier prosecutionCaseIdentifier = ProsecutionCaseIdentifier
                .prosecutionCaseIdentifier().withCaseURN("caseURN").build();
        final Map<UUID, UUID> eventDefinitionsIds = new HashMap<>();
        eventDefinitionsIds.putIfAbsent(hearingId, eventDefinitionsId);

        when(prosecutionCase.getProsecutionCaseIdentifier()).thenReturn(prosecutionCaseIdentifier);
        when(prosecutionCase.getDefendants()).thenReturn(defendantList);
        when(defendant.getPersonDefendant()).thenReturn(personDefendant);
        when(personDefendant.getPersonDetails()).thenReturn(person);
        when(person.getFirstName()).thenReturn("firstName");
        when(person.getMiddleName()).thenReturn("middleName");
        when(person.getLastName()).thenReturn("lastName");
        when(hearing.getHearingDays()).thenReturn(hearingDays);
        when(hearingDay.getSittingDay()).thenReturn(ZonedDateTime.now());
        when(hearing.getId()).thenReturn(hearingId);
        when(hearing.getType()).thenReturn(HearingType.hearingType().withDescription("hearingTypeDescription").build());
        when(hearing.getProsecutionCases()).thenReturn(prosecutionCases);
        when(hearing.getReportingRestrictionReason()).thenReturn("Automatic anonymity under the Sexual Offences (Amendment) Act 1992");
        when(hearing.getCourtCentre()).thenReturn(CourtCentre.courtCentre().withName(COURT_NAME).withRoomId(courtRoomId).withId(courtCentreId).build());
        when(hearingEventsToHearingMapper.getHearingList()).thenReturn(hearingList);
        when(hearingEventsToHearingMapper.getAllHearingEventBy(hearingId)).thenReturn(Optional.of(hearingEvent));
        when(hearingEventsToHearingMapper.getHearingIdAndEventDefinitionIds()).thenReturn(eventDefinitionsIds);
        when(commonXhibitReferenceDataService.getCourtRoomMappingBy(any(), any())).thenReturn(courtRoomMapping);
        when(courtRoomMapping.getCrestCourtRoomName()).thenReturn("x");
        when(courtRoomMapping.getCrestCourtSiteUUID()).thenReturn(randomUUID());

        mockHearingTypeId();

        when(commonXhibitReferenceDataService.getXhibitHearingType(hearingTypeId).getExhibitHearingDescription()).thenReturn("Plea");

        final CurrentCourtStatus currentCourtStatus = hearingListXhibitResponseTransformer.transformFrom(hearingEventsToHearingMapper);
        final CourtRoom courtRoom = currentCourtStatus.getCourt().getCourtSites().get(0).getCourtRooms().get(0);
        final CaseDetail caseDetail = courtRoom.getCases().getCasesDetails().get(0);
        final String courtRoomName = courtRoom.getCourtRoomName();

        assertThat(currentCourtStatus.getCourt().getCourtName(), is(COURT_NAME));
        assertThat(caseDetail.getActivecase(), is(INACTIVE.getStatusCode()));
        assertThat(caseDetail.getHearingprogress(), is(FINISHED.getProgressCode()));
        assertThat(caseDetail.getDefendants().size(), is(1));
        assertThat(caseDetail.getDefendants().get(0).getFirstName(), nullValue());
        assertThat(caseDetail.getDefendants().get(0).getMiddleName(), nullValue());
        assertThat(caseDetail.getDefendants().get(0).getLastName(), nullValue());
        assertThat(caseDetail.getCppUrn(), is("caseURN"));

        assertThat(courtRoomName, is("x"));
        assertThat(currentCourtStatus.getCourt().getCourtSites().size(), is(1));
    }

    @Test
    public void shouldTransformFromForStandaloneApplication() {
        final UUID courtCentreId = randomUUID();
        final UUID courtRoomId = randomUUID();
        final UUID hearingId = randomUUID();
        final HearingEvent hearingEvent = HearingEvent.hearingEvent().build();
        final List<Hearing> hearingList = asList(hearing);
        final List<CourtApplication> courtApplications = courtApplicationsSerializer.courtApplications(FileUtil.getPayload("court-applications.json"));
        final List<HearingDay> hearingDays = asList(hearingDay);

        final Set<UUID> activeHearingIds = new HashSet<>();

        final Map<UUID, UUID> eventDefinitionsIds = new HashMap<>();
        final UUID eventDefinitionsId = randomUUID();
        eventDefinitionsIds.putIfAbsent(hearingId, eventDefinitionsId);
        when(hearingEventsToHearingMapper.getActiveHearingIds()).thenReturn(activeHearingIds);
        when(hearingEventsToHearingMapper.getHearingIdAndEventDefinitionIds()).thenReturn(eventDefinitionsIds);
        when(hearing.getHearingDays()).thenReturn(hearingDays);
        when(hearingDay.getSittingDay()).thenReturn(ZonedDateTime.now());
        when(hearing.getId()).thenReturn(hearingId);
        when(hearing.getType()).thenReturn(HearingType.hearingType().withDescription("hearingTypeDescription").build());
        when(hearing.getCourtApplications()).thenReturn(courtApplications);
        when(hearing.getProsecutionCases()).thenReturn(Collections.emptyList());
        when(hearing.getCourtCentre()).thenReturn(CourtCentre.courtCentre().withName(COURT_NAME).withRoomId(courtRoomId).withId(courtCentreId).build());
        when(hearingEventsToHearingMapper.getHearingList()).thenReturn(hearingList);
        when(hearingEventsToHearingMapper.getAllHearingEventBy(hearingId)).thenReturn(Optional.of(hearingEvent));
        when(commonXhibitReferenceDataService.getCourtRoomMappingBy(any(), any())).thenReturn(courtRoomMapping);
        when(courtRoomMapping.getCrestCourtRoomName()).thenReturn("x");
        when(courtRoomMapping.getCrestCourtSiteUUID()).thenReturn(randomUUID());

        mockHearingTypeId();

        when(commonXhibitReferenceDataService.getXhibitHearingType(hearingTypeId).getExhibitHearingDescription()).thenReturn("Application");

        final CurrentCourtStatus currentCourtStatus = hearingListXhibitResponseTransformer.transformFrom(hearingEventsToHearingMapper);
        final CourtRoom courtRoom = currentCourtStatus.getCourt().getCourtSites().get(0).getCourtRooms().get(0);
        final CaseDetail caseDetail = courtRoom.getCases().getCasesDetails().get(0);
        final String courtRoomName = courtRoom.getCourtRoomName();

        assertThat(currentCourtStatus.getCourt().getCourtName(), is(COURT_NAME));
        assertThat(caseDetail.getDefendants().size(), is(1));
        assertThat(caseDetail.getDefendants().get(0).getFirstName(), is(courtApplications.get(0).getApplicant().getPersonDetails().getFirstName()));
        assertThat(caseDetail.getDefendants().get(0).getMiddleName(), is(courtApplications.get(0).getApplicant().getPersonDetails().getMiddleName()));
        assertThat(caseDetail.getDefendants().get(0).getLastName(), is(courtApplications.get(0).getApplicant().getPersonDetails().getLastName()));
        assertThat(caseDetail.getCppUrn(), is(courtApplications.get(0).getApplicationReference()));
        assertThat(courtRoomName, is("x"));
        assertThat(currentCourtStatus.getCourt().getCourtSites().size(), is(1));
    }

    @Test
    public void shouldTransformFromForStandaloneApplicationForOrganisation() {
        final UUID courtCentreId = randomUUID();
        final UUID courtRoomId = randomUUID();
        final UUID hearingId = randomUUID();
        final HearingEvent hearingEvent = HearingEvent.hearingEvent().build();
        final List<Hearing> hearingList = asList(hearing);
        final List<CourtApplication> courtApplications = courtApplicationsSerializer.courtApplications(FileUtil.getPayload("court-applications-with-organisation.json"));
        final List<HearingDay> hearingDays = asList(hearingDay);

        final Set<UUID> activeHearingIds = new HashSet<>();

        final Map<UUID, UUID> eventDefinitionsIds = new HashMap<>();
        final UUID eventDefinitionsId = randomUUID();
        eventDefinitionsIds.putIfAbsent(hearingId, eventDefinitionsId);
        when(hearingEventsToHearingMapper.getActiveHearingIds()).thenReturn(activeHearingIds);
        when(hearingEventsToHearingMapper.getHearingIdAndEventDefinitionIds()).thenReturn(eventDefinitionsIds);
        when(hearing.getHearingDays()).thenReturn(hearingDays);
        when(hearingDay.getSittingDay()).thenReturn(ZonedDateTime.now());
        when(hearing.getId()).thenReturn(hearingId);
        when(hearing.getType()).thenReturn(HearingType.hearingType().withDescription("hearingTypeDescription").build());
        when(hearing.getCourtApplications()).thenReturn(courtApplications);
        when(hearing.getProsecutionCases()).thenReturn(Collections.emptyList());
        when(hearing.getCourtCentre()).thenReturn(CourtCentre.courtCentre().withName(COURT_NAME).withRoomId(courtRoomId).withId(courtCentreId).build());
        when(hearingEventsToHearingMapper.getHearingList()).thenReturn(hearingList);
        when(hearingEventsToHearingMapper.getAllHearingEventBy(hearingId)).thenReturn(Optional.of(hearingEvent));
        when(commonXhibitReferenceDataService.getCourtRoomMappingBy(any(), any())).thenReturn(courtRoomMapping);
        when(courtRoomMapping.getCrestCourtRoomName()).thenReturn("x");
        when(courtRoomMapping.getCrestCourtSiteUUID()).thenReturn(randomUUID());

        mockHearingTypeId();

        when(commonXhibitReferenceDataService.getXhibitHearingType(hearingTypeId).getExhibitHearingDescription()).thenReturn("Application");

        final CurrentCourtStatus currentCourtStatus = hearingListXhibitResponseTransformer.transformFrom(hearingEventsToHearingMapper);
        final CourtRoom courtRoom = currentCourtStatus.getCourt().getCourtSites().get(0).getCourtRooms().get(0);
        final CaseDetail caseDetail = courtRoom.getCases().getCasesDetails().get(0);
        final String courtRoomName = courtRoom.getCourtRoomName();

        assertThat(currentCourtStatus.getCourt().getCourtName(), is(COURT_NAME));
        assertThat(caseDetail.getDefendants().size(), is(1));
        assertThat(caseDetail.getDefendants().get(0).getFirstName(), is(courtApplications.get(0).getApplicant().getOrganisation().getName()));
        assertThat(caseDetail.getCppUrn(), is(courtApplications.get(0).getApplicationReference()));
        assertThat(courtRoomName, is("x"));
        assertThat(currentCourtStatus.getCourt().getCourtSites().size(), is(1));
    }

    @Test
    public void shouldTransformWithAdjournedWhenLastEventIsPaused() {
        final UUID courtCentreId = randomUUID();
        final UUID courtRoomId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID hearing2Id= randomUUID();

        final List<Hearing> hearingList = Arrays.asList(Hearing.hearing()
                .withId(hearingId)
                .withHearingDays(asList(HearingDay.hearingDay().withSittingDay(ZonedDateTime.now()).build()))
                .withType(HearingType.hearingType().withId(randomUUID()).withDescription("hearingTypeDescription").build())
                .withJudiciary(asList(JudicialRole.judicialRole().withJudicialId(randomUUID()).withJudicialRoleType(JudicialRoleType.judicialRoleType().withJudiciaryType("circuit").build()).build()))
                .withCourtCentre(CourtCentre.courtCentre()
                        .withRoomId(courtRoomId)
                        .withName(COURT_NAME)
                        .withId(courtCentreId)
                        .build())
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withId(randomUUID())
                        .withProsecutionCaseIdentifier(ProsecutionCaseIdentifier.prosecutionCaseIdentifier().withCaseURN("case urn").build())
                        .withDefendants(asList(Defendant.defendant()
                                .withId(randomUUID())
                                .withPersonDefendant(PersonDefendant.personDefendant().withPersonDetails(Person.person().build()).build())
                                .withOffences(asList(Offence.offence()
                                        .withId(randomUUID())
                                        .build()))
                                .build()))
                        .build()))
                .build(),
                Hearing.hearing()
                        .withId(hearing2Id)
                        .withHearingDays(asList(HearingDay.hearingDay().withSittingDay(ZonedDateTime.now()).build()))
                        .withType(HearingType.hearingType().withId(randomUUID()).withDescription("hearingTypeDescription").build())
                        .withJudiciary(asList(JudicialRole.judicialRole().withJudicialId(randomUUID()).withJudicialRoleType(JudicialRoleType.judicialRoleType().withJudiciaryType("circuit").build()).build()))
                        .withCourtCentre(CourtCentre.courtCentre()
                                .withRoomId(courtRoomId)
                                .withName(COURT_NAME)
                                .withId(courtCentreId)
                                .build())
                        .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                                .withId(randomUUID())
                                .withProsecutionCaseIdentifier(ProsecutionCaseIdentifier.prosecutionCaseIdentifier().withCaseURN("case urn").build())
                                .withDefendants(asList(Defendant.defendant()
                                        .withId(randomUUID())
                                        .withPersonDefendant(PersonDefendant.personDefendant().withPersonDetails(Person.person().build()).build())
                                        .withOffences(asList(Offence.offence()
                                                .withId(randomUUID())
                                                .withJudicialResults(asList(JudicialResult.judicialResult()
                                                        .withJudicialResultId(randomUUID())
                                                        .build()))
                                                .build()))
                                        .build()))
                                .build()))
                        .build());

        when(hearingEventsToHearingMapper.getHearingList()).thenReturn(hearingList);
        when(hearingEventsToHearingMapper.getAllHearingEventBy(hearingId)).thenReturn(Optional.of(hearingEvent));
        when(hearingEventsToHearingMapper.getAllHearingEventBy(hearing2Id)).thenReturn(Optional.of(hearingEvent));
        final Set<UUID> activeHearingIds = new HashSet<>();
        activeHearingIds.add(hearingId);
        final Map<UUID, UUID> eventDefinitionsIds = new HashMap<>();
        final UUID finishedEventDefinitionsId = EventDefinitions.FINISHED.getEventDefinitionsId();
        final UUID adjournedEventDefinitionsId = EventDefinitions.PAUSED.getEventDefinitionsId();
        eventDefinitionsIds.putIfAbsent(hearingId, finishedEventDefinitionsId);
        eventDefinitionsIds.putIfAbsent(hearing2Id, adjournedEventDefinitionsId);
        final HearingEvent hearingEvent = HearingEvent.hearingEvent().withHearingId(hearingId).withHearingEventDefinitionId(adjournedEventDefinitionsId).build();
        final HearingEvent hearing2Event = HearingEvent.hearingEvent().withHearingId(hearing2Id).withHearingEventDefinitionId(finishedEventDefinitionsId).build();

        when(hearingEventsToHearingMapper.getHearingList()).thenReturn(hearingList);
        when(hearingEventsToHearingMapper.getAllHearingEventBy(hearingId)).thenReturn(Optional.of(hearingEvent));
        when(hearingEventsToHearingMapper.getAllHearingEventBy(hearing2Id)).thenReturn(Optional.of(hearing2Event));
        when(hearingEventsToHearingMapper.getHearingIdAndEventDefinitionIds()).thenReturn(eventDefinitionsIds);
        when(hearingEventsToHearingMapper.getActiveHearingIds()).thenReturn(activeHearingIds);
        when(commonXhibitReferenceDataService.getCourtRoomMappingBy(any(), any())).thenReturn(courtRoomMapping);
        when(courtRoomMapping.getCrestCourtRoomName()).thenReturn("x");
        when(courtRoomMapping.getCrestCourtSiteUUID()).thenReturn(randomUUID());

        final CurrentCourtStatus currentCourtStatus = hearingListXhibitResponseTransformer.transformFrom(hearingEventsToHearingMapper);
        final CourtRoom courtRoom = currentCourtStatus.getCourt().getCourtSites().get(0).getCourtRooms().get(0);
        final CaseDetail caseDetail = courtRoom.getCases().getCasesDetails().get(0);

        assertThat(caseDetail.getHearingprogress(), is(ADJOURNED.getProgressCode()));
        assertThat(currentCourtStatus.getCourt().getCourtName(), is(COURT_NAME));
        assertThat(caseDetail.getActivecase(), is(ACTIVE.getStatusCode()));
        assertThat(currentCourtStatus.getCourt().getCourtSites().size(), is(1));


        final CourtRoom courtRoomForSecondHearing = currentCourtStatus.getCourt().getCourtSites().get(0).getCourtRooms().get(0);
        final CaseDetail caseDetailForSecondHearing = courtRoomForSecondHearing.getCases().getCasesDetails().get(1);
        assertThat(caseDetailForSecondHearing.getHearingprogress(), is(FINISHED.getProgressCode()));
        assertThat(caseDetailForSecondHearing.getActivecase(), is(INACTIVE.getStatusCode()));

    }

    @Test
    public void shouldTransformWithInProgressWhenLastEventIsResume() {
        final UUID courtCentreId = randomUUID();
        final UUID courtRoomId = randomUUID();
        final UUID hearingId = randomUUID();


        final List<Hearing> hearingList = Arrays.asList(Hearing.hearing()
                        .withId(hearingId)
                        .withHearingDays(asList(HearingDay.hearingDay().withSittingDay(ZonedDateTime.now()).build()))
                        .withType(HearingType.hearingType().withId(randomUUID()).withDescription("hearingTypeDescription").build())
                        .withJudiciary(asList(JudicialRole.judicialRole().withJudicialId(randomUUID()).withJudicialRoleType(JudicialRoleType.judicialRoleType().withJudiciaryType("circuit").build()).build()))
                        .withCourtCentre(CourtCentre.courtCentre()
                                .withRoomId(courtRoomId)
                                .withName(COURT_NAME)
                                .withId(courtCentreId)
                                .build())
                        .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                                .withId(randomUUID())
                                .withProsecutionCaseIdentifier(ProsecutionCaseIdentifier.prosecutionCaseIdentifier().withCaseURN("case urn").build())
                                .withDefendants(asList(Defendant.defendant()
                                        .withId(randomUUID())
                                        .withPersonDefendant(PersonDefendant.personDefendant().withPersonDetails(Person.person().build()).build())
                                        .withOffences(asList(Offence.offence()
                                                .withId(randomUUID())
                                                .build()))
                                        .build()))
                                .build()))
                        .build());

        when(hearingEventsToHearingMapper.getHearingList()).thenReturn(hearingList);
        when(hearingEventsToHearingMapper.getAllHearingEventBy(hearingId)).thenReturn(Optional.of(hearingEvent));
        final Set<UUID> activeHearingIds = new HashSet<>();
        final Map<UUID, UUID> eventDefinitionsIds = new HashMap<>();
        final UUID resumeEventDefinitionsId = EventDefinitions.RESUME.getEventDefinitionsId();
        eventDefinitionsIds.putIfAbsent(hearingId, resumeEventDefinitionsId);
        final HearingEvent hearingEvent = HearingEvent.hearingEvent().withHearingId(hearingId).withHearingEventDefinitionId(resumeEventDefinitionsId).build();

        when(hearingEventsToHearingMapper.getHearingList()).thenReturn(hearingList);
        when(hearingEventsToHearingMapper.getAllHearingEventBy(hearingId)).thenReturn(Optional.of(hearingEvent));
        when(hearingEventsToHearingMapper.getHearingIdAndEventDefinitionIds()).thenReturn(eventDefinitionsIds);
        when(hearingEventsToHearingMapper.getActiveHearingIds()).thenReturn(activeHearingIds);
        when(commonXhibitReferenceDataService.getCourtRoomMappingBy(any(), any())).thenReturn(courtRoomMapping);
        when(courtRoomMapping.getCrestCourtRoomName()).thenReturn("x");
        when(courtRoomMapping.getCrestCourtSiteUUID()).thenReturn(randomUUID());

        final CurrentCourtStatus currentCourtStatus = hearingListXhibitResponseTransformer.transformFrom(hearingEventsToHearingMapper);
        final CourtRoom courtRoom = currentCourtStatus.getCourt().getCourtSites().get(0).getCourtRooms().get(0);
        final CaseDetail caseDetail = courtRoom.getCases().getCasesDetails().get(0);

        assertThat(caseDetail.getHearingprogress(), is(INPROGRESS.getProgressCode()));
        assertThat(currentCourtStatus.getCourt().getCourtName(), is(COURT_NAME));
        assertThat(caseDetail.getActivecase(), is(INACTIVE.getStatusCode()));
        assertThat(currentCourtStatus.getCourt().getCourtSites().size(), is(1));


    }

    private void mockHearingTypeId() {
        hearingTypeId = UUID.randomUUID();
        when(hearing.getType()).thenReturn(hearingType);
        when(hearingType.getId()).thenReturn(hearingTypeId);
    }
}