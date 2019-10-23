package uk.gov.moj.cpp.hearing.domain.transformation.ctl;

import org.slf4j.Logger;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.tools.eventsourcing.transformation.api.Action;
import uk.gov.justice.tools.eventsourcing.transformation.api.EventTransformation;
import uk.gov.justice.tools.eventsourcing.transformation.api.annotation.Transformation;

import javax.json.JsonObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Stream.of;
import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;
import static uk.gov.justice.tools.eventsourcing.transformation.api.Action.NO_ACTION;
import static uk.gov.justice.tools.eventsourcing.transformation.api.Action.TRANSFORM;
import static uk.gov.moj.cpp.hearing.domain.transformation.ctl.core.SchemaVariableConstants.APPLICATION_DETAIL_CHANGED;
import static uk.gov.moj.cpp.hearing.domain.transformation.ctl.core.SchemaVariableConstants.DEFENDANT_ADDED;
import static uk.gov.moj.cpp.hearing.domain.transformation.ctl.core.SchemaVariableConstants.DEFENDANT_DETAILS_UPDATED;
import static uk.gov.moj.cpp.hearing.domain.transformation.ctl.core.SchemaVariableConstants.HEARING_EXTENDED;
import static uk.gov.moj.cpp.hearing.domain.transformation.ctl.core.SchemaVariableConstants.HEARING_INITIATED;
import static uk.gov.moj.cpp.hearing.domain.transformation.ctl.core.SchemaVariableConstants.MAGS_COURT_HEARING_RECORDED;
import static uk.gov.moj.cpp.hearing.domain.transformation.ctl.core.SchemaVariableConstants.NOWS_REQUESTED;
import static uk.gov.moj.cpp.hearing.domain.transformation.ctl.core.SchemaVariableConstants.OFFENCE_ADDED;
import static uk.gov.moj.cpp.hearing.domain.transformation.ctl.core.SchemaVariableConstants.OFFENCE_UPDATED;
import static uk.gov.moj.cpp.hearing.domain.transformation.ctl.core.SchemaVariableConstants.PENDING_NOWS_REQUESTED;
import static uk.gov.moj.cpp.hearing.domain.transformation.ctl.core.SchemaVariableConstants.RESULTS_SHARED;
import static uk.gov.moj.cpp.hearing.domain.transformation.ctl.core.SchemaVariableConstants.SENDING_SHEET_RECORDED;
import static uk.gov.moj.cpp.hearing.domain.transformation.ctl.core.SchemaVariableConstants.UPDATE_CASE_DEFENDANT_DETAILS_ENRICHED_HEARING_IDS;

@Transformation
@SuppressWarnings({"pmd:BeanMembersShouldSerialize", "squid:S1450", "squid:S1186"})
public class HearingEventStreamTransform implements EventTransformation {

    private Enveloper enveloper;

    private uk.gov.moj.cpp.hearing.domain.transformation.ctl.BailStatusEnum2ObjectTransformer bailStatusTransformer = new BailStatusEnum2ObjectTransformer();

    private static final Logger LOGGER = getLogger(HearingEventStreamTransform.class);

    public HearingEventStreamTransform() {
    }

    protected static final Set<String> eventsToTransform = new HashSet(Arrays.asList(
            OFFENCE_UPDATED,
            HEARING_INITIATED,
            NOWS_REQUESTED,
            PENDING_NOWS_REQUESTED,
            MAGS_COURT_HEARING_RECORDED,
            UPDATE_CASE_DEFENDANT_DETAILS_ENRICHED_HEARING_IDS,
            DEFENDANT_ADDED,
            OFFENCE_ADDED,
            HEARING_EXTENDED,
            DEFENDANT_DETAILS_UPDATED,
            RESULTS_SHARED,
            SENDING_SHEET_RECORDED,
            APPLICATION_DETAIL_CHANGED
    ));

    @Override
    public Action actionFor(final JsonEnvelope event) {
        if (eventsToTransform.contains(event.metadata().name())) {
            return TRANSFORM;
        }
        return NO_ACTION;
    }

    @Override
    public Stream<JsonEnvelope> apply(final JsonEnvelope event) {

        final JsonObject payload = event.payloadAsJsonObject();

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("----------------------event name------------ {}", event.metadata().name());
        }

        final JsonObject transformedPayload = bailStatusTransformer.transform(payload);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("-------------------transformedPayload---------------{}", transformedPayload);
        }

        return of(envelopeFrom(metadataFrom(event.metadata()), transformedPayload));

    }

    @Override
    public void setEnveloper(final Enveloper enveloper) {
        this.enveloper = enveloper;
    }


}
