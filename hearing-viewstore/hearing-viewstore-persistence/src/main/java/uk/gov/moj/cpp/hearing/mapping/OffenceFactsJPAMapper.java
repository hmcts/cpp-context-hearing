package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.moj.cpp.hearing.persist.entity.ha.OffenceFacts;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OffenceFactsJPAMapper {

    public OffenceFacts toJPA(final uk.gov.justice.core.courts.OffenceFacts pojo) {
        if (null == pojo) {
            return null;
        }
        final OffenceFacts offenceFacts = new OffenceFacts();
        offenceFacts.setAlcoholReadingMethod(pojo.getAlcoholReadingMethod());
        offenceFacts.setAlcoholReadingAmount(pojo.getAlcoholReadingAmount());
        offenceFacts.setVehicleRegistration(pojo.getVehicleRegistration());
        return offenceFacts;
    }

    public uk.gov.justice.core.courts.OffenceFacts fromJPA(final OffenceFacts entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.core.courts.OffenceFacts.offenceFacts()
                .withAlcoholReadingAmount(entity.getAlcoholReadingAmount())
                .withAlcoholReadingMethod(entity.getAlcoholReadingMethod())
                .withVehicleRegistration(entity.getVehicleRegistration())
                .build();
    }
}
