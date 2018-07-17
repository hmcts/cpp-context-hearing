package uk.gov.justice.ccr.notepad.process;


import static com.google.common.collect.Sets.cartesianProduct;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.powerSet;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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

    public ResultPromptMatchingOutput match(final List<String> values, final LocalDate orderedDate) {
        return matchEqual(values, orderedDate);
    }


    ResultPromptMatchingOutput matchEqual(final List<String> values, final LocalDate orderedDate) {
        final Map<String, Set<String>> matchedSynonymWords = findPromptSynonyms.run(values, orderedDate);
        ResultPromptMatchingOutput resultPromptMatchingOutput = new ResultPromptMatchingOutput();
        resultPromptMatchingOutput.setResultPrompt(matchResultPrompts(matchedSynonymWords, orderedDate));
        return resultPromptMatchingOutput;
    }

    private ResultPrompt matchResultPrompts(final Map<String, Set<String>> matchedSynonymWords, final LocalDate orderedDate) {
        Set<List<String>> allCombinations = cartesianProduct(matchedSynonymWords.entrySet().stream().map(Map.Entry::getValue)
                .collect(Collectors.toList()));
        Set<Set<String>> setOfPossibleCombination = newHashSet();
        for (List<String> words : allCombinations) {
            Set<Set<String>> powerSets = powerSet(new HashSet<>(words));
            powerSets.stream().filter(CollectionUtils::isNotEmpty).forEach(setOfPossibleCombination::add);
        }

        final Map<Long, Set<Set<String>>> matchingWordsInDescendingOrder = findCombinationHaveMaximumMatches(setOfPossibleCombination, orderedDate);


        for (Map.Entry<Long, Set<Set<String>>> entry : matchingWordsInDescendingOrder.entrySet()) {
            final ResultPrompt resultPrompt = getMatchedResultDefinition(entry.getValue(), orderedDate);
            if (resultPrompt != null) {
                return resultPrompt;
            }
        }


        return null;
    }

    private ResultPrompt getMatchedResultDefinition(final Set<Set<String>> setOfWords, final LocalDate orderedDate) {
        Map<Set<String>, Set<Long>> input = Maps.newHashMap();
        for (Set<String> words : setOfWords) {
            input.put(words, new HashSet<>(findPromptsIndexesByKeyword.run(words, orderedDate)));
        }
        return comparePromptKeywordsUsingIndexes.run(input, orderedDate);
    }

    private Map<Long, Set<Set<String>>> findCombinationHaveMaximumMatches(final Set<Set<String>> allCombinations, final LocalDate orderedDate) {
        Map<Long, Set<Set<String>>> orderedMatchedAsPerCount = Maps.newHashMap();
        for (Set<String> words : allCombinations) {
            final List<Long> resultDefinitionIndexes = findPromptsIndexesByKeyword.run(words, orderedDate);
            Map<Long, Long> byIndex = groupResultByIndex.run(resultDefinitionIndexes);
            if (!byIndex.isEmpty()) {
                long maxMatch = Collections.max(byIndex.values());
                if (maxMatch == words.size()) {
                    orderedMatchedAsPerCount.putIfAbsent(maxMatch, newHashSet());
                    orderedMatchedAsPerCount.get(maxMatch).add(words);
                }
            }
        }
        Map<Long, Set<Set<String>>> descendingOrder = new TreeMap<>(Collections.reverseOrder());
        descendingOrder.putAll(orderedMatchedAsPerCount);
        return descendingOrder;
    }
}
