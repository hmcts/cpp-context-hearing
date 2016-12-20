package uk.gov.moj.cpp.hearing.event.listener.converter;

import static java.util.Arrays.asList;
import static java.util.UUID.fromString;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDefinitionsCreated;
import uk.gov.moj.cpp.hearing.persist.entity.HearingEventDefinitionEntity;

import java.util.List;

import org.hamcrest.core.Is;
import org.junit.Test;

public class HearingEventDefinitionsConverterTest {

    private HearingEventDefinitionsConverter converter = new HearingEventDefinitionsConverter();

    @Test
    public void convert() throws Exception {
        HearingEventDefinitionsCreated eventDefinitionsCreated = hearingEventDefinitionsCreated();
        List<HearingEventDefinitionEntity> hearingEventDefinitionEntities = converter.convert(eventDefinitionsCreated);

        for (int i = 0; i < eventDefinitionsCreated.getEventDefinitions().size(); i++) {
            HearingEventDefinition eventDefinitionExpected = eventDefinitionsCreated.getEventDefinitions().get(i);

            HearingEventDefinitionEntity eventDefinitionEntityActual = hearingEventDefinitionEntities.get(i);
            assertThat(eventDefinitionEntityActual.getActionLabel(), Is.is(eventDefinitionExpected.getActionLabel()));
            assertThat(eventDefinitionEntityActual.getRecordedLabel(), Is.is(eventDefinitionExpected.getRecordedLabel()));
            assertThat(eventDefinitionEntityActual.getSequenceNumber(), Is.is((i + 1)));
        }
    }

    private HearingEventDefinitionsCreated hearingEventDefinitionsCreated() {
        List<HearingEventDefinition> hearingEventDefinitions = asList(
                new HearingEventDefinition("Identify defendant", "Defendant identified"),
                new HearingEventDefinition("Start Hearing", "Hearing started"));
        return new HearingEventDefinitionsCreated(fromString("4daefec6-5f78-4109-82d9-1e60544a6c02"), hearingEventDefinitions);
    }

}