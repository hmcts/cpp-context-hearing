package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.core.courts.AllocationDecision.allocationDecision;
import static uk.gov.justice.core.courts.DelegatedPowers.delegatedPowers;
import static uk.gov.justice.core.courts.IndicatedPlea.indicatedPlea;
import static uk.gov.justice.core.courts.Plea.plea;
import static uk.gov.justice.core.courts.PleaModel.pleaModel;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asSet;

import uk.gov.justice.core.courts.AllocationDecision;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.IndicatedPlea;
import uk.gov.justice.core.courts.IndicatedPleaValue;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.PleaValue;
import uk.gov.justice.core.courts.Source;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.domain.event.PleaUpsert;
import uk.gov.moj.cpp.hearing.mapping.AllocationDecisionJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.IndicatedPleaJPAMapper;
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

    @Mock
    private IndicatedPleaJPAMapper indicatedPleaJPAMapper;

    @Mock
    private AllocationDecisionJPAMapper allocationDecisionJPAMapper;

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
        final Plea pleaPojo = plea()
                .withOffenceId(offenceId)
                .withPleaDate(LocalDate.now())
                .withPleaValue(PleaValue.GUILTY).build();
        final PleaUpsert offencePleaUpdated = PleaUpsert.pleaUpsert()
                .setHearingId(hearingId)
                .setPleaModel(pleaModel()
                        .withOffenceId(offenceId)
                        .withPlea(pleaPojo)
                        .build());


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

        final Plea offencePlea = offencePleaUpdated.getPleaModel().getPlea();

        assertThat(offence.getId().getId(), is(offencePlea.getOffenceId()));
        assertThat(offence.getId().getHearingId(), is(offencePleaUpdated.getHearingId()));
        assertThat(offence.getPlea().getPleaDate(), is(offencePlea.getPleaDate()));
        assertThat(offence.getPlea().getPleaValue(), is(offencePlea.getPleaValue()));
    }

    @Test
    public void pleaUpdate_shouldUpdatePlea_toToNotGuilty() {
        final UUID hearingId = randomUUID();
        final UUID offenceId = randomUUID();
        final DelegatedPowers delegatedPowers = delegatedPowers()
                .withUserId(UUID.randomUUID())
                .withLastName("David")
                .withFirstName("Bowie")
                .build();
        final Plea pleaPojo = plea()
                .withOffenceId(offenceId)
                .withPleaDate(LocalDate.now())
                .withPleaValue(PleaValue.GUILTY)
                .withDelegatedPowers(delegatedPowers)
                .build();
        final PleaUpsert offencePleaUpdated = PleaUpsert.pleaUpsert()
                .setHearingId(hearingId)
                .setPleaModel(pleaModel()
                        .withOffenceId(offenceId)
                        .withPlea(pleaPojo)
                        .build());

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

        final Plea offencePlea = offencePleaUpdated.getPleaModel().getPlea();

        assertThat(result.getId().getId(), is(offencePlea.getOffenceId()));
        assertThat(result.getId().getHearingId(), is(offencePleaUpdated.getHearingId()));
        assertThat(result.getPlea().getPleaDate(), is(offencePlea.getPleaDate()));
        assertThat(result.getPlea().getPleaValue(), is(offencePlea.getPleaValue()));
        assertThat(result.getPlea().getDelegatedPowers().getDelegatedPowersUserId(), is(delegatedPowers.getUserId()));
        assertThat(result.getPlea().getDelegatedPowers().getDelegatedPowersFirstName(), is(delegatedPowers.getFirstName()));
        assertThat(result.getPlea().getDelegatedPowers().getDelegatedPowersLastName(), is(delegatedPowers.getLastName()));
    }

    @Test
    public void pleaUpdate_shouldUpdateIndicatedPlea_toGuilty() {

        final UUID hearingId = randomUUID();
        final UUID offenceId = randomUUID();
        final IndicatedPlea indicatedPleaPojo = indicatedPlea()
                .withOffenceId(offenceId)
                .withOriginatingHearingId(hearingId)
                .withIndicatedPleaDate(LocalDate.now())
                .withSource(Source.IN_COURT)
                .withIndicatedPleaValue(IndicatedPleaValue.INDICATED_GUILTY).build();
        final PleaUpsert offencePleaUpdated = PleaUpsert.pleaUpsert()
                .setHearingId(hearingId)
                .setPleaModel(pleaModel()
                        .withOffenceId(offenceId)
                        .withIndicatedPlea(indicatedPleaPojo)
                        .build());


        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);

        final Offence offence = new Offence();
        offence.setId(new HearingSnapshotKey(offenceId, hearingId));

        final Defendant defendant = new Defendant();
        defendant.setOffences(asSet(offence));

        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        prosecutionCase.setDefendants(asSet(defendant));

        hearing.setProsecutionCases(asSet(prosecutionCase));

        final uk.gov.moj.cpp.hearing.persist.entity.ha.IndicatedPlea indicatedPlea = new uk.gov.moj.cpp.hearing.persist.entity.ha.IndicatedPlea();
        indicatedPlea.setIndicatedPleaDate(indicatedPleaPojo.getIndicatedPleaDate());
        indicatedPlea.setIndicatedPleaValue(indicatedPleaPojo.getIndicatedPleaValue());
        indicatedPlea.setIndicatedPleaSource(indicatedPleaPojo.getSource());
        when(indicatedPleaJPAMapper.toJPA(Mockito.any())).thenReturn(indicatedPlea);

        when(this.offenceRepository.findBy(offence.getId())).thenReturn(offence);

        pleaUpdateEventListener.offencePleaUpdated(envelopeFrom(metadataWithRandomUUID("hearing.hearing-offence-plea-updated"),
                objectToJsonObjectConverter.convert(offencePleaUpdated)));

        verify(this.offenceRepository).save(offence);

        final IndicatedPlea offenceIndicatedPlea = offencePleaUpdated.getPleaModel().getIndicatedPlea();

        assertThat(offence.getId().getId(), is(offenceIndicatedPlea.getOffenceId()));
        assertThat(offence.getId().getHearingId(), is(offenceIndicatedPlea.getOriginatingHearingId()));
        assertThat(offence.getIndicatedPlea().getIndicatedPleaDate(), is(offenceIndicatedPlea.getIndicatedPleaDate()));
        assertThat(offence.getIndicatedPlea().getIndicatedPleaValue(), is(offenceIndicatedPlea.getIndicatedPleaValue()));
    }

    @Test
    public void pleaUpdate_shouldUpdateAllocationDecision_toGuilty() {

        final UUID hearingId = randomUUID();
        final UUID offenceId = randomUUID();

        final AllocationDecision allocationDecisionPojo = allocationDecision()
                .withOffenceId(offenceId)
                .withOriginatingHearingId(hearingId)
                .withAllocationDecisionDate(LocalDate.now())
                .withMotReasonCode("124")
                .withSequenceNumber(10)
                .build();
        final PleaUpsert offencePleaUpdated = PleaUpsert.pleaUpsert()
                .setHearingId(hearingId)
                .setPleaModel(pleaModel()
                        .withOffenceId(offenceId)
                        .withAllocationDecision(allocationDecisionPojo)
                        .build());


        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);

        final Offence offence = new Offence();
        offence.setId(new HearingSnapshotKey(offenceId, hearingId));

        final Defendant defendant = new Defendant();
        defendant.setOffences(asSet(offence));

        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        prosecutionCase.setDefendants(asSet(defendant));

        hearing.setProsecutionCases(asSet(prosecutionCase));

        final uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision allocationDecision = new uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision();
        allocationDecision.setAllocationDecisionDate(allocationDecisionPojo.getAllocationDecisionDate());
        allocationDecision.setMotReasonCode(allocationDecisionPojo.getMotReasonCode());
        allocationDecision.setSequenceNumber(allocationDecisionPojo.getSequenceNumber());
        when(allocationDecisionJPAMapper.toJPA(Mockito.any())).thenReturn(allocationDecision);

        when(this.offenceRepository.findBy(offence.getId())).thenReturn(offence);

        pleaUpdateEventListener.offencePleaUpdated(envelopeFrom(metadataWithRandomUUID("hearing.hearing-offence-plea-updated"),
                objectToJsonObjectConverter.convert(offencePleaUpdated)));

        verify(this.offenceRepository).save(offence);

        final AllocationDecision offenceAllocationDecision = offencePleaUpdated.getPleaModel().getAllocationDecision();

        assertThat(offence.getId().getId(), is(offenceAllocationDecision.getOffenceId()));
        assertThat(offence.getId().getHearingId(), is(offenceAllocationDecision.getOriginatingHearingId()));
        assertThat(offence.getAllocationDecision().getAllocationDecisionDate(), is(offenceAllocationDecision.getAllocationDecisionDate()));
        assertThat(offence.getAllocationDecision().getMotReasonCode(), is(offenceAllocationDecision.getMotReasonCode()));
        assertThat(offence.getAllocationDecision().getSequenceNumber(),is(offenceAllocationDecision.getSequenceNumber()));
    }

    @Test
    public void pleaUpdate_shouldUpdateAll() {

        final UUID hearingId = randomUUID();
        final UUID offenceId = randomUUID();
        final DelegatedPowers delegatedPowers = delegatedPowers()
                .withUserId(UUID.randomUUID())
                .withLastName("David")
                .withFirstName("Bowie")
                .build();
        final Plea pleaPojo = plea()
                .withOffenceId(offenceId)
                .withPleaDate(LocalDate.now())
                .withPleaValue(PleaValue.GUILTY)
                .withDelegatedPowers(delegatedPowers)
                .build();
        final IndicatedPlea indicatedPleaPojo = indicatedPlea()
                .withOffenceId(offenceId)
                .withOriginatingHearingId(hearingId)
                .withIndicatedPleaDate(LocalDate.now())
                .withSource(Source.IN_COURT)
                .withIndicatedPleaValue(IndicatedPleaValue.INDICATED_GUILTY).build();
        final AllocationDecision allocationDecisionPojo = allocationDecision()
                .withOffenceId(offenceId)
                .withOriginatingHearingId(hearingId)
                .withAllocationDecisionDate(LocalDate.now())
                .withMotReasonCode("124")
                .withSequenceNumber(10)
                .build();

        final PleaUpsert offencePleaUpdated = PleaUpsert.pleaUpsert()
                .setHearingId(hearingId)
                .setPleaModel(pleaModel()
                        .withOffenceId(offenceId)
                        .withPlea(pleaPojo)
                        .withIndicatedPlea(indicatedPleaPojo)
                        .withAllocationDecision(allocationDecisionPojo)
                        .build());


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

        final uk.gov.moj.cpp.hearing.persist.entity.ha.IndicatedPlea indicatedPlea = new uk.gov.moj.cpp.hearing.persist.entity.ha.IndicatedPlea();
        indicatedPlea.setIndicatedPleaDate(indicatedPleaPojo.getIndicatedPleaDate());
        indicatedPlea.setIndicatedPleaValue(indicatedPleaPojo.getIndicatedPleaValue());
        indicatedPlea.setIndicatedPleaSource(indicatedPleaPojo.getSource());
        when(indicatedPleaJPAMapper.toJPA(Mockito.any())).thenReturn(indicatedPlea);

        final uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision allocationDecision = new uk.gov.moj.cpp.hearing.persist.entity.ha.AllocationDecision();
        allocationDecision.setAllocationDecisionDate(allocationDecisionPojo.getAllocationDecisionDate());
        allocationDecision.setMotReasonCode(allocationDecisionPojo.getMotReasonCode());
        allocationDecision.setSequenceNumber(allocationDecisionPojo.getSequenceNumber());
        when(allocationDecisionJPAMapper.toJPA(Mockito.any())).thenReturn(allocationDecision);

        when(this.offenceRepository.findBy(offence.getId())).thenReturn(offence);

        pleaUpdateEventListener.offencePleaUpdated(envelopeFrom(metadataWithRandomUUID("hearing.hearing-offence-plea-updated"),
                objectToJsonObjectConverter.convert(offencePleaUpdated)));

        verify(this.offenceRepository).save(offence);

        final Plea offencePlea = offencePleaUpdated.getPleaModel().getPlea();
        final AllocationDecision offenceAllocationDecision = offencePleaUpdated.getPleaModel().getAllocationDecision();
        final IndicatedPlea offenceIndicatedPlea = offencePleaUpdated.getPleaModel().getIndicatedPlea();

        assertThat(offence.getId().getId(), is(offenceAllocationDecision.getOffenceId()));
        assertThat(offence.getId().getHearingId(), is(offenceAllocationDecision.getOriginatingHearingId()));
        assertThat(offence.getAllocationDecision().getAllocationDecisionDate(), is(offenceAllocationDecision.getAllocationDecisionDate()));
        assertThat(offence.getAllocationDecision().getMotReasonCode(), is(offenceAllocationDecision.getMotReasonCode()));
        assertThat(offence.getAllocationDecision().getSequenceNumber(), is(offenceAllocationDecision.getSequenceNumber()));
        assertThat(offence.getIndicatedPlea().getIndicatedPleaDate(), is(offenceIndicatedPlea.getIndicatedPleaDate()));
        assertThat(offence.getIndicatedPlea().getIndicatedPleaValue(), is(offenceIndicatedPlea.getIndicatedPleaValue()));
        assertThat(offence.getPlea().getPleaDate(), is(offencePlea.getPleaDate()));
        assertThat(offence.getPlea().getPleaValue(), is(offencePlea.getPleaValue()));
        assertThat(offence.getPlea().getDelegatedPowers().getDelegatedPowersUserId(), is(delegatedPowers.getUserId()));
        assertThat(offence.getPlea().getDelegatedPowers().getDelegatedPowersFirstName(), is(delegatedPowers.getFirstName()));
        assertThat(offence.getPlea().getDelegatedPowers().getDelegatedPowersLastName(), is(delegatedPowers.getLastName()));
    }
}