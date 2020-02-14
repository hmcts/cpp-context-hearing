package uk.gov.moj.cpp.hearing.mapping;

import static java.util.Objects.isNull;

import uk.gov.moj.cpp.hearing.persist.entity.ha.LaaApplnReference;

public class LaaApplnReferenceJPAMapper {
    public LaaApplnReferenceJPAMapper() {
        //for cdi tester
    }

    public LaaApplnReference toJpa(final uk.gov.justice.core.courts.LaaReference courtsApplnLaaReference) {
        if (isNull(courtsApplnLaaReference)) {
            return null;
        }
        final LaaApplnReference laaApplnReference = new LaaApplnReference();
        laaApplnReference.setApplicationReference(courtsApplnLaaReference.getApplicationReference());
        laaApplnReference.setEffectiveEndDate(courtsApplnLaaReference.getEffectiveEndDate());
        laaApplnReference.setEffectiveStartDate(courtsApplnLaaReference.getEffectiveStartDate());
        laaApplnReference.setStatusCode(courtsApplnLaaReference.getStatusCode());
        laaApplnReference.setStatusDate(courtsApplnLaaReference.getStatusDate());
        laaApplnReference.setStatusDescription(courtsApplnLaaReference.getStatusDescription());
        laaApplnReference.setStatusId(courtsApplnLaaReference.getStatusId());
        return laaApplnReference;
    }

    public uk.gov.justice.core.courts.LaaReference fromJpa(final LaaApplnReference entityLaaApplnReference){
        if(isNull(entityLaaApplnReference)){
            return null;
        }
        return uk.gov.justice.core.courts.LaaReference.laaReference()
                .withApplicationReference(entityLaaApplnReference.getApplicationReference())
                .withEffectiveEndDate(entityLaaApplnReference.getEffectiveEndDate())
                .withEffectiveStartDate(entityLaaApplnReference.getEffectiveStartDate())
                .withStatusCode(entityLaaApplnReference.getStatusCode())
                .withStatusDescription(entityLaaApplnReference.getStatusDescription())
                .withStatusDate(entityLaaApplnReference.getStatusDate())
                .withStatusId(entityLaaApplnReference.getStatusId())
                .build();
    }
}
