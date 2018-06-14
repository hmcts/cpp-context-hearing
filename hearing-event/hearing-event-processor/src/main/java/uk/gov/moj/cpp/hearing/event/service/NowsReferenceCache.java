package uk.gov.moj.cpp.hearing.event.service;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.core.handler.exception.MissingHandlerException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


@Startup
@ApplicationScoped
public class NowsReferenceCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(NowsReferenceCache.class);

    private static final String ALL_NOWS_REFERENCE_KEY = "allNowsKey";
    private static final String ALL_RESULT_DEFINITIONS_KEY = "allResultDefinitionsKey";
    final LoadingCache<String, Object> cache = CacheBuilder
            .newBuilder()
            .concurrencyLevel(20)
            .maximumSize(10)
            .build(new CacheLoader<String, Object>() {
                @Override
                public Object load(String key) {
                    return null;
                }
            });
    private JsonEnvelope context;
    @Inject
    private NowsReferenceDataLoader nowsReferenceDataLoader;

    public void setContext(JsonEnvelope context) {
        this.context = context;
    }

    public void setNowsReferenceDataLoader(NowsReferenceDataLoader nowsReferenceDataLoader) {
        this.nowsReferenceDataLoader = nowsReferenceDataLoader;
    }

    @SuppressWarnings("squid:S00112")
    public ResultDefinition getResultDefinitionById(UUID resultDefinitionId) {
        ResultDefinition result = null;
        try {
            result = getAllResultDefinitions().getResultDefinitions().stream().filter(rd -> resultDefinitionId.equals(rd.getId())).findFirst().orElse(null);
        } catch (ExecutionException executionException) {
            LOGGER.error("getResultDefinitionById reference data service not available", executionException);
            throw new RuntimeException("unrecoverable system error", executionException);
        }
        return result;
    }

    public AllNows getAllNows() throws ExecutionException {
        if (!cache.asMap().containsKey(ALL_NOWS_REFERENCE_KEY)) {
            loadResultCache();
        }
        return (AllNows) cache.get(ALL_NOWS_REFERENCE_KEY);
    }

    public AllResultDefinitions getAllResultDefinitions() throws ExecutionException {
        if (!cache.asMap().containsKey(ALL_RESULT_DEFINITIONS_KEY)) {
            loadResultCache();
        }
        return (AllResultDefinitions) cache.get(ALL_RESULT_DEFINITIONS_KEY);
    }

    private void loadResultCache() {
        AllNows allNowsResult = null;
        nowsReferenceDataLoader.setContext(context);
        try {
            allNowsResult = nowsReferenceDataLoader.loadAllNowsReference(LocalDate.now());
        } catch (MissingHandlerException missingHandler) {
            LOGGER.info("nows reference load failed as expected ", missingHandler);
        }
        cache.asMap().put(ALL_NOWS_REFERENCE_KEY, allNowsResult);
        AllResultDefinitions allResultDefinitionsResult = null;
        try {
            allResultDefinitionsResult = nowsReferenceDataLoader.loadAllResultDefinitions(LocalDate.now());
        } catch (MissingHandlerException missingHandler) {
            LOGGER.info("nows reference load failed unexpectedly ", missingHandler);
        }
        cache.asMap().put(ALL_RESULT_DEFINITIONS_KEY, allResultDefinitionsResult);

    }

    public void reload() {
        LOGGER.info("Reloading cache");
        loadResultCache();
    }
}
