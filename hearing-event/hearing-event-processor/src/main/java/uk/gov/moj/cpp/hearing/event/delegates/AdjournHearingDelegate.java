package uk.gov.moj.cpp.hearing.event.delegates;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.relist.HearingAdjournTransformer;
import uk.gov.moj.cpp.hearing.event.relist.HearingAdjournValidator;
import uk.gov.moj.cpp.hearing.event.relist.RelistReferenceDataService;
import uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingResultDefinition;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.json.JsonObject;

@ServiceComponent(Component.EVENT_PROCESSOR)
@Named
public class AdjournHearingDelegate {

    private static final String PRIVATE_HEARING_COMMAND_ADJOURN_HEARING = "hearing.adjourn-hearing";

    @Inject
    private HearingAdjournValidator hearingAdjournValidator;

    @Inject
    private HearingAdjournTransformer hearingAdjournTransformer;

    @Inject
    private RelistReferenceDataService relistReferenceDataService;

    @Inject
    private Enveloper enveloper;

    @Inject
    private Sender sender;


    public void execute(final ResultsShared resultsShared, final JsonEnvelope jsonEnvelope) {
        final LocalDate hearingStartDate = resultsShared.getHearing().getHearingDays().stream()
                .map(ZonedDateTime::toLocalDate)
                .min(Comparator.comparing(LocalDate::toEpochDay))
                .orElse(LocalDate.now());
        final List<UUID> withdrawnResultDefinitionUuid = relistReferenceDataService.getWithdrawnResultDefinitionUuids(jsonEnvelope, hearingStartDate);
        final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions = relistReferenceDataService.getNextHearingResultDefinitions(jsonEnvelope, hearingStartDate);

        if (hearingAdjournValidator.validate(resultsShared, withdrawnResultDefinitionUuid, nextHearingResultDefinitions)) {
            final JsonObject adjournHearingRequestPayload = hearingAdjournTransformer.transform(resultsShared, nextHearingResultDefinitions);
            this.sender.send(this.enveloper.withMetadataFrom(jsonEnvelope, PRIVATE_HEARING_COMMAND_ADJOURN_HEARING).apply(adjournHearingRequestPayload));
        }

    }

    @Handles("hearing.hearing.adjourn-hearing-dummy")
    public void doesNothing(final JsonEnvelope jsonEnvelope) {
        // required by framework
    }
}
