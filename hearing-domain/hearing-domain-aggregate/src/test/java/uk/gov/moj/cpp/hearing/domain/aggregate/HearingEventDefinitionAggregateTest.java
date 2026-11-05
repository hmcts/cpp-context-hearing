package uk.gov.moj.cpp.hearing.domain.aggregate;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.fail;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
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

    @AfterEach
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
        Assertions.assertEquals(2, lEvents.size());
        Assertions.assertEquals(event.getClass(), HearingEventDefinitionsDeleted.class);
        final HearingEventDefinitionsDeleted hearingEventDefinitionsDeleted = (HearingEventDefinitionsDeleted) event;
        Assertions.assertEquals(hearingEventDefinitionsDeleted.getId(), hearingEventDefinitionId);

        event = lEvents.get(1);
        Assertions.assertEquals(event.getClass(), HearingEventDefinitionsCreated.class);
        final HearingEventDefinitionsCreated hearingEventDefinitionsCreated = (HearingEventDefinitionsCreated) event;
        Assertions.assertEquals(hearingEventDefinitionsCreated.getId(), hearingEventDefinitionId);
        Assertions.assertEquals(2, hearingEventDefinitionsCreated.getEventDefinitions().size());

        Assertions.assertEquals(hearingEventDefinitionsCreated.getEventDefinitions().get(0).getActionLabel(), hearingDefinitions().get(0).getActionLabel());
        Assertions.assertEquals(hearingEventDefinitionsCreated.getEventDefinitions().get(0).getRecordedLabel(), hearingDefinitions().get(0).getRecordedLabel());
        Assertions.assertEquals(hearingEventDefinitionsCreated.getEventDefinitions().get(0).getActionSequence(), hearingDefinitions().get(0).getActionSequence());
        Assertions.assertEquals(hearingEventDefinitionsCreated.getEventDefinitions().get(0).getGroupSequence(), hearingDefinitions().get(0).getGroupSequence());
        Assertions.assertEquals(hearingEventDefinitionsCreated.getEventDefinitions().get(0).isAlterable(), hearingDefinitions().get(0).isAlterable());

        Assertions.assertEquals(hearingEventDefinitionsCreated.getEventDefinitions().get(1).getActionLabel(), hearingDefinitions().get(1).getActionLabel());
        Assertions.assertEquals(hearingEventDefinitionsCreated.getEventDefinitions().get(1).getRecordedLabel(), hearingDefinitions().get(1).getRecordedLabel());
        Assertions.assertEquals(hearingEventDefinitionsCreated.getEventDefinitions().get(1).getActionSequence(), hearingDefinitions().get(1).getActionSequence());
        Assertions.assertEquals(hearingEventDefinitionsCreated.getEventDefinitions().get(1).getGroupSequence(), hearingDefinitions().get(1).getGroupSequence());
        Assertions.assertEquals(hearingEventDefinitionsCreated.getEventDefinitions().get(1).isAlterable(), hearingDefinitions().get(1).isAlterable());

    }


    private List<HearingEventDefinition> hearingDefinitions() {

        return asList(
                new HearingEventDefinition(randomUUID(), ACTION_LABEL_0, ACTION_SEQUENCE_0, RECORDED_LABEL_0, null, GROUP_LABEL_0, GROUP_SEQUENCE_0, false),
                new HearingEventDefinition(randomUUID(), ACTION_LABEL_1, ACTION_SEQUENCE_1, RECORDED_LABEL_1, null, GROUP_LABEL_1, GROUP_SEQUENCE_1, true)

        );
    }


}