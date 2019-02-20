package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDefinitionsCreated;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDeleted;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventsUpdated;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent;
import uk.gov.moj.cpp.hearing.persist.entity.heda.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.repository.HearingEventDefinitionRepository;
import uk.gov.moj.cpp.hearing.repository.HearingEventRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

@SuppressWarnings("WeakerAccess")
@ServiceComponent(EVENT_LISTENER)
public class HearingLogEventListener {

    private final HearingEventDefinitionRepository hearingEventDefinitionRepository;
    private final HearingEventRepository hearingEventRepository;
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    public HearingLogEventListener(HearingEventDefinitionRepository hearingEventDefinitionRepository,
                                   HearingEventRepository hearingEventRepository, JsonObjectToObjectConverter jsonObjectToObjectConverter) {
        this.hearingEventDefinitionRepository = hearingEventDefinitionRepository;
        this.hearingEventRepository = hearingEventRepository;
        this.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
    }

    @Handles("hearing.hearing-event-definitions-created")
    public void hearingEventDefinitionsCreated(final JsonEnvelope event) {
        final HearingEventDefinitionsCreated eventDefinitionsCreated = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), HearingEventDefinitionsCreated.class);

        eventDefinitionsCreated.getEventDefinitions().stream()
                .map(e -> new HearingEventDefinition(e.getId(),
                        e.getRecordedLabel(), e.getActionLabel(), e.getSequence(), e.getSequenceType(),
                        e.getCaseAttribute(), e.getGroupLabel(), e.getActionLabelExtension(), e.isAlterable()))
                .forEach(hearingEventDefinitionRepository::save);
    }

    @Handles("hearing.hearing-event-definitions-deleted")
    public void hearingEventDefinitionsDeleted(final JsonEnvelope event) {
        final List<HearingEventDefinition> activeEventDefinitions = hearingEventDefinitionRepository.findAllActive();

        activeEventDefinitions.stream()
                .map(eventDefinition -> eventDefinition.builder().delete().build())
                .forEach(hearingEventDefinitionRepository::save);
    }

    @Handles("hearing.hearing-event-logged")
    public void hearingEventLogged(final JsonEnvelope event) {
        final HearingEventLogged hearingEventLogged = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), HearingEventLogged.class);

        hearingEventRepository.save(HearingEvent.hearingEvent()
                .setId(hearingEventLogged.getHearingEventId())
                .setHearingId(hearingEventLogged.getHearingId())
                .setHearingEventDefinitionId(hearingEventLogged.getHearingEventDefinitionId())
                .setRecordedLabel(hearingEventLogged.getRecordedLabel())
                .setEventTime(hearingEventLogged.getEventTime())
                .setLastModifiedTime(hearingEventLogged.getLastModifiedTime())
                .setAlterable(hearingEventLogged.isAlterable())
                .setDefenceCounselId(hearingEventLogged.getDefenceCounselId())
        );
    }

    @Handles("hearing.hearing-event-deleted")
    public void hearingEventDeleted(final JsonEnvelope event) {
        final HearingEventDeleted hearingEventDeleted = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), HearingEventDeleted.class);

        final Optional<HearingEvent> optionalHearingEvent = hearingEventRepository.findOptionalById(hearingEventDeleted.getHearingEventId());
        optionalHearingEvent.ifPresent(hearingEvent -> hearingEventRepository.save(hearingEvent.setDeleted(true)));
    }

    @Handles("hearing.hearing-events-updated")
    public void hearingEventsUpdated(final JsonEnvelope event) {
        final HearingEventsUpdated hearingEventsUpdated = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), HearingEventsUpdated.class);

        final Map<UUID, HearingEvent> hearingEventIdToHearingEvent = hearingEventRepository
                .findByHearingIdOrderByEventTimeAsc(hearingEventsUpdated.getHearingId()).stream()
                .collect(Collectors.toMap(HearingEvent::getId, hearingEvent -> hearingEvent));

        hearingEventsUpdated.getHearingEvents().stream().forEach(hearingEvent -> {

            final HearingEvent repositoryEvent = hearingEventIdToHearingEvent.get(hearingEvent.getHearingEventId());

            if (repositoryEvent != null) {
                repositoryEvent.setRecordedLabel(hearingEvent.getRecordedLabel());
                hearingEventRepository.save(repositoryEvent);
            }
        });

    }

}