package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantAttendance;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped //TODO Will be covered by GPE-5565 story
public class DefendantAttendanceJPAMapper {

    private DefendantAttendance toJPA(final Hearing hearing, final uk.gov.justice.json.schemas.core.DefendantAttendance pojo) {
        if (null == pojo) {
            return null;
        }
        final DefendantAttendance defendantAttendance = new DefendantAttendance();
        defendantAttendance.setHearingId(hearing.getId());
        return defendantAttendance;
    }

    private uk.gov.justice.json.schemas.core.DefendantAttendance fromJPA(final DefendantAttendance entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.json.schemas.core.DefendantAttendance.defendantAttendance()
                .build();
    }

    public List<DefendantAttendance> toJPA(Hearing hearing,
                                           List<uk.gov.justice.json.schemas.core.DefendantAttendance> pojos) {
        if (null == pojos) {
            return new ArrayList<>();
        }
        return pojos.stream().map(pojo -> toJPA(hearing, pojo)).collect(Collectors.toList());
    }

    public List<uk.gov.justice.json.schemas.core.DefendantAttendance> fromJPA(
            List<DefendantAttendance> entities) {
        if (null == entities) {
            return new ArrayList<>();
        }
        return entities.stream().map(this::fromJPA).collect(Collectors.toList());
    }
}
