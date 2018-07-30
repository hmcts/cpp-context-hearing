package uk.gov.justice.ccr.notepad.process;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.ccr.notepad.result.cache.CacheFactory;
import uk.gov.justice.ccr.notepad.result.cache.ResultCache;
import uk.gov.justice.ccr.notepad.result.loader.FileResultLoader;
import uk.gov.justice.ccr.notepad.view.Part;
import uk.gov.justice.ccr.notepad.view.parser.PartsResolver;

import java.time.LocalDate;
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
public class FindDefinitionExactMatchSynonymsTest {
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
    FindDefinitionExactMatchSynonyms testObj;

    @Before
    public void init() throws ExecutionException {
        when(cacheFactory.build()).thenReturn(cache);
        final ConcurrentHashMap<String, Object> cacheValue = new ConcurrentHashMap<>();
        when(cache.asMap()).thenReturn(cacheValue);
        resultCache.lazyLoad(null, LocalDate.now());
    }

    @Test
    public void run() throws Exception {
        List<Part> parts = new PartsResolver().getParts("imp sus Rehabilitation excep");

        Map<String, Set<String>> output = testObj.run(parts.stream().map(part -> part.getValueAsString().toLowerCase()).collect(toList()), LocalDate.now());

        assertThat(
                output.size()
                , is(3)
        );

        assertThat(output.keySet(), hasItems("imp", "rehabilitation", "sus"));
    }

    @Test
    public void run_WhenNothingMatched() throws Exception {
        List<Part> parts = new PartsResolver().getParts("[ssssss]");

        Map<String, Set<String>> words = testObj.run(parts.stream().map(part -> part.getValueAsString().toLowerCase()).collect(toList()), LocalDate.now());

        assertThat(
                words.size()
                , is(0)
        );

    }

}
