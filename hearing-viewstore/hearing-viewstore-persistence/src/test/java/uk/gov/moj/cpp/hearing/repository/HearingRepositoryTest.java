package uk.gov.moj.cpp.hearing.repository;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimumInitiateHearingTemplate;

import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingApplication;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingApplicationKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingCaseNote;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

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
    }

    @Before
    public void setup() {

        hearings.forEach(hearing -> {

            final Hearing hearingEntity = hearingJPAMapper.toJPA(hearing);
            // because h2 incorrectly maps column type TEXT to VARCHAR(255)
            hearingEntity.setCourtApplicationsJson(hearingEntity.getCourtApplicationsJson().substring(0, 255));
            hearingEntity.getProsecutionCases().iterator().next().setMarkers(null);
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
    @Ignore("because of issues with hearing case note jsonb column")
    public void shouldFindAssociatedCaseNote() {
        final UUID hearingId = hearings.get(0).getId();
        final Hearing hearing = hearingRepository.findBy(hearingId);

        final HearingCaseNote hearingCaseNote = new HearingCaseNote();
        hearingCaseNote.setHearing(hearing);
        hearingCaseNote.setId(new HearingSnapshotKey(UUID.randomUUID(), hearing.getId()));
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

    private void saveHearingApplication(final Hearing hearing, UUID applicationId) {
        UUID hearingId = hearing.getId();

        HearingApplication hearingApplication = new HearingApplication();
        HearingApplicationKey hearingApplicationKey = new HearingApplicationKey(applicationId, hearingId);
        hearingApplication.setId(hearingApplicationKey);
        hearingApplication.setHearing(hearing);
        hearing.getHearingApplications().add(hearingApplication);
        hearingApplicationRepository.save(hearingApplication);
    }

}
