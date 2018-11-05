package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.domain.event.OffenceAdded;
import uk.gov.moj.cpp.hearing.domain.event.OffenceDeleted;
import uk.gov.moj.cpp.hearing.domain.event.OffenceUpdated;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.LegalCase;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.repository.DefendantRepository;
import uk.gov.moj.cpp.hearing.repository.LegalCaseRepository;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CaseDefendantOffencesChangedEventListenerTest {

    @InjectMocks
    private CaseDefendantOffencesChangedEventListener caseDefendantOffencesChangedEventListener;

    @Mock
    private OffenceRepository offenceRepository;

    @Mock
    private DefendantRepository defendantRepository;

    @Mock
    private LegalCaseRepository legalCaseRepository;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void testAddOffence() {

        final OffenceAdded offenceToBeAdded = OffenceAdded.builder()
                .withId(randomUUID())
                .withHearingId(randomUUID())
                .withDefendantId(randomUUID())
                .withCaseId(randomUUID()).build();

        final JsonEnvelope envelope = envelopeFrom((Metadata) null, objectToJsonObjectConverter.convert(offenceToBeAdded));

        final Defendant defendant = Defendant.builder()
                .withId(new HearingSnapshotKey(offenceToBeAdded.getDefendantId(), offenceToBeAdded.getHearingId()))
                .build();

        final LegalCase legalCase = LegalCase.builder()
                .withId(offenceToBeAdded.getCaseId())
                .withCaseurn(STRING.next())
                .build();

        when(defendantRepository.findBy(new HearingSnapshotKey(offenceToBeAdded.getDefendantId(), offenceToBeAdded.getHearingId()))).thenReturn(defendant);

        when(legalCaseRepository.findById(offenceToBeAdded.getCaseId())).thenReturn(legalCase);

        caseDefendantOffencesChangedEventListener.addOffence(envelope);

        ArgumentCaptor<Offence> defendantExArgumentCaptor = ArgumentCaptor.forClass(Offence.class);

        verify(offenceRepository).saveAndFlush(defendantExArgumentCaptor.capture());

        final Offence offenceOut = defendantExArgumentCaptor.getValue();

        assertThat(offenceToBeAdded.getId(), is(offenceOut.getId().getId()));

        assertThat(offenceToBeAdded.getHearingId(), is(offenceOut.getId().getHearingId()));

        assertThat(offenceToBeAdded.getDefendantId(), is(offenceOut.getDefendantId()));

        assertThat(offenceToBeAdded.getCaseId(), is(offenceOut.getLegalCase().getId()));
    }

    @Test
    public void testUpdateOffence() {

        final OffenceUpdated offenceUpdated = OffenceUpdated.builder()
                .withId(randomUUID())
                .withHearingId(randomUUID())
                .build();

        final JsonEnvelope envelope = envelopeFrom((Metadata) null, objectToJsonObjectConverter.convert(offenceUpdated));

        final Offence offence = Offence.builder()
                .withId(new HearingSnapshotKey(offenceUpdated.getId(), offenceUpdated.getHearingId()))
                .build();

        when(offenceRepository.findBy(new HearingSnapshotKey(offenceUpdated.getId(), offenceUpdated.getHearingId()))).thenReturn(offence);

        caseDefendantOffencesChangedEventListener.updateOffence(envelope);

        final ArgumentCaptor<Offence> defendantExArgumentCaptor = ArgumentCaptor.forClass(Offence.class);

        verify(offenceRepository).saveAndFlush(defendantExArgumentCaptor.capture());

        final Offence offenceOut = defendantExArgumentCaptor.getValue();

        assertThat(offenceUpdated.getId(), is(offenceOut.getId().getId()));

        assertThat(offenceUpdated.getHearingId(), is(offenceOut.getId().getHearingId()));
    }

    @Test
    public void testDeleteOffence() {

        final OffenceDeleted offenceDeleted = OffenceDeleted.builder().withId(randomUUID()).withHearingId(randomUUID()).build();

        final JsonEnvelope envelope = envelopeFrom((Metadata) null, objectToJsonObjectConverter.convert(offenceDeleted));

        final Defendant defendant = Defendant.builder()
                .withId(new HearingSnapshotKey(randomUUID(), offenceDeleted.getHearingId()))
                .withOffences(Collections.emptyList())
                .build();

        final Offence offence = Offence.builder()
                .withId(new HearingSnapshotKey(offenceDeleted.getId(), offenceDeleted.getHearingId()))
                .withDefendant(defendant)
                .build();

        when(offenceRepository.findBy(new HearingSnapshotKey(offenceDeleted.getId(), offenceDeleted.getHearingId()))).thenReturn(offence);

        when(defendantRepository.findBy(new HearingSnapshotKey(offence.getDefendantId(), offenceDeleted.getHearingId()))).thenReturn(defendant);

        caseDefendantOffencesChangedEventListener.deleteOffence(envelope);

        final ArgumentCaptor<Offence> defendantExArgumentCaptor = ArgumentCaptor.forClass(Offence.class);

        verify(offenceRepository).removeAndFlush(defendantExArgumentCaptor.capture());

        final Offence offenceOut = defendantExArgumentCaptor.getValue();

        assertThat(offenceDeleted.getId(), is(offenceOut.getId().getId()));
    }
}