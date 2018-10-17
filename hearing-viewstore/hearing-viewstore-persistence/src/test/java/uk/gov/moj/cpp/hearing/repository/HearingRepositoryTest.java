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
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingCaseNote;
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

    private static final List<uk.gov.justice.json.schemas.core.Hearing> hearings = new ArrayList<>();

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private HearingCaseNoteRepository hearingCaseNoteRepository;

    @Inject
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Inject
    private HearingJPAMapper hearingJPAMapper;

    @BeforeClass
    public static void create() {

        final InitiateHearingCommand initiateHearingCommand = minimumInitiateHearingTemplate();

        hearings.add(initiateHearingCommand.getHearing());
    }

    @Before
    public void setup() {

        hearings.forEach(hearing -> {

            Hearing hearingEntity = hearingJPAMapper.toJPA(hearing);

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
}
