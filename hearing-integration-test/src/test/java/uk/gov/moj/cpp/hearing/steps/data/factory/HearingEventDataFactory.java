package uk.gov.moj.cpp.hearing.steps.data.factory;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.values;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent;
import uk.gov.moj.cpp.hearing.steps.data.HearingEventDefinitionData;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

public class HearingEventDataFactory {

    private static HearingEventDefinition START_HEARING_EVENT_DEFINITION;
    private static HearingEventDefinition END_HEARING_EVENT_DEFINITION;
    private static HearingEventDefinition PAUSE_HEARING_EVENT_DEFINITION;
    private static HearingEventDefinition RESUME_HEARING_EVENT_DEFINITION;
    private static HearingEventDefinition IDENTIFY_DEFENDANT_HEARING_EVENT_DEFINITION;
    private static HearingEventDefinition MITIGATION_HEARING_EVENT_DEFINITION;

    private static List<HearingEventDefinition> HEARING_EVENT_DEFINITIONS;

    private static final String ID_1 = "b71e7d2a-d3b3-4a55-a393-6d451767fc05";
    private static final String RECORDED_LABEL_1 = "Hearing Started-1";
    private static final String ACTION_LABEL_1 = "Start-1";
    private static final Integer ACTION_SEQUENCE_1 = 1;
    private static final String GROUP_LABEL_1 = "Recording1";
    private static final Integer GROUP_SEQUENCE_1 = 1;

    private static final String ID_2 = "0df93f18-0a21-40f5-9fb3-da4749cd70fe";
    private static final String RECORDED_LABEL_2 = "Hearing Ended-1";
    private static final String ACTION_LABEL_2 = "End-2";
    private static final Integer ACTION_SEQUENCE_2 = 2;

    private static final String ID_3 = "160ecb51-29ee-4954-bbbf-daab18a24fbb";
    private static final String RECORDED_LABEL_3 = "Hearing Paused-3";
    private static final String ACTION_LABEL_3 = "Pause-3";
    private static final Integer ACTION_SEQUENCE_3 = 3;

    private static final String ID_4 = "64476e43-2138-46d5-b58b-848582cf9b07";
    private static final String RECORDED_LABEL_4 = "Hearing Resumed-4";
    private static final String ACTION_LABEL_4 = "Resume-4";
    private static final Integer ACTION_SEQUENCE_4 = 4;

    private static final boolean ALTERABLE = BOOLEAN.next();

    public static HearingEvent hearingEventWithMissingEventTime(final UUID hearingId) {
        final UUID hearingEventId = randomUUID();
        final ZonedDateTime lastModifiedTime = new UtcClock().now();
        final HearingEventDefinition randomHearingEventDefinition = values(HEARING_EVENT_DEFINITIONS).next();

        return HearingEvent.hearingEvent()
                .setId(hearingEventId)
                .setHearingId(hearingId)
                .setHearingEventDefinitionId(randomHearingEventDefinition.getId())
                .setRecordedLabel(randomHearingEventDefinition.getRecordedLabel())
                .setAlterable(randomHearingEventDefinition.isAlterable())
                .setLastModifiedTime(lastModifiedTime);
    }

    public static HearingEvent hearingStartedEvent(final UUID hearingId) {
        return newHearingEvent(hearingId, START_HEARING_EVENT_DEFINITION);
    }

    public static HearingEvent hearingEndedEvent(final UUID hearingId) {
        return newHearingEvent(hearingId, END_HEARING_EVENT_DEFINITION);
    }

    public static HearingEvent hearingPausedEvent(final UUID hearingId) {
        return newHearingEvent(hearingId, PAUSE_HEARING_EVENT_DEFINITION);
    }

    public static HearingEvent hearingResumedEvent(final UUID hearingId) {
        return newHearingEvent(hearingId, RESUME_HEARING_EVENT_DEFINITION);
    }

    public static HearingEvent identifyDefendantEvent(final UUID hearingId) {
        return newHearingEvent(hearingId, IDENTIFY_DEFENDANT_HEARING_EVENT_DEFINITION);
    }

    public static List<HearingEvent> manyRandomEvents(final UUID hearingId, Integer numberOfEvents) {
        return IntStream.range(0, numberOfEvents)
                .mapToObj((int i) -> randomEvent(hearingId, values(HEARING_EVENT_DEFINITIONS).next()))
                .collect(toList());
    }

    private static HearingEvent randomEvent(final UUID hearingId, final HearingEventDefinition hearingEventDefinition) {
        return newHearingEvent(hearingId, hearingEventDefinition);
    }

    public static HearingEventDefinitionData hearingEventDefinitionsWithPauseAndResumeEvents() {
        PAUSE_HEARING_EVENT_DEFINITION = new HearingEventDefinition(java.util.UUID.fromString(ID_3), ACTION_LABEL_3, ACTION_SEQUENCE_3, RECORDED_LABEL_3,  null, GROUP_LABEL_1, GROUP_SEQUENCE_1, ALTERABLE);
        RESUME_HEARING_EVENT_DEFINITION = new HearingEventDefinition(java.util.UUID.fromString(ID_4), ACTION_LABEL_4, ACTION_SEQUENCE_4, RECORDED_LABEL_4, null, GROUP_LABEL_1, GROUP_SEQUENCE_1, ALTERABLE);

        final List<HearingEventDefinition> eventDefinitions = newArrayList();
        eventDefinitions.add(PAUSE_HEARING_EVENT_DEFINITION);
        eventDefinitions.add(RESUME_HEARING_EVENT_DEFINITION);

        HEARING_EVENT_DEFINITIONS = newArrayList();
        HEARING_EVENT_DEFINITIONS.addAll(eventDefinitions);

        return new HearingEventDefinitionData(randomUUID(), eventDefinitions);
    }

    private static HearingEvent newHearingEvent(final UUID hearingId, final HearingEventDefinition hearingEventDefinition) {
        return newHearingEvent(hearingId, hearingEventDefinition.getRecordedLabel(), hearingEventDefinition);
    }

    private static HearingEvent newHearingEvent(final UUID hearingId, final String recordedLabel,
                                                final HearingEventDefinition hearingEventDefinition) {
        final UUID hearingEventId = randomUUID();
        final ZonedDateTime eventTime = new UtcClock().now();
        final ZonedDateTime lastModifiedTime = new UtcClock().now();

        return HearingEvent.hearingEvent()
                .setId(hearingEventId)
                .setHearingId(hearingId)
                .setHearingEventDefinitionId(hearingEventDefinition.getId())
                .setEventTime(eventTime)
                .setLastModifiedTime(lastModifiedTime)
                .setAlterable(hearingEventDefinition.isAlterable())
                .setRecordedLabel(recordedLabel);
    }

}
