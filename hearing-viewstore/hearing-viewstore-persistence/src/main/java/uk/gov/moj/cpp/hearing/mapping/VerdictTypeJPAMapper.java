package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.moj.cpp.hearing.persist.entity.ha.VerdictType;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class VerdictTypeJPAMapper {

    public VerdictType toJPA(final uk.gov.justice.core.courts.VerdictType pojo) {
        if (null == pojo) {
            return null;
        }
        final VerdictType verdictType = new VerdictType();
        verdictType.setVerdictCategory(pojo.getCategory());
        verdictType.setVerdictCategoryType(pojo.getCategoryType());
        verdictType.setVerdictTypeId(pojo.getId());
        verdictType.setDescription(pojo.getDescription());
        verdictType.setSequence(pojo.getSequence());
        return verdictType;
    }

    public uk.gov.justice.core.courts.VerdictType fromJPA(final VerdictType entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.core.courts.VerdictType.verdictType()
                .withCategory(entity.getVerdictCategory())
                .withCategoryType(entity.getVerdictCategoryType())
                .withId(entity.getVerdictTypeId())
                .withDescription(entity.getDescription())
                .withSequence(entity.getSequence())
                .build();
    }
}