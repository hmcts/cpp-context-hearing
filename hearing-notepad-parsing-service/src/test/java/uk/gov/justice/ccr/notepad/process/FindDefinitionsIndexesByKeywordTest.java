package uk.gov.justice.ccr.notepad.process;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.ccr.notepad.result.cache.CacheFactory;
import uk.gov.justice.ccr.notepad.result.cache.ResultCache;
import uk.gov.justice.ccr.notepad.result.loader.FileResultLoader;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.LoadingCache;
import org.hamcrest.core.AnyOf;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FindDefinitionsIndexesByKeywordTest {
    @Spy
    @InjectMocks
    ResultCache resultCache = new ResultCache();

    @Spy
    FileResultLoader fileResultLoader = new FileResultLoader();

    @Mock
    private CacheFactory cacheFactory;

    @Mock
    private LoadingCache<String, Object> cache;

    @InjectMocks
    FindDefinitionsIndexesByKeyword testObj;

    @Before
    public void init() throws ExecutionException {
        when(cacheFactory.build()).thenReturn(cache);
        final ConcurrentHashMap<String, Object> cacheValue = new ConcurrentHashMap<>();
        when(cache.asMap()).thenReturn(cacheValue);
        resultCache.lazyLoad(null, LocalDate.now());
    }

    @Test
    public void run() throws Exception {
        Set<String> words = newHashSet("rehabilitation", "imprisonment", "suspended");

        List<Long> resultDefinitionsIndex = testObj.run(words, LocalDate.now());

        assertThat(
                resultDefinitionsIndex.size()
                , is(41)
        );
        resultDefinitionsIndex.forEach(resultDefinition -> {
//            try {
                assertThat(
                        resultCache.getResultDefinitions(LocalDate.now()).get(resultDefinition.intValue()).getKeywords().toString(),
                        AnyOf.anyOf(containsString("rehabilitation"), containsString("imprisonment"), containsString("suspended"))
                );
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            }
        });
    }

}
