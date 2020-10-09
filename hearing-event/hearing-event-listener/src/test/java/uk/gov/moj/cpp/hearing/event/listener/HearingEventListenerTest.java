package uk.gov.moj.cpp.hearing.event.listener;

import static com.google.common.io.Resources.getResource;
import static java.lang.Boolean.TRUE;
import static java.nio.charset.Charset.defaultCharset;
import static java.time.ZoneOffset.UTC;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.integer;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.VariantDirectoryTemplates.standardVariantTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.targetTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asSet;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import uk.gov.justice.core.courts.CourtApplicationOutcome;
import uk.gov.justice.core.courts.CourtApplicationOutcomeType;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.domain.event.HearingDaysCancelled;
import uk.gov.moj.cpp.hearing.domain.event.HearingEffectiveTrial;
import uk.gov.moj.cpp.hearing.domain.event.HearingTrialType;
import uk.gov.moj.cpp.hearing.domain.event.HearingTrialVacated;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstApplication;
import uk.gov.moj.cpp.hearing.domain.event.TargetRemoved;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;
import uk.gov.moj.cpp.hearing.domain.event.result.ApplicationDraftResulted;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.mapping.ApplicationDraftResultJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.TargetJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.application.ApplicationDraftResult;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingApplication;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;
import uk.gov.moj.cpp.hearing.repository.HearingApplicationRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.json.JsonObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingEventListenerTest {

    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
    @Captor
    ArgumentCaptor<Hearing> saveHearingCaptor;
    @Captor
    ArgumentCaptor<HearingApplication> saveHearingApplicationCaptor;
    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;
    @Spy
    @InjectMocks
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;
    @InjectMocks
    private HearingEventListener hearingEventListener;
    @Mock
    private HearingRepository hearingRepository;
    @Mock
    private TargetJPAMapper targetJPAMapper;
    @Mock
    private HearingJPAMapper hearingJPAMapper;
    @Mock
    private ApplicationDraftResultJPAMapper applicationDraftResultJPAMapper;
    @Mock
    private HearingApplicationRepository hearingApplicationRepository;

    private static final String LAST_SHARED_DATE = "lastSharedDate";
    private static final String DIRTY = "dirty";
    private static final String RESULTS = "results";
    private static final String CHILD_RESULT_LINES = "childResultLines";

    @Before
    public void setUp() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void draftResultSaved_shouldPersist_with_hasSharedResults_false() {

        final UUID hearingId = randomUUID();
        final Target targetOut = new Target();
        final DraftResultSaved draftResultSaved = new DraftResultSaved(CoreTestTemplates.target(hearingId, randomUUID(), randomUUID(), randomUUID()).build());
        final Hearing dbHearing = new Hearing()
                .setHasSharedResults(true)
                .setId(hearingId)
                .setTargets(asSet(new Target()
                        .setId(draftResultSaved.getTarget().getTargetId())
                ));

        when(hearingRepository.findBy(hearingId)).thenReturn(dbHearing);
        when(targetJPAMapper.toJPA(dbHearing, draftResultSaved.getTarget())).thenReturn(targetOut);

        hearingEventListener.draftResultSaved(envelopeFrom(metadataWithRandomUUID("hearing.draft-result-saved"),
                objectToJsonObjectConverter.convert(draftResultSaved)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getHasSharedResults, is(false))
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getTargets, hasSize(1))
                .with(Hearing::getTargets, first(is(targetOut)))
        );
    }

    @Test
    public void draftResultSaved_shouldPersist_with_hasSharedResults_no_change() {

        final UUID hearingId = randomUUID();
        final Target targetOut = new Target();
        final DraftResultSaved draftResultSaved = new DraftResultSaved(CoreTestTemplates.target(hearingId, randomUUID(), randomUUID(), randomUUID()).build());
        final Hearing dbHearing = new Hearing()
                .setHasSharedResults(false)
                .setId(hearingId)
                .setTargets(asSet(new Target()
                        .setId(draftResultSaved.getTarget().getTargetId())
                ));

        when(hearingRepository.findBy(hearingId)).thenReturn(dbHearing);
        when(targetJPAMapper.toJPA(dbHearing, draftResultSaved.getTarget())).thenReturn(targetOut);

        hearingEventListener.draftResultSaved(envelopeFrom(metadataWithRandomUUID("hearing.draft-result-saved"),
                objectToJsonObjectConverter.convert(draftResultSaved)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getHasSharedResults, is(false))
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getTargets, hasSize(1))
                .with(Hearing::getTargets, first(is(targetOut)))
        );
    }

    @Test
    public void resultsShared_shouldPersist_with_hasSharedResults_true() {
        final ResultsShared resultsShared = resultsSharedTemplate();
        final List<uk.gov.justice.core.courts.Target> targets = asList(targetTemplate());
        final Target target = new Target()
                .setId(randomUUID());
        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        final Defendant defendant = new Defendant();
        final Set<Defendant> defendants = asSet(defendant);
        prosecutionCase.setDefendants(defendants);

        final Hearing dbHearing = new Hearing()
                .setHasSharedResults(false)
                .setTargets(asSet(target))
                .setProsecutionCases(asSet(prosecutionCase))
                .setId(resultsShared.getHearingId()
                );

        when(hearingRepository.findBy(resultsShared.getHearingId())).thenReturn(dbHearing);
        when(hearingRepository.findTargetsByHearingId(resultsShared.getHearingId())).thenReturn(asList(target));
        when(hearingRepository.findProsecutionCasesByHearingId(dbHearing.getId()))
                .thenReturn(Lists.newArrayList(dbHearing.getProsecutionCases()));
        when(targetJPAMapper.fromJPA(asSet(target), asSet(prosecutionCase))).thenReturn(targets);

        hearingEventListener.resultsShared(envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getHasSharedResults, is(true))
                .with(Hearing::getTargets, hasSize(1))
                .with(Hearing::getId, is(resultsShared.getHearingId()))
        );
    }

    @Test
    public void shouldRegisterHearingAgainstApplication() {
        final UUID hearingId = UUID.randomUUID();
        final UUID applicationId = UUID.randomUUID();
        final RegisteredHearingAgainstApplication registeredHearingAgainstApplication = new RegisteredHearingAgainstApplication(applicationId, hearingId);

        hearingEventListener.registerHearingAgainstApplication(envelopeFrom(metadataWithRandomUUID("hearing.events.registered-hearing-against-application"),
                objectToJsonObjectConverter.convert(registeredHearingAgainstApplication)
        ));

        verify(this.hearingApplicationRepository).save(saveHearingApplicationCaptor.capture());

        assertThat(saveHearingApplicationCaptor.getValue().getId().getApplicationId(), is(applicationId));
        assertThat(saveHearingApplicationCaptor.getValue().getId().getHearingId(), is(hearingId));
    }

    private ResultsShared resultsSharedTemplate() {

        CommandHelpers.InitiateHearingCommandHelper hearingOne = h(standardInitiateHearingTemplate());
        UUID completedResultLineId = randomUUID();
        hearingOne.it().getHearing().setHasSharedResults(true);

        final VerdictUpsert verdictUpsert = VerdictUpsert.verdictUpsert()
                .setHearingId(hearingOne.getFirstOffenceIdForFirstDefendant())
                .setVerdict(uk.gov.justice.core.courts.Verdict.verdict()
                        .withVerdictDate(PAST_LOCAL_DATE.next())
                        .withOffenceId(hearingOne.getFirstOffenceIdForFirstDefendant())
                        .withOriginatingHearingId(randomUUID())
                        .withJurors(
                                uk.gov.justice.core.courts.Jurors.jurors()
                                        .withNumberOfJurors(integer(9, 12).next())
                                        .withNumberOfSplitJurors(integer(0, 3).next())
                                        .withUnanimous(BOOLEAN.next())
                                        .build())
                        .withVerdictType(
                                uk.gov.justice.core.courts.VerdictType.verdictType()
                                        .withId(randomUUID())
                                        .withCategoryType(STRING.next())
                                        .withCategory(STRING.next())
                                        .withDescription(STRING.next())
                                        .withSequence(INTEGER.next())
                                        .build())
                        .withLesserOrAlternativeOffence(uk.gov.justice.core.courts.LesserOrAlternativeOffence.lesserOrAlternativeOffence()
                                .withOffenceLegislationWelsh(STRING.next())
                                .withOffenceLegislation(STRING.next())
                                .withOffenceTitleWelsh(STRING.next())
                                .withOffenceTitle(STRING.next())
                                .withOffenceCode(STRING.next())
                                .withOffenceDefinitionId(randomUUID())
                                .build())
                        .build());

        return ResultsShared.builder()
                .withHearingId(hearingOne.getHearingId())
                .withTargets(new ArrayList<>(singletonList(
                        CoreTestTemplates.target(hearingOne.getHearingId(), hearingOne.getFirstDefendantForFirstCase().getId(), hearingOne.getFirstOffenceIdForFirstDefendant(), completedResultLineId).build()
                )))
                .withSharedTime(PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC")))
                .withHearing(hearingOne.getHearing())
                .withCourtClerk(DelegatedPowers.delegatedPowers()
                        .withUserId(randomUUID())
                        .withFirstName(STRING.next())
                        .withLastName(STRING.next())
                        .build())
                .withCompletedResultLinesStatus(ImmutableMap.of(completedResultLineId, CompletedResultLineStatus.builder()
                        .withCourtClerk(DelegatedPowers.delegatedPowers()
                                .withUserId(randomUUID())
                                .withFirstName(STRING.next())
                                .withLastName(STRING.next())
                                .build())
                        .withId(completedResultLineId)
                        .withLastSharedDateTime(PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC")))
                        .build()
                ))
                .withVariantDirectory(singletonList(
                        standardVariantTemplate(randomUUID(), hearingOne.getHearingId(), hearingOne.getFirstDefendantForFirstCase().getId())
                ))
                .build();
    }


    @Test
    public void applicationDraftResulted_shouldPersist() {

        final UUID hearingId = randomUUID();
        final ApplicationDraftResult applicationDraftResult = new ApplicationDraftResult();
        final ApplicationDraftResulted applicationDraftResulted = ApplicationDraftResulted.applicationDraftResulted()
                .setDraftResult("result")
                .setTargetId(randomUUID())
                .setHearingId(hearingId)
                .setApplicationId(randomUUID());
        final Hearing dbHearing = new Hearing()
                .setHasSharedResults(true)
                .setId(hearingId)
                .setApplicationDraftResults(asSet(new ApplicationDraftResult()
                        .setId(applicationDraftResulted.getTargetId())
                ));

        when(hearingRepository.findBy(hearingId)).thenReturn(dbHearing);
        when(applicationDraftResultJPAMapper.toJPA(dbHearing, applicationDraftResulted.getTargetId(), applicationDraftResulted.getApplicationId(), applicationDraftResulted.getDraftResult())).thenReturn(applicationDraftResult);

        hearingEventListener.applicationDraftResulted(envelopeFrom(metadataWithRandomUUID("hearing.application-draft-resulted"),
                objectToJsonObjectConverter.convert(applicationDraftResulted)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getApplicationDraftResults, hasSize(1))
                .with(Hearing::getApplicationDraftResults, first(is(applicationDraftResult)))
        );
    }

    @Test
    public void applicationDraftResultedWithOutcome_shouldPersist() {

        final UUID hearingId = randomUUID();
        final UUID applicationId = randomUUID();
        final ApplicationDraftResult applicationDraftResult = new ApplicationDraftResult();
        final CourtApplicationOutcomeType courtApplicationOutcomeType = CourtApplicationOutcomeType.courtApplicationOutcomeType().withDescription("Admitted")
                .withId(UUID.randomUUID())
                .withSequence(1).build();
        final CourtApplicationOutcome courtApplicationOutcome = new CourtApplicationOutcome(applicationId, LocalDate.now(), courtApplicationOutcomeType, hearingId);
        final String applicationJson = "application json";
        final ApplicationDraftResulted applicationDraftResulted = ApplicationDraftResulted.applicationDraftResulted()
                .setDraftResult("result")
                .setTargetId(randomUUID())
                .setHearingId(hearingId)
                .setApplicationId(applicationId)
                .setApplicationOutcomeType(courtApplicationOutcomeType)
                .setApplicationOutcomeDate(LocalDate.now());
        final Hearing dbHearing = new Hearing()
                .setHasSharedResults(true)
                .setId(hearingId)
                .setCourtApplicationsJson(applicationJson)
                .setApplicationDraftResults(asSet(new ApplicationDraftResult()
                        .setId(applicationDraftResulted.getTargetId())
                ));

        when(hearingRepository.findBy(hearingId)).thenReturn(dbHearing);
        when(applicationDraftResultJPAMapper.toJPA(dbHearing, applicationDraftResulted.getTargetId(), applicationDraftResulted.getApplicationId(), applicationDraftResulted.getDraftResult())).thenReturn(applicationDraftResult);
        when(hearingJPAMapper.saveApplicationOutcome(applicationJson, courtApplicationOutcome)).thenReturn(applicationJson);
        hearingEventListener.applicationDraftResulted(envelopeFrom(metadataWithRandomUUID("hearing.application-draft-resulted"),
                objectToJsonObjectConverter.convert(applicationDraftResulted)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getApplicationDraftResults, hasSize(1))
                .with(Hearing::getApplicationDraftResults, first(is(applicationDraftResult)))
                .with(Hearing::getCourtApplicationsJson, is(applicationJson))
        );
    }

    @Test
    public void setInEffectiveTrialType_shouldPersist_with_hearing() {

        final UUID hearingId = randomUUID();
        final UUID trialTypeId = randomUUID();
        final Hearing hearingEntity = new Hearing()
                .setId(hearingId);
        final HearingTrialType hearingTrialType = new HearingTrialType(hearingId, trialTypeId, "A", "Effective", "full description");
        when(hearingRepository.findBy(hearingId)).thenReturn(hearingEntity);

        hearingEventListener.setHearingTrialType(envelopeFrom(metadataWithRandomUUID("hearing.hearing-trial-type-set"),
                objectToJsonObjectConverter.convert(hearingTrialType)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getTrialTypeId, is(trialTypeId))
        );
    }

    @Test
    public void setEffectiveTrialType_shouldPersist_with_hearing() {

        final UUID hearingId = randomUUID();
        Hearing hearingEntity = new Hearing()
                .setId(hearingId);

        final HearingEffectiveTrial hearingEffectiveTrial = new HearingEffectiveTrial(hearingId, TRUE);

        when(hearingRepository.findBy(hearingId)).thenReturn(hearingEntity);

        hearingEventListener.setHearingEffectiveTrial(envelopeFrom(metadataWithRandomUUID("hearing.hearing-effective-trial-set"),
                objectToJsonObjectConverter.convert(hearingEffectiveTrial)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getIsEffectiveTrial, is(true))
        );
    }

    @Test
    public void setInVacateTrialType_shouldPersist_with_hearing() {

        final UUID hearingId = randomUUID();
        final UUID vacateTrialTypeId = randomUUID();
        final Hearing hearingEntity = new Hearing()
                .setId(hearingId);
        final HearingTrialVacated hearingTrialVacated = new HearingTrialVacated(hearingId, vacateTrialTypeId, "A", "Vacated", "full description");
        when(hearingRepository.findBy(hearingId)).thenReturn(hearingEntity);

        hearingEventListener.setHearingVacateTrialType(envelopeFrom(metadataWithRandomUUID("hearing.trial-vacated"),
                objectToJsonObjectConverter.convert(hearingTrialVacated)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getVacatedTrialReasonId, is(vacateTrialTypeId))
        );
    }

    @Test
    public void shouldCancelHearingDaysWhenHearingNotNull() {
        final ZonedDateTime sittingDay = ZonedDateTime.now();
        final UUID hearingId = randomUUID();
        final Hearing hearing = new Hearing().setId(hearingId);
        final List<HearingDay> hearingDayList = Arrays.asList(new HearingDay.Builder().withSittingDay(sittingDay).withIsCancelled(TRUE).build());
        final HearingDaysCancelled hearingDaysCancelled = new HearingDaysCancelled(hearingId, hearingDayList);
        final uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay hearingDayEntity = new uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay();

        hearingDayEntity.setSittingDay(sittingDay);
        hearingDayEntity.setIsCancelled(null);
        hearing.setHearingDays(new HashSet<>(Arrays.asList(hearingDayEntity)));

        when(hearingRepository.findBy(hearingId)).thenReturn(hearing);

        hearingEventListener.cancelHearingDays(envelopeFrom(metadataWithRandomUUID("hearing.hearing-days-cancelled"),
                objectToJsonObjectConverter.convert(hearingDaysCancelled)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());
        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getHearingDays, first(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay.class)
                        .with(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay::getIsCancelled, is(TRUE))))
        );
    }

    @Test
    public void shouldNotCancelHearingDaysWhenHearingNull() {
        final UUID hearingId = randomUUID();
        final List<HearingDay> hearingDayList = new ArrayList<>();
        final HearingDaysCancelled hearingDaysCancelled = new HearingDaysCancelled(hearingId, hearingDayList);

        when(hearingRepository.findBy(hearingId)).thenReturn(null);

        hearingEventListener.cancelHearingDays(envelopeFrom(metadataWithRandomUUID("hearing.hearing-days-cancelled"),
                objectToJsonObjectConverter.convert(hearingDaysCancelled)
        ));

        verify(this.hearingRepository, never()).save(any());
    }

    @Test
    public void draftResultRemoved_shouldPersist_with_hasSharedResults_false() {

        final UUID hearingId = randomUUID();
        final Target targetOut = new Target();
        final DraftResultSaved draftResultSaved = new DraftResultSaved(CoreTestTemplates.target(hearingId, randomUUID(), randomUUID(), randomUUID()).build());
        final Hearing dbHearing = new Hearing()
                .setHasSharedResults(true)
                .setId(hearingId)
                .setTargets(asSet(new Target()
                        .setId(draftResultSaved.getTarget().getTargetId())
                ));

        when(hearingRepository.findBy(hearingId)).thenReturn(dbHearing);
        when(targetJPAMapper.toJPA(dbHearing, draftResultSaved.getTarget())).thenReturn(targetOut);

        hearingEventListener.draftResultSaved(envelopeFrom(metadataWithRandomUUID("hearing.draft-result-saved"),
                objectToJsonObjectConverter.convert(draftResultSaved)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getHasSharedResults, is(false))
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getTargets, hasSize(1))
                .with(Hearing::getTargets, first(is(targetOut)))
        );
    }

    @Test
    public void targetRemoved_IfPresent() {

        final UUID hearingId = randomUUID();
        final UUID targetId = randomUUID();
        final TargetRemoved targetRemoved = new TargetRemoved(hearingId, targetId);
        final Hearing dbHearing = new Hearing()
                .setId(hearingId)
                .setTargets(asSet(new Target()
                        .setId(targetId)
                ));

        when(hearingRepository.findBy(hearingId)).thenReturn(dbHearing);

        hearingEventListener.targetRemoved(envelopeFrom(metadataWithRandomUUID("hearing.target-removed"),
                objectToJsonObjectConverter.convert(targetRemoved)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getTargets, is(empty()))
        );
    }

    @Test
    public void targetIgnoredForRemoval_IfNotPresent() {
        // this is a specific test for production data scenario (as entries have been removed from viewstore manuallu)
        final UUID hearingId = randomUUID();
        final UUID unknownTargetId = randomUUID();
        final TargetRemoved targetRemoved = new TargetRemoved(hearingId, unknownTargetId);
        final Hearing dbHearing = new Hearing()
                .setId(hearingId)
                .setTargets(asSet(new Target()
                        .setId(randomUUID())
                ));

        when(hearingRepository.findBy(hearingId)).thenReturn(dbHearing);

        hearingEventListener.targetRemoved(envelopeFrom(metadataWithRandomUUID("hearing.target-removed"),
                objectToJsonObjectConverter.convert(targetRemoved)
        ));

        verify(this.hearingRepository, never()).save(saveHearingCaptor.capture());

    }

    @Test
    public void testDraftResults() throws IOException {
        final ZonedDateTime sharedTime = ZonedDateTime.now(UTC);
        final String draftResults = getDraftResultFromResource("hearing.draft-result.json");
        final String enrichedDraftResultAsString = hearingEventListener.enrichDraftResult(draftResults, sharedTime);
        final JsonObject enrichedDraftResultJson = new StringToJsonObjectConverter().convert(enrichedDraftResultAsString);
        assertThat(enrichedDraftResultJson.getJsonArray(RESULTS).getJsonObject(0).getString(LAST_SHARED_DATE), is(sharedTime.toLocalDate().toString()));
        assertThat(enrichedDraftResultJson.getJsonArray(RESULTS).getJsonObject(0).getBoolean(DIRTY), is(false));
        assertThat(enrichedDraftResultJson.getJsonArray(RESULTS).getJsonObject(0).getJsonArray(CHILD_RESULT_LINES).getJsonObject(0).getString(LAST_SHARED_DATE), is(sharedTime.toLocalDate().toString()));
        assertThat(enrichedDraftResultJson.getJsonArray(RESULTS).getJsonObject(0).getJsonArray(CHILD_RESULT_LINES).getJsonObject(0).getBoolean(DIRTY), is(false));
        assertThat(enrichedDraftResultJson.getJsonArray(RESULTS).getJsonObject(0).getJsonArray(CHILD_RESULT_LINES).getJsonObject(0).getJsonArray(CHILD_RESULT_LINES).getJsonObject(0).getString(LAST_SHARED_DATE), is(sharedTime.toLocalDate().toString()));
        assertThat(enrichedDraftResultJson.getJsonArray(RESULTS).getJsonObject(0).getJsonArray(CHILD_RESULT_LINES).getJsonObject(0).getJsonArray(CHILD_RESULT_LINES).getJsonObject(0).getBoolean(DIRTY), is(false));
    }

    private String getDraftResultFromResource(final String path) throws IOException {
        return Resources.toString(getResource(path), defaultCharset());
    }
}
