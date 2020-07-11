package uk.gov.moj.cpp.hearing.event.service;


import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllFixedList;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S00112", "squid:S2139"})
@Startup
@ApplicationScoped
public class NowsReferenceCache {

    protected static final String UNRECOVERABLE_SYSTEM_ERROR = "unrecoverable system error";
    private static final Logger LOGGER = LoggerFactory.getLogger(NowsReferenceCache.class);
    private final ThreadLocal<JsonEnvelope> context = new ThreadLocal<>();
    @Inject
    private NowsReferenceDataLoader nowsReferenceDataLoader;
    private final LoadingCache<CacheKey, Object> cache = CacheBuilder.newBuilder()
            .refreshAfterWrite(1, TimeUnit.DAYS)
            .expireAfterAccess(4, TimeUnit.HOURS)
            .concurrencyLevel(20)
            .maximumSize(100)
            .build(new CacheLoader<CacheKey, Object>() {
                @Override
                public Object load(final CacheKey key) {
                    if (Type.RESULT_DEFINITIONS.equals(key.getType())) {
                        return nowsReferenceDataLoader.loadAllResultDefinitionAsTree(context.get(), key.getReferenceDate());
                    } else if (Type.FIXED_LIST.equals(key.getType())) {
                        return nowsReferenceDataLoader.loadAllFixedList(context.get(), key.getReferenceDate());
                    }
                    return null;
                }
            });

    public TreeNode<ResultDefinition> getResultDefinitionById(final JsonEnvelope context, final LocalDate referenceDate, final UUID resultDefinitionId) {
        try {
            this.context.set(context);
            final Map<UUID, TreeNode<ResultDefinition>> allResultDefinitions = (Map<UUID, TreeNode<ResultDefinition>>) cache.get(new CacheKey(Type.RESULT_DEFINITIONS, referenceDate));

            return allResultDefinitions.get(resultDefinitionId);
        } catch (final ExecutionException executionException) {
            LOGGER.error("getResultDefinitionById reference data service not available", executionException);
            throw new RuntimeException(UNRECOVERABLE_SYSTEM_ERROR, executionException);
        } finally {
            this.context.remove();
        }
    }

    public AllFixedList getAllFixedList(final JsonEnvelope context, final LocalDate referenceDate) {
        this.context.set(context);
        try {
            return (AllFixedList) cache.get(new CacheKey(Type.FIXED_LIST, referenceDate));
        } catch (final ExecutionException executionException) {
            LOGGER.error("getAllFixedList reference data service not available", executionException);
            throw new RuntimeException(UNRECOVERABLE_SYSTEM_ERROR, executionException);
        } finally {
            this.context.remove();
        }
    }

    private enum Type {
        RESULT_DEFINITIONS, FIXED_LIST
    }

    private static class CacheKey {
        private final Type type;
        private final LocalDate referenceDate;

        private CacheKey(final Type type, final LocalDate referenceDate) {
            this.type = type;
            this.referenceDate = referenceDate;
        }

        @Override
        public boolean equals(final Object o) {
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
