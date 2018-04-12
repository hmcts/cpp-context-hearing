package uk.gov.moj.cpp.hearing.steps.data.factory;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.values;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.persist.entity.HearingEvent;
import uk.gov.moj.cpp.hearing.steps.data.DefenceCounselData;
import uk.gov.moj.cpp.hearing.steps.data.HearingEventDefinitionData;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

public class HearingEventDataFactory {

    private static final String SEQUENCE_TYPE_SENTENCING = "SENTENCING";
    private static final String SEQUENCE_TYPE_PAUSE_RESUME = "PAUSE_RESUME";
    private static final String SEQUENCE_TYPE_NOT_REGISTERED = "NOT_REGISTERED";

    private static HearingEventDefinition START_HEARING_EVENT_DEFINITION;
    private static HearingEventDefinition END_HEARING_EVENT_DEFINITION;
    private static HearingEventDefinition PAUSE_HEARING_EVENT_DEFINITION;
    private static HearingEventDefinition RESUME_HEARING_EVENT_DEFINITION;
    private static HearingEventDefinition IDENTIFY_DEFENDANT_HEARING_EVENT_DEFINITION;
    private static HearingEventDefinition MITIGATION_HEARING_EVENT_DEFINITION;

    private static List<HearingEventDefinition> HEARING_EVENT_DEFINITIONS;

    public static HearingEvent hearingEventWithMissingEventTime(final UUID hearingId) {
        final UUID hearingEventId = randomUUID();
        final ZonedDateTime lastModifiedTime = new UtcClock().now();
        final HearingEventDefinition randomHearingEventDefinition = values(HEARING_EVENT_DEFINITIONS).next();

        return new HearingEvent(hearingEventId, randomHearingEventDefinition.getId(), hearingId,
                randomHearingEventDefinition.getRecordedLabel(),null, lastModifiedTime,
                randomHearingEventDefinition.isAlterable(), null);
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

    public static HearingEventDefinitionData hearingEventDefinitionsWithOnlySequencedEvents() {
        START_HEARING_EVENT_DEFINITION = new HearingEventDefinition(randomUUID(), "Start Hearing", "Call Case On", 1, SEQUENCE_TYPE_SENTENCING, null, null, null, false);
        IDENTIFY_DEFENDANT_HEARING_EVENT_DEFINITION = new HearingEventDefinition(randomUUID(),"Identify defendant", "Defendant Identified", 2, SEQUENCE_TYPE_SENTENCING, null, null, null, true);
        MITIGATION_HEARING_EVENT_DEFINITION = new HearingEventDefinition(randomUUID(), "<counsel.name>", "Defence <counsel.name> mitigated for <defendant.name>", 5, SEQUENCE_TYPE_SENTENCING, "defendant.name,counsel.name", "Mitigation by:", "defending <defendant.name>", true);
        END_HEARING_EVENT_DEFINITION = new HearingEventDefinition(randomUUID(), "End Hearing", "Hearing Ended", 7, SEQUENCE_TYPE_SENTENCING, null, null, null, false);

        final List<HearingEventDefinition> eventDefinitions = newArrayList(
                START_HEARING_EVENT_DEFINITION,
                IDENTIFY_DEFENDANT_HEARING_EVENT_DEFINITION,
                new HearingEventDefinition(randomUUID(),"Take Plea", "Plea", 3, SEQUENCE_TYPE_SENTENCING, null, null, null, true),
                new HearingEventDefinition(randomUUID(),"Prosecution Opening", "Prosecution Opening", 4, SEQUENCE_TYPE_SENTENCING, null, null, null, true),
                MITIGATION_HEARING_EVENT_DEFINITION,
                new HearingEventDefinition(randomUUID(),"Sentencing", "Sentencing", 6, SEQUENCE_TYPE_SENTENCING, null, null, null, true),
                END_HEARING_EVENT_DEFINITION
        );

        HEARING_EVENT_DEFINITIONS = newArrayList();
        HEARING_EVENT_DEFINITIONS.addAll(eventDefinitions);

        return new HearingEventDefinitionData(randomUUID(), eventDefinitions);
    }

    public static HearingEventDefinitionData hearingEventDefinitionsWithOnlyNonSequencedEvents() {
        final List<HearingEventDefinition> eventDefinitions = newArrayList(
                new HearingEventDefinition(randomUUID(),"Prosecution challenges defence application", "Prosecution challenged defence application", null, null, null, null, null, true),
                new HearingEventDefinition(randomUUID(),"Judge ruling: Contempt of court", "Judge ruling: Contempt of court", null, null, null, null, null, true)
        );

        HEARING_EVENT_DEFINITIONS = newArrayList();
        HEARING_EVENT_DEFINITIONS.addAll(eventDefinitions);

        return new HearingEventDefinitionData(randomUUID(), eventDefinitions);
    }

    public static HearingEventDefinitionData hearingEventDefinitionsWithBothSequencedAndNonSequencedEvents() {
        final List<HearingEventDefinition> eventDefinitions = newArrayList();
        eventDefinitions.addAll(hearingEventDefinitionsWithOnlySequencedEvents().getEventDefinitions());
        eventDefinitions.addAll(hearingEventDefinitionsWithOnlyNonSequencedEvents().getEventDefinitions());

        HEARING_EVENT_DEFINITIONS = newArrayList();
        HEARING_EVENT_DEFINITIONS.addAll(eventDefinitions);

        return new HearingEventDefinitionData(randomUUID(), eventDefinitions);
    }

    public static HearingEventDefinitionData hearingEventDefinitionsWithPauseAndResumeEvents() {
        PAUSE_HEARING_EVENT_DEFINITION = new HearingEventDefinition(randomUUID(), "Pause", "Hearing paused", 1, SEQUENCE_TYPE_PAUSE_RESUME, null, null, null, false);
        RESUME_HEARING_EVENT_DEFINITION = new HearingEventDefinition(randomUUID(), "Resume", "Hearing resumed", 2, SEQUENCE_TYPE_PAUSE_RESUME, null, null, null, false);

        final List<HearingEventDefinition> eventDefinitions = newArrayList();
        eventDefinitions.addAll(hearingEventDefinitionsWithBothSequencedAndNonSequencedEvents().getEventDefinitions());
        eventDefinitions.add(PAUSE_HEARING_EVENT_DEFINITION);
        eventDefinitions.add(RESUME_HEARING_EVENT_DEFINITION);

        HEARING_EVENT_DEFINITIONS = newArrayList();
        HEARING_EVENT_DEFINITIONS.addAll(eventDefinitions);

        return new HearingEventDefinitionData(randomUUID(), eventDefinitions);
    }

    public static HearingEventDefinitionData hearingEventDefinitionsWithNotRegisteredSequenceTypeEvents() {
        final List<HearingEventDefinition> eventDefinitions = newArrayList();
        eventDefinitions.addAll(hearingEventDefinitionsWithPauseAndResumeEvents().getEventDefinitions());
        eventDefinitions.add(new HearingEventDefinition(randomUUID(),"Interpreter swears-in", "Interpreter sworn-in", 1, SEQUENCE_TYPE_NOT_REGISTERED, null, null, null, true));
        eventDefinitions.add(new HearingEventDefinition(randomUUID(),"Probation reads oral PSR", "Probation read oral PSR", 2, SEQUENCE_TYPE_NOT_REGISTERED, null, null, null, true));

        HEARING_EVENT_DEFINITIONS = newArrayList();
        HEARING_EVENT_DEFINITIONS.addAll(eventDefinitions);

        return new HearingEventDefinitionData(randomUUID(), eventDefinitions);
    }

    public static HearingEvent mitigationEvent(final UUID hearingId, final DefenceCounselData defenceCounsel) {
        final String recordedLabel = format("Defence %s mitigated for %s", defenceCounsel.getPersonName(), defenceCounsel.getMapOfDefendantIdToNames().values().stream().findFirst().orElse(null));
        return newHearingEvent(hearingId, recordedLabel, MITIGATION_HEARING_EVENT_DEFINITION);
    }

    private static HearingEvent newHearingEvent(final UUID hearingId, final HearingEventDefinition hearingEventDefinition) {
        return newHearingEvent(hearingId, hearingEventDefinition.getRecordedLabel(), hearingEventDefinition);
    }

    private static HearingEvent newHearingEvent(final UUID hearingId, final String recordedLabel,
                                                final HearingEventDefinition hearingEventDefinition) {
        final UUID hearingEventId = randomUUID();
        final ZonedDateTime eventTime = new UtcClock().now();
        final ZonedDateTime lastModifiedTime = new UtcClock().now();

        return new HearingEvent(hearingEventId, hearingEventDefinition.getId(), hearingId, recordedLabel,
                eventTime, lastModifiedTime, hearingEventDefinition.isAlterable(), null);
    }

}
