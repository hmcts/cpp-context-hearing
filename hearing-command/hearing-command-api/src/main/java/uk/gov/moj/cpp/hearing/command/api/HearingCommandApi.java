package uk.gov.moj.cpp.hearing.command.api;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;

import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.MetadataBuilder;
import uk.gov.moj.cpp.hearing.command.api.service.ReferenceDataService;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

@ServiceComponent(COMMAND_API)
public class HearingCommandApi {

    private static final String DEFENDANT_DETAILS_CHANGED_SHORT_CODE = "DDCH";

    private final Sender sender;
    private final Enveloper enveloper;
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter;
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter;
    private final ReferenceDataService referenceDataService;


    @Inject
    public HearingCommandApi(final Sender sender, final Enveloper enveloper, final JsonObjectToObjectConverter jsonObjectToObjectConverter, final ObjectToJsonObjectConverter objectToJsonObjectConverter, final ReferenceDataService referenceDataService) {
        this.sender = sender;
        this.enveloper = enveloper;
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
            command.getHearing().getProsecutionCases().stream().forEach(prosecutionCase ->
                    prosecutionCase.getDefendants().stream().forEach(defendant -> {
                        if (!isEmpty(defendant.getJudicialResults())) {
                            final ResultDefinition resultDefinition = referenceDataService.getResults(envelope, DEFENDANT_DETAILS_CHANGED_SHORT_CODE);
                            final List<JudicialResult> results = defendant.getJudicialResults().stream().filter(judicialResult -> judicialResult.getJudicialResultTypeId().compareTo(resultDefinition.getId()) != 0).collect(Collectors.toList());
                            defendant.setJudicialResults(results.isEmpty() ? null : results);
                        }
                    })
            );
        }
        return command;
    }

    @Handles("hearing.save-draft-result")
    public void saveDraftResult(final JsonEnvelope command) {
        this.sender.send(this.enveloper.withMetadataFrom(command, "hearing.command.save-draft-result").apply(command.payloadAsJsonObject()));
    }

    @Handles("hearing.application-draft-result")
    public void applicationDraftResult(final JsonEnvelope command) {
        this.sender.send(this.enveloper.withMetadataFrom(command, "hearing.command.application-draft-result").apply(command.payloadAsJsonObject()));
    }

    @Handles("hearing.add-prosecution-counsel")
    public void addProsecutionCounsel(final JsonEnvelope envelope) {
        this.sender.send(this.enveloper.withMetadataFrom(envelope, "hearing.command.add-prosecution-counsel").apply(envelope.payloadAsJsonObject()));
    }

    @Handles("hearing.remove-prosecution-counsel")
    public void removeProsecutionCounsel(final JsonEnvelope envelope) {
        this.sender.send(this.enveloper.withMetadataFrom(envelope, "hearing.command.remove-prosecution-counsel").apply(envelope.payloadAsJsonObject()));
    }

    @Handles("hearing.update-prosecution-counsel")
    public void updateProsecutionCounsel(final JsonEnvelope envelope) {
        this.sender.send(this.enveloper.withMetadataFrom(envelope, "hearing.command.update-prosecution-counsel").apply(envelope.payloadAsJsonObject()));
    }

    @Handles("hearing.add-defence-counsel")
    public void addDefenceCounsel(final JsonEnvelope envelope) {
        this.sender.send(this.enveloper.withMetadataFrom(envelope, "hearing.command.add-defence-counsel").apply(envelope.payloadAsJsonObject()));
    }

    @Handles("hearing.remove-defence-counsel")
    public void removeDefenceCounsel(final JsonEnvelope envelope) {
        this.sender.send(this.enveloper.withMetadataFrom(envelope, "hearing.command.remove-defence-counsel").apply(envelope.payloadAsJsonObject()));
    }

    @Handles("hearing.update-defence-counsel")
    public void updateDefenceCounsel(final JsonEnvelope envelope) {
        this.sender.send(this.enveloper.withMetadataFrom(envelope, "hearing.command.update-defence-counsel").apply(envelope.payloadAsJsonObject()));
    }

    @Handles("hearing.update-plea")
    public void updatePlea(final JsonEnvelope command) {
        this.sender.send(this.enveloper.withMetadataFrom(command, "hearing.hearing-offence-plea-update").apply(command.payloadAsJsonObject()));
    }

    @Handles("hearing.update-verdict")
    public void updateVerdict(final JsonEnvelope command) {
        this.sender.send(this.enveloper.withMetadataFrom(command, "hearing.command.update-verdict").apply(command.payloadAsJsonObject()));
    }

    @Handles("hearing.generate-nows")
    public void generateNows(final JsonEnvelope command) {
        this.sender.send(this.enveloper.withMetadataFrom(command, "hearing.command.generate-nows").apply(command.payloadAsJsonObject()));
    }

    @Handles("hearing.share-results")
    public void shareResults(final JsonEnvelope envelope) {
        this.sender.send(this.enveloper.withMetadataFrom(envelope, "hearing.command.share-results").apply(envelope.payloadAsJsonObject()));
    }

    @Handles("hearing.update-defendant-attendance-on-hearing-day")
    public void updateDefendantAttendance(final JsonEnvelope envelope) {
        this.sender.send(this.enveloper.withMetadataFrom(envelope, "hearing.update-defendant-attendance-on-hearing-day").apply(envelope.payloadAsJsonObject()));
    }

    @Handles("hearing.save-hearing-case-note")
    public void saveHearingCaseNote(final JsonEnvelope envelope) {
        this.sender.send(this.enveloper.withMetadataFrom(envelope, "hearing.command.save-hearing-case-note").apply(envelope.payloadAsJsonObject()));
    }

    @Handles("hearing.save-application-response")
    public void saveApplicationResponse(final JsonEnvelope envelope) {
        this.sender.send(this.enveloper.withMetadataFrom(envelope, "hearing.command.save-application-response").apply(envelope.payloadAsJsonObject()));
    }

    @Handles("hearing.remove-respondent-counsel")
    public void removeRespondentCounsel(final JsonEnvelope envelope) {
        this.sender.send(this.enveloper.withMetadataFrom(envelope, "hearing.command.remove-respondent-counsel").apply(envelope.payloadAsJsonObject()));
    }

    @Handles("hearing.update-respondent-counsel")
    public void updateRespondentCounsel(final JsonEnvelope envelope) {
        this.sender.send(this.enveloper.withMetadataFrom(envelope, "hearing.command.update-respondent-counsel").apply(envelope.payloadAsJsonObject()));
    }

    @Handles("hearing.add-respondent-counsel")
    public void addRespondentCounsel(final JsonEnvelope envelope) {
        this.sender.send(this.enveloper.withMetadataFrom(envelope, "hearing.command.add-respondent-counsel").apply(envelope.payloadAsJsonObject()));
    }

    @Handles("hearing.remove-applicant-counsel")
    public void removeApplicantCounsel(final JsonEnvelope envelope) {
        this.sender.send(this.enveloper.withMetadataFrom(envelope, "hearing.command.remove-applicant-counsel").apply(envelope.payloadAsJsonObject()));
    }

    @Handles("hearing.update-applicant-counsel")
    public void updateApplicantCounsel(final JsonEnvelope envelope) {
        this.sender.send(this.enveloper.withMetadataFrom(envelope, "hearing.command.update-applicant-counsel").apply(envelope.payloadAsJsonObject()));
    }

    @Handles("hearing.add-applicant-counsel")
    public void addApplicantCounsel(final JsonEnvelope envelope) {
        this.sender.send(this.enveloper.withMetadataFrom(envelope, "hearing.command.add-applicant-counsel").apply(envelope.payloadAsJsonObject()));
    }

    @Handles("hearing.add-interpreter-intermediary")
    public void addInterpreterIntermediary(final JsonEnvelope envelope) {
        this.sender.send(this.enveloper.withMetadataFrom(envelope, "hearing.command.add-interpreter-intermediary").apply(envelope.payloadAsJsonObject()));
    }

    @Handles("hearing.remove-interpreter-intermediary")
    public void removeInterpreterIntermediary(final JsonEnvelope envelope) {
        this.sender.send(this.enveloper.withMetadataFrom(envelope, "hearing.command.remove-interpreter-intermediary").apply(envelope.payloadAsJsonObject()));
    }

    @Handles("hearing.update-interpreter-intermediary")
    public void updateInterpreterIntermediary(final JsonEnvelope envelope) {
        this.sender.send(this.enveloper.withMetadataFrom(envelope, "hearing.command.update-interpreter-intermediary").apply(envelope.payloadAsJsonObject()));
    }

    @Handles("hearing.set-trial-type")
    public void setTrialType(final JsonEnvelope envelope) {
        final MetadataBuilder metadata = metadataFrom(envelope.metadata()).withName("hearing.command.set-trial-type");
        sender.send(envelopeFrom(metadata, envelope.payloadAsJsonObject()));
    }

    @Handles("hearing.add-company-representative")
    public void addCompanyRepresentative(final JsonEnvelope envelope) {
        final MetadataBuilder metadata = metadataFrom(envelope.metadata()).withName("hearing.command.add-company-representative");
        sender.send(envelopeFrom(metadata, envelope.payloadAsJsonObject()));
    }

    @Handles("hearing.update-company-representative")
    public void updateCompanyRepresentative(final JsonEnvelope envelope) {
        this.sender.send(this.enveloper.withMetadataFrom(envelope, "hearing.command.update-company-representative").apply(envelope.payloadAsJsonObject()));
    }

    @Handles("hearing.remove-company-representative")
    public void removeCompanyRepresentative(final JsonEnvelope envelope) {
        this.sender.send(this.enveloper.withMetadataFrom(envelope, "hearing.command.remove-company-representative").apply(envelope.payloadAsJsonObject()));
    }

    @Handles("hearing.publish-court-list")
    public void publishCourtList(final JsonEnvelope envelope) {
        this.sender.send(this.enveloper.withMetadataFrom(envelope, "hearing.command.publish-court-list").apply(envelope.payloadAsJsonObject()));
    }

    @Handles("hearing.publish-hearing-lists-for-crown-courts")
    public void publishHearingListsForCrownCourts(final JsonEnvelope envelope) {
        this.sender.send(this.enveloper.withMetadataFrom(envelope, "hearing.command.publish-hearing-lists-for-crown-courts").apply(envelope.payloadAsJsonObject()));
    }

    @Handles("hearing.compute-outstanding-fines")
    public void computeOutstandingFines(final JsonEnvelope envelope) {
        this.sender.send(this.enveloper.withMetadataFrom(envelope, "hearing.command.compute-outstanding-fines").apply(envelope.payloadAsJsonObject()));
    }

    @Handles("hearing.book-provisional-hearing-slots")
    public void bookProvisionalHearingSlots(final JsonEnvelope envelope) {
        this.sender.send(Enveloper.envelop(envelope.payloadAsJsonObject())
                .withName("hearing.command.book-provisional-hearing-slots")
                .withMetadataFrom(envelope));
    }
}