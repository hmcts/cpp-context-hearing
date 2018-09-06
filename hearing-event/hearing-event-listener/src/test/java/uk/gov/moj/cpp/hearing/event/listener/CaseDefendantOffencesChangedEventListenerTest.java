package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
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
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.repository.DefendantRepository;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;

import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public class CaseDefendantOffencesChangedEventListenerTest {

    @InjectMocks
    private CaseDefendantOffencesChangedEventListener caseDefendantOffencesChangedEventListener;

    @Mock
    private OffenceRepository offenceRepository;

    @Mock
    private DefendantRepository defendantRepository;

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

        caseDefendantOffencesChangedEventListener.addOffence(envelope);

        ArgumentCaptor<Offence> defendantExArgumentCaptor = ArgumentCaptor.forClass(Offence.class);

        verify(offenceRepository).saveAndFlush(defendantExArgumentCaptor.capture());

        final Offence offenceOut = defendantExArgumentCaptor.getValue();

        assertThat(offenceToBeAdded.getId(), is(offenceOut.getId().getId()));

    }

    @Test
    public void testUpdateOffence() {

        final OffenceUpdated offenceUpdated = OffenceUpdated.builder()
                .withId(randomUUID())
                .withHearingId(randomUUID())
                .build();

        final JsonEnvelope envelope = envelopeFrom((Metadata) null, objectToJsonObjectConverter.convert(offenceUpdated));

        final Offence offence = new Offence();
        offence.setId(new HearingSnapshotKey(offenceUpdated.getId(), offenceUpdated.getHearingId()));

        when(offenceRepository.findBy(offence.getId())).thenReturn(offence);

        caseDefendantOffencesChangedEventListener.updateOffence(envelope);

        final ArgumentCaptor<Offence> defendantExArgumentCaptor = ArgumentCaptor.forClass(Offence.class);

        verify(offenceRepository).saveAndFlush(defendantExArgumentCaptor.capture());

        final Offence offenceOut = defendantExArgumentCaptor.getValue();

        assertThat(offenceUpdated.getId(), is(offenceOut.getId().getId()));
    }

    @Test
    public void testDeleteOffence() {

        final OffenceDeleted offenceDeleted = OffenceDeleted.builder().withId(randomUUID()).withHearingId(randomUUID()).build();

        final JsonEnvelope envelope = envelopeFrom((Metadata) null, objectToJsonObjectConverter.convert(offenceDeleted));

        final Defendant defendant = new Defendant();
        defendant.setId(new HearingSnapshotKey(randomUUID(), offenceDeleted.getHearingId()));
        defendant.setOffences(Collections.emptyList());

        final Offence offence = new Offence();
        offence.setId(new HearingSnapshotKey(offenceDeleted.getId(), offenceDeleted.getHearingId()));
        offence.setDefendant(defendant);

        when(offenceRepository.findBy(offence.getId())).thenReturn(offence);

        caseDefendantOffencesChangedEventListener.deleteOffence(envelope);

        final ArgumentCaptor<Defendant> defendantExArgumentCaptor = ArgumentCaptor.forClass(Defendant.class);

        verify(defendantRepository).save(defendantExArgumentCaptor.capture());

        final Defendant defendantOut = defendantExArgumentCaptor.getValue();

        assertThat(defendant.getId().getId(), is(defendantOut.getId().getId()));
    }
}
