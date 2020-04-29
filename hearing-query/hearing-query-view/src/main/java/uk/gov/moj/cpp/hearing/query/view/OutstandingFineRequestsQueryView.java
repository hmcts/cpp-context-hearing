package uk.gov.moj.cpp.hearing.query.view;

import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;

import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.DefendantOutstandingFineRequestsResult;
import uk.gov.moj.cpp.hearing.query.view.service.OutstandingFineRequestsService;

import java.time.LocalDate;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.persistence.NoResultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(Component.QUERY_VIEW)
@SuppressWarnings({"squid:S3655"})
public class OutstandingFineRequestsQueryView {

    private static final String FIELD_HEARING_DATE = "hearingDate";

    private static final Logger LOGGER = LoggerFactory.getLogger(OutstandingFineRequestsQueryView.class);

    @Inject
    private OutstandingFineRequestsService outstandingFineRequestsService;
    @Inject
    private ObjectToJsonValueConverter objectToJsonValueConverter;

    @SuppressWarnings("squid:S1166")
    @Handles("hearing.defendant.outstanding-fine-requests")
    public JsonEnvelope getDefendantOutstandingFineRequests(final JsonEnvelope envelope) {

        final JsonObject payload = envelope.payloadAsJsonObject();

        final LocalDate hearingDate = LocalDate.parse(payload.getString(FIELD_HEARING_DATE));
        try {
            final DefendantOutstandingFineRequestsResult result = outstandingFineRequestsService.getDefendantOutstandingFineRequestsByHearingDate(hearingDate);
            return envelopeFrom(envelope.metadata(), objectToJsonValueConverter.convert(result));
        } catch (final NoResultException nre) {
            LOGGER.error("### No defendant found with hearingDate = '{}'", payload.getString(FIELD_HEARING_DATE));
            return envelopeFrom(envelope.metadata(), Json.createObjectBuilder().build());
        }
    }
}
