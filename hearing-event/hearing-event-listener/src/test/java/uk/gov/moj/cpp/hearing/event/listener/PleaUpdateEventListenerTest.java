package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.json.schemas.core.DelegatedPowers;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;

import java.time.LocalDate;
import java.util.UUID;

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

        final UUID offenceId = randomUUID();

        final OffencePleaUpdated offencePleaUpdated = OffencePleaUpdated.builder()
                .withHearingId(hearingId)
                .withOffenceId(offenceId)
                .withPleaDate(LocalDate.now())
                .withValue(PleaValueType.GUILTY.name())
                .build();

        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);

        final Offence offence = new Offence();
        offence.setId(new HearingSnapshotKey(offenceId, hearingId));

        final Defendant defendant = new Defendant();
        defendant.setOffences(singletonList(offence));

        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        prosecutionCase.setDefendants(singletonList(defendant));

        hearing.setProsecutionCases(singletonList(prosecutionCase));

        when(this.offenceRepository.findBy(offence.getId())).thenReturn(offence);

        pleaUpdateEventListener.offencePleaUpdated(envelopeFrom(metadataWithRandomUUID("hearing.hearing-offence-plea-updated"),
                objectToJsonObjectConverter.convert(offencePleaUpdated)));

        verify(this.offenceRepository).save(offence);

        assertThat(offence.getId().getId(), is(offencePleaUpdated.getOffenceId()));
        assertThat(offence.getId().getHearingId(), is(offencePleaUpdated.getHearingId()));
        assertThat(offence.getPlea().getPleaDate(), is(offencePleaUpdated.getPleaDate()));
        assertThat(offence.getPlea().getPleaValue().toString(), is(offencePleaUpdated.getValue()));
    }

    @Test
    public void pleaUpdate_shouldUpdatePlea_toToNotGuilty() throws Exception {

        final UUID hearingId = randomUUID();

        final UUID offenceId = randomUUID();

        final DelegatedPowers delegatedPowers = DelegatedPowers.delegatedPowers()
                .withUserId(UUID.randomUUID())
                .withLastName("David")
                .withFirstName("Bowie")
                .build();

        final OffencePleaUpdated offencePleaUpdated = OffencePleaUpdated.builder()
                .withHearingId(hearingId)
                .withOffenceId(offenceId)
                .withPleaDate(LocalDate.now())
                .withValue(PleaValueType.NOT_GUILTY.name())
                .withDelegatedPowers(delegatedPowers)
                .build();

        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);

        final Offence offence = new Offence();
        offence.setId(new HearingSnapshotKey(offenceId, hearingId));

        final Defendant defendant = new Defendant();
        defendant.setOffences(singletonList(offence));

        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        prosecutionCase.setDefendants(singletonList(defendant));

        hearing.setProsecutionCases(singletonList(prosecutionCase));

        when(this.offenceRepository.findBy(offence.getId())).thenReturn(offence);

        pleaUpdateEventListener.offencePleaUpdated(envelopeFrom(metadataWithRandomUUID("hearing.hearing-offence-plea-updated"),
                objectToJsonObjectConverter.convert(offencePleaUpdated)));

        verify(this.offenceRepository).save(offence);

        final Offence result = hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0);

        assertThat(result.getId().getId(), is(offencePleaUpdated.getOffenceId()));
        assertThat(result.getId().getHearingId(), is(offencePleaUpdated.getHearingId()));
        assertThat(result.getPlea().getPleaDate(), is(offencePleaUpdated.getPleaDate()));
        assertThat(result.getPlea().getPleaValue().toString(), is(offencePleaUpdated.getValue()));
        assertThat(result.getPlea().getDelegatedPowers().getDelegatedPowersUserId(), is(delegatedPowers.getUserId()));
        assertThat(result.getPlea().getDelegatedPowers().getDelegatedPowersFirstName(), is(delegatedPowers.getFirstName()));
        assertThat(result.getPlea().getDelegatedPowers().getDelegatedPowersLastName(), is(delegatedPowers.getLastName()));
    }
}