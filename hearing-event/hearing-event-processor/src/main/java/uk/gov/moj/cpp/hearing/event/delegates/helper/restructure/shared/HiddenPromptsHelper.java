package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Prompts flagged {@code hidden} on a result definition are populated by the system rather than the
 * court user (booking reference, existing hearing id, the NAMEADDRESS name and e-mail lines). Result text
 * already leaves them out ({@code ResultTextHelper} / {@code ResultTextHelperV3}); this helper lets the
 * {@code publishedAsAPrompt} fold do the same, so they do not surface on NOW / EDT documents.
 * <p>
 * Hidden prompts are matched by {@code reference}, not by prompt id: the children of a NAMEADDRESS prompt
 * share one id while only some of them are hidden.
 */
public final class HiddenPromptsHelper {

    private HiddenPromptsHelper() {
    }

    public static Set<String> hiddenPromptReferences(final ResultDefinition resultDefinition) {
        return ofNullable(resultDefinition)
                .map(ResultDefinition::getPrompts)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .filter(prompt -> TRUE.equals(prompt.isHidden()))
                .map(Prompt::getReference)
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    /**
     * Keeps a published prompt unless the definition prompt it came from is hidden; prompts without a
     * reference are always kept.
     */
    public static Predicate<JudicialResultPrompt> notHiddenIn(final ResultDefinition resultDefinition) {
        final Set<String> hiddenPromptReferences = hiddenPromptReferences(resultDefinition);
        return prompt -> isNull(prompt.getPromptReference()) || !hiddenPromptReferences.contains(prompt.getPromptReference());
    }
}
