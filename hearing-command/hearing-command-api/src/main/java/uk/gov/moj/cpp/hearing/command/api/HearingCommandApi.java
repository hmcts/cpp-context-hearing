package uk.gov.moj.cpp.hearing.command.api;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.FeatureControl;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.api.service.ReferenceDataService;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ServiceComponent(COMMAND_API)
public class HearingCommandApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(HearingCommandApi.class);


    private static final String DEFENDANT_DETAILS_CHANGED_SHORT_CODE = "DDCH";

    private final Sender sender;
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter;
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter;
    private final ReferenceDataService referenceDataService;

    @Inject
    public HearingCommandApi(final Sender sender, final JsonObjectToObjectConverter jsonObjectToObjectConverter, final ObjectToJsonObjectConverter objectToJsonObjectConverter, final ReferenceDataService referenceDataService) {
        this.sender = sender;
        this.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
        this.objectToJsonObjectConverter = objectToJsonObjectConverter;
        this.referenceDataService = referenceDataService;
    }

    @Handles("hearing.initiate")
    public void initiateHearing(final JsonEnvelope envelope) {
        final InitiateHearingCommand command = verifyAndRemoveDDCHJudicialResult(envelope);

        this.sender.send(Enveloper.envelop(this.objectToJsonObjectConverter.convert(command))
                .withName("hearing.initiate")
                .withMetadataFrom(envelope));
    }

    @SuppressWarnings("pmd:NullAssignment")
    private InitiateHearingCommand verifyAndRemoveDDCHJudicialResult(final JsonEnvelope envelope) {
        final InitiateHearingCommand command = this.jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), InitiateHearingCommand.class);
        if (command.getHearing().getProsecutionCases() != null) {
            command.getHearing().getProsecutionCases().forEach(prosecutionCase ->
                    prosecutionCase.getDefendants().forEach(defendant -> {
                        if (!isEmpty(defendant.getDefendantCaseJudicialResults())) {
                            final ResultDefinition resultDefinition = referenceDataService.getResults(envelope, DEFENDANT_DETAILS_CHANGED_SHORT_CODE);
                            final List<JudicialResult> results = defendant.getDefendantCaseJudicialResults().stream().filter(judicialResult -> judicialResult.getJudicialResultTypeId().compareTo(resultDefinition.getId()) != 0).collect(Collectors.toList());
                            defendant.setDefendantCaseJudicialResults(results.isEmpty() ? null : results);
                        }
                    })
            );
        }
        return command;
    }

    @Handles("hearing.save-draft-result")
    public void saveDraftResult(final JsonEnvelope envelope) {
        if(LOGGER.isErrorEnabled()){LOGGER.error("INV: handling to command handler clienCorrelationId: {}" , envelope.metadata().clientCorrelationId().orElse(null));}
        sendEnvelopeWithName(envelope, "hearing.command.save-draft-result");
    }

    @Handles("hearing.save-multiple-draft-results")
    public void saveMultipleDraftResult(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.save-multiple-draft-results");
    }

    @Handles("hearing.save-draft-result-v2")
    public void saveDraftResultV2(final JsonEnvelope envelope) {
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error("INV: V2-handling to command handler clienCorrelationId: {}", envelope.metadata().clientCorrelationId().orElse(null));
        }
        sendEnvelopeWithName(envelope, "hearing.command.save-draft-result-v2");
    }

    @Handles("hearing.update-draft-result")
    public void updateDraftResult(final JsonEnvelope envelope) {
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error("INV: update-draft-result clienCorrelationId: {}", envelope.metadata().clientCorrelationId().orElse(null));
        }
        sendEnvelopeWithName(envelope, "hearing.command.update-draft-result");
    }

    @Handles("hearing.delete-draft-result-v2")
    public void deleteDraftResultV2(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.delete-draft-result-v2");
    }

    @FeatureControl("amendReshare")
    @Handles("hearing.save-days-draft-result")
    public void saveDraftResultForHearingDay(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.save-days-draft-result");
    }

    @FeatureControl("amendReshare")
    @Handles("hearing.save-days-draft-results")
    public void saveDraftResultsForHearingDay(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.save-days-draft-results");
    }

    @Handles("hearing.add-prosecution-counsel")
    public void addProsecutionCounsel(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.add-prosecution-counsel");
    }

    @Handles("hearing.remove-prosecution-counsel")
    public void removeProsecutionCounsel(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.remove-prosecution-counsel");
    }

    @Handles("hearing.update-prosecution-counsel")
    public void updateProsecutionCounsel(final JsonEnvelope envelope) {
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error("INV: will cause hearing.prosecution-counsel-added clienCorrelationId: {}", envelope.metadata().clientCorrelationId().orElse(null));
        }
        sendEnvelopeWithName(envelope, "hearing.command.update-prosecution-counsel");
    }

    @Handles("hearing.add-defence-counsel")
    public void addDefenceCounsel(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.add-defence-counsel");
    }

    @Handles("hearing.remove-defence-counsel")
    public void removeDefenceCounsel(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.remove-defence-counsel");
    }

    @Handles("hearing.update-defence-counsel")
    public void updateDefenceCounsel(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.update-defence-counsel");
    }

    @Handles("hearing.update-plea")
    public void updatePlea(final JsonEnvelope envelope) {
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error("INV: will cause hearing.hearing-offence-plea-updated clienCorrelationId: {}", envelope.metadata().clientCorrelationId().orElse(null));
        }
        sendEnvelopeWithName(envelope, "hearing.hearing-offence-plea-update");
    }

    @Handles("hearing.update-verdict")
    public void updateVerdict(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.update-verdict");
    }

    @Handles("hearing.generate-nows")
    public void generateNows(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.generate-nows");
    }

    @Handles("hearing.share-results")
    public void shareResults(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.share-results");
    }

    @Handles("hearing.share-results-v2")
    @FeatureControl("amendReshare")
    public void shareResultsV2(final JsonEnvelope envelope) {
       if(LOGGER.isErrorEnabled()){ LOGGER.error("INV: sharev2 - handling to command handler clienCorrelationId: {}" , envelope.metadata().clientCorrelationId().orElse(null));}
        sendEnvelopeWithName(envelope, "hearing.command.share-results-v2");
    }

    @Handles("hearing.share-days-results")
    @FeatureControl("amendReshare")
    public void shareResultsForHearingDay(final JsonEnvelope envelope) {
        if(LOGGER.isErrorEnabled()){LOGGER.error("INV: shareday - handling to command handler clienCorrelationId: {}" , envelope.metadata().clientCorrelationId().orElse(null));}
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error("INV: will cause hearing.events.results-shared-v3 clienCorrelationId: {}", envelope.metadata().clientCorrelationId().orElse(null));
        }
        sendEnvelopeWithName(envelope, "hearing.command.share-days-results");
    }

    @Handles("hearing.amend")
    public void amendHearing(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.amend");
    }

    @Handles("hearing.update-defendant-attendance-on-hearing-day")
    public void updateDefendantAttendance(final JsonEnvelope envelope) {
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error("INV: will cause hearing.defendant-attendance-updated clienCorrelationId: {}", envelope.metadata().clientCorrelationId().orElse(null));
        }
        sendEnvelopeWithName(envelope, "hearing.update-defendant-attendance-on-hearing-day");
    }

    @Handles("hearing.save-hearing-case-note")
    public void saveHearingCaseNote(final JsonEnvelope envelope) {
        if(LOGGER.isErrorEnabled()){ LOGGER.error("INV: addcasenote handling to command handler clienCorrelationId: {}" , envelope.metadata().clientCorrelationId().orElse(null));}
        sendEnvelopeWithName(envelope, "hearing.command.save-hearing-case-note");
    }

    @Handles("hearing.remove-respondent-counsel")
    public void removeRespondentCounsel(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.remove-respondent-counsel");
    }

    @Handles("hearing.update-respondent-counsel")
    public void updateRespondentCounsel(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.update-respondent-counsel");
    }

    @Handles("hearing.add-respondent-counsel")
    public void addRespondentCounsel(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.add-respondent-counsel");
    }

    @Handles("hearing.remove-applicant-counsel")
    public void removeApplicantCounsel(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.remove-applicant-counsel");
    }

    @Handles("hearing.update-applicant-counsel")
    public void updateApplicantCounsel(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.update-applicant-counsel");
    }

    @Handles("hearing.add-applicant-counsel")
    public void addApplicantCounsel(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.add-applicant-counsel");
    }

    @Handles("hearing.add-interpreter-intermediary")
    public void addInterpreterIntermediary(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.add-interpreter-intermediary");
    }

    @Handles("hearing.remove-interpreter-intermediary")
    public void removeInterpreterIntermediary(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.remove-interpreter-intermediary");
    }

    @Handles("hearing.update-interpreter-intermediary")
    public void updateInterpreterIntermediary(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.update-interpreter-intermediary");
    }

    @Handles("hearing.set-trial-type")
    public void setTrialType(final JsonEnvelope envelope) {
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error("INV: will cause hearing.hearing-effective-trial-set clienCorrelationId: {}", envelope.metadata().clientCorrelationId().orElse(null));
        }
        sendEnvelopeWithName(envelope, "hearing.command.set-trial-type");
    }

    @Handles("hearing.add-company-representative")
    public void addCompanyRepresentative(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.add-company-representative");
    }

    @Handles("hearing.update-company-representative")
    public void updateCompanyRepresentative(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.update-company-representative");
    }

    @Handles("hearing.remove-company-representative")
    public void removeCompanyRepresentative(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.remove-company-representative");
    }

    @Handles("hearing.publish-court-list")
    public void publishCourtList(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.publish-court-list");
    }

    @Handles("hearing.publish-hearing-lists-for-crown-courts")
    public void publishHearingListsForCrownCourts(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.publish-hearing-lists-for-crown-courts");
    }

    @Handles("hearing.compute-outstanding-fines")
    public void computeOutstandingFines(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.compute-outstanding-fines");
    }

    @Handles("hearing.book-provisional-hearing-slots")
    public void bookProvisionalHearingSlots(final JsonEnvelope envelope) {
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error("INV: will cause hearing.event.book-provisional-hearing-slots clienCorrelationId: {}", envelope.metadata().clientCorrelationId().orElse(null));
        }
        sendEnvelopeWithName(envelope, "hearing.command.book-provisional-hearing-slots");
    }

    @Handles("hearing.change-hearing-detail")
    public void updateHearingDetails(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.change-hearing-detail");
    }

    @Handles("hearing.remove-targets")
    public void removeTargets(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.remove-targets");
    }

    @Handles("hearing.add-master-defendant-id-to-defendant")
    public void addMasterDefendantIdToDefendant(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.add-master-defendant-id-to-defendant");
    }

    @Handles("hearing.mark-as-duplicate")
    public void markAsDuplicateHearing(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.mark-as-duplicate");
    }

    @Handles("hearing.request-approval")
    public void requestApproval(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.request-approval");
    }

    @Handles("hearing.validate-result-amendments")
    public void validateResultAmendments(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.validate-result-amendments");
    }

    @Handles("hearing.update-related-hearing")
    public void updateRelatedHearing(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.update-related-hearing");
    }

    @Handles("hearing.correct-hearing-days-without-court-centre")
    public void correctHearingDaysWithoutCourtCentre(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.correct-hearing-days-without-court-centre");
    }

    @Handles("hearing.update-resultline-shared-dates")
    public void updateResultLineSharedDates(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.update-resultline-shared-dates");
    }

    @Handles("hearing.change-cancel-amendments")
    public void cancelAmendments(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.change-cancel-amendments");
    }

    @Handles("hearing.remove-offences-from-existing-hearing")
    public void removeOffences(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.remove-offences-from-existing-hearing");
    }

    /**
     * Updates the original envelope with the new name and sends.
     *
     * @param envelope - the original envelope
     * @param name     - the updated name to insert into the envelope being sent
     */
    private void sendEnvelopeWithName(final JsonEnvelope envelope, final String name) {
        sender.send(Enveloper.envelop(envelope.payloadAsJsonObject())
                .withName(name)
                .withMetadataFrom(envelope));
    }
}