package uk.gov.moj.cpp.hearing.event;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.enforcement.FineRequest;
import uk.gov.moj.cpp.hearing.command.enforcement.RequestOutstandingFines;
import uk.gov.moj.cpp.hearing.domain.DefendantOutstandingFineRequestsResult;
import uk.gov.moj.cpp.hearing.domain.event.OutstandingFinesRequested;
import uk.gov.moj.cpp.hearing.event.service.CourtHouseReverseLookup;

import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.JsonObject;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
public class OutstandingFinesRequestedProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutstandingFinesRequestedProcessor.class);

    // can be configurable
    private int outstandingFinesBatchSize = 100;

    @Inject
    private Sender sender;

    @Inject
    @ServiceComponent(Component.EVENT_PROCESSOR)
    private Requester requester;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Inject
    private CourtHouseReverseLookup courtHouseReverseLookup;

    @Handles("hearing.outstanding-fines-requested")
    public void hearingOutstandingFinesRequested(final JsonEnvelope event) {

        final OutstandingFinesRequested outstandingFinesRequested = this.jsonObjectToObjectConverter.convert(
                event.payloadAsJsonObject(),
                OutstandingFinesRequested.class);

        LOGGER.info("hearing.outstanding-fines-requested event received with hearingDate {}", outstandingFinesRequested.getHearingDate());


        final Envelope<JsonObject> envelope = envelop(createObjectBuilder()
                .add("hearingDate", outstandingFinesRequested.getHearingDate().toString())
                .build()
        ).withName("hearing.defendant.outstanding-fine-requests").withMetadataFrom(event);

        final Envelope<JsonObject> hearingAccountQueryInformation = requester.requestAsAdmin(envelopeFrom(envelope.metadata(), envelope.payload()), JsonObject.class);
        final DefendantOutstandingFineRequestsResult defendantInfoQueryResult = this.jsonObjectToObjectConverter.convert(hearingAccountQueryInformation.payload(),
                DefendantOutstandingFineRequestsResult.class);

        if (defendantInfoQueryResult == null || defendantInfoQueryResult.getDefendantDetails() == null || defendantInfoQueryResult.getDefendantDetails().isEmpty()) {
            LOGGER.info("hearing.defendant.outstanding-fine-requests response Information is empty");
        } else {
            LOGGER.info("hearing.defendant.outstanding-fine-requests Query Information with Size {}",
                    defendantInfoQueryResult.getDefendantDetails() != null ? defendantInfoQueryResult.getDefendantDetails().size() : 0);

            final Map<String, String> courtCentreIdToOuCodeMap = courtHouseReverseLookup.getCourtRoomsResult(event).getOrganisationunits().stream()
                    .map(courtCentreOrganisationUnit -> Pair.of(courtCentreOrganisationUnit.getId(), courtCentreOrganisationUnit.getOucode()))
                    .filter(pair -> pair.getKey() != null && pair.getValue() != null)
                    .collect(Collectors.toMap(Pair::getKey, Pair::getValue));

            final RequestOutstandingFines requestOutstandingFines = new RequestOutstandingFines(
                    defendantInfoQueryResult.getDefendantDetails().stream()
                            .map(defendantDetail -> FineRequest.fineRequest()
                                    .withDefendantId(defendantDetail.getDefendantId())
                                    .withCaseId(defendantDetail.getCaseId())
                                    .withOuCode(courtCentreIdToOuCodeMap.get(defendantDetail.getCourtCentreId().toString()))
                                    .withDateOfHearing(defendantDetail.getDateOfHearing())
                                    .withTimeOfHearing(defendantDetail.getTimeOfHearing())
                                    .withFirstName(defendantDetail.getFirstName())
                                    .withLastName(defendantDetail.getLastName())
                                    .withDateOfBirth(defendantDetail.getDateOfBirth())
                                    .withNationalInsuranceNumber(defendantDetail.getNationalInsuranceNumber())
                                    .withLegalEntityDefendantName(defendantDetail.getLegalEntityDefendantName())
                                    .build())
                            .collect(Collectors.toList())
            );
            Lists.partition(requestOutstandingFines.getFineRequests(), outstandingFinesBatchSize).forEach(
                    fineRequests ->
                            this.sender.send(envelopeFrom(
                                    metadataFrom(event.metadata()).withName("stagingenforcement.request-outstanding-fine"),
                                    objectToJsonObjectConverter.convert(new RequestOutstandingFines(fineRequests))))
            );

        }

    }

}
