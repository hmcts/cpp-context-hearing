package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static uk.gov.justice.core.courts.JudicialResult.judicialResult;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.PublishAsPromptHelper.processPublishAsPrompt;
import static uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition.resultDefinition;

import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.NextHearing;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

public class PublishAsPromptHelperTest {

    @Test
    public void shouldProcessAListOfResultLineTreeNodes() {
        final UUID existingHearingId = randomUUID();
        JudicialResult judicialResult = createJudicialResultWithNextHearing(existingHearingId);
        final TreeNode<ResultLine> parentResultLineTreeNode = createResultLineTreeNode(false, false, 100, null);
        final TreeNode<ResultLine> firstChildResultLineTreeNode = createResultLineTreeNode(false, true, 200, judicialResult);
        final TreeNode<ResultLine> secondChildResultLineTreeNode = createResultLineTreeNode(false, true, 300, null);
        parentResultLineTreeNode.addChild(firstChildResultLineTreeNode);
        parentResultLineTreeNode.addChild(secondChildResultLineTreeNode);
        firstChildResultLineTreeNode.addParent(parentResultLineTreeNode);
        secondChildResultLineTreeNode.addParent(parentResultLineTreeNode);
        parentResultLineTreeNode.setJudicialResult(judicialResult().build());
        final List<TreeNode<ResultLine>> resultLineTreeNodes = new ArrayList<>(asList(parentResultLineTreeNode, firstChildResultLineTreeNode, secondChildResultLineTreeNode));

        processPublishAsPrompt(resultLineTreeNodes);
        assertThat(resultLineTreeNodes.size(), is(1));
        assertThat(resultLineTreeNodes.size(), is(1));
        JudicialResult parentJudicialResult = resultLineTreeNodes.get(0).getJudicialResult();
        final List<JudicialResultPrompt> judicialResultPrompts = parentJudicialResult.getJudicialResultPrompts();
        assertThat(judicialResultPrompts.size(), is(2));
        final NextHearing nextHearing = parentJudicialResult.getNextHearing();
        assertThat(nextHearing, is(notNullValue()));
        assertThat(nextHearing.getExistingHearingId(), is(existingHearingId));
    }

    @Test
    public void shouldMovePromptsToNewParent() {
        final TreeNode<ResultLine> parentResultLineTreeNode = createResultLineTreeNode(false, false, 100, null);
        final TreeNode<ResultLine> childResultLineTreeNode = createResultLineTreeNode(false, false, 200, null);
        parentResultLineTreeNode.addChild(childResultLineTreeNode);
        childResultLineTreeNode.addParent(parentResultLineTreeNode);
        parentResultLineTreeNode.setJudicialResult(judicialResult().build());
        final List<TreeNode<ResultLine>> resultLineTreeNodes = new ArrayList<>(asList(parentResultLineTreeNode, childResultLineTreeNode));
        processPublishAsPrompt(parentResultLineTreeNode, childResultLineTreeNode, resultLineTreeNodes);
        assertThat(resultLineTreeNodes.size(), is(1));
        final List<JudicialResultPrompt> judicialResultPrompts = resultLineTreeNodes.get(0).getJudicialResult().getJudicialResultPrompts();
        assertThat(resultLineTreeNodes.get(0).getJudicialResult().getQualifier(), is(childResultLineTreeNode.getResultDefinition().getData().getQualifier()));
        assertThat(judicialResultPrompts.size(), is(1));
    }

    private TreeNode<ResultLine> createResultLineTreeNode(final boolean excludedFromResults, final boolean publishedAsAPrompt, final int rank, final JudicialResult judicialResult) {
        final TreeNode<ResultLine> resultLineTreeNode = new TreeNode<>(randomUUID(), ResultLine.resultLine().build());
        final TreeNode<ResultDefinition> resultDefinition = createResultDefinitionTreeNode(excludedFromResults, publishedAsAPrompt, rank);
        resultLineTreeNode.setResultDefinition(resultDefinition);
        resultLineTreeNode.setJudicialResult(judicialResult);
        return resultLineTreeNode;
    }

    private TreeNode<ResultDefinition> createResultDefinitionTreeNode(final boolean excludedFromResults, final boolean publishedAsAPrompt, final int rank) {
        final ResultDefinition resultDefinition = resultDefinition()
                .setExcludedFromResults(excludedFromResults)
                .setPublishedAsAPrompt(publishedAsAPrompt)
                .setQualifier(STRING.next())
                .setRank(rank);
        final TreeNode<ResultDefinition> resultDefinitionTreeNode = new TreeNode<>(randomUUID(), resultDefinition);
        return resultDefinitionTreeNode;
    }

    private JudicialResult createJudicialResultWithNextHearing(final UUID existingHearingId) {
        return judicialResult()
                .withNextHearing(NextHearing.nextHearing()
                        .withExistingHearingId(existingHearingId)
                        .build())
                .build();
    }

}