package uk.gov.moj.cpp.hearing.event.listener;


import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.defendantTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asSet;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.mapping.DefendantJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.util.HashSet;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AddDefendantEventListenerTest {

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private DefendantJPAMapper defendantJPAMapper;

    @Mock
    private HearingJPAMapper hearingJPAMapper;

    @InjectMocks
    private AddDefendantEventListener addCaseDefendantEventListener;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @BeforeEach
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldInsertNewDefendant() {
        //given
        final UUID arbitraryHearingId = UUID.randomUUID();
        when(hearingRepository.findBy(arbitraryHearingId)).thenReturn(new Hearing());

        //when
        addCaseDefendantEventListener.caseDefendantAdded(getDefendantAddedJsonEnvelope(arbitraryHearingId));

        //then
        final ArgumentCaptor<Hearing> hearingExArgumentCaptor = ArgumentCaptor.forClass(Hearing.class);

        verify(hearingRepository, times(1)).save(hearingExArgumentCaptor.capture());
    }

    @Test
    void shouldInsertNewDefendantToOnlyOneCaseInMultiCaseHearing() {
        final UUID arbitraryHearingId = UUID.randomUUID();
        final UUID caseId1 = UUID.randomUUID();
        final UUID caseId2 = UUID.randomUUID();

        HearingSnapshotKey key1 = new HearingSnapshotKey(caseId1, arbitraryHearingId);
        HearingSnapshotKey key2 = new HearingSnapshotKey(caseId2, arbitraryHearingId);

        ProsecutionCase pc1 = new ProsecutionCase();
        pc1.setId(key1);
        pc1.setDefendants(new HashSet<>());

        ProsecutionCase pc2 = new ProsecutionCase();
        pc2.setId(key2);
        pc2.setDefendants(new HashSet<>());

        final Hearing hearing = new Hearing();
        hearing.setProsecutionCases(asSet(pc1, pc2));

        //given
        when(hearingRepository.findBy(arbitraryHearingId)).thenReturn(hearing);

        //when
        addCaseDefendantEventListener.caseDefendantAdded(getDefendantAddedJsonEnvelope(arbitraryHearingId, caseId1));

        //then
        final ArgumentCaptor<Hearing> hearingExArgumentCaptor = ArgumentCaptor.forClass(Hearing.class);

        verify(defendantJPAMapper).toJPA(any(Hearing.class), any(ProsecutionCase.class), any(uk.gov.justice.core.courts.Defendant.class));
        verify(hearingRepository, times(1)).save(hearingExArgumentCaptor.capture());
    }

    @Test
    public void shouldNotInsertNewDefendantWhenThereIsNoHearing() {
        //given
        final UUID arbitraryHearingId = UUID.randomUUID();
        when(hearingRepository.findBy(arbitraryHearingId)).thenReturn(null);

        //when
        addCaseDefendantEventListener.caseDefendantAdded(getDefendantAddedJsonEnvelope(arbitraryHearingId));

        //then
        final ArgumentCaptor<Hearing> hearingExArgumentCaptor = ArgumentCaptor.forClass(Hearing.class);

        verify(hearingRepository, never()).save(hearingExArgumentCaptor.capture());
    }


    private JsonEnvelope getDefendantAddedJsonEnvelope(final UUID arbitraryHearingId){
        return getDefendantAddedJsonEnvelope(arbitraryHearingId, UUID.randomUUID());
    }

    private JsonEnvelope getDefendantAddedJsonEnvelope(final UUID arbitraryHearingId, final UUID caseId) {
        final uk.gov.moj.cpp.hearing.command.defendant.Defendant arbitraryDefendant = defendantTemplate(caseId);
        JsonObject payload = Json.createObjectBuilder()
                .add("hearingId", arbitraryHearingId.toString())
                .add("defendant", objectToJsonObjectConverter.convert(arbitraryDefendant))
                .build();
        return envelopeFrom((Metadata) null, payload);
    }
}
