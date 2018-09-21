package uk.gov.moj.cpp.hearing.mapping;

import javax.enterprise.context.ApplicationScoped;

import uk.gov.moj.cpp.hearing.persist.entity.ha.LesserOrAlternativeOffence;

@ApplicationScoped
public class LesserOrAlternativeOffenceJPAMapper {

    public LesserOrAlternativeOffence toJPA(final uk.gov.justice.json.schemas.core.LesserOrAlternativeOffence pojo) {
        if (null == pojo) {
            return null;
        }

        return getLesserOrAlternativeOffence(pojo);
    }

    public uk.gov.justice.json.schemas.core.LesserOrAlternativeOffence fromJPA(final LesserOrAlternativeOffence entity) {
        if (null == entity) {
            return null;
        }

        return getLesserOrAlternativeOffence(entity);
    }

    private uk.gov.justice.json.schemas.core.LesserOrAlternativeOffence getLesserOrAlternativeOffence(LesserOrAlternativeOffence entity) {
        return uk.gov.justice.json.schemas.core.LesserOrAlternativeOffence.lesserOrAlternativeOffence()
                .withOffenceLegislation(entity.getLesserOffenceLegislation())
                .withOffenceLegislationWelsh(entity.getLesserOffenceLegislationWelsh())
                .withOffenceDefinitionId(entity.getLesserOffenceDefinitionId())
                .withOffenceCode(entity.getLesserOffenceCode())
                .withOffenceTitle(entity.getLesserOffenceTitle())
                .withOffenceTitleWelsh(entity.getLesserOffenceTitleWelsh())
                .build();
    }

    private LesserOrAlternativeOffence getLesserOrAlternativeOffence(uk.gov.justice.json.schemas.core.LesserOrAlternativeOffence pojo) {
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