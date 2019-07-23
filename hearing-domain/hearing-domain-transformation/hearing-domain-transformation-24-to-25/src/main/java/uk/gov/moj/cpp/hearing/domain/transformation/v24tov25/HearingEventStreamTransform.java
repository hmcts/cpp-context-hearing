package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Stream.of;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;
import static uk.gov.justice.tools.eventsourcing.transformation.api.Action.DEACTIVATE;
import static uk.gov.justice.tools.eventsourcing.transformation.api.Action.NO_ACTION;
import static uk.gov.justice.tools.eventsourcing.transformation.api.Action.TRANSFORM;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.DEFENDANT_DETAILS_UPDATED;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.HEARING_CASE_NOTE_SAVED;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.HEARING_EVENTS_INITIATED;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.HEARING_OFFENCE_VERDICT_UPDATED;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.INHERITED_VERDICT_ADDED;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.NOWS_REQUESTED;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.OFFENCE_ADDED;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.OFFENCE_UPDATED;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.PENDING_NOWS_REQUESTED;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.RESULTS_SHARED;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.RESULT_LINES_STATUS_UPDATED;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.tools.eventsourcing.transformation.api.Action;
import uk.gov.justice.tools.eventsourcing.transformation.api.EventTransformation;
import uk.gov.justice.tools.eventsourcing.transformation.api.annotation.Transformation;
import uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.transform.EventInstance;
import uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.transform.TransformFactory;

import java.util.List;
import java.util.stream.Stream;

import javax.json.JsonObject;

@Transformation
public class HearingEventStreamTransform implements EventTransformation {

    private Enveloper enveloper;

    private TransformFactory factory;

    public HearingEventStreamTransform() {
        factory = new TransformFactory();
    }

    private final List<String> eventsToTransform = newArrayList(
            DEFENDANT_DETAILS_UPDATED, HEARING_OFFENCE_VERDICT_UPDATED, INHERITED_VERDICT_ADDED,
            HEARING_EVENTS_INITIATED, OFFENCE_ADDED, OFFENCE_UPDATED, RESULTS_SHARED,
            HEARING_CASE_NOTE_SAVED, PENDING_NOWS_REQUESTED, RESULT_LINES_STATUS_UPDATED, NOWS_REQUESTED);

    @Override
    public Action actionFor(final JsonEnvelope event) {
        if(eventsToTransform.stream().anyMatch(eventToTransform -> event.metadata().name().equalsIgnoreCase(eventToTransform))) {
            return TRANSFORM;
        }

        return NO_ACTION;
    }

    @Override
    public Stream<JsonEnvelope> apply(final JsonEnvelope event) {

        final JsonObject payload = event.payloadAsJsonObject();

        final EventInstance eventInstance = factory.getEventInstance(event.metadata().name());

        final JsonObject transformedPayload = eventInstance.transform(payload);

        return of(envelopeFrom(metadataOf(event.metadata().asJsonObject().getString(ID), event.metadata().name()).build(), transformedPayload));
    }

    @Override
    public void setEnveloper(final Enveloper enveloper) {
        this.enveloper = enveloper;
    }

    public void setFactory(final TransformFactory factory) {
        this.factory = factory;
    }
}
