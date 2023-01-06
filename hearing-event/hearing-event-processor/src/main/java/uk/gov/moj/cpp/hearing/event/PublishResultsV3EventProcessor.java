package uk.gov.moj.cpp.hearing.event;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.justice.core.courts.Address.address;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.ContactNumber;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.CourtOrderOffence;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JudicialResultCategory;
import uk.gov.justice.core.courts.Level;
import uk.gov.justice.core.courts.LjaDetails;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutingAuthority;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.justice.hearing.courts.referencedata.OrganisationalUnit;
import uk.gov.justice.hearing.courts.referencedata.Prosecutor;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.common.ReferenceDataLoader;
import uk.gov.moj.cpp.hearing.domain.OffenceResult;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV3;
import uk.gov.moj.cpp.hearing.event.delegates.PublishResultsDelegateV3;
import uk.gov.moj.cpp.hearing.event.delegates.UpdateDefendantWithApplicationDetailsDelegateV3;
import uk.gov.moj.cpp.hearing.event.delegates.UpdateResultLineStatusDelegateV3;
import uk.gov.moj.cpp.hearing.event.delegates.exception.ResultDefinitionNotFoundException;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
@SuppressWarnings({"squid:S1188"})
public class PublishResultsV3EventProcessor {

    protected static final List<String> dismissedResultList = Collections.unmodifiableList(Arrays.asList("14d66587-8fbe-424f-a369-b1144f1684e3", "f8bd4d1f-1467-4903-b1e6-d2249ccc8c25", "8542b0d9-27f0-4df3-a4a3-0ac0a85c33ad"));
    protected static final List<String> withDrawnResultList = Collections.unmodifiableList(Arrays.asList("6feb0f2e-8d1e-40c7-af2c-05b28c69e5fc", "eb2e4c4f-b738-4a4d-9cce-0572cecb7cb8",
            "c8326b9e-56eb-406c-b74b-9f90c772b657", "eaecff82-32da-4cc1-b530-b55195485cc7", "4d5f25a5-9102-472f-a2da-c58d1eeb9c93"));
    private static final Logger LOGGER = LoggerFactory.getLogger(PublishResultsV3EventProcessor.class.getName());
    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private UpdateDefendantWithApplicationDetailsDelegateV3 updateDefendantWithApplicationDetailsDelegate;

    @Inject
    private UpdateResultLineStatusDelegateV3 updateResultLineStatusDelegate;

    @Inject
    private PublishResultsDelegateV3 publishResultsDelegate;

    @Inject
    private NewTargetToLegacyTargetConverter newTargetToLegacyTargetConverter;

    @Inject
    private Sender sender;

    @Inject
    private Enveloper enveloper;

    @Inject
    private ReferenceDataService referenceDataService;

    @Inject
    private ReferenceDataLoader referenceDataLoader;

    @Handles("hearing.events.results-shared-v3")
    public void resultsShared(final JsonEnvelope event) {
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error("INV: privateEventResultsSharedV3  clientCorrelationId: {}", event.metadata().clientCorrelationId().orElse(null));
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.events.results-shared-v3 event received {}", event.toObfuscatedDebugString());
        }

        final ResultsSharedV3 resultsShared = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ResultsSharedV3.class);


        resultsShared.setTargets(newTargetToLegacyTargetConverter.convert(resultsShared.getTargets()));




        final Hearing hearing = resultsShared.getHearing();

        final List<CourtApplication> courtApplications = ofNullable(hearing.getCourtApplications()).map(Collection::stream).orElseGet(Stream::empty).collect(toList());
        final List<ProsecutionCase> prosecutionCaseList = ofNullable(hearing.getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty).collect(toList());

        setProsecutorInformation(event, courtApplications, prosecutionCaseList);

        setCourtCentreOrganisationalUnitInfo(resultsShared.getHearing().getCourtCentre());

        setLJADetails(event, resultsShared.getHearing().getCourtCentre());

        ofNullable(resultsShared.getHearing().getProsecutionCases()).ifPresent(
                prosecutionCases ->
                        prosecutionCases
                                .forEach(prosecutionCase -> {
                                            populateProsecutorInformation(event, prosecutionCase.getProsecutionCaseIdentifier());
                                            prosecutionCase.getDefendants().forEach(defendant ->
                                                    {
                                                        final UUID caseId = prosecutionCase.getId();
                                                        final UUID defendantId = defendant.getId();
                                                        final List<UUID> offenceIds = defendant.getOffences().stream().map(Offence::getId).collect(toList());
                                                        final Map<UUID, OffenceResult> offenceResultMap = mapOffenceWithOffenceResult(event, defendant.getOffences(), resultsShared);
                                                        updateTheDefendantsCase(event, resultsShared.getHearing().getId(), caseId, defendantId, offenceIds, offenceResultMap);
                                                    }
                                            );
                                        }
                                ));


        if (isNotEmpty(courtApplications)) {
            // invoke command (resultsShared)
            updateDefendantWithApplicationDetailsDelegate.execute(sender, event, resultsShared);
        }

        LOGGER.info("requested target size {}, saved target size {}", resultsShared.getTargets().size(), resultsShared.getSavedTargets().size());
        LOGGER.info("combined target size {}", resultsShared.getTargets().size());
        publishResultsDelegate.shareResults(event, sender, resultsShared);

        updateResultLineStatusDelegate.updateDaysResultLineStatus(sender, event, resultsShared);

    }

    public void updateTheDefendantsCase(final JsonEnvelope event, final UUID hearingId, final UUID caseId, final UUID defendantId, final List<UUID> offenceIds, final Map<UUID, OffenceResult> offenceResultMap) {

        final JsonObject payload = Json.createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("caseId", caseId.toString())
                .add("defendantId", defendantId.toString())
                .add("offenceIds", convertToJsonArray(offenceIds))
                .add("resultedOffences", convertOffenceResultMapToJsonArray(offenceResultMap))
                .build();

        sender.send(envelop(payload)
                .withName("hearing.command.handler.update-offence-results")
                .withMetadataFrom(event));
    }

    private JsonArrayBuilder convertToJsonArray(final List<UUID> offenceIds) {
        final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        offenceIds.stream().map(UUID::toString).forEach(arrayBuilder::add);
        return arrayBuilder;
    }

    private JsonArrayBuilder convertOffenceResultMapToJsonArray(final Map<UUID, OffenceResult> offenceResultMap) {
        final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        offenceResultMap.entrySet().stream().forEach(offenceResult -> {
            final JsonObject offenceResultObject = Json.createObjectBuilder()
                    .add("offenceId", offenceResult.getKey().toString())
                    .add("offenceResult", offenceResult.getValue().name())
                    .build();
            arrayBuilder.add(offenceResultObject);
        });
        return arrayBuilder;
    }

    private Map<UUID, OffenceResult> mapOffenceWithOffenceResult(JsonEnvelope event, List<Offence> offences, ResultsSharedV3 resultsShared) {

        final Map<UUID, OffenceResult> offenceResultMap = new HashMap<>();

        offences.stream().forEach(offence -> {
            final List<ResultLine2> completedResultLinesForThisOffence = resultsShared.getTargets().stream()
                    .filter(target -> offence.getId().equals(target.getOffenceId()))
                    .flatMap(target -> target.getResultLines().stream())
                    .filter(r -> r.getLevel().equals(Level.OFFENCE))
                    .filter(ResultLine2::getIsComplete)
                    .filter(r -> (isNull(r.getIsDeleted()) || !r.getIsDeleted()))
                    .collect(toList());

            completedResultLinesForThisOffence.forEach(resultLine -> {
                final ResultDefinition resultDefinition = this.referenceDataService.getResultDefinitionById(event, resultLine.getOrderedDate(), resultLine.getResultDefinitionId());
                if (isNull(resultDefinition)) {
                    throw new ResultDefinitionNotFoundException(format(
                            "resultDefinition not found for resultLineId: %s, resultDefinitionId: %s, hearingId: %s orderedDate: %s",
                            resultLine.getResultLineId(), resultLine.getResultDefinitionId(), resultsShared.getHearing().getId(), resultLine.getOrderedDate()));
                }
                final JudicialResultCategory resultCategory = getCategory(resultDefinition);
                if (JudicialResultCategory.FINAL.equals(resultCategory)) {
                    offenceResultMap.put(offence.getId(), mapOffenceResult(resultDefinition));
                } else if (JudicialResultCategory.ANCILLARY.equals(resultCategory)
                        || JudicialResultCategory.INTERMEDIARY.equals(resultCategory)) {
                    offenceResultMap.put(offence.getId(), OffenceResult.ADJOURNED);
                } else {
                    throw new ResultDefinitionNotFoundException(format(
                            "don't know how to handle the category for not found for resultLineId: %s, resultDefinitionId: %s, hearingId: %s orderedDate: %s",
                            resultLine.getResultLineId(), resultLine.getResultDefinitionId(), resultsShared.getHearing().getId(), resultLine.getOrderedDate()));
                }
            });
        });
        return offenceResultMap;
    }

    private OffenceResult mapOffenceResult(final ResultDefinition resultDefinition) {
        if (dismissedResultList.contains(resultDefinition.getId().toString())) {
            return OffenceResult.DISMISSED;
        } else if (withDrawnResultList.contains(resultDefinition.getId().toString())) {
            return OffenceResult.WITHDRAWN;
        } else {
            return OffenceResult.GUILTY;
        }
    }

    private JudicialResultCategory getCategory(final ResultDefinition resultDefinition) {
        JudicialResultCategory category = null;

        if (nonNull(resultDefinition) && nonNull(resultDefinition.getCategory())) {

            switch (resultDefinition.getCategory()) {
                case "A":
                    category = JudicialResultCategory.ANCILLARY;
                    break;
                case "F":
                    category = JudicialResultCategory.FINAL;
                    break;
                case "I":
                    category = JudicialResultCategory.INTERMEDIARY;
                    break;
                default:
                    throw new IllegalArgumentException(format("No valid category found for result defnition %s", resultDefinition.getId().toString()));
            }
        }

        return category;
    }

    private void populateProsecutorInformation(final JsonEnvelope context, final ProsecutionCaseIdentifier prosecutionCaseIdentifier) {
        final Optional<Prosecutor> optionalProsecutor = fetchProsecutorInformationById(context, prosecutionCaseIdentifier.getProsecutionAuthorityId());
        optionalProsecutor.ifPresent(prosecutor -> {
            prosecutionCaseIdentifier.setProsecutionAuthorityName(prosecutor.getFullName());
            prosecutionCaseIdentifier.setProsecutionAuthorityOUCode(prosecutor.getOucode());
            prosecutionCaseIdentifier.setMajorCreditorCode(prosecutor.getMajorCreditorCode());
            if (nonNull(prosecutor.getAddress())) {
                prosecutionCaseIdentifier.setAddress(getProsecutorAddress(prosecutor));
            }
            if (nonNull(prosecutor.getInformantEmailAddress())) {
                prosecutionCaseIdentifier.setContact(getProsecutorContact(prosecutor));
            }
        });
    }

    private Address getProsecutorAddress(Prosecutor prosecutor) {
        final uk.gov.justice.hearing.courts.referencedata.Address prosecutorAddress = prosecutor.getAddress();
        return Address.address()
                .withAddress1(prosecutorAddress.getAddress1())
                .withAddress2(prosecutorAddress.getAddress2())
                .withAddress3(prosecutorAddress.getAddress3())
                .withAddress4(prosecutorAddress.getAddress4())
                .withAddress5(prosecutorAddress.getAddress5())
                .withPostcode(prosecutorAddress.getPostcode())
                .build();
    }

    private ContactNumber getProsecutorContact(Prosecutor prosecutor) {
        return ContactNumber.contactNumber()
                .withPrimaryEmail(prosecutor.getInformantEmailAddress())
                .build();
    }

    private void setLJADetails(final JsonEnvelope context, final CourtCentre courtCentre) {
        final UUID courtCentreId = courtCentre.getId();
        final LjaDetails ljaDetails = referenceDataService.getLjaDetails(context, courtCentreId);
        courtCentre.setLja(ljaDetails);
    }

    private void setCourtCentreOrganisationalUnitInfo(final CourtCentre courtCentre) {
        final OrganisationalUnit organisationalUnit = referenceDataLoader.getOrganisationUnitById(courtCentre.getId());

        courtCentre.setCode(organisationalUnit.getOucode());
        courtCentre.setCourtLocationCode(organisationalUnit.getCourtLocationCode());
        if (nonNull(organisationalUnit.getIsWelsh()) && organisationalUnit.getIsWelsh()) {
            courtCentre.setWelshName(organisationalUnit.getOucodeL3WelshName());
            courtCentre.setWelshCourtCentre(organisationalUnit.getIsWelsh());
            courtCentre.setWelshAddress(address()
                    .withAddress1(organisationalUnit.getWelshAddress1())
                    .withAddress2(organisationalUnit.getWelshAddress2())
                    .withAddress3(organisationalUnit.getWelshAddress3())
                    .withAddress4(organisationalUnit.getWelshAddress4())
                    .withAddress5(organisationalUnit.getWelshAddress5())
                    .withPostcode(organisationalUnit.getPostcode())
                    .build());
        }
        if (isNull(courtCentre.getAddress())) {
            courtCentre.setAddress(address()
                    .withAddress1(organisationalUnit.getAddress1())
                    .withAddress2(organisationalUnit.getAddress2())
                    .withAddress3(organisationalUnit.getAddress3())
                    .withAddress4(organisationalUnit.getAddress4())
                    .withAddress5(organisationalUnit.getAddress5())
                    .withPostcode(organisationalUnit.getPostcode())
                    .build());
        }
    }

    private void setProsecutorInformation(final JsonEnvelope context, final List<CourtApplication> courtApplications, final List<ProsecutionCase> prosecutionCases) {
        courtApplications.stream()
                .map(courtApplication -> ofNullable(courtApplication.getCourtApplicationCases()).map(Collection::stream).orElseGet(Stream::empty))
                .flatMap(courtApplicationCaseStream -> courtApplicationCaseStream.map(CourtApplicationCase::getProsecutionCaseIdentifier))
                .forEach(prosecutionCaseIdentifier -> populateProsecutorInformation(context, prosecutionCaseIdentifier));

        courtApplications.stream()
                .map(CourtApplication::getCourtOrder)
                .filter(Objects::nonNull)
                .map(courtOrder -> ofNullable(courtOrder.getCourtOrderOffences()).map(Collection::stream).orElseGet(Stream::empty))
                .flatMap(courtOrderOffenceStream -> courtOrderOffenceStream.map(CourtOrderOffence::getProsecutionCaseIdentifier))
                .forEach(prosecutionCaseIdentifier -> populateProsecutorInformation(context, prosecutionCaseIdentifier));

        courtApplications.forEach(courtApplication -> populateProsecutingAuthorityInformation(context, courtApplication));

        prosecutionCases.forEach(prosecutionCase -> populateProsecutorInformation(context, prosecutionCase.getProsecutionCaseIdentifier()));
    }

    private void populateProsecutingAuthorityInformation(final JsonEnvelope context, final CourtApplication courtApplication) {

        ofNullable(courtApplication.getApplicant()).ifPresent(courtApplicationParty -> setProsecutingAuthorityInformation(context, courtApplicationParty));

        ofNullable(courtApplication.getSubject()).ifPresent(courtApplicationParty -> setProsecutingAuthorityInformation(context, courtApplicationParty));

        ofNullable(courtApplication.getRespondents())
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .forEach(courtApplicationParty -> setProsecutingAuthorityInformation(context, courtApplicationParty));

        ofNullable(courtApplication.getThirdParties())
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .forEach(courtApplicationParty -> setProsecutingAuthorityInformation(context, courtApplicationParty));
    }

    private void setProsecutingAuthorityInformation(final JsonEnvelope context, final CourtApplicationParty courtApplicationParty) {

        if (nonNull(courtApplicationParty.getProsecutingAuthority())) {
            final ProsecutingAuthority prosecutingAuthority = courtApplicationParty.getProsecutingAuthority();
            final Optional<Prosecutor> optionalProsecutor = fetchProsecutorInformationById(context, prosecutingAuthority.getProsecutionAuthorityId());

            optionalProsecutor.ifPresent(prosecutor -> {
                prosecutingAuthority.setName(prosecutor.getFullName());
                if (nonNull(prosecutor.getAddress())) {
                    prosecutingAuthority.setAddress(getProsecutorAddress(prosecutor));
                }
                if (nonNull(prosecutor.getInformantEmailAddress())) {
                    prosecutingAuthority.setContact(getProsecutorContact(prosecutor));
                }
            });
        }
    }

    private Optional<Prosecutor> fetchProsecutorInformationById(final JsonEnvelope context, final UUID prosecutorId) {
        return Optional.ofNullable(referenceDataService.getProsecutorById(context, prosecutorId));
    }
}