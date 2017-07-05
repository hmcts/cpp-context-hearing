package uk.gov.justice.ccr.notepad.process;


import uk.gov.justice.ccr.notepad.result.cache.ResultCache;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import com.google.common.collect.Lists;

class CompareDefinitionKeywordsUsingIndexes implements ResultFilter<List<ResultDefinition>, Map<List<String>, Set<Long>>> {


    @Inject
    ResultCache resultCache;

    @Override
    public List<ResultDefinition> run(final Map<List<String>, Set<Long>> indexes) throws ExecutionException {

        List<ResultDefinition> matchedResultDefinition = Lists.newArrayList();
        List<ResultDefinition> resultDefinitions = resultCache.getResultDefinition();
        long[] resultsFound = indexes.entrySet()
                .stream()
                .mapToLong(value -> {
                    Optional<Long> index1 = value.getValue().stream().filter(aLong -> {
                        Set<String> keywords = resultDefinitions.get(aLong.intValue()).getKeywords();
                        return keywords.containsAll(value.getKey()) && keywords.size() == value.getKey().size()
                                ;
                    }).findFirst();
                    return index1.orElse(-1l);
                }).filter(v -> v > -1).toArray();

        for (Long l : resultsFound) {
            matchedResultDefinition.add(resultDefinitions.get(l.intValue()));
        }


        return matchedResultDefinition;
    }
}
