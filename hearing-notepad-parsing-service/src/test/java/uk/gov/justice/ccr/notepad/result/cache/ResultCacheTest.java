package uk.gov.justice.ccr.notepad.result.cache;

import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.ccr.notepad.result.loader.ReadStoreResultLoader;
import uk.gov.justice.ccr.notepad.result.loader.ResultLoader;

import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheLoader;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ResultCacheTest {

    ResultCache testObj = new ResultCache();


    @Before
    public void setup() throws ExecutionException {
        testObj.reloadCache(true,null);
    }

    @Test
    public void getResultLoader() throws Exception {

        ResultLoader resultLoader = testObj.getResultLoader();

        assertThat(resultLoader instanceof ReadStoreResultLoader, Matchers.is(true));

    }

    @Test(expected = CacheLoader.InvalidCacheLoadException.class)
    public void getResultLoaderWithKeyNotFound() throws Exception {
        ResultCache testObj = new ResultCache();

        testObj.cache.get("UNKNOWN");

    }

    @After
    public void tearDown() throws ExecutionException {
        testObj.reloadCache(false,null);
    }

}