package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static com.google.common.collect.ImmutableList.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.core.courts.JudicialResult.judicialResult;
import static uk.gov.justice.core.courts.JudicialResultPrompt.judicialResultPrompt;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.JudicialResultPromptHelperV3.makePrompt;
import static uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition.resultDefinition;

import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

public class JudicialResultPromptHelperV3Test {

    private static final String RESULT_DEFINITION_LABEL = "Next hearing in magistrates' court";
    private static final BigDecimal PROMPT_SEQUENCE_NUMBER = new BigDecimal(1000);

    @Test
    public void shouldNotFoldHiddenPromptsIntoThePublishedAsAPromptValue() {
        final TreeNode<ResultLine2> resultLineTreeNode = createResultLineTreeNode();
        final ResultDefinition resultDefinition = resultLineTreeNode.getResultDefinition().getData();
        resultDefinition.getPrompts().add(new Prompt().setId(randomUUID()).setReference("HDATE").setLabel("Date of hearing").setHidden(false));
        resultDefinition.getPrompts().add(new Prompt().setId(randomUUID()).setReference("HTYPE").setLabel("Hearing type"));
        resultDefinition.getPrompts().add(new Prompt().setId(randomUUID()).setReference("bookingReference").setLabel("Booking reference").setHidden(true));
        resultDefinition.getPrompts().add(new Prompt().setId(randomUUID()).setReference("existingHearingId").setLabel("Existing Hearing Id").setHidden(true));

        setJudicialResult(resultLineTreeNode, of(
                prompt("Date of hearing", "07/09/2026", "HDATE"),
                prompt("Hearing type", "Plea", "HTYPE"),
                prompt("Booking reference", "17c85d32-ba02-4aae-967f-aeeea552ea61", "bookingReference"),
                prompt("Existing Hearing Id", "02a88544-005b-4209-a192-ad15cd74fd08", "existingHearingId")));

        final JudicialResultPrompt foldedPrompt = makePrompt(resultLineTreeNode, PROMPT_SEQUENCE_NUMBER);

        assertThat(foldedPrompt.getLabel(), is(RESULT_DEFINITION_LABEL));
        assertThat(foldedPrompt.getValue(), is("Date of hearing:07/09/2026" + System.lineSeparator() + "Hearing type:Plea"));
    }

    @Test
    public void shouldStillFoldPromptsThatCarryNoReference() {
        final TreeNode<ResultLine2> resultLineTreeNode = createResultLineTreeNode();
        resultLineTreeNode.getResultDefinition().getData().getPrompts()
                .add(new Prompt().setId(randomUUID()).setReference("bookingReference").setLabel("Booking reference").setHidden(true));

        setJudicialResult(resultLineTreeNode, of(
                prompt("Prompt Label 1", "Prompt Value 1", null),
                prompt("Prompt Label 2", "Prompt Value 2", null)));

        final JudicialResultPrompt foldedPrompt = makePrompt(resultLineTreeNode, PROMPT_SEQUENCE_NUMBER);

        assertThat(foldedPrompt.getValue(), is("Prompt Label 1:Prompt Value 1" + System.lineSeparator() + "Prompt Label 2:Prompt Value 2"));
    }

    private static JudicialResultPrompt prompt(final String label, final String value, final String reference) {
        return judicialResultPrompt()
                .withLabel(label)
                .withValue(value)
                .withType("TEXT")
                .withPromptReference(reference)
                .build();
    }

    private static TreeNode<ResultLine2> createResultLineTreeNode() {
        final ResultDefinition resultDefinition = resultDefinition().setLabel(RESULT_DEFINITION_LABEL);
        resultDefinition.setId(randomUUID());
        final TreeNode<ResultLine2> resultLineTreeNode = new TreeNode<>(randomUUID(), ResultLine2.resultLine2().build());
        resultLineTreeNode.setResultDefinition(new TreeNode<>(randomUUID(), resultDefinition));
        return resultLineTreeNode;
    }

    private static void setJudicialResult(final TreeNode<ResultLine2> resultLineTreeNode, final List<JudicialResultPrompt> judicialResultPrompts) {
        resultLineTreeNode.setJudicialResult(judicialResult()
                .withJudicialResultId(randomUUID())
                .withJudicialResultPrompts(judicialResultPrompts)
                .build());
    }
}
