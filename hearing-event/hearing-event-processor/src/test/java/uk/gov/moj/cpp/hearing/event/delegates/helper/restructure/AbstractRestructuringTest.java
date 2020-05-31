package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
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
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllFixedList;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.FixedList;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.service.FixedListLookup;
import uk.gov.moj.cpp.hearing.event.service.BailStatusReferenceDataLoader;
import uk.gov.moj.cpp.hearing.event.service.LjaReferenceDataLoader;
import uk.gov.moj.cpp.hearing.event.service.HearingTypeReverseLookup;
import uk.gov.moj.cpp.hearing.event.service.CourtHouseReverseLookup;
import uk.gov.moj.cpp.hearing.event.service.CourtRoomOuCodeReverseLookup;
import uk.gov.moj.cpp.hearing.event.service.NowsReferenceDataLoader;
import uk.gov.moj.cpp.hearing.event.service.NowsReferenceCache;
import uk.gov.moj.cpp.hearing.event.service.NowsReferenceDataServiceImpl;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;
import uk.gov.moj.cpp.hearing.test.FileResourceObjectMapper;

import javax.json.JsonValue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.justice.core.courts.DelegatedPowers.delegatedPowers;
import static uk.gov.justice.core.courts.Hearing.hearing;
import static uk.gov.justice.core.courts.ResultLine.resultLine;
import static uk.gov.justice.core.courts.Target.target;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared.builder;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.COURT_NAME;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.DUMMY_NAME;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.RESULT_DEFINITIONS_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.COURT_ROOM_ID;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.COURT_ROOM_OU_CODE;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.FIXED_LIST_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_TYPE;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_RESULTS_SHARED_EVENT;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.REFERENCE_DATE;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.COURT_ROOM_NAME;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.metadataFor;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractRestructuringTest {
    @Mock
    protected FixedListLookup fixedListLookup;

    @Mock
    protected BailStatusReferenceDataLoader bailStatusReferenceDataLoader;

    @Mock
    protected LjaReferenceDataLoader ljaReferenceDataLoader;

    @Mock
    protected HearingTypeReverseLookup hearingTypeReverseLookup;

    @Mock
    protected CourtHouseReverseLookup courtHouseReverseLookup;

    @Mock
    protected CourtRoomOuCodeReverseLookup courtRoomOuCodeReverseLookup;

    @Mock
    protected Requester requester;

    @Spy
    protected final Enveloper enveloper = createEnveloper();

    @Spy
    protected final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Spy
    @InjectMocks
    protected final JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter();

    @Spy
    @InjectMocks
    protected final ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();

    @Spy
    @InjectMocks
    protected final NowsReferenceDataLoader nowsReferenceDataLoader = new NowsReferenceDataLoader();

    @Spy
    @InjectMocks
    protected final NowsReferenceCache nowsReferenceCache = new NowsReferenceCache();

    @Spy
    @InjectMocks
    protected final NextHearingHelper nextHearingHelper = new NextHearingHelper();

    @Spy
    @InjectMocks
    protected ReferenceDataService referenceDataService = new NowsReferenceDataServiceImpl(nowsReferenceCache, ljaReferenceDataLoader, fixedListLookup, bailStatusReferenceDataLoader);

    protected static final FileResourceObjectMapper fileResourceObjectMapper = new FileResourceObjectMapper();
    protected static final JsonEnvelope dummyEnvelope = envelopeFrom(metadataWithRandomUUID(DUMMY_NAME), JsonValue.NULL);
    protected List<ResultDefinition> resultDefinitions;

    @Before
    public void setUp() throws IOException {
        resultDefinitions = fileResourceObjectMapper.convertFromFile(RESULT_DEFINITIONS_JSON, AllResultDefinitions.class).getResultDefinitions();
        final List<FixedList> fixedLists = fileResourceObjectMapper.convertFromFile(FIXED_LIST_JSON, AllFixedList.class).getFixedListCollection();
        final Metadata metadata = metadataFor(DUMMY_NAME, UUID.randomUUID());
        final Envelope fixedListsEnvelopeDefinition = uk.gov.justice.services.messaging.Envelope.envelopeFrom(metadata, new AllFixedList().setFixedListCollection(fixedLists));
        final JsonEnvelope resultEnvelopeDefinition = envelopeFrom(metadataWithRandomUUID(DUMMY_NAME), objectToJsonObjectConverter.convert(new AllResultDefinitions().setResultDefinitions(resultDefinitions)));
        final Courtrooms expectedCourtRooms = getCourtrooms();
        final CourtCentreOrganisationUnit expectedCourtHouseByNameResult = getCourtCentreOrganisationUnit(expectedCourtRooms);

        when(courtHouseReverseLookup.getCourtCentreByName(any(), any())).thenReturn(ofNullable(expectedCourtHouseByNameResult));
        when(requester.requestAsAdmin(any())).thenReturn(resultEnvelopeDefinition);
        when(requester.request(any(), any())).thenReturn(fixedListsEnvelopeDefinition);
        when(courtHouseReverseLookup.getCourtRoomByRoomName(any(CourtCentreOrganisationUnit.class), anyString())).thenReturn(ofNullable(expectedCourtRooms));
        when(courtRoomOuCodeReverseLookup.getcourtRoomOuCode(any(JsonEnvelope.class), anyInt(), anyString())).thenReturn(COURT_ROOM_OU_CODE);
        when(hearingTypeReverseLookup.getHearingTypeByName(any(JsonEnvelope.class), anyString())).thenReturn(HEARING_TYPE);
    }

    protected JsonEnvelope getEnvelope(final ResultsShared resultsShared) {
        return envelopeFrom(
                Envelope.metadataBuilder().withId(randomUUID()).withName(HEARING_RESULTS_SHARED_EVENT).build(),
                objectToJsonObjectConverter.convert(resultsShared));
    }

    protected ResultsShared getResultsShared(List<ResultDefinition> resultDefinitions) {
        final List<ResultLine> resultLines = new ArrayList<>();

        resultDefinitions.forEach(resultDefinition -> {
            final List<Prompt> promptList = resultDefinition
                    .getPrompts()
                    .stream()
                    .map(p -> new Prompt(null, p.getId(), null, null, null, null))
                    .collect(toList());

            resultLines.add(resultLine()
                    .withResultDefinitionId(resultDefinition.getId())
                    .withPrompts(promptList)
                    .withOrderedDate(REFERENCE_DATE)
                    .withResultLineId(randomUUID())
                    .build());
        });

        return builder().withTargets(Arrays.asList(target().withResultLines(resultLines).build()))
                .withCourtClerk(delegatedPowers()
                        .withUserId(randomUUID())
                        .withFirstName("ClerkFirstName")
                        .withLastName("ClerkLasttName")
                        .build()
                ).withHearing(hearing().build()).build();
    }

    protected List<TreeNode<ResultLine>> filterBy(List<TreeNode<ResultLine>> inputList, Predicate<TreeNode<ResultLine>> predicate) {
        return inputList.stream().filter(predicate).collect(Collectors.toList());
    }

    private Courtrooms getCourtrooms() {
        return Courtrooms.courtrooms()
                .withCourtroomId(COURT_ROOM_ID)
                .withCourtroomName(COURT_ROOM_NAME)
                .withId(randomUUID())
                .build();
    }

    private CourtCentreOrganisationUnit getCourtCentreOrganisationUnit(Courtrooms expectedCourtRoomResult) {
        return CourtCentreOrganisationUnit.courtCentreOrganisationUnit()
                .withId(randomUUID().toString())
                .withLja("3255")
                .withOucodeL3Name(COURT_NAME)
                .withOucode("B47GL")
                .withCourtrooms(asList(expectedCourtRoomResult))
                .withAddress1("Address1")
                .withAddress2("Address2")
                .withAddress3("Address3")
                .withAddress4("Address4")
                .withAddress5("Address5")
                .withPostcode("UB10 0HB")
                .build();
    }
}
