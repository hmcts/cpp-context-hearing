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

        assertThat(factory.build(), is(instanceOf(LoadingCache.class)));
    }
}