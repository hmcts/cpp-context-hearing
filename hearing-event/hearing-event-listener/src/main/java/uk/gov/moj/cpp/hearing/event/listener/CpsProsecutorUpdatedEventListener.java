package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.CpsProsecutorUpdated;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier;
import uk.gov.moj.cpp.hearing.repository.ProsecutionCaseRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;

@ServiceComponent(EVENT_LISTENER)
public class CpsProsecutorUpdatedEventListener {


    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private ProsecutionCaseRepository prosecutionCaseRepository;

    @Transactional
    @Handles("hearing.cps-prosecutor-updated")
    public void cpsProsecutorUpdated(final JsonEnvelope envelope) {

        final CpsProsecutorUpdated cpsProsecutorUpdated = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), CpsProsecutorUpdated.class);
        final ProsecutionCase prosecutionCase = prosecutionCaseRepository.findBy(new HearingSnapshotKey(cpsProsecutorUpdated.getProsecutionCaseId(), cpsProsecutorUpdated.getHearingId()));
        final ProsecutionCaseIdentifier prosecutionCaseIdentifier =  prosecutionCase.getProsecutionCaseIdentifier();
        prosecutionCaseIdentifier.setProsecutionAuthorityReference(cpsProsecutorUpdated.getProsecutionAuthorityReference());
        prosecutionCaseIdentifier.setProsecutionAuthorityCode(cpsProsecutorUpdated.getProsecutionAuthorityCode());
        prosecutionCaseIdentifier.setProsecutionAuthorityId(cpsProsecutorUpdated.getProsecutionAuthorityId());
        prosecutionCaseIdentifier.setCaseURN(cpsProsecutorUpdated.getCaseURN());
        prosecutionCaseRepository.save(prosecutionCase);
    }
}
