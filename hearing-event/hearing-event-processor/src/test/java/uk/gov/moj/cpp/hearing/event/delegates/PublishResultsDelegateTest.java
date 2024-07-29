package uk.gov.moj.cpp.hearing.event.delegates;

import static java.lang.System.lineSeparator;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.core.courts.JudicialResult.judicialResult;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_RESULTS_CASE_LEVEL_SHARED_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_RESULTS_DEFENDANT_LEVEL_SHARED_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_RESULTS_SHARED_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_RESULTS_SHARED_MULTIPLE_DEFENDANT_MULTIPLE_CASE_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_RESULTS_SHARED_OPTIONAL_PROMPT_REF_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_RESULTS_SHARED_TO_SET_ACQUITTAL_DATE;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_RESULTS_SHARED_TO_SET_ACQUITTAL_DATE_FOR_COURTAPPLICATIONCASES;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_RESULTS_SHARED_TO_SET_ACQUITTAL_DATE_FOR_COURTORDER;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_RESULTS_SHARED_WITH_ACQUITTAL_DATE;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_RESULTS_SHARED_WITH_ACQUITTAL_DATE_FOR_COURTAPPLICATIONCASES;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_RESULTS_SHARED_WITH_ACQUITTAL_DATE_FOR_COURTORDER;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_RESULTS_SHARED_WITH_NO_PROMPTS_JSON;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;


import java.time.LocalDate;
import org.mockito.Mockito;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.DefendantJudicialResult;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.JudicialResultCategory;
import uk.gov.justice.core.courts.JudicialRole;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.PublicHearingResulted;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV2;
import uk.gov.moj.cpp.hearing.event.delegates.helper.BailStatusHelper;
import uk.gov.moj.cpp.hearing.event.delegates.helper.OffenceHelper;
import uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.AbstractRestructuringTest;
import uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.RestructuringHelper;
import uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.ResultTextConfHelper;
import uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.ResultTreeBuilder;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.relist.RelistReferenceDataService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;

public class PublishResultsDelegateTest extends AbstractRestructuringTest {

    @Mock
    private Sender sender;

    @Mock
    private RelistReferenceDataService relistReferenceDataService;

    @Mock
    private CustodyTimeLimitCalculator custodyTimeLimitCalculator;

    @Mock
    private BailStatusHelper bailStatusHelper;

    @Captor
    private ArgumentCaptor<Envelope> envelopeArgumentCaptor;

    @Captor
    private ArgumentCaptor<JsonEnvelope> jsonEnvelopeArgumentCaptor;

    @Captor
    private ArgumentCaptor<Hearing> custodyLimitCalculatorHearingIn;

    @Mock
    protected ResultTextConfHelper resultTextConfHelper = Mockito.mock(ResultTextConfHelper.class);

    @Captor
    private ArgumentCaptor<LocalDate> hearingDayArgumentCaptor;

    @Captor
    private ArgumentCaptor<ResultsSharedV2> resultsSharedV2ArgumentCaptor;

    @Spy
    private ResultTreeBuilder resultTreeBuilder = new ResultTreeBuilder(referenceDataService, nextHearingHelper, resultLineHelper, resultTextConfHelper);

    @Spy
    private RestructuringHelper restructringHelper = new RestructuringHelper(resultTreeBuilder, resultTextConfHelper);

    @Mock
    private OffenceHelper offenceHelper;

    private PublishResultsDelegate target;

    @Before
    public void setUp() throws IOException {
        super.setUp();
        when(resultTextConfHelper.isOldResultDefinition(any(LocalDate.class))).thenReturn(false);

        target = new PublishResultsDelegate(enveloper,
                objectToJsonObjectConverter,
                referenceDataService,
                relistReferenceDataService,
                custodyTimeLimitCalculator,
                bailStatusHelper,
                restructringHelper,
                offenceHelper);
    }

    @Test
    public void shareResults() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_SHARED_JSON, ResultsShared.class);
        final JsonEnvelope envelope = getEnvelope(resultsShared);
        target.shareResults(envelope, sender, resultsShared);

        verify(sender).send(envelopeArgumentCaptor.capture());

        final Envelope<JsonObject> sharedResultsMessage = envelopeArgumentCaptor.getValue();

        assertThat(sharedResultsMessage.metadata().name(), is("public.hearing.resulted"));
        verify(custodyTimeLimitCalculator, times(1)).calculate(custodyLimitCalculatorHearingIn.capture());

        final Hearing calculatorHearingIn = custodyLimitCalculatorHearingIn.getValue();

        assertEquals(resultsShared.getHearing(), calculatorHearingIn);

        final Optional<Defendant> defendant = resultsShared.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream()).findFirst();
        assertThat(defendant.isPresent(), is(true));
    }

    @Test
    public void shouldNotEnrichPayloadWithCTLOrBailStatusInformationForBulkCases() throws IOException {
        final ResultsSharedV2 resultsShared = fileResourceObjectMapper.convertFromFile("hearing.events.results-shared-v2.json", ResultsSharedV2.class);
        final JsonEnvelope envelope = getEnvelope(resultsShared);
        target.shareResults(envelope, sender, resultsShared);

        verify(sender,times(2)).send(envelopeArgumentCaptor.capture());

        final Envelope<JsonObject> sharedResultsMessage = envelopeArgumentCaptor.getValue();

        assertThat(sharedResultsMessage.metadata().name(), is("public.events.hearing.hearing-resulted"));
        verify(custodyTimeLimitCalculator, times(0)).calculate(custodyLimitCalculatorHearingIn.capture());
        verify(custodyTimeLimitCalculator, times(0)).calculateDateHeldInCustody(custodyLimitCalculatorHearingIn.capture(), hearingDayArgumentCaptor.capture());
        verify(custodyTimeLimitCalculator, times(0)).updateExtendedCustodyTimeLimit(resultsSharedV2ArgumentCaptor.capture());

        verify(bailStatusHelper, times(0)).mapBailStatuses(jsonEnvelopeArgumentCaptor.capture(), custodyLimitCalculatorHearingIn.capture());

    }

    @Test
    public void shouldAssignDefendantLevelResultsToTheDefendantOnlyOncePerHearing() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_SHARED_MULTIPLE_DEFENDANT_MULTIPLE_CASE_JSON, ResultsShared.class);
        final JsonEnvelope envelope = getEnvelope(resultsShared);
        target.shareResults(envelope, sender, resultsShared);
        final UUID masterDefendantId = fromString("98e1c8e0-83b3-4d9d-a8f0-28b9bfcf6610");
        final List<Defendant> prosecutionCaseDefendants = resultsShared
                .getHearing()
                .getProsecutionCases()
                .stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .filter(defendant -> masterDefendantId.equals(defendant.getMasterDefendantId()))
                .collect(toList());

        assertEquals(0L, resultsShared.getHearing().getDefendantJudicialResults().stream().filter(djr -> Objects.isNull(djr.getJudicialResult().getOffenceId())).count());
        assertThat(prosecutionCaseDefendants, hasSize(2));
        final Defendant masterDefendant = prosecutionCaseDefendants
                .stream()
                .filter(defendant -> masterDefendantId.equals(defendant.getId()))
                .findFirst()
                .orElse(null);
        assertNotNull(masterDefendant);
        assertThat(masterDefendant.getDefendantCaseJudicialResults(), hasSize(1));
        final Defendant otherDefendant = prosecutionCaseDefendants
                .stream()
                .filter(defendant -> !masterDefendantId.equals(defendant.getId()))
                .findFirst()
                .orElse(null);
        final UUID caseLevelResultLineId = fromString("cfa5ef9a-db03-470a-bbf7-dc1a79c9bfc5");
        final UUID caseLevelResultOffenceId = fromString("914c5385-f1cc-471d-9a93-f81b48293cbb");
        // should add only case level results
        assertThat(otherDefendant.getDefendantCaseJudicialResults(), hasSize(1));
        assertEquals(otherDefendant.getDefendantCaseJudicialResults().get(0).getOffenceId(), caseLevelResultOffenceId);
        assertEquals(caseLevelResultLineId, otherDefendant.getDefendantCaseJudicialResults().get(0).getJudicialResultId());
    }

    @Test
    public void shareResultsIncludingDDCH() throws IOException {

        doNothing().when(bailStatusHelper).mapBailStatuses(any(JsonEnvelope.class), any(Hearing.class));
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile("hearing.results-shared-ddch.json", ResultsShared.class);
        final JsonEnvelope envelope = getEnvelope(resultsShared);

        final ResultDefinition resultDefinition = new ResultDefinition();
        resultDefinition.setId(randomUUID());
        resultDefinition.setLabel("Defendant Details Changed");
        resultDefinition.setCategory("A");

        when(relistReferenceDataService.getResults(envelope, "DDCH")).thenReturn(resultDefinition);

        target.shareResults(envelope, sender, resultsShared);

        verify(sender).send(envelopeArgumentCaptor.capture());

        final Envelope<JsonObject> sharedResultsMessage = envelopeArgumentCaptor.getValue();

        assertThat(sharedResultsMessage.metadata().name(), is("public.hearing.resulted"));

        final PublicHearingResulted publicHearingResulted = jsonObjectToObjectConverter.convert(sharedResultsMessage.payload(), PublicHearingResulted.class);

        final Hearing hearingIn = resultsShared.getHearing();

        assertThat(hearingIn.getProsecutionCases().get(0).getDefendants().get(0).getDefendantCaseJudicialResults().get(0).getResultText(), is("Defendant Details Changed"));
        verify(custodyTimeLimitCalculator, times(1)).calculate(custodyLimitCalculatorHearingIn.capture());
        final Hearing calHearingIn = custodyLimitCalculatorHearingIn.getValue();
        assertEquals(resultsShared.getHearing(), calHearingIn);
        assertThat(publicHearingResulted.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getDefendantCaseJudicialResults().get(0).getOrderedDate().toString(), is("2021-07-02"));


        final Optional<Defendant> defendant = resultsShared.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream()).findFirst();
        assertThat(defendant.isPresent(), is(true));
    }

    @Test
    public void whenAnyJudicialResultCategorytIsFinal_Then_IsDiposed_Should_BeSetToTrue() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_SHARED_JSON, ResultsShared.class);
        setJudicialResultsWithCategoryOf(resultsShared, JudicialResultCategory.FINAL);

        target.shareResults(dummyEnvelope, sender, resultsShared);

        verify(sender).send(envelopeArgumentCaptor.capture());
        verify(sender, times(1)).send(envelopeArgumentCaptor.capture());
    }

    @Test
    public void whenAnyJudicialResultsHaveResultsPrompts_Then_BailConditions_Should_BeSetBasedOnRank() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_SHARED_JSON, ResultsShared.class);
        final JsonEnvelope envelope = getEnvelope(resultsShared);
        target.shareResults(envelope, sender, resultsShared);

        verify(sender).send(envelopeArgumentCaptor.capture());

        final Envelope<JsonObject> sharedResultsMessage = envelopeArgumentCaptor.getValue();

        final PublicHearingResulted publicHearingResulted = jsonObjectToObjectConverter.convert(sharedResultsMessage.payload(), PublicHearingResulted.class);

        verify(sender, times(1)).send(envelopeArgumentCaptor.capture());

        final Hearing hearingIn = resultsShared.getHearing();

        final String bailCondition = "Time of hearing" + lineSeparator() +
                "Time of hearing : 777" + lineSeparator() +
                "Date of hearing : 888" + lineSeparator();

        assertThat(publicHearingResulted, isBean(PublicHearingResulted.class)
                .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                        .withValue(Hearing::getId, hearingIn.getId())
                        .withValue(sh -> sh.getJurisdictionType().name(), hearingIn.getJurisdictionType().name())
                        .withValue(sh -> sh.getHearingDays().size(), hearingIn.getHearingDays().size())
                        .with(Hearing::getHearingDays, first(isBean(HearingDay.class)))
                        .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                .withValue(CourtCentre::getId, hearingIn.getCourtCentre().getId())
                                .withValue(CourtCentre::getName, hearingIn.getCourtCentre().getName())
                                .withValue(CourtCentre::getWelshName, hearingIn.getCourtCentre().getWelshName())
                                .withValue(CourtCentre::getRoomId, hearingIn.getCourtCentre().getRoomId())
                                .withValue(CourtCentre::getRoomName, hearingIn.getCourtCentre().getRoomName())
                                .withValue(CourtCentre::getWelshRoomName, hearingIn.getCourtCentre().getWelshRoomName())
                        )
                        .withValue(sh -> sh.getJudiciary().size(), hearingIn.getJudiciary().size())
                        .with(Hearing::getJudiciary, first(isBean(JudicialRole.class)
                                .withValue(JudicialRole::getJudicialId, hearingIn.getJudiciary().get(0).getJudicialId())
                        ))
                        .withValue(sh -> sh.getProsecutionCases().size(), hearingIn.getProsecutionCases().size())
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .withValue(ProsecutionCase::getId, hearingIn.getProsecutionCases().get(0).getId())
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)))
                                .withValue(def -> def.getDefendants().size(), hearingIn.getProsecutionCases().get(0).getDefendants().size())
                                .with(off -> off.getDefendants().get(0).getOffences(), first(isBean(Offence.class)))
                                .with(off -> off.getDefendants().get(0).getOffences().get(0).getJudicialResults(), first(isBean(JudicialResult.class)))
                        ))));
    }

    @Test
    public void whenNoJudicialResultArePresent_Then_IsDisposed_Flag_ShouldBe_False() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_SHARED_JSON, ResultsShared.class);
        resultsShared.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setJudicialResults(Collections.EMPTY_LIST);

        target.shareResults(dummyEnvelope, sender, resultsShared);

        verify(sender).send(envelopeArgumentCaptor.capture());

        final Envelope<JsonObject> sharedResultsMessage = envelopeArgumentCaptor.getValue();
        final PublicHearingResulted publicHearingResulted = jsonObjectToObjectConverter.convert(sharedResultsMessage.payload(), PublicHearingResulted.class);
        final ArgumentCaptor<Envelope> envelopeArgumentCaptor = ArgumentCaptor.forClass(Envelope.class);
        verify(sender, times(1)).send(envelopeArgumentCaptor.capture());

        Boolean isDisposed = publicHearingResulted.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getIsDisposed();

        assertFalse(isDisposed);
    }

    @Test
    public void whenNoJudicialResultArePresent_Then_IsDisposed_Flag_ShouldBe_FalseWithOptionalPromptRef() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_SHARED_OPTIONAL_PROMPT_REF_JSON, ResultsShared.class);
        resultsShared.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setJudicialResults(Collections.EMPTY_LIST);

        target.shareResults(dummyEnvelope, sender, resultsShared);

        verify(sender).send(envelopeArgumentCaptor.capture());

        final Envelope<JsonObject> sharedResultsMessage = envelopeArgumentCaptor.getValue();
        final PublicHearingResulted publicHearingResulted = jsonObjectToObjectConverter.convert(sharedResultsMessage.payload(), PublicHearingResulted.class);
        final ArgumentCaptor<Envelope> envelopeArgumentCaptor = ArgumentCaptor.forClass(Envelope.class);
        verify(sender, times(1)).send(envelopeArgumentCaptor.capture());

        Boolean isDisposed = publicHearingResulted.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getIsDisposed();

        assertFalse(isDisposed);
    }

    @Test
    public void shouldShareResultsWhenJudicialResultCategoryIsNotFinalThenOffenceIsDisposedFalse() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_SHARED_JSON, ResultsShared.class);
        setJudicialResultsWithCategoryOf(resultsShared, JudicialResultCategory.ANCILLARY);
        target.shareResults(dummyEnvelope, sender, resultsShared);

        verify(sender).send(envelopeArgumentCaptor.capture());

        final Envelope<JsonObject> sharedResultsMessage = envelopeArgumentCaptor.getValue();
        final PublicHearingResulted publicHearingResulted = jsonObjectToObjectConverter.convert(sharedResultsMessage.payload(), PublicHearingResulted.class);

        verify(sender, times(1)).send(envelopeArgumentCaptor.capture());

        Boolean isDisposed = publicHearingResulted.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getIsDisposed();

        assertFalse(isDisposed);
    }

    @Test
    public void whenJudicialResultCagtegory_Is_NotFinal_Then_Offence_IsDisposed_isFalse() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_SHARED_JSON, ResultsShared.class);
        setJudicialResultsWithCategoryOf(resultsShared, JudicialResultCategory.ANCILLARY);

        target.shareResults(dummyEnvelope, sender, resultsShared);

        verify(sender).send(envelopeArgumentCaptor.capture());

        final Envelope<JsonObject> sharedResultsMessage = envelopeArgumentCaptor.getValue();
        final PublicHearingResulted publicHearingResulted = jsonObjectToObjectConverter.convert(sharedResultsMessage.payload(), PublicHearingResulted.class);

        verify(sender, times(1)).send(envelopeArgumentCaptor.capture());
        assertFalse(getIsDisposedValueForOffence(publicHearingResulted.getHearing()));
    }

    @Test
    public void shouldShareResultsWhenResultDefinitionHavingCADate() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_SHARED_JSON, ResultsShared.class);
        final ResultLine resultLine = resultsShared.getTargets().get(0).getResultLines().get(0);
        resultLine.setDelegatedPowers(DelegatedPowers.delegatedPowers().withUserId(randomUUID()).build());

        target.shareResults(dummyEnvelope, sender, resultsShared);

        verify(sender, times(1)).send(envelopeArgumentCaptor.capture());
        assertThat(resultsShared.getTargets().get(0).getResultLines().get(0).getOrderedDate().toString(), is("2019-11-08"));
    }

    @Test
    public void shouldShareResultsWhenResultDefinitionHavingNoPrompts() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_SHARED_WITH_NO_PROMPTS_JSON, ResultsShared.class);
        final ResultLine resultLine = resultsShared.getTargets().get(0).getResultLines().get(0);
        resultLine.setDelegatedPowers(DelegatedPowers.delegatedPowers().withUserId(randomUUID()).build());

        target.shareResults(dummyEnvelope, sender, resultsShared);

        verify(sender, times(1)).send(envelopeArgumentCaptor.capture());
        assertThat(resultsShared.getTargets().get(0).getResultLines().get(0).getOrderedDate().toString(), is("2019-11-08"));
        assertThat(resultsShared.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults().get(0).getJudicialResultPrompts(), nullValue());
        assertThat(resultsShared.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getDefendantCaseJudicialResults(), nullValue());
    }

    @Test
    public void shouldShareResultsWithAcquittalDateForOffenceLevelJudicialResults() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_SHARED_TO_SET_ACQUITTAL_DATE, ResultsShared.class);

        target.shareResults(dummyEnvelope, sender, resultsShared);

        verify(sender, times(1)).send(envelopeArgumentCaptor.capture());
        assertThat(resultsShared.getTargets().get(0).getResultLines().get(0).getOrderedDate().toString(), is("2020-08-19"));
        assertThat(resultsShared.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getAquittalDate().toString(), is("2020-08-19"));
    }

    @Test
    public void shouldShareResultsWithAcquittalDateForOffenceLevelJudicialResultsWhenCourtAplicationCasesExist() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_SHARED_TO_SET_ACQUITTAL_DATE_FOR_COURTAPPLICATIONCASES, ResultsShared.class);

        target.shareResults(dummyEnvelope, sender, resultsShared);

        verify(sender, times(1)).send(envelopeArgumentCaptor.capture());
        assertThat(resultsShared.getTargets().get(0).getResultLines().get(0).getOrderedDate().toString(), is("2020-08-19"));
//        assertThat(resultsShared.getHearing().getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(0).getOffence().getAquittalDate().toString(), is("2020-08-19"));
    }

    @Test
    public void shouldShareResultsWithAcquittalDateForOffenceLevelJudicialResultsWhenCourtOrderExist() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_SHARED_TO_SET_ACQUITTAL_DATE_FOR_COURTORDER, ResultsShared.class);

        target.shareResults(dummyEnvelope, sender, resultsShared);

        verify(sender, times(1)).send(envelopeArgumentCaptor.capture());
        assertThat(resultsShared.getTargets().get(0).getResultLines().get(0).getOrderedDate().toString(), is("2020-08-19"));
//        assertThat(resultsShared.getHearing().getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getAquittalDate().toString(), is("2020-08-19"));
    }

    @Test
    public void shouldShareResultsWithoutUpdatingAcquittalDateForOffenceLevelJudicialResults() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_SHARED_WITH_ACQUITTAL_DATE, ResultsShared.class);

        target.shareResults(dummyEnvelope, sender, resultsShared);

        verify(sender, times(1)).send(envelopeArgumentCaptor.capture());
        assertThat(resultsShared.getTargets().get(0).getResultLines().get(0).getOrderedDate().toString(), is("2020-08-19"));
        assertThat(resultsShared.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getAquittalDate().toString(), is("2020-08-18"));
    }

    @Test
    public void shouldShareResultsWithoutUpdatingAcquittalDateForOffenceLevelJudicialResultsWhenCourtApplicationCasesExist() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_SHARED_WITH_ACQUITTAL_DATE_FOR_COURTAPPLICATIONCASES, ResultsShared.class);

        target.shareResults(dummyEnvelope, sender, resultsShared);

        verify(sender, times(1)).send(envelopeArgumentCaptor.capture());
        assertThat(resultsShared.getTargets().get(0).getResultLines().get(0).getOrderedDate().toString(), is("2020-08-19"));
        assertThat(resultsShared.getHearing().getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(0).getAquittalDate().toString(), is("2020-08-18"));
    }

    @Test
    public void shouldShareResultsWithoutUpdatingAcquittalDateForOffenceLevelJudicialResultsWhenCourtOrderExists() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_SHARED_WITH_ACQUITTAL_DATE_FOR_COURTORDER, ResultsShared.class);

        target.shareResults(dummyEnvelope, sender, resultsShared);

        verify(sender, times(1)).send(envelopeArgumentCaptor.capture());
        assertThat(resultsShared.getTargets().get(0).getResultLines().get(0).getOrderedDate().toString(), is("2020-08-19"));
        assertThat(resultsShared.getHearing().getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getAquittalDate().toString(), is("2020-08-18"));
    }
    @Test
    public void shouldOnlyPopulateDefendantLevelResultsWhenResultLinesPresentForDefendantLevel() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_DEFENDANT_LEVEL_SHARED_JSON, ResultsShared.class);
        final JudicialResult judicialResult1 = judicialResult().withJudicialResultId(randomUUID()).build();

        // deliberately polluting state of hearing with stale results
        resultsShared.getHearing().getProsecutionCases().get(0).getDefendants().get(0).setDefendantCaseJudicialResults(asList(judicialResult1));

        final JsonEnvelope envelope = getEnvelope(resultsShared);
        target.shareResults(envelope, sender, resultsShared);

        verify(sender).send(envelopeArgumentCaptor.capture());

        final Envelope<JsonObject> sharedResultsMessage = envelopeArgumentCaptor.getValue();

        assertThat(sharedResultsMessage.metadata().name(), is("public.hearing.resulted"));

        assertThat(resultsShared.getHearing().getDefendantJudicialResults(), hasSize(2));
        assertThat(resultsShared.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getDefendantCaseJudicialResults(), nullValue());
        assertThat(resultsShared.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults(), nullValue());
    }

    @Test
    public void shouldOnlyPopulateCaseLevelResultsWhenResultLinesPresentForCaseLevel() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_CASE_LEVEL_SHARED_JSON, ResultsShared.class);
        final JudicialResult judicialResult1 = judicialResult().withJudicialResultId(randomUUID()).build();
        final DefendantJudicialResult defendantJudicialResult = DefendantJudicialResult.defendantJudicialResult().withMasterDefendantId(randomUUID()).withJudicialResult(judicialResult1).build();
        // deliberately polluting state of hearing with stale results
        resultsShared.getHearing().setDefendantJudicialResults(Arrays.asList(defendantJudicialResult));
        resultsShared.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setJudicialResults(asList(judicialResult1));

        final JsonEnvelope envelope = getEnvelope(resultsShared);
        target.shareResults(envelope, sender, resultsShared);

        verify(sender).send(envelopeArgumentCaptor.capture());

        final Envelope<JsonObject> sharedResultsMessage = envelopeArgumentCaptor.getValue();

        assertThat(sharedResultsMessage.metadata().name(), is("public.hearing.resulted"));

        assertThat(resultsShared.getHearing().getDefendantJudicialResults(), nullValue());
        assertThat(resultsShared.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getDefendantCaseJudicialResults(), hasSize(2));
        assertThat(resultsShared.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults(), nullValue());
    }

    @Test
    public void shouldOnlyPopulateOffenceLevelResultsWhenResultLinesPresentForOffenceLevel() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_SHARED_JSON, ResultsShared.class);
        final JudicialResult judicialResult1 = judicialResult().withJudicialResultId(randomUUID()).build();
        final DefendantJudicialResult defendantJudicialResult = DefendantJudicialResult.defendantJudicialResult().withMasterDefendantId(randomUUID()).withJudicialResult(judicialResult1).build();

        // deliberately polluting state of hearing with stale results
        resultsShared.getHearing().setDefendantJudicialResults(asList(defendantJudicialResult));
        resultsShared.getHearing().getProsecutionCases().get(0).getDefendants().get(0).setDefendantCaseJudicialResults(asList(judicialResult1));

        final JsonEnvelope envelope = getEnvelope(resultsShared);
        target.shareResults(envelope, sender, resultsShared);

        verify(sender).send(envelopeArgumentCaptor.capture());

        final Envelope<JsonObject> sharedResultsMessage = envelopeArgumentCaptor.getValue();

        assertThat(sharedResultsMessage.metadata().name(), is("public.hearing.resulted"));

        assertThat(resultsShared.getHearing().getDefendantJudicialResults(), nullValue());
        assertThat(resultsShared.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getDefendantCaseJudicialResults(), nullValue());
        assertThat(resultsShared.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults(), hasSize(2));
    }

    private void setJudicialResultsWithCategoryOf(final ResultsShared expected, final JudicialResultCategory category) {
        final List<JudicialResult> judicialResultList = new ArrayList<>();
        judicialResultList.add(judicialResult().withCategory(JudicialResultCategory.INTERMEDIARY).withCjsCode("cjsCode1").build());
        judicialResultList.add(judicialResult().withCategory(category).withCjsCode("cjsCode2").build());
        expected.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setJudicialResults(judicialResultList);
    }

    private Boolean getIsDisposedValueForOffence(final Hearing hearingIn) {
        return hearingIn.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getIsDisposed();
    }
}
