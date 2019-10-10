package uk.gov.moj.cpp.hearing.event.listener;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
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
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asSet;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import uk.gov.justice.core.courts.CourtApplicationOutcome;
import uk.gov.justice.core.courts.CourtApplicationOutcomeType;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.domain.event.HearingEffectiveTrial;
import uk.gov.moj.cpp.hearing.domain.event.HearingTrialType;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;
import uk.gov.moj.cpp.hearing.domain.event.result.ApplicationDraftResulted;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.mapping.ApplicationDraftResultJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.TargetJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.application.ApplicationDraftResult;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
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

    @Before
    public void setUp() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
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

        final Hearing dbHearing = new Hearing()
                .setHasSharedResults(false)
                .setId(resultsShared.getHearingId()
                );

        when(hearingRepository.findBy(resultsShared.getHearingId())).thenReturn(dbHearing);

        hearingEventListener.resultsShared(envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getHasSharedResults, is(true))
                .with(Hearing::getId, is(resultsShared.getHearingId()))
        );
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
}