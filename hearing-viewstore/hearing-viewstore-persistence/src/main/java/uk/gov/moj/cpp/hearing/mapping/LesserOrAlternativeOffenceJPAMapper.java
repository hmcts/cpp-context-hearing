package uk.gov.moj.cpp.hearing.mapping;

import javax.enterprise.context.ApplicationScoped;

import uk.gov.moj.cpp.hearing.persist.entity.ha.LesserOrAlternativeOffence;

@ApplicationScoped
public class LesserOrAlternativeOffenceJPAMapper {

    public LesserOrAlternativeOffence toJPA(final uk.gov.justice.json.schemas.core.LesserOrAlternativeOffence pojo) {
        if (null == pojo) {
            return null;
        }
        final LesserOrAlternativeOffence lesserOrAlternativeOffence = new LesserOrAlternativeOffence();

        lesserOrAlternativeOffence.setLesserOffenceCode(pojo.getOffenceCode());
        lesserOrAlternativeOffence.setLesserOffenceDefinitionId(pojo.getOffenceDefinitionId());
        lesserOrAlternativeOffence.setLesserOffenceLegislation(pojo.getLegislation());
        lesserOrAlternativeOffence.setLesserOffenceTitle(pojo.getDescription());

        return lesserOrAlternativeOffence;
    }

    public uk.gov.justice.json.schemas.core.LesserOrAlternativeOffence fromJPA(final LesserOrAlternativeOffence entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.json.schemas.core.LesserOrAlternativeOffence.lesserOrAlternativeOffence()
                .withLegislation(entity.getLesserOffenceLegislation())
                .withOffenceDefinitionId(entity.getLesserOffenceDefinitionId())
                .withOffenceCode(entity.getLesserOffenceCode())
                .withDescription(entity.getLesserOffenceTitle())
                .build();
    }
}