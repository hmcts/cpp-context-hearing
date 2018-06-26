package uk.gov.moj.cpp.hearing.event.service;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

public class NowsReferenceDataServiceImpl implements ReferenceDataService {


    private final NowsReferenceCache nowsReferenceCache;
    private JsonEnvelope context;

    @Inject
    public NowsReferenceDataServiceImpl(NowsReferenceCache nowsReferenceCache) {
        this.nowsReferenceCache = nowsReferenceCache;
    }

    public void setContext(JsonEnvelope context) {
        this.context = context;
    }

    @Override
    //the underlying service is still under development
    @SuppressWarnings({"squid:S00112"})
    public NowDefinition getNowDefinitionByPrimaryResultDefinitionId(UUID resultDefinitionId) {
        AllNows allNows = null;
        try {
            nowsReferenceCache.setContext(context);
            allNows = nowsReferenceCache.getAllNows();
        } catch (ExecutionException ex) {
            throw new RuntimeException(ex);
        }
        final Predicate<NowDefinition> matchNow = n -> n.getResultDefinitions().stream().anyMatch(rd -> rd.getPrimary() && rd.getId().equals(resultDefinitionId));
        final Optional<NowDefinition> result = allNows.getNows().stream().filter(matchNow).findAny();
        return result.orElse(null);
    }

    @Override
    public ResultDefinition getResultDefinitionById(UUID id) {
        nowsReferenceCache.setContext(context);
        return nowsReferenceCache.getResultDefinitionById(id);
    }
}
