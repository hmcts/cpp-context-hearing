package uk.gov.justice.ccr.notepad.result.cache;

import java.util.concurrent.TimeUnit;

import javax.inject.Named;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Named("ResultCacheFactory")
public class CacheFactory {

    public LoadingCache<String, Object> build() {
        return CacheBuilder
                .newBuilder()
                .expireAfterAccess(3, TimeUnit.DAYS)
                .concurrencyLevel(20)
                .maximumSize(20)
                .build(new CacheLoader<String, Object>() {
                    @Override
                    public Object load(String key) {
                        return null;
                    }
                });
    }
}
