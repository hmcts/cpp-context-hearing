package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Objects.isNull;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.CaseMarkersUpdated;
import uk.gov.moj.cpp.hearing.mapping.CaseMarkerJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.repository.ProsecutionCaseRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;

@ServiceComponent(EVENT_LISTENER)
public class CaseMarkerEventListener {

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private CaseMarkerJPAMapper caseMarkerJPAMapper;

    @Inject
    private ProsecutionCaseRepository prosecutionCaseRepository;

    @Inject
    private HearingRepository hearingRepository;

    @Transactional
    @Handles("hearing.events.case-markers-updated")
    public void caseMarkersUpdated(final JsonEnvelope envelope) {
        final CaseMarkersUpdated caseMarkersUpdated = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), CaseMarkersUpdated.class);

        if(isNull(hearingRepository.findBy(caseMarkersUpdated.getHearingId()))){
            return;
        }
        final ProsecutionCase prosecutionCase = prosecutionCaseRepository.findBy(new HearingSnapshotKey(caseMarkersUpdated.getProsecutionCaseId(), caseMarkersUpdated.getHearingId()));
        prosecutionCase.getMarkers().clear();
        if (null != caseMarkersUpdated.getCaseMarkers() && !caseMarkersUpdated.getCaseMarkers().isEmpty()) {
            prosecutionCase.getMarkers().addAll(caseMarkerJPAMapper.toJPA(caseMarkersUpdated.getHearingId(), prosecutionCase, caseMarkersUpdated.getCaseMarkers()));
        }
        prosecutionCaseRepository.save(prosecutionCase);
    }
}