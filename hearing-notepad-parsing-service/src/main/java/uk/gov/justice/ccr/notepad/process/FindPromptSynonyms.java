package uk.gov.justice.ccr.notepad.process;


import static com.google.common.collect.Maps.newHashMap;
import static java.util.Map.Entry;

import uk.gov.justice.ccr.notepad.result.cache.ResultCache;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.google.common.collect.Sets;

public class FindPromptSynonyms implements ResultFilter<Map<String, Set<String>>, List<String>> {

    @Inject
    private ResultCache resultCache;

    @Override
    public Map<String, Set<String>> run(final List<String> values, final LocalDate orderedDate) {
        Map<String, Set<String>> output = newHashMap();
        values.forEach(v -> output.putIfAbsent(v, Sets.newHashSet()));

        resultCache.getResultPromptSynonym(orderedDate)
                .forEach(v -> output.entrySet().stream().forEach(stringSetEntry -> {
                    if (v.getSynonym().equals(stringSetEntry.getKey())) {
                        stringSetEntry.getValue().add(v.getWord());
                    }
                }));

        return output.entrySet().stream().filter(stringSetEntry -> !stringSetEntry.getValue().isEmpty()).collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    }
}
