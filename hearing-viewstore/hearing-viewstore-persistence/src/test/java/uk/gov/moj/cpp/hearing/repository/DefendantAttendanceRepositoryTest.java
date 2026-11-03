package uk.gov.moj.cpp.hearing.repository;

import javax.persistence.NonUniqueResultException;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThrows;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimumInitiateHearingTemplate;

import uk.gov.justice.core.courts.AttendanceType;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantAttendance;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@SuppressWarnings("CdiInjectionPointsInspection")
@RunWith(CdiTestRunner.class)
public class DefendantAttendanceRepositoryTest {

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private DefendantAttendanceRepository defendantAttendanceRepository;

    @Inject
    private HearingJPAMapper hearingJPAMapper;


    @Before
    public void setup() {

    }

    @After
    public void teardown() {
        //hearings.forEach(hearing -> hearingRepository.attachAndRemove(hearingRepository.findBy(hearing.getId())));
    }

    @Test
    public void saveDefendantAttendance() {

        List<uk.gov.justice.core.courts.Hearing> hearings = new ArrayList<>();
        final InitiateHearingCommand initiateHearingCommand = minimumInitiateHearingTemplate();
        hearings.add(initiateHearingCommand.getHearing());
        uk.gov.justice.core.courts.Hearing hearing = hearings.get(0);
        Hearing hearingEntity = hearingJPAMapper.toJPA(hearing);
        hearingEntity.getProsecutionCases().iterator().next().setMarkers(null);
        hearingRepository.save(hearingEntity);

        final UUID id = randomUUID();
        final UUID hearingId = hearing.getId();
        final UUID defendantId = randomUUID();
        final LocalDate date = LocalDate.of(2017, 12, 13);
        final AttendanceType isInAttendance = AttendanceType.NOT_PRESENT;

        DefendantAttendance defendantAttendance = new DefendantAttendance();
        defendantAttendance.setId(new HearingSnapshotKey(id, hearingId));
        defendantAttendance.setDefendantId(defendantId);
        defendantAttendance.setDay(date);
        defendantAttendance.setAttendanceType(isInAttendance);

        this.defendantAttendanceRepository.saveAndFlush(defendantAttendance);

        DefendantAttendance entity = defendantAttendanceRepository.findByHearingIdDefendantIdAndDate(hearingId, defendantId, date);

        assertThat(entity.getId().getHearingId(), is(hearingId));
        assertThat(entity.getDefendantId(), is(defendantId));
        assertThat(entity.getDay(), is(date));
        assertThat(entity.getAttendanceType(), is(isInAttendance));

    }

    @Test
    public void chd08_findByHearingIdDefendantIdAndDateThrowsNonUniqueResultWhenTwoRowsMatch() {
        final UUID hearingId = savedHearingId();
        final UUID defendantId = randomUUID();
        final LocalDate day = LocalDate.of(2017, 12, 13);
        saveAttendance(hearingId, defendantId, day, AttendanceType.IN_PERSON);
        saveAttendance(hearingId, defendantId, day, AttendanceType.NOT_PRESENT);

        assertThrows(NonUniqueResultException.class,
                () -> defendantAttendanceRepository.findByHearingIdDefendantIdAndDate(hearingId, defendantId, day));
    }

    @Test
    public void chd08_findByHearingIdDefendantIdAndDateReturnsNullWhenNothingMatches() {
        assertThat(defendantAttendanceRepository.findByHearingIdDefendantIdAndDate(savedHearingId(), randomUUID(), LocalDate.of(2017, 12, 13)),
                is(nullValue()));
    }

    @Test
    public void chd11_findByHearingIdDefendantIdAndDateMatchesOnlyTheRequestedDay() {
        final UUID hearingId = savedHearingId();
        final UUID defendantId = randomUUID();
        saveAttendance(hearingId, defendantId, LocalDate.of(2017, 12, 13), AttendanceType.IN_PERSON);
        saveAttendance(hearingId, defendantId, LocalDate.of(2017, 12, 14), AttendanceType.NOT_PRESENT);

        final DefendantAttendance found = defendantAttendanceRepository.findByHearingIdDefendantIdAndDate(hearingId, defendantId, LocalDate.of(2017, 12, 14));

        assertThat(found.getAttendanceType(), is(AttendanceType.NOT_PRESENT));
    }

    // ── CHD harness ──────────────────────────────────────────────────────────────────────────────────────────────

    private UUID savedHearingId() {
        final Hearing hearingEntity = hearingJPAMapper.toJPA(minimumInitiateHearingTemplate().getHearing());
        hearingEntity.getProsecutionCases().iterator().next().setMarkers(null);
        hearingRepository.save(hearingEntity);
        return hearingEntity.getId();
    }

    private void saveAttendance(final UUID hearingId, final UUID defendantId, final LocalDate day, final AttendanceType attendanceType) {
        final DefendantAttendance defendantAttendance = new DefendantAttendance();
        defendantAttendance.setId(new HearingSnapshotKey(randomUUID(), hearingId));
        defendantAttendance.setDefendantId(defendantId);
        defendantAttendance.setDay(day);
        defendantAttendance.setAttendanceType(attendanceType);
        defendantAttendanceRepository.save(defendantAttendance);
    }
}
