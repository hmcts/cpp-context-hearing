package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.moj.cpp.hearing.persist.entity.ha.CpsProsecutor;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CpsProsecutorJPAMapper {

    public CpsProsecutor toJPA(final uk.gov.justice.core.courts.Prosecutor pojo) {
        if (null == pojo) {
            return null;
        }
        final CpsProsecutor cpsProsecutor = new CpsProsecutor();
        cpsProsecutor.setCpsProsecutorId(pojo.getProsecutorId());
        cpsProsecutor.setCpsProsecutorCode(pojo.getProsecutorCode());
        return cpsProsecutor;
    }

    public uk.gov.justice.core.courts.Prosecutor fromJPA(final CpsProsecutor entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.core.courts.Prosecutor.prosecutor()
                .withProsecutorId(entity.getCpsProsecutorId())
                .withProsecutorCode(entity.getCpsProsecutorCode())
                .build();
    }
}