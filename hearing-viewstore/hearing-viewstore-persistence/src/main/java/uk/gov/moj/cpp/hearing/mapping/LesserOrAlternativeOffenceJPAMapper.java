package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.moj.cpp.hearing.persist.entity.ha.LesserOrAlternativeOffence;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LesserOrAlternativeOffenceJPAMapper {

    public LesserOrAlternativeOffence toJPA(final uk.gov.justice.core.courts.LesserOrAlternativeOffence pojo) {
        if (null == pojo) {
            return null;
        }

        return getLesserOrAlternativeOffence(pojo);
    }

    public uk.gov.justice.core.courts.LesserOrAlternativeOffence fromJPA(final LesserOrAlternativeOffence entity) {
        if (null == entity) {
            return null;
        }

        return getLesserOrAlternativeOffence(entity);
    }

    private uk.gov.justice.core.courts.LesserOrAlternativeOffence getLesserOrAlternativeOffence(LesserOrAlternativeOffence entity) {
        return uk.gov.justice.core.courts.LesserOrAlternativeOffence.lesserOrAlternativeOffence()
                .withOffenceLegislation(entity.getLesserOffenceLegislation())
                .withOffenceLegislationWelsh(entity.getLesserOffenceLegislationWelsh())
                .withOffenceDefinitionId(entity.getLesserOffenceDefinitionId())
                .withOffenceCode(entity.getLesserOffenceCode())
                .withOffenceTitle(entity.getLesserOffenceTitle())
                .withOffenceTitleWelsh(entity.getLesserOffenceTitleWelsh())
                .build();
    }

    private LesserOrAlternativeOffence getLesserOrAlternativeOffence(uk.gov.justice.core.courts.LesserOrAlternativeOffence pojo) {
        final LesserOrAlternativeOffence lesserOrAlternativeOffence = new LesserOrAlternativeOffence();
        lesserOrAlternativeOffence.setLesserOffenceCode(pojo.getOffenceCode());
        lesserOrAlternativeOffence.setLesserOffenceDefinitionId(pojo.getOffenceDefinitionId());
        lesserOrAlternativeOffence.setLesserOffenceLegislation(pojo.getOffenceLegislation());
        lesserOrAlternativeOffence.setLesserOffenceLegislationWelsh(pojo.getOffenceLegislationWelsh());
        lesserOrAlternativeOffence.setLesserOffenceTitle(pojo.getOffenceTitle());
        lesserOrAlternativeOffence.setLesserOffenceTitleWelsh(pojo.getOffenceTitleWelsh());
        return lesserOrAlternativeOffence;
    }
}