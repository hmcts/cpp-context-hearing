package uk.gov.moj.cpp.hearing.event.listener.converter;

import uk.gov.justice.services.common.converter.Converter;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDefinitionsCreated;
import uk.gov.moj.cpp.hearing.persist.entity.HearingEventDefinitions;

import java.util.ArrayList;
import java.util.List;

public class HearingEventDefinitionsConverter implements Converter<HearingEventDefinitionsCreated, List<HearingEventDefinitions>> {

    @Override
    public List<HearingEventDefinitions> convert(final HearingEventDefinitionsCreated eventDefinitionsCreated) {
        final List<HearingEventDefinitions> entities = new ArrayList<>();
        eventDefinitionsCreated.getEventDefinitions().forEach(hearingEventDefinition -> entities.add(
                new HearingEventDefinitions(
                        hearingEventDefinition.getRecordedLabel(),
                        hearingEventDefinition.getActionLabel(),
                        hearingEventDefinition.getSequence(),
                        hearingEventDefinition.getCaseAttribute())
        ));
        return entities;
    }
}
