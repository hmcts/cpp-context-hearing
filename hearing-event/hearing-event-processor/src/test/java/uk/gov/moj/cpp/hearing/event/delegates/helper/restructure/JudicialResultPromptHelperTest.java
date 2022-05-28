package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static com.google.common.collect.ImmutableList.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static uk.gov.justice.core.courts.JudicialResult.judicialResult;
import static uk.gov.justice.core.courts.JudicialResultPrompt.judicialResultPrompt;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.JudicialResultPromptHelper.makePrompt;
import static uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition.resultDefinition;

import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

public class JudicialResultPromptHelperTest {

    private static final String PROMPT_LABEL_1 = "Prompt Label 1";
    private static final String PROMPT_VALUE_1 = "Prompt Value 1";
    private static final String PROMPT_LABEL_2 = "Prompt Label 2";
    private static final String PROMPT_VALUE_2 = "Prompt Value 2";
    private static final String PROMPT_LABEL_3 = "Prompt Label 3";
    private static final String PROMPT_VALUE_3 = "Prompt Value 3";
    private static final String PROMPT_VALUE_TRUE = "true";
    private static final String PROMPT_VALUE_FALSE = "false";
    private static final String RESULT_DEFINITION_LABEL = "Result Definition Label";
    private static final String RESULT_DEFINITION_QUALIFIER = "Result Definition Qualifier";

    @Test
    public void shouldMakePrompt_resultAvailableForCourtExtractIsNull() {
        final TreeNode<ResultLine> resultLineTreeNode = createResultLineTreeNode();

        final JudicialResultPrompt judicialResultPrompt1 = createJudicialResultPrompt(PROMPT_LABEL_1, PROMPT_VALUE_1, "TEXT");

        final JudicialResultPrompt judicialResultPrompt2 = createJudicialResultPrompt(PROMPT_LABEL_2, PROMPT_VALUE_2, "TEXT");

        createJudicialResult(resultLineTreeNode,of(judicialResultPrompt1, judicialResultPrompt2), null);

        final BigDecimal newPromptSequenceNumber = new BigDecimal(1000);
        final JudicialResultPrompt judicialResultPrompt = makePrompt(resultLineTreeNode, newPromptSequenceNumber);
        assertThat(judicialResultPrompt.getPromptSequence(), is(newPromptSequenceNumber));
        assertThat(judicialResultPrompt.getLabel(), is(RESULT_DEFINITION_LABEL));
        assertThat(judicialResultPrompt.getQualifier(), is(RESULT_DEFINITION_QUALIFIER));
        assertThat(judicialResultPrompt.getValue(), is(PROMPT_LABEL_1+":"+PROMPT_VALUE_1 + System.lineSeparator() + PROMPT_LABEL_2+":"+PROMPT_VALUE_2));
        assertThat(judicialResultPrompt.getCourtExtract(), is("N"));
        assertThat(judicialResultPrompt.getJudicialResultPromptTypeId(), notNullValue());
        assertThat(judicialResultPrompt.getJudicialResultPromptTypeId(), is(resultLineTreeNode.getResultDefinition().getData().getId()));
        assertThat(judicialResultPrompt.getPromptReference(), is(resultLineTreeNode.getJudicialResult().getJudicialResultId().toString()));
    }

    @Test
    public void shouldNotMakePromptForHmiSlots() {
        final TreeNode<ResultLine> resultLineTreeNode = createResultLineTreeNode();

        final JudicialResultPrompt judicialResultPrompt1 = createJudicialResultPrompt(PROMPT_LABEL_1, PROMPT_VALUE_1, "TEXT");

        final JudicialResultPrompt judicialResultPrompt2 = createJudicialResultPrompt(PROMPT_LABEL_2, PROMPT_VALUE_2, "TEXT");

        final JudicialResultPrompt judicialResultPrompt3 = createJudicialResultPromptwithHmiSlotReference(PROMPT_LABEL_3, PROMPT_VALUE_3, "TEXT", "hmiSlots");

        createJudicialResult(resultLineTreeNode,of(judicialResultPrompt1, judicialResultPrompt2, judicialResultPrompt3), null);

        final BigDecimal newPromptSequenceNumber = new BigDecimal(1000);
        final JudicialResultPrompt judicialResultPrompt = makePrompt(resultLineTreeNode, newPromptSequenceNumber);
        assertThat(judicialResultPrompt.getPromptSequence(), is(newPromptSequenceNumber));
        assertThat(judicialResultPrompt.getLabel(), is(RESULT_DEFINITION_LABEL));
        assertThat(judicialResultPrompt.getQualifier(), is(RESULT_DEFINITION_QUALIFIER));
        assertThat(judicialResultPrompt.getValue(), is(PROMPT_LABEL_1+":"+PROMPT_VALUE_1 + System.lineSeparator() + PROMPT_LABEL_2+":"+PROMPT_VALUE_2));
        assertThat(judicialResultPrompt.getCourtExtract(), is("N"));
        assertThat(judicialResultPrompt.getJudicialResultPromptTypeId(), notNullValue());
        assertThat(judicialResultPrompt.getJudicialResultPromptTypeId(), is(resultLineTreeNode.getResultDefinition().getData().getId()));
        assertThat(judicialResultPrompt.getPromptReference(), is(resultLineTreeNode.getJudicialResult().getJudicialResultId().toString()));
    }

    private JudicialResultPrompt createJudicialResultPrompt(final String s, final String s2, final String type) {
        return judicialResultPrompt()
                .withLabel(s)
                .withValue(s2)
                .withType(type)
                .build();
    }

    private JudicialResultPrompt createJudicialResultPromptwithHmiSlotReference(final String s, final String s2, final String type, String reference) {
        return judicialResultPrompt()
                .withLabel(s)
                .withValue(s2)
                .withType(type)
                .withPromptReference(reference)
                .build();
    }

    @Test
    public void shouldMakePrompt_resultAvailableForCourtExtractIsFalse() {
        final TreeNode<ResultLine> resultLineTreeNode = createResultLineTreeNode();

        final JudicialResultPrompt judicialResultPrompt1 = createJudicialResultPrompt(PROMPT_LABEL_1, PROMPT_VALUE_1,"TEXT");

        final JudicialResultPrompt judicialResultPrompt2 = createJudicialResultPrompt(PROMPT_LABEL_2, PROMPT_VALUE_2, "TEXT");

        createJudicialResult(resultLineTreeNode,of(judicialResultPrompt1, judicialResultPrompt2), Boolean.FALSE);


        final BigDecimal newPromptSequenceNumber = new BigDecimal(1000);
        final JudicialResultPrompt judicialResultPrompt = makePrompt(resultLineTreeNode, newPromptSequenceNumber);
        assertThat(judicialResultPrompt.getPromptSequence(), is(newPromptSequenceNumber));
        assertThat(judicialResultPrompt.getLabel(), is(RESULT_DEFINITION_LABEL));
        assertThat(judicialResultPrompt.getQualifier(), is(RESULT_DEFINITION_QUALIFIER));
        assertThat(judicialResultPrompt.getValue(), is(PROMPT_LABEL_1+":"+PROMPT_VALUE_1 + System.lineSeparator() + PROMPT_LABEL_2+":"+PROMPT_VALUE_2));
        assertThat(judicialResultPrompt.getCourtExtract(), is("N"));
        assertThat(judicialResultPrompt.getPromptReference(), is(resultLineTreeNode.getJudicialResult().getJudicialResultId().toString()));
    }

    @Test
    public void shouldMakePrompt_resultAvailableForCourtExtractIsTrue() {
        final TreeNode<ResultLine> resultLineTreeNode = createResultLineTreeNode();

        final JudicialResultPrompt judicialResultPrompt1 = createJudicialResultPrompt(PROMPT_LABEL_1, PROMPT_VALUE_1, "TEXT");

        final JudicialResultPrompt judicialResultPrompt2 = createJudicialResultPrompt(PROMPT_LABEL_2, PROMPT_VALUE_2, "TEXT");

        createJudicialResult(resultLineTreeNode,of(judicialResultPrompt1, judicialResultPrompt2), Boolean.TRUE);

        final BigDecimal newPromptSequenceNumber = new BigDecimal(1000);
        final JudicialResultPrompt judicialResultPrompt = makePrompt(resultLineTreeNode, newPromptSequenceNumber);
        assertThat(judicialResultPrompt.getPromptSequence(), is(newPromptSequenceNumber));
        assertThat(judicialResultPrompt.getLabel(), is(RESULT_DEFINITION_LABEL));
        assertThat(judicialResultPrompt.getQualifier(), is(RESULT_DEFINITION_QUALIFIER));
        assertThat(judicialResultPrompt.getValue(), is(PROMPT_LABEL_1+":"+PROMPT_VALUE_1 + System.lineSeparator() + PROMPT_LABEL_2+":"+PROMPT_VALUE_2));
        assertThat(judicialResultPrompt.getCourtExtract(), is("Y"));
        assertThat(judicialResultPrompt.getPromptReference(), is(resultLineTreeNode.getJudicialResult().getJudicialResultId().toString()));
    }

    @Test
    public void shouldConvertPromptValues_whenPromptTypeIsBoolean() {
        final TreeNode<ResultLine> resultLineTreeNode = createResultLineTreeNode();

        final JudicialResultPrompt judicialResultPrompt1 = createJudicialResultPrompt(PROMPT_LABEL_1, PROMPT_VALUE_TRUE, "BOOLEAN");

        final JudicialResultPrompt judicialResultPrompt2 = createJudicialResultPrompt(PROMPT_LABEL_2, PROMPT_VALUE_FALSE, "BOOLEAN");

        createJudicialResult(resultLineTreeNode, of(judicialResultPrompt1, judicialResultPrompt2), null);

        final BigDecimal newPromptSequenceNumber = new BigDecimal(1000);
        final JudicialResultPrompt judicialResultPrompt = makePrompt(resultLineTreeNode, newPromptSequenceNumber);
        assertThat(judicialResultPrompt.getPromptSequence(), is(newPromptSequenceNumber));
        assertThat(judicialResultPrompt.getLabel(), is(RESULT_DEFINITION_LABEL));
        assertThat(judicialResultPrompt.getQualifier(), is(RESULT_DEFINITION_QUALIFIER));
        assertThat(judicialResultPrompt.getValue(), is(PROMPT_LABEL_1 + ":" + "Yes" + System.lineSeparator() + PROMPT_LABEL_2 + ":" + "No"));
        assertThat(judicialResultPrompt.getCourtExtract(), is("N"));
        assertThat(judicialResultPrompt.getJudicialResultPromptTypeId(), notNullValue());
        assertThat(judicialResultPrompt.getJudicialResultPromptTypeId(), is(resultLineTreeNode.getResultDefinition().getData().getId()));
        assertThat(judicialResultPrompt.getPromptReference(), is(resultLineTreeNode.getJudicialResult().getJudicialResultId().toString()));
    }

    private TreeNode<ResultLine> createResultLineTreeNode() {
        final TreeNode<ResultLine> resultLineTreeNode = new TreeNode<>(randomUUID(), ResultLine.resultLine().build());
        final TreeNode<ResultDefinition> resultDefinition = createResultDefinitionTreeNode();
        resultLineTreeNode.setResultDefinition(resultDefinition);
        return resultLineTreeNode;
    }

    private TreeNode<ResultDefinition> createResultDefinitionTreeNode() {
        ResultDefinition resultDefinition = resultDefinition();
        resultDefinition.setId(UUID.randomUUID());
        final TreeNode<ResultDefinition> resultDefinitionTreeNode = new TreeNode<>(randomUUID(), resultDefinition);
        return resultDefinitionTreeNode;
    }

    private void createJudicialResult(final TreeNode<ResultLine> resultLineTreeNode, final List<JudicialResultPrompt> judicialResultPrompts, final Boolean isAvailableForCourtExtract){
        final JudicialResult judicialResult = judicialResult().withJudicialResultId(randomUUID()).withJudicialResultPrompts(judicialResultPrompts).build();
        resultLineTreeNode.setJudicialResult(judicialResult);

        resultLineTreeNode.getResultDefinition().getData()
                .setIsAvailableForCourtExtract(isAvailableForCourtExtract)
                .setLabel(RESULT_DEFINITION_LABEL)
                .setQualifier(RESULT_DEFINITION_QUALIFIER)
                .setUserGroups(of("Result Definition User Group"));
    }
}