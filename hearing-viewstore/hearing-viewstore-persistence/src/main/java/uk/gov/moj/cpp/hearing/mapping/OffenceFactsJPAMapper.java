package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.moj.cpp.hearing.persist.entity.ha.OffenceFacts;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OffenceFactsJPAMapper {

    public OffenceFacts toJPA(final uk.gov.justice.core.courts.OffenceFacts pojo) {
        if (null == pojo) {
            return null;
        }
        final OffenceFacts offenceFacts = new OffenceFacts();
        offenceFacts.setAlcoholReadingMethodCode(pojo.getAlcoholReadingMethodCode());
        offenceFacts.setAlcoholReadingAmount(Optional.ofNullable(pojo.getAlcoholReadingAmount()).orElse(0).toString());
        offenceFacts.setVehicleRegistration(pojo.getVehicleRegistration());
        return offenceFacts;
    }

    public uk.gov.justice.core.courts.OffenceFacts fromJPA(final OffenceFacts entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.core.courts.OffenceFacts.offenceFacts()
                .withAlcoholReadingAmount(parseAlcoholReadingAmount(entity.getAlcoholReadingAmount()))
                .withAlcoholReadingMethodCode(entity.getAlcoholReadingMethodCode())
                .withVehicleRegistration(entity.getVehicleRegistration())
                .build();
    }

    public Integer parseAlcoholReadingAmount(final String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException pex) {
            return 0;
        }
    }

}
