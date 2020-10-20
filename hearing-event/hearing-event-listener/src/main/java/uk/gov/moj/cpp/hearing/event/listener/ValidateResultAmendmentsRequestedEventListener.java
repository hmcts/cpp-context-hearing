package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.ValidateResultAmendmentsRequested;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ApprovalRequested;
import uk.gov.moj.cpp.hearing.repository.ApprovalRequestedRepository;

import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;

@ServiceComponent(EVENT_LISTENER)
public class ValidateResultAmendmentsRequestedEventListener {

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private ApprovalRequestedRepository approvalRequestedRepository;

    @Transactional
    @Handles("hearing.event.validate-result-amendments-requested")
    public void validateResultAmendmentsRequested(final JsonEnvelope envelope) {
        final ValidateResultAmendmentsRequested validateResultAmendmentsRequested = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), ValidateResultAmendmentsRequested.class);

        final List<ApprovalRequested> approvalsRequested = approvalRequestedRepository.findApprovalsRequestByHearingId(validateResultAmendmentsRequested.getHearingId());
        if (!approvalsRequested.isEmpty()) {
            approvalRequestedRepository.removeAllRequestApprovals(approvalsRequested.get(0).getHearingId());
        }
    }
}