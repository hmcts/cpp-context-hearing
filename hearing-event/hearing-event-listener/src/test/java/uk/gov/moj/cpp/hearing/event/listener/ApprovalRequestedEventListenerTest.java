package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.domain.event.result.ApprovalRequested;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.util.UUID;

import javax.json.JsonObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApprovalRequestedEventListenerTest {
    private static final UUID HEARING_ID = randomUUID();
    private static final UUID USER_ID = randomUUID();


    @Spy
    private ObjectMapper objectMapper;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Mock
    private HearingRepository hearingRepository;

    @InjectMocks
    private ApprovalRequestedEventListener approvalRequestedEventListener;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.approvalRequestedEventListener, "jsonObjectToObjectConverter", jsonObjectToObjectConverter);
    }

    @Test
    public void shouldRecordApprovalRequests() {
        Hearing mockHearing = mock(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing.class);
        when(hearingRepository.findBy(any())).thenReturn(mockHearing);
        final ApprovalRequested approvalRequested1 = new ApprovalRequested(HEARING_ID, USER_ID);
        approvalRequestedEventListener.approvalRequested(createJsonEnvelope(approvalRequested1));
        final ArgumentCaptor<uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing> hearingArgumentCaptor1 = ArgumentCaptor
                .forClass(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing.class);
        verify(hearingRepository, times(1)).save(hearingArgumentCaptor1.capture());
    }

    private JsonEnvelope createJsonEnvelope(final ApprovalRequested approvalRequested) {
        final JsonObject jsonObject = objectToJsonObjectConverter.convert(approvalRequested);
        return envelopeFrom((Metadata) null, jsonObject);
    }
}