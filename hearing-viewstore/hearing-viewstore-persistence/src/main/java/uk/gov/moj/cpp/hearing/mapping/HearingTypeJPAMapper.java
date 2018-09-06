package uk.gov.moj.cpp.hearing.mapping;

import javax.enterprise.context.ApplicationScoped;

import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingType;

@ApplicationScoped
public class HearingTypeJPAMapper {

    public HearingType toJPA(final uk.gov.justice.json.schemas.core.HearingType pojo) {
        if (null == pojo) {
            return null;
        }
        final HearingType hearingType = new HearingType();
        hearingType.setId(pojo.getId());
        hearingType.setDescription(pojo.getDescription());
        return hearingType;
    }

    public uk.gov.justice.json.schemas.core.HearingType fromJPA(final HearingType entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.json.schemas.core.HearingType.hearingType()
                .withId(entity.getId())
                .withDescription(entity.getDescription())
                .build();
    }
}