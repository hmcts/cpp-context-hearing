package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimumInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.domain.event.ApplicationDetailChanged;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.ExistingHearingUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingExtended;
import uk.gov.moj.cpp.hearing.domain.event.InheritedPlea;
import uk.gov.moj.cpp.hearing.mapping.CourtCentreJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.PleaJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.ProsecutionCaseJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DelegatedPowers;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Plea;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;
import uk.gov.moj.cpp.hearing.repository.ProsecutionCaseRepository;

import java.io.StringReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InitiateHearingEventListenerTest {
    private static final String GUILTY = "GUILTY";

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private HearingJPAMapper hearingJPAMapper;

    @Mock
    private CourtCentreJPAMapper courtCentreJPAMapper;

    @Mock
    private PleaJPAMapper pleaJPAMapper;

    @Mock
    private ProsecutionCaseRepository prosecutionCaseRepository;

    @Mock
    private ProsecutionCaseJPAMapper prosecutionCaseJPAMapper;

    @Mock
    private OffenceRepository offenceRepository;

    @InjectMocks
    private InitiateHearingEventListener initiateHearingEventListener;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        reset(prosecutionCaseJPAMapper, prosecutionCaseRepository, hearingJPAMapper, offenceRepository, hearingRepository, offenceRepository);
    }

    @Test
    public void shouldInsertHearingWhenInitiated() {

        final InitiateHearingCommand command = minimumInitiateHearingTemplate();

        final uk.gov.justice.core.courts.Hearing hearing = command.getHearing();

        when(hearingJPAMapper.toJPA(any(uk.gov.justice.core.courts.Hearing.class))).thenReturn(new Hearing());

        initiateHearingEventListener.newHearingInitiated(getInitiateHearingJsonEnvelope(hearing));

        final ArgumentCaptor<Hearing> hearingExArgumentCaptor = ArgumentCaptor.forClass(Hearing.class);

        verify(hearingRepository, times(1)).save(hearingExArgumentCaptor.capture());
    }

    @Test
    public void shouldExtendHearing() {

        final List<ProsecutionCase> prosecutionCases = new ArrayList<>();
        prosecutionCases.add(ProsecutionCase.prosecutionCase().withId(UUID.randomUUID()).build());
        final HearingExtended hearingExtended = new HearingExtended(UUID.randomUUID(),null, null, null, CourtApplication.courtApplication().withId(UUID.randomUUID()).build(), prosecutionCases, null);

        Hearing hearing = new Hearing();
        hearing.setCourtApplicationsJson("zyz");
        final String expectedUpdatedCourtApplicationJson = "abcdef";
        final Set<uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase> prosecutionCaseSet = new HashSet<>();
        uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase prosecutionCaseEntitiy = new uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase();
        prosecutionCaseSet.add(prosecutionCaseEntitiy);
        when(hearingRepository.findBy(hearingExtended.getHearingId())).thenReturn(hearing);
        when(hearingJPAMapper.addOrUpdateCourtApplication(hearing.getCourtApplicationsJson(), hearingExtended.getCourtApplication())).thenReturn(expectedUpdatedCourtApplicationJson);
        when(prosecutionCaseJPAMapper.toJPA(any(hearing.getClass()), any(ProsecutionCase.class))).thenReturn(prosecutionCaseEntitiy);
        when(prosecutionCaseRepository.save(any())).thenReturn(prosecutionCaseEntitiy);
        initiateHearingEventListener.hearingExtended(envelopeFrom((Metadata) null, objectToJsonObjectConverter.convert(hearingExtended)));
        final ArgumentCaptor<Hearing> hearingExArgumentCaptor = ArgumentCaptor.forClass(Hearing.class);

        verify(hearingRepository, times(1)).save(hearingExArgumentCaptor.capture());
        verify(prosecutionCaseRepository).save(any());

        final String updatedCourtApplicationsJson = hearingExArgumentCaptor.getValue().getCourtApplicationsJson();
        assertThat(updatedCourtApplicationsJson, is(expectedUpdatedCourtApplicationJson));
    }

    @Test
    public void shouldAddProsecutionCaseToTheExistingHearingWhenProsecutionCaseIsNotPresentInTheHearing() {

        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId1 = randomUUID();
        final UUID prosecutionCaseId2 = randomUUID();
        final UUID defendantId1 = randomUUID();
        final UUID defendantId2 = randomUUID();
        final UUID offenceId1 = randomUUID();
        final UUID offenceId2 = randomUUID();

        final ProsecutionCase prosecutionCaseInEvent = createProsecutionCase(prosecutionCaseId1, defendantId1, offenceId1);
        final List<ProsecutionCase> prosecutionCasesInEvent = new ArrayList<>();
        prosecutionCasesInEvent.add(prosecutionCaseInEvent);

        final HearingExtended hearingExtended = new HearingExtended(hearingId, null, null, null,null, prosecutionCasesInEvent, null);

        final Set<Offence> offencesEntities = new HashSet<>();
        final Offence offenceEntity = new Offence();
        offenceEntity.setId(new HearingSnapshotKey(offenceId2, hearingId));
        offencesEntities.add(offenceEntity);

        final Set<uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant> defendantsEntities = new HashSet<>();
        final uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant defendantEntity = new uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant();
        defendantEntity.setId(new HearingSnapshotKey(defendantId2, hearingId));
        defendantEntity.setOffences(offencesEntities);
        defendantsEntities.add(defendantEntity);

        final Set<uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase> prosecutionCasesEntities = new HashSet<>();
        final uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase prosecutionCaseEntity = new uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase();
        prosecutionCaseEntity.setId(new HearingSnapshotKey(prosecutionCaseId2, hearingId));
        prosecutionCaseEntity.setDefendants(defendantsEntities);
        prosecutionCasesEntities.add(prosecutionCaseEntity);

        final ProsecutionCase prosecutionCaseInEntity = createProsecutionCase(prosecutionCaseId2, defendantId2, offenceId2);
        final List<ProsecutionCase> prosecutionCasesInEntity = new ArrayList<>();
        prosecutionCasesInEntity.add(prosecutionCaseInEntity);

        final Hearing hearing = new Hearing();
        hearing.setProsecutionCases(prosecutionCasesEntities);

        when(hearingRepository.findBy(hearingExtended.getHearingId())).thenReturn(hearing);
        when(prosecutionCaseJPAMapper.toJPA(any(hearing.getClass()), any(ProsecutionCase.class))).thenReturn(prosecutionCaseEntity);
        when(prosecutionCaseJPAMapper.fromJPA(anySet())).thenReturn(prosecutionCasesInEntity);
        when(prosecutionCaseRepository.save(any())).thenReturn(prosecutionCaseEntity);

        initiateHearingEventListener.hearingExtended(envelopeFrom((Metadata) null, objectToJsonObjectConverter.convert(hearingExtended)));

        final ArgumentCaptor<Hearing> hearingExArgumentCaptor = ArgumentCaptor.forClass(Hearing.class);
        final ArgumentCaptor<ProsecutionCase> prosecutionCaseArgumentCaptor = ArgumentCaptor.forClass(ProsecutionCase.class);

        verify(prosecutionCaseJPAMapper, times(1)).toJPA(hearingExArgumentCaptor.capture(), prosecutionCaseArgumentCaptor.capture());
        verify(prosecutionCaseRepository).save(any());

        final ProsecutionCase prosecutionCase = prosecutionCaseArgumentCaptor.getValue();

        assertThat(prosecutionCase.getDefendants().size(), is(1));
        assertThat(prosecutionCase.getId(), is(prosecutionCaseId1));
        assertThat(prosecutionCase.getDefendants().size(), is(1));

        assertThat(prosecutionCase.getDefendants().get(0).getId(), is(defendantId1));
        assertThat(prosecutionCase.getDefendants().get(0).getOffences().size(), is(1));
        assertThat(prosecutionCase.getDefendants().get(0).getOffences().get(0).getId(), is(offenceId1));

    }

    @Test
    public void shouldUpdateProsecutionCasesInExistingHearing() {

        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();

        final ProsecutionCase prosecutionCaseInEvent = createProsecutionCase(prosecutionCaseId, defendantId, offenceId);
        final List<ProsecutionCase> prosecutionCasesInEvent = new ArrayList<>();
        prosecutionCasesInEvent.add(prosecutionCaseInEvent);

        final ExistingHearingUpdated existingHearingUpdated = new ExistingHearingUpdated(hearingId, prosecutionCasesInEvent, null);

        final Set<Offence> offencesEntities = new HashSet<>();
        final Offence offenceEntity = new Offence();
        offenceEntity.setId(new HearingSnapshotKey(offenceId, hearingId));
        offencesEntities.add(offenceEntity);

        final Set<uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant> defendantsEntities = new HashSet<>();
        final uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant defendantEntity = new uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant();
        defendantEntity.setId(new HearingSnapshotKey(defendantId, hearingId));
        defendantEntity.setOffences(offencesEntities);
        defendantsEntities.add(defendantEntity);

        final Set<uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase> prosecutionCasesEntities = new HashSet<>();
        final uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase prosecutionCaseEntity = new uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase();
        prosecutionCaseEntity.setId(new HearingSnapshotKey(prosecutionCaseId, hearingId));
        prosecutionCaseEntity.setDefendants(defendantsEntities);
        prosecutionCasesEntities.add(prosecutionCaseEntity);

        final ProsecutionCase prosecutionCaseInEntity = createProsecutionCase(prosecutionCaseId, defendantId, offenceId);
        final List<ProsecutionCase> prosecutionCasesInEntity = new ArrayList<>();
        prosecutionCasesInEntity.add(prosecutionCaseInEntity);

        final Hearing hearing = new Hearing();
        hearing.setProsecutionCases(prosecutionCasesEntities);

        when(hearingRepository.findBy(existingHearingUpdated.getHearingId())).thenReturn(hearing);
        when(prosecutionCaseJPAMapper.toJPA(any(hearing.getClass()), any(ProsecutionCase.class))).thenReturn(prosecutionCaseEntity);
        when(prosecutionCaseJPAMapper.fromJPA(anySet())).thenReturn(prosecutionCasesInEntity);
        when(prosecutionCaseRepository.save(any())).thenReturn(prosecutionCaseEntity);

        initiateHearingEventListener.handleExistingHearingUpdatedEvent(envelopeFrom((Metadata) null, objectToJsonObjectConverter.convert(existingHearingUpdated)));

        final ArgumentCaptor<Hearing> hearingExArgumentCaptor = ArgumentCaptor.forClass(Hearing.class);
        final ArgumentCaptor<ProsecutionCase> prosecutionCaseArgumentCaptor = ArgumentCaptor.forClass(ProsecutionCase.class);

        verify(prosecutionCaseJPAMapper, times(1)).toJPA(hearingExArgumentCaptor.capture(), prosecutionCaseArgumentCaptor.capture());
        verify(prosecutionCaseRepository).save(any());

        final ProsecutionCase prosecutionCase = prosecutionCaseArgumentCaptor.getValue();

        assertThat(prosecutionCase.getDefendants().size(), is(1));
        assertThat(prosecutionCase.getId(), is(prosecutionCaseId));
        assertThat(prosecutionCase.getDefendants().size(), is(1));

        assertThat(prosecutionCase.getDefendants().get(0).getId(), is(defendantId));
        assertThat(prosecutionCase.getDefendants().get(0).getOffences().size(), is(1));
        assertThat(prosecutionCase.getDefendants().get(0).getOffences().get(0).getId(), is(offenceId));

    }

    @Test
    public void shouldAddDefendantToTheExistingProsecutionCaseWhenDefendantIsNotPresentInTheProsecutionCase() {

        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final UUID defendantId1 = randomUUID();
        final UUID defendantId2 = randomUUID();
        final UUID offenceId1 = randomUUID();
        final UUID offenceId2 = randomUUID();

        final ProsecutionCase prosecutionCaseInEvent = createProsecutionCase(prosecutionCaseId, defendantId1, offenceId1);
        final List<ProsecutionCase> prosecutionCasesInEvent = new ArrayList<>();
        prosecutionCasesInEvent.add(prosecutionCaseInEvent);

        final HearingExtended hearingExtended = new HearingExtended(hearingId, null, null, null,null, prosecutionCasesInEvent, null);

        final Set<Offence> offencesEntities = new HashSet<>();
        final Offence offenceEntity = new Offence();
        offenceEntity.setId(new HearingSnapshotKey(offenceId2, hearingId));
        offencesEntities.add(offenceEntity);

        final Set<uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant> defendantsEntities = new HashSet<>();
        final uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant defendantEntity = new uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant();
        defendantEntity.setId(new HearingSnapshotKey(defendantId2, hearingId));
        defendantEntity.setOffences(offencesEntities);
        defendantsEntities.add(defendantEntity);

        final Set<uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase> prosecutionCasesEntities = new HashSet<>();
        final uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase prosecutionCaseEntity = new uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase();
        prosecutionCaseEntity.setId(new HearingSnapshotKey(prosecutionCaseId, hearingId));
        prosecutionCaseEntity.setDefendants(defendantsEntities);
        prosecutionCasesEntities.add(prosecutionCaseEntity);

        final ProsecutionCase prosecutionCaseInEntity = createProsecutionCase(prosecutionCaseId, defendantId2, offenceId2);
        final List<ProsecutionCase> prosecutionCasesInEntity = new ArrayList<>();
        prosecutionCasesInEntity.add(prosecutionCaseInEntity);

        final Hearing hearing = new Hearing();
        hearing.setProsecutionCases(prosecutionCasesEntities);

        when(hearingRepository.findBy(hearingExtended.getHearingId())).thenReturn(hearing);
        when(prosecutionCaseJPAMapper.toJPA(any(hearing.getClass()), any(ProsecutionCase.class))).thenReturn(prosecutionCaseEntity);
        when(prosecutionCaseJPAMapper.fromJPA(anySet())).thenReturn(prosecutionCasesInEntity);
        when(prosecutionCaseRepository.save(any())).thenReturn(prosecutionCaseEntity);

        initiateHearingEventListener.hearingExtended(envelopeFrom((Metadata) null, objectToJsonObjectConverter.convert(hearingExtended)));

        final ArgumentCaptor<Hearing> hearingExArgumentCaptor = ArgumentCaptor.forClass(Hearing.class);
        final ArgumentCaptor<ProsecutionCase> prosecutionCaseArgumentCaptor = ArgumentCaptor.forClass(ProsecutionCase.class);

        verify(prosecutionCaseJPAMapper, times(1)).toJPA(hearingExArgumentCaptor.capture(), prosecutionCaseArgumentCaptor.capture());
        verify(prosecutionCaseRepository).save(any());

        final ProsecutionCase prosecutionCase = prosecutionCaseArgumentCaptor.getValue();

        assertThat(prosecutionCase.getId(), is(prosecutionCaseId));
        assertThat(prosecutionCase.getDefendants().size(), is(2));

        assertThat(prosecutionCase.getDefendants().get(0).getId(), is(defendantId2));
        assertThat(prosecutionCase.getDefendants().get(0).getOffences().size(), is(1));
        assertThat(prosecutionCase.getDefendants().get(0).getOffences().get(0).getId(), is(offenceId2));

        assertThat(prosecutionCase.getDefendants().get(1).getId(), is(defendantId1));
        assertThat(prosecutionCase.getDefendants().get(1).getOffences().size(), is(1));
        assertThat(prosecutionCase.getDefendants().get(1).getOffences().get(0).getId(), is(offenceId1));

    }

    @Test
    public void shouldAddOffenceToTheExistingDefendantWhenOffenceIsNotPresentInTheDefendant() {

        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();

        final ProsecutionCase prosecutionCaseInEvent = createProsecutionCase(prosecutionCaseId, defendantId, offenceId);
        final List<ProsecutionCase> prosecutionCasesInEvent = new ArrayList<>();
        prosecutionCasesInEvent.add(prosecutionCaseInEvent);

        final HearingExtended hearingExtended = new HearingExtended(hearingId, null, null, null,null, prosecutionCasesInEvent, null);

        final Set<Offence> offencesEntities = new HashSet<>();
        final Offence offenceEntity = new Offence();
        offenceEntity.setId(new HearingSnapshotKey(offenceId, hearingId));
        offencesEntities.add(offenceEntity);

        final Set<uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant> defendantsEntities = new HashSet<>();
        final uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant defendantEntity = new uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant();
        defendantEntity.setId(new HearingSnapshotKey(defendantId, hearingId));
        defendantEntity.setOffences(offencesEntities);
        defendantsEntities.add(defendantEntity);

        final Set<uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase> prosecutionCasesEntities = new HashSet<>();
        final uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase prosecutionCaseEntity = new uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase();
        prosecutionCaseEntity.setId(new HearingSnapshotKey(prosecutionCaseId, hearingId));
        prosecutionCaseEntity.setDefendants(defendantsEntities);
        prosecutionCasesEntities.add(prosecutionCaseEntity);

        final ProsecutionCase prosecutionCaseInEntity = createProsecutionCase(prosecutionCaseId, defendantId, offenceId);
        final List<ProsecutionCase> prosecutionCasesInEntity = new ArrayList<>();
        prosecutionCasesInEntity.add(prosecutionCaseInEntity);

        final Hearing hearing = new Hearing();
        hearing.setProsecutionCases(prosecutionCasesEntities);

        when(hearingRepository.findBy(hearingExtended.getHearingId())).thenReturn(hearing);
        when(prosecutionCaseJPAMapper.toJPA(any(hearing.getClass()), any(ProsecutionCase.class))).thenReturn(prosecutionCaseEntity);
        when(prosecutionCaseJPAMapper.fromJPA(anySet())).thenReturn(prosecutionCasesInEntity);
        when(prosecutionCaseRepository.save(any())).thenReturn(prosecutionCaseEntity);

        initiateHearingEventListener.hearingExtended(envelopeFrom((Metadata) null, objectToJsonObjectConverter.convert(hearingExtended)));

        final ArgumentCaptor<Hearing> hearingExArgumentCaptor = ArgumentCaptor.forClass(Hearing.class);
        final ArgumentCaptor<ProsecutionCase> prosecutionCaseArgumentCaptor = ArgumentCaptor.forClass(ProsecutionCase.class);

        verify(prosecutionCaseJPAMapper, times(1)).toJPA(hearingExArgumentCaptor.capture(), prosecutionCaseArgumentCaptor.capture());
        verify(prosecutionCaseRepository).save(any());

        final ProsecutionCase prosecutionCase = prosecutionCaseArgumentCaptor.getValue();
        assertThat(prosecutionCase.getId(), is(prosecutionCaseId));
        assertThat(prosecutionCase.getRemovalReason(), is("removal reason"));
        assertThat(prosecutionCase.getDefendants().size(), is(1));
        assertThat(prosecutionCase.getDefendants().get(0).getId(), is(defendantId));
        assertThat(prosecutionCase.getDefendants().get(0).getWitnessStatement(), is("witness statement"));

        assertThat(prosecutionCase.getDefendants().get(0).getOffences().size(), is(1));
        assertThat(prosecutionCase.getDefendants().get(0).getOffences().get(0).getId(), is(offenceId));
        assertThat(prosecutionCase.getDefendants().get(0).getOffences().get(0).getOffenceLegislation(), is("offence legislation"));

    }

    private ProsecutionCase createProsecutionCase(final UUID prosecutionCaseId, final UUID defendantId, final UUID offenceId) {
        final List<Defendant> defendants = new ArrayList<>();
        defendants.add(createDefendant(defendantId, offenceId));
        return ProsecutionCase.prosecutionCase()
                .withId(prosecutionCaseId)
                .withRemovalReason("removal reason")
                .withDefendants(defendants)
                .build();
    }

    private Defendant createDefendant(final UUID defendantId, final UUID offenceId) {
        final List<uk.gov.justice.core.courts.Offence> offences = new ArrayList<>();
        offences.add(createOffence(offenceId));
        return Defendant.defendant()
                .withId(defendantId)
                .withWitnessStatement("witness statement")
                .withOffences(offences)
                .build();
    }

    private uk.gov.justice.core.courts.Offence createOffence(final UUID offenceId) {
        return uk.gov.justice.core.courts.Offence.offence()
                .withId(offenceId)
                .withOffenceLegislation("offence legislation")
                .build();
    }

    @Test
    public void shouldExtendHearing_whenProsecutionCaseIsNull() {

        final HearingExtended hearingExtended = new HearingExtended(UUID.randomUUID(), null, null, null,CourtApplication.courtApplication().withId(UUID.randomUUID()).build(), null, null);

        Hearing hearing = new Hearing();
        hearing.setCourtApplicationsJson("zyz");
        final String expectedUpdatedCourtApplicationJson = "abcdef";
        final Set<uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase> prosecutionCaseSet = new HashSet<>();
        uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase prosecutionCaseEntitiy = new uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase();
        prosecutionCaseSet.add(prosecutionCaseEntitiy);
        when(hearingRepository.findBy(hearingExtended.getHearingId())).thenReturn(hearing);
        when(hearingJPAMapper.addOrUpdateCourtApplication(hearing.getCourtApplicationsJson(), hearingExtended.getCourtApplication())).thenReturn(expectedUpdatedCourtApplicationJson);
        initiateHearingEventListener.hearingExtended(envelopeFrom((Metadata) null, objectToJsonObjectConverter.convert(hearingExtended)));
        final ArgumentCaptor<Hearing> hearingExArgumentCaptor = ArgumentCaptor.forClass(Hearing.class);

        verify(hearingRepository, times(1)).save(hearingExArgumentCaptor.capture());
        verify(prosecutionCaseRepository,never()).save(any());

        final String updatedCourtApplicationsJson = hearingExArgumentCaptor.getValue().getCourtApplicationsJson();
        assertThat(updatedCourtApplicationsJson, is(expectedUpdatedCourtApplicationJson));
    }

    @Test
    public void shouldUpdateApplicationDetails() {

        final ApplicationDetailChanged applicationDetailChanged = new ApplicationDetailChanged(UUID.randomUUID(), CourtApplication.courtApplication().withId(UUID.randomUUID()).build());

        Hearing hearing = new Hearing();
        hearing.setCourtApplicationsJson("zyz");
        final String expectedUpdatedCourtApplicationJson = "abcdef";
        when(hearingRepository.findBy(applicationDetailChanged.getHearingId())).thenReturn(hearing);
        when(hearingJPAMapper.addOrUpdateCourtApplication(hearing.getCourtApplicationsJson(), applicationDetailChanged.getCourtApplication())).thenReturn(expectedUpdatedCourtApplicationJson);

        initiateHearingEventListener.hearingApplicationDetailChanged(envelopeFrom((Metadata) null, objectToJsonObjectConverter.convert(applicationDetailChanged)));
        final ArgumentCaptor<Hearing> hearingExArgumentCaptor = ArgumentCaptor.forClass(Hearing.class);

        verify(hearingRepository, times(1)).save(hearingExArgumentCaptor.capture());
        final String updatedCourtApplicationsJson = hearingExArgumentCaptor.getValue().getCourtApplicationsJson();
        assertThat(updatedCourtApplicationsJson, is(expectedUpdatedCourtApplicationJson));
    }

    @Test
    public void convictionDateUpdated_shouldUpdateTheConvictionDate() {

        final UUID caseId = randomUUID();
        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final HearingSnapshotKey snapshotKey = new HearingSnapshotKey(offenceId, hearingId);
        final ConvictionDateAdded convictionDateAdded = new ConvictionDateAdded(caseId, hearingId, offenceId, PAST_LOCAL_DATE.next(), null);

        final Offence offence = new Offence();
        offence.setId(snapshotKey);

        when(this.offenceRepository.findBy(snapshotKey)).thenReturn(offence);

        initiateHearingEventListener.convictionDateUpdated(envelopeFrom(metadataWithRandomUUID("hearing.conviction-date-added"),
                objectToJsonObjectConverter.convert(convictionDateAdded)));

        verify(this.offenceRepository).saveAndFlush(offence);

        assertThat(offence.getId().getId(), is(convictionDateAdded.getOffenceId()));
        assertThat(offence.getConvictionDate(), is(convictionDateAdded.getConvictionDate()));
    }

    @Test
    public void convictionDateUpdated_shouldUpdateTheConvictionDateToOffenceUnderCourtApplication() {

        final UUID applicationId = randomUUID();
        final UUID hearingId = randomUUID();
        final ConvictionDateAdded convictionDateAdded = ConvictionDateAdded.convictionDateAdded()
                .setCourtApplicationId(applicationId)
                .setHearingId(hearingId)
                .setOffenceId(randomUUID())
                .setConvictionDate(PAST_LOCAL_DATE.next());

        final Hearing hearing = new Hearing();
        hearing.setCourtApplicationsJson("abc");


        when(this.hearingRepository.findBy(convictionDateAdded.getHearingId())).thenReturn(hearing);
        when(hearingJPAMapper.updateConvictedDateOnOffencesInCourtApplication(hearing.getCourtApplicationsJson(), convictionDateAdded.getCourtApplicationId(), convictionDateAdded.getOffenceId(), convictionDateAdded.getConvictionDate())).thenReturn("def");

        initiateHearingEventListener.convictionDateUpdated(envelopeFrom(metadataWithRandomUUID("hearing.conviction-date-added"),
                objectToJsonObjectConverter.convert(convictionDateAdded)));

        verify(this.hearingRepository).save(hearing);
        assertThat(hearing.getCourtApplicationsJson(), is("def"));

    }

    @Test
    public void convictionDateUpdated_shouldUpdateTheConvictionDateToCourtApplication() {

        final UUID applicationId = randomUUID();
        final UUID hearingId = randomUUID();
        final ConvictionDateAdded convictionDateAdded = ConvictionDateAdded.convictionDateAdded()
                .setCourtApplicationId(applicationId)
                .setHearingId(hearingId)
                .setConvictionDate(PAST_LOCAL_DATE.next());

        final Hearing hearingEntity = new Hearing();
        hearingEntity.setCourtApplicationsJson("abc");

        final uk.gov.justice.core.courts.Hearing hearing = uk.gov.justice.core.courts.Hearing.hearing()
                .withCourtApplications(Collections.singletonList(CourtApplication.courtApplication().withId(applicationId).build()))
                .build();

        when(this.hearingRepository.findBy(convictionDateAdded.getHearingId())).thenReturn(hearingEntity);
        when(hearingJPAMapper.fromJPA(any())).thenReturn(hearing);
        when(hearingJPAMapper.addOrUpdateCourtApplication(any(),any() )).thenReturn("def");

        initiateHearingEventListener.convictionDateUpdated(envelopeFrom(metadataWithRandomUUID("hearing.conviction-date-added"),
                objectToJsonObjectConverter.convert(convictionDateAdded)));

        verify(this.hearingRepository).save(hearingEntity);
        assertThat(hearingEntity.getCourtApplicationsJson(), is("def"));

        final ArgumentCaptor<CourtApplication> courtApplicationArgumentCaptor = ArgumentCaptor.forClass(CourtApplication.class);
        verify(hearingJPAMapper).addOrUpdateCourtApplication(any(), courtApplicationArgumentCaptor.capture());
        final CourtApplication updatedCourtApplication = courtApplicationArgumentCaptor.getValue();
        assertThat(updatedCourtApplication.getConvictionDate(), is(convictionDateAdded.getConvictionDate()));
    }

    @Test
    public void convictionDateRemoves_shouldRemoveTheConvictionDateFromCourtApplication() {

        final UUID applicationId = randomUUID();
        final UUID hearingId = randomUUID();
        final ConvictionDateRemoved convictionDateRemoved = ConvictionDateRemoved.convictionDateRemoved()
                .setCourtApplicationId(applicationId)
                .setHearingId(hearingId);

        final Hearing hearingEntity = new Hearing();
        hearingEntity.setCourtApplicationsJson("abc");

        final uk.gov.justice.core.courts.Hearing hearing = uk.gov.justice.core.courts.Hearing.hearing()
                .withCourtApplications(Collections.singletonList(CourtApplication.courtApplication().withId(applicationId).withConvictionDate(LocalDate.now()).build()))
                .build();

        when(this.hearingRepository.findBy(convictionDateRemoved.getHearingId())).thenReturn(hearingEntity);
        when(hearingJPAMapper.fromJPA(any())).thenReturn(hearing);
        when(hearingJPAMapper.addOrUpdateCourtApplication(any(),any() )).thenReturn("def");

        initiateHearingEventListener.convictionDateUpdated(envelopeFrom(metadataWithRandomUUID("hearing.conviction-date-added"),
                objectToJsonObjectConverter.convert(convictionDateRemoved)));

        verify(this.hearingRepository).save(hearingEntity);
        assertThat(hearingEntity.getCourtApplicationsJson(), is("def"));

        final ArgumentCaptor<CourtApplication> courtApplicationArgumentCaptor = ArgumentCaptor.forClass(CourtApplication.class);
        verify(hearingJPAMapper).addOrUpdateCourtApplication(any(), courtApplicationArgumentCaptor.capture());
        final CourtApplication updatedCourtApplication = courtApplicationArgumentCaptor.getValue();
        assertThat(updatedCourtApplication.getConvictionDate(), is(nullValue()));
    }

    @Test
    public void convictionDateRemoves_shouldSetNullConvictionDateToOffenceInCourtApplication() {

        final UUID applicationId = randomUUID();
        final UUID hearingId = randomUUID();
        final ConvictionDateRemoved convictionDateRemoved = ConvictionDateRemoved.convictionDateRemoved()
                .setCourtApplicationId(applicationId)
                .setOffenceId(randomUUID())
                .setHearingId(hearingId);

        final Hearing hearing = new Hearing();
        hearing.setCourtApplicationsJson("abc");


        when(this.hearingRepository.findBy(convictionDateRemoved.getHearingId())).thenReturn(hearing);
        when(hearingJPAMapper.updateConvictedDateOnOffencesInCourtApplication(any(String.class), any(UUID.class), any(UUID.class), any(LocalDate.class))).thenReturn("def");

        initiateHearingEventListener.convictionDateRemoved(envelopeFrom(metadataWithRandomUUID("hearing.conviction-date-removed"),
                objectToJsonObjectConverter.convert(convictionDateRemoved)));

        verify(this.hearingRepository).save(hearing);
        assertThat(hearing.getCourtApplicationsJson(), is("def"));

    }

    @Test
    public void convictionDateRemoved_shouldSetConvictionDateToNull() {

        final UUID caseId = randomUUID();
        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final HearingSnapshotKey snapshotKey = new HearingSnapshotKey(offenceId, hearingId);

        final ConvictionDateRemoved convictionDateRemoved = new ConvictionDateRemoved(caseId, hearingId, offenceId, null);

        final Offence offence = new Offence();
        offence.setId(snapshotKey);

        when(offenceRepository.findBy(snapshotKey)).thenReturn(offence);

        initiateHearingEventListener.convictionDateRemoved(envelopeFrom(metadataWithRandomUUID("hearing.conviction-date-removed"),
                objectToJsonObjectConverter.convert(convictionDateRemoved)));

        verify(this.offenceRepository).saveAndFlush(offence);

        assertThat(offence.getId().getId(), is(offenceId));
        assertThat(offence.getId().getHearingId(), is(hearingId));
        assertThat(offence.getConvictionDate(), is(nullValue()));
    }

    @Test
    public void testHearingInitiatedPleaData() {

        final uk.gov.justice.core.courts.DelegatedPowers delegatedPowersPojo = uk.gov.justice.core.courts.DelegatedPowers.delegatedPowers()
                .withUserId(randomUUID())
                .withFirstName(STRING.next())
                .withLastName(STRING.next())
                .build();
        final uk.gov.justice.core.courts.Plea pleaPojo = uk.gov.justice.core.courts.Plea.plea()
                .withOffenceId(randomUUID())
                .withOriginatingHearingId(randomUUID())
                .withPleaDate(PAST_LOCAL_DATE.next())
                .withPleaValue(GUILTY)
                .withDelegatedPowers(delegatedPowersPojo)
                .build();

        final InheritedPlea event = new InheritedPlea()
                .setHearingId(randomUUID())
                .setPlea(pleaPojo);

        final HearingSnapshotKey snapshotKey = new HearingSnapshotKey(event.getPlea().getOffenceId(), event.getHearingId());

        final Offence offence = new Offence();
        offence.setId(snapshotKey);
        final LocalDate convictionDate = LocalDate.now();
        offence.setConvictionDate(convictionDate);
        when(offenceRepository.findBy(snapshotKey)).thenReturn(offence);

        final DelegatedPowers delegatedPowers = new DelegatedPowers();
        delegatedPowers.setDelegatedPowersUserId(delegatedPowersPojo.getUserId());
        delegatedPowers.setDelegatedPowersLastName(delegatedPowersPojo.getLastName());
        delegatedPowers.setDelegatedPowersFirstName(delegatedPowersPojo.getFirstName());

        final Plea plea = new Plea();
        plea.setPleaValue(pleaPojo.getPleaValue());
        plea.setPleaDate(pleaPojo.getPleaDate());
        plea.setOriginatingHearingId(pleaPojo.getOriginatingHearingId());
        plea.setDelegatedPowers(delegatedPowers);

        when(pleaJPAMapper.toJPA(Mockito.any())).thenReturn(plea);

        initiateHearingEventListener.hearingInitiatedPleaData(envelopeFrom(metadataWithRandomUUID("hearing.initiate-hearing-offence-plead"),
                objectToJsonObjectConverter.convert(event)));

        verify(this.offenceRepository).save(offence);

        assertThat(offence, isBean(Offence.class)
                .with(Offence::getId, is(snapshotKey))
                .with(Offence::getConvictionDate, is(pleaPojo.getPleaDate()))
                .with(Offence::getPlea, isBean(Plea.class)
                        .with(Plea::getOriginatingHearingId, is(event.getPlea().getOriginatingHearingId()))
                        .with(Plea::getPleaDate, is(event.getPlea().getPleaDate()))
                        .with(Plea::getPleaValue, is(event.getPlea().getPleaValue()))
                        .with(Plea::getDelegatedPowers, isBean(DelegatedPowers.class)
                                .with(DelegatedPowers::getDelegatedPowersUserId, is(event.getPlea().getDelegatedPowers().getUserId()))
                                .with(DelegatedPowers::getDelegatedPowersFirstName, is(event.getPlea().getDelegatedPowers().getFirstName()))
                                .with(DelegatedPowers::getDelegatedPowersLastName, is(event.getPlea().getDelegatedPowers().getLastName()))
                        )
                )
        );
    }

    private JsonEnvelope getInitiateHearingJsonEnvelope(final uk.gov.justice.core.courts.Hearing hearing) {

        final InitiateHearingCommand document = new InitiateHearingCommand(hearing);

        final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

        String strJsonDocument;
        try {
            strJsonDocument = objectMapper.writer().writeValueAsString(document);
        } catch (final JsonProcessingException jpe) {
            throw new RuntimeException("failed ot serialise " + document, jpe);
        }
        final JsonObject jsonObject = Json.createReader(new StringReader(strJsonDocument)).readObject();

        return envelopeFrom((Metadata) null, jsonObject);
    }
}
