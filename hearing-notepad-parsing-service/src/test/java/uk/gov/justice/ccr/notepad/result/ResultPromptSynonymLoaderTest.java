package uk.gov.justice.ccr.notepad.result;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.ccr.notepad.result.cache.ResultCache;

import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;


public class ResultPromptSynonymLoaderTest {
    ResultCache resultCache = new ResultCache();

    @Before
    public void init() throws ExecutionException {
        resultCache.loadResultCache();
    }
    @Test
    public void getResultPromptSynonyms() throws Exception {
        assertThat(resultCache.getResultPromptSynonym().size(), is(42));
    }

}