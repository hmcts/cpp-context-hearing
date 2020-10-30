package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.domain.event.CpsProsecutorUpdated;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier;
import uk.gov.moj.cpp.hearing.repository.ProsecutionCaseRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CpsProsecutorUpdatedEventListenerTest {
    @Mock
    private ProsecutionCaseRepository prosecutionCaseRepository;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @InjectMocks
    private CpsProsecutorUpdatedEventListener cpsProsecutorUpdatedEventListener;

    @Before
    public void setUp() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldUpdateProsecutionCase() {
        //given
        final ProsecutionCaseIdentifier prosecutionCaseIdentifier = new ProsecutionCaseIdentifier();
        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        prosecutionCase.setProsecutionCaseIdentifier(prosecutionCaseIdentifier);

        //when
        when(prosecutionCaseRepository.findBy(any(HearingSnapshotKey.class))).thenReturn(prosecutionCase);
        final CpsProsecutorUpdated cpsProsecutorUpdated = getCpsProsecutorCaseEnvelope();
        cpsProsecutorUpdatedEventListener.cpsProsecutorUpdated(envelopeFrom(metadataWithRandomUUID("hearing.cps-prosecutor-updated"),
                objectToJsonObjectConverter.convert(cpsProsecutorUpdated)));

        //then
        final ArgumentCaptor<ProsecutionCase> prosecutionCaseArgumentCaptor = ArgumentCaptor.forClass(ProsecutionCase.class);

        verify(prosecutionCaseRepository, times(1)).save(prosecutionCaseArgumentCaptor.capture());

        final ProsecutionCaseIdentifier capturedProsecutionCaseIdentifier = prosecutionCaseArgumentCaptor.getAllValues().get(0).getProsecutionCaseIdentifier();
        assertThat(capturedProsecutionCaseIdentifier.getProsecutionAuthorityId(), is(cpsProsecutorUpdated.getProsecutionAuthorityId()));
        assertThat(capturedProsecutionCaseIdentifier.getProsecutionAuthorityReference(), is(cpsProsecutorUpdated.getProsecutionAuthorityReference()));
        assertThat(capturedProsecutionCaseIdentifier.getProsecutionAuthorityCode(), is(cpsProsecutorUpdated.getProsecutionAuthorityCode()));
        assertThat(capturedProsecutionCaseIdentifier.getCaseURN(), is(cpsProsecutorUpdated.getCaseURN()));

    }

    private CpsProsecutorUpdated getCpsProsecutorCaseEnvelope() {

        return CpsProsecutorUpdated.cpsProsecutorUpdated()
                .setProsecutionCaseId(randomUUID())
                .setHearingId(randomUUID())
                .setProsecutionAuthorityReference(STRING.next())
                .setProsecutionAuthorityCode(STRING.next())
                .setProsecutionAuthorityId(randomUUID())
                .setCaseURN(STRING.next());
    }
}
