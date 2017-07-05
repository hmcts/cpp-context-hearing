package uk.gov.justice.ccr.notepad.process;


import static com.google.common.collect.Sets.cartesianProduct;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.powerSet;
import static java.util.Map.Entry;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;


class ResultPromptMatcher {

    @Inject
    FindPromptsIndexesByKeyword findPromptsIndexesByKeyword;

    @Inject
    ComparePromptKeywordsUsingIndexes comparePromptKeywordsUsingIndexes;

    @Inject
    GroupResultByIndex groupResultByIndex;

    @Inject
    FindPromptSynonyms findPromptSynonyms;

    public ResultPromptMatchingOutput match(final List<String> values) throws ExecutionException {
        return matchEqual(values);
    }


    ResultPromptMatchingOutput matchEqual(final List<String> values) throws ExecutionException {
        Map<String, Set<String>> matchedSynonymWords = findPromptSynonyms.run(values);
        ResultPromptMatchingOutput resultPromptMatchingOutput = new ResultPromptMatchingOutput();
        resultPromptMatchingOutput.setResultPrompt(getResultPrompt(matchedSynonymWords));
        return resultPromptMatchingOutput;
    }

    private ResultPrompt getResultPrompt(final Map<String, Set<String>> matchedSynonymWords) throws ExecutionException {
        Set<List<String>> allCombinations = cartesianProduct(matchedSynonymWords.entrySet().stream().map(Entry::getValue)
                .collect(Collectors.toList()));
        for (Set<List<String>> combinations : findCombinationHaveMaximumMatches(allCombinations).values()) {
            for (List<String> words : combinations) {
                List<Long> resultPromptIndexes = findPromptsIndexesByKeyword.run(words);
                Map<Long, Long> byIndex = groupResultByIndex.run(resultPromptIndexes);
                Set<Set<String>> powerSets = powerSet(new HashSet<>(words));
                Map<Integer, Set<Object>> bucketOfSets = Maps.newHashMap();
                powerSets.stream().filter(CollectionUtils::isNotEmpty).forEach(strings -> {
                    Set<Object> newItem = newHashSet();
                    bucketOfSets.putIfAbsent(strings.size(), newItem);
                    bucketOfSets.computeIfPresent(strings.size(), (integer, sets) -> {
                        sets.add(strings);
                        return sets;
                    });

                });
                for (int i = bucketOfSets.size(); i > 0; i--) {
                    ResultPrompt resultPrompt = getMatchedResultPrompt(byIndex, bucketOfSets, i);
                    if (resultPrompt != null) {
                        return resultPrompt;
                    }
                }
            }
        }
        return null;
    }

    private ResultPrompt getMatchedResultPrompt(final Map<Long, Long> byIndex, final Map<Integer, Set<Object>> bucketOfSets, final int i) throws ExecutionException {
        Map<List<String>, Set<Long>> input = Maps.newHashMap();
        for (Object powerSet : bucketOfSets.get(i)) {
            int finalI = i;
            Set<Long> indexes = byIndex.entrySet().stream().filter(v -> v.getValue() == finalI).collect(Collectors.toMap(Entry::getKey, Entry::getValue)).keySet();
            input.putIfAbsent(new ArrayList<>((Set) powerSet), indexes);
        }
        return comparePromptKeywordsUsingIndexes.run(input);
    }

    private Map<Long, Set<List<String>>> findCombinationHaveMaximumMatches(final Set<List<String>> allCombinations) throws ExecutionException {
        Map<Long, Set<List<String>>> orderedMatchedAsPerCount = Maps.newHashMap();
        for (List<String> words : allCombinations) {
            List<Long> resultPromptIndexes = findPromptsIndexesByKeyword.run(words);
            Map<Long, Long> byIndex = groupResultByIndex.run(resultPromptIndexes);
            if (!byIndex.isEmpty()) {
                long maxMatch = Collections.max(byIndex.values());
                orderedMatchedAsPerCount.putIfAbsent(maxMatch, newHashSet());
                orderedMatchedAsPerCount.get(maxMatch).add(words);
            }
        }
        Map<Long, Set<List<String>>> descendingOrder = new TreeMap(Collections.reverseOrder());
        descendingOrder.putAll(orderedMatchedAsPerCount);
        return descendingOrder;
    }
}
