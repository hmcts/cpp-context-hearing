package uk.gov.justice.ccr.notepad.result;


import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.ccr.notepad.result.cache.ResultCache;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

public class ResultDefinitionLoaderTest {

    ResultCache resultCache = new ResultCache();
    @Before
    public void init() throws ExecutionException {
        resultCache.loadResultCache();
    }

    @Test
    public void getResultDefinition() throws Exception {
        assertThat(resultCache.getResultDefinition().size(), is(414));
    }

    @Test
    public void resultDefinitionKeywordsShouldhaveSynonymsDefined() throws Exception {
        List<String> allWords = newArrayList();
        resultCache.getResultDefinition()
                .stream()
                .map(value -> value.getKeywords()).forEach(s -> allWords.addAll(s));

        List<String> resultDefinitionSynonyms = resultCache.getResultDefinitionSynonym()
                .stream().map(value -> value.getWord().toLowerCase()).collect(Collectors.toList());
        allWords.stream().forEach(value -> {
                    assertThat(resultDefinitionSynonyms.stream().filter(s -> s.equalsIgnoreCase(value)).findFirst().orElse(""), is(value.toLowerCase()));
                }
        );

    }

}