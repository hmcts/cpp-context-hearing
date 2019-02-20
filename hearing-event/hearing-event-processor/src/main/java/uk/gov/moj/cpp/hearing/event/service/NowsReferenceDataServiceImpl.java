package uk.gov.moj.cpp.hearing.event.service;

import uk.gov.justice.core.courts.LjaDetails;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

@SuppressWarnings("squid:S00112")
public class NowsReferenceDataServiceImpl implements ReferenceDataService {

    private final NowsReferenceCache nowsReferenceCache;

    private final LjaReferenceDataLoader ljaReferenceDataLoader;

    @Inject
    public NowsReferenceDataServiceImpl(final NowsReferenceCache nowsReferenceCache, final LjaReferenceDataLoader ljaReferenceDataLoader) {
        this.nowsReferenceCache = nowsReferenceCache;
        this.ljaReferenceDataLoader = ljaReferenceDataLoader;
    }

    @Override
    public LjaDetails getLjaDetailsByCourtCentreId(JsonEnvelope context, UUID courtCentreId) {
        return ljaReferenceDataLoader.getLjaDetailsByCourtCentreId(context, courtCentreId);
    }

    @Override
    public Set<NowDefinition> getNowDefinitionByPrimaryResultDefinitionId(final JsonEnvelope context, final LocalDate referenceDate, UUID resultDefinitionId) {

        return nowsReferenceCache.getAllNows(context, referenceDate).getNows()
                .stream()
                .filter(n -> n.getResultDefinitions().stream().anyMatch(rd -> rd.getPrimary() && rd.getId().equals(resultDefinitionId)))
                .collect(Collectors.toSet());
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
