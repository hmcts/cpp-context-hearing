package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

public class RestructuringHelperV3Test extends AbstractRestructuringTest {

    private ResultTreeBuilderV3 resultTreeBuilder;
    private RestructuringHelperV3 target;

    @Before
    public void setUp() throws IOException {
        super.setUp();
        resultTreeBuilder = new ResultTreeBuilderV3(referenceDataService, nextHearingHelperV3, resultLineHelperV3);
        target = new RestructuringHelperV3(resultTreeBuilder);
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
}
