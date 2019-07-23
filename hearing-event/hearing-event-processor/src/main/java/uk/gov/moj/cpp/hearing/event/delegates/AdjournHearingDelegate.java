package uk.gov.moj.cpp.hearing.event.delegates;

import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.HearingAdjourned;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.relist.HearingAdjournTransformer;
import uk.gov.moj.cpp.hearing.event.relist.HearingAdjournValidator;
import uk.gov.moj.cpp.hearing.event.relist.RelistReferenceDataService;
import uk.gov.moj.cpp.hearing.event.relist.ResultsSharedFilter;
import uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingResultDefinition;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.json.JsonObject;

import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:CallToDeprecatedMethod", "squid:S1612"})
@ServiceComponent(Component.EVENT_PROCESSOR)
@Named
public class AdjournHearingDelegate {

    private static final String PRIVATE_HEARING_COMMAND_ADJOURN_HEARING = "hearing.adjourn-hearing";
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AdjournHearingDelegate.class.getName());

    @Inject
    private HearingAdjournValidator hearingAdjournValidator;

    @Inject
    private HearingAdjournTransformer hearingAdjournTransformer;

    @Inject
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Inject
    private RelistReferenceDataService relistReferenceDataService;

    @Inject
    private Enveloper enveloper;

    @Inject
    private Sender sender;

    private ResultsSharedFilter resultsSharedFilter = new ResultsSharedFilter();

    public HearingAdjourned execute(final ResultsShared resultsShared, final JsonEnvelope jsonEnvelope) {
        final LocalDate orderedDate = resultsShared.getHearing().getHearingDays().stream()
                .map(HearingDay::getSittingDay)
                .map(ZonedDateTime::toLocalDate)
                .min(Comparator.comparing(LocalDate::toEpochDay))
                .orElse(LocalDate.now());
        final List<UUID> withdrawnResultDefinitionUuid = relistReferenceDataService.getWithdrawnResultDefinitionUuids(jsonEnvelope, orderedDate);
        final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions = relistReferenceDataService.getNextHearingResultDefinitions(jsonEnvelope, orderedDate);
        HearingAdjourned hearingAdjourned = null;

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(String.format("checking for adjournmenet  based on  %s non application targets, %s application targets,  withdrawnResultDefinitionUuid==%s, nextHearingResultDefinitions==%s ",
                    resultsShared.getTargets().stream().filter(target -> target.getApplicationId() == null).count(),
                    resultsShared.getTargets().stream().filter(target -> target.getApplicationId() != null).count(),
                    withdrawnResultDefinitionUuid.stream().map(uuid -> uuid.toString()).collect(Collectors.joining(",")),
                    nextHearingResultDefinitions.keySet().stream().map(uuid -> uuid.toString()).collect(Collectors.joining(","))
            ));
        }

        if (hearingAdjournValidator.validateProsecutionCase(resultsSharedFilter.filterTargets(resultsShared, target -> target.getApplicationId() == null), withdrawnResultDefinitionUuid, nextHearingResultDefinitions)) {
            final ResultsShared filteredResultsShared = resultsSharedFilter.filterTargets(resultsShared, target -> target.getApplicationId() == null);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(String.format("adjourning based on  %s non application targets ", filteredResultsShared.getTargets().size()
                ));
            }
            hearingAdjourned = hearingAdjournTransformer.transform2Adjournment(jsonEnvelope, filteredResultsShared, nextHearingResultDefinitions);
            final JsonObject adjournHearingRequestPayload = objectToJsonObjectConverter.convert(hearingAdjourned);
            this.sender.send(this.enveloper.withMetadataFrom(jsonEnvelope, PRIVATE_HEARING_COMMAND_ADJOURN_HEARING).apply(adjournHearingRequestPayload));
        }
        final ResultsShared filteredResultsShared = resultsSharedFilter.filterTargets(resultsShared, t -> t.getApplicationId() != null);
        final Set<UUID> uniqueApplicationIds = filteredResultsShared.getTargets().stream().map(Target::getApplicationId).filter(Objects::nonNull).collect(Collectors.toSet());
        LOGGER.info("uniqueApplicationIds {}", uniqueApplicationIds);
        for (final UUID applicationId : uniqueApplicationIds) {
            final ResultsShared filteredResultsSharedSingleApplication = resultsSharedFilter.filterTargets(filteredResultsShared, target -> target.getApplicationId().equals(applicationId));

            if (hearingAdjournValidator.validateApplication(filteredResultsSharedSingleApplication, nextHearingResultDefinitions)) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(String.format("adjourning based on  %s application targets ", filteredResultsSharedSingleApplication.getTargets().size()
                    ));
                }
                hearingAdjourned = hearingAdjournTransformer.transform2Adjournment(jsonEnvelope, filteredResultsSharedSingleApplication, nextHearingResultDefinitions);
                final JsonObject adjournHearingRequestPayload = objectToJsonObjectConverter.convert(hearingAdjourned);
                this.sender.send(this.enveloper.withMetadataFrom(jsonEnvelope, PRIVATE_HEARING_COMMAND_ADJOURN_HEARING).apply(adjournHearingRequestPayload));
            }
        }

        return hearingAdjourned;
    }

    @Handles("hearing.hearing.adjourn-hearing-dummy")
    public void doesNothing(final JsonEnvelope jsonEnvelope) {
        // required by framework
    }
}
