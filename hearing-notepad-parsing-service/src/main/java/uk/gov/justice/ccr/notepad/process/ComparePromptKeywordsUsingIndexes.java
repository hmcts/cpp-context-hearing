package uk.gov.justice.ccr.notepad.process;


import uk.gov.justice.ccr.notepad.result.cache.ResultCache;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

import javax.inject.Inject;

public class ComparePromptKeywordsUsingIndexes {

    @Inject
    private ResultCache resultCache;

    private static final int INDEX_NOT_FOUND_LONG = -1;

    public ResultPrompt run(final Map<Set<String>, Set<Long>> indexes, final LocalDate orderedDate) {
        return run(indexes, orderedDate, Optional.empty());
    }

    public ResultPrompt run(final Map<Set<String>, Set<Long>> indexes, final LocalDate orderedDate, final Optional<Set<String>> resultDefinitionIds) {
        final List<ResultPrompt> resultPrompts = resultCache.getResultPrompt(orderedDate);
        OptionalLong index = getIndex(indexes, resultPrompts, resultDefinitionIds);

        if (index.isPresent()) { // Search highest prompt match by result definition
            return resultPrompts.get((int) index.getAsLong());
        } else { // If not found by result definition, search without it
            index = getIndex(indexes, resultPrompts, Optional.empty());
            if (index.isPresent()) {
                return resultPrompts.get((int) index.getAsLong());
            } else {
                return null;
            }
        }
    }

    private OptionalLong getIndex(final Map<Set<String>, Set<Long>> indexes, final List<ResultPrompt> resultPrompts, final Optional<Set<String>> resultDefinitionIds) {
        return indexes.entrySet().stream()
                .mapToLong(indexMap -> indexMap.getValue().stream().filter(index -> {
                    final ResultPrompt resultPrompt = resultPrompts.get(index.intValue());
                    final String resultDefinitionId = resultPrompt.getResultDefinitionId().toString();
                    final Set<String> keywords = resultPrompt.getKeywords();

                    if (resultDefinitionIds.isPresent()) {
                        final Set<String> resultDefinitionIdSet = resultDefinitionIds.get();
                        return resultDefinitionIdSet.contains(resultDefinitionId) && keywords.containsAll(indexMap.getKey()) && keywords.size() == indexMap.getKey().size();
                    } else {
                        return keywords.containsAll(indexMap.getKey()) && keywords.size() == indexMap.getKey().size();
                    }
                }).findFirst().orElse(Long.valueOf(INDEX_NOT_FOUND_LONG)))
                .filter(value -> value > INDEX_NOT_FOUND_LONG).findFirst();
    }
}
