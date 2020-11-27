package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.core.courts.ApprovalType.CHANGE;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultAmendmentsValidated;
import uk.gov.moj.cpp.hearing.repository.ApprovalRequestedRepository;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ValidateResultAmendmentsRequestedEventListenerTest {
    @Mock
    private ApprovalRequestedRepository approvalRequestedRepository;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter = new JsonObjectConvertersFactory().objectToJsonObjectConverter();

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();

    @InjectMocks
    private ResultAmendmentsValidatedEventListener validateResultAmendmentsRequestedEventListener;

    @Test
    public void shouldRecordSessionTime() {
        final UUID hearingId = randomUUID();
        final UUID userId = randomUUID();
        final ZonedDateTime validateResultAmendmentsTime = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC"));

        final ResultAmendmentsValidated validateResultAmendmentsRequested = new ResultAmendmentsValidated(hearingId, userId, validateResultAmendmentsTime);
        when(approvalRequestedRepository.findApprovalsRequestByHearingId(hearingId)).thenReturn(getApprovalRequesteds(hearingId, userId, validateResultAmendmentsTime));

        validateResultAmendmentsRequestedEventListener.resultAmendmentsValidated(createJsonEnvelope(validateResultAmendmentsRequested));

        verify(approvalRequestedRepository).removeAllRequestApprovals(hearingId);
    }

    private List<uk.gov.moj.cpp.hearing.persist.entity.ha.ApprovalRequested> getApprovalRequesteds(final UUID hearingId, final UUID userId, final ZonedDateTime approvalsRequestedTime) {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.ApprovalRequested approvalRequested = new uk.gov.moj.cpp.hearing.persist.entity.ha.ApprovalRequested(randomUUID(), hearingId, userId, approvalsRequestedTime, CHANGE);
        final List<uk.gov.moj.cpp.hearing.persist.entity.ha.ApprovalRequested> approvalsRequested = new ArrayList();
        approvalsRequested.add(approvalRequested);
        return approvalsRequested;
    }

    private JsonEnvelope createJsonEnvelope(final ResultAmendmentsValidated validateResultAmendmentsRequested) {
        final JsonObject jsonObject = objectToJsonObjectConverter.convert(validateResultAmendmentsRequested);
        return envelopeFrom((Metadata) null, jsonObject);
    }
}