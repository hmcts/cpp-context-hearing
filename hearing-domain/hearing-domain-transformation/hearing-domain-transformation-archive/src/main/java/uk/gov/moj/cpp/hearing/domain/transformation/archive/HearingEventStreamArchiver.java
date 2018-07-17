package uk.gov.moj.cpp.hearing.domain.transformation.archive;

import static java.util.Arrays.asList;
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

    private static final String EARLIER_ARCHIVED_EVENTS_NAME_ENDS_WITH = ".archived.1.9.release";

    private static final List<String> EVENTS_TO_ARCHIVE = asList(
            "hearing.case-associated",
            "hearing.case.plea-changed",
            "hearing.case.plea-added",
            "hearing.court-assigned",
            "hearing.defence-counsel-added",
            "hearing.draft-result-saved",
            "hearing.hearing.confirmed-recorded",
            "hearing.hearing-event-definitions-created",
            "hearing.hearing-event-definitions-deleted",
            "hearing.hearing-event-deleted",
            "hearing.hearing-event-logged",
            "hearing.hearing-initiated",
            "hearing.hearing-plea-updated",
            "hearing.judge-assigned",
            "hearing.mags-court-hearing-recorded",
            "hearing.plea-added",
            "hearing.plea-changed",
            "hearing.prosecution-counsel-added",
            "hearing.result-amended",
            "hearing.results-shared",
            "hearing.room-booked",
            "hearing.sending-sheet-recorded",
            "hearing.sending-sheet-previously-recorded",
            "hearing.hearing-update-plea-ignored",
            "hearing.hearing-event-ignored",
            "hearing.hearing-event-deletion-ignored",
            "hearing.adjourn-date-updated"
    );

    private Enveloper enveloper;

    @Override
    public Stream<JsonEnvelope> apply(final JsonEnvelope event) {
        final String restoredEventName = event.metadata().name().replace(EARLIER_ARCHIVED_EVENTS_NAME_ENDS_WITH, "");
        final JsonEnvelope transformedEnvelope = enveloper
                .withMetadataFrom(event, restoredEventName)
                .apply(event.payload());
        return Stream.of(transformedEnvelope);
    }

    @Override
    public Action actionFor(final JsonEnvelope event) {
        if (EVENTS_TO_ARCHIVE.stream()
                .anyMatch(eventToArchive -> event.metadata().name().equalsIgnoreCase(eventToArchive))) {
            return DEACTIVATE;
        } else if (event.metadata().name().endsWith(EARLIER_ARCHIVED_EVENTS_NAME_ENDS_WITH)) {
            return new Action(true, true, false);
        }
        return NO_ACTION;
    }

    @Override
    public void setEnveloper(final Enveloper enveloper) {
        this.enveloper = enveloper;
    }
}