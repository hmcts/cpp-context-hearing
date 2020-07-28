package uk.gov.moj.cpp.hearing.repository;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.core.courts.HearingDay.hearingDay;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimumInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.application.ApplicationDraftResult;
import uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingApplication;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingApplicationKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingCaseNote;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import com.google.common.collect.Sets;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class HearingRepositoryTest {

    private static final List<uk.gov.justice.core.courts.Hearing> hearings = new ArrayList<>();
    private static final uk.gov.justice.core.courts.Hearing simpleHearing = minimumInitiateHearingTemplate().getHearing();

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private HearingCaseNoteRepository hearingCaseNoteRepository;

    @Inject
    private HearingProsecutionCounselRepository hearingProsecutionCounselRepository;

    @Inject
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Inject
    private HearingJPAMapper hearingJPAMapper;

    @Inject
    private HearingApplicationRepository hearingApplicationRepository;

    @BeforeClass
    public static void create() {

        final InitiateHearingCommand initiateHearingCommand = minimumInitiateHearingTemplate();

        hearings.add(initiateHearingCommand.getHearing());
        //hearings.add(initiateHearingCommand.getHearing());
    }

    @Before
    public void setup() {

        hearings.forEach(hearing -> {

            final Hearing hearingEntity = hearingJPAMapper.toJPA(hearing);
            // because h2 incorrectly maps column type TEXT to VARCHAR(255)
            hearingEntity.setCourtApplicationsJson(hearingEntity.getCourtApplicationsJson().substring(0, 255));
            hearingEntity.getProsecutionCases().iterator().next().setMarkers(null);
            hearingEntity.setTargets(Sets.newHashSet(Target.target().setId(randomUUID()).setHearing(hearingEntity)));
            hearingEntity.setApplicationDraftResults(Sets.newHashSet(ApplicationDraftResult.applicationDraftResult().setId(randomUUID()).setHearing(hearingEntity)));
            hearingRepository.save(hearingEntity);

        });
    }

    @After
    public void teardown() {
        hearings.forEach(hearing -> hearingRepository.attachAndRemove(hearingRepository.findBy(hearing.getId())));
    }

    @Test
    @Ignore("because of issues with hearing case note jsonb column")
    public void shouldFindByStartDate() {
        assertEquals(1, hearingRepository.findByFilters(hearings.get(0).getHearingDays().get(0).getSittingDay().toLocalDate(), hearings.get(0).getCourtCentre().getId(), hearings.get(0).getCourtCentre().getRoomId()).size());
    }

    @Test
    public void shouldFindAll() {
        assertEquals(hearings.size(), hearingRepository.findAll().size());
    }

    @Test
    public void shouldFindByHearingId() {
        final UUID hearingId = hearings.get(0).getId();
        final Hearing hearingEntityRetrieved = hearingRepository.findBy(hearingId);
        assertNotNull(hearingEntityRetrieved);
    }

    @Test
    public void shouldFindCourtCenterByHearingID(){
        final UUID hearingId = hearings.get(0).getId();
        final CourtCentre courtCenter = hearingRepository.findCourtCenterByHearingId(hearingId);
        assertNotNull(courtCenter);
    }

    @Test
    public void shouldReturnNullWhenHearingIdIsAbsent(){
        final CourtCentre courtCenter = hearingRepository.findCourtCenterByHearingId(randomUUID());
        assertNull(courtCenter);
    }

    @Test
    public void shouldFindTargetsByHearingId(){
        final UUID hearingId = hearings.get(0).getId();
        final List<Target> targets = hearingRepository.findTargetsByHearingId(hearingId);
        assertThat(targets.size(), is(1));
    }

    @Test
    public void shouldReturnEmptyTargetsIfHearingDoNotHaveTargets(){
        final List<Target> targets = hearingRepository.findTargetsByHearingId(simpleHearing.getId());
        assertThat(targets.size(), is(0));
    }

    @Test
    public void shouldFindApplicationDraftResultsByHearingId(){
        final UUID hearingId = hearings.get(0).getId();
        final List<ApplicationDraftResult> applicationDraftResults = hearingRepository.findApplicationDraftResultsByHearingId(hearingId);
        assertThat(applicationDraftResults.size(), is(1));
    }

    @Test
    public void shouldReturnEmptyApplicationDraftResultsIfHearingDoNotHaveApplicationDraftResults(){
        final List<ApplicationDraftResult> applicationDraftResults = hearingRepository.findApplicationDraftResultsByHearingId(simpleHearing.getId());
        assertThat(applicationDraftResults.size(), is(0));
    }

    @Test
    public void shouldFindProsecutionCasesByHearingId(){
        final UUID hearingId = hearings.get(0).getId();
        final List<ProsecutionCase> prosecutionCases = hearingRepository.findProsecutionCasesByHearingId(hearingId);
        assertThat(prosecutionCases.size(), is(1));
    }

    @Test
    public void shouldReturnEmptyProsecutionCasesIfHearingDoNotHaveTargets(){
        final List<ProsecutionCase> prosecutionCases =
                hearingRepository.findProsecutionCasesByHearingId(simpleHearing.getId());
        assertThat(prosecutionCases.size(), is(0));
    }

    @Test
    @Ignore("because of issues with hearing case note jsonb column")
    public void shouldFindAssociatedCaseNote() {
        final UUID hearingId = hearings.get(0).getId();
        final Hearing hearing = hearingRepository.findBy(hearingId);

        final HearingCaseNote hearingCaseNote = new HearingCaseNote();
        hearingCaseNote.setHearing(hearing);
        hearingCaseNote.setId(new HearingSnapshotKey(randomUUID(), hearing.getId()));
        hearingCaseNoteRepository.save(hearingCaseNote);

        final Hearing hearingEntityRetrieved = hearingRepository.findBy(hearingId);

        assertNotNull(hearingEntityRetrieved.getHearingCaseNotes());
        assertThat(hearingEntityRetrieved.getHearingCaseNotes().size(), is(1));
    }

    @Test
    @Ignore("because of issues with hearing case note jsonb column")
    public void shouldNotFindByHearingId() {
        assertNull(hearingRepository.findBy(randomUUID()));
    }

    @Test
    public void shouldFindHearingsByCaseId() {

        final UUID caseId = hearings.get(0).getProsecutionCases().get(0).getId();

        final List<Hearing> hearingEntityRetrieved = hearingRepository.findByCaseId(caseId);

        assertNotNull(hearingEntityRetrieved);

        final Hearing hearing = hearingEntityRetrieved.get(0);
        assertThat(hearing.getId(), is(hearings.get(0).getId()));
        final HearingDay hearingDay = hearing.getHearingDays().iterator().next();
        assertThat(hearingDay.getSittingDay(), is(hearings.get(0).getHearingDays().get(0).getSittingDay()));
        assertThat(hearingDay.getListedDurationMinutes(), is(hearings.get(0).getHearingDays().get(0).getListedDurationMinutes()));
        assertThat(hearingDay.getListingSequence(), is(hearings.get(0).getHearingDays().get(0).getListingSequence()));
    }

    @Test
    public void shouldFindAllHearingsByApplicationId() {

        final UUID applicationId = hearings.get(0).getCourtApplications().get(0).getId();
        final Hearing hearingSaved = hearingRepository.findBy(hearings.get(0).getId());
        saveHearingApplication(hearingSaved, applicationId);

        final List<Hearing> hearingEntityRetrieved = hearingRepository.findAllHearingsByApplicationId(applicationId);
        assertNotNull(hearingEntityRetrieved);
        final Hearing hearing = hearingEntityRetrieved.get(0);
        assertThat(hearing.getId(), is(hearings.get(0).getId()));
        final HearingDay hearingDay = hearing.getHearingDays().iterator().next();
        assertThat(hearingDay.getSittingDay(), is(hearings.get(0).getHearingDays().get(0).getSittingDay()));
        assertThat(hearingDay.getListedDurationMinutes(), is(hearings.get(0).getHearingDays().get(0).getListedDurationMinutes()));
        assertThat(hearingDay.getListingSequence(), is(hearings.get(0).getHearingDays().get(0).getListingSequence()));
    }

    private void saveHearingApplication(final Hearing hearing, final UUID applicationId) {
        final UUID hearingId = hearing.getId();

        final HearingApplication hearingApplication = new HearingApplication();
        final HearingApplicationKey hearingApplicationKey = new HearingApplicationKey(applicationId, hearingId);
        hearingApplication.setId(hearingApplicationKey);
        hearingApplication.setHearing(hearing);
        hearing.getHearingApplications().add(hearingApplication);
        hearingApplicationRepository.save(hearingApplication);
    }

    @Test
    public void findByFilters() {

    }

    @Test
    public void findByHearingDate() {

        final uk.gov.justice.core.courts.Hearing hearing1 = addSampleHearing(false,
                ZonedDateTime.now(),
                ZonedDateTime.now().plusDays(1).plusMinutes(15));
        final uk.gov.justice.core.courts.Hearing hearing2 = addSampleHearing(false,
                ZonedDateTime.now(),
                ZonedDateTime.now().plusDays(1).plusMinutes(5),
                ZonedDateTime.now().plusDays(2).plusMinutes(5));
        addSampleHearing(false,
                ZonedDateTime.now(),
                ZonedDateTime.now().plusDays(2).plusMinutes(5));
        addSampleHearing(true,
                ZonedDateTime.now(),
                ZonedDateTime.now().plusDays(1).plusMinutes(5));


        final LocalDate localDate = LocalDate.now().plusDays(1);

        final List<Hearing> byHearingDate = hearingRepository.findByHearingDate(localDate);

        assertThat(byHearingDate.size(), is(2));
        assertThat(byHearingDate, containsInAnyOrder(
                isBean(Hearing.class).with(Hearing::getId, is(hearing1.getId())),
                isBean(Hearing.class).with(Hearing::getId, is(hearing2.getId()))
        ));
    }

    public uk.gov.justice.core.courts.Hearing addSampleHearing(final Boolean isBoxHearing, final ZonedDateTime... sittingDays) {
        final InitiateHearingCommand initiateHearingCommand;
        initiateHearingCommand = minimumInitiateHearingTemplate();
        for (final ZonedDateTime sittingDay : sittingDays) {
            initiateHearingCommand.getHearing().getHearingDays().add(hearingDay()
                    .withListedDurationMinutes(15)
                    .withSittingDay(sittingDay)
                    .withListingSequence(2)
                    .build());
        }
        initiateHearingCommand.getHearing().setIsBoxHearing(isBoxHearing);
        saveHearing(initiateHearingCommand);
        return initiateHearingCommand.getHearing();
    }

    public void saveHearing(final InitiateHearingCommand initiateHearingCommand) {
        final Hearing hearingEntity = hearingJPAMapper.toJPA(initiateHearingCommand.getHearing());
        // because h2 incorrectly maps column type TEXT to VARCHAR(255)
        hearingEntity.setCourtApplicationsJson(hearingEntity.getCourtApplicationsJson().substring(0, 255));
        hearingEntity.getProsecutionCases().iterator().next().setMarkers(null);
        hearingRepository.save(hearingEntity);
        hearings.add(initiateHearingCommand.getHearing());
    }
}
