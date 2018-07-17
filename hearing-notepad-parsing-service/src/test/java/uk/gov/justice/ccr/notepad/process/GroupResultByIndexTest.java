package uk.gov.justice.ccr.notepad.process;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.ccr.notepad.result.cache.CacheFactory;
import uk.gov.justice.ccr.notepad.result.cache.ResultCache;
import uk.gov.justice.ccr.notepad.result.loader.FileResultLoader;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.LoadingCache;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GroupResultByIndexTest {
    @Spy
    @InjectMocks
    ResultCache resultCache = new ResultCache();
    @Spy
    FileResultLoader fileResultLoader = new FileResultLoader();

    @InjectMocks
    FindDefinitionsIndexesByKeyword findDefinitionsIndexesByKeyword;

    @Mock
    private CacheFactory cacheFactory;

    @Mock
    private LoadingCache<String, Object> cache;

    @Before
    public void init() throws ExecutionException {
        when(cacheFactory.build()).thenReturn(cache);
        final ConcurrentHashMap<String, Object> cacheValue = new ConcurrentHashMap<>();
        when(cache.asMap()).thenReturn(cacheValue);
        resultCache.lazyLoad(null, LocalDate.now());
    }

    @Test
    public void run() throws Exception {
        Set<String> words = new HashSet<>(Arrays.asList("imprisonment", "suspended"));
        List<Long> resultDefinitions = findDefinitionsIndexesByKeyword.run(words, LocalDate.now());

        GroupResultByIndex testObj = new GroupResultByIndex();
        Map<Long, Long> outPut = testObj.run(resultDefinitions);

        assertThat(
                outPut.size()
                , is(29)
        );

    }


    @Test
    public void runWithMultipleWords() throws Exception {
        Set<String> words = new HashSet<>(Arrays.asList("restraining", "order","period","imprisonment", "suspended"));
        List<Long> resultDefinitions = findDefinitionsIndexesByKeyword.run(words, LocalDate.now());

        GroupResultByIndex testObj = new GroupResultByIndex();
        Map<Long, Long> outPut = testObj.run(resultDefinitions);

        assertThat(
                outPut.size()
                , is(88)
        );

    }

}
