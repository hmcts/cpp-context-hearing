package uk.gov.moj.cpp.hearing.event.listener;

import static javax.json.Json.createObjectBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole;
import uk.gov.moj.cpp.hearing.repository.JudicialRoleRepository;

import java.util.UUID;

import javax.json.JsonObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HearingJudiciaryListenerTest  {

    @Mock
    private JudicialRoleRepository judicialRoleRepository;

    @Captor
    private ArgumentCaptor<JudicialRole> argumentCaptor;
    @InjectMocks
    HearingJudiciaryListener hearingJudiciaryListener;
    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;
    @BeforeEach
    public void setUp() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    void testJudiciaryUserIdSaved() {

        final UUID judiciaryId = UUID.randomUUID();
        final String emailId = "abc@xyx.com";
        final UUID cpUserId = UUID.randomUUID();
        final UUID id = UUID.randomUUID();
        final UUID hearingId = UUID.randomUUID();
        final JsonObject eventPayload = createObjectBuilder()
                .add("judiciaryId", judiciaryId.toString())
                .add("emailId", emailId)
                .add("cpUserId", cpUserId.toString())
                .add("hearingId", hearingId.toString())
                .add("id", id.toString())
                .build();
        final JsonEnvelope event = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.event.user-added-to-judiciary"),
                eventPayload);

        JudicialRole judicialRole = new JudicialRole();
        judicialRole.setJudicialId(judiciaryId);
        judicialRole.setUserId(null);
        judicialRole.setId(new HearingSnapshotKey(id,hearingId));

        when(judicialRoleRepository.findBy(any())).thenReturn(judicialRole);
        hearingJudiciaryListener.userAddedToJudiciary(event);
        verify(judicialRoleRepository, times(1)).save(argumentCaptor.capture());

        assertEquals(argumentCaptor.getValue().getUserId(), cpUserId);
    }
}