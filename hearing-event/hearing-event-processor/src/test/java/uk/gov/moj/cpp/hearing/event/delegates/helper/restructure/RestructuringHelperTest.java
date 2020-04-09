package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.core.courts.DelegatedPowers.delegatedPowers;
import static uk.gov.justice.core.courts.Hearing.hearing;
import static uk.gov.justice.core.courts.ResultLine.resultLine;
import static uk.gov.justice.core.courts.Target.target;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared.builder;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.metadataFor;

import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.hearing.courts.referencedata.CourtCentreOrganisationUnit;
import uk.gov.justice.hearing.courts.referencedata.Courtrooms;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.delegates.helper.NextHearingHelper;
import uk.gov.moj.cpp.hearing.event.delegates.helper.ResultQualifier;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllFixedList;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.FixedList;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinitionRule;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import javax.json.JsonValue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class RestructuringHelperTest {

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

    private final NowsReferenceCache nowsReferenceCache = new NowsReferenceCache();
    @InjectMocks
    private final NowsReferenceDataLoader nowsReferenceDataLoader = new NowsReferenceDataLoader();
    @Spy
    private final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();
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
    @Mock
    private LjaReferenceDataLoader ljaReferenceDataLoader;
    @Mock
    private BailStatusReferenceDataLoader bailStatusReferenceDataLoader;
    @Mock
    private FixedListLookup fixedListLookup;
    private ReferenceDataService referenceDataService;
    @Captor
    private ArgumentCaptor<Envelope> envelopeArgumentCaptor;
    @Mock
    private Requester requester;

    private RestructuringHelper restructringHelper;

    private List<ResultDefinitionRule> resultDefinitionRules;
    private List<ResultDefinition> resultDefinitions;
    private LocalDate referenceDate;
    private JsonEnvelope context;
    private FileResourceObjectMapper fileResourceObjectMapper;
    @Mock
    private HearingTypeReverseLookup hearingTypeReverseLookup;
    @Mock
    private CourtHouseReverseLookup courtHouseReverseLookup;
    @Mock
    private CourtRoomOuCodeReverseLookup courtRoomOuCodeReverseLookup;
    private List<FixedList> fixedLists;

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);

        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.nowsReferenceCache, "nowsReferenceDataLoader", nowsReferenceDataLoader);

        referenceDataService = new NowsReferenceDataServiceImpl(nowsReferenceCache, ljaReferenceDataLoader, fixedListLookup, bailStatusReferenceDataLoader);
        fileResourceObjectMapper = new FileResourceObjectMapper();
        resultDefinitions = fileResourceObjectMapper.convertFromFile("result-definitions.json", AllResultDefinitions.class).getResultDefinitions();

        final JsonEnvelope resultEnvelopeDefinition = envelopeFrom(metadataWithRandomUUID("something"), objectToJsonObjectConverter.convert(new AllResultDefinitions().setResultDefinitions(resultDefinitions)));
        when(requester.requestAsAdmin(any())).thenReturn(resultEnvelopeDefinition);
        fixedLists = fileResourceObjectMapper.convertFromFile("fixed-list.json", AllFixedList.class).getFixedListCollection();
        final Metadata metadata = metadataFor("something", UUID.randomUUID());
        final Envelope fixedListsEnvelopeDefinition = uk.gov.justice.services.messaging.Envelope.envelopeFrom(metadata, new AllFixedList().setFixedListCollection(fixedLists));
        when(requester.request(any(), any())).thenReturn(fixedListsEnvelopeDefinition);
        referenceDate = PAST_LOCAL_DATE.next();
        context = envelopeFrom(metadataWithRandomUUID("something"), JsonValue.NULL);

        setField(nextHearingHelper, "referenceDataService", referenceDataService);
        restructringHelper = new RestructuringHelper(referenceDataService, nextHearingHelper);
    }


    @Test
    public void shouldBuildSimpleTwoLayerTree() {

        final String REMAND_IN_CUSTODY_ID = "0056b9e1-7585-4bfa-82ec-f06202670bb1";
        final String REMANDED_IN_CUSTODY_ID = "d0a369c9-5a28-40ec-99cb-da7943550b18";
        final String REMANDED_IN_CUSTODY_TO_HOSPITAL_ID = "e3315a27-35fd-4c43-8ba6-8b5d69aa96fb";

        final ResultDefinition remandInCustodyDefinition = this.resultDefinitions.stream().filter(def -> REMAND_IN_CUSTODY_ID.equals(def.getId().toString())).findFirst().get();
        final ResultDefinition remandedInCustodyDefinition = this.resultDefinitions.stream().filter(def -> REMANDED_IN_CUSTODY_ID.equals(def.getId().toString())).findFirst().get();
        final ResultDefinition remandInCustodyToHospitalDefinition = this.resultDefinitions.stream().filter(def -> REMANDED_IN_CUSTODY_TO_HOSPITAL_ID.equals(def.getId().toString())).findFirst().get();

        final List<Prompt> remandInCustodyPrompts = remandInCustodyDefinition
                .getPrompts()
                .stream()
                .map(p -> new Prompt(null, p.getId(), null, null, null, null))
                .collect(toList());

        final List<Prompt> remandedInCustodyPrompts = remandedInCustodyDefinition
                .getPrompts()
                .stream()
                .map(p -> new Prompt(null, p.getId(), null, null, null, null))
                .collect(toList());

        final List<Prompt> remandInCustodyToHospitalPrompts = remandInCustodyToHospitalDefinition
                .getPrompts()
                .stream()
                .map(p -> new Prompt(null, p.getId(), null, null, null, null))
                .collect(toList());

        final ResultsShared resultsShared = builder()
                .withTargets(
                        asList(target()
                                .withResultLines(asList(
                                        resultLine()
                                                .withResultDefinitionId(fromString(REMAND_IN_CUSTODY_ID))
                                                .withPrompts(remandInCustodyPrompts)
                                                .withOrderedDate(referenceDate)
                                                .withResultLineId(randomUUID())
                                                .build(),
                                        resultLine()
                                                .withResultDefinitionId(fromString(REMANDED_IN_CUSTODY_ID))
                                                .withPrompts(remandedInCustodyPrompts)
                                                .withOrderedDate(referenceDate)
                                                .withResultLineId(randomUUID())
                                                .build(),
                                        resultLine()
                                                .withResultDefinitionId(fromString(REMANDED_IN_CUSTODY_TO_HOSPITAL_ID))
                                                .withPrompts(remandInCustodyToHospitalPrompts)
                                                .withOrderedDate(referenceDate)
                                                .withResultLineId(randomUUID())
                                                .build())
                                )

                                .build())
                ).withCourtClerk(delegatedPowers()
                        .withUserId(randomUUID())
                        .withFirstName("ClerkFirstName")
                        .withLastName("ClerkLasttName")
                        .build()
                ).withHearing(hearing().build())
                .build();


        final List<TreeNode<ResultLine>> results = restructringHelper.buildResultTreeFromPayload(context, resultsShared);
        assertThat(results.stream().filter(jr -> fromString(REMAND_IN_CUSTODY_ID).equals(jr.getResultDefinitionId())).findAny().get().getChildren().size(), is(0));
        assertThat(results.size(), is(3));
    }

    @Test
    public void caseWithOneDefendantAndOneOffence() throws IOException {
        final String REMANDED_ON_CONDITIONAL_BAIL_ID = "3a529001-2f43-45ba-a0a8-d3ced7e9e7ad";
        final String NEXT_HEARING_ID = "f00359b5-7303-403b-b59e-0b1a1daa89bc";
        final String NEXT_HEARING_IN_CROWN_COURT_ID = "fbed768b-ee95-4434-87c8-e81cbc8d24c8";
        final String NEXT_HEARING_IN_MAGISTRATE_COURT_ID = "70c98fa6-804d-11e8-adc0-fa7ae01bbebc";

        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile("hearing.results-shared.json", ResultsShared.class);

        final JsonEnvelope event = JsonEnvelope.envelopeFrom(
                Envelope.metadataBuilder().withId(randomUUID()).withName("hearing.results-shared").build(),
                objectToJsonObjectConverter.convert(resultsShared));
        when(courtHouseReverseLookup.getCourtCentreByName(any(), any())).thenReturn(ofNullable(expectedCourtHouseByNameResult));
        when(courtHouseReverseLookup.getCourtRoomByRoomName(expectedCourtHouseByNameResult, courtRoomName)).thenReturn(ofNullable(expectedCourtRoomResult));
        when(courtRoomOuCodeReverseLookup.getcourtRoomOuCode(event, 291, "B47GL")).thenReturn("B47GL00");
        when(hearingTypeReverseLookup.getHearingTypeByName(event, hearingTypeDescription)).thenReturn(hearingType);

        final List<TreeNode<ResultLine>> results = restructringHelper.buildResultTreeFromPayload(context, resultsShared);

        assertThat(results.size(), is(11));

        final List<TreeNode<ResultLine>> topLevelParents = results
                .stream()
                .filter(r -> r.getParents().size() == 0 && r.getChildren().size() > 0)
                .collect(toList());

        assertThat(topLevelParents.size(), is(2));

        final TreeNode<ResultLine> remandedOnConditionalBailResult = results.stream().filter(jr -> fromString(REMANDED_ON_CONDITIONAL_BAIL_ID).equals(jr.getResultDefinitionId())).findAny().get();

        assertThat(remandedOnConditionalBailResult.getResultDefinitionId().toString(), is(REMANDED_ON_CONDITIONAL_BAIL_ID));
        assertThat(remandedOnConditionalBailResult.getChildren().size(), is(2));

        final TreeNode<ResultLine> nextHearingResult = remandedOnConditionalBailResult.getChildren().get(0);
        assertThat(nextHearingResult.getResultDefinitionId().toString(), is(NEXT_HEARING_ID));
        assertThat(nextHearingResult.getChildren().size(), is(5));

        final List<TreeNode<ResultLine>> nextHearingInCrownCourtResults = nextHearingResult.getChildren();

        assertTrue(nextHearingInCrownCourtResults
                .stream()
                .allMatch(r -> NEXT_HEARING_IN_CROWN_COURT_ID.equals(r.getResultDefinitionId().toString()) || NEXT_HEARING_IN_MAGISTRATE_COURT_ID.equals(r.getResultDefinitionId().toString())));
    }

    @Test
    public void shouldBuildMultipleTrees() throws IOException {
        final String REMANDED_ON_CONDITIONAL_BAIL_ID = "3a529001-2f43-45ba-a0a8-d3ced7e9e7ad";
        final String NEXT_HEARING_ID = "f00359b5-7303-403b-b59e-0b1a1daa89bc";
        final String NEXT_HEARING_IN_CROWN_COURT_ID = "fbed768b-ee95-4434-87c8-e81cbc8d24c8";
        final String NEXT_HEARING_IN_MAGISTRATE_COURT_ID = "70c98fa6-804d-11e8-adc0-fa7ae01bbebc";


        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile("hearing.results-shared_multiple_defendant.json", ResultsShared.class);
        final JsonEnvelope event = JsonEnvelope.envelopeFrom(
                Envelope.metadataBuilder().withId(randomUUID()).withName("hearing.results-shared").build(),
                objectToJsonObjectConverter.convert(resultsShared));
        when(courtHouseReverseLookup.getCourtCentreByName(any(), any())).thenReturn(ofNullable(expectedCourtHouseByNameResult));
        when(courtHouseReverseLookup.getCourtRoomByRoomName(expectedCourtHouseByNameResult, courtRoomName)).thenReturn(ofNullable(expectedCourtRoomResult));
        when(courtRoomOuCodeReverseLookup.getcourtRoomOuCode(event, 291, "B47GL")).thenReturn("B47GL00");
        when(hearingTypeReverseLookup.getHearingTypeByName(event, hearingTypeDescription)).thenReturn(hearingType);

        final List<TreeNode<ResultLine>> results = restructringHelper.buildResultTreeFromPayload(context, resultsShared);

        final List<TreeNode<ResultLine>> resultLinesTree = restructringHelper.buildResultTreeFromPayload(context, resultsShared);

        assertThat(results.size(), is(16));

        assertThat(resultLinesTree.size(), is(16));

        final List<TreeNode<ResultLine>> topLevelParents = results
                .stream()
                .filter(r -> r.getParents().size() == 0 && r.getChildren().size() > 0)
                .collect(toList());

        assertThat(topLevelParents.size(), is(4));

        final List<TreeNode<ResultLine>> topLevelResultLineParents = resultLinesTree
                .stream()
                .filter(r -> r.getParents().size() == 0 && r.getChildren().size() > 0)
                .collect(toList());

        assertThat(topLevelResultLineParents.size(), is(4));

        final TreeNode<ResultLine> remandedOnConditionalBailResult = results.stream().filter(jr -> fromString(REMANDED_ON_CONDITIONAL_BAIL_ID).equals(jr.getResultDefinitionId())).findAny().get();

        assertThat(remandedOnConditionalBailResult.getResultDefinitionId().toString(), is(REMANDED_ON_CONDITIONAL_BAIL_ID));
        assertThat(remandedOnConditionalBailResult.getChildren().size(), is(1));

        final TreeNode<ResultLine> nextHearingResult = remandedOnConditionalBailResult.getChildren().get(0);
        assertThat(nextHearingResult.getResultDefinitionId().toString(), is(NEXT_HEARING_ID));
        assertThat(nextHearingResult.getChildren().size(), is(1));

        final List<TreeNode<ResultLine>> nextHearingInCrownCourtResults = nextHearingResult.getChildren();

        assertTrue(nextHearingInCrownCourtResults
                .stream()
                .allMatch(r -> NEXT_HEARING_IN_CROWN_COURT_ID.equals(r.getResultDefinitionId().toString()) || NEXT_HEARING_IN_MAGISTRATE_COURT_ID.equals(r.getResultDefinitionId().toString())));
    }


    @Test
    public void SingleDefendantSingleOffenceOneImpOneTimpHearingResultShared() throws IOException {

        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile("imp-timp-hearing-results-shared.json", ResultsShared.class);
        final JsonEnvelope event = JsonEnvelope.envelopeFrom(
                Envelope.metadataBuilder().withId(randomUUID()).withName("hearing.results-shared").build(),
                objectToJsonObjectConverter.convert(resultsShared));
        when(courtHouseReverseLookup.getCourtCentreByName(any(), any())).thenReturn(ofNullable(expectedCourtHouseByNameResult));
        when(courtHouseReverseLookup.getCourtRoomByRoomName(expectedCourtHouseByNameResult, courtRoomName)).thenReturn(ofNullable(expectedCourtRoomResult));
        when(courtRoomOuCodeReverseLookup.getcourtRoomOuCode(event, 291, "B47GL")).thenReturn("B47GL00");
        when(hearingTypeReverseLookup.getHearingTypeByName(event, hearingTypeDescription)).thenReturn(hearingType);

        final List<TreeNode<ResultLine>> resultLinesTree = restructringHelper.buildResultTreeFromPayload(context, resultsShared);

        assertThat(resultLinesTree.size(), is(2));
        assertThat(resultLinesTree.get(0).getJudicialResult().getJudicialResultPrompts().size(), is(2));
        assertThat(resultLinesTree.get(1).getJudicialResult().getJudicialResultPrompts().size(), is(3));

        final List<TreeNode<ResultLine>> topLevelResultLineParents = resultLinesTree
                .stream()
                .filter(r -> r.getParents().size() == 0 && r.getChildren().size() > 0)
                .collect(toList());

        assertThat(topLevelResultLineParents.size(), is(1));

        assertThat(topLevelResultLineParents.get(0).getChildren().size(), is(1));

        final List<TreeNode<ResultLine>> restructuredTree = restructringHelper.restructure(context, resultsShared);

        assertThat(restructuredTree.size(), is(1));
        assertThat(restructuredTree.get(0).getJudicialResult().getJudicialResultPrompts().size(), is(5));
        assertThat(restructuredTree.get(0).getJudicialResult().getDelegatedPowers(), nullValue());
        assertThat(restructuredTree.get(0).getJudicialResult().getResultText().split("\\R").length, is(6));
        assertThat(restructuredTree.get(0).getJudicialResult().getJudicialResultTypeId(), is(topLevelResultLineParents.get(0).getResultDefinitionId()));
        assertTrue(restructuredTree.get(0).getJudicialResult().getJudicialResultPrompts().stream().allMatch(jrp -> nonNull(jrp.getJudicialResultPromptTypeId())));
    }

    @Test
    public void scenario1ShortCodeSendToCCOnCB() throws IOException {

        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile("scenario1-shortCode-SendToCCOnCB.json", ResultsShared.class);
        final JsonEnvelope event = JsonEnvelope.envelopeFrom(
                Envelope.metadataBuilder().withId(randomUUID()).withName("hearing.results-shared").build(),
                objectToJsonObjectConverter.convert(resultsShared));
        when(courtHouseReverseLookup.getCourtCentreByName(any(), any())).thenReturn(ofNullable(expectedCourtHouseByNameResult));
        when(courtHouseReverseLookup.getCourtRoomByRoomName(expectedCourtHouseByNameResult, courtRoomName)).thenReturn(ofNullable(expectedCourtRoomResult));
        when(courtRoomOuCodeReverseLookup.getcourtRoomOuCode(event, 291, "B47GL")).thenReturn("B47GL00");
        when(hearingTypeReverseLookup.getHearingTypeByName(event, hearingTypeDescription)).thenReturn(hearingType);

        final List<TreeNode<ResultLine>> resultLinesTree = restructringHelper.buildResultTreeFromPayload(context, resultsShared);

        assertThat(resultLinesTree.size(), is(9));

        final List<TreeNode<ResultLine>> topLevelResultLineParents = resultLinesTree
                .stream()
                .filter(r -> r.getParents().size() == 0 && r.getChildren().size() > 0)
                .collect(toList());

        assertThat(topLevelResultLineParents.size(), is(1));

        assertThat(topLevelResultLineParents.get(0).getChildren().size(), is(1));

        final List<TreeNode<ResultLine>> restructuredTree = restructringHelper.restructure(context, resultsShared);
        assertThat(restructuredTree.size(), is(3));
        final List<TreeNode<ResultLine>> topLevelResultLineRestructuredParents = restructuredTree
                .stream()
                .filter(r -> r.getParents().size() == 0 && r.getChildren().size() > 0)
                .collect(toList());
        assertThat(restructuredTree.stream().filter(node -> node.isStandalone()).collect(toList()).size(), is(1));

        assertThat(topLevelResultLineRestructuredParents.size(), is(1));

        restructuredTree.forEach(rl -> {
            List<JudicialResultPrompt> judicialResultPrompts = rl.getJudicialResult().getJudicialResultPrompts();
            if (judicialResultPrompts != null && !judicialResultPrompts.isEmpty()) {
                assertTrue(judicialResultPrompts.stream()
                        .filter(jrp -> StringUtils.isNotEmpty(jrp.getValue()))
                        .noneMatch(jrp -> jrp.getValue().contains(ResultQualifier.SEPARATOR)));
            }
        });
    }


    @Test
    public void coHearingEvent() throws IOException {

        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile("CO-hearing-event.json", ResultsShared.class);
        final JsonEnvelope event = JsonEnvelope.envelopeFrom(
                Envelope.metadataBuilder().withId(randomUUID()).withName("hearing.results-shared").build(),
                objectToJsonObjectConverter.convert(resultsShared));
        when(courtHouseReverseLookup.getCourtCentreByName(any(), any())).thenReturn(ofNullable(expectedCourtHouseByNameResult));
        when(courtHouseReverseLookup.getCourtRoomByRoomName(expectedCourtHouseByNameResult, courtRoomName)).thenReturn(ofNullable(expectedCourtRoomResult));
        when(courtRoomOuCodeReverseLookup.getcourtRoomOuCode(event, 291, "B47GL")).thenReturn("B47GL00");
        when(hearingTypeReverseLookup.getHearingTypeByName(event, hearingTypeDescription)).thenReturn(hearingType);

        final List<TreeNode<ResultLine>> resultLinesTree = restructringHelper.buildResultTreeFromPayload(context, resultsShared);

        assertThat(resultLinesTree.size(), is(2));

        final List<TreeNode<ResultLine>> topLevelResultLineParents = resultLinesTree
                .stream()
                .filter(r -> r.getParents().size() == 0 && r.getChildren().size() > 0)
                .collect(toList());

        assertThat(topLevelResultLineParents.size(), is(1));

        assertThat(topLevelResultLineParents.get(0).getChildren().size(), is(1));

        final List<TreeNode<ResultLine>> restructuredTree = restructringHelper.restructure(context, resultsShared);
        final List<TreeNode<ResultLine>> topLevelResultLineRestructuredParents = restructuredTree
                .stream()
                .filter(r -> r.getParents().size() == 0 && r.getChildren().size() > 0)
                .collect(toList());
        assertThat(restructuredTree.stream().filter(node -> node.isStandalone()).collect(toList()).size(), is(2));

        assertThat(topLevelResultLineRestructuredParents.size(), is(0));
    }

    @Test
    public void dirsHearing() throws IOException {

        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile("DIRS-hearing.json", ResultsShared.class);
        final JsonEnvelope event = JsonEnvelope.envelopeFrom(
                Envelope.metadataBuilder().withId(randomUUID()).withName("hearing.results-shared").build(),
                objectToJsonObjectConverter.convert(resultsShared));
        when(courtHouseReverseLookup.getCourtCentreByName(any(), any())).thenReturn(ofNullable(expectedCourtHouseByNameResult));
        when(courtHouseReverseLookup.getCourtRoomByRoomName(expectedCourtHouseByNameResult, courtRoomName)).thenReturn(ofNullable(expectedCourtRoomResult));
        when(courtRoomOuCodeReverseLookup.getcourtRoomOuCode(event, 291, "B47GL")).thenReturn("B47GL00");
        when(hearingTypeReverseLookup.getHearingTypeByName(event, hearingTypeDescription)).thenReturn(hearingType);

        final List<TreeNode<ResultLine>> resultLinesTree = restructringHelper.buildResultTreeFromPayload(context, resultsShared);

        assertThat(resultLinesTree.size(), is(3));

        final List<TreeNode<ResultLine>> topLevelResultLineParents = resultLinesTree
                .stream()
                .filter(r -> r.getParents().size() == 0 && r.getChildren().size() > 0)
                .collect(toList());

        assertThat(topLevelResultLineParents.size(), is(1));

        assertThat(topLevelResultLineParents.get(0).getChildren().size(), is(2));

        final List<TreeNode<ResultLine>> restructuredTree = restructringHelper.restructure(context, resultsShared);
        final List<TreeNode<ResultLine>> topLevelResultLineRestructuredParents = restructuredTree
                .stream()
                .filter(r -> r.getParents().size() == 0 && r.getChildren().size() > 0)
                .collect(toList());
        assertThat(restructuredTree.stream().filter(node -> node.isStandalone()).collect(toList()).size(), is(1));

        assertThat(topLevelResultLineRestructuredParents.size(), is(0));
    }

}
