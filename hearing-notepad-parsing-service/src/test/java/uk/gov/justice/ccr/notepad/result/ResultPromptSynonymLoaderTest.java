package uk.gov.justice.ccr.notepad.result;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.ccr.notepad.result.cache.ResultCache;
import uk.gov.justice.ccr.notepad.result.loader.FileResultLoader;

import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;


public class ResultPromptSynonymLoaderTest {
    ResultCache resultCache = new ResultCache();
    FileResultLoader fileResultLoader = new FileResultLoader();

    @Before
    public void init() throws ExecutionException {
        resultCache.setResultLoader(fileResultLoader);
        resultCache.lazyLoad(null);
    }
    @Test
    public void getResultPromptSynonyms() throws Exception {
        assertThat(resultCache.getResultPromptSynonym().size(), is(42));
    }

}