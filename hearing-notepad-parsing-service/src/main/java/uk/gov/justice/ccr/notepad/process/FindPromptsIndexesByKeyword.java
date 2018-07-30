package uk.gov.justice.ccr.notepad.process;


import uk.gov.justice.ccr.notepad.result.cache.ResultCache;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

class FindPromptsIndexesByKeyword implements ResultFilter<List<Long>, Set<String>> {


    @Inject
    ResultCache resultCache;

    @Override
    public List<Long> run(final Set<String> words, final LocalDate orderedDate) {
        return resultCache.getResultPromptsIndexGroupByKeyword(orderedDate).entrySet()
                .stream()
                .filter(entry -> words.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
