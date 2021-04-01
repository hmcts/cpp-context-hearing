package uk.gov.moj.cpp.hearing.event.delegates;

import static java.util.Optional.of;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.CourtApplicationType;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.nces.UpdateDefendantWithApplicationDetails;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import javax.inject.Inject;
import javax.json.JsonObject;

public class UpdateDefendantWithApplicationDetailsDelegate {

    private final Enveloper enveloper;
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Inject
    public UpdateDefendantWithApplicationDetailsDelegate(final Enveloper enveloper,
                                                         final ObjectToJsonObjectConverter objectToJsonObjectConverter) {
        this.enveloper = enveloper;
        this.objectToJsonObjectConverter = objectToJsonObjectConverter;
    }

    public void execute(Sender sender, JsonEnvelope event, ResultsShared resultsShared) {
        Optional.of(resultsShared)
                .map(ResultsShared::getHearing)
                .map(Hearing::getCourtApplications)
                .ifPresent(courtApplications ->
                        courtApplications.stream()
                                .filter(courtApplication -> hasAtLeastOneResultAndCase(resultsShared, courtApplication.getId()))
                                .map(this::getUpdateDefendantWithApplicationDetails)
                                .filter(Objects::nonNull)
                                .forEach(notification -> sendNotify(sender, event, notification))
                );
    }

    private UpdateDefendantWithApplicationDetails getUpdateDefendantWithApplicationDetails(final CourtApplication courtApplication) {
        final Optional<UUID> applicationTypeId = getApplicationTypeId(courtApplication);
        final Optional<UUID> defendantId = getDefendantId(courtApplication);

        if (applicationTypeId.isPresent()
                && defendantId.isPresent()) {
            return UpdateDefendantWithApplicationDetails.newBuilder()
                    .withDefendantId(defendantId.get())
                    .withApplicationTypeId(applicationTypeId.get())
                    .build();
        }
        return null;
    }

    private boolean hasAtLeastOneResultAndCase(ResultsShared resultsShared, UUID applicationId) {
        return resultsShared.getHearing().getProsecutionCases() != null &&
                !resultsShared.getHearing().getProsecutionCases().isEmpty() &&
                resultsShared.getTargets()
                        .stream()
                        .filter(target -> applicationId.equals(target.getApplicationId()))
                        .filter(target -> !target.getResultLines().isEmpty())
                        .flatMap(target -> target.getResultLines().stream())
                        .map(ResultLine::getResultDefinitionId)
                        .anyMatch(Objects::nonNull);
    }

    private Optional<UUID> getDefendantId(CourtApplication courtApplication) {
        return Optional.of(courtApplication)
                .map(CourtApplication::getApplicant)
                .map(CourtApplicationParty::getMasterDefendant)
                .map(MasterDefendant::getMasterDefendantId);
    }

    private Optional<UUID> getApplicationTypeId(CourtApplication courtApplication) {
        return of(courtApplication)
                .map(CourtApplication::getType)
                .map(CourtApplicationType::getId);
    }



    private void sendNotify(Sender sender, JsonEnvelope event, UpdateDefendantWithApplicationDetails updateDefendantWithApplicationDetails) {
        final JsonObject jsonObject = this.objectToJsonObjectConverter.convert(updateDefendantWithApplicationDetails);
        final Function<Object, JsonEnvelope> objectJsonEnvelopeFunction = this.enveloper.withMetadataFrom(event, "hearing.command.update-defendant-with-application-details");
        sender.sendAsAdmin(objectJsonEnvelopeFunction.apply(jsonObject));
    }
}