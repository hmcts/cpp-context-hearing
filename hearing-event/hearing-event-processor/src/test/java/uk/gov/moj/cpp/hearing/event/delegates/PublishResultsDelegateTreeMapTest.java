package uk.gov.moj.cpp.hearing.event.delegates;

import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.BAIL_CONDITIONS_ID;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.BAIL_CONDITION_ASSESSMENTS_REPORTS_ID;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_RESULTS_SHARED_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_RESULTS_SHARED_MULTIPLE_DEFENDANT_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.NEXT_HEARING_ID;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.NEXT_HEARING_IN_CROWN_COURT_ID;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.NEXT_HEARING_IN_MAGISTRATE_COURT_ID;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.REMANDED_IN_CUSTODY_ID;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.REMANDED_IN_CUSTODY_TO_HOSPITAL_ID;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.REMANDED_ON_CONDITIONAL_BAIL_ID;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.REMAND_IN_CUSTODY_ID;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.SEND_TO_CROWN_COURT_ON_CONDITIONAL_BAIL_ID;


import java.time.LocalDate;
import org.mockito.Mockito;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.AbstractRestructuringTest;
import uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.ResultTextConfHelper;
import uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.ResultTreeBuilder;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Spy;


public class PublishResultsDelegateTreeMapTest extends AbstractRestructuringTest {

    private ResultTreeBuilder resultTreeBuilder;


    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter = new JsonObjectConvertersFactory().objectToJsonObjectConverter();

    @Before
    public void setUp() throws IOException {
        ResultTextConfHelper resultTextConfHelper = Mockito.mock(ResultTextConfHelper.class);
        when(resultTextConfHelper.isOldResultDefinition(any(LocalDate.class))).thenReturn(false);
        super.setUp();
        doReturn(Optional.empty()).when(nextHearingHelper).getNextHearing(any(), any(), any(), any());
        resultTreeBuilder = new ResultTreeBuilder(referenceDataService, nextHearingHelper, resultLineHelper, resultTextConfHelper);
    }

    @Test
    public void shouldBuildSimpleTwoLayerTree() {
        final List<ResultDefinition> resultDefinitionList = resultDefinitions.stream().filter(resultDefinition ->
                REMAND_IN_CUSTODY_ID.equals(resultDefinition.getId().toString()) ||
                        REMANDED_IN_CUSTODY_ID.equals(resultDefinition.getId().toString()) ||
                        REMANDED_IN_CUSTODY_TO_HOSPITAL_ID.equals(resultDefinition.getId().toString())).collect(toList());


        final ResultsShared resultsShared = getResultsShared(resultDefinitionList);
        final List<TreeNode<ResultLine>> results = resultTreeBuilder.build(dummyEnvelope, resultsShared);

        assertThat(results.stream().filter(jr -> fromString(REMAND_IN_CUSTODY_ID).equals(jr.getResultDefinitionId())).findAny().get().getChildren().size(), is(0));
        assertThat(results.size(), is(3));
    }

    @Test
    public void shouldBuildMultiLayerTree() {
        final List<ResultDefinition> resultDefinitionList = resultDefinitions.stream().filter(resultDefinition ->
                SEND_TO_CROWN_COURT_ON_CONDITIONAL_BAIL_ID.equals(resultDefinition.getId().toString()) ||
                        REMANDED_ON_CONDITIONAL_BAIL_ID.equals(resultDefinition.getId().toString()) ||
                        NEXT_HEARING_ID.equals(resultDefinition.getId().toString()) ||
                        BAIL_CONDITIONS_ID.equals(resultDefinition.getId().toString()) ||
                        NEXT_HEARING_IN_CROWN_COURT_ID.equals(resultDefinition.getId().toString()) ||
                        BAIL_CONDITION_ASSESSMENTS_REPORTS_ID.equals(resultDefinition.getId().toString())).collect(toList());

        final ResultsShared resultsShared = getResultsShared(resultDefinitionList);
        final List<TreeNode<ResultLine>> results = resultTreeBuilder.build(dummyEnvelope, resultsShared);
        final TreeNode<ResultLine> sendToCrownCourtOnConditionalBailResultTree = results.stream().filter(jr -> fromString(SEND_TO_CROWN_COURT_ON_CONDITIONAL_BAIL_ID).equals(jr.getResultDefinitionId())).findAny().get();
        final List<TreeNode<ResultLine>> sendToCrownCourtOnConditionalBailChildren = sendToCrownCourtOnConditionalBailResultTree.getChildren();

        assertThat(sendToCrownCourtOnConditionalBailChildren.size(), is(0));
        assertThat(results.size(), is(6));
    }

    @Test
    public void caseWithOneDefendantAndOneOffence() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_SHARED_JSON, ResultsShared.class);
        final List<TreeNode<ResultLine>> results = resultTreeBuilder.build(dummyEnvelope, resultsShared);

        assertThat(results.size(), is(11));

        final List<TreeNode<ResultLine>> topLevelParents = filterBy(results, r -> r.getParents().size() == 0 && r.getChildren().size() > 0);

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
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_SHARED_MULTIPLE_DEFENDANT_JSON, ResultsShared.class);
        final List<TreeNode<ResultLine>> results = resultTreeBuilder.build(dummyEnvelope, resultsShared);

        assertThat(results.size(), is(16));

        final List<TreeNode<ResultLine>> topLevelParents = filterBy(results, r -> r.getParents().size() == 0 && r.getChildren().size() > 0);

        assertThat(topLevelParents.size(), is(4));

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
    public void shouldNotDisplayHiddenPrompts() {
        final List<ResultDefinition> resultDefinitionList = resultDefinitions.stream().filter(resultDefinition ->
                SEND_TO_CROWN_COURT_ON_CONDITIONAL_BAIL_ID.equals(resultDefinition.getId().toString()) ||
                        REMANDED_ON_CONDITIONAL_BAIL_ID.equals(resultDefinition.getId().toString()) ||
                        NEXT_HEARING_ID.equals(resultDefinition.getId().toString()) ||
                        BAIL_CONDITIONS_ID.equals(resultDefinition.getId().toString()) ||
                        NEXT_HEARING_IN_CROWN_COURT_ID.equals(resultDefinition.getId().toString()) ||
                        BAIL_CONDITION_ASSESSMENTS_REPORTS_ID.equals(resultDefinition.getId().toString())).collect(toList());

        final ResultsShared resultsShared = getResultsShared(resultDefinitionList);
        final List<TreeNode<ResultLine>> results = resultTreeBuilder.build(dummyEnvelope, resultsShared);
        final TreeNode<ResultLine> sendToCrownCourtOnConditionalBailResultTree = results.stream().filter(jr -> fromString(SEND_TO_CROWN_COURT_ON_CONDITIONAL_BAIL_ID).equals(jr.getResultDefinitionId())).findAny().get();
        final List<TreeNode<ResultLine>> sendToCrownCourtOnConditionalBailChildren = sendToCrownCourtOnConditionalBailResultTree.getChildren();

        assertThat(sendToCrownCourtOnConditionalBailChildren.size(), is(0));
        assertThat(results.size(), is(6));

        final List<String> judicialResultPromptIds = results.stream()
                .map(TreeNode::getJudicialResult)
                .filter(p -> Objects.nonNull(p.getJudicialResultPrompts()))
                .map(JudicialResult::getJudicialResultPrompts)
                .flatMap(Collection::stream)
                .map(m ->m.getJudicialResultPromptTypeId().toString())
                .collect(Collectors.toList());

        assertThat(judicialResultPromptIds.size(), is(20));
        assertTrue(judicialResultPromptIds.contains("2493a3a4-918a-4b83-b3c0-d221ff83d6fc"));

    }
}
