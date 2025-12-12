package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.moj.cpp.hearing.domain.HearingState.VALIDATED;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultAmendmentsValidated;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;

@ServiceComponent(EVENT_LISTENER)
public class ResultAmendmentsValidatedEventListener {

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private HearingRepository hearingRepository;

    @Transactional
    @Handles("hearing.event.result-amendments-validated")
    public void resultAmendmentsValidated(final JsonEnvelope envelope) {
        final ResultAmendmentsValidated validateResultAmendmentsRequested = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), ResultAmendmentsValidated.class);

        hearingRepository.findBy(validateResultAmendmentsRequested.getHearingId()).setHearingState(VALIDATED);
    }

    @Transactional
    @Handles("hearing.event.validate-result-amendments-requested")
    @SuppressWarnings({"squid:S4144", "squid:S1186"})
    //Dummy event to avoid DLQ caused due to event renaming
    public void validateResultAmendmentsRequested(final JsonEnvelope envelope) {

    }
}