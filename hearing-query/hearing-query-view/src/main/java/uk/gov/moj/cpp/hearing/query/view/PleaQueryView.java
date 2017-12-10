package uk.gov.moj.cpp.hearing.query.view;

import static uk.gov.justice.services.messaging.JsonObjects.getUUID;

import uk.gov.justice.services.common.converter.Converter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.persist.entity.PleaHearing;
import uk.gov.moj.cpp.hearing.query.view.service.PleaService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;

@ServiceComponent(Component.QUERY_VIEW)
public class PleaQueryView {
    private static final String FIELD_CASE_ID = "caseId";
    private static final String RESPONSE_NAME_HEARING_PLEA = "hearing.get-pleas";


    @Inject
    private PleaService pleaService;

    @Inject
    private Enveloper enveloper;

    @Inject
    private Converter<List<PleaHearing>, JsonArray> jsonConverter;

    @Handles("hearing.get.pleas")
    public JsonEnvelope getPleas(final JsonEnvelope envelope) {
        final Optional<UUID> caseId = getUUID(envelope.payloadAsJsonObject(), FIELD_CASE_ID);
        return enveloper.withMetadataFrom(envelope, RESPONSE_NAME_HEARING_PLEA).apply(
                Json.createObjectBuilder()
                        .add("pleas", jsonConverter.convert(pleaService.getPleaHearingByCaseId(caseId.get())))
                        .build()
        );

    }

}
