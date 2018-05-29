package uk.gov.moj.cpp.hearing.event.service;

import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.Now;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.util.UUID;

public interface ReferenceDataService {
   Now getNowDefinitionByPrimaryResultDefinitionId(UUID resultDefinitionId);
   ResultDefinition getResultDefinitionById(UUID id);
}
