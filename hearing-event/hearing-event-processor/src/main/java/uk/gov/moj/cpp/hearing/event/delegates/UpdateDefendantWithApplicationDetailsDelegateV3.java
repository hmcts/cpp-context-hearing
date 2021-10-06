package uk.gov.moj.cpp.hearing.event.delegates;

import static java.util.Optional.of;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.CourtApplicationType;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV3;
import uk.gov.moj.cpp.hearing.nces.UpdateDefendantWithApplicationDetails;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import javax.inject.Inject;
import javax.json.JsonObject;

@SuppressWarnings("squid:CallToDeprecatedMethod")
public class UpdateDefendantWithApplicationDetailsDelegateV3 {

    private final Enveloper enveloper;
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Inject
    public UpdateDefendantWithApplicationDetailsDelegateV3(final Enveloper enveloper,
                                                           final ObjectToJsonObjectConverter objectToJsonObjectConverter) {
        this.enveloper = enveloper;
        this.objectToJsonObjectConverter = objectToJsonObjectConverter;
    }


    public void execute(final Sender sender, final JsonEnvelope event, final ResultsSharedV3 resultsShared) {
        Optional.of(resultsShared)
                .map(ResultsSharedV3::getHearing)
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


    private boolean hasAtLeastOneResultAndCase(final ResultsSharedV3 resultsShared, final UUID applicationId) {
        return resultsShared.getHearing().getProsecutionCases() != null &&
                !resultsShared.getHearing().getProsecutionCases().isEmpty() &&
                resultsShared.getTargets()
                        .stream()
                        .filter(target -> applicationId.equals(target.getApplicationId()))
                        .filter(target -> !target.getResultLines().isEmpty())
                        .flatMap(target -> target.getResultLines().stream())
                        .map(ResultLine2::getResultDefinitionId)
                        .anyMatch(Objects::nonNull);
    }

    private Optional<UUID> getDefendantId(final CourtApplication courtApplication) {
        return Optional.of(courtApplication)
                .map(CourtApplication::getApplicant)
                .map(CourtApplicationParty::getMasterDefendant)
                .map(MasterDefendant::getMasterDefendantId);
    }

    private Optional<UUID> getApplicationTypeId(final CourtApplication courtApplication) {
        return of(courtApplication)
                .map(CourtApplication::getType)
                .map(CourtApplicationType::getId);
    }



    private void sendNotify(final Sender sender, final JsonEnvelope event, final UpdateDefendantWithApplicationDetails updateDefendantWithApplicationDetails) {
        final JsonObject jsonObject = this.objectToJsonObjectConverter.convert(updateDefendantWithApplicationDetails);
        final Function<Object, JsonEnvelope> objectJsonEnvelopeFunction = this.enveloper.withMetadataFrom(event, "hearing.command.update-defendant-with-application-details");
        sender.sendAsAdmin(objectJsonEnvelopeFunction.apply(jsonObject));

    }
}