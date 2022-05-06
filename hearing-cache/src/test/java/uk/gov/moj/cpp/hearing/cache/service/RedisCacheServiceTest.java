package uk.gov.moj.cpp.hearing.cache.service;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.moj.cpp.hearing.cache.CacheDomain;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

@RunWith(MockitoJUnitRunner.class)
public class RedisCacheServiceTest {

    @InjectMocks
    private RedisCacheService redisCacheService;

    @Mock
    private RedisClient redisClient;

    @Mock
    private StatefulRedisConnection statefulRedisConnection;

    @Mock
    private RedisCommands<String, String> redisCommands;

    private void mockRedis() {
        setField(redisCacheService, "redisClient", redisClient);
        setField(redisCacheService, "host", "redisHost");
        setField(redisCacheService, "key", "test_key");
        setField(redisCacheService, "port", "6380");
        setField(redisCacheService, "useSsl", "false");
        setField(redisCacheService, "ttlSeconds", "86400");

        when(redisClient.connect()).thenReturn(statefulRedisConnection);
        when(statefulRedisConnection.sync()).thenReturn(redisCommands);
    }

    @Before
    public void setUp() {
        redisCacheService = new RedisCacheService();
        mockRedis();
    }

    @Test
    public void shouldAddToCacheSuccessfully() {
        // given
        when(redisCommands.set(eq("key1"), eq("value1"), anyObject())).thenReturn("value1");

        // when
        String result = redisCacheService.add("key1", "value1", CacheDomain.RESULT_DEFINITION_ID);

        // then
        verify(redisCommands).sadd(CacheDomain.RESULT_DEFINITION_ID.name(), "key1");
        assertThat(result, is("value1"));
    }

    @Test
    public void shouldGetFromCacheSuccessfully() {
        when(redisCommands.get("key1")).thenReturn("value1");
        assertThat(redisCacheService.get("key1"), is("value1"));
    }

    @Test
    public void shouldRemoveFromCacheSuccessfully() {
        when(redisCommands.get("key1")).thenReturn("value1");
        assertThat(redisCacheService.remove("key1"), is(true));
    }

    @Test
    public void shouldRemoveDomainsFromCacheSuccessfully() {
        // given
        when(redisCommands.smembers(eq(CacheDomain.RESULT_DEFINITION_ID.name()))).thenReturn(newHashSet("key1", "key2"));

        // when
        redisCacheService.removeCacheDomains(CacheDomain.RESULT_DEFINITION_ID);

        // then
        verify(redisCommands).del(CacheDomain.RESULT_DEFINITION_ID.name());
    }
}