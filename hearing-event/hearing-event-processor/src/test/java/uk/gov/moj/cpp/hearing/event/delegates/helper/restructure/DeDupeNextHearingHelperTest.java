package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.metadataFor;

import uk.gov.justice.core.courts.HearingType;
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
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class DeDupeNextHearingHelperTest {

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
    @Mock
    private FixedListLookup fixedListLookup;

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
        final Envelope fixedListsEnvelopeDefinition = Envelope.envelopeFrom(metadata, new AllFixedList().setFixedListCollection(fixedLists));
        when(requester.request(any(), any())).thenReturn(fixedListsEnvelopeDefinition);

        referenceDate = PAST_LOCAL_DATE.next();
        context = envelopeFrom(metadataWithRandomUUID("something"), JsonValue.NULL);

        setField(nextHearingHelper, "referenceDataService", referenceDataService);
        restructringHelper = new RestructuringHelper(referenceDataService, nextHearingHelper);
    }

    @Test
    public void restructureNextHearing_single_defendant_and_offence() throws IOException {

        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile("hearing.results-shared.json", ResultsShared.class);

        final JsonEnvelope event = JsonEnvelope.envelopeFrom(
                Envelope.metadataBuilder().withId(randomUUID()).withName("hearing.results-shared").build(),
                objectToJsonObjectConverter.convert(resultsShared));
        when(courtHouseReverseLookup.getCourtCentreByName(any(), any())).thenReturn(ofNullable(expectedCourtHouseByNameResult));
        when(courtHouseReverseLookup.getCourtRoomByRoomName(expectedCourtHouseByNameResult, courtRoomName)).thenReturn(ofNullable(expectedCourtRoomResult));
        when(courtRoomOuCodeReverseLookup.getcourtRoomOuCode(event, 291, "B47GL")).thenReturn("B47GL00");
        when(hearingTypeReverseLookup.getHearingTypeByName(event, hearingTypeDescription)).thenReturn(hearingType);

        final List<TreeNode<ResultLine>> results = DeDupeNextHearingHelper.deDupNextHearing(new RestructuringHelper(referenceDataService, nextHearingHelper).buildResultTreeFromPayload(context, resultsShared));

        final String NEXT_HEARING_IN_CROWN_COURT_ID = "fbed768b-ee95-4434-87c8-e81cbc8d24c8";

        final List<TreeNode<ResultLine>> nextHearingInCrownCourtResults = results
                .stream()
                .filter(r -> NEXT_HEARING_IN_CROWN_COURT_ID.equals(r.getResultDefinitionId().toString()))
                .collect(toList());

        assertThat(nextHearingInCrownCourtResults.size(), is(5));

        final List<TreeNode<ResultLine>> treeResults = nextHearingInCrownCourtResults
                .stream()
                .filter((r -> !(r.getParents().size() == 0 && r.getChildren().size() == 0)))
                .collect(toList());

        assertThat(treeResults.size(), is(5));
        assertThat(treeResults.get(0).getId().toString(), is("53efa5f1-9e2f-40ea-aba9-5145d1bec83c"));

        final List<TreeNode<ResultLine>> standAloneResults = nextHearingInCrownCourtResults
                .stream()
                .filter(r -> r.getParents().size() == 0 && r.getChildren().size() == 0)
                .collect(toList());

        assertThat(standAloneResults.size(), is(0));

        final String NEXT_HEARING_ID = "f00359b5-7303-403b-b59e-0b1a1daa89bc";

        final List<TreeNode<ResultLine>> nextHearingResult = results
                .stream()
                .filter(r -> NEXT_HEARING_ID.equals(r.getResultDefinitionId().toString()))
                .collect(toList());

        assertThat(nextHearingResult.get(0).getChildren().size(), is (5));
    }

    @Test
    public void restructureNextHearing_multiple_defendant_and_offences() throws IOException {

        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile("hearing.results-shared_multiple_defendant.json", ResultsShared.class);

        final JsonEnvelope event = JsonEnvelope.envelopeFrom(
                Envelope.metadataBuilder().withId(randomUUID()).withName("hearing.results-shared").build(),
                objectToJsonObjectConverter.convert(resultsShared));
        when(courtHouseReverseLookup.getCourtCentreByName(any(), any())).thenReturn(ofNullable(expectedCourtHouseByNameResult));
        when(courtHouseReverseLookup.getCourtRoomByRoomName(expectedCourtHouseByNameResult, courtRoomName)).thenReturn(ofNullable(expectedCourtRoomResult));
        when(courtRoomOuCodeReverseLookup.getcourtRoomOuCode(event, 291, "B47GL")).thenReturn("B47GL00");
        when(hearingTypeReverseLookup.getHearingTypeByName(event, hearingTypeDescription)).thenReturn(hearingType);

        final List<TreeNode<ResultLine>> results = DeDupeNextHearingHelper.deDupNextHearing(new RestructuringHelper(referenceDataService, nextHearingHelper).buildResultTreeFromPayload(context, resultsShared));

        final String NEXT_HEARING_IN_CROWN_COURT_ID = "fbed768b-ee95-4434-87c8-e81cbc8d24c8";

        final List<TreeNode<ResultLine>> nextHearingInCrownCourtResults = results
                .stream()
                .filter(r -> NEXT_HEARING_IN_CROWN_COURT_ID.equals(r.getResultDefinitionId().toString()))
                .collect(toList());

        assertThat(nextHearingInCrownCourtResults.size(), is(6));

        final List<TreeNode<ResultLine>> treeResults = nextHearingInCrownCourtResults
                .stream()
                .filter((r -> !(r.getParents().size() == 0 && r.getChildren().size() == 0)))
                .collect(toList());

        assertThat(treeResults.size(), is(3));

        final List<TreeNode<ResultLine>> standAloneResults = nextHearingInCrownCourtResults
                .stream()
                .filter(r -> r.getParents().size() == 0 && r.getChildren().size() == 0)
                .collect(toList());

        assertThat(standAloneResults.size(), is(3));

        final String NEXT_HEARING_ID = "f00359b5-7303-403b-b59e-0b1a1daa89bc";

        final List<TreeNode<ResultLine>> nextHearingResult = results
                .stream()
                .filter(r -> NEXT_HEARING_ID.equals(r.getResultDefinitionId().toString()))
                .collect(toList());

        assertThat(nextHearingResult.get(0).getChildren().size(), is (1));

        List<TreeNode<ResultLine>> results1 = RestructureNextHearingHelper.restructureNextHearing(results);

        final List<TreeNode<ResultLine>> nextHearingResult1 = results1
                .stream()
                .filter(r -> NEXT_HEARING_ID.equals(r.getResultDefinitionId().toString()))
                .collect(toList());
        assertThat(nextHearingResult1.size(), is (0));
    }
}
