package uk.gov.moj.cpp.hearing.mapping;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EthnicityJPAMapper {

    public uk.gov.moj.cpp.hearing.persist.entity.ha.Ethnicity toJPA(final uk.gov.justice.core.courts.Ethnicity pojo) {
        if (null == pojo) {
            return null;
        }
        final uk.gov.moj.cpp.hearing.persist.entity.ha.Ethnicity ethnicity = new uk.gov.moj.cpp.hearing.persist.entity.ha.Ethnicity();

        ethnicity.setObservedEthnicityCode(pojo.getObservedEthnicityCode());
        ethnicity.setObservedEthnicityId(pojo.getObservedEthnicityId());
        ethnicity.setObservedEthnicityDescription(pojo.getObservedEthnicityDescription());
        ethnicity.setSelfDefinedEthnicityCode(pojo.getSelfDefinedEthnicityCode());
        ethnicity.setSelfDefinedEthnicityId(pojo.getSelfDefinedEthnicityId());
        ethnicity.setSelfDefinedEthnicityDescription(pojo.getSelfDefinedEthnicityDescription());

        return ethnicity;
    }

    public uk.gov.justice.core.courts.Ethnicity fromJPA(final uk.gov.moj.cpp.hearing.persist.entity.ha.Ethnicity entity) {
        if (null == entity) {
            return null;
        }

        return uk.gov.justice.core.courts.Ethnicity.ethnicity()
                .withObservedEthnicityId(entity.getObservedEthnicityId())
                .withObservedEthnicityCode(entity.getObservedEthnicityCode())
                .withObservedEthnicityDescription(entity.getObservedEthnicityDescription())
                .withSelfDefinedEthnicityCode(entity.getSelfDefinedEthnicityCode())
                .withSelfDefinedEthnicityDescription(entity.getSelfDefinedEthnicityDescription())
                .withSelfDefinedEthnicityId(entity.getSelfDefinedEthnicityId()).build();

    }
}