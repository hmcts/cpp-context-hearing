package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingType;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HearingTypeJPAMapper {

    public HearingType toJPA(final uk.gov.justice.core.courts.HearingType pojo) {
        if (null == pojo) {
            return null;
        }
        final HearingType hearingType = new HearingType();
        hearingType.setId(pojo.getId());
        hearingType.setDescription(pojo.getDescription());
        return hearingType;
    }

    public uk.gov.justice.core.courts.HearingType fromJPA(final HearingType entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.core.courts.HearingType.hearingType()
                .withId(entity.getId())
                .withDescription(entity.getDescription())
                .build();
    }
}