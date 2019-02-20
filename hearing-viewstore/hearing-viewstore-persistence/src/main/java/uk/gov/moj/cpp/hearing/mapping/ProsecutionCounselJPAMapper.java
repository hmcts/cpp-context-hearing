package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCounsel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped // Will be covered by GGPE-5825 story
public class ProsecutionCounselJPAMapper {

    private ProsecutionCounsel toJPA(final Hearing hearing, final uk.gov.justice.core.courts.ProsecutionCounsel pojo) {
        if (null == pojo) {
            return null;
        }
        final ProsecutionCounsel prosecutionCounsel = new ProsecutionCounsel();
        prosecutionCounsel.setHearingId(hearing.getId());
        return prosecutionCounsel;
    }

    private uk.gov.justice.core.courts.ProsecutionCounsel fromJPA(final ProsecutionCounsel entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.core.courts.ProsecutionCounsel.prosecutionCounsel()
                .build();
    }

    public Set<ProsecutionCounsel> toJPA(Hearing hearing, List<uk.gov.justice.core.courts.ProsecutionCounsel> pojos) {
        if (null == pojos) {
            return new HashSet<>();
        }
        return pojos.stream().map(pojo -> toJPA(hearing, pojo)).collect(Collectors.toSet());
    }

    public List<uk.gov.justice.core.courts.ProsecutionCounsel> fromJPA(Set<ProsecutionCounsel> entities) {
        if (null == entities) {
            return new ArrayList<>();
        }
        return entities.stream().map(this::fromJPA).collect(Collectors.toList());
    }
}