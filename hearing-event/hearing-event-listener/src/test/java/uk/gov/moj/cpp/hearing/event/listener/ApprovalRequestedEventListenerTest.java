package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.core.courts.ApprovalType.APPROVAL;
import static uk.gov.justice.core.courts.ApprovalType.CHANGE;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.domain.event.result.ApprovalRequested;
import uk.gov.moj.cpp.hearing.mapping.ApprovalRequestedJPAMapper;
import uk.gov.moj.cpp.hearing.repository.ApprovalRequestedRepository;

import java.time.ZoneId;
import java.time.ZonedDateTime;
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

    @Mock
    private ApprovalRequestedRepository approvalRequestedRepository;

    @Spy
    private ObjectMapper objectMapper;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ApprovalRequestedJPAMapper approvalRequestedJPAMapper;

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

        final ApprovalRequested approvalRequested1 = new ApprovalRequested(HEARING_ID, USER_ID, ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")), CHANGE);
        approvalRequestedEventListener.approvalRequested(createJsonEnvelope(approvalRequested1));
        final ArgumentCaptor<uk.gov.moj.cpp.hearing.persist.entity.ha.ApprovalRequested> approvalRequestedArgumentCaptor1 = ArgumentCaptor
                .forClass(uk.gov.moj.cpp.hearing.persist.entity.ha.ApprovalRequested.class);
        verify(approvalRequestedRepository, times(1)).save(approvalRequestedArgumentCaptor1.capture());
        verifyApprovalRequest(approvalRequested1, approvalRequestedArgumentCaptor1);

        final ApprovalRequested approvalRequested2 = new ApprovalRequested(HEARING_ID, USER_ID, ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")), CHANGE);
        approvalRequestedEventListener.approvalRequested(createJsonEnvelope(approvalRequested2));
        final ArgumentCaptor<uk.gov.moj.cpp.hearing.persist.entity.ha.ApprovalRequested> approvalRequestedArgumentCaptor2 = ArgumentCaptor
                .forClass(uk.gov.moj.cpp.hearing.persist.entity.ha.ApprovalRequested.class);
        verify(approvalRequestedRepository, times(2)).save(approvalRequestedArgumentCaptor2.capture());
        verifyApprovalRequest(approvalRequested2, approvalRequestedArgumentCaptor2);

        final ApprovalRequested approvalRequested3 = new ApprovalRequested(HEARING_ID, USER_ID, ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")), APPROVAL);
        approvalRequestedEventListener.approvalRequested(createJsonEnvelope(approvalRequested3));
        final ArgumentCaptor<uk.gov.moj.cpp.hearing.persist.entity.ha.ApprovalRequested> approvalRequestedArgumentCaptor3 = ArgumentCaptor
                .forClass(uk.gov.moj.cpp.hearing.persist.entity.ha.ApprovalRequested.class);
        verify(approvalRequestedRepository, times(3)).save(approvalRequestedArgumentCaptor3.capture());
        verifyApprovalRequest(approvalRequested3, approvalRequestedArgumentCaptor3);

    }

    private void verifyApprovalRequest(final ApprovalRequested approvalRequested,
                                       final ArgumentCaptor<uk.gov.moj.cpp.hearing.persist.entity.ha.ApprovalRequested> approvalRequestedArgumentCaptor) {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.ApprovalRequested approvalRequestedActual = approvalRequestedArgumentCaptor.getValue();
        assertThat(approvalRequestedActual.getHearingId(), is(approvalRequested.getHearingId()));
        assertThat(approvalRequestedActual.getUserId(), is(approvalRequested.getUserId()));
        assertThat(approvalRequestedActual.getRequestApprovalTime(), is(approvalRequested.getRequestApprovalTime()));
        assertThat(approvalRequestedActual.getApprovalType(), is(approvalRequested.getApprovalType()));
    }

    private JsonEnvelope createJsonEnvelope(final ApprovalRequested approvalRequested) {
        final JsonObject jsonObject = objectToJsonObjectConverter.convert(approvalRequested);
        return envelopeFrom((Metadata) null, jsonObject);
    }
}