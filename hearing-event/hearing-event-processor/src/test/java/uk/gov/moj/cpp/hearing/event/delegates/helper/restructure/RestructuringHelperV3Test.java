package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_RESULTS_DATEORDERS_HEARING_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_RESULTS_NEW_REVIEW_HEARING_ALWAYS_PUBLISHED_LEAF_NODE_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_RESULTS_NEW_REVIEW_HEARING_JSON;

import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV3;
import uk.gov.moj.cpp.hearing.event.delegates.helper.ResultQualifier;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
public class RestructuringHelperV3Test extends AbstractRestructuringTest {

    private ResultTreeBuilderV3 resultTreeBuilder;
    private RestructuringHelperV3 target;

    @BeforeEach
    public void setUp() throws IOException {
        ResultTextConfHelper resultTextConfHelper = Mockito.mock(ResultTextConfHelper.class);
        when(resultTextConfHelper.isOldResultDefinition(any(LocalDate.class))).thenReturn(false);
        super.setUp();
        resultTreeBuilder = new ResultTreeBuilderV3(referenceDataService, nextHearingHelperV3, resultLineHelperV3, resultTextConfHelper);
        target = new RestructuringHelperV3(resultTreeBuilder, resultTextConfHelper);
    }


    @Test
    public void shouldPublishWhenAlwaysPublishedIsALeafNode() throws IOException {
        final ResultsSharedV3 resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_NEW_REVIEW_HEARING_ALWAYS_PUBLISHED_LEAF_NODE_JSON, ResultsSharedV3.class);
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

        final List<TreeNode<ResultLine2>> restructuredTree = target.restructure(envelope, resultsShared, treeNodes);

        assertThat(restructuredTree.size(), is(3));
        assertThat(restructuredTree.get(0).getJudicialResult().getCanExtendActiveOrder(), is(true));
        assertThat(restructuredTree.get(0).getJudicialResult().getJudicialResultPrompts().get(0).getActiveOrderExtended(), is(true));
        assertThat(restructuredTree.get(0).getJudicialResult().getJudicialResultPrompts().get(0).getActiveOrderNotExtended(), is(false));

        final List<TreeNode<ResultLine2>> topLevelResultLineRestructuredParents = filterV3ResultsBy(restructuredTree, r -> r.getParents().isEmpty() && r.getChildren().size() > 0);

        assertThat((int) restructuredTree.stream().filter(TreeNode::isStandalone).count(), is(3));
        assertThat(topLevelResultLineRestructuredParents.size(), is(0));

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
    public void shouldPublishWhenAlwaysPublishedIsAnIntermediaryNodeWhenLeafNodePublishedFalse() throws IOException {
        final ResultsSharedV3 resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_NEW_REVIEW_HEARING_JSON, ResultsSharedV3.class);
        final JsonEnvelope envelope = getEnvelope(resultsShared);
        final ResultLine2 rl2 = resultsShared.getTargets().get(0).getResultLines().stream().filter(rl3 -> rl3.getResultLabel().equalsIgnoreCase("Drug rehabilitation residential with review")).findFirst().get();
        final ResultLine2 firstReviewResultLint = resultsShared.getTargets().get(0).getResultLines().stream().filter(rl3 -> rl3.getResultLabel().equalsIgnoreCase("First Review Hearing – Drug Rehab")).findFirst().get();
        assertThat(rl2.getPrompts().size(), is(3));
        assertThat(firstReviewResultLint.getPrompts().size(), is(12));

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
        when(hearingTypeReverseLookup.getHearingTypeByName(any(), any())).thenReturn(HearingType.hearingType().withDescription("REV").build());
        final List<TreeNode<ResultLine2>> restructuredTree = target.restructure(envelope, resultsShared, treeNodes);
        assertThat(restructuredTree.size(), is(2));
        assertThat(restructuredTree.stream().map(r -> r.getJudicialResult()).filter(j -> j.getLabel().equals("Drug rehabilitation residential with review")).findFirst().get().getJudicialResultPrompts().size(), is(firstReviewResultLint.getPrompts().size() + rl2.getPrompts().size()));
        assertTrue(restructuredTree.stream().map(r -> r.getJudicialResult()).filter(j -> j.getLabel().equals("Drug rehabilitation residential with review")).findFirst().isPresent());
        assertTrue(restructuredTree.stream().map(r -> r.getJudicialResult()).filter(j -> j.getLabel().equals("Community order England / Wales")).findFirst().isPresent());
        final List<TreeNode<ResultLine2>> topLevelResultLineRestructuredParents = filterV3ResultsBy(restructuredTree, r -> r.getParents().isEmpty() && r.getChildren().size() > 0);

        assertThat((int) restructuredTree.stream().filter(TreeNode::isStandalone).count(), is(2));
        assertThat(topLevelResultLineRestructuredParents.size(), is(0));

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
    public void ShouldReturnResultTextForEachOrderedDateCorrectly() throws IOException {

        final ResultsSharedV3 resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_DATEORDERS_HEARING_JSON, ResultsSharedV3.class);
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
        when(hearingTypeReverseLookup.getHearingTypeByName(any(), any())).thenReturn(HearingType.hearingType().withDescription("REV").build());
        ResultTextConfHelper resultTextConfHelper = new ResultTextConfHelper();
        setField(resultTextConfHelper, "liveDateOfResultTextTemplateConf", "01042023");
        resultTextConfHelper.setDate();
        resultTreeBuilder = new ResultTreeBuilderV3(referenceDataService, nextHearingHelperV3, resultLineHelperV3, resultTextConfHelper);
        target = new RestructuringHelperV3(resultTreeBuilder, resultTextConfHelper);
        final List<TreeNode<ResultLine2>> restructuredTree = target.restructure(envelope, resultsShared, treeNodes);

        restructuredTree.forEach(resultLine2TreeNode ->
                assertNotNull(resultLine2TreeNode.getJudicialResult().getResultText()));

    }
}
