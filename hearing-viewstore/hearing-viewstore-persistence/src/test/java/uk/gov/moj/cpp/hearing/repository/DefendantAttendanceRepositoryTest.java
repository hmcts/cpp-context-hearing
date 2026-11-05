package uk.gov.moj.cpp.hearing.repository;


import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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

import jakarta.persistence.NonUniqueResultException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.justice.services.test.utils.persistence.HibernateTestEntityManagerProvider;

@ExtendWith(MockitoExtension.class)
class DefendantAttendanceRepositoryTest {

    private static final String PERSISTENCE_UNIT = "hearing-test-persistence-unit";

    @RegisterExtension
    static HibernateTestEntityManagerProvider hibernateTestEntityManagerProvider =
            new HibernateTestEntityManagerProvider(PERSISTENCE_UNIT);

    @Mock
    private HearingJPAMapper hearingJPAMapper;

    private HearingRepository hearingRepository;
    private DefendantAttendanceRepository defendantAttendanceRepository;

    @BeforeEach
    void openEntityManagerAndCreateRepository() {
        hearingRepository = new HearingRepository();
        hibernateTestEntityManagerProvider.injectEntityManagerInto(hearingRepository);

        defendantAttendanceRepository = new DefendantAttendanceRepository();
        hibernateTestEntityManagerProvider.injectEntityManagerInto(defendantAttendanceRepository);
    }

    @Test
    void saveDefendantAttendance() {

        List<uk.gov.justice.core.courts.Hearing> hearings = new ArrayList<>();
        final InitiateHearingCommand initiateHearingCommand = minimumInitiateHearingTemplate();
        hearings.add(initiateHearingCommand.getHearing());
        uk.gov.justice.core.courts.Hearing hearing = hearings.get(0);

        final Hearing hearingEntity = new Hearing();
        hearingEntity.setId(hearing.getId());
        when(hearingJPAMapper.toJPA(any(uk.gov.justice.core.courts.Hearing.class))).thenReturn(hearingEntity);

        Hearing mappedHearingEntity = hearingJPAMapper.toJPA(hearing);
        hearingRepository.save(mappedHearingEntity);

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

        this.defendantAttendanceRepository.save(defendantAttendance);

        DefendantAttendance entity = defendantAttendanceRepository.findByHearingIdDefendantIdAndDate(hearingId, defendantId, date);

        assertThat(entity.getId().getHearingId(), is(hearingId));
        assertThat(entity.getDefendantId(), is(defendantId));
        assertThat(entity.getDay(), is(date));
        assertThat(entity.getAttendanceType(), is(isInAttendance));
    }

    @Test
    void chd08_findByHearingIdDefendantIdAndDateThrowsNonUniqueResultWhenTwoRowsMatch() {
        final UUID hearingId = savedHearingId();
        final UUID defendantId = randomUUID();
        final LocalDate day = LocalDate.of(2017, 12, 13);
        saveAttendance(hearingId, defendantId, day, AttendanceType.IN_PERSON);
        saveAttendance(hearingId, defendantId, day, AttendanceType.NOT_PRESENT);

        assertThrows(NonUniqueResultException.class,
                () -> defendantAttendanceRepository.findByHearingIdDefendantIdAndDate(hearingId, defendantId, day));
    }

    @Test
    void chd08_findByHearingIdDefendantIdAndDateReturnsNullWhenNothingMatches() {
        assertThat(defendantAttendanceRepository.findByHearingIdDefendantIdAndDate(savedHearingId(), randomUUID(), LocalDate.of(2017, 12, 13)),
                is(nullValue()));
    }

    @Test
    void chd11_findByHearingIdDefendantIdAndDateMatchesOnlyTheRequestedDay() {
        final UUID hearingId = savedHearingId();
        final UUID defendantId = randomUUID();
        saveAttendance(hearingId, defendantId, LocalDate.of(2017, 12, 13), AttendanceType.IN_PERSON);
        saveAttendance(hearingId, defendantId, LocalDate.of(2017, 12, 14), AttendanceType.NOT_PRESENT);

        final DefendantAttendance found = defendantAttendanceRepository.findByHearingIdDefendantIdAndDate(hearingId, defendantId, LocalDate.of(2017, 12, 14));

        assertThat(found.getAttendanceType(), is(AttendanceType.NOT_PRESENT));
    }

    // ── CHD harness ──────────────────────────────────────────────────────────────────────────────────────────────

    private UUID savedHearingId() {
        final Hearing hearingEntity = new Hearing();
        hearingEntity.setId(minimumInitiateHearingTemplate().getHearing().getId());
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
