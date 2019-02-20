package uk.gov.moj.cpp.hearing.mapping;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantAttendance;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;

public class DefendantAttendanceJPAMapperTest {

    private DefendantAttendanceJPAMapper mapper = new DefendantAttendanceJPAMapper();

    @Test
    public void testFromJPA() {

        final DefendantAttendance defendantAttendanceEntity1 = createDefendantAttendance(UUID.randomUUID(), UUID.randomUUID(), UUID.fromString("e830fa5a-0a90-4f4a-a68c-3e782d16832c"), LocalDate.of(2018, 8, 9), true);
        Set<DefendantAttendance> defendantAttendanceEntities = new HashSet<>();
        defendantAttendanceEntities.add(defendantAttendanceEntity1);

        List<uk.gov.justice.core.courts.DefendantAttendance> defendantAttendances = mapper.fromJPA(defendantAttendanceEntities);

        assertThat(defendantAttendances.get(0).getDefendantId(), is(defendantAttendanceEntity1.getDefendantId()));
        assertThat(defendantAttendances.get(0).getAttendanceDays().get(0).getDay(), is(defendantAttendanceEntity1.getDay()));
        assertThat(defendantAttendances.get(0).getAttendanceDays().get(0).getIsInAttendance(), is(defendantAttendanceEntity1.getInAttendance()));
    }

    private DefendantAttendance createDefendantAttendance(final UUID id, final UUID hearingId, final UUID defendantId, LocalDate day, boolean attendance) {
        DefendantAttendance defendantAttendance = new DefendantAttendance();
        defendantAttendance.setId(new HearingSnapshotKey(id, hearingId));
        defendantAttendance.setDefendantId(defendantId);
        defendantAttendance.setDay(day);
        defendantAttendance.setInAttendance(attendance);
        return defendantAttendance;
    }
}
