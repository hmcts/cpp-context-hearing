package uk.gov.moj.cpp.hearing.event.delegates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_RESULTS_NEW_REVIEW_HEARING_JSON;


import java.time.LocalDate;
import org.mockito.Mockito;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.NextHearing;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.justice.hearing.courts.referencedata.CourtCentreOrganisationUnit;
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
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.relist.RelistReferenceDataService;
import uk.gov.moj.cpp.hearing.test.FileResourceObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
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

    @Before
    public void setUp() throws IOException {
        super.setUp();
        when(resultTextConfHelper.isOldResultDefinition(any(LocalDate.class))).thenReturn(false);

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
        when(courtHouseReverseLookup.getCourtCentreByName(envelope, "Aberdeen JP Court")).thenReturn(Optional.of(CourtCentreOrganisationUnit.courtCentreOrganisationUnit().withOucode("oucode2").withLja("2500").withId(UUID.randomUUID().toString()).build()));
        target.shareResults(envelope, sender, resultsShared,treeNodes);
        final List<Offence> offences = resultsShared.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream()
                        .flatMap(defendant -> defendant.getOffences().stream())).collect(Collectors.toList());
        final List<Offence> offencesWithJudicialResults = offences.stream().filter(offence -> offence.getJudicialResults()!=null).collect(Collectors.toList());
        final JudicialResult judicialResult = offencesWithJudicialResults.stream()
                                .flatMap(offence -> offence.getJudicialResults().stream()
                                        .filter(judicialResult1 -> judicialResult1.getNextHearing() != null)).findFirst().get();
        final NextHearing nextHearing = judicialResult.getNextHearing();
        //Optional<NextHearing> optionalNextHearing = resultsShared.getHearing().getDefendantJudicialResults().stream().map(defendantJudicialResult -> defendantJudicialResult.getJudicialResult()).map(nextHearing-> nextHearing.getNextHearing()).filter(nextHearing -> nextHearing != null).findFirst();
        //assertEquals (true, judicialResultOptional.isPresent());
        //NextHearing nextHearing = optionalNextHearing.get();
        assertEquals(true, nextHearing.getIsFirstReviewHearing());
        assertNotNull(nextHearing.getOrderName());
        //assertEquals("1 Years", nextHearing.getTotalCustodialPeriod());
        //assertEquals("1 Years", nextHearing.getSuspendedPeriod());
        //assertEquals("North East Division NPS", nextHearing.getProbationTeamName());
        assertEquals(10, nextHearing.getEstimatedMinutes().intValue());
        assertEquals("Community order England / Wales", nextHearing.getOrderName());
        assertNotNull(nextHearing.getCourtCentre());
        assertNotNull(nextHearing.getListedStartDateTime());
        assertEquals("2022-02-05T11:30Z", nextHearing.getListedStartDateTime().toString());


    }

}
