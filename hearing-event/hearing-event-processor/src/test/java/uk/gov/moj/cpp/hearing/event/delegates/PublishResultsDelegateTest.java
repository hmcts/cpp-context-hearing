package uk.gov.moj.cpp.hearing.event.delegates;

import static java.lang.System.lineSeparator;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.core.courts.JudicialResult.judicialResult;
import static uk.gov.justice.core.courts.JudicialResultPrompt.judicialResultPrompt;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.metadataFor;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import uk.gov.justice.core.courts.Category;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.JudicialRole;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.hearing.courts.referencedata.CourtCentreOrganisationUnit;
import uk.gov.justice.hearing.courts.referencedata.Courtrooms;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.domain.event.result.PublicHearingResulted;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.delegates.helper.BailStatusHelper;
import uk.gov.moj.cpp.hearing.event.delegates.helper.NextHearingHelper;
import uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.RestructuringHelper;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllFixedList;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.FixedList;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinitionRule;
import uk.gov.moj.cpp.hearing.event.relist.RelistReferenceDataService;
import uk.gov.moj.cpp.hearing.event.service.BailStatusReferenceDataLoader;
import uk.gov.moj.cpp.hearing.event.service.CourtHouseReverseLookup;
import uk.gov.moj.cpp.hearing.event.service.CourtRoomOuCodeReverseLookup;
import uk.gov.moj.cpp.hearing.event.service.FixedListLookup;
import uk.gov.moj.cpp.hearing.event.service.HearingTypeReverseLookup;
import uk.gov.moj.cpp.hearing.event.service.LjaReferenceDataLoader;
import uk.gov.moj.cpp.hearing.event.service.NowsReferenceCache;
import uk.gov.moj.cpp.hearing.event.service.NowsReferenceDataLoader;
import uk.gov.moj.cpp.hearing.event.service.NowsReferenceDataServiceImpl;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;
import uk.gov.moj.cpp.hearing.test.FileResourceObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.json.JsonObject;
import javax.json.JsonValue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class PublishResultsDelegateTest {

    @Spy
    private final Enveloper enveloper = createEnveloper();
    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
    @Spy
    @InjectMocks
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter();
    @Spy
    @InjectMocks
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();
    private final int COURT_ROOM_ID = 54321;
    private final String courtRoomName = "ROOM A";
    private final String courtName = "Wimbledon Magistrates Court";
    private final String hearingTypeDescription = "Plea & Trial Preparation";
    private final Courtrooms expectedCourtRoomResult = Courtrooms.courtrooms()
            .withCourtroomId(COURT_ROOM_ID)
            .withCourtroomName(courtRoomName)
            .withId(randomUUID())
            .build();
    private final CourtCentreOrganisationUnit expectedCourtHouseByNameResult = CourtCentreOrganisationUnit.courtCentreOrganisationUnit()
            .withId(randomUUID().toString())
            .withLja("3255")
            .withOucodeL3Name(courtName)
            .withOucode("B47GL")
            .withCourtrooms(asList(expectedCourtRoomResult))
            .withAddress1("Address1")
            .withAddress2("Address2")
            .withAddress3("Address3")
            .withAddress4("Address4")
            .withAddress5("Address5")
            .withPostcode("UB10 0HB")
            .build();
    private final HearingType hearingType = new HearingType(hearingTypeDescription, randomUUID(), hearingTypeDescription);
    @InjectMocks
    private final NextHearingHelper nextHearingHelper = new NextHearingHelper();
    private final NowsReferenceCache nowsReferenceCache = new NowsReferenceCache();
    @InjectMocks
    private final NowsReferenceDataLoader nowsReferenceDataLoader = new NowsReferenceDataLoader();
    private ReferenceDataService referenceDataService;
    @Mock
    private BailStatusHelper bailStatusHelper;
    @Mock
    private RelistReferenceDataService relistReferenceDataService;
    @Mock
    private Sender sender;
    @Mock
    private CustodyTimeLimitCalculator custodyTimeLimitCalculator;
    @Captor
    private ArgumentCaptor<Envelope> envelopeArgumentCaptor;
    @Captor
    private ArgumentCaptor<Hearing> custodyLimitCalculatorHearingIn;
    private PublishResultsDelegate publishResultsDelegate;
    @Mock
    private Requester requester;
    @Mock
    private LjaReferenceDataLoader ljaReferenceDataLoader;
    @Mock
    private BailStatusReferenceDataLoader bailStatusReferenceDataLoader;
    private List<ResultDefinitionRule> resultDefinitionRules;
    private List<ResultDefinition> resultDefinitions;
    private List<FixedList> fixedLists;
    private FileResourceObjectMapper fileResourceObjectMapper;
    private LocalDate referenceDate;
    private JsonEnvelope context;
    @Mock
    private HearingTypeReverseLookup hearingTypeReverseLookup;
    @Mock
    private CourtHouseReverseLookup courtHouseReverseLookup;
    @Mock
    private CourtRoomOuCodeReverseLookup courtRoomOuCodeReverseLookup;
    private RestructuringHelper restructringHelper;

    @Mock
    private FixedListLookup fixedListLookup;

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);

        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.nowsReferenceCache, "nowsReferenceDataLoader", nowsReferenceDataLoader);

        fileResourceObjectMapper = new FileResourceObjectMapper();
        resultDefinitions = fileResourceObjectMapper.convertFromFile("result-definitions.json", AllResultDefinitions.class).getResultDefinitions();
        fixedLists = fileResourceObjectMapper.convertFromFile("fixed-list.json", AllFixedList.class).getFixedListCollection();

        referenceDate = PAST_LOCAL_DATE.next();
        context = envelopeFrom(metadataWithRandomUUID("something"), JsonValue.NULL);

        final JsonEnvelope resultEnvelopeDefinition = envelopeFrom(metadataWithRandomUUID("something"), objectToJsonObjectConverter.convert(new AllResultDefinitions().setResultDefinitions(resultDefinitions)));

        final Metadata metadata = metadataFor("something", UUID.randomUUID());
        final Envelope fixedListsEnvelopeDefinition = uk.gov.justice.services.messaging.Envelope.envelopeFrom(metadata, new AllFixedList().setFixedListCollection(fixedLists));
        when(requester.requestAsAdmin(any())).thenReturn(resultEnvelopeDefinition);

        when(requester.request(any(), any())).thenReturn(fixedListsEnvelopeDefinition);

        referenceDataService = new NowsReferenceDataServiceImpl(nowsReferenceCache, ljaReferenceDataLoader, fixedListLookup, bailStatusReferenceDataLoader);

        setField(nextHearingHelper, "referenceDataService", referenceDataService);
        restructringHelper = new RestructuringHelper(referenceDataService, nextHearingHelper);

        publishResultsDelegate = new PublishResultsDelegate(enveloper, objectToJsonObjectConverter, referenceDataService, relistReferenceDataService, custodyTimeLimitCalculator, bailStatusHelper, restructringHelper);


    }

    @Test
    public void shareResults() throws IOException {

        final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt promptReferenceData =
                uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt.prompt()
                        .setId(randomUUID())
                        .setLabel("promptReferenceData0")
                        .setWelshLabel("welshLabel")
                        .setType("CURR")
                        .setUserGroups(Arrays.asList("usergroup0", "usergroup1"))
                        .setWelshLabel("welshLabel");
        doNothing().when(bailStatusHelper).mapBailStatuses(any(JsonEnvelope.class), any(ResultsShared.class));
        final ResultsShared resultsShared = resultShared();

        final Prompt prompt0 = Prompt.prompt()
                .withLabel(promptReferenceData.getLabel())
                .withValue("400")
                .withWelshValue("welshValue")
                .withId(promptReferenceData.getId())
                .withFixedListCode("fixedListCode0")
                .build();
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));

        when(courtHouseReverseLookup.getCourtCentreByName(any(), any())).thenReturn(ofNullable(expectedCourtHouseByNameResult));
        when(courtHouseReverseLookup.getCourtRoomByRoomName(expectedCourtHouseByNameResult, courtRoomName)).thenReturn(ofNullable(expectedCourtRoomResult));
        when(courtRoomOuCodeReverseLookup.getcourtRoomOuCode(event, 291, "B47GL")).thenReturn("B47GL00");
        when(hearingTypeReverseLookup.getHearingTypeByName(event, hearingTypeDescription)).thenReturn(hearingType);


        when(relistReferenceDataService.getWithdrawnResultDefinitionUuids(any(JsonEnvelope.class), any(LocalDate.class))).thenReturn(new ArrayList<>());

        final JsonEnvelope context = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"), objectToJsonObjectConverter.convert(resultsShared));

        when(relistReferenceDataService.getWithdrawnResultDefinitionUuids(any(JsonEnvelope.class), any(LocalDate.class))).thenReturn(new ArrayList<>());


        //the actual test !!!
        publishResultsDelegate.shareResults(context, sender, resultsShared);

        verify(sender).send(envelopeArgumentCaptor.capture());

        final Envelope<JsonObject> sharedResultsMessage = envelopeArgumentCaptor.getValue();

        assertThat(sharedResultsMessage.metadata().name(), is("public.hearing.resulted"));

        final PublicHearingResulted publicHearingResulted = jsonObjectToObjectConverter.convert(sharedResultsMessage.payload(), PublicHearingResulted.class);

        final Hearing hearingIn = resultsShared.getHearing();


        verify(custodyTimeLimitCalculator, times(1)).calculate(custodyLimitCalculatorHearingIn.capture());
        final Hearing calHearingIn = custodyLimitCalculatorHearingIn.getValue();
        Assert.assertEquals(resultsShared.getHearing(), calHearingIn);


        final Optional<Defendant> defendant = resultsShared.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream()).findFirst();
        assertThat(defendant.isPresent(), is(true));
    }

    @Test
    public void whenAnyJudicialResulCategorytIsFinal_Then_IsDiposed_Should_BeSetToTrue() throws IOException {

        final ResultsShared resultsShared = resultShared();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));

        setJudicialResultsWithCategoryOf(resultsShared, Category.FINAL);

        when(courtHouseReverseLookup.getCourtCentreByName(any(), any())).thenReturn(ofNullable(expectedCourtHouseByNameResult));
        when(courtHouseReverseLookup.getCourtRoomByRoomName(expectedCourtHouseByNameResult, courtRoomName)).thenReturn(ofNullable(expectedCourtRoomResult));
        when(courtRoomOuCodeReverseLookup.getcourtRoomOuCode(event, 291, "B47GL")).thenReturn("B47GL00");
        when(hearingTypeReverseLookup.getHearingTypeByName(event, hearingTypeDescription)).thenReturn(hearingType);

        publishResultsDelegate.shareResults(context, sender, resultsShared);

        verify(sender).send(envelopeArgumentCaptor.capture());

        final Envelope<JsonObject> sharedResultsMessage = envelopeArgumentCaptor.getValue();

        final PublicHearingResulted publicHearingResulted = jsonObjectToObjectConverter.convert(sharedResultsMessage.payload(), PublicHearingResulted.class);

        verify(sender, times(1)).send(envelopeArgumentCaptor.capture());

    }


    @Test
    public void whenAnyJudicialResultsHaveResultsPrompts_Then_BailConditions_Should_BeSetBasedOnRank() throws IOException {

        final ResultsShared resultsShared = resultShared();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));

        when(courtHouseReverseLookup.getCourtCentreByName(any(), any())).thenReturn(ofNullable(expectedCourtHouseByNameResult));
        when(courtHouseReverseLookup.getCourtRoomByRoomName(expectedCourtHouseByNameResult, courtRoomName)).thenReturn(ofNullable(expectedCourtRoomResult));
        when(courtRoomOuCodeReverseLookup.getcourtRoomOuCode(event, 291, "B47GL")).thenReturn("B47GL00");
        when(hearingTypeReverseLookup.getHearingTypeByName(event, hearingTypeDescription)).thenReturn(hearingType);

        publishResultsDelegate.shareResults(context, sender, resultsShared);

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
                        // no nested or detailed check because shareResults just copies the array references
                        .withValue(sh -> sh.getJudiciary().size(), hearingIn.getJudiciary().size())
                        .with(Hearing::getJudiciary, first(isBean(JudicialRole.class)
                                .withValue(JudicialRole::getJudicialId, hearingIn.getJudiciary().get(0).getJudicialId())
                        ))
                        // no nested or detailed check because shareResults just copies the array references
                        //TODO uncomment these 3 lines below
//                        .withValue(sh -> sh.getDefenceCounsels().size(), hearingIn.getDefenceCounsels().size())
//                        .with(Hearing::getDefenceCounsels, first(isBean(DefenceCounsel.class)
//                                .withValue(DefenceCounsel::getId, hearingIn.getDefenceCounsels().get(0).getId())
//                        ))
                        // no nested or detailed check because shareResults just copies the array references
                        .withValue(sh -> sh.getProsecutionCases().size(), hearingIn.getProsecutionCases().size())
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                        .withValue(ProsecutionCase::getId, hearingIn.getProsecutionCases().get(0).getId())
                                        .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)))
                                        .withValue(def -> def.getDefendants().size(), hearingIn.getProsecutionCases().get(0).getDefendants().size())
                                        .with(off -> off.getDefendants().get(0).getOffences(), first(isBean(Offence.class)))
                                        .with(off -> off.getDefendants().get(0).getOffences().get(0).getJudicialResults(), first(isBean(JudicialResult.class)))
                                //.withValue(off -> off.getDefendants().get(0).getPersonDefendant().getBailConditions(), bailCondition)
                        ))));
    }

    @Test
    public void whenNoJudicialResultArePresent_Then_IsDisposed_Flag_ShouldBe_False() throws IOException {

        final ResultsShared resultsShared = resultShared();

        resultsShared.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setJudicialResults(Collections.EMPTY_LIST);

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));

        when(courtHouseReverseLookup.getCourtCentreByName(any(), any())).thenReturn(ofNullable(expectedCourtHouseByNameResult));
        when(courtHouseReverseLookup.getCourtRoomByRoomName(expectedCourtHouseByNameResult, courtRoomName)).thenReturn(ofNullable(expectedCourtRoomResult));
        when(courtRoomOuCodeReverseLookup.getcourtRoomOuCode(event, 291, "B47GL")).thenReturn("B47GL00");
        when(hearingTypeReverseLookup.getHearingTypeByName(event, hearingTypeDescription)).thenReturn(hearingType);

        // Actual Method under Test
        publishResultsDelegate.shareResults(context, sender, resultsShared);

        verify(sender).send(envelopeArgumentCaptor.capture());

        final Envelope<JsonObject> sharedResultsMessage = envelopeArgumentCaptor.getValue();

        final PublicHearingResulted publicHearingResulted = jsonObjectToObjectConverter.convert(sharedResultsMessage.payload(), PublicHearingResulted.class);
        final ArgumentCaptor<Envelope> envelopeArgumentCaptor = ArgumentCaptor.forClass(Envelope.class);
        verify(sender, times(1)).send(envelopeArgumentCaptor.capture());


        assertFalse(getIsDisposedValueForOffence(publicHearingResulted.getHearing()));
    }


    @Test
    public void whenJudicialResultCagtegory_Is_NotFinal_Then_Offence_IsDisposed_isFalse() throws IOException {

        final ResultsShared resultsShared = resultShared();

        setJudicialResultsWithCategoryOf(resultsShared, Category.ANCILLARY);

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));

        when(courtHouseReverseLookup.getCourtCentreByName(any(), any())).thenReturn(ofNullable(expectedCourtHouseByNameResult));
        when(courtHouseReverseLookup.getCourtRoomByRoomName(expectedCourtHouseByNameResult, courtRoomName)).thenReturn(ofNullable(expectedCourtRoomResult));
        when(courtRoomOuCodeReverseLookup.getcourtRoomOuCode(event, 291, "B47GL")).thenReturn("B47GL00");
        when(hearingTypeReverseLookup.getHearingTypeByName(event, hearingTypeDescription)).thenReturn(hearingType);

        // Actual Method under Test
        publishResultsDelegate.shareResults(context, sender, resultsShared);

        verify(sender).send(envelopeArgumentCaptor.capture());

        final Envelope<JsonObject> sharedResultsMessage = envelopeArgumentCaptor.getValue();

        final PublicHearingResulted publicHearingResulted = jsonObjectToObjectConverter.convert(sharedResultsMessage.payload(), PublicHearingResulted.class);

        verify(sender, times(1)).send(envelopeArgumentCaptor.capture());

        assertFalse(getIsDisposedValueForOffence(publicHearingResulted.getHearing()));

    }


    @Test
    public void resultsShared_withCADate() throws IOException {

        final ResultsShared resultsShared = resultShared();

        final ResultLine resultLine = resultsShared.getTargets().get(0).getResultLines().get(0);

        final UUID promptId = resultLine.getPrompts().get(0).getId();

        resultLine.setDelegatedPowers(DelegatedPowers.delegatedPowers().withUserId(UUID.randomUUID()).build());

        final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt promptReferenceData = setPromptReferenceData(promptId);

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));

        when(courtHouseReverseLookup.getCourtCentreByName(any(), any())).thenReturn(ofNullable(expectedCourtHouseByNameResult));
        when(courtHouseReverseLookup.getCourtRoomByRoomName(expectedCourtHouseByNameResult, courtRoomName)).thenReturn(ofNullable(expectedCourtRoomResult));
        when(courtRoomOuCodeReverseLookup.getcourtRoomOuCode(event, 291, "B47GL")).thenReturn("B47GL00");
        when(hearingTypeReverseLookup.getHearingTypeByName(event, hearingTypeDescription)).thenReturn(hearingType);

        publishResultsDelegate.shareResults(context, sender, resultsShared);

        verify(sender, times(1)).send(envelopeArgumentCaptor.capture());

        assertThat(resultsShared.getTargets().get(0).getResultLines().get(0).getOrderedDate().toString(), is("2019-11-08"));

    }

    @Test
    public void resultsShared_withResultDefinitionHavingNoPrompts() throws IOException {

        final ResultsShared resultsShared = resultShared("hearing.results-shared-with-no-prompts.json");

        final ResultLine resultLine = resultsShared.getTargets().get(0).getResultLines().get(0);

        resultLine.setDelegatedPowers(DelegatedPowers.delegatedPowers().withUserId(UUID.randomUUID()).build());

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));

        when(courtHouseReverseLookup.getCourtCentreByName(any(), any())).thenReturn(ofNullable(expectedCourtHouseByNameResult));
        when(courtHouseReverseLookup.getCourtRoomByRoomName(expectedCourtHouseByNameResult, courtRoomName)).thenReturn(ofNullable(expectedCourtRoomResult));
        when(courtRoomOuCodeReverseLookup.getcourtRoomOuCode(event, 291, "B47GL")).thenReturn("B47GL00");
        when(hearingTypeReverseLookup.getHearingTypeByName(event, hearingTypeDescription)).thenReturn(hearingType);

        publishResultsDelegate.shareResults(context, sender, resultsShared);

        verify(sender, times(1)).send(envelopeArgumentCaptor.capture());

        assertThat(resultsShared.getTargets().get(0).getResultLines().get(0).getOrderedDate().toString(), is("2019-11-08"));
        assertThat(resultsShared.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults().get(0).getJudicialResultPrompts(), nullValue());
        assertThat(resultsShared.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getJudicialResults().get(0).getJudicialResultPrompts(), nullValue());

    }

    private void setJudicialResultsWithCategoryOf(final ResultsShared expected, final Category category) {
        final List<JudicialResult> judicialResultList = new ArrayList<>();
        judicialResultList.add(judicialResult().withCategory(Category.INTERMEDIARY).withCjsCode("cjsCode1").build());
        judicialResultList.add(judicialResult().withCategory(category).withCjsCode("cjsCode2").build());
        expected.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setJudicialResults(judicialResultList);
    }

    private List<JudicialResult> getJudicialResultsForBailConditions() {
        final List<JudicialResult> judicialResults = new ArrayList<>();
        final List<JudicialResultPrompt> judicialResultPromptsList = new ArrayList<>();
        judicialResultPromptsList.add(judicialResultPrompt().withPromptSequence(new BigDecimal(1)).withLabel("Time of hearing").withValue("777").build());
        judicialResultPromptsList.add(judicialResultPrompt().withPromptSequence(new BigDecimal(2)).withLabel("Date of hearing").withValue("888").build());
        judicialResults.add(judicialResult()
                .withResultDefinitionGroup("Bail Conditions")
                .withLabel("Time of hearing").withRank(new BigDecimal(1))
                .withJudicialResultPrompts(judicialResultPromptsList)
                .build());
        return judicialResults;
    }

    private ResultsShared resultShared() throws IOException {
        return resultShared("hearing.results-shared.json");
    }

    private ResultsShared resultShared(final String filename) throws IOException {
        return fileResourceObjectMapper.convertFromFile(filename, ResultsShared.class);
    }

    private uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt setPromptReferenceData(final UUID promptId) {
        return uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt.prompt()
                .setId(promptId)
                .setLabel("promptReferenceData0")
                .setReference("CADATE")
                .setUserGroups(Arrays.asList("usergroup0", "usergroup1"));
    }

    private Boolean getIsDisposedValueForOffence(final Hearing hearingIn) {
        return hearingIn.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getIsDisposed();
    }


}
