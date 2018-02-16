package uk.gov.moj.cpp.hearing.query.view;

import static uk.gov.justice.services.messaging.JsonObjects.getUUID;

import uk.gov.justice.services.common.converter.Converter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.persist.entity.VerdictHearing;
import uk.gov.moj.cpp.hearing.query.view.service.VerdictService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;

@ServiceComponent(Component.QUERY_VIEW)
public class VerdictQueryView {
    private static final String FIELD_CASE_ID = "caseId";
    private static final String RESPONSE_NAME_CASE_VERDICTS = "hearing.get.case.verdicts";


    @Inject
    private VerdictService verdictService;

    @Inject
    private Enveloper enveloper;

    @Inject
    private Converter<List<VerdictHearing>, JsonArray> jsonConverter;

    @Handles("hearing.get.case.verdicts")
    public JsonEnvelope getCaseVerdicts(final JsonEnvelope envelope) {
        final Optional<UUID> caseId = getUUID(envelope.payloadAsJsonObject(), FIELD_CASE_ID);
        return this.enveloper.withMetadataFrom(envelope, RESPONSE_NAME_CASE_VERDICTS).apply(
                Json.createObjectBuilder()
                        .add("verdicts", this.jsonConverter.convert(this.verdictService.getVerdictHearingByCaseId(caseId.get())))
                        .build()
        );

    }
}
