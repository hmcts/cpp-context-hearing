package uk.gov.moj.cpp.hearing.event.service;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.core.handler.exception.MissingHandlerException;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
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

    @Inject
    private NowsReferenceDataLoader nowsReferenceDataLoader;

    public void setNowsReferenceDataLoader(NowsReferenceDataLoader nowsReferenceDataLoader) {
        this.nowsReferenceDataLoader=nowsReferenceDataLoader;
    }

    public ResultDefinition getResultDefinitionById(UUID resultDefinitionId) {
        ResultDefinition result=null;
        try {
            result = nowsReferenceDataLoader.getResultDefinitionById(resultDefinitionId);
        } catch (MissingHandlerException expectedReferenceServiceNotAvailable) {
            LOGGER.error("getResultDefinitionById reference data service not available", expectedReferenceServiceNotAvailable);
            result = (new DefaultNowsReferenceData()).defaultId2ResultDefinition().get(resultDefinitionId);
        }
        return result;
    }

    public AllNows getAllNows() throws ExecutionException {
        loadResultCache();
        return (AllNows) cache.get(ALL_NOWS_REFERENCE_KEY);
    }

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


    private void loadResultCache() {
        AllNows result = null;
        try {
            result = nowsReferenceDataLoader.loadAllNowsReference(LocalDate.now());
        }
        catch (MissingHandlerException missingHandler) {
            LOGGER.info("nows reference load failed as expected ", missingHandler);
            result = (new DefaultNowsReferenceData()).defaultAllNows();
        }
        cache.asMap().put(ALL_NOWS_REFERENCE_KEY, result);
    }

    public void reload()  {
            LOGGER.info("Reloading cache by NowsReferenceMidnightScheduler ");
            loadResultCache();
    }
}
