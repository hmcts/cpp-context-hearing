package uk.gov.justice.ccr.notepad.process;

import static com.google.common.collect.Sets.cartesianProduct;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.powerSet;
import static uk.gov.justice.ccr.notepad.process.ResultDefinitionMatchingOutput.MatchingType.EQUALS;
import static uk.gov.justice.ccr.notepad.process.ResultDefinitionMatchingOutput.MatchingType.SHORT_CODE;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;

import java.time.LocalDate;
import java.util.Collections;
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


public class ResultDefinitionMatcher {

    @Inject
    FindDefinitionsIndexesByKeyword findDefinitionsIndexesByKeyword;

    @Inject
    CompareDefinitionKeywordsUsingIndexes compareDefinitionKeywordsUsingIndexes;

    @Inject
    GroupResultByIndex groupResultByIndex;

    @Inject
    FindDefinitionPartialMatchSynonyms findDefinitionPartialMatchSynonyms;

    @Inject
    FindDefinitionsByShortCodes findDefinitionsByShortCodes;

    @Inject
    FindDefinitionExactMatchSynonyms findDefinitionExactMatchSynonyms;

    public ResultDefinitionMatchingOutput match(final List<String> values, final LocalDate orderedDate) {
        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = new ResultDefinitionMatchingOutput();
        Optional<ResultDefinition> resultDefinition = matchBySynonym(values, orderedDate);

        if (resultDefinition.isPresent()) {
            resultDefinitionMatchingOutput.setResultDefinition(resultDefinition.get());
            resultDefinitionMatchingOutput.setMatchingType(EQUALS);
        } else {
            resultDefinition = matchByShortCode(values, orderedDate);
            if (resultDefinition.isPresent()) {
                resultDefinitionMatchingOutput.setResultDefinition(resultDefinition.get());
                resultDefinitionMatchingOutput.setMatchingType(SHORT_CODE);
            }
        }
        return resultDefinitionMatchingOutput;
    }

    public Optional<ResultDefinition> matchContains(final List<String> values, final LocalDate orderedDate) {
        final List<String> filteredValues = values.stream().filter(value -> value.length() > 1).collect(Collectors.toList());
        final Map<String, Set<String>> matchedSynonymWords = findDefinitionPartialMatchSynonyms.run(filteredValues, orderedDate);
        return Optional.ofNullable(matchResultDefinition(matchedSynonymWords, orderedDate));
    }

    public Optional<ResultDefinition> matchByShortCode(final List<String> values, final LocalDate orderedDate) {
        final Set<ResultDefinition> outPut = findDefinitionsByShortCodes.run(values, orderedDate);
        if (outPut.size() > 1) {
            return Optional.empty();
        }
        return outPut.stream().findFirst();
    }

    public Optional<ResultDefinition> matchBySynonym(final List<String> values, final LocalDate orderedDate) {
        final Map<String, Set<String>> matchedSynonymWords = findDefinitionExactMatchSynonyms.run(values, orderedDate);
        return Optional.ofNullable(matchResultDefinition(matchedSynonymWords, orderedDate));
    }

    private ResultDefinition matchResultDefinition(final Map<String, Set<String>> matchedSynonymWords, final LocalDate orderedDate) {
        Set<List<String>> allCombinations = cartesianProduct(matchedSynonymWords.entrySet().stream().map(Map.Entry::getValue)
                .collect(Collectors.toList()));
        Set<Set<String>> setOfPossibleCombination = newHashSet();
        for (List<String> words : allCombinations) {
            Set<Set<String>> powerSets = powerSet(new HashSet<>(words));
            powerSets.stream().filter(CollectionUtils::isNotEmpty).forEach(setOfPossibleCombination::add);
        }

        final Map<Long, Set<Set<String>>> matchingWordsInDescendingOrder = findCombinationHaveMaximumMatches(setOfPossibleCombination, orderedDate);

        for (Map.Entry<Long, Set<Set<String>>> entry : matchingWordsInDescendingOrder.entrySet()) {
            final List<ResultDefinition> resultDefinitions = getMatchedResultDefinition(entry.getValue(), orderedDate);
            if (resultDefinitions.size() > 1) {
                return null;
            } else if (resultDefinitions.size() == 1) {
                return resultDefinitions.get(0);
            }
        }

        return null;
    }

    private List<ResultDefinition> getMatchedResultDefinition(final Set<Set<String>> setOfWords, final LocalDate orderedDate) {
        Map<Set<String>, Set<Long>> input = Maps.newHashMap();
        for (Set<String> words : setOfWords) {
            input.put(words, new HashSet<>(findDefinitionsIndexesByKeyword.run(words, orderedDate)));
        }
        return compareDefinitionKeywordsUsingIndexes.run(input, orderedDate);
    }

    private Map<Long, Set<Set<String>>> findCombinationHaveMaximumMatches(final Set<Set<String>> allCombinations, final LocalDate orderedDate) {
        Map<Long, Set<Set<String>>> orderedMatchedAsPerCount = Maps.newHashMap();
        for (Set<String> words : allCombinations) {
            final List<Long> resultDefinitionIndexes = findDefinitionsIndexesByKeyword.run(words, orderedDate);
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
