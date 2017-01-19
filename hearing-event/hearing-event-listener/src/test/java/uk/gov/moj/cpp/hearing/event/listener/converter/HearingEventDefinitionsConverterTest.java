package uk.gov.moj.cpp.hearing.event.listener.converter;

import static java.util.Arrays.asList;
import static java.util.UUID.fromString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDefinitionsCreated;
import uk.gov.moj.cpp.hearing.persist.entity.HearingEventDefinitionEntity;

import java.util.List;

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
            assertThat(eventDefinitionEntityActual.getActionLabel(), is(eventDefinitionExpected.getActionLabel()));
            assertThat(eventDefinitionEntityActual.getRecordedLabel(), is(eventDefinitionExpected.getRecordedLabel()));
            assertThat(eventDefinitionEntityActual.getSequenceNumber(), is((eventDefinitionExpected.getSequence())));
            assertThat(eventDefinitionEntityActual.getCaseAttribute(), is((eventDefinitionExpected.getCaseAttribute())));
        }
    }

    private HearingEventDefinitionsCreated hearingEventDefinitionsCreated() {
        List<HearingEventDefinition> hearingEventDefinitions = asList(
                new HearingEventDefinition("Identify defendant", "Defendant identified", 1, "defendant.name"),
                new HearingEventDefinition("Start Hearing", "Hearing started", 2, null));
        return new HearingEventDefinitionsCreated(fromString("4daefec6-5f78-4109-82d9-1e60544a6c02"), hearingEventDefinitions);
    }

}