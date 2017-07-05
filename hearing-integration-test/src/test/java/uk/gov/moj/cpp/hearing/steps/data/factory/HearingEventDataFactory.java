package uk.gov.moj.cpp.hearing.steps.data.factory;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import java.util.stream.IntStream;
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.persist.entity.HearingEvent;
import uk.gov.moj.cpp.hearing.steps.data.DefenceCounselData;
import uk.gov.moj.cpp.hearing.steps.data.HearingEventDefinitionData;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class HearingEventDataFactory {

    private static final String SEQUENCE_TYPE_SENTENCING = "SENTENCING";
    private static final String SEQUENCE_TYPE_PAUSE_RESUME = "PAUSE_RESUME";
    private static final String SEQUENCE_TYPE_NOT_REGISTERED = "NOT_REGISTERED";

    public static HearingEvent hearingEventWithMissingEventTime(final UUID hearingId) {
        final UUID hearingEventId = randomUUID();
        final String randomRecordedLabel = STRING.next();
        final ZonedDateTime lastModifiedTime = new UtcClock().now();

        return new HearingEvent(hearingEventId, hearingId, randomRecordedLabel, null, lastModifiedTime);
    }

    public static HearingEvent hearingStartedEvent(final UUID hearingId) {
        return newHearingEvent(hearingId, "Hearing started");
    }

    public static HearingEvent identifyDefendantEvent(final UUID hearingId) {
        return newHearingEvent(hearingId, "Defendant Identified");
    }

    public static List<HearingEvent> manyRandomEvents(final UUID hearingId, Integer numberOfEvents) {
        return IntStream.range(0, numberOfEvents)
                .mapToObj((int i) -> randomEvent(hearingId))
                .collect(toList());
    }

    public static HearingEvent randomEvent(final UUID hearingId) {
        return newHearingEvent(hearingId, STRING.next());
    }

    public static HearingEventDefinitionData hearingEventDefinitionsWithOnlySequencedEvents() {
        final List<HearingEventDefinition> eventDefinitions = newArrayList(
                new HearingEventDefinition("Start Hearing", "Call Case On", 1, SEQUENCE_TYPE_SENTENCING, null, null, null),
                new HearingEventDefinition("Identify defendant", "Defendant Identified", 2, SEQUENCE_TYPE_SENTENCING, null, null, null),
                new HearingEventDefinition("Take Plea", "Plea", 3, SEQUENCE_TYPE_SENTENCING, null, null, null),
                new HearingEventDefinition("Prosecution Opening", "Prosecution Opening", 4, SEQUENCE_TYPE_SENTENCING, null, null, null),
                new HearingEventDefinition("<counsel.name>", "Defence <counsel.name> mitigated for <defendant.name>", 5, SEQUENCE_TYPE_SENTENCING, "defendant.name,counsel.name", "Mitigation by:", "defending <defendant.name>"),
                new HearingEventDefinition("Sentencing", "Sentencing", 6, SEQUENCE_TYPE_SENTENCING, null, null, null),
                new HearingEventDefinition("End Hearing", "Hearing Ended", 7, SEQUENCE_TYPE_SENTENCING, null, null, null)
        );

        return new HearingEventDefinitionData(randomUUID(), eventDefinitions);
    }

    public static HearingEventDefinitionData hearingEventDefinitionsWithOnlyNonSequencedEvents() {
        final List<HearingEventDefinition> eventDefinitions = newArrayList(
                new HearingEventDefinition("Prosecution challenges defence application", "Prosecution challenged defence application", null, null, null, null, null),
                new HearingEventDefinition("Judge ruling: Contempt of court", "Judge ruling: Contempt of court", null, null, null, null, null)
        );

        return new HearingEventDefinitionData(randomUUID(), eventDefinitions);
    }

    public static HearingEventDefinitionData hearingEventDefinitionsWithBothSequencedAndNonSequencedEvents() {
        final List<HearingEventDefinition> eventDefinitions = newArrayList();
        eventDefinitions.addAll(hearingEventDefinitionsWithOnlySequencedEvents().getEventDefinitions());
        eventDefinitions.addAll(hearingEventDefinitionsWithOnlyNonSequencedEvents().getEventDefinitions());

        return new HearingEventDefinitionData(randomUUID(), eventDefinitions);
    }

    public static HearingEventDefinitionData hearingEventDefinitionsWithPauseAndResumeEvents() {
        final List<HearingEventDefinition> eventDefinitions = newArrayList();
        eventDefinitions.addAll(hearingEventDefinitionsWithBothSequencedAndNonSequencedEvents().getEventDefinitions());
        eventDefinitions.add(new HearingEventDefinition("Pause", "Hearing paused", 1, SEQUENCE_TYPE_PAUSE_RESUME, null, null, null));
        eventDefinitions.add(new HearingEventDefinition("Resume", "Hearing resumed", 2, SEQUENCE_TYPE_PAUSE_RESUME, null, null, null));

        return new HearingEventDefinitionData(randomUUID(), eventDefinitions);
    }

    public static HearingEventDefinitionData hearingEventDefinitionsWithNotRegisteredSequenceTypeEvents() {
        final List<HearingEventDefinition> eventDefinitions = newArrayList();
        eventDefinitions.addAll(hearingEventDefinitionsWithPauseAndResumeEvents().getEventDefinitions());
        eventDefinitions.add(new HearingEventDefinition("Interpreter swears-in", "Interpreter sworn-in", 1, SEQUENCE_TYPE_NOT_REGISTERED, null, null, null));
        eventDefinitions.add(new HearingEventDefinition("Probation reads oral PSR", "Probation read oral PSR", 2, SEQUENCE_TYPE_NOT_REGISTERED, null, null, null));

        return new HearingEventDefinitionData(randomUUID(), eventDefinitions);
    }

    public static HearingEvent mitigationEvent(final UUID hearingId, final DefenceCounselData defenceCounsel) {
        final String recordedLabel = format("Defence %s mitigated for %s", defenceCounsel.getPersonName(), defenceCounsel.getMapOfDefendantIdToNames().values().stream().findFirst().orElse(null));
        return newHearingEvent(hearingId, recordedLabel);
    }

    private static HearingEvent newHearingEvent(final UUID hearingId, final String recordedLabel) {
        final UUID hearingEventId = randomUUID();
        final ZonedDateTime eventTime = new UtcClock().now();
        final ZonedDateTime lastModifiedTime = new UtcClock().now();

        return new HearingEvent(hearingEventId, hearingId, recordedLabel, eventTime, lastModifiedTime);
    }

}
