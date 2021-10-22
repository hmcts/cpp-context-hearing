package uk.gov.justice.ccr.notepad.result.cache;

import uk.gov.justice.services.common.configuration.Value;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Named("ResultCacheFactory")
public class CacheFactory {

    public static final String ZERO = "0";

    /**
     * Cache will expire in specified days
     */
    @Inject
    @Value(key = "cacheExpiryInDays", defaultValue = "3")
    String cacheExpiryInDays;

    /**
     * Number of thread that can modify the cache concurrently
     */
    @Inject
    @Value(key = "cacheConcurrencyLevel", defaultValue = "20")
    String cacheConcurrencyLevel;

    /**
     * Maximum number of entries the cache may contain
     */
    @Inject
    @Value(key = "cacheMaxSize", defaultValue = "20")
    String cacheMaxSize;

    public LoadingCache<String, Object> build() {

        CacheBuilder cacheBuilder =  CacheBuilder
                .newBuilder()
                .expireAfterAccess(Long.parseLong(cacheExpiryInDays), TimeUnit.DAYS)
                .concurrencyLevel(Integer.parseInt(cacheConcurrencyLevel));

        if (this.cacheMaxSize != null && !this.cacheMaxSize.equals(ZERO)){
            cacheBuilder.maximumSize(Long.parseLong(cacheMaxSize));
        }

        return cacheBuilder.build(new CacheLoader<String, Object>() {
                    @Override
                    public Object load(String key) {
                        return null;
                    }
                });
    }
}
