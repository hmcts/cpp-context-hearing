package uk.gov.moj.cpp.hearing.domain.aggregate;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import uk.gov.moj.cpp.hearing.command.logEvent.CreateHearingEventDefinitionsCommand;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDefinitionsCreated;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDefinitionsDeleted;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class HearingEventDefinitionAggregateTest {


    private final static String ACTION_LABEL_0 = "Start Hearing";
    private final static String ACTION_LABEL_1 = "Identify defendant";
    private final static String RECORDED_LABEL_0 = "Call Case On";
    private final static String RECORDED_LABEL_1 = "Defendant Identified";
    private final static String SEQUENCE_TYPE = "SENTENCING";

    @InjectMocks
    private HearingEventDefinitionAggregate hearingEventDefinitionAggregate;

    @After
    public void teardown() {
        try {
            // ensure aggregate is serializable
            SerializationUtils.serialize(hearingEventDefinitionAggregate);
        } catch (SerializationException e) {
            fail("Aggregate should be serializable");
        }
    }

    @Test
    public void testHearingEventDefinition() throws Exception {
        final UUID hearingEventDefinitionId = randomUUID();
        final Stream<Object> events = hearingEventDefinitionAggregate.createEventDefinitions(new CreateHearingEventDefinitionsCommand(hearingEventDefinitionId, hearingDefinitions()));
        final List<Object> lEvents = events.collect(Collectors.toList());


        Object event = lEvents.get(0);
        Assert.assertEquals(2, lEvents.size());
        Assert.assertEquals(event.getClass(), HearingEventDefinitionsDeleted.class);
        final HearingEventDefinitionsDeleted hearingEventDefinitionsDeleted = (HearingEventDefinitionsDeleted) event;
        Assert.assertEquals(hearingEventDefinitionsDeleted.getId(), hearingEventDefinitionId);

        event = lEvents.get(1);
        Assert.assertEquals(event.getClass(), HearingEventDefinitionsCreated.class);
        final HearingEventDefinitionsCreated hearingEventDefinitionsCreated = (HearingEventDefinitionsCreated) event;
        Assert.assertEquals(hearingEventDefinitionsCreated.getId(), hearingEventDefinitionId);
        Assert.assertEquals(2, hearingEventDefinitionsCreated.getEventDefinitions().size());

        Assert.assertEquals(hearingEventDefinitionsCreated.getEventDefinitions().get(0).getActionLabel(), hearingDefinitions().get(0).getActionLabel());
        Assert.assertEquals(hearingEventDefinitionsCreated.getEventDefinitions().get(0).getRecordedLabel(), hearingDefinitions().get(0).getRecordedLabel());
        Assert.assertEquals(hearingEventDefinitionsCreated.getEventDefinitions().get(0).getSequenceType(), hearingDefinitions().get(0).getSequenceType());
        Assert.assertEquals(hearingEventDefinitionsCreated.getEventDefinitions().get(0).getSequence(), hearingDefinitions().get(0).getSequence());
        Assert.assertEquals(hearingEventDefinitionsCreated.getEventDefinitions().get(0).isAlterable(), hearingDefinitions().get(0).isAlterable());

        Assert.assertEquals(hearingEventDefinitionsCreated.getEventDefinitions().get(1).getActionLabel(), hearingDefinitions().get(1).getActionLabel());
        Assert.assertEquals(hearingEventDefinitionsCreated.getEventDefinitions().get(1).getRecordedLabel(), hearingDefinitions().get(1).getRecordedLabel());
        Assert.assertEquals(hearingEventDefinitionsCreated.getEventDefinitions().get(1).getSequenceType(), hearingDefinitions().get(1).getSequenceType());
        Assert.assertEquals(hearingEventDefinitionsCreated.getEventDefinitions().get(1).getSequence(), hearingDefinitions().get(1).getSequence());
        Assert.assertEquals(hearingEventDefinitionsCreated.getEventDefinitions().get(1).isAlterable(), hearingDefinitions().get(1).isAlterable());

    }


    private List<HearingEventDefinition> hearingDefinitions() {

        return asList(
                new HearingEventDefinition(randomUUID(), ACTION_LABEL_0, RECORDED_LABEL_0, 1, SEQUENCE_TYPE, null, null, null, false),
                new HearingEventDefinition(randomUUID(), ACTION_LABEL_1, RECORDED_LABEL_1, 2, SEQUENCE_TYPE, null, null, null, true)

        );
    }


}