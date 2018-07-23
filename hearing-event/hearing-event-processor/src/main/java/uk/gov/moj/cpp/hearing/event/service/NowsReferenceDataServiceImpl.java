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

    @Inject
    public NowsReferenceDataServiceImpl(NowsReferenceCache nowsReferenceCache) {
        this.nowsReferenceCache = nowsReferenceCache;
    }

    @Override
    public NowDefinition getNowDefinitionByPrimaryResultDefinitionId(final JsonEnvelope context, final LocalDate referenceDate, UUID resultDefinitionId) {
        return nowsReferenceCache.getAllNows(context, referenceDate).getNows()
                .stream()
                .filter(n -> n.getResultDefinitions().stream().anyMatch(rd -> rd.getPrimary() && rd.getId().equals(resultDefinitionId)))
                .findAny()
                .orElse(null);
    }

    @Override
    public NowDefinition getNowDefinitionById(final JsonEnvelope context, final LocalDate referenceDate, final UUID id) {
        return nowsReferenceCache.getAllNows(context, referenceDate).getNows()
                .stream()
                .filter(n -> n.getId().equals(id))
                .findAny()
                .orElse(null);
    }

    @Override
    public ResultDefinition getResultDefinitionById(final JsonEnvelope context, final LocalDate referenceDate, UUID id) {
        return nowsReferenceCache.getResultDefinitionById(context, referenceDate, id);
    }
}
