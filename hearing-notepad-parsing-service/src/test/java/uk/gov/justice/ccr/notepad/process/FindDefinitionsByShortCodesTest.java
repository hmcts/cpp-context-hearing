package uk.gov.justice.ccr.notepad.process;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.ccr.notepad.result.cache.CacheFactory;
import uk.gov.justice.ccr.notepad.result.cache.ResultCache;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;
import uk.gov.justice.ccr.notepad.result.loader.FileResultLoader;
import uk.gov.justice.ccr.notepad.view.Part;
import uk.gov.justice.ccr.notepad.view.parser.PartsResolver;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
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
public class FindDefinitionsByShortCodesTest {
    @Spy
    @InjectMocks
    ResultCache resultCache = new ResultCache();
    @Spy
    FileResultLoader fileResultLoader = new FileResultLoader();
    @InjectMocks
    FindDefinitionsByShortCodes testObj;
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
    public void run1() throws Exception {
        List<Part> parts = new PartsResolver().getParts("Imp SHOPE PaRP pr sati stimp");

        Set<ResultDefinition> resultDefinitions = testObj.run(parts.stream().map(part -> part.getValueAsString().toLowerCase()).collect(toList()), LocalDate.now());

        assertThat(
                resultDefinitions.size()
                , is(5)
        );
        assertThat(
                Arrays.asList("imp", "parp", "pr", "shope", "stimp").containsAll(resultDefinitions.stream().map(ResultDefinition::getShortCode).collect(toList()))
                , is(true)
        );

    }

    @Test
    public void run2() throws Exception {
        List<Part> parts = new PartsResolver().getParts("SHOPE");

        Set<ResultDefinition> resultDefinitions = testObj.run(parts.stream().map(part -> part.getValueAsString().toLowerCase()).collect(toList()), LocalDate.now());

        assertThat(
                resultDefinitions.size()
                , is(1)
        );
        assertThat(
                Arrays.asList("shope").containsAll(resultDefinitions.stream().map(ResultDefinition::getShortCode).collect(toList()))
                , is(true)
        );

    }
}