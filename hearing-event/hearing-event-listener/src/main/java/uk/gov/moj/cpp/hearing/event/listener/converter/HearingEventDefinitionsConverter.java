package uk.gov.moj.cpp.hearing.event.listener.converter;

import uk.gov.justice.services.common.converter.Converter;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDefinitionsCreated;
import uk.gov.moj.cpp.hearing.persist.entity.HearingEventDefinitionEntity;

import java.util.ArrayList;
import java.util.List;

public class HearingEventDefinitionsConverter implements Converter<HearingEventDefinitionsCreated, List<HearingEventDefinitionEntity>> {

    @Override
    public List<HearingEventDefinitionEntity> convert(HearingEventDefinitionsCreated eventDefinitionsCreated) {
        List<HearingEventDefinitionEntity> entities = new ArrayList<>();
        for (int index = 0; index < eventDefinitionsCreated.getEventDefinitions().size(); index++) {
            HearingEventDefinition hearingEventDefinition = eventDefinitionsCreated.getEventDefinitions().get(index);
            HearingEventDefinitionEntity entity = new HearingEventDefinitionEntity();
            entity.setActionLabel(hearingEventDefinition.getActionLabel());
            entity.setRecordedLabel(hearingEventDefinition.getRecordedLabel());
            entity.setSequenceNumber(index + 1);
            entities.add(entity);
        }
        return entities;
    }
}
