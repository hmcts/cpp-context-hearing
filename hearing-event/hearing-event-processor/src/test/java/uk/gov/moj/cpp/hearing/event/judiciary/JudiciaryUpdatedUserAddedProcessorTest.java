package uk.gov.moj.cpp.hearing.event.judiciary;

import static javax.json.Json.createObjectBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole;
import uk.gov.moj.cpp.hearing.repository.JudicialRoleRepository;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.json.JsonObject;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JudiciaryUpdatedUserAddedProcessorTest  {

    @Mock
    private JudicialRoleRepository judicialRoleRepository;

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<Envelope<JsonObject>> argumentCaptor;
    @InjectMocks
    JudiciaryUpdatedUserAddedProcessor judiciaryUpdatedUserAddedProcessor;

    @Test
    void testBothJudiciaryHasNullUser() {

        final UUID judiciaryId = UUID.randomUUID();
        final String emailId = "abc@xyx.com";
        final UUID cpUserId = UUID.randomUUID();
        final UUID id1 = UUID.randomUUID();
        final UUID hearingId1 = UUID.randomUUID();
        final UUID id2 = UUID.randomUUID();
        final UUID hearingId2 = UUID.randomUUID();
        final JsonObject eventPayload = createObjectBuilder()
                .add("judiciaryId", judiciaryId.toString())
                .add("emailId", emailId)
                .add("cpUserId", cpUserId.toString())
                .build();
        final JsonEnvelope event = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("public.referencedata.event.user-associated-with-judiciary"),
                eventPayload);

        final Hearing hearing1 = new Hearing();
        HearingDay hearingDay1 = new HearingDay();
        hearingDay1.setDateTime(ZonedDateTime.now().plusDays(1));
        hearing1.setHearingDays(Sets.newHashSet(hearingDay1));
        JudicialRole judicialRole1 = new JudicialRole();
        judicialRole1.setJudicialId(judiciaryId);
        judicialRole1.setUserId(null);
        judicialRole1.setId(new HearingSnapshotKey(id1,hearingId1));
        judicialRole1.setHearing(hearing1);

        final Hearing hearing2 = new Hearing();
        HearingDay hearingDay2 = new HearingDay();
        hearingDay2.setDateTime(ZonedDateTime.now().plusDays(2));
        hearing2.setHearingDays(Sets.newHashSet(hearingDay2));
        JudicialRole judicialRole2 = new JudicialRole();
        judicialRole2.setJudicialId(judiciaryId);
        judicialRole2.setUserId(null);
        judicialRole2.setId(new HearingSnapshotKey(id2,hearingId2));
        judicialRole2.setHearing(hearing2);

        final List<JudicialRole> judicialRoleList = Arrays.asList(judicialRole1, judicialRole2);

        when(judicialRoleRepository.findByJudicialId(any())).thenReturn(judicialRoleList);
        judiciaryUpdatedUserAddedProcessor.userAssociatedWithJudiciary(event);
        verify(sender, times(2)).send(argumentCaptor.capture());

        assertEquals("abc@xyx.com", argumentCaptor.getAllValues().get(0).payload().getString("emailId"));
        assertEquals("abc@xyx.com", argumentCaptor.getAllValues().get(1).payload().getString("emailId"));

        assertEquals(argumentCaptor.getAllValues().get(0).payload().getString("judiciaryId"), judiciaryId.toString());
        assertEquals(argumentCaptor.getAllValues().get(1).payload().getString("judiciaryId"), judiciaryId.toString());

        assertEquals(argumentCaptor.getAllValues().get(0).payload().getString("hearingId"), hearingId1.toString());
        assertEquals(argumentCaptor.getAllValues().get(1).payload().getString("hearingId"), hearingId2.toString());

        assertEquals(argumentCaptor.getAllValues().get(0).payload().getString("id"), id1.toString());
        assertEquals(argumentCaptor.getAllValues().get(1).payload().getString("id"), id2.toString());



    }

    @Test
    void testOneJudiciaryHasNullUser() {

        final UUID judiciaryId = UUID.randomUUID();
        final String emailId = "abc@xyx.com";
        final UUID cpUserId = UUID.randomUUID();
        final UUID id1 = UUID.randomUUID();
        final UUID hearingId1 = UUID.randomUUID();
        final UUID id2 = UUID.randomUUID();
        final UUID hearingId2 = UUID.randomUUID();
        final JsonObject eventPayload = createObjectBuilder()
                .add("judiciaryId", judiciaryId.toString())
                .add("emailId", emailId)
                .add("cpUserId", cpUserId.toString())
                .build();
        final JsonEnvelope event = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("public.referencedata.event.user-associated-with-judiciary"),
                eventPayload);

        final Hearing hearing1 = new Hearing();
        HearingDay hearingDay1 = new HearingDay();
        hearingDay1.setDateTime(ZonedDateTime.now().plusDays(1));
        hearing1.setHearingDays(Sets.newHashSet(hearingDay1));
        JudicialRole judicialRole1 = new JudicialRole();
        judicialRole1.setJudicialId(judiciaryId);
        judicialRole1.setUserId(null);
        judicialRole1.setId(new HearingSnapshotKey(id1,hearingId1));
        judicialRole1.setHearing(hearing1);

        final Hearing hearing2 = new Hearing();
        HearingDay hearingDay2 = new HearingDay();
        hearingDay2.setDateTime(ZonedDateTime.now().plusDays(2));
        hearing2.setHearingDays(Sets.newHashSet(hearingDay2));
        JudicialRole judicialRole2 = new JudicialRole();
        judicialRole2.setJudicialId(judiciaryId);
        judicialRole2.setUserId(UUID.randomUUID());
        judicialRole2.setId(new HearingSnapshotKey(id2,hearingId2));
        judicialRole2.setHearing(hearing2);

        final List<JudicialRole> judicialRoleList = Arrays.asList(judicialRole1, judicialRole2);

        when(judicialRoleRepository.findByJudicialId(any())).thenReturn(judicialRoleList);
        judiciaryUpdatedUserAddedProcessor.userAssociatedWithJudiciary(event);
        verify(sender, times(1)).send(argumentCaptor.capture());

        assertEquals(argumentCaptor.getAllValues().size(), 1);
        assertEquals("abc@xyx.com", argumentCaptor.getAllValues().get(0).payload().getString("emailId"));

        assertEquals(argumentCaptor.getAllValues().get(0).payload().getString("judiciaryId"), judiciaryId.toString());

        assertEquals(argumentCaptor.getAllValues().get(0).payload().getString("hearingId"), hearingId1.toString());

        assertEquals(argumentCaptor.getAllValues().get(0).payload().getString("id"), id1.toString());



    }
    @Test
    void testJudiciaryHasFutureHearing() {

        final UUID judiciaryId = UUID.randomUUID();
        final String emailId = "abc@xyx.com";
        final UUID cpUserId = UUID.randomUUID();
        final UUID id1 = UUID.randomUUID();
        final UUID hearingId1 = UUID.randomUUID();
        final UUID id2 = UUID.randomUUID();
        final UUID hearingId2 = UUID.randomUUID();
        final JsonObject eventPayload = createObjectBuilder()
                .add("judiciaryId", judiciaryId.toString())
                .add("emailId", emailId)
                .add("cpUserId", cpUserId.toString())
                .build();
        final JsonEnvelope event = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("public.referencedata.event.user-associated-with-judiciary"),
                eventPayload);

        final Hearing hearing1 = new Hearing();
        HearingDay hearingDay1 = new HearingDay();
        hearingDay1.setDateTime(ZonedDateTime.now().minusDays(1));
        hearing1.setHearingDays(Sets.newHashSet(hearingDay1));
        JudicialRole judicialRole1 = new JudicialRole();
        judicialRole1.setJudicialId(judiciaryId);
        judicialRole1.setUserId(null);
        judicialRole1.setId(new HearingSnapshotKey(id1,hearingId1));
        judicialRole1.setHearing(hearing1);

        final Hearing hearing2 = new Hearing();
        HearingDay hearingDay2 = new HearingDay();
        hearingDay2.setDateTime(ZonedDateTime.now().plusDays(2));
        hearing2.setHearingDays(Sets.newHashSet(hearingDay2));
        JudicialRole judicialRole2 = new JudicialRole();
        judicialRole2.setJudicialId(judiciaryId);
        judicialRole2.setUserId(null);
        judicialRole2.setId(new HearingSnapshotKey(id2,hearingId2));
        judicialRole2.setHearing(hearing2);

        final List<JudicialRole> judicialRoleList = Arrays.asList(judicialRole1, judicialRole2);

        when(judicialRoleRepository.findByJudicialId(any())).thenReturn(judicialRoleList);
        judiciaryUpdatedUserAddedProcessor.userAssociatedWithJudiciary(event);
        verify(sender, times(1)).send(argumentCaptor.capture());

        assertEquals(argumentCaptor.getAllValues().size(), 1);
        assertEquals("abc@xyx.com", argumentCaptor.getAllValues().get(0).payload().getString("emailId"));

        assertEquals(argumentCaptor.getAllValues().get(0).payload().getString("judiciaryId"), judiciaryId.toString());

        assertEquals(argumentCaptor.getAllValues().get(0).payload().getString("hearingId"), hearingId2.toString());

        assertEquals(argumentCaptor.getAllValues().get(0).payload().getString("id"), id2.toString());



    }

    @Test
    void testJudiciaryHasMultiDayFutureHearing() {

        final UUID judiciaryId = UUID.randomUUID();
        final String emailId = "abc@xyx.com";
        final UUID cpUserId = UUID.randomUUID();
        final UUID id1 = UUID.randomUUID();
        final UUID hearingId1 = UUID.randomUUID();
        final UUID id2 = UUID.randomUUID();
        final UUID hearingId2 = UUID.randomUUID();
        final JsonObject eventPayload = createObjectBuilder()
                .add("judiciaryId", judiciaryId.toString())
                .add("emailId", emailId)
                .add("cpUserId", cpUserId.toString())
                .build();
        final JsonEnvelope event = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("public.referencedata.event.user-associated-with-judiciary"),
                eventPayload);

        final Hearing hearing1 = new Hearing();
        HearingDay hearingDay1 = new HearingDay();
        hearingDay1.setDateTime(ZonedDateTime.now().minusDays(1));
        hearing1.setHearingDays(Sets.newHashSet(hearingDay1));
        JudicialRole judicialRole1 = new JudicialRole();
        judicialRole1.setJudicialId(judiciaryId);
        judicialRole1.setUserId(null);
        judicialRole1.setId(new HearingSnapshotKey(id1,hearingId1));
        judicialRole1.setHearing(hearing1);

        final Hearing hearing2 = new Hearing();

        HearingDay hearingDay21 = new HearingDay();
        hearingDay21.setId(new HearingSnapshotKey(UUID.randomUUID(),hearingId2));
        hearingDay21.setDateTime(ZonedDateTime.now().minusDays(1));

        HearingDay hearingDay22 = new HearingDay();
        hearingDay21.setId(new HearingSnapshotKey(UUID.randomUUID(),hearingId2));
        hearingDay22.setDateTime(ZonedDateTime.now().plusDays(2));

        hearing2.setHearingDays(Sets.newHashSet(hearingDay21, hearingDay22));
        JudicialRole judicialRole2 = new JudicialRole();
        judicialRole2.setJudicialId(judiciaryId);
        judicialRole2.setUserId(null);
        judicialRole2.setId(new HearingSnapshotKey(id2,hearingId2));
        judicialRole2.setHearing(hearing2);

        final List<JudicialRole> judicialRoleList = Arrays.asList(judicialRole1, judicialRole2);

        when(judicialRoleRepository.findByJudicialId(any())).thenReturn(judicialRoleList);
        judiciaryUpdatedUserAddedProcessor.userAssociatedWithJudiciary(event);
        verify(sender, times(1)).send(argumentCaptor.capture());

        assertEquals(1, argumentCaptor.getAllValues().size());
        assertEquals("abc@xyx.com", argumentCaptor.getAllValues().get(0).payload().getString("emailId") );

        assertEquals(argumentCaptor.getAllValues().get(0).payload().getString("judiciaryId"), judiciaryId.toString());

        assertEquals(argumentCaptor.getAllValues().get(0).payload().getString("hearingId"), hearingId2.toString());

        assertEquals(argumentCaptor.getAllValues().get(0).payload().getString("id"), id2.toString());



    }

    @Test
    void testJudiciaryHasMultiDayPastHearing() {

        final UUID judiciaryId = UUID.randomUUID();
        final String emailId = "abc@xyx.com";
        final UUID cpUserId = UUID.randomUUID();
        final UUID id1 = UUID.randomUUID();
        final UUID hearingId1 = UUID.randomUUID();
        final UUID id2 = UUID.randomUUID();
        final UUID hearingId2 = UUID.randomUUID();
        final JsonObject eventPayload = createObjectBuilder()
                .add("judiciaryId", judiciaryId.toString())
                .add("emailId", emailId)
                .add("cpUserId", cpUserId.toString())
                .build();
        final JsonEnvelope event = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("public.referencedata.event.user-associated-with-judiciary"),
                eventPayload);

        final Hearing hearing1 = new Hearing();
        HearingDay hearingDay1 = new HearingDay();
        hearingDay1.setDateTime(ZonedDateTime.now().minusDays(1));
        hearing1.setHearingDays(Sets.newHashSet(hearingDay1));
        JudicialRole judicialRole1 = new JudicialRole();
        judicialRole1.setJudicialId(judiciaryId);
        judicialRole1.setUserId(null);
        judicialRole1.setId(new HearingSnapshotKey(id1,hearingId1));
        judicialRole1.setHearing(hearing1);

        final Hearing hearing2 = new Hearing();

        HearingDay hearingDay21 = new HearingDay();
        hearingDay21.setId(new HearingSnapshotKey(UUID.randomUUID(),hearingId2));
        hearingDay21.setDateTime(ZonedDateTime.now().minusDays(1));

        HearingDay hearingDay22 = new HearingDay();
        hearingDay21.setId(new HearingSnapshotKey(UUID.randomUUID(),hearingId2));
        hearingDay22.setDateTime(ZonedDateTime.now().minusDays(3));

        hearing2.setHearingDays(Sets.newHashSet(hearingDay21, hearingDay22));
        JudicialRole judicialRole2 = new JudicialRole();
        judicialRole2.setJudicialId(judiciaryId);
        judicialRole2.setUserId(null);
        judicialRole2.setId(new HearingSnapshotKey(id2,hearingId2));
        judicialRole2.setHearing(hearing2);

        final List<JudicialRole> judicialRoleList = Arrays.asList(judicialRole1, judicialRole2);
        when(judicialRoleRepository.findByJudicialId(any())).thenReturn(judicialRoleList);
        judiciaryUpdatedUserAddedProcessor.userAssociatedWithJudiciary(event);
        verify(sender, never()).send(argumentCaptor.capture());


    }
}