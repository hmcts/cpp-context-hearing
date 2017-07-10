package uk.gov.justice.ccr.notepad.process;


import static com.google.common.collect.Sets.cartesianProduct;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.powerSet;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;

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
        resultPromptMatchingOutput.setResultPrompt(matchResultPrompts(matchedSynonymWords));
        return resultPromptMatchingOutput;
    }

    private ResultPrompt matchResultPrompts(final Map<String, Set<String>> matchedSynonymWords) throws ExecutionException {
        Set<List<String>> allCombinations = cartesianProduct(matchedSynonymWords.entrySet().stream().map(Map.Entry::getValue)
                .collect(Collectors.toList()));
        Set<Set<String>> setOfPossibleCombination = newHashSet();
        for (List<String> words : allCombinations) {
            Set<Set<String>> powerSets = powerSet(new HashSet<>(words));
            powerSets.stream().filter(CollectionUtils::isNotEmpty).forEach(setOfPossibleCombination::add);
        }

        Map<Long, Set<Set<String>>> matchingWordsInDescendingOrder = findCombinationHaveMaximumMatches(setOfPossibleCombination);


        for (Map.Entry<Long, Set<Set<String>>> entry : matchingWordsInDescendingOrder.entrySet()) {
            ResultPrompt resultPrompt = getMatchedResultDefinition(entry.getValue());
            if (resultPrompt != null) {
                return resultPrompt;
            }
        }


        return null;
    }

    private ResultPrompt getMatchedResultDefinition(final Set<Set<String>> setOfWords) throws ExecutionException {
        Map<Set<String>, Set<Long>> input = Maps.newHashMap();
        for (Set<String> words : setOfWords) {
            input.put(words, new HashSet<>(findPromptsIndexesByKeyword.run(words)));
        }
        return comparePromptKeywordsUsingIndexes.run(input);
    }

    private Map<Long, Set<Set<String>>> findCombinationHaveMaximumMatches(final Set<Set<String>> allCombinations) throws ExecutionException {
        Map<Long, Set<Set<String>>> orderedMatchedAsPerCount = Maps.newHashMap();
        for (Set<String> words : allCombinations) {
            List<Long> resultDefinitionIndexes = findPromptsIndexesByKeyword.run(words);
            Map<Long, Long> byIndex = groupResultByIndex.run(resultDefinitionIndexes);
            if (!byIndex.isEmpty()) {
                long maxMatch = Collections.max(byIndex.values());
                if (maxMatch == words.size()) {
                    orderedMatchedAsPerCount.putIfAbsent(maxMatch, newHashSet());
                    orderedMatchedAsPerCount.get(maxMatch).add(words);
                }
            }
        }
        Map<Long, Set<Set<String>>> descendingOrder = new TreeMap(Collections.reverseOrder());
        descendingOrder.putAll(orderedMatchedAsPerCount);
        return descendingOrder;
    }
}
