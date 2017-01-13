package uk.gov.moj.cpp.hearing.event.listener.converter;

import uk.gov.justice.services.common.converter.Converter;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDefinitionsCreated;
import uk.gov.moj.cpp.hearing.persist.entity.HearingEventDefinitionEntity;

import java.util.ArrayList;
import java.util.List;

public class HearingEventDefinitionsConverter implements Converter<HearingEventDefinitionsCreated, List<HearingEventDefinitionEntity>> {

    @Override
    public List<HearingEventDefinitionEntity> convert(final HearingEventDefinitionsCreated eventDefinitionsCreated) {
        final List<HearingEventDefinitionEntity> entities = new ArrayList<>();
        eventDefinitionsCreated.getEventDefinitions().forEach(hearingEventDefinition -> entities.add(
                new HearingEventDefinitionEntity(
                        hearingEventDefinition.getRecordedLabel(),
                        hearingEventDefinition.getActionLabel(),
                        hearingEventDefinition.getSequence(),
                        hearingEventDefinition.getCaseAttribute())
        ));
        return entities;
    }
}
