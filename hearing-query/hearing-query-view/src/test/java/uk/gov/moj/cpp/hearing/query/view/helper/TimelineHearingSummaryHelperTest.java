package uk.gov.moj.cpp.hearing.query.view.helper;

import static com.google.common.collect.ImmutableSet.of;
import static java.lang.String.format;
import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.justice.core.courts.Person.person;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.CrackedIneffectiveTrial;
import uk.gov.justice.services.test.utils.core.random.Generator;
import uk.gov.justice.services.test.utils.core.random.StringGenerator;
import uk.gov.moj.cpp.hearing.mapping.CourtApplicationsSerializer;
import uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingType;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Person;
import uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.query.view.response.TimelineHearingSummary;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
    @InjectMocks
    private TimelineHearingSummaryHelper timelineHearingSummaryHelper;
    private Person person;
    private Organisation organisation;
    private ProsecutionCase prosecutionCase;
    private UUID applicationId;

    @Mock
    private CourtApplicationsSerializer courtApplicationsSerializer;

    @Before
    public void setup() throws IOException {
        hearing = new Hearing();
        hearingDay = new HearingDay();
        final ZonedDateTime zonedDateTime = now().minusYears(1);
        hearingDay.setDate(zonedDateTime.toLocalDate());
        hearingDay.setDateTime(zonedDateTime);
        hearingDay.setListedDurationMinutes(new Random().nextInt());
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
        crackedIneffectiveTrial = new CrackedIneffectiveTrial(STRING.next(), STRING.next(), randomUUID(), STRING.next());
        applicationId = UUID.randomUUID();
    }

    @Test
    public void shouldCreateTimelineHearingSummary() {
        final TimelineHearingSummary timeLineHearingSummary = timelineHearingSummaryHelper.createTimeLineHearingSummary(hearingDay, hearing, crackedIneffectiveTrial);
        assertThat(timeLineHearingSummary.getHearingId(), is(hearing.getId()));
        assertThat(timeLineHearingSummary.getHearingDate(), is(hearingDay.getDate()));
        assertThat(timeLineHearingSummary.getHearingDateAsString(), is(hearingDay.getDate().format(DATE_FORMATTER)));
        assertThat(timeLineHearingSummary.getHearingTime(), is(hearingDay.getDateTime().format(TIME_FORMATTER)));
        assertThat(timeLineHearingSummary.getHearingType(), is(hearing.getHearingType().getDescription()));
        assertThat(timeLineHearingSummary.getCourtHouse(), is(hearing.getCourtCentre().getName()));
        assertThat(timeLineHearingSummary.getCourtRoom(), is(hearing.getCourtCentre().getRoomName()));
        assertThat(timeLineHearingSummary.getEstimatedDuration(), is(hearingDay.getListedDurationMinutes()));
        assertThat(timeLineHearingSummary.getDefendants().size(), is(2));
        assertThat(timeLineHearingSummary.getDefendants().get(0), is(format("%s %s", person.getFirstName(), person.getLastName())));
        assertThat(timeLineHearingSummary.getDefendants().get(1), is(organisation.getName()));
        assertThat(timeLineHearingSummary.getOutcome(), is(crackedIneffectiveTrial.getType()));
    }

    @Test
    public void shouldCreateTimelineHearingSummaryFilteredByApplicationId() {

        when(courtApplicationsSerializer.courtApplications(anyString())).thenReturn(asList(getCourtApplication(applicationId)));

        final TimelineHearingSummary timeLineHearingSummary = timelineHearingSummaryHelper
                .createTimeLineHearingSummary(hearingDay, hearing, crackedIneffectiveTrial, applicationId);

        assertThat(timeLineHearingSummary.getHearingId(), is(hearing.getId()));
        assertThat(timeLineHearingSummary.getHearingDate(), is(hearingDay.getDate()));
        assertThat(timeLineHearingSummary.getHearingDateAsString(), is(hearingDay.getDate().format(DATE_FORMATTER)));
        assertThat(timeLineHearingSummary.getHearingTime(), is(hearingDay.getDateTime().format(TIME_FORMATTER)));
        assertThat(timeLineHearingSummary.getHearingType(), is(hearing.getHearingType().getDescription()));
        assertThat(timeLineHearingSummary.getCourtHouse(), is(hearing.getCourtCentre().getName()));
        assertThat(timeLineHearingSummary.getCourtRoom(), is(hearing.getCourtCentre().getRoomName()));
        assertThat(timeLineHearingSummary.getEstimatedDuration(), is(hearingDay.getListedDurationMinutes()));
        assertThat(timeLineHearingSummary.getApplicants().size(), is(1));
        assertThat(timeLineHearingSummary.getApplicants().get(0), is(format("%s %s", person.getFirstName(), person.getLastName())));
    }

    private CourtApplication getCourtApplication(UUID applicationId) {
        return CourtApplication.courtApplication()
                .withId(applicationId)
                .withApplicant(CourtApplicationParty.courtApplicationParty()
                        .withPersonDetails(person().withFirstName(person.getFirstName()).withLastName(person.getLastName()).build())
                        .build()).build();
    }

    @Test
    public void shouldHandleEmptyFields() {
        final TimelineHearingSummary timeLineHearingSummary = timelineHearingSummaryHelper.createTimeLineHearingSummary(new HearingDay(), new Hearing(), new CrackedIneffectiveTrial(null, null, null, null));
        assertThat(timeLineHearingSummary, is(notNullValue()));
    }

    @Test
    public void shouldHandleEmptyFields2() {
        prosecutionCase = new ProsecutionCase();
        hearing.setProsecutionCases(of(prosecutionCase));
        final TimelineHearingSummary timeLineHearingSummary = timelineHearingSummaryHelper.createTimeLineHearingSummary(hearingDay, hearing, crackedIneffectiveTrial);
        assertThat(timeLineHearingSummary, is(notNullValue()));
    }
}