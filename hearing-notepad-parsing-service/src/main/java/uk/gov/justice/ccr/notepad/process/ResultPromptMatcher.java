package uk.gov.justice.ccr.notepad.process;

import static com.google.common.collect.Sets.cartesianProduct;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.powerSet;
import static java.util.Objects.nonNull;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;
import uk.gov.justice.ccr.notepad.view.Part;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;

public class ResultPromptMatcher {

    @Inject
    FindPromptsIndexesByKeyword findPromptsIndexesByKeyword;

    @Inject
    ComparePromptKeywordsUsingIndexes comparePromptKeywordsUsingIndexes;

    @Inject
    GroupResultByIndex groupResultByIndex;

    @Inject
    FindPromptSynonyms findPromptSynonyms;

    public ResultPromptMatchingOutput match(final List<String> values, final LocalDate orderedDate) {
        return match(values, orderedDate, Optional.empty());
    }

    public ResultPromptMatchingOutput match(final List<String> values, final LocalDate orderedDate, Optional<Knowledge> knowledge) {
        final Map<String, Set<String>> matchedSynonyms = findPromptSynonyms.run(values, orderedDate);
        final ResultPrompt resultPrompt = matchResultPromptsBySynonyms(matchedSynonyms, orderedDate, knowledge);
        final ResultPromptMatchingOutput resultPromptMatchingOutput = new ResultPromptMatchingOutput();

        resultPromptMatchingOutput.setResultPrompt(resultPrompt);
        return resultPromptMatchingOutput;
    }

    private ResultPrompt matchResultPromptsBySynonyms(final Map<String, Set<String>> matchedSynonyms, final LocalDate orderedDate, final Optional<Knowledge> knowledge) {
        final Set<List<String>> allCombinations = cartesianProduct(matchedSynonyms.entrySet().stream().map(Map.Entry::getValue)
                .collect(Collectors.toList()));
        Set<Set<String>> setOfPossibleCombination = newHashSet();

        for (List<String> words : allCombinations) {
            Set<Set<String>> powerSets = powerSet(new HashSet<>(words));
            powerSets.stream().filter(CollectionUtils::isNotEmpty).forEach(setOfPossibleCombination::add);
        }

        final Map<Long, Set<Set<String>>> matchingWordsInDescendingOrder = findCombinationHaveMaximumMatches(setOfPossibleCombination, orderedDate);

        for (Map.Entry<Long, Set<Set<String>>> entry : matchingWordsInDescendingOrder.entrySet()) {
            final Map<Set<String>, Set<Long>> indexHashMap = new HashMap<>();
            final Set<Set<String>> entryValue = entry.getValue();

            for (final Set<String> words : entryValue) {
                indexHashMap.put(words, new HashSet<>(findPromptsIndexesByKeyword.run(words, orderedDate)));
            }

            if (!knowledge.isPresent()) {
                final ResultPrompt resultPrompt = comparePromptKeywordsUsingIndexes.run(indexHashMap, orderedDate);
                if (nonNull(resultPrompt)) {
                    return resultPrompt;
                }
            } else {
                final Set<String> resultDefinitionIds = knowledge.get().getResultDefinitionParts().values().stream().map(Part::getCode).collect(Collectors.toSet());
                final ResultPrompt resultPrompt = comparePromptKeywordsUsingIndexes.run(indexHashMap, orderedDate, Optional.of(resultDefinitionIds));
                if (nonNull(resultPrompt)) {
                    return resultPrompt;
                }
            }

        }
        return null;
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
