package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PleaUpdateEventListenerTest {
    
    private enum PleaValueType {GUILTY, NOT_GUILTY}

    @Mock
    private OffenceRepository offenceRepository;

    @InjectMocks
    private PleaUpdateEventListener pleaUpdateEventListener;

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
    public void pleaUpdate_shouldUpdatePlea_toGuilty() throws Exception {

        final UUID hearingId = randomUUID();

        final OffencePleaUpdated offencePleaUpdated = OffencePleaUpdated.builder()
                .withHearingId(hearingId)
                .withOffenceId(randomUUID())
                .withPleaDate(LocalDate.now())
                .withValue(PleaValueType.GUILTY.name())
                .build();

        final Hearing hearing = Hearing.builder().withId(hearingId)
                .withDefendants(asList
                        (Defendant.builder()
                                .withOffences(asList(
                                        Offence.builder()
                                                .withId(new HearingSnapshotKey(offencePleaUpdated.getOffenceId(), hearingId))
                                                .withPleaDate(offencePleaUpdated.getPleaDate())
                                                .withPleaValue(offencePleaUpdated.getValue())
                                                .build()
                                ))
                                .build()))
                .build();

        final Offence offence = hearing.getDefendants().get(0).getOffences().get(0);

        when(this.offenceRepository.findBySnapshotKey(offence.getId())).thenReturn(offence);

        pleaUpdateEventListener.offencePleaUpdated(envelopeFrom(metadataWithRandomUUID("hearing.hearing-offence-plea-updated"),
                objectToJsonObjectConverter.convert(offencePleaUpdated)));

        verify(this.offenceRepository).save(offence);

        assertThat(offence.getId().getId(), is(offencePleaUpdated.getOffenceId()));
        assertThat(offence.getId().getHearingId(), is(offencePleaUpdated.getHearingId()));
        assertThat(offence.getPleaDate(), is(offencePleaUpdated.getPleaDate()));
        assertThat(offence.getPleaValue(), is(offencePleaUpdated.getValue()));
    }

    @Test
    public void pleaUpdate_shouldUpdatePlea_toToNotGuilty() throws Exception {

        final UUID hearingId = randomUUID();

        final OffencePleaUpdated offencePleaUpdated = OffencePleaUpdated.builder()
                .withHearingId(hearingId)
                .withOffenceId(randomUUID())
                .withPleaDate(LocalDate.now())
                .withValue(PleaValueType.NOT_GUILTY.name())
                .build();

        final Hearing hearing = Hearing.builder().withId(hearingId)
                .withDefendants(asList
                        (Defendant.builder()
                                .withOffences(asList(
                                        Offence.builder()
                                                .withId(new HearingSnapshotKey(offencePleaUpdated.getOffenceId(), hearingId))
                                                .withPleaDate(offencePleaUpdated.getPleaDate())
                                                .withPleaValue(offencePleaUpdated.getValue())
                                                .build()
                                ))
                                .build()))
                .build();

        final Offence offence = hearing.getDefendants().get(0).getOffences().get(0);

        when(this.offenceRepository.findBySnapshotKey(offence.getId())).thenReturn(offence);

        pleaUpdateEventListener.offencePleaUpdated(envelopeFrom(metadataWithRandomUUID("hearing.hearing-offence-plea-updated"),
                objectToJsonObjectConverter.convert(offencePleaUpdated)));

        verify(this.offenceRepository).save(offence);

        assertThat(offence.getId().getId(), is(offencePleaUpdated.getOffenceId()));
        assertThat(offence.getId().getHearingId(), is(offencePleaUpdated.getHearingId()));
        assertThat(offence.getPleaDate(), is(offencePleaUpdated.getPleaDate()));
        assertThat(offence.getPleaValue(), is(offencePleaUpdated.getValue()));
    }
}