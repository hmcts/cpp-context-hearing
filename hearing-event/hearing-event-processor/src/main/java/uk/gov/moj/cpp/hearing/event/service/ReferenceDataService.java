package uk.gov.moj.cpp.hearing.event.service;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.time.LocalDate;
import java.util.UUID;

public interface ReferenceDataService {
    NowDefinition getNowDefinitionByPrimaryResultDefinitionId(JsonEnvelope context, LocalDate referenceDate, UUID resultDefinitionId);

    NowDefinition getNowDefinitionById(JsonEnvelope context, LocalDate referenceDate, UUID id);

    ResultDefinition getResultDefinitionById(JsonEnvelope context, LocalDate referenceDate, UUID id);
}
