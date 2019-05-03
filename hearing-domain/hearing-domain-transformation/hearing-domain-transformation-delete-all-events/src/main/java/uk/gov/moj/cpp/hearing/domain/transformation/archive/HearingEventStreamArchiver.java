package uk.gov.moj.cpp.hearing.domain.transformation.archive;

import static com.google.common.collect.Lists.newArrayList;
import static uk.gov.justice.tools.eventsourcing.transformation.api.Action.DEACTIVATE;
import static uk.gov.justice.tools.eventsourcing.transformation.api.Action.NO_ACTION;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.tools.eventsourcing.transformation.api.Action;
import uk.gov.justice.tools.eventsourcing.transformation.api.EventTransformation;
import uk.gov.justice.tools.eventsourcing.transformation.api.annotation.Transformation;

import java.util.List;
import java.util.stream.Stream;

@Transformation
public class HearingEventStreamArchiver implements EventTransformation {

    private static final List<String> EVENTS_TO_KEEP = newArrayList( "hearing.subscriptions-uploaded", "hearing.hearing-event-definitions-deleted", "hearing.hearing-event-definitions-created");

    private Enveloper enveloper;

    @Override
    public Action actionFor(final JsonEnvelope event) {
        if (EVENTS_TO_KEEP.stream()
                .anyMatch(eventToArchive -> event.metadata().name().equalsIgnoreCase(eventToArchive))) {
            return NO_ACTION;
        } else {
            return DEACTIVATE;
        }
    }

    @Override
    public Stream<JsonEnvelope> apply(final JsonEnvelope event) {
        return Stream.of(event);
    }

    @Override
    public void setEnveloper(final Enveloper enveloper) {
        this.enveloper = enveloper;
    }
}