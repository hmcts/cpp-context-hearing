package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.moj.cpp.hearing.persist.entity.ha.DefenceCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped // Will be covered by GGPE-5825 story
public class DefenceCounselJPAMapper {

    private DefenceCounsel toJPA(final Hearing hearing, final uk.gov.justice.core.courts.DefenceCounsel pojo) {
        if (null == pojo) {
            return null;
        }
        final DefenceCounsel defenceCounsel = new DefenceCounsel();
        defenceCounsel.setHearingId(hearing.getId());
        return defenceCounsel;
    }

    private uk.gov.justice.core.courts.DefenceCounsel fromJPA(final DefenceCounsel entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.core.courts.DefenceCounsel.defenceCounsel()
                .build();
    }

    public Set<DefenceCounsel> toJPA(Hearing hearing, List<uk.gov.justice.core.courts.DefenceCounsel> pojos) {
        if (null == pojos) {
            return new HashSet<>();
        }
        return pojos.stream().map(pojo -> toJPA(hearing, pojo)).collect(Collectors.toSet());
    }

    public List<uk.gov.justice.core.courts.DefenceCounsel> fromJPA(Set<DefenceCounsel> entities) {
        if (null == entities) {
            return new ArrayList<>();
        }
        return entities.stream().map(this::fromJPA).collect(Collectors.toList());
    }
}