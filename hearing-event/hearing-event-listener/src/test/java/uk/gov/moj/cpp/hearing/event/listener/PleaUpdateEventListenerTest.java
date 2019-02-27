package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asSet;

import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.PleaValue;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated;
import uk.gov.moj.cpp.hearing.mapping.PleaJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PleaUpdateEventListenerTest {

    @Mock
    private OffenceRepository offenceRepository;

    @Mock
    private PleaJPAMapper pleaJpaMapper;

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
    public void pleaUpdate_shouldUpdatePlea_toGuilty() {

        final UUID hearingId = randomUUID();
        final UUID offenceId = randomUUID();
        final Plea pleaPojo = Plea.plea()
                .withOffenceId(offenceId)
                .withPleaDate(LocalDate.now())
                .withPleaValue(PleaValue.GUILTY).build();
        final OffencePleaUpdated offencePleaUpdated = OffencePleaUpdated.builder()
                .withHearingId(hearingId)
                .withPlea(pleaPojo)
                .build();

        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);

        final Offence offence = new Offence();
        offence.setId(new HearingSnapshotKey(offenceId, hearingId));

        final Defendant defendant = new Defendant();
        defendant.setOffences(asSet(offence));

        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        prosecutionCase.setDefendants(asSet(defendant));

        hearing.setProsecutionCases(asSet(prosecutionCase));

        final uk.gov.moj.cpp.hearing.persist.entity.ha.Plea plea = new uk.gov.moj.cpp.hearing.persist.entity.ha.Plea();
        plea.setPleaDate(pleaPojo.getPleaDate());
        plea.setPleaValue(pleaPojo.getPleaValue());
        plea.setDelegatedPowers(null);
        when(pleaJpaMapper.toJPA(Mockito.any())).thenReturn(plea);

        when(this.offenceRepository.findBy(offence.getId())).thenReturn(offence);

        pleaUpdateEventListener.offencePleaUpdated(envelopeFrom(metadataWithRandomUUID("hearing.hearing-offence-plea-updated"),
                objectToJsonObjectConverter.convert(offencePleaUpdated)));

        verify(this.offenceRepository).save(offence);

        assertThat(offence.getId().getId(), is(offencePleaUpdated.getPlea().getOffenceId()));
        assertThat(offence.getId().getHearingId(), is(offencePleaUpdated.getHearingId()));
        assertThat(offence.getPlea().getPleaDate(), is(offencePleaUpdated.getPlea().getPleaDate()));
        assertThat(offence.getPlea().getPleaValue(), is(offencePleaUpdated.getPlea().getPleaValue()));
    }

    @Test
    public void pleaUpdate_shouldUpdatePlea_toToNotGuilty() {
        final UUID hearingId = randomUUID();
        final UUID offenceId = randomUUID();
        final DelegatedPowers delegatedPowers = DelegatedPowers.delegatedPowers()
                .withUserId(UUID.randomUUID())
                .withLastName("David")
                .withFirstName("Bowie")
                .build();
        final Plea pleaPojo = Plea.plea()
                .withOffenceId(offenceId)
                .withPleaDate(LocalDate.now())
                .withPleaValue(PleaValue.GUILTY)
                .withDelegatedPowers(delegatedPowers)
                .build();
        final OffencePleaUpdated offencePleaUpdated = OffencePleaUpdated.builder()
                .withHearingId(hearingId)
                .withPlea(pleaPojo)
                .build();

        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);

        final Offence offence = new Offence();
        offence.setId(new HearingSnapshotKey(offenceId, hearingId));

        final Defendant defendant = new Defendant();
        defendant.setOffences(asSet(offence));

        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        prosecutionCase.setDefendants(asSet(defendant));

        hearing.setProsecutionCases(asSet(prosecutionCase));

        final uk.gov.moj.cpp.hearing.persist.entity.ha.Plea plea = new uk.gov.moj.cpp.hearing.persist.entity.ha.Plea();
        plea.setPleaDate(pleaPojo.getPleaDate());
        plea.setPleaValue(pleaPojo.getPleaValue());

        final uk.gov.moj.cpp.hearing.persist.entity.ha.DelegatedPowers delegatedPowersEntity = new uk.gov.moj.cpp.hearing.persist.entity.ha.DelegatedPowers();
        delegatedPowersEntity.setDelegatedPowersFirstName(delegatedPowers.getFirstName());
        delegatedPowersEntity.setDelegatedPowersLastName(delegatedPowers.getLastName());
        delegatedPowersEntity.setDelegatedPowersUserId(delegatedPowers.getUserId());
        plea.setDelegatedPowers(delegatedPowersEntity);

        when(pleaJpaMapper.toJPA(Mockito.any())).thenReturn(plea);

        when(this.offenceRepository.findBy(offence.getId())).thenReturn(offence);

        pleaUpdateEventListener.offencePleaUpdated(envelopeFrom(metadataWithRandomUUID("hearing.hearing-offence-plea-updated"),
                objectToJsonObjectConverter.convert(offencePleaUpdated)));

        verify(this.offenceRepository).save(offence);

        final Offence result = hearing.getProsecutionCases().iterator().next().getDefendants().iterator().next().getOffences().iterator().next();

        assertThat(result.getId().getId(), is(offencePleaUpdated.getPlea().getOffenceId()));
        assertThat(result.getId().getHearingId(), is(offencePleaUpdated.getHearingId()));
        assertThat(result.getPlea().getPleaDate(), is(offencePleaUpdated.getPlea().getPleaDate()));
        assertThat(result.getPlea().getPleaValue(), is(offencePleaUpdated.getPlea().getPleaValue()));
        assertThat(result.getPlea().getDelegatedPowers().getDelegatedPowersUserId(), is(delegatedPowers.getUserId()));
        assertThat(result.getPlea().getDelegatedPowers().getDelegatedPowersFirstName(), is(delegatedPowers.getFirstName()));
        assertThat(result.getPlea().getDelegatedPowers().getDelegatedPowersLastName(), is(delegatedPowers.getLastName()));
    }
}