package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.moj.cpp.hearing.persist.entity.ha.IndicatedPlea;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class IndicatedPleaJPAMapper {

    //To keep cdi tester happy
    @SuppressWarnings({"squid:S1186"})
    public IndicatedPleaJPAMapper() { }

    public IndicatedPlea toJPA(final uk.gov.justice.core.courts.IndicatedPlea pojo) {
        if (null == pojo) {
            return null;
        }
        final IndicatedPlea indicatedPlea = new IndicatedPlea();
        indicatedPlea.setIndicatedPleaDate(pojo.getIndicatedPleaDate());
        indicatedPlea.setIndicatedPleaSource(pojo.getSource());
        indicatedPlea.setIndicatedPleaValue(pojo.getIndicatedPleaValue());
        return indicatedPlea;
    }

    public uk.gov.justice.core.courts.IndicatedPlea fromJPA(final UUID offenceId, final IndicatedPlea entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.core.courts.IndicatedPlea.indicatedPlea()
                .withIndicatedPleaDate(entity.getIndicatedPleaDate())
                .withIndicatedPleaValue(entity.getIndicatedPleaValue())
                .withSource(entity.getIndicatedPleaSource())
                .withOffenceId(offenceId)
                .build();
    }
}