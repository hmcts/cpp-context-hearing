package uk.gov.moj.cpp.hearing.domain.transformation.mot;

import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.tools.eventsourcing.transformation.api.Action;
import uk.gov.justice.tools.eventsourcing.transformation.api.EventTransformation;
import uk.gov.justice.tools.eventsourcing.transformation.api.annotation.Transformation;
import uk.gov.moj.cpp.hearing.domain.transformation.mot.transform.EventInstance;
import uk.gov.moj.cpp.hearing.domain.transformation.mot.transform.TransformFactory;

import javax.json.JsonObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Stream.of;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;
import static uk.gov.justice.tools.eventsourcing.transformation.api.Action.NO_ACTION;
import static uk.gov.justice.tools.eventsourcing.transformation.api.Action.TRANSFORM;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.HEARING_EVENTS_INITIATED;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.HEARING_OFFENCE_PLEA_UPDATED;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.HEARING_SENDING_SHEET_RECORDED;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.NOWS_REQUESTED;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.OFFENCE_ADDED;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.OFFENCE_UPDATED;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.OFFENCE_UPDATED_FOR_HEARINGS;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.PENDING_NOWS_REQUESTED;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.RESULTS_SHARED;

@Transformation
public class HearingEventStreamTransform implements EventTransformation {

    private Enveloper enveloper;

    private TransformFactory factory;
    private Map<String, ProsecutionCase> hearingMap = new HashMap<>();

    public HearingEventStreamTransform() {
        factory = new TransformFactory();
    }

    private final List<String> eventsToTransform = newArrayList(OFFENCE_UPDATED, OFFENCE_ADDED, OFFENCE_UPDATED_FOR_HEARINGS, HEARING_EVENTS_INITIATED,
            RESULTS_SHARED, HEARING_OFFENCE_PLEA_UPDATED, PENDING_NOWS_REQUESTED, NOWS_REQUESTED, HEARING_SENDING_SHEET_RECORDED);

    @Override
    public Action actionFor(final JsonEnvelope event) {
        if (eventsToTransform.stream().anyMatch(eventToTransform -> event.metadata().name().equalsIgnoreCase(eventToTransform))) {
            return TRANSFORM;
        }

        return NO_ACTION;
    }

    @Override
    public Stream<JsonEnvelope> apply(final JsonEnvelope event) {

        final JsonObject payload = event.payloadAsJsonObject();

        final EventInstance eventInstance = factory.getEventInstance(event.metadata().name());


        final JsonObject transformedPayload = eventInstance.transform(payload, hearingMap);

        return of(envelopeFrom(metadataFrom(event.metadata()), transformedPayload));

    }

    @Override
    public void setEnveloper(final Enveloper enveloper) {
        this.enveloper = enveloper;
    }

    public void setFactory(final TransformFactory factory) {
        this.factory = factory;
    }
}
