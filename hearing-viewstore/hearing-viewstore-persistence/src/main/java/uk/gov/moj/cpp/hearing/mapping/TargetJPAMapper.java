package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class TargetJPAMapper {

    private ResultLineJPAMapper resultLineJPAMapper;

    @Inject
    public TargetJPAMapper(ResultLineJPAMapper resultLineJPAMapper) {
        this.resultLineJPAMapper = resultLineJPAMapper;
    }

    //to satisfy CDI test runner
    public TargetJPAMapper() {
    }

    public Target toJPA(final Hearing hearing, final uk.gov.justice.core.courts.Target pojo) {
        if (null == pojo) {
            return null;
        }
        final Target target = new Target();
        target.setId(pojo.getTargetId());
        target.setHearing(hearing);
        target.setDefendantId(pojo.getDefendantId());
        target.setDraftResult(pojo.getDraftResult());
        target.setOffenceId(pojo.getOffenceId());
        target.setApplicationId(pojo.getApplicationId());
        target.setResultLines(resultLineJPAMapper.toJPA(target, pojo.getResultLines()));
        return target;
    }

    public uk.gov.justice.core.courts.Target fromJPA(final Target entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.core.courts.Target.target()
                .withDefendantId(entity.getDefendantId())
                .withDraftResult(entity.getDraftResult())
                .withHearingId(entity.getHearing().getId())
                .withOffenceId(entity.getOffenceId())
                .withApplicationId(entity.getApplicationId())
                .withTargetId(entity.getId())
                .withResultLines(resultLineJPAMapper.fromJPA(entity.getResultLines()))
                .build();
    }

    public Set<Target> toJPA(Hearing hearing, List<uk.gov.justice.core.courts.Target> pojos) {
        if (null == pojos) {
            return new HashSet<>();
        }
        return pojos.stream().map(pojo -> toJPA(hearing, pojo)).collect(Collectors.toSet());
    }

    public List<uk.gov.justice.core.courts.Target> fromJPA(Set<Target> entities) {
        if (null == entities) {
            return new ArrayList<>();
        }
        return entities.stream().map(this::fromJPA).collect(Collectors.toList());
    }
}