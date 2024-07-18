package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Objects.nonNull;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.CasesUpdatedAfterCaseRemovedFromGroupCases;
import uk.gov.moj.cpp.hearing.mapping.ProsecutionCaseJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.repository.ProsecutionCaseRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;

@ServiceComponent(EVENT_LISTENER)
public class CaseRemovedFromGroupCasesEventListener {

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private ProsecutionCaseRepository prosecutionCaseRepository;

    @Inject
    private ProsecutionCaseJPAMapper prosecutionCaseJPAMapper;

    @Transactional
    @Handles("hearing.events.cases-updated-after-case-removed-from-group-cases")
    public void casesUpdatedAfterCaseRemovedFromGroupCases(final JsonEnvelope envelope) {
        final CasesUpdatedAfterCaseRemovedFromGroupCases casesUpdated =
                jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), CasesUpdatedAfterCaseRemovedFromGroupCases.class);

        final Hearing hearingEntity = hearingRepository.findBy(casesUpdated.getHearingId());

        final ProsecutionCase removedCaseEntity = prosecutionCaseJPAMapper.toJPA(hearingEntity, casesUpdated.getRemovedCase());
        prosecutionCaseRepository.save(removedCaseEntity);

        if (nonNull(casesUpdated.getNewGroupMaster())) {
            final ProsecutionCase newGroupMasterEntity = prosecutionCaseJPAMapper.toJPA(hearingEntity, casesUpdated.getNewGroupMaster());
            prosecutionCaseRepository.save(newGroupMasterEntity);
        }
    }
}