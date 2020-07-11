package uk.gov.justice.ccr.notepad.shared;

import com.google.common.cache.LoadingCache;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import uk.gov.justice.ccr.notepad.process.*;
import uk.gov.justice.ccr.notepad.result.cache.CacheFactory;
import uk.gov.justice.ccr.notepad.result.cache.ResultCache;
import uk.gov.justice.ccr.notepad.result.loader.FileResultLoader;
import uk.gov.justice.ccr.notepad.result.loader.ResultLoader;

import java.util.concurrent.ConcurrentHashMap;

import static java.time.LocalDate.now;
import static org.mockito.Mockito.when;

@SuppressWarnings("squid:S2187")
public class AbstractTest {
    @Mock
    protected CacheFactory cacheFactory;
    @Mock
    protected LoadingCache<String, Object> cache;

    @Spy
    protected final ResultLoader resultLoader = new FileResultLoader();
    @Spy
    protected GroupResultByIndex groupResultByIndex = new GroupResultByIndex();
    @Spy
    protected Time24HoursMatcher time24HoursMatcher = new Time24HoursMatcher();
    @Spy
    protected DateMatcher dateMatcher = new DateMatcher();
    @Spy
    protected FileResultLoader fileResultLoader = new FileResultLoader();

    @Spy
    @InjectMocks
    protected ResultCache resultCache = new ResultCache();
    @Spy
    @InjectMocks
    protected ResultPromptMatcher resultPromptMatcher = new ResultPromptMatcher();
    @Spy
    @InjectMocks
    protected ResultDefinitionMatcher resultDefinitionMatcher = new ResultDefinitionMatcher();
    @Spy
    @InjectMocks
    protected FindPromptsIndexesByKeyword findPromptsIndexesByKeyword = new FindPromptsIndexesByKeyword();
    @Spy
    @InjectMocks
    protected ComparePromptKeywordsUsingIndexes comparePromptKeywordsUsingIndexes = new ComparePromptKeywordsUsingIndexes();
    @Spy
    @InjectMocks
    protected FindPromptSynonyms findPromptSynonyms = new FindPromptSynonyms();
    @Spy
    @InjectMocks
    protected FindDefinitionsIndexesByKeyword findDefinitionsIndexesByKeyword = new FindDefinitionsIndexesByKeyword();
    @Spy
    @InjectMocks
    protected CompareDefinitionKeywordsUsingIndexes compareDefinitionKeywordsUsingIndexes = new CompareDefinitionKeywordsUsingIndexes();
    @Spy
    @InjectMocks
    protected FindDefinitionPartialMatchSynonyms findDefinitionPartialMatchSynonyms = new FindDefinitionPartialMatchSynonyms();
    @Spy
    @InjectMocks
    protected FindDefinitionsByShortCodes findDefinitionsByShortCodes = new FindDefinitionsByShortCodes();
    @Spy
    @InjectMocks
    protected FindDefinitionExactMatchSynonyms findDefinitionExactMatchSynonyms = new FindDefinitionExactMatchSynonyms();

    @Before
    public void setUp() {
        when(cacheFactory.build()).thenReturn(cache);
        final ConcurrentHashMap<String, Object> cacheValue = new ConcurrentHashMap<>();
        when(cache.asMap()).thenReturn(cacheValue);
        resultCache.lazyLoad(null, now());
    }
}
