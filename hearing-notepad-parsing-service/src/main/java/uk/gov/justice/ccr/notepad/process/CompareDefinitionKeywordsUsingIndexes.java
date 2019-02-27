package uk.gov.justice.ccr.notepad.process;


import static com.google.common.collect.Lists.newArrayList;

import uk.gov.justice.ccr.notepad.result.cache.ResultCache;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

class CompareDefinitionKeywordsUsingIndexes implements ResultFilter<List<ResultDefinition>, Map<Set<String>, Set<Long>>> {

    @Inject
    ResultCache resultCache;

    @Override
    public List<ResultDefinition> run(final Map<Set<String>, Set<Long>> indexes, final LocalDate orderedDate) {

        List<ResultDefinition> matchedResultDefinition = newArrayList();
        final List<ResultDefinition> resultDefinitions = resultCache.getResultDefinitions(orderedDate);
        long[] resultsFound = indexes.entrySet()
                .stream()
                .mapToLong(value -> {
                    Optional<Long> index1 = value.getValue().stream().filter(aLong -> {
                        Set<String> keywords = resultDefinitions.get(aLong.intValue()).getKeywords();
                        return keywords.containsAll(value.getKey()) && keywords.size() == value.getKey().size();
                    }).findFirst();
                    return index1.orElse(-1l);
                }).filter(v -> v > -1).toArray();

        for (Long l : resultsFound) {
            matchedResultDefinition.add(resultDefinitions.get(l.intValue()));
        }

        return matchedResultDefinition;
    }
}
