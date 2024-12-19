package uk.gov.moj.cpp.hearing.steps.data.factory;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;

import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.steps.data.HearingEventDefinitionData;

import java.util.List;

public class HearingEventDataFactory {

    private static HearingEventDefinition PAUSE_HEARING_EVENT_DEFINITION;
    private static HearingEventDefinition RESUME_HEARING_EVENT_DEFINITION;

    private static List<HearingEventDefinition> HEARING_EVENT_DEFINITIONS;

    private static final String GROUP_LABEL_1 = "Recording1";
    private static final Integer GROUP_SEQUENCE_1 = 1;

    private static final String ID_3 = "160ecb51-29ee-4954-bbbf-daab18a24fbb";
    private static final String RECORDED_LABEL_3 = "Hearing Paused-3";
    private static final String ACTION_LABEL_3 = "Pause-3";
    private static final Integer ACTION_SEQUENCE_3 = 3;

    private static final String ID_4 = "64476e43-2138-46d5-b58b-848582cf9b07";
    private static final String RECORDED_LABEL_4 = "Hearing Resumed-4";
    private static final String ACTION_LABEL_4 = "Resume-4";
    private static final Integer ACTION_SEQUENCE_4 = 4;

    private static final boolean ALTERABLE = BOOLEAN.next();

    public static HearingEventDefinitionData hearingEventDefinitionsWithPauseAndResumeEvents() {
        PAUSE_HEARING_EVENT_DEFINITION = new HearingEventDefinition(java.util.UUID.fromString(ID_3), ACTION_LABEL_3, ACTION_SEQUENCE_3, RECORDED_LABEL_3, null, GROUP_LABEL_1, GROUP_SEQUENCE_1, ALTERABLE);
        RESUME_HEARING_EVENT_DEFINITION = new HearingEventDefinition(java.util.UUID.fromString(ID_4), ACTION_LABEL_4, ACTION_SEQUENCE_4, RECORDED_LABEL_4, null, GROUP_LABEL_1, GROUP_SEQUENCE_1, ALTERABLE);

        final List<HearingEventDefinition> eventDefinitions = newArrayList();
        eventDefinitions.add(PAUSE_HEARING_EVENT_DEFINITION);
        eventDefinitions.add(RESUME_HEARING_EVENT_DEFINITION);

        HEARING_EVENT_DEFINITIONS = newArrayList();
        HEARING_EVENT_DEFINITIONS.addAll(eventDefinitions);

        return new HearingEventDefinitionData(randomUUID(), eventDefinitions);
    }

}
