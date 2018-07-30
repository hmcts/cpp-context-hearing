package uk.gov.moj.cpp.hearing.event.nows.activiti.listener;

import static java.util.UUID.randomUUID;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;
import static uk.gov.justice.services.messaging.JsonMetadata.ID;
import static uk.gov.justice.services.messaging.JsonMetadata.NAME;
import static uk.gov.moj.cpp.hearing.activiti.common.JsonHelper.assembleEnvelopeWithPayloadAndMetaDetails;
import static uk.gov.moj.cpp.hearing.activiti.common.ProcessMapConstant.MATERIAL_ID;
import static uk.gov.moj.cpp.hearing.event.nows.activiti.worlflow.materialupload.listener.HmpsMaterialStatusUpdatedListener.PUBLIC_RESULTINGHMPS_EVENT_NOWS_MATERIAL_STATUS_UPDATED;
import static uk.gov.moj.cpp.hearing.event.nows.activiti.worlflow.materialupload.listener.HmpsMaterialStatusUpdatedListener.RECEIVE_STATUS_UPDATE_CONFIRMATION_HMPS;

import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;
import uk.gov.moj.cpp.hearing.activiti.common.JsonHelper;
import uk.gov.moj.cpp.hearing.activiti.service.ActivitiService;
import uk.gov.moj.cpp.hearing.event.nows.activiti.worlflow.materialupload.listener.HmpsMaterialStatusUpdatedListener;


@RunWith(MockitoJUnitRunner.class)
public class HmpsMaterialStatusUpdatedListenerTest {

    private static final String USER_ID = randomUUID().toString();
    @Mock
    private ActivitiService activitiService;
    @InjectMocks
    private HmpsMaterialStatusUpdatedListener hmpsMaterialStatusUpdatedListener;
    @Mock
    private JsonObjectToObjectConverter jsonObjectConverter;
    @Mock
    private JsonEnvelope event;
    @Mock
    private JsonObject jsonObject;

    @Test
    public void shouldSignalProcessWithActivitiIdIfCaseIdPresent() {
        //Given
        final UUID materialId = UUID.randomUUID();
        when(event.payloadAsJsonObject()).thenReturn(jsonObject);
        when(event.metadata()).thenReturn(getMetedata());
        when(jsonObject.containsKey(MATERIAL_ID)).thenReturn(true);
        when(jsonObject.getString(MATERIAL_ID)).thenReturn(materialId.toString());

        //when
        this.hmpsMaterialStatusUpdatedListener.processEvent(event);
        //then
        verify(activitiService).signalProcessByActivitiIdAndFieldName(eq(RECEIVE_STATUS_UPDATE_CONFIRMATION_HMPS), eq(MATERIAL_ID), eq(materialId.toString()));
    }

    @Test
    public void shouldNotSignalProcessWithActivitiIdIfCaseIdNotPresent() {
        //Given
        final String fakeId = UUID.randomUUID().toString();
        final JsonObject payload = Json.createObjectBuilder().add("fakeId", fakeId).build();
        when(event.metadata()).thenReturn(getMetedata());
        final JsonEnvelope jsonEnvelope = assembleEnvelopeWithPayloadAndMetaDetails(payload, PUBLIC_RESULTINGHMPS_EVENT_NOWS_MATERIAL_STATUS_UPDATED, fakeId.toString(), USER_ID.toString());

        //when
        hmpsMaterialStatusUpdatedListener.processEvent(jsonEnvelope);
        //then
        verify(activitiService, never()).signalProcessByActivitiIdAndFieldName(any(), any(), any());
    }
    private Metadata getMetedata() {
        return metadataFrom(Json.createObjectBuilder()
                        .add(JsonHelper.ORIGINATOR, JsonHelper.ORIGINATOR_VALUE)
                        .add(ID, randomUUID().toString())
                        .add(NAME, RandomGenerator.STRING.next())
                        .build()).build();
    }
}
