package uk.gov.moj.cpp.hearing.event.service;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


@SuppressWarnings("squid:S00112")
@Startup
@ApplicationScoped
public class NowsReferenceCache {

    @Inject
    private NowsReferenceDataLoader nowsReferenceDataLoader;

    private static final Logger LOGGER = LoggerFactory.getLogger(NowsReferenceCache.class);

    private enum Type {
        NOWS, RESULT_DEFINITIONS
    }
    private final LoadingCache<CacheKey, Object> cache = CacheBuilder.newBuilder()
            .refreshAfterWrite(1, TimeUnit.DAYS)
            .expireAfterAccess(4, TimeUnit.HOURS)
            .concurrencyLevel(20)
            .maximumSize(100)
            .build(new CacheLoader<CacheKey, Object>() {
                @Override
                public Object load(CacheKey key) {
                    if (Type.NOWS.equals(key.getType())) {
                        nowsReferenceDataLoader.setContext(context);
                        return nowsReferenceDataLoader.loadAllNowsReference(key.getReferenceDate());
                    } else if (Type.RESULT_DEFINITIONS.equals(key.getType())) {
                        nowsReferenceDataLoader.setContext(context);
                        return nowsReferenceDataLoader.loadAllResultDefinitions(key.getReferenceDate());
                    }
                    return null;
                }
            });

    private JsonEnvelope context;

    public void setContext(JsonEnvelope context) {
        this.context = context;
    }

    public ResultDefinition getResultDefinitionById(LocalDate referenceDate, UUID resultDefinitionId) {
        try {
            final AllResultDefinitions allResultDefinitions = (AllResultDefinitions) cache.get(new CacheKey(Type.RESULT_DEFINITIONS, referenceDate));

            return allResultDefinitions.getResultDefinitions().stream()
                    .filter(rd -> resultDefinitionId.equals(rd.getId()))
                    .findFirst()
                    .orElse(null);
        } catch (ExecutionException executionException) {
            LOGGER.error("getResultDefinitionById reference data service not available", executionException);
            throw new RuntimeException("unrecoverable system error", executionException);
        }
    }

    public AllNows getAllNows(LocalDate referenceDate) {
        try {
            return (AllNows) cache.get(new CacheKey(Type.NOWS, referenceDate));
        } catch (ExecutionException executionException) {
            LOGGER.error("getAllNows reference data service not available", executionException);
            throw new RuntimeException("unrecoverable system error", executionException);
        }
    }

    private class CacheKey {
        private Type type;
        private LocalDate referenceDate;

        private CacheKey(Type type, LocalDate referenceDate) {
            this.type = type;
            this.referenceDate = referenceDate;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final CacheKey cacheKey = (CacheKey) o;
            return Objects.equals(type, cacheKey.type) &&
                    Objects.equals(referenceDate, cacheKey.referenceDate);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, referenceDate);
        }

        public Type getType() {
            return type;
        }

        public LocalDate getReferenceDate() {
            return referenceDate;
        }
    }
}
