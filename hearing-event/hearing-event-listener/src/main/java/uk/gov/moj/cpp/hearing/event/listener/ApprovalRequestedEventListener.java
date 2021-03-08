package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.core.courts.ApprovalRequest;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.HearingState;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;

@ServiceComponent(EVENT_LISTENER)
public class ApprovalRequestedEventListener {

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private HearingRepository hearingRepository;


    @Transactional
    @Handles("hearing.event.approval-requested")
    public void approvalRequested(final JsonEnvelope envelope) {
        final ApprovalRequest approvalRequest = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), ApprovalRequest.class);
        final Hearing hearing = hearingRepository.findBy(approvalRequest.getHearingId());
        hearing.setHearingState(HearingState.APPROVAL_REQUESTED);
        hearing.setAmendedByUserId(approvalRequest.getUserId());
        hearingRepository.save(hearing);

    }
}