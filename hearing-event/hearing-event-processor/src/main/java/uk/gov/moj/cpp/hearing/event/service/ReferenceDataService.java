package uk.gov.moj.cpp.hearing.event.service;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.util.UUID;

public interface ReferenceDataService {
    NowDefinition getNowDefinitionByPrimaryResultDefinitionId(UUID resultDefinitionId);

    NowDefinition getNowDefinitionById(UUID id);

    ResultDefinition getResultDefinitionById(UUID id);

    void setContext(JsonEnvelope jsonEnvelope);
}
