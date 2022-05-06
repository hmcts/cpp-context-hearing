package uk.gov.moj.cpp.hearing.cache.service;

import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.common.configuration.Value;
import uk.gov.moj.cpp.hearing.cache.CacheDomain;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.util.Arrays.stream;

@Stateless
public class RedisCacheService implements CacheService {

    private static final Duration CONNECT_TIMEOUT_ONE_SEC = Duration.ofSeconds(1);
    private static final String LOCALHOST = "localhost";
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCacheService.class);
    private static final String DB_NAME = "7";

    @Inject
    @Value(key = "redisCommonCacheHost", defaultValue = "localhost")
    private String host;

    @Inject
    @Value(key = "redisCommonCacheKey", defaultValue = "none")
    private String key;

    @Inject
    @Value(key = "redisCommonCachePort", defaultValue = "6380")
    private String port;

    @Inject
    @Value(key = "redisCommonCacheUseSsl", defaultValue = "false")
    private String useSsl;

    @Inject
    @Value(key = "redisCommonCacheKeyTTL", defaultValue = "86400")
    private String ttlSeconds;

    private RedisClient redisClient = null;

    @Override
    public String add(String key, String value, CacheDomain... cacheDomains) {
        if (LOCALHOST.equals(host)) {
            return null;
        }
        if (redisClient == null) {
            LOGGER.info("Inside CacheService.add() - redisClient not initialised; invoking setRedisClient");
            setRedisClient();
        }
        return executeAddCommand(key, value, cacheDomains);
    }

    @Override
    public String get(String key) {
        if (LOCALHOST.equals(host)) {
            return null;
        }
        if (redisClient == null) {
            LOGGER.info("Inside CacheService.get() - redisClient not initialised; invoking setRedisClient");
            setRedisClient();
        }
        return executeGetCommand(key);
    }

    @Override
    public String flushAllCacheKeys() {
        if (LOCALHOST.equals(host)) {
            return null;
        }
        if (redisClient == null) {
            LOGGER.info("Inside CacheService.flushAllCacheKeys() - redisClient not initialised; invoking setRedisClient");
            setRedisClient();
        }
        return executeFlushAllCommand();
    }

    @Override
    public boolean remove(String key) {
        if (LOCALHOST.equals(host)) {
            return true;
        }
        if (redisClient == null) {
            LOGGER.info("Inside CacheService.remove() - redisClient not initialised; invoking setRedisClient");
            setRedisClient();
        }
        return executeRemoveCommand(key);
    }

    @Override
    public boolean removeCacheDomains(CacheDomain... cacheDomains) {
        if (LOCALHOST.equals(host)) {
            return true;
        }
        if (redisClient == null) {
            LOGGER.info("Inside CacheService.remove() - redisClient not initialised; invoking setRedisClient");
            setRedisClient();
        }
        return executeRemoveCommand(cacheDomains);
    }

    @Override
    public boolean smokeTest() {
        String smokeTestKey = "SMOKE_TEST_KEY";
        String smokeTestValue = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        if (redisClient == null) {
            LOGGER.info("Inside CacheService.smokeTest() - redisClient not initialised; invoking setRedisClient");
            setRedisClient();
        }

        try (final StatefulRedisConnection<String, String> connection = this.redisClient.connect()) {

            final RedisCommands<String, String> command = connection.sync();
            final SetArgs args = new SetArgs().ex(Integer.parseInt(ttlSeconds));

            LOGGER.info("Cache SMOKE_TEST SET:KEY:{}, VALUE:{}", smokeTestKey, smokeTestValue);
            command.set(smokeTestKey, smokeTestValue, args);

            String retrievedCacheValue = command.get(smokeTestKey);
            LOGGER.info("Cache SMOKE_TEST GET:KEY:{}, VALUE:{}", smokeTestKey, retrievedCacheValue);

            return smokeTestValue.equals(retrievedCacheValue);
        } catch (RedisConnectionException ex) {
            LOGGER.warn("Exception in RedisCache SmokeTest() - {} ", ex.getMessage(), ex);
            return false;
        }
    }

    private void setRedisClient() {
        LOGGER.info("Redis host : {}", host);
        final String keyPart = ("none".equals(this.key) ? "" : this.key + "@");
        final RedisURI redisURI = RedisURI.create("redis://" + keyPart + host + ":" + port + "/" + DB_NAME);
        redisURI.setSsl(Boolean.parseBoolean(useSsl));

        redisClient = RedisClient.create(redisURI);

        final SocketOptions socketOptions =
                SocketOptions.builder()
                        .connectTimeout(CONNECT_TIMEOUT_ONE_SEC).build();

        final ClientOptions clientOptions =
                ClientOptions.builder().socketOptions(socketOptions).build();
        this.redisClient.setOptions(clientOptions);
    }

    private String executeAddCommand(final String key, final String value, CacheDomain... cacheDomains) {

        try (final StatefulRedisConnection<String, String> connection = this.redisClient.connect()) {
            final RedisCommands<String, String> command = connection.sync();
            final SetArgs args = new SetArgs().ex(Integer.parseInt(ttlSeconds));
            stream(cacheDomains).forEach(cacheDomain -> {
                LOGGER.info("Cache S_ADD:Key:{}, Value:{}", cacheDomain.name(), key);
                command.sadd(cacheDomain.name(), key);
            });
            LOGGER.info("Cache SET:Key:{}, Value:{}", key, value);
            return command.set(key, value, args);

        } catch (RedisConnectionException ex) {
            LOGGER.warn("Exception in RedisCache executeAddCommand() - {} ", ex.getMessage(), ex);
            return null;
        }
    }

    private String executeGetCommand(final String key) {
        try (final StatefulRedisConnection<String, String> connection = this.redisClient.connect()) {
            final RedisCommands<String, String> command = connection.sync();
            LOGGER.info("Cache GET:Key:{}", key);
            return command.get(key);
        } catch (RedisConnectionException ex) {
            LOGGER.warn("Exception in RedisCache executeGetCommand() - {} ", ex.getMessage(), ex);
            return null;
        }
    }

    private boolean executeRemoveCommand(final String key) {
        try (final StatefulRedisConnection<String, String> connection = this.redisClient.connect()) {
            final RedisCommands<String, String> command = connection.sync();
            LOGGER.info("Cache DEL:Key:{}", key);
            command.del(key);
            return true;
        } catch (RedisConnectionException ex) {
            LOGGER.warn("Exception in RedisCache executeRemoveCommand() - {} ", ex.getMessage(), ex);
            return false;
        }
    }


    private boolean executeRemoveCommand(final CacheDomain... cacheDomains) {
        try (final StatefulRedisConnection<String, String> connection = this.redisClient.connect()) {
            final RedisCommands<String, String> command = connection.sync();
            stream(cacheDomains).forEach(cacheDomain -> {
                // remove all keys associated with the domain
                command.smembers(cacheDomain.name()).forEach(command::del);
                command.del(cacheDomain.name());
            });
            return true;
        } catch (RedisConnectionException ex) {
            LOGGER.warn("Exception in RedisCache executeRemoveCommand() - {} ", ex.getMessage(), ex);
            return false;
        }
    }

    private String executeFlushAllCommand() {
        try (final StatefulRedisConnection<String, String> connection = this.redisClient.connect()) {
            final RedisCommands<String, String> command = connection.sync();
            LOGGER.info("Cache FLUSHDB");
            return command.flushdb();
        } catch (RedisConnectionException ex) {
            LOGGER.warn("Exception in RedisCache executeFlushAllCommand() - {} ", ex.getMessage(), ex);
            return null;
        }
    }

}
