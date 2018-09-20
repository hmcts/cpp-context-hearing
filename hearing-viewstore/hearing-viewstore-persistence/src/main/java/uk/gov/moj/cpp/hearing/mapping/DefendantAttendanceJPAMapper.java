package uk.gov.moj.cpp.hearing.mapping;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantAttendance;
import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class DefendantAttendanceJPAMapper {

    private DefendantAttendance toJPA(final uk.gov.justice.json.schemas.core.DefendantAttendance pojo) {
        if (null == pojo) {
            return null;
        }
        return new DefendantAttendance();
    }

    public Set<DefendantAttendance> toJPA(List<uk.gov.justice.json.schemas.core.DefendantAttendance> pojos) {
        if (null == pojos) {
            return new HashSet<>();
        }
        return pojos.stream().map(this::toJPA).collect(Collectors.toSet());
    }

    public List<uk.gov.justice.json.schemas.core.DefendantAttendance> fromJPA(Set<DefendantAttendance> entities) {
        if (null == entities) {
            return new ArrayList<>();
        }
        return entities.stream()
                .collect(groupingBy(DefendantAttendance::getDefendantId)).entrySet().stream()
                .map(da -> createDefendantAttendance(da.getKey(), da.getValue()))
                .collect(Collectors.toList());
    }

    private uk.gov.justice.json.schemas.core.DefendantAttendance createDefendantAttendance(UUID key, List<DefendantAttendance> value) {
        return uk.gov.justice.json.schemas.core.DefendantAttendance.defendantAttendance()
                .withDefendantId(key)
                .withAttendanceDays(value.stream()
                        .map(this::createAttendanceDays)
                        .collect(toList()))
                .build();
    }

    private uk.gov.justice.json.schemas.core.AttendanceDay createAttendanceDays(DefendantAttendance v) {
        return uk.gov.justice.json.schemas.core.AttendanceDay.attendanceDay()
                .withDay(v.getDay())
                .withIsInAttendance(v.getInAttendance())
                .build();
    }
}
