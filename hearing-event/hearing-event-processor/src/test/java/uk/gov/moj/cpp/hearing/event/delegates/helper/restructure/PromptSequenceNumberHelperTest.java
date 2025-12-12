package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static com.google.common.collect.ImmutableList.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.core.courts.JudicialResult.judicialResult;
import static uk.gov.justice.core.courts.JudicialResultPrompt.judicialResultPrompt;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.PublishAsPromptHelper.DEFAULT_PROMPT_SEQUENCE_NUMBER;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.PublishAsPromptHelper.PROMPT_SEQUENCE_NUMBER_INCREMENT;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.PromptSequenceNumberHelper.getNextPromptSequenceNumber;
import static uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition.resultDefinition;

import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

public class PromptSequenceNumberHelperTest {

    @Test
    public void shouldReturnDefaultValueIfUnableToFindExistingPromptSequenceNumber() {
        final BigDecimal result = getNextPromptSequenceNumber(null);
        assertThat(result, is(DEFAULT_PROMPT_SEQUENCE_NUMBER));
    }

    @Test
    public void shouldCreateNewPromptSequenceNumber() {
        final TreeNode<ResultLine> resultLineTreeNode = createResultLineTreeNode();
        final BigDecimal initialPromptSequenceNumber = new BigDecimal(100);
        final JudicialResultPrompt judicialResultPrompt = judicialResultPrompt().withPromptSequence(initialPromptSequenceNumber).build();
        final JudicialResult judicialResult = judicialResult().withJudicialResultPrompts(of(judicialResultPrompt)).build();
        resultLineTreeNode.setJudicialResult(judicialResult);

        final BigDecimal result = getNextPromptSequenceNumber(resultLineTreeNode);
        assertThat(result, is(initialPromptSequenceNumber.add(PROMPT_SEQUENCE_NUMBER_INCREMENT)));
    }
    
    private TreeNode<ResultLine> createResultLineTreeNode() {
        final TreeNode<ResultLine> resultLineTreeNode = new TreeNode<>(randomUUID(), ResultLine.resultLine().build());
        final TreeNode<ResultDefinition> resultDefinition = createResultDefinitionTreeNode();
        resultLineTreeNode.setResultDefinition(resultDefinition);
        return resultLineTreeNode;
    }

    private TreeNode<ResultDefinition> createResultDefinitionTreeNode() {
        final TreeNode<ResultDefinition> resultDefinitionTreeNode = new TreeNode<>(randomUUID(), resultDefinition());
        return resultDefinitionTreeNode;
    }
}