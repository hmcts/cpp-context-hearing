package uk.gov.justice.ccr.notepad.process;


import static java.util.stream.Collectors.toMap;

import uk.gov.justice.ccr.notepad.result.cache.ResultCache;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

class FindDefinitionsByShortCodes implements ResultFilter<Set<ResultDefinition>, List<String>> {

    @Inject
    ResultCache resultCache;

    @Override
    public Set<ResultDefinition> run(final List<String> values) throws ExecutionException {

        Map<String, ResultDefinition> filteredResult = resultCache.getResultDefinition().stream()
                .filter(v -> !v.getShortCode().isEmpty())
                .filter(v -> values.contains(v.getShortCode()))
                .collect(toMap(ResultDefinition::getShortCode, v -> v, (p1, p2) -> p1));
        return new HashSet<>(filteredResult.values());
    }
}
