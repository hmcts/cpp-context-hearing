package uk.gov.moj.cpp.hearing.event.service;

import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.Now;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

public class NowsReferenceDataServiceImpl implements ReferenceDataService {


    private final NowsReferenceCache nowsReferenceCache;

    @Inject
    public NowsReferenceDataServiceImpl(NowsReferenceCache nowsReferenceCache) {
        this.nowsReferenceCache=nowsReferenceCache;
            nowsReferenceCache.reload();
    }



    @Override
    //the underlying service is still under development
    @SuppressWarnings({"squid:S00112"})
    public Now getNowDefinitionByPrimaryResultDefinitionId(UUID resultDefinitionId) {
        AllNows allNows =null;
        try {
            allNows = nowsReferenceCache.getAllNows();
        }
        catch (ExecutionException ex) {
            throw new RuntimeException(ex);
            }
        Predicate<Now> matchNow = n->n.getResultDefinitions().stream().anyMatch(rd->rd.getPrimaryResult()&&rd.getId().equals(resultDefinitionId));
        Optional<Now> result = allNows.getNows().stream().filter(matchNow).findAny();
        return result.orElse(null);
    }

    @Override
    public ResultDefinition getResultDefinitionById(UUID id) {
        return nowsReferenceCache.getResultDefinitionById(id);
    }
}
