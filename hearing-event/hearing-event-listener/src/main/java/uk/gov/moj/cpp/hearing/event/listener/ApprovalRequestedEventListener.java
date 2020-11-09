package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.core.courts.ApprovalType.CHANGE;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.core.courts.ApprovalRequest;
import uk.gov.justice.core.courts.ApprovalType;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.mapping.ApprovalRequestedJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ApprovalRequested;
import uk.gov.moj.cpp.hearing.repository.ApprovalRequestedRepository;

import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;

@ServiceComponent(EVENT_LISTENER)
public class ApprovalRequestedEventListener {

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private ApprovalRequestedRepository approvalRequestedRepository;

    @Inject
    private ApprovalRequestedJPAMapper approvalRequestJPAMapper;


    @Transactional
    @Handles("hearing.event.approval-requested")
    public void approvalRequested(final JsonEnvelope envelope) {
        final ApprovalRequest convert = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), ApprovalRequest.class);
        final uk.gov.moj.cpp.hearing.persist.entity.ha.ApprovalRequested approvalRequested = approvalRequestJPAMapper.toJPA(convert);
        final List<ApprovalRequested> approvalsRequested = approvalRequestedRepository.findApprovalsRequestByHearingId(approvalRequested.getHearingId());
        if (approvalRequested.getApprovalType().equals(ApprovalType.APPROVAL)) {
            if (!approvalsRequested.isEmpty()) {
                approvalRequestedRepository.removeAllRequestApprovalsForApprovalType(approvalsRequested.get(0).getHearingId(), CHANGE);
            }
        } else {
            approvalsRequested.stream().filter(x -> x.getUserId().equals(convert.getUserId())).forEach(x -> approvalRequestedRepository.remove(x));

        }
        approvalRequestedRepository.save(approvalRequested);
    }
}