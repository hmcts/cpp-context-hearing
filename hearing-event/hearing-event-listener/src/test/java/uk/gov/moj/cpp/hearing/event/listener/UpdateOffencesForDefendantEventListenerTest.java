package uk.gov.moj.cpp.hearing.event.listener;

import static java.time.LocalDate.now;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asSet;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;


import java.util.HashSet;
import org.mockito.Mockito;
import uk.gov.justice.core.courts.ReportingRestriction;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.domain.event.OffenceAdded;
import uk.gov.moj.cpp.hearing.domain.event.OffenceDeleted;
import uk.gov.moj.cpp.hearing.domain.event.OffenceUpdated;
import uk.gov.moj.cpp.hearing.domain.event.OffencesRemovedFromExistingHearing;
import uk.gov.moj.cpp.hearing.mapping.AllocationDecisionJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.CourtIndicatedSentenceJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.DelegatedPowersJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.IndicatedPleaJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.JurorsJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.LaaApplnReferenceJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.LesserOrAlternativeOffenceForPleaJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.LesserOrAlternativeOffenceJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.NotifiedPleaJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.OffenceFactsJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.OffenceJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.PleaJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.ReportingRestrictionJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.VerdictJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.VerdictTypeJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.repository.DefendantRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateOffencesForDefendantEventListenerTest {

    public static final String HEARING = "Hearing";

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
    private ReportingRestrictionJPAMapper reportingRestrictionJPAMapper;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Mock
    private UpdateOffencesForDefendantService updateOffencesForDefendantService;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.offenceJPAMapper, "notifiedPleaJPAMapper", new NotifiedPleaJPAMapper());
        setField(this.offenceJPAMapper, "indicatedPleaJPAMapper", new IndicatedPleaJPAMapper());
        setField(this.offenceJPAMapper, "offenceFactsJPAMapper", new OffenceFactsJPAMapper());
        setField(this.offenceJPAMapper, "allocationDecisionJPAMapper", new AllocationDecisionJPAMapper(new CourtIndicatedSentenceJPAMapper()));
        setField(this.offenceJPAMapper, "pleaJPAMapper", new PleaJPAMapper(new DelegatedPowersJPAMapper(), new LesserOrAlternativeOffenceForPleaJPAMapper()));
        setField(this.offenceJPAMapper, "verdictJPAMapper", new VerdictJPAMapper(new JurorsJPAMapper(), new LesserOrAlternativeOffenceJPAMapper(), new VerdictTypeJPAMapper()));
        setField(this.offenceJPAMapper, "laaApplnReferenceJPAMapper", new LaaApplnReferenceJPAMapper());
        setField(this.offenceJPAMapper, "reportingRestrictionJPAMapper", new ReportingRestrictionJPAMapper());
    }

    @Test
    public void testAddOffence() {

        final OffenceAdded offenceAdded = OffenceAdded.offenceAdded()
                .withHearingId(randomUUID())
                .withDefendantId(randomUUID())
                .withProsecutionCaseId(randomUUID())
                .withOffence(uk.gov.justice.core.courts.Offence.offence()
                        .withId(randomUUID())
                        .build());

        final JsonEnvelope envelope = envelopeFrom((Metadata) null, objectToJsonObjectConverter.convert(offenceAdded));

        final Hearing hearing = new Hearing() {{
            setId(offenceAdded.getHearingId());
            setProsecutionCases(new HashSet<>(Collections.singletonList(new ProsecutionCase(){{
                setDefendants(new HashSet<>(Collections.singletonList(new Defendant(){{
                    setId(new HearingSnapshotKey(){{
                        setId(offenceAdded.getDefendantId());
                    }});
                }})));
            }})));
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
    public void testNoExceptionWhenAddOffenceToRemovedHearingDLQReplay() {

        final OffenceAdded offenceAdded = OffenceAdded.offenceAdded()
                .withHearingId(randomUUID())
                .withDefendantId(randomUUID())
                .withProsecutionCaseId(randomUUID())
                .withOffence(uk.gov.justice.core.courts.Offence.offence()
                        .withId(randomUUID())
                        .build());

        final JsonEnvelope envelope = envelopeFrom((Metadata) null, objectToJsonObjectConverter.convert(offenceAdded));

        when(hearingRepository.findBy(offenceAdded.getHearingId())).thenReturn(null);

        updateOffencesForDefendantEventListener.addOffence(envelope);

        final ArgumentCaptor<Offence> defendantExArgumentCaptor = ArgumentCaptor.forClass(Offence.class);

        verify(offenceRepository, never()).saveAndFlush(defendantExArgumentCaptor.capture());
    }

    @Test
    public void testNoExceptionWhenAddOffenceToAnotherHearingDLQReplay() {

        final OffenceAdded offenceAdded = OffenceAdded.offenceAdded()
                .withHearingId(randomUUID())
                .withDefendantId(randomUUID())
                .withProsecutionCaseId(randomUUID())
                .withOffence(uk.gov.justice.core.courts.Offence.offence()
                        .withId(randomUUID())
                        .build());

        final JsonEnvelope envelope = envelopeFrom((Metadata) null, objectToJsonObjectConverter.convert(offenceAdded));

        final Hearing hearing = new Hearing() {{
            setId(offenceAdded.getHearingId());
        }};

        when(hearingRepository.findBy(offenceAdded.getHearingId())).thenReturn(hearing);

        updateOffencesForDefendantEventListener.addOffence(envelope);

        final ArgumentCaptor<Offence> defendantExArgumentCaptor = ArgumentCaptor.forClass(Offence.class);

        verify(offenceRepository, never()).saveAndFlush(defendantExArgumentCaptor.capture());

    }

    @Test
    public void testUpdateOffence() {
        final ReportingRestriction reportingRestriction = ReportingRestriction.reportingRestriction()
                .withId(randomUUID())
                .withJudicialResultId(randomUUID())
                .withLabel("label")
                .withOrderedDate(now()).build();

        final OffenceUpdated offenceUpdated = OffenceUpdated.offenceUpdated()
                .withHearingId(randomUUID())
                .withDefendantId(randomUUID())
                .withOffence(uk.gov.justice.core.courts.Offence.offence()
                        .withId(randomUUID())
                        .withIntroducedAfterInitialProceedings(true)
                        .withIsDiscontinued(true)
                        .withProceedingsConcluded(true)
                        .withReportingRestrictions(asList(reportingRestriction))
                        .build());

        final JsonEnvelope envelope = envelopeFrom((Metadata) null, objectToJsonObjectConverter.convert(offenceUpdated));

        final Hearing hearing = new Hearing() {{
            setId(offenceUpdated.getHearingId());
        }};

        when(hearingRepository.findBy(hearing.getId())).thenReturn(hearing);

        final Offence offence = new Offence() {{
            setId(new HearingSnapshotKey(offenceUpdated.getOffence().getId(), offenceUpdated.getHearingId()));
        }};

        final Defendant defendant = new Defendant() {{
            setId(new HearingSnapshotKey(offenceUpdated.getDefendantId(), offenceUpdated.getHearingId()));
            setOffences(asSet(offence));
        }};

        when(defendantRepository.findBy(defendant.getId())).thenReturn(defendant);

        updateOffencesForDefendantEventListener.updateOffence(envelope);

        final ArgumentCaptor<Defendant> defendantExArgumentCaptor = ArgumentCaptor.forClass(Defendant.class);

        verify(defendantRepository).saveAndFlush(defendantExArgumentCaptor.capture());

        final Offence offenceOut = defendantExArgumentCaptor.getValue().getOffences().iterator().next();

        assertThat(offenceUpdated.getOffence().getId(), is(offenceOut.getId().getId()));
        assertThat(offenceUpdated.getHearingId(), is(offenceOut.getId().getHearingId()));
        assertThat(offenceUpdated.getOffence().getProceedingsConcluded(), is(offenceOut.isProceedingsConcluded()));
        assertThat(offenceUpdated.getOffence().getIntroducedAfterInitialProceedings(), is(offenceOut.isIntroduceAfterInitialProceedings()));
        assertThat(offenceUpdated.getOffence().getIsDiscontinued(), is(offenceOut.isDiscontinued()));
        assertThat(offenceUpdated.getOffence().getReportingRestrictions().get(0).getId(), is(reportingRestriction.getId()));
        assertThat(offenceUpdated.getOffence().getReportingRestrictions().get(0).getJudicialResultId(), is(reportingRestriction.getJudicialResultId()));
        assertThat(offenceUpdated.getOffence().getReportingRestrictions().get(0).getLabel(), is(reportingRestriction.getLabel()));
        assertThat(offenceUpdated.getOffence().getReportingRestrictions().get(0).getOrderedDate(), is(reportingRestriction.getOrderedDate()));
    }

    @Test
    public void testUpdateOffenceWithOutProceedingsConcludedData() {

        final OffenceUpdated offenceUpdated = OffenceUpdated.offenceUpdated()
                .withHearingId(randomUUID())
                .withDefendantId(randomUUID())
                .withOffence(uk.gov.justice.core.courts.Offence.offence()
                        .withId(randomUUID())
                        .build());

        final JsonEnvelope envelope = envelopeFrom((Metadata) null, objectToJsonObjectConverter.convert(offenceUpdated));

        final Hearing hearing = new Hearing() {{
            setId(offenceUpdated.getHearingId());
        }};

        when(hearingRepository.findBy(hearing.getId())).thenReturn(hearing);

        final Offence offence = new Offence() {{
            setId(new HearingSnapshotKey(offenceUpdated.getOffence().getId(), offenceUpdated.getHearingId()));
        }};

        final Defendant defendant = new Defendant() {{
            setId(new HearingSnapshotKey(offenceUpdated.getDefendantId(), offenceUpdated.getHearingId()));
            setOffences(asSet(offence));
        }};

        when(defendantRepository.findBy(defendant.getId())).thenReturn(defendant);

        updateOffencesForDefendantEventListener.updateOffence(envelope);

        final ArgumentCaptor<Defendant> defendantExArgumentCaptor = ArgumentCaptor.forClass(Defendant.class);

        verify(defendantRepository).saveAndFlush(defendantExArgumentCaptor.capture());

        final Offence offenceOut = defendantExArgumentCaptor.getValue().getOffences().iterator().next();

        assertThat(offenceUpdated.getOffence().getId(), is(offenceOut.getId().getId()));
        assertThat(offenceUpdated.getHearingId(), is(offenceOut.getId().getHearingId()));
        assertThat(offenceUpdated.getOffence().getProceedingsConcluded(), nullValue());
        assertThat(offenceUpdated.getOffence().getIntroducedAfterInitialProceedings(), is(nullValue()));
        assertThat(offenceUpdated.getOffence().getIsDiscontinued(), is(nullValue()));
    }

    @Test
    public void testUpdateOffenceWhenDefendantNotPresentForCombinationOfHearingIdAndDefendantId() {

        final OffenceUpdated offenceUpdated = OffenceUpdated.offenceUpdated()
                .withHearingId(randomUUID())
                .withDefendantId(randomUUID())
                .withOffence(uk.gov.justice.core.courts.Offence.offence()
                        .withId(randomUUID())
                        .build());

        final Hearing hearing = new Hearing() {{
            setId(offenceUpdated.getHearingId());
        }};

        when(hearingRepository.findBy(hearing.getId())).thenReturn(hearing);

        final JsonEnvelope envelope = envelopeFrom((Metadata) null, objectToJsonObjectConverter.convert(offenceUpdated));

        when(defendantRepository.findBy(new HearingSnapshotKey(offenceUpdated.getDefendantId(), offenceUpdated.getHearingId()))).thenReturn(null);

        updateOffencesForDefendantEventListener.updateOffence(envelope);


        verify(defendantRepository, never()).saveAndFlush(Mockito.any());

    }


    @Test
    public void testDeleteOffence() {

        final OffenceDeleted offenceDeleted = OffenceDeleted.builder().withId(randomUUID()).withHearingId(randomUUID()).build();

        final JsonEnvelope envelope = envelopeFrom((Metadata) null, objectToJsonObjectConverter.convert(offenceDeleted));

        final Defendant defendant = new Defendant();
        defendant.setId(new HearingSnapshotKey(randomUUID(), offenceDeleted.getHearingId()));
        defendant.setOffences(Collections.emptySet());

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

    @Test
    public void shouldRemoveOffenceFromExistingHearingWhenOffenceToBeRemoved() {

        final UUID hearingId = randomUUID();
        final UUID prosecutionId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceId1 = randomUUID();
        final UUID offenceId2 = randomUUID();

        final List<UUID> offenceIds = Collections.singletonList(offenceId1);

        final OffencesRemovedFromExistingHearing offencesRemovedFromExistingHearing = new OffencesRemovedFromExistingHearing(hearingId, new ArrayList<>(), new ArrayList<>(), offenceIds, HEARING);
        final JsonEnvelope envelope = envelopeFrom((Metadata) null, objectToJsonObjectConverter.convert(offencesRemovedFromExistingHearing));

        final Offence offence1 = new Offence();
        offence1.setId(new HearingSnapshotKey(offenceId1, hearingId));

        final Offence offence2 = new Offence();
        offence2.setId(new HearingSnapshotKey(offenceId2, hearingId));

        final Defendant defendant = new Defendant();
        defendant.setId(new HearingSnapshotKey(defendantId, hearingId));
        defendant.getOffences().add(offence1);
        defendant.getOffences().add(offence2);

        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        prosecutionCase.setId(new HearingSnapshotKey(prosecutionId, hearingId));
        prosecutionCase.getDefendants().add(defendant);

        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);
        hearing.getProsecutionCases().add(prosecutionCase);

        when(hearingRepository.findBy(hearingId)).thenReturn(hearing);
        final Hearing hearing1 = new Hearing();
        hearing1.setId(hearingId);
        final Offence offence3 = new Offence();
        offence1.setId(new HearingSnapshotKey(offenceId1, hearingId));
        final Defendant defendant1 = new Defendant();
        defendant1.setId(new HearingSnapshotKey(defendantId, hearingId));
        defendant1.getOffences().add(offence3);
        final ProsecutionCase prosecutionCase1 = new ProsecutionCase();
        prosecutionCase1.setId(new HearingSnapshotKey(prosecutionId, hearingId));
        prosecutionCase1.getDefendants().add(defendant1);
        hearing1.getProsecutionCases().add(prosecutionCase1);

        when(updateOffencesForDefendantService.removeOffencesFromExistingHearing(Matchers.any(Hearing.class), anyListOf(UUID.class), anyListOf(UUID.class), anyListOf(UUID.class), anySetOf(UUID.class))).thenReturn(hearing1);


        updateOffencesForDefendantEventListener.removeOffencesFromExistingAllocatedHearing(envelope);

        final ArgumentCaptor<Hearing> hearingArgumentCaptor = ArgumentCaptor.forClass(Hearing.class);

        verify(hearingRepository).save(hearingArgumentCaptor.capture());


        final Hearing hearingOut = hearingArgumentCaptor.getValue();

        assertThat(hearingOut, isBean(Hearing.class)
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getProsecutionCases, hasSize(1))
                .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                        .with(ProsecutionCase::getDefendants, hasSize(1))
                        .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                .with(Defendant::getOffences, hasSize(1)))))));

    }

    @Test
    public void shouldRemoveDefendantFromExistingHearingWhenDefendantIsToBeRemoved() {

        final UUID hearingId = randomUUID();
        final UUID prosecutionId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceId1 = randomUUID();

        final List<UUID> defendantIds = Collections.singletonList(defendantId);
        final List<UUID> offenceIds = Collections.singletonList(offenceId1);

        final OffencesRemovedFromExistingHearing offencesRemovedFromExistingHearing = new OffencesRemovedFromExistingHearing(hearingId, new ArrayList<>(), defendantIds, offenceIds, HEARING);
        final JsonEnvelope envelope = envelopeFrom((Metadata) null, objectToJsonObjectConverter.convert(offencesRemovedFromExistingHearing));

        final Offence offence1 = new Offence();
        offence1.setId(new HearingSnapshotKey(offenceId1, hearingId));

        final Defendant defendant = new Defendant();
        defendant.setId(new HearingSnapshotKey(defendantId, hearingId));
        defendant.getOffences().add(offence1);

        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        prosecutionCase.setId(new HearingSnapshotKey(prosecutionId, hearingId));
        prosecutionCase.getDefendants().add(defendant);

        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);
        hearing.getProsecutionCases().add(prosecutionCase);

        when(hearingRepository.findBy(hearingId)).thenReturn(hearing);
        final Hearing hearing1 = new Hearing();
        hearing1.setId(hearingId);
        final ProsecutionCase prosecutionCase1 = new ProsecutionCase();
        prosecutionCase1.setId(new HearingSnapshotKey(prosecutionId, hearingId));
        hearing1.getProsecutionCases().add(prosecutionCase1);

        when(updateOffencesForDefendantService.removeOffencesFromExistingHearing(Matchers.any(Hearing.class), anyListOf(UUID.class), anyListOf(UUID.class), anyListOf(UUID.class), anySetOf(UUID.class))).thenReturn(hearing1);

        updateOffencesForDefendantEventListener.removeOffencesFromExistingAllocatedHearing(envelope);

        final ArgumentCaptor<Hearing> hearingArgumentCaptor = ArgumentCaptor.forClass(Hearing.class);

        verify(hearingRepository).save(hearingArgumentCaptor.capture());


        final Hearing hearingOut = hearingArgumentCaptor.getValue();

        assertThat(hearingOut, isBean(Hearing.class)
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getProsecutionCases, hasSize(1))
                .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                        .with(ProsecutionCase::getDefendants, hasSize(0)))));

    }

    @Test
    public void shouldRemoveProsecutionCaseFromExistingHearingWhenProsecutionCaseToBeRemoved() {

        final UUID hearingId = randomUUID();
        final UUID prosecutionId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceId1 = randomUUID();

        final List<UUID> prosecutionCaseIds = Collections.singletonList(prosecutionId);
        final List<UUID> defendantIds = Collections.singletonList(defendantId);
        final List<UUID> offenceIds = Collections.singletonList(offenceId1);

        final OffencesRemovedFromExistingHearing offencesRemovedFromExistingHearing = new OffencesRemovedFromExistingHearing(hearingId, prosecutionCaseIds, defendantIds, offenceIds, HEARING);
        final JsonEnvelope envelope = envelopeFrom((Metadata) null, objectToJsonObjectConverter.convert(offencesRemovedFromExistingHearing));

        final Offence offence1 = new Offence();
        offence1.setId(new HearingSnapshotKey(offenceId1, hearingId));

        final Defendant defendant = new Defendant();
        defendant.setId(new HearingSnapshotKey(defendantId, hearingId));
        defendant.getOffences().add(offence1);

        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        prosecutionCase.setId(new HearingSnapshotKey(prosecutionId, hearingId));
        prosecutionCase.getDefendants().add(defendant);

        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);
        hearing.getProsecutionCases().add(prosecutionCase);

        when(hearingRepository.findBy(hearingId)).thenReturn(hearing);
        final Hearing hearing1 = new Hearing();
        hearing1.setId(hearingId);

        when(updateOffencesForDefendantService.removeOffencesFromExistingHearing(Matchers.any(Hearing.class), anyListOf(UUID.class), anyListOf(UUID.class), anyListOf(UUID.class), anySetOf(UUID.class))).thenReturn(hearing1);

        updateOffencesForDefendantEventListener.removeOffencesFromExistingAllocatedHearing(envelope);

        final ArgumentCaptor<Hearing> hearingArgumentCaptor = ArgumentCaptor.forClass(Hearing.class);

        verify(hearingRepository).save(hearingArgumentCaptor.capture());


        final Hearing hearingOut = hearingArgumentCaptor.getValue();

        assertThat(hearingOut, isBean(Hearing.class)
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getProsecutionCases, hasSize(0)));

    }

}
