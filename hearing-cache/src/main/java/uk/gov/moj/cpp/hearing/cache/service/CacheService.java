package uk.gov.moj.cpp.hearing.cache.service;


import uk.gov.moj.cpp.hearing.cache.CacheDomain;

public interface CacheService {
    String add(String key, String value, CacheDomain... cacheDomains);

    String get(String key);

    String flushAllCacheKeys();

    boolean remove(String key);

    boolean removeCacheDomains(CacheDomain... cacheDomains);

    boolean smokeTest();
}