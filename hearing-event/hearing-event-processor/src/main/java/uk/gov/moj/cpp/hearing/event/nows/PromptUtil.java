package uk.gov.moj.cpp.hearing.event.nows;

import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings({"squid:S1118", "squid:S134"})
public class PromptUtil {
    public static final Optional<String> extractByPromptReference(final Map<UUID, Prompt> id2PromptRef, final String promptReference, final List<ResultLine> resultLines4Now) {
        for (final ResultLine resultLine : resultLines4Now) {
            if (resultLine.getPrompts() != null) {
                for (final uk.gov.justice.core.courts.Prompt prompt : resultLine.getPrompts()) {
                    final Prompt promptDef = id2PromptRef.get(prompt.getId());
                    if (promptDef != null && promptReference.equals(promptDef.getReference())) {
                        return Optional.ofNullable(prompt.getValue());
                    }
                }
            }
        }
        return Optional.empty();
    }
}
