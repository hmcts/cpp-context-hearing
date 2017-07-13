package uk.gov.justice.ccr.notepad.process;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.ccr.notepad.result.cache.ResultCache;
import uk.gov.justice.ccr.notepad.result.loader.FileResultLoader;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;


public class GroupResultByIndexTest {
    ResultCache resultCache = new ResultCache();
    FileResultLoader fileResultLoader = new FileResultLoader();
    FindDefinitionsIndexesByKeyword findDefinitionsIndexesByKeyword = new FindDefinitionsIndexesByKeyword();

    @Before
    public void init() throws ExecutionException {
        resultCache.setResultLoader(fileResultLoader);
        resultCache.lazyLoad(null);
        findDefinitionsIndexesByKeyword.resultCache = resultCache;
    }

    @Test
    public void run() throws Exception {
        Set<String> words = new HashSet<>(Arrays.asList("imprisonment", "suspended"));
        List<Long> resultDefinitions = findDefinitionsIndexesByKeyword.run(words);

        GroupResultByIndex testObj = new GroupResultByIndex();
        Map<Long, Long> outPut = testObj.run(resultDefinitions);

        assertThat(
                outPut.size()
                , is(25)
        );

    }


    @Test
    public void runWithMutipleWords() throws Exception {
        Set<String> words = new HashSet<>(Arrays.asList("restraining", "order","period","imprisonment", "suspended"));
        List<Long> resultDefinitions = findDefinitionsIndexesByKeyword.run(words);

        GroupResultByIndex testObj = new GroupResultByIndex();
        Map<Long, Long> outPut = testObj.run(resultDefinitions);

        assertThat(
                outPut.size()
                , is(49)
        );

    }

}