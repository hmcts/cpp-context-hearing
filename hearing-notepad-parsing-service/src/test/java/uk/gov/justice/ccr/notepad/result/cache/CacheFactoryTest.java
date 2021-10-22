package uk.gov.justice.ccr.notepad.result.cache;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import com.google.common.cache.LoadingCache;
import org.junit.Test;

public class CacheFactoryTest {

    @Test
    public void shouldReturnAnInstanceOfTheCache() {
        final CacheFactory factory = new CacheFactory();
        factory.cacheConcurrencyLevel = "120";
        factory.cacheExpiryInDays = "3";
        factory.cacheMaxSize = "20";
        assertThat(factory.build(), is(instanceOf(LoadingCache.class)));
    }

    @Test
    public void shouldReturnAnInstanceOfTheCacheWithZeroCacheSize() {
        final CacheFactory factory = new CacheFactory();
        factory.cacheConcurrencyLevel = "120";
        factory.cacheExpiryInDays = "3";
        factory.cacheMaxSize = "0";
        assertThat(factory.build(), is(instanceOf(LoadingCache.class)));
    }
}