package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static uk.gov.justice.core.courts.JudicialResult.judicialResult;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.PublishAsPromptHelper.processPublishAsPrompt;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.RollUpPromptsHelper.filterNodesWithRollUpPrompts;
import static uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition.resultDefinition;

import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class RollUpPromptsHelperTest {

    @Test
    public void shouldProcessAListOfResultLineTreeNodes() {
        final TreeNode<ResultLine> resultLineTreeNode = createResultLineTreeNode();
        resultLineTreeNode.setJudicialResult(judicialResult().build());
        final List<TreeNode<ResultLine>> resultLineTreeNodes = new ArrayList<>(Collections.singletonList(resultLineTreeNode));
        filterNodesWithRollUpPrompts(resultLineTreeNodes);
        assertThat(resultLineTreeNodes.size(), is(1));
    }

    private TreeNode<ResultLine> createResultLineTreeNode() {
        final TreeNode<ResultLine> resultLineTreeNode = new TreeNode<>(randomUUID(), ResultLine.resultLine().build());
        final TreeNode<ResultDefinition> resultDefinition = createResultDefinitionTreeNode(false, false, 100);
        resultLineTreeNode.setResultDefinition(resultDefinition);
        resultLineTreeNode.setJudicialResult(null);
        return resultLineTreeNode;
    }

    private TreeNode<ResultDefinition> createResultDefinitionTreeNode(final boolean excludedFromResults, final boolean publishedAsAPrompt, final int rank) {
        final ResultDefinition resultDefinition = resultDefinition()
                .setExcludedFromResults(excludedFromResults)
                .setPublishedAsAPrompt(publishedAsAPrompt)
                .setQualifier(STRING.next())
                .setRank(rank);
        return new TreeNode<>(randomUUID(), resultDefinition);
    }
}