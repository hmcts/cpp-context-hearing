package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.Arrays.asList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.moj.cpp.hearing.query.view.service.CaseStatusCode.ACTIVE;
import static uk.gov.moj.cpp.hearing.query.view.service.CaseStatusCode.INACTIVE;
import static uk.gov.moj.cpp.hearing.query.view.service.ProgessStatusCode.FINISHED;
import static uk.gov.moj.cpp.hearing.query.view.service.ProgessStatusCode.INPROGRESS;
import static uk.gov.moj.cpp.hearing.query.view.service.ProgessStatusCode.STARTED;

import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.HearingEvent;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.moj.cpp.external.domain.referencedata.CourtRoomMapping;
import uk.gov.moj.cpp.hearing.query.view.referencedata.XhibitCourtRoomMapperCache;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CaseDetail;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtRoom;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingListXhibitResponseTransformerTest {
    private static final String COURT_NAME = "Court 1";

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

    @Mock
    private XhibitCourtRoomMapperCache xhibitCourtRoomMapperCache;

    @Mock
    private HearingEventsToHearingMapper hearingEventsToHearingMapper;

    @Mock
    private Hearing hearing;

    @Mock
    private CourtRoomMapping courtRoomMapping;

    @Mock
    private HearingEvent hearingEvent;

    @InjectMocks
    private HearingListXhibitResponseTransformer hearingListXhibitResponseTransformer;


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
        when(xhibitCourtRoomMapperCache.getXhibitCourtRoomForCourtCentreAndRoomId(any(), any())).thenReturn(courtRoomMapping);
        when(courtRoomMapping.getCrestCourtRoomName()).thenReturn("x");
        when(courtRoomMapping.getCrestCourtSiteUUID()).thenReturn(randomUUID());

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
        when(xhibitCourtRoomMapperCache.getXhibitCourtRoomForCourtCentreAndRoomId(any(), any())).thenReturn(courtRoomMapping);
        when(courtRoomMapping.getCrestCourtRoomName()).thenReturn("x");
        when(courtRoomMapping.getCrestCourtSiteUUID()).thenReturn(randomUUID());

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
        final UUID finishedEventDefinitionId = fromString("0df93f18-0a21-40f5-9fb3-da4749cd70fe");
        final UUID courtCentreId = randomUUID();
        final UUID courtRoomId = randomUUID();
        final UUID hearingId = randomUUID();
        final HearingEvent hearingEvent = HearingEvent.hearingEvent().withHearingEventDefinitionId(finishedEventDefinitionId).build();
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
        when(xhibitCourtRoomMapperCache.getXhibitCourtRoomForCourtCentreAndRoomId(any(), any())).thenReturn(courtRoomMapping);
        when(courtRoomMapping.getCrestCourtRoomName()).thenReturn("x");
        when(courtRoomMapping.getCrestCourtSiteUUID()).thenReturn(randomUUID());

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
    public void shouldTransformFromWithFinishedEventWithInactiveCaseStatus() {
        final UUID eventDefinitionsId = EventDefinitions.FINISHED.getEventDefinitionsId();

        final UUID courtCentreId = randomUUID();
        final UUID courtRoomId = randomUUID();
        final UUID hearingId = randomUUID();
        final HearingEvent hearingEvent = HearingEvent.hearingEvent().withHearingEventDefinitionId(eventDefinitionsId).build();
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
        when(xhibitCourtRoomMapperCache.getXhibitCourtRoomForCourtCentreAndRoomId(any(), any())).thenReturn(courtRoomMapping);
        when(courtRoomMapping.getCrestCourtRoomName()).thenReturn("x");
        when(courtRoomMapping.getCrestCourtSiteUUID()).thenReturn(randomUUID());

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
        assertThat(courtRoomName, is("x"));
        assertThat(currentCourtStatus.getCourt().getCourtSites().size(), is(1));
    }
}