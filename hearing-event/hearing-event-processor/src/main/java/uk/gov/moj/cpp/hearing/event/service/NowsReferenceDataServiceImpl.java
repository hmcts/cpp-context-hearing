package uk.gov.moj.cpp.hearing.event.service;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.UUID;

@SuppressWarnings("squid:S00112")
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
    public NowDefinition getNowDefinitionByPrimaryResultDefinitionId(final LocalDate referenceDate, UUID resultDefinitionId) {
        nowsReferenceCache.setContext(context);
        return nowsReferenceCache.getAllNows(referenceDate).getNows()
                .stream()
                .filter(n -> n.getResultDefinitions().stream().anyMatch(rd -> rd.getPrimary() && rd.getId().equals(resultDefinitionId)))
                .findAny()
                .orElse(null);
    }

    @Override
    public NowDefinition getNowDefinitionById(final LocalDate referenceDate, final UUID id) {
        nowsReferenceCache.setContext(context);
        return nowsReferenceCache.getAllNows(referenceDate).getNows()
                .stream()
                .filter(n -> n.getId().equals(id))
                .findAny()
                .orElse(null);
    }

    @Override
    public ResultDefinition getResultDefinitionById(final LocalDate referenceDate, UUID id) {
        nowsReferenceCache.setContext(context);
        return nowsReferenceCache.getResultDefinitionById(referenceDate, id);
    }
}
