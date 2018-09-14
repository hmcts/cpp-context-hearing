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
import uk.gov.moj.cpp.hearing.mapping.AllocationDecisionJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.DelegatedPowersJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.IndicatedPleaJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.JurorsJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.LesserOrAlternativeOffenceJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.NotifiedPleaJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.OffenceFactsJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.OffenceJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.PleaJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.VerdictJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.VerdictTypeJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.repository.DefendantRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public class UpdateOffencesForDefendantEventListenerTest {

    @InjectMocks
    private UpdateOffencesForDefendantEventListener updateOffencesForDefendantEventListener;

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private OffenceRepository offenceRepository;

    @Mock
    private DefendantRepository defendantRepository;

    @Spy
    private OffenceJPAMapper offenceJPAMapper;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.offenceJPAMapper, "notifiedPleaJPAMapper", new NotifiedPleaJPAMapper());
        setField(this.offenceJPAMapper, "indicatedPleaJPAMapper", new IndicatedPleaJPAMapper(new AllocationDecisionJPAMapper()));
        setField(this.offenceJPAMapper, "offenceFactsJPAMapper", new OffenceFactsJPAMapper());
        setField(this.offenceJPAMapper, "pleaJPAMapper", new PleaJPAMapper(new DelegatedPowersJPAMapper()));
        setField(this.offenceJPAMapper, "verdictJPAMapper", new VerdictJPAMapper(new JurorsJPAMapper(), new LesserOrAlternativeOffenceJPAMapper(), new VerdictTypeJPAMapper()));
    }

    @Test
    public void testAddOffence() {

        final OffenceAdded offenceAdded = OffenceAdded.offenceAdded()
                .withHearingId(randomUUID())
                .withDefendantId(randomUUID())
                .withProsecutionCaseId(randomUUID())
                .withOffence(uk.gov.justice.json.schemas.core.Offence.offence()
                        .withId(randomUUID())
                        .build());

        final JsonEnvelope envelope = envelopeFrom((Metadata) null, objectToJsonObjectConverter.convert(offenceAdded));

        final Hearing hearing = new Hearing(){{
            setId(offenceAdded.getHearingId());
        }};

        when(hearingRepository.findBy(offenceAdded.getHearingId())).thenReturn(hearing);

        updateOffencesForDefendantEventListener.addOffence(envelope);

        final ArgumentCaptor<Offence> defendantExArgumentCaptor = ArgumentCaptor.forClass(Offence.class);

        verify(offenceRepository).saveAndFlush(defendantExArgumentCaptor.capture());

        final Offence offenceOut = defendantExArgumentCaptor.getValue();

        assertThat(offenceAdded.getOffence().getId(), is(offenceOut.getId().getId()));
        assertThat(offenceAdded.getHearingId(), is(offenceOut.getId().getHearingId()));
    }

    @Test
    public void testUpdateOffence() {

        final OffenceUpdated offenceUpdated = OffenceUpdated.offenceUpdated()
                .withHearingId(randomUUID())
                .withDefendantId(randomUUID())
                .withOffence(uk.gov.justice.json.schemas.core.Offence.offence()
                        .withId(randomUUID())
                        .build());

        final JsonEnvelope envelope = envelopeFrom((Metadata) null, objectToJsonObjectConverter.convert(offenceUpdated));

        final Hearing hearing = new Hearing(){{
            setId(offenceUpdated.getHearingId());
        }};

        when(hearingRepository.findBy(hearing.getId())).thenReturn(hearing);

        final Offence offence = new Offence() {{
            setId(new HearingSnapshotKey(offenceUpdated.getOffence().getId(), offenceUpdated.getHearingId()));
        }};

        final Defendant defendant = new Defendant() {{
            setId(new HearingSnapshotKey(offenceUpdated.getDefendantId(), offenceUpdated.getHearingId()));
            setOffences(new ArrayList<>(Arrays.asList(offence)));
        }};

        when(defendantRepository.findBy(defendant.getId())).thenReturn(defendant);

        updateOffencesForDefendantEventListener.updateOffence(envelope);

        final ArgumentCaptor<Defendant> defendantExArgumentCaptor = ArgumentCaptor.forClass(Defendant.class);

        verify(defendantRepository).saveAndFlush(defendantExArgumentCaptor.capture());

        final Offence offenceOut = defendantExArgumentCaptor.getValue().getOffences().get(0);

        assertThat(offenceUpdated.getOffence().getId(), is(offenceOut.getId().getId()));
        assertThat(offenceUpdated.getHearingId(), is(offenceOut.getId().getHearingId()));
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

        updateOffencesForDefendantEventListener.deleteOffence(envelope);

        final ArgumentCaptor<Defendant> defendantExArgumentCaptor = ArgumentCaptor.forClass(Defendant.class);

        verify(defendantRepository).save(defendantExArgumentCaptor.capture());

        final Defendant defendantOut = defendantExArgumentCaptor.getValue();

        assertThat(defendant.getId().getId(), is(defendantOut.getId().getId()));
    }
}
