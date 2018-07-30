package uk.gov.moj.cpp.hearing.event.nows.activiti.task;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.moj.cpp.hearing.activiti.common.ProcessMapConstant.FILE_SERVICE_ID;
import static uk.gov.moj.cpp.hearing.activiti.common.ProcessMapConstant.MATERIAL_ID;
import static uk.gov.moj.cpp.hearing.activiti.common.ProcessMapConstant.USER_ID;

import java.util.UUID;

import org.activiti.engine.delegate.DelegateExecution;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nows.activiti.worlflow.materialupload.task.MaterialUpload;


@RunWith(MockitoJUnitRunner.class)
public class MaterialUploadTest {

    @InjectMocks
    private MaterialUpload materialUpload;

    @Mock
    private Sender sender;

    @Mock
    private DelegateExecution delegateExecution;


    @Test
    public void shouldInitiateHearingForCaseRequestUsingSender() throws Exception {
        //Given
        final UUID userId = UUID.randomUUID();
        final UUID materialId = UUID.randomUUID();
        final UUID fileServiceId = UUID.randomUUID();

        when(delegateExecution.getVariable(MATERIAL_ID, String.class))
                        .thenReturn(materialId.toString());
        when(delegateExecution.getVariable(FILE_SERVICE_ID, UUID.class)).thenReturn(fileServiceId);
        when(delegateExecution.getVariable(USER_ID, UUID.class)).thenReturn(userId);

        //when
        materialUpload.execute(delegateExecution);


        final ArgumentCaptor<JsonEnvelope> senderJsonEnvelopeCaptor =
                ArgumentCaptor.forClass(JsonEnvelope.class);
        verify(sender, times(1)).send(senderJsonEnvelopeCaptor.capture());

        final JsonEnvelope envelope = senderJsonEnvelopeCaptor.getValue();
        assertThat(envelope.payloadAsJsonObject().getString(MATERIAL_ID), equalTo(materialId.toString()));
        assertThat(envelope.payloadAsJsonObject().getString(FILE_SERVICE_ID), equalTo(fileServiceId.toString()));
        assertThat(envelope.metadata().userId().get(), equalTo(userId.toString()));
        assertThat(envelope.metadata().name(),
                equalTo(MaterialUpload.MATERIAL_COMMAND_UPLOAD_FILE));

    }
}
