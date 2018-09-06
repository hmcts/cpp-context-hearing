package uk.gov.moj.cpp.hearing.mapping;

import javax.enterprise.context.ApplicationScoped;

import uk.gov.moj.cpp.hearing.persist.entity.ha.OffenceFacts;

@ApplicationScoped
public class OffenceFactsJPAMapper {

    public OffenceFacts toJPA(final uk.gov.justice.json.schemas.core.OffenceFacts pojo) {
        if (null == pojo) {
            return null;
        }
        final OffenceFacts offenceFacts = new OffenceFacts();
        offenceFacts.setAlcoholReadingMethod(pojo.getAlcoholReadingMethod());
        offenceFacts.setAlcoholReadingAmount(pojo.getAlcoholReadingAmount());
        offenceFacts.setVehicleRegistration(pojo.getVehicleRegistration());
        return offenceFacts;
    }

    public uk.gov.justice.json.schemas.core.OffenceFacts fromJPA(final OffenceFacts entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.json.schemas.core.OffenceFacts.offenceFacts()
                .withAlcoholReadingAmount(entity.getAlcoholReadingAmount())
                .withAlcoholReadingMethod(entity.getAlcoholReadingMethod())
                .withVehicleRegistration(entity.getVehicleRegistration())
                .build();
    }
}
