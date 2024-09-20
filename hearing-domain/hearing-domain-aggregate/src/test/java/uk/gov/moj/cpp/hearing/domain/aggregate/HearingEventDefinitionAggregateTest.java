package uk.gov.moj.cpp.hearing.domain.aggregate;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.fail;

import uk.gov.moj.cpp.hearing.command.logEvent.CreateHearingEventDefinitionsCommand;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDefinitionsCreated;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDefinitionsDeleted;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HearingEventDefinitionAggregateTest {


    private final static String ACTION_LABEL_0 = "Start Hearing";
    private final static String GROUP_LABEL_0 = "RECORDING";
    private final static Integer ACTION_SEQUENCE_0 = 1;
    private final static Integer GROUP_SEQUENCE_0 = 1;
    private final static String ACTION_LABEL_1 = "Identify defendant";
    private final static String GROUP_LABEL_1 = "DEFENDANT";
    private final static Integer ACTION_SEQUENCE_1 = 2;
    private final static Integer GROUP_SEQUENCE_1 = 2;
    private final static String RECORDED_LABEL_0 = "Call Case On";
    private final static String RECORDED_LABEL_1 = "Defendant Identified";

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
        CreateHearingEventDefinitionsCommand createHearingEventDefinitionsCommand = new CreateHearingEventDefinitionsCommand(hearingEventDefinitionId, hearingDefinitions());
        final Stream<Object> events = hearingEventDefinitionAggregate.createEventDefinitions(createHearingEventDefinitionsCommand.getId(), createHearingEventDefinitionsCommand.getEventDefinitions());
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
        Assert.assertEquals(hearingEventDefinitionsCreated.getEventDefinitions().get(0).getActionSequence(), hearingDefinitions().get(0).getActionSequence());
        Assert.assertEquals(hearingEventDefinitionsCreated.getEventDefinitions().get(0).getGroupSequence(), hearingDefinitions().get(0).getGroupSequence());
        Assert.assertEquals(hearingEventDefinitionsCreated.getEventDefinitions().get(0).isAlterable(), hearingDefinitions().get(0).isAlterable());

        Assert.assertEquals(hearingEventDefinitionsCreated.getEventDefinitions().get(1).getActionLabel(), hearingDefinitions().get(1).getActionLabel());
        Assert.assertEquals(hearingEventDefinitionsCreated.getEventDefinitions().get(1).getRecordedLabel(), hearingDefinitions().get(1).getRecordedLabel());
        Assert.assertEquals(hearingEventDefinitionsCreated.getEventDefinitions().get(1).getActionSequence(), hearingDefinitions().get(1).getActionSequence());
        Assert.assertEquals(hearingEventDefinitionsCreated.getEventDefinitions().get(1).getGroupSequence(), hearingDefinitions().get(1).getGroupSequence());
        Assert.assertEquals(hearingEventDefinitionsCreated.getEventDefinitions().get(1).isAlterable(), hearingDefinitions().get(1).isAlterable());

    }


    private List<HearingEventDefinition> hearingDefinitions() {

        return asList(
                new HearingEventDefinition(randomUUID(), ACTION_LABEL_0, ACTION_SEQUENCE_0, RECORDED_LABEL_0, null, GROUP_LABEL_0, GROUP_SEQUENCE_0, false),
                new HearingEventDefinition(randomUUID(), ACTION_LABEL_1, ACTION_SEQUENCE_1, RECORDED_LABEL_1, null, GROUP_LABEL_1, GROUP_SEQUENCE_1, true)

        );
    }


}