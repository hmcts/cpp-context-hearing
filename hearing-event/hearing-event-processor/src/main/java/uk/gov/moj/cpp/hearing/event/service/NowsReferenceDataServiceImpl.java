package uk.gov.moj.cpp.hearing.event.service;

import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

public class NowsReferenceDataServiceImpl implements ReferenceDataService {


    private final NowsReferenceCache nowsReferenceCache;

    @Inject
    public NowsReferenceDataServiceImpl(NowsReferenceCache nowsReferenceCache) {
        this.nowsReferenceCache = nowsReferenceCache;
        nowsReferenceCache.reload();
    }


    @Override
    //the underlying service is still under development
    @SuppressWarnings({"squid:S00112"})
    public NowDefinition getNowDefinitionByPrimaryResultDefinitionId(UUID resultDefinitionId) {
        AllNows allNows;
        try {
            allNows = nowsReferenceCache.getAllNows();
        } catch (ExecutionException ex) {
            throw new RuntimeException(ex);
        }
        return allNows.getNows().stream().filter(matchNow(resultDefinitionId)).findAny().orElse(null);
    }

    private static Predicate<NowDefinition> matchNow(UUID resultDefinitionId) {
        return n -> n.getResultDefinitions().stream().anyMatch(rd -> rd.getPrimaryResult() && rd.getId().equals(resultDefinitionId));
    }

    @Override
    public ResultDefinition getResultDefinitionById(UUID id) {
        return nowsReferenceCache.getResultDefinitionById(id);
    }
}
