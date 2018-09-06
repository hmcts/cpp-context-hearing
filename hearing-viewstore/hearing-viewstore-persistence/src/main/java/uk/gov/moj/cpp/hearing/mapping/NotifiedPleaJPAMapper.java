package uk.gov.moj.cpp.hearing.mapping;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import uk.gov.moj.cpp.hearing.persist.entity.ha.NotifiedPlea;

@ApplicationScoped
public class NotifiedPleaJPAMapper {

    public NotifiedPlea toJPA(final uk.gov.justice.json.schemas.core.NotifiedPlea pojo) {
        if (null == pojo) {
            return null;
        }
        final NotifiedPlea notifiedPlea = new NotifiedPlea();
        notifiedPlea.setNotifiedPleaDate(pojo.getNotifiedPleaDate());
        notifiedPlea.setNotifiedPleaValue(pojo.getNotifiedPleaValue());
        return notifiedPlea;
    }

    public uk.gov.justice.json.schemas.core.NotifiedPlea fromJPA(final UUID offenceId, final NotifiedPlea entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.json.schemas.core.NotifiedPlea.notifiedPlea()
                .withNotifiedPleaDate(entity.getNotifiedPleaDate())
                .withNotifiedPleaValue(entity.getNotifiedPleaValue())
                .withOffenceId(offenceId)
                .build();
    }
}