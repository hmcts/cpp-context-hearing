package uk.gov.justice.ccr.notepad.process;


import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.cartesianProduct;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.powerSet;
import static uk.gov.justice.ccr.notepad.process.ResultDefinitionMatchingOutput.MatchingType.EQUALS;
import static uk.gov.justice.ccr.notepad.process.ResultDefinitionMatchingOutput.MatchingType.SHORT_CODE;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;


class ResultDefinitionMatcher {

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

    public ResultDefinitionMatchingOutput match(final List<String> values) throws ExecutionException {
        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = new ResultDefinitionMatchingOutput();
        Optional<ResultDefinition> resultDefinition = matchEqual(values);
        if (resultDefinition.isPresent()) {
            resultDefinitionMatchingOutput.setResultDefinition(resultDefinition.get());
            resultDefinitionMatchingOutput.setMatchingType(EQUALS);
        } else {
            resultDefinition = matchShortCode(values);
            if (resultDefinition.isPresent()) {
                resultDefinitionMatchingOutput.setResultDefinition(resultDefinition.get());
                resultDefinitionMatchingOutput.setMatchingType(SHORT_CODE);
            }
        }
        return resultDefinitionMatchingOutput;
    }

    Optional<ResultDefinition> matchContains(final List<String> values) throws ExecutionException {
        //For contains search part length should be > 1
        List<String> filteredValues = values.stream().filter(v -> v.length() > 1).collect(Collectors.toList());
        Map<String, Set<String>> matchedSynonymWords = findDefinitionPartialMatchSynonyms.run(filteredValues);
        return Optional.ofNullable(matchResultDefinition(matchedSynonymWords));
    }

    Optional<ResultDefinition> matchShortCode(final List<String> values) throws ExecutionException {
        Set<ResultDefinition> outPut = findDefinitionsByShortCodes.run(values);
        if (outPut.size() > 1) {
            return Optional.empty();
        }
        return outPut.stream().findFirst();
    }

    Optional<ResultDefinition> matchEqual(final List<String> values) throws ExecutionException {
        Map<String, Set<String>> matchedSynonymWords = findDefinitionExactMatchSynonyms.run(values);
        return Optional.ofNullable(matchResultDefinition(matchedSynonymWords));
    }

    private ResultDefinition matchResultDefinition(final Map<String, Set<String>> matchedSynonymWords) throws ExecutionException {
        Set<List<String>> allCombinations = cartesianProduct(matchedSynonymWords.entrySet().stream().map(Map.Entry::getValue)
                .collect(Collectors.toList()));
        for (Set<List<String>> combinations : findCombinationHaveMaximumMatches(allCombinations).values()) {
            for (List<String> words : combinations) {
                List<Long> resultDefinitionIndexes = findDefinitionsIndexesByKeyword.run(words);
                Map<Long, Long> byIndex = groupResultByIndex.run(resultDefinitionIndexes);
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
                    List<ResultDefinition> resultDefinitions = getMatchedResultDefinition(byIndex, bucketOfSets, i);
                    if (resultDefinitions.size() > 1) {
                        return null;
                    } else if (resultDefinitions.size() == 1) {
                        return resultDefinitions.get(0);
                    }
                }
            }
        }
        return null;
    }

    private List<ResultDefinition> getMatchedResultDefinition(final Map<Long, Long> byIndex, final Map<Integer, Set<Object>> bucketOfSets, final int i) throws ExecutionException {
        Map<List<String>, Set<Long>> input = Maps.newHashMap();
        for (Object powerSet : bucketOfSets.get(i)) {
            int finalI = i;
            Set<Long> indexes = byIndex.entrySet().stream().filter(v -> v.getValue() == finalI).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).keySet();
            input.putIfAbsent(new ArrayList<>((Set) powerSet), indexes);
        }
        return compareDefinitionKeywordsUsingIndexes.run(input);
    }

    private Map<Long, Set<List<String>>> findCombinationHaveMaximumMatches(final Set<List<String>> allCombinations) throws ExecutionException {
        Map<Long, Set<List<String>>> orderedMatchedAsPerCount = Maps.newHashMap();
        List<String> singleWordMatch = newArrayList();
        Set<List<String>> entry = newHashSet();
        entry.add(singleWordMatch);
        orderedMatchedAsPerCount.putIfAbsent(1l, entry);
        for (List<String> words : allCombinations) {
            List<Long> resultDefinitionIndexes = findDefinitionsIndexesByKeyword.run(words);
            Map<Long, Long> byIndex = groupResultByIndex.run(resultDefinitionIndexes);
            if (!byIndex.isEmpty()) {
                long maxMatch = Collections.max(byIndex.values());
                if (maxMatch > 1) {
                    orderedMatchedAsPerCount.putIfAbsent(maxMatch, newHashSet());
                    orderedMatchedAsPerCount.get(maxMatch).add(words);
                } else {
                    singleWordMatch.addAll(words);
                }
            }
        }
        Map<Long, Set<List<String>>> descendingOrder = new TreeMap(Collections.reverseOrder());
        descendingOrder.putAll(orderedMatchedAsPerCount);
        return descendingOrder;
    }

}
