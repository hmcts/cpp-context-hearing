package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.core.courts.JudicialResultPrompt.judicialResultPrompt;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.HiddenPromptsHelper.hiddenPromptReferences;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.HiddenPromptsHelper.notHiddenIn;
import static uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition.resultDefinition;

import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import org.junit.jupiter.api.Test;

public class HiddenPromptsHelperTest {

    @Test
    public void shouldCollectTheReferencesOfHiddenPromptsOnly() {
        final ResultDefinition resultDefinition = resultDefinition();
        resultDefinition.getPrompts().add(prompt("bookingReference", true));
        resultDefinition.getPrompts().add(prompt("hCHOUSEEmailAddress1", true));
        resultDefinition.getPrompts().add(prompt("hCHOUSEAddress1", false));
        resultDefinition.getPrompts().add(prompt("HDATE", null));
        resultDefinition.getPrompts().add(new Prompt().setId(randomUUID()).setHidden(true));

        assertThat(hiddenPromptReferences(resultDefinition), containsInAnyOrder("bookingReference", "hCHOUSEEmailAddress1"));
    }

    @Test
    public void shouldTreatAMissingDefinitionAsHavingNoHiddenPrompts() {
        assertThat(hiddenPromptReferences(null), hasSize(0));
        assertThat(notHiddenIn(null).test(judicialResultPrompt().withPromptReference("bookingReference").build()), is(true));
    }

    @Test
    public void shouldRejectOnlyPromptsWhoseDefinitionPromptIsHidden() {
        final ResultDefinition resultDefinition = resultDefinition();
        resultDefinition.getPrompts().add(prompt("bookingReference", true));
        resultDefinition.getPrompts().add(prompt("HDATE", false));

        assertThat(notHiddenIn(resultDefinition).test(judicialResultPrompt().withPromptReference("bookingReference").build()), is(false));
        assertThat(notHiddenIn(resultDefinition).test(judicialResultPrompt().withPromptReference("HDATE").build()), is(true));
        assertThat(notHiddenIn(resultDefinition).test(judicialResultPrompt().withPromptReference("notOnTheDefinition").build()), is(true));
        assertThat(notHiddenIn(resultDefinition).test(judicialResultPrompt().build()), is(true));
    }

    private static Prompt prompt(final String reference, final Boolean hidden) {
        return new Prompt().setId(randomUUID()).setReference(reference).setHidden(hidden);
    }
}
