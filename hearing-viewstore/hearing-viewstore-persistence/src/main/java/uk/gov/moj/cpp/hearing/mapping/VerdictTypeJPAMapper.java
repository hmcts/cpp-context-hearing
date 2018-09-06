package uk.gov.moj.cpp.hearing.mapping;

import javax.enterprise.context.ApplicationScoped;

import uk.gov.moj.cpp.hearing.persist.entity.ha.VerdictType;

@ApplicationScoped
public class VerdictTypeJPAMapper {

    public VerdictType toJPA(final uk.gov.justice.json.schemas.core.VerdictType pojo) {
        if (null == pojo) {
            return null;
        }
        final VerdictType verdictType = new VerdictType();
        verdictType.setVerdictCategory(pojo.getCategory());
        verdictType.setVerdictCategoryType(pojo.getCategoryType());
        verdictType.setVerdictTypeId(pojo.getVerdictTypeId());
        return verdictType;
    }

    public uk.gov.justice.json.schemas.core.VerdictType fromJPA(final VerdictType entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.json.schemas.core.VerdictType.verdictType()
                .withCategory(entity.getVerdictCategory())
                .withCategoryType(entity.getVerdictCategoryType())
                .withVerdictTypeId(entity.getVerdictTypeId())
                .build();
    }
}