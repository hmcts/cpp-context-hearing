package uk.gov.justice.ccr.notepad.process;


import static java.util.stream.Collectors.toList;

import uk.gov.justice.ccr.notepad.result.cache.ResultCache;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

public class FindDefinitionsIndexesByKeyword implements ResultFilter<List<Long>, Set<String>> {

    @Inject
    private ResultCache resultCache;

    @Override
    public List<Long> run(final Set<String> words, final LocalDate orderedDate) {
        return resultCache.getResultDefinitionsIndexGroupByKeyword(orderedDate).entrySet()
                .stream()
                .filter(entry -> words.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .collect(toList());
    }
}
