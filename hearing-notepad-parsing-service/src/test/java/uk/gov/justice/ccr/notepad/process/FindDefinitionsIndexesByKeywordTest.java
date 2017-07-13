package uk.gov.justice.ccr.notepad.process;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.ccr.notepad.result.cache.ResultCache;
import uk.gov.justice.ccr.notepad.result.loader.FileResultLoader;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.hamcrest.core.AnyOf;
import org.junit.Before;
import org.junit.Test;


public class FindDefinitionsIndexesByKeywordTest {
    ResultCache resultCache = new ResultCache();
    FileResultLoader fileResultLoader = new FileResultLoader();
    FindDefinitionsIndexesByKeyword testObj = new FindDefinitionsIndexesByKeyword();

    @Before
    public void init() throws ExecutionException {
        resultCache.setResultLoader(fileResultLoader);
        resultCache.lazyLoad(null);
        testObj.resultCache = resultCache;
    }

    @Test
    public void run() throws Exception {
        Set<String> words = new HashSet<>(Arrays.asList("rehabilitation", "imprisonment", "suspended"));

        List<Long> resultDefinitionsIndex = testObj.run(words);

        assertThat(
                resultDefinitionsIndex.size()
                , is(35)
        );
        resultDefinitionsIndex.forEach(resultDefinition -> {
            try {
                assertThat(
                        resultCache.getResultDefinition().get(resultDefinition.intValue()).getKeywords().toString(),
                        AnyOf.anyOf(containsString("rehabilitation"), containsString("imprisonment"), containsString("suspended"))
                );
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

}