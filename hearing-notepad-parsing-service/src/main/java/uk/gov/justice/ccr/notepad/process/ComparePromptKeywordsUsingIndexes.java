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

class ComparePromptKeywordsUsingIndexes implements ResultFilter<ResultPrompt, Map<Set<String>, Set<Long>>> {


    @Inject
    ResultCache resultCache;

    @Override
    public ResultPrompt run(final Map<Set<String>, Set<Long>> indexes, final LocalDate orderedDate) {

        final List<ResultPrompt> resultPrompts = resultCache.getResultPrompt(orderedDate);
        OptionalLong index = indexes.entrySet()
                .stream()
                .mapToLong(value -> {
                    Optional<Long> index1 = value.getValue().stream().filter(aLong -> {
                        Set<String> keywords = resultPrompts.get(aLong.intValue()).getKeywords();
                        return keywords.containsAll(value.getKey()) && keywords.size() == value.getKey().size();
                    }).findFirst();
                    return index1.orElse(-1l);
                }).filter(v -> v > -1).findFirst();

        if (index.isPresent()) {
            return resultPrompts.get((int) index.getAsLong());
        }

        return null;
    }
}
