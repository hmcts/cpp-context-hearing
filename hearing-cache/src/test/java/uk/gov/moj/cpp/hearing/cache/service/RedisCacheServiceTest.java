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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    public void shouldNotAddToCacheWhenHostIsLocalHost() {
        //given
        setField(redisCacheService, "host", "localhost");
        // when
        String result = redisCacheService.add("key1", "value1", CacheDomain.RESULT_DEFINITION_ID);

        // then
        assertThat(result, is(nullValue()));
    }

    @Test
    public void shouldAddToCacheSuccessfullyWhenRedisClientIsNullBySettingUpRedisClient() {
        setField(redisCacheService, "redisClient", null);

        // given
        when(redisCommands.set(eq("key1"), eq("value1"), anyObject())).thenReturn("value1");

        // when
        String result = redisCacheService.add("key1", "value1", CacheDomain.RESULT_DEFINITION_ID);

        // then
        assertThat(result, is(nullValue()));
    }

    @Test
    public void shouldGetFromCacheSuccessfully() {
        when(redisCommands.get("key1")).thenReturn("value1");
        assertThat(redisCacheService.get("key1"), is("value1"));
    }

    @Test
    public void shouldNotGetFromCacheWhenHostIsLocalHost() {
        setField(redisCacheService, "host", "localhost");
        String value = redisCacheService.get("key1");
        assertThat(value, is(nullValue()));
    }

    @Test
    public void shouldNotGetFromCacheWhenRedisClientIsNull() {
        setField(redisCacheService, "redisClient", null);
        String value = redisCacheService.get("key1");
        assertThat(value, is(nullValue()));
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

    @Test
    public void shouldFlushAllCacheKeys() {
        // when
        String result = redisCacheService.flushAllCacheKeys();
        assertThat(redisCacheService.get("key1"), is(nullValue()));
    }

    @Test
    public void shouldNotFlushAllCacheKeysWhenHostIsLocalHost() {
        setField(redisCacheService, "host", "localhost");
        String result = redisCacheService.flushAllCacheKeys();
        assertThat(result, is(nullValue()));
    }

    @Test
    public void shouldNotFlushAllCacheKeysWhenRedisClientIsNull() {
        setField(redisCacheService, "redisClient", null);
        String result = redisCacheService.flushAllCacheKeys();
        assertThat(result, is(nullValue()));
    }

    @Test
    public void shouldNotRemoveFromCacheWhenHostIsLocalHost() {
        setField(redisCacheService, "host", "localhost");
        assertThat(redisCacheService.remove("key1"), is(true));
    }

    @Test
    public void shouldNotRemoveFromCacheWhenRedisClientIsNull() {
        setField(redisCacheService, "redisClient", null);
        assertThat(redisCacheService.remove("key1"), is(false));
    }

    @Test
    public void shouldNotRemoveDomainsFromCacheWhenHostIsLocalHost()  {
        setField(redisCacheService, "host", "localhost");
        final boolean result = redisCacheService.removeCacheDomains(CacheDomain.RESULT_DEFINITION_ID);
        assertThat(result, is(true));
    }

    @Test
    public void shouldNotRemoveDomainsFromCacheWhenRedisClientIsNull()  {
        setField(redisCacheService, "redisClient", null);
        final boolean result = redisCacheService.removeCacheDomains(CacheDomain.RESULT_DEFINITION_ID);
        assertThat(result, is(false));
    }

    @Test
    public void shouldDoSmokeTestSuccessfully(){
        when(redisCommands.get("SMOKE_TEST_KEY")).thenReturn(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        final boolean result = redisCacheService.smokeTest();
        assertThat(result,is(notNullValue()));
    }

    @Test
    public void shouldNotDoSmokeTestSuccessfullyWhenRedisClientIsNull(){
        setField(redisCacheService, "redisClient", null);
        final boolean result = redisCacheService.smokeTest();
        assertThat(result,is(false));
    }
}