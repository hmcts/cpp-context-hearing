package uk.gov.moj.cpp.hearing.query.view.helper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.CrackedIneffectiveTrial;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.ProsecutingAuthority;
import uk.gov.justice.services.test.utils.core.random.Generator;
import uk.gov.justice.services.test.utils.core.random.StringGenerator;
import uk.gov.moj.cpp.hearing.mapping.CourtApplicationsSerializer;
import uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre;
import uk.gov.moj.cpp.hearing.persist.entity.ha.CpsProsecutor;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingType;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingYouthCourDefendantsKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingYouthCourtDefendants;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Person;
import uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier;
import uk.gov.moj.cpp.hearing.query.view.response.TimelineHearingSummary;

import javax.json.JsonObject;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static com.google.common.collect.ImmutableSet.of;
import static java.lang.String.format;
import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.justice.core.courts.LegalEntityDefendant.legalEntityDefendant;
import static uk.gov.justice.core.courts.Organisation.organisation;
import static uk.gov.justice.core.courts.Person.person;

@RunWith(MockitoJUnitRunner.class)
public class TimelineHearingSummaryHelperTest {
    public static final Generator<String> STRING = new StringGenerator();
    private static final DateTimeFormatter DATE_FORMATTER = ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = ofPattern("HH:mm");
    private Hearing hearing;
    private HearingDay hearingDay;
    private HearingType hearingType;
    private CourtCentre courtCentre;
    private CrackedIneffectiveTrial crackedIneffectiveTrial;
    private JsonObject allCourtRooms;
    private String courtCentreName;
    private String courtRoomName;
    private UUID courtCentreId;
    private UUID courtRoomId;
    @InjectMocks
    private TimelineHearingSummaryHelper timelineHearingSummaryHelper;
    private Person person;
    private Organisation organisation;
    private ProsecutionCase prosecutionCase;
    private UUID applicationId;
    private ProsecutionCaseIdentifier prosecutionCaseIdentifier;
    private List<HearingYouthCourtDefendants> hearingYouthCourtDefendantList;
    @Mock
    private CourtApplicationsSerializer courtApplicationsSerializer;

    @Before
    public void setup() throws IOException {
        courtCentreId = UUID.randomUUID();
        courtRoomId = UUID.randomUUID();
        courtCentreName = STRING.next();
        courtRoomName = STRING.next();
        hearing = new Hearing();
        hearingDay = new HearingDay();
        final ZonedDateTime zonedDateTime = now().minusYears(1);
        hearingDay.setDate(zonedDateTime.toLocalDate());
        hearingDay.setDateTime(zonedDateTime);
        hearingDay.setListedDurationMinutes(new Random().nextInt());
        hearingDay.setCourtCentreId(courtCentreId);
        hearingDay.setCourtRoomId(courtRoomId);
        hearing.setHearingDays(of(hearingDay));
        hearingType = new HearingType();
        hearingType.setDescription(STRING.next());
        hearing.setHearingType(hearingType);
        courtCentre = new CourtCentre();
        courtCentre.setName(STRING.next());
        courtCentre.setRoomName(STRING.next());
        hearing.setCourtCentre(courtCentre);
        person = new Person();
        person.setFirstName(STRING.next());
        person.setLastName(STRING.next());
        final PersonDefendant personDefendant1 = new PersonDefendant();
        personDefendant1.setPersonDetails(person);
        final Defendant defendant1 = new Defendant();
        final HearingSnapshotKey hearingSnapshotKey1 = new HearingSnapshotKey();
        hearingSnapshotKey1.setId(randomUUID());
        defendant1.setId(hearingSnapshotKey1);
        defendant1.setPersonDefendant(personDefendant1);
        final Defendant defendant2 = new Defendant();
        final HearingSnapshotKey hearingSnapshotKey2 = new HearingSnapshotKey();
        hearingSnapshotKey2.setId(randomUUID());
        defendant2.setId(hearingSnapshotKey2);
        organisation = new Organisation();
        organisation.setName(STRING.next());
        defendant2.setLegalEntityOrganisation(organisation);
        final Set<Defendant> defendants = of(defendant1, defendant2);
        prosecutionCase = new ProsecutionCase();
        prosecutionCase.setDefendants(defendants);
        hearing.setProsecutionCases(of(prosecutionCase));
        crackedIneffectiveTrial = new CrackedIneffectiveTrial(STRING.next(), LocalDate.now(), STRING.next(), randomUUID(), STRING.next());
        applicationId = UUID.randomUUID();
        allCourtRooms = buildCourtRoomsJson();
        HearingYouthCourDefendantsKey hearingYouthCourDefendantsKey = new HearingYouthCourDefendantsKey(UUID.randomUUID(),UUID.randomUUID());
        HearingYouthCourtDefendants hearingYouthCourtDefendants = new HearingYouthCourtDefendants(hearingYouthCourDefendantsKey);
        hearingYouthCourtDefendantList = Arrays.asList(hearingYouthCourtDefendants);
        prosecutionCaseIdentifier = new ProsecutionCaseIdentifier();
        prosecutionCaseIdentifier.setProsecutionAuthorityCode(STRING.next());
        prosecutionCaseIdentifier.setProsecutorAuthorityName(STRING.next());
    }

    @Test
    public void shouldCreateTimelineHearingSummary() {
        final TimelineHearingSummary timeLineHearingSummary = timelineHearingSummaryHelper.createTimeLineHearingSummary(hearingDay, hearing, crackedIneffectiveTrial, allCourtRooms, hearingYouthCourtDefendantList);
        assertThat(timeLineHearingSummary.getHearingId(), is(hearing.getId()));
        assertThat(timeLineHearingSummary.getHearingDate(), is(hearingDay.getDate()));
        assertThat(timeLineHearingSummary.getHearingDateAsString(), is(hearingDay.getDate().format(DATE_FORMATTER)));
        assertThat(timeLineHearingSummary.getHearingTime(), is(hearingDay.getDateTime().format(TIME_FORMATTER)));
        assertThat(timeLineHearingSummary.getStartTime(), is(hearingDay.getSittingDay()));
        assertThat(timeLineHearingSummary.getHearingType(), is(hearing.getHearingType().getDescription()));
        assertThat(timeLineHearingSummary.getCourtHouse(), is(courtCentreName));
        assertThat(timeLineHearingSummary.getCourtRoom(), is(courtRoomName));
        assertThat(timeLineHearingSummary.getEstimatedDuration(), is(hearingDay.getListedDurationMinutes()));
        assertThat(timeLineHearingSummary.getDefendants().size(), is(2));
        assertThat(timeLineHearingSummary.getDefendants().get(0), is(format("%s %s", person.getFirstName(), person.getLastName())));
        assertThat(timeLineHearingSummary.getDefendants().get(1), is(organisation.getName()));
        assertThat(timeLineHearingSummary.getOutcome(), is(crackedIneffectiveTrial.getType()));
        assertThat(timeLineHearingSummary.getIsBoxHearing(), nullValue());
    }

    @Test
    public void shouldCreateTimelineHearingSummaryWithIsBoxHearing() {
        hearing.setIsBoxHearing(true);
        final TimelineHearingSummary timeLineHearingSummary = timelineHearingSummaryHelper.createTimeLineHearingSummary(hearingDay, hearing, crackedIneffectiveTrial, allCourtRooms, hearingYouthCourtDefendantList);
        assertThat(timeLineHearingSummary.getHearingId(), is(hearing.getId()));
        assertThat(timeLineHearingSummary.getHearingDate(), is(hearingDay.getDate()));
        assertThat(timeLineHearingSummary.getHearingDateAsString(), is(hearingDay.getDate().format(DATE_FORMATTER)));
        assertThat(timeLineHearingSummary.getHearingTime(), is(hearingDay.getDateTime().format(TIME_FORMATTER)));
        assertThat(timeLineHearingSummary.getStartTime(), is(hearingDay.getSittingDay()));
        assertThat(timeLineHearingSummary.getHearingType(), is(hearing.getHearingType().getDescription()));
        assertThat(timeLineHearingSummary.getCourtHouse(), is(courtCentreName));
        assertThat(timeLineHearingSummary.getCourtRoom(), is(courtRoomName));
        assertThat(timeLineHearingSummary.getEstimatedDuration(), is(hearingDay.getListedDurationMinutes()));
        assertThat(timeLineHearingSummary.getDefendants().size(), is(2));
        assertThat(timeLineHearingSummary.getDefendants().get(0), is(format("%s %s", person.getFirstName(), person.getLastName())));
        assertThat(timeLineHearingSummary.getDefendants().get(1), is(organisation.getName()));
        assertThat(timeLineHearingSummary.getOutcome(), is(crackedIneffectiveTrial.getType()));
        assertThat(timeLineHearingSummary.getIsBoxHearing(), is(true));
    }

    @Test
    public void shouldCreateTimelineHearingSummaryWithoutIsBoxHearing() {
        hearing.setIsBoxHearing(false);
        final TimelineHearingSummary timeLineHearingSummary = timelineHearingSummaryHelper.createTimeLineHearingSummary(hearingDay, hearing, crackedIneffectiveTrial, allCourtRooms, null);
        assertThat(timeLineHearingSummary.getHearingId(), is(hearing.getId()));
        assertThat(timeLineHearingSummary.getHearingDate(), is(hearingDay.getDate()));
        assertThat(timeLineHearingSummary.getHearingDateAsString(), is(hearingDay.getDate().format(DATE_FORMATTER)));
        assertThat(timeLineHearingSummary.getHearingTime(), is(hearingDay.getDateTime().format(TIME_FORMATTER)));
        assertThat(timeLineHearingSummary.getStartTime(), is(hearingDay.getSittingDay()));
        assertThat(timeLineHearingSummary.getHearingType(), is(hearing.getHearingType().getDescription()));
        assertThat(timeLineHearingSummary.getCourtHouse(), is(courtCentreName));
        assertThat(timeLineHearingSummary.getCourtRoom(), is(courtRoomName));
        assertThat(timeLineHearingSummary.getEstimatedDuration(), is(hearingDay.getListedDurationMinutes()));
        assertThat(timeLineHearingSummary.getDefendants().size(), is(2));
        assertThat(timeLineHearingSummary.getDefendants().get(0), is(format("%s %s", person.getFirstName(), person.getLastName())));
        assertThat(timeLineHearingSummary.getDefendants().get(1), is(organisation.getName()));
        assertThat(timeLineHearingSummary.getOutcome(), is(crackedIneffectiveTrial.getType()));
        assertThat(timeLineHearingSummary.getIsBoxHearing(), nullValue());
    }

    @Test
    public void shouldCreateTimelineHearingSummaryFilteredByApplicationId() {

        when(courtApplicationsSerializer.courtApplications(anyString())).thenReturn(asList(getCourtApplication(applicationId)));

        final TimelineHearingSummary timeLineHearingSummary = timelineHearingSummaryHelper
                .createTimeLineHearingSummary(hearingDay, hearing, crackedIneffectiveTrial, allCourtRooms, hearingYouthCourtDefendantList, applicationId);

        assertThat(timeLineHearingSummary.getHearingId(), is(hearing.getId()));
        assertThat(timeLineHearingSummary.getHearingDate(), is(hearingDay.getDate()));
        assertThat(timeLineHearingSummary.getHearingDateAsString(), is(hearingDay.getDate().format(DATE_FORMATTER)));
        assertThat(timeLineHearingSummary.getHearingTime(), is(hearingDay.getDateTime().format(TIME_FORMATTER)));
        assertThat(timeLineHearingSummary.getStartTime(), is(hearingDay.getSittingDay()));
        assertThat(timeLineHearingSummary.getHearingType(), is(hearing.getHearingType().getDescription()));
        assertThat(timeLineHearingSummary.getCourtHouse(), is(courtCentreName));
        assertThat(timeLineHearingSummary.getCourtRoom(), is(courtRoomName));
        assertThat(timeLineHearingSummary.getEstimatedDuration(), is(hearingDay.getListedDurationMinutes()));
        assertThat(timeLineHearingSummary.getApplicants().size(), is(1));
        assertThat(timeLineHearingSummary.getApplicants().get(0), is(format("%s %s", person.getFirstName(), person.getLastName())));
        assertThat(timeLineHearingSummary.getIsBoxHearing(), nullValue());
    }

    @Test
    public void shouldCreateTimelineHearingSummaryForApplicantOrganisationFilteredByApplicationId() {
        when(courtApplicationsSerializer.courtApplications(anyString())).thenReturn(asList(getCourtApplicationApplicantAsOrganisation(applicationId)));

        final TimelineHearingSummary timeLineHearingSummary = timelineHearingSummaryHelper
                .createTimeLineHearingSummary(hearingDay, hearing, crackedIneffectiveTrial, allCourtRooms, null, applicationId);

        assertThat(timeLineHearingSummary.getApplicants().size(), is(1));
        assertThat(timeLineHearingSummary.getApplicants().get(0), is(organisation.getName()));
        assertThat(timeLineHearingSummary.getIsBoxHearing(), nullValue());
    }

    @Test
    public void shouldCreateTimelineHearingSummaryForApplicantMasterDefendantilteredByApplicationId() {
        when(courtApplicationsSerializer.courtApplications(anyString())).thenReturn(asList(getCourtApplicationApplicantAsMasterDefendant(applicationId)));

        final TimelineHearingSummary timeLineHearingSummary = timelineHearingSummaryHelper
                .createTimeLineHearingSummary(hearingDay, hearing, crackedIneffectiveTrial, allCourtRooms, null, applicationId);

        assertThat(timeLineHearingSummary.getApplicants().size(), is(1));
        assertThat(timeLineHearingSummary.getApplicants().get(0), is(organisation.getName()));
        assertThat(timeLineHearingSummary.getIsBoxHearing(), nullValue());
    }

    @Test
    public void shouldCreateTimelineHearingSummaryForApplicantProsecutingAuthorityFilteredByApplicationId() {
        when(courtApplicationsSerializer.courtApplications(anyString())).thenReturn(asList(getCourtApplicationApplicantAsProsecutingAuthority(applicationId)));

        final TimelineHearingSummary timeLineHearingSummary = timelineHearingSummaryHelper
                .createTimeLineHearingSummary(hearingDay, hearing, crackedIneffectiveTrial, allCourtRooms, null, applicationId);

        assertThat(timeLineHearingSummary.getApplicants().size(), is(1));
        assertThat(timeLineHearingSummary.getApplicants().get(0), is(prosecutionCaseIdentifier.getProsecutionAuthorityCode()));
        assertThat(timeLineHearingSummary.getIsBoxHearing(), nullValue());
    }

    private CourtApplication getCourtApplication(UUID applicationId) {
        return CourtApplication.courtApplication()
                .withId(applicationId)
                .withApplicant(CourtApplicationParty.courtApplicationParty()
                        .withPersonDetails(person().withFirstName(person.getFirstName()).withLastName(person.getLastName()).build())
                        .build()).build();
    }

    private CourtApplication getCourtApplicationApplicantAsOrganisation(UUID applicationId) {
        return CourtApplication.courtApplication()
                .withId(applicationId)
                .withApplicant(CourtApplicationParty.courtApplicationParty()
                        .withOrganisation(organisation().withName(organisation.getName()).build())
                        .build()).build();
    }

    private CourtApplication getCourtApplicationApplicantAsMasterDefendant(UUID applicationId) {
        return CourtApplication.courtApplication()
                .withId(applicationId)
                .withApplicant(CourtApplicationParty.courtApplicationParty()
                        .withMasterDefendant(MasterDefendant.masterDefendant().withLegalEntityDefendant(legalEntityDefendant()
                                .withOrganisation(organisation().withName(organisation.getName()).build()).build()).build())
                        .build()).build();
    }

    private CourtApplication getCourtApplicationApplicantAsProsecutingAuthority(UUID applicationId) {
        return CourtApplication.courtApplication()
                .withId(applicationId)
                .withApplicant(CourtApplicationParty.courtApplicationParty()
                        .withProsecutingAuthority(ProsecutingAuthority.prosecutingAuthority().withName(prosecutionCaseIdentifier.getProsecutorAuthorityName())
                                .withProsecutionAuthorityCode(prosecutionCaseIdentifier.getProsecutionAuthorityCode()).build())
                        .build()).build();
    }

    @Test
    public void shouldHandleEmptyFields() {
        final TimelineHearingSummary timeLineHearingSummary = timelineHearingSummaryHelper.createTimeLineHearingSummary(new HearingDay(), new Hearing(), new CrackedIneffectiveTrial(null, null,  null,null, null),createObjectBuilder().build(), hearingYouthCourtDefendantList);
        assertThat(timeLineHearingSummary, is(notNullValue()));
    }

    @Test
    public void shouldHandleEmptyFields2() {

        prosecutionCase = new ProsecutionCase();
        hearing.setProsecutionCases(of(prosecutionCase));
        final TimelineHearingSummary timeLineHearingSummary = timelineHearingSummaryHelper.createTimeLineHearingSummary(hearingDay, hearing, crackedIneffectiveTrial, allCourtRooms, hearingYouthCourtDefendantList);
        assertThat(timeLineHearingSummary, is(notNullValue()));
    }

    @Test
    public void shouldIndicateEffectiveOutcomeInTimelineHearingSummary() {
        hearing.setIsEffectiveTrial(true);

        final TimelineHearingSummary timeLineHearingSummary = timelineHearingSummaryHelper.createTimeLineHearingSummary(hearingDay, hearing, null,null, hearingYouthCourtDefendantList);
        assertThat(timeLineHearingSummary.getOutcome(), is("Effective"));
    }

    @Test
    public void shouldIndicateVacatedOutcomeInTimelineHearingSummary() {
        hearing.setIsVacatedTrial(true);

        final TimelineHearingSummary timeLineHearingSummary = timelineHearingSummaryHelper.createTimeLineHearingSummary(hearingDay, hearing, null,null, hearingYouthCourtDefendantList);
        assertThat(timeLineHearingSummary.getOutcome(), is("Vacated"));
    }

    private JsonObject buildCourtRoomsJson() {
        return createObjectBuilder()
                .add("organisationunits", createArrayBuilder()
                        .add(createObjectBuilder()
                                .add("id", "60853c27-8a9d-349a-aeb5-7f5049a774dd")
                                .add("oucodeL3Name", "Dummy Court Centre Name")
                                .add("courtrooms", createArrayBuilder()
                                        .add(createObjectBuilder()
                                                .add("id", "67348d5d-4742-3ba6-9e9b-29a7595b5c3e")
                                                .add("courtroomName", "Dummy Court Room 1")))
                                .build())
                        .add(createObjectBuilder()
                            .add("id", courtCentreId.toString())
                            .add("oucodeL3Name", courtCentreName)
                            .add("courtrooms", createArrayBuilder()
                                    .add(createObjectBuilder()
                                            .add("id", courtRoomId.toString())
                                            .add("courtroomName", "Dummy Court Room 2")))
                            .add("courtrooms", createArrayBuilder()
                                    .add(createObjectBuilder()
                                            .add("id", courtRoomId.toString())
                                            .add("courtroomName", courtRoomName)))
                            .build())
                        .build()
                )
                .build();
    }
}
