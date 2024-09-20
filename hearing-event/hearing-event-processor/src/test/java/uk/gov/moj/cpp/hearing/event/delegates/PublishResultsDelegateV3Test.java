package uk.gov.moj.cpp.hearing.event.delegates;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNoneEmpty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.COURT_ROOM_OU_CODE;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_APPLICATION_RESULTS_SHARED_WITH_DRIVINGLICENCENUMBER_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_CASE_RESULTS_SHARED_WITH_DRIVINGLICENCENUMBER_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_RESULTS_HEARING_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_RESULTS_NEW_REVIEW_HEARING_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_TYPE;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.RESULT_DEFINITIONS_JSON;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.NextHearing;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.justice.hearing.courts.referencedata.CourtCentreOrganisationUnit;
import uk.gov.justice.hearing.courts.referencedata.Courtrooms;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV3;
import uk.gov.moj.cpp.hearing.event.delegates.helper.BailStatusHelper;
import uk.gov.moj.cpp.hearing.event.delegates.helper.OffenceHelper;
import uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.AbstractRestructuringTest;
import uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.RestructuringHelperV3;
import uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.ResultTextConfHelper;
import uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.ResultTreeBuilderV3;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.relist.RelistReferenceDataService;
import uk.gov.moj.cpp.hearing.test.FileResourceObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;

public class PublishResultsDelegateV3Test  extends AbstractRestructuringTest {
    protected static final FileResourceObjectMapper fileResourceObjectMapper = new FileResourceObjectMapper();

    @Mock
    private Sender sender;

    @Mock
    private RelistReferenceDataService relistReferenceDataService;

    @Mock
    private CustodyTimeLimitCalculatorV3 custodyTimeLimitCalculator;

    @Mock
    private BailStatusHelper bailStatusHelper;

    @Captor
    private ArgumentCaptor<Envelope> envelopeArgumentCaptor;

    @Captor
    private ArgumentCaptor<Hearing> custodyLimitCalculatorHearingIn;

    @Mock
    protected ResultTextConfHelper resultTextConfHelper = Mockito.mock(ResultTextConfHelper.class);

    @Spy
    private ResultTreeBuilderV3 resultTreeBuilder = new ResultTreeBuilderV3(referenceDataService, nextHearingHelperV3, resultLineHelperV3, resultTextConfHelper);

    @Spy
    private RestructuringHelperV3 restructringHelper = new RestructuringHelperV3(resultTreeBuilder, resultTextConfHelper);

    @Mock
    private OffenceHelper offenceHelper;


    @Mock
    private PublishResultsDelegateV3 target;

    @BeforeEach
    public void setUp() throws IOException {
        resultDefinitions = fileResourceObjectMapper.convertFromFile(RESULT_DEFINITIONS_JSON, AllResultDefinitions.class).getResultDefinitions();

        target = new PublishResultsDelegateV3(enveloper,
                objectToJsonObjectConverter,
                referenceDataService,
                relistReferenceDataService,
                custodyTimeLimitCalculator,
                bailStatusHelper,
                restructringHelper,
                offenceHelper);
    }


    @Test
    public void shouldCreateNextHearing() throws Exception {
        final Courtrooms expectedCourtRooms = getCourtrooms();
        final CourtCentreOrganisationUnit expectedCourtHouseByNameResult = getCourtCentreOrganisationUnit(expectedCourtRooms);

        stubFixedListJson();
        stubResultDefinitionJson();
        when(courtHouseReverseLookup.getCourtCentreByName(any(), any())).thenReturn(ofNullable(expectedCourtHouseByNameResult));
        when(courtHouseReverseLookup.getCourtRoomByRoomName(any(CourtCentreOrganisationUnit.class), anyString())).thenReturn(ofNullable(expectedCourtRooms));
        when(courtRoomOuCodeReverseLookup.getcourtRoomOuCode(any(JsonEnvelope.class), anyInt(), anyString())).thenReturn(COURT_ROOM_OU_CODE);
        when(pleaTypeReferenceDataLoader.retrieveGuiltyPleaTypes()).thenReturn(createGuiltyPleaTypes());
        when(hearingTypeReverseLookup.getHearingTypeByName(any(JsonEnvelope.class), anyString())).thenReturn(HEARING_TYPE);
        when(hearingTypeReverseLookup.getDefaultDurationInMin(any(JsonEnvelope.class), anyString())).thenReturn(10);

        final ResultsSharedV3 resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_NEW_REVIEW_HEARING_JSON, ResultsSharedV3.class);
        final JsonEnvelope envelope = getEnvelope(resultsShared);
        List<UUID> resultDefinitionIds=resultsShared.getTargets().stream()
                .flatMap(t->t.getResultLines().stream())
                .map(ResultLine2::getResultDefinitionId)
                .collect(Collectors.toList());

        final List<TreeNode<ResultDefinition>> treeNodes = new ArrayList<>();

        for(UUID resulDefinitionId:resultDefinitionIds){
            TreeNode<ResultDefinition> resultDefinitionTreeNode=new TreeNode(resulDefinitionId,resultDefinitions);
            resultDefinitionTreeNode.setResultDefinitionId(resulDefinitionId);
            resultDefinitionTreeNode.setData(resultDefinitions.stream().filter(resultDefinition -> resultDefinition.getId().equals(resulDefinitionId)).findFirst().get());
            treeNodes.add(resultDefinitionTreeNode);
        }
        target.shareResults(envelope, sender, resultsShared,treeNodes);
        final List<Offence> offences = resultsShared.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream()
                        .flatMap(defendant -> defendant.getOffences().stream())).collect(Collectors.toList());
        final List<Offence> offencesWithJudicialResults = offences.stream().filter(offence -> offence.getJudicialResults()!=null).collect(Collectors.toList());
        final JudicialResult judicialResult = offencesWithJudicialResults.stream()
                                .flatMap(offence -> offence.getJudicialResults().stream()
                                        .filter(judicialResult1 -> judicialResult1.getNextHearing() != null)).findFirst().get();
        final NextHearing nextHearing = judicialResult.getNextHearing();
        assertEquals(true, nextHearing.getIsFirstReviewHearing());
        assertNotNull(nextHearing.getOrderName());
        assertEquals(10, nextHearing.getEstimatedMinutes().intValue());
        assertEquals("Community order England / Wales", nextHearing.getOrderName());
        assertNotNull(nextHearing.getCourtCentre());
        assertNotNull(nextHearing.getListedStartDateTime());
        assertEquals("2022-02-05T11:30Z", nextHearing.getListedStartDateTime().toString());
    }

    @Test
    public void shouldCreateNextHearingWithBailConditions() throws Exception {
        final Courtrooms expectedCourtRooms = getCourtrooms();
        final CourtCentreOrganisationUnit expectedCourtHouseByNameResult = getCourtCentreOrganisationUnit(expectedCourtRooms);

        stubFixedListJson();
        stubResultDefinitionJson();
        when(courtHouseReverseLookup.getCourtCentreByName(any(), any())).thenReturn(ofNullable(expectedCourtHouseByNameResult));
        when(courtHouseReverseLookup.getCourtRoomByRoomName(any(CourtCentreOrganisationUnit.class), anyString())).thenReturn(ofNullable(expectedCourtRooms));
        when(courtRoomOuCodeReverseLookup.getcourtRoomOuCode(any(JsonEnvelope.class), anyInt(), anyString())).thenReturn(COURT_ROOM_OU_CODE);
        when(hearingTypeReverseLookup.getHearingTypeByName(any(JsonEnvelope.class), anyString())).thenReturn(HEARING_TYPE);
        when(pleaTypeReferenceDataLoader.retrieveGuiltyPleaTypes()).thenReturn(createGuiltyPleaTypes());

        final ResultsSharedV3 resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_HEARING_JSON, ResultsSharedV3.class);
        final JsonEnvelope envelope = getEnvelope(resultsShared);
        List<UUID> resultDefinitionIds=resultsShared.getTargets().stream()
                .flatMap(t->t.getResultLines().stream())
                .map(ResultLine2::getResultDefinitionId)
                .collect(Collectors.toList());

        final List<TreeNode<ResultDefinition>> treeNodes = new ArrayList<>();

        for(UUID resulDefinitionId:resultDefinitionIds){
            TreeNode<ResultDefinition> resultDefinitionTreeNode=new TreeNode(resulDefinitionId,resultDefinitions);
            resultDefinitionTreeNode.setResultDefinitionId(resulDefinitionId);
            resultDefinitionTreeNode.setData(resultDefinitions.stream().filter(resultDefinition -> resultDefinition.getId().equals(resulDefinitionId)).findFirst().get());
            treeNodes.add(resultDefinitionTreeNode);
        }
        target.shareResults(envelope, sender, resultsShared,treeNodes);

        final List<Defendant> defendants = resultsShared.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream()).collect(Collectors.toList());

        final List<Defendant> defendantWithPersonDefendants = defendants.stream().filter(defendant -> defendant.getPersonDefendant() != null).collect(Collectors.toList());
        final Defendant defendant = defendantWithPersonDefendants.stream().filter(defendant1 -> defendant1.getPersonDefendant().getBailStatus() != null).findFirst().get();

        assertTrue(isNoneEmpty(defendant.getPersonDefendant().getBailConditions()));
    }

    @Test
    public void shouldCreateNextHearingWithNoBailConditions() throws Exception {
        final Courtrooms expectedCourtRooms = getCourtrooms();
        final CourtCentreOrganisationUnit expectedCourtHouseByNameResult = getCourtCentreOrganisationUnit(expectedCourtRooms);

        stubFixedListJson();
        stubResultDefinitionJson();
        when(courtHouseReverseLookup.getCourtCentreByName(any(), any())).thenReturn(ofNullable(expectedCourtHouseByNameResult));
        when(courtHouseReverseLookup.getCourtRoomByRoomName(any(CourtCentreOrganisationUnit.class), anyString())).thenReturn(ofNullable(expectedCourtRooms));
        when(courtRoomOuCodeReverseLookup.getcourtRoomOuCode(any(JsonEnvelope.class), anyInt(), anyString())).thenReturn(COURT_ROOM_OU_CODE);
        when(hearingTypeReverseLookup.getHearingTypeByName(any(JsonEnvelope.class), anyString())).thenReturn(HEARING_TYPE);
        when(pleaTypeReferenceDataLoader.retrieveGuiltyPleaTypes()).thenReturn(createGuiltyPleaTypes());

        final ResultsSharedV3 resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_NEW_REVIEW_HEARING_JSON, ResultsSharedV3.class);
        final JsonEnvelope envelope = getEnvelope(resultsShared);
        List<UUID> resultDefinitionIds=resultsShared.getTargets().stream()
                .flatMap(t->t.getResultLines().stream())
                .map(ResultLine2::getResultDefinitionId)
                .collect(Collectors.toList());

        final List<TreeNode<ResultDefinition>> treeNodes = new ArrayList<>();

        for(UUID resulDefinitionId:resultDefinitionIds){
            TreeNode<ResultDefinition> resultDefinitionTreeNode=new TreeNode(resulDefinitionId,resultDefinitions);
            resultDefinitionTreeNode.setResultDefinitionId(resulDefinitionId);
            resultDefinitionTreeNode.setData(resultDefinitions.stream().filter(resultDefinition -> resultDefinition.getId().equals(resulDefinitionId)).findFirst().get());
            treeNodes.add(resultDefinitionTreeNode);
        }
        target.shareResults(envelope, sender, resultsShared,treeNodes);

        final List<Offence> offences = resultsShared.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream()
                        .flatMap(defendant -> defendant.getOffences().stream())).collect(Collectors.toList());
        final List<Offence> offencesWithJudicialResults = offences.stream().filter(offence -> offence.getJudicialResults()!=null).collect(Collectors.toList());
        final JudicialResult judicialResult = offencesWithJudicialResults.stream()
                .flatMap(offence -> offence.getJudicialResults().stream()
                        .filter(judicialResult1 -> judicialResult1.getNextHearing() != null)).findFirst().get();

        final List<Defendant> defendants = resultsShared.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream()).collect(Collectors.toList());

        final List<Defendant> defendantWithPersonDefendants = defendants.stream().filter(defendant -> defendant.getPersonDefendant() != null).collect(Collectors.toList());
        final Defendant defendant = defendantWithPersonDefendants.stream().findFirst().get();

        assertTrue(isEmpty(defendant.getPersonDefendant().getBailConditions()));

    }
    @Test
    public void shouldUpdateCaseDefendantDrivingLicenseNumber() throws Exception {
        final ResultsSharedV3 resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_CASE_RESULTS_SHARED_WITH_DRIVINGLICENCENUMBER_JSON, ResultsSharedV3.class);
        final JsonEnvelope envelope = getEnvelope(resultsShared);
        List<UUID> resultDefinitionIds=resultsShared.getTargets().stream()
                .flatMap(t->t.getResultLines().stream())
                .map(ResultLine2::getResultDefinitionId)
                .collect(Collectors.toList());

        final List<TreeNode<ResultDefinition>> treeNodes = new ArrayList<>();

        for(UUID resulDefinitionId:resultDefinitionIds){
            TreeNode<ResultDefinition> resultDefinitionTreeNode=new TreeNode(resulDefinitionId,resultDefinitions);
            resultDefinitionTreeNode.setResultDefinitionId(resulDefinitionId);
            resultDefinitionTreeNode.setData(resultDefinitions.stream().filter(resultDefinition -> resultDefinition.getId().equals(resulDefinitionId)).findFirst().get());
            treeNodes.add(resultDefinitionTreeNode);
        }
        target.shareResults(envelope, sender, resultsShared,treeNodes);


        assertEquals("DVL1234", resultsShared.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getDriverNumber());
    }

    @Test
    public void shouldUpdateApplicationDefendantDrivingLicenseNumber() throws Exception {
        final ResultsSharedV3 resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_APPLICATION_RESULTS_SHARED_WITH_DRIVINGLICENCENUMBER_JSON, ResultsSharedV3.class);
        final JsonEnvelope envelope = getEnvelope(resultsShared);
        List<UUID> resultDefinitionIds = resultsShared.getTargets().stream()
                .flatMap(t -> t.getResultLines().stream())
                .map(ResultLine2::getResultDefinitionId)
                .collect(Collectors.toList());

        final List<TreeNode<ResultDefinition>> treeNodes = new ArrayList<>();

        for (UUID resulDefinitionId : resultDefinitionIds) {
            TreeNode<ResultDefinition> resultDefinitionTreeNode = new TreeNode(resulDefinitionId, resultDefinitions);
            resultDefinitionTreeNode.setResultDefinitionId(resulDefinitionId);
            resultDefinitionTreeNode.setData(resultDefinitions.stream().filter(resultDefinition -> resultDefinition.getId().equals(resulDefinitionId)).findFirst().get());
            treeNodes.add(resultDefinitionTreeNode);
        }
        target.shareResults(envelope, sender, resultsShared, treeNodes);


        assertEquals("DVL1234", resultsShared.getHearing().getCourtApplications().get(0).getApplicant().getMasterDefendant().getPersonDefendant().getDriverNumber());
        assertEquals("DVL1234", resultsShared.getHearing().getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant().getDriverNumber());
    }
}
