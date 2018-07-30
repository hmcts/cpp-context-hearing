package uk.gov.moj.cpp.hearing.event.nows.activiti.task;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.moj.cpp.hearing.activiti.common.ProcessMapConstant.HEARING_ID;
import static uk.gov.moj.cpp.hearing.activiti.common.ProcessMapConstant.MATERIAL_ID;
import static uk.gov.moj.cpp.hearing.activiti.common.ProcessMapConstant.USER_ID;
import static uk.gov.moj.cpp.hearing.event.nows.activiti.worlflow.materialupload.task.NowsMaterialStatusUpdateHmps.RESULTINGHMPS_UPDATE_NOWS_MATERIAL_STATUS;

import java.util.UUID;

import javax.json.JsonObject;

import org.activiti.engine.delegate.DelegateExecution;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nows.activiti.worlflow.materialupload.task.NowsMaterialStatusUpdateHmps;


@RunWith(MockitoJUnitRunner.class)
public class NowsMaterialStatusUpdateHmpsTest {

    @InjectMocks
    private NowsMaterialStatusUpdateHmps nowsMaterialStatusUpdateHmps;

    @Mock
    private Sender sender;

    @Mock
    private DelegateExecution delegateExecution;


    @Mock
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Mock
    private JsonObject jsonObject;

    @Test
    public void shouldInitiateHearingForCaseRequestUsingSender() throws Exception {
        //Given
        final UUID userId = UUID.randomUUID();
        final UUID materialId = UUID.randomUUID();
        final UUID hearingId = UUID.randomUUID();

        when(delegateExecution.getVariable(MATERIAL_ID, String.class))
                        .thenReturn(materialId.toString());
        when(delegateExecution.getVariable(HEARING_ID, UUID.class)).thenReturn(hearingId);
        when(delegateExecution.getVariable(USER_ID, UUID.class)).thenReturn(userId);

        //when
        nowsMaterialStatusUpdateHmps.execute(delegateExecution);


        final ArgumentCaptor<JsonEnvelope> senderJsonEnvelopeCaptor =
                ArgumentCaptor.forClass(JsonEnvelope.class);
        verify(sender, times(1)).send(senderJsonEnvelopeCaptor.capture());

        final JsonEnvelope envelope = senderJsonEnvelopeCaptor.getValue();
        assertThat(envelope.payloadAsJsonObject().getString(MATERIAL_ID), equalTo(materialId.toString()));
        assertThat(envelope.payloadAsJsonObject().getString(HEARING_ID), equalTo(hearingId.toString()));

        assertThat(envelope.metadata().userId().get(), equalTo(userId.toString()));
        assertThat(envelope.metadata().name(),
                equalTo(RESULTINGHMPS_UPDATE_NOWS_MATERIAL_STATUS));

    }
}
