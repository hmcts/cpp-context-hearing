package uk.gov.moj.cpp.hearing.event.delegates;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.justice.core.courts.JurisdictionType.MAGISTRATES;
import static uk.gov.justice.core.courts.Level.CASE;
import static uk.gov.justice.core.courts.Level.DEFENDANT;
import static uk.gov.justice.core.courts.Level.OFFENCE;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.CategoryEnumUtils.getCategory;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.TypeUtils.getBooleanValue;
import static uk.gov.moj.cpp.hearing.event.helper.HearingHelper.getOffencesFromHearing;
import static uk.gov.moj.cpp.util.DuplicateOffencesHelper.filterDuplicateOffencesById;
import static uk.gov.moj.cpp.util.ReportingRestrictionHelper.dedupReportingRestrictions;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.DefendantJudicialResult;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.JudicialResultCategory;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.OffenceFacts;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ReportingRestriction;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.justice.core.courts.VerdictType;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.PublicHearingResulted;
import uk.gov.moj.cpp.hearing.domain.event.result.PublicHearingResultedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV2;
import uk.gov.moj.cpp.hearing.event.delegates.helper.BailConditionsHelper;
import uk.gov.moj.cpp.hearing.event.delegates.helper.BailStatusHelper;
import uk.gov.moj.cpp.hearing.event.delegates.helper.BailStatusReasonHelper;
import uk.gov.moj.cpp.hearing.event.delegates.helper.OffenceHelper;
import uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.RestructuringHelper;
import uk.gov.moj.cpp.hearing.event.helper.ResultsSharedHelper;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.model.ExtendedCustodyTimeLimit;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.alcohollevel.AlcoholLevelMethod;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.relist.RelistReferenceDataService;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S1188", "squid:S1612", "squid:UnusedPrivateMethod"})
public class PublishResultsDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishResultsDelegate.class.getName());
    private static final String DDCH = "DDCH";
    private static final String PRESS_ON = "PressOn";

    private final Enveloper enveloper;

    private final ObjectToJsonObjectConverter objectToJsonObjectConverter;

    private final ReferenceDataService referenceDataService;

    private final RelistReferenceDataService relistReferenceDataService;

    private final BailStatusHelper bailStatusHelper;

    private final CustodyTimeLimitCalculator custodyTimeLimitCalculator;

    private final RestructuringHelper restructuringHelper;

    private final OffenceHelper offenceHelper;

    @Inject
    public PublishResultsDelegate(final Enveloper enveloper, final ObjectToJsonObjectConverter objectToJsonObjectConverter,
                                  final ReferenceDataService referenceDataService, final RelistReferenceDataService relistReferenceDataService,
                                  final CustodyTimeLimitCalculator custodyTimeLimitCalculator,
                                  final BailStatusHelper bailStatusHelper,
                                  final RestructuringHelper restructuringHelper,
                                  final OffenceHelper offenceHelper) {
        this.enveloper = enveloper;
        this.objectToJsonObjectConverter = objectToJsonObjectConverter;
        this.referenceDataService = referenceDataService;
        this.relistReferenceDataService = relistReferenceDataService;
        this.custodyTimeLimitCalculator = custodyTimeLimitCalculator;
        this.bailStatusHelper = bailStatusHelper;
        this.restructuringHelper = restructuringHelper;
        this.offenceHelper = offenceHelper;
    }

    public void shareResults(final JsonEnvelope context, final Sender sender, final ResultsShared resultsShared) {

        final List<TreeNode<ResultLine>> restructuredResults = this.restructuringHelper.restructure(context, resultsShared);

        mapApplicationLevelJudicialResults(resultsShared, restructuredResults);

        offenceHelper.enrichOffence(context, resultsShared.getHearing());

        mapDefendantLevelJudicialResults(resultsShared, restructuredResults);

        mapDefendantCaseLevelJudicialResults(resultsShared, restructuredResults);

        mapOffenceLevelJudicialResults(resultsShared, restructuredResults);

        mapAcquittalDate(resultsShared);

        bailStatusHelper.mapBailStatuses(context, resultsShared);

        this.custodyTimeLimitCalculator.calculate(resultsShared.getHearing());

        this.custodyTimeLimitCalculator.calculateDateHeldInCustody(resultsShared.getHearing());

        new ResultsSharedHelper().setIsDisposedFlagOnOffence(resultsShared);

        new BailStatusReasonHelper().setReason(resultsShared);

        new BailConditionsHelper().setBailConditions(resultsShared);

        new ResultsSharedHelper().cancelFutureHearingDays(context, sender, resultsShared, objectToJsonObjectConverter);

        if (isNotEmpty(resultsShared.getDefendantDetailsChanged())) {
            final Optional<LocalDate> orderedDate = getMaxOrderedDate(resultsShared.getTargets());
            mapDefendantLevelDDCHJudicialResults(resultsShared, relistReferenceDataService.getResults(context, DDCH), orderedDate);
        }

        final PublicHearingResulted hearingResulted = PublicHearingResulted.publicHearingResulted()
                .setHearing(resultsShared.getHearing())
                .setSharedTime(resultsShared.getSharedTime())
                .setShadowListedOffences(getOffenceShadowListedForMagistratesNextHearing(resultsShared));

        final JsonObject payload = this.objectToJsonObjectConverter.convert(hearingResulted);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Payload for event 'public.hearing.resulted': \n {}", payload);
        }

        sender.send(Enveloper.envelop(payload).withName("public.hearing.resulted").withMetadataFrom(context));
    }

    public void shareResults(final JsonEnvelope context, final Sender sender, final ResultsSharedV2 resultsShared) {

        final List<TreeNode<ResultLine>> restructuredResults = this.restructuringHelper.restructure(context, resultsShared);

        mapApplicationLevelJudicialResults(resultsShared, restructuredResults);

        offenceHelper.enrichOffence(context, resultsShared.getHearing());

        mapDefendantLevelJudicialResults(resultsShared, restructuredResults);

        mapDefendantCaseLevelJudicialResults(resultsShared, restructuredResults);

        mapOffenceLevelJudicialResults(resultsShared, restructuredResults);

        mapAcquittalDate(resultsShared);

        bailStatusHelper.mapBailStatuses(context, resultsShared.getHearing());

        this.custodyTimeLimitCalculator.calculate(resultsShared.getHearing());
        this.custodyTimeLimitCalculator.calculateDateHeldInCustody(resultsShared.getHearing(), resultsShared.getHearingDay());
        this.custodyTimeLimitCalculator.updateExtendedCustodyTimeLimit(resultsShared);

        new ResultsSharedHelper().setIsDisposedFlagOnOffence(resultsShared);
        new BailStatusReasonHelper().setReason(resultsShared.getHearing());
        new BailConditionsHelper().setBailConditions(resultsShared.getHearing());
        new ResultsSharedHelper().cancelFutureHearingDays(context, sender, resultsShared, objectToJsonObjectConverter);
        if (!isEmpty(resultsShared.getDefendantDetailsChanged())) {
            final Optional<LocalDate> orderedDate = getMaxOrderedDate(resultsShared.getTargets());
            mapDefendantLevelDDCHJudicialResults(resultsShared, relistReferenceDataService.getResults(context, DDCH), orderedDate);
        }

        final PublicHearingResultedV2 hearingResulted = PublicHearingResultedV2.publicHearingResultedV2()
                .setIsReshare(resultsShared.getIsReshare())
                .setHearing(resultsShared.getHearing())
                .setSharedTime(resultsShared.getSharedTime())
                .setHearingDay(resultsShared.getHearingDay())
                .setShadowListedOffences(getOffenceShadowListedForMagistratesNextHearing(resultsShared));

        final JsonObject jsonObject = this.objectToJsonObjectConverter.convert(hearingResulted);

        final JsonEnvelope jsonEnvelope = this.enveloper.withMetadataFrom(context, "public.events.hearing.hearing-resulted").apply(jsonObject);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Payload for event 'public.events.hearing.hearing-resulted': \n{}", jsonEnvelope);
        }
        sender.send(jsonEnvelope);
    }

    private void sendCommandToExtendCustodyTimeLimit(final JsonEnvelope context, final Sender sender, final ExtendedCustodyTimeLimit extendedCustodyTimeLimit) {

        final JsonObject payload = this.objectToJsonObjectConverter.convert(extendedCustodyTimeLimit);

         sender.send(envelop(payload)
                .withName("hearing.command.extend-custody-time-limit")
                .withMetadataFrom(context));

    }

    /**
     * Updates each verdict type with additional fields from reference data (such as verdict type
     * code).
     *
     * @param offence      - the offence to be updated.
     * @param verdictTypes - the full set of verdict types from refrencedata.
     */
    private void populateFullVerdictTypeData(final Offence offence, final List<VerdictType> verdictTypes) {
        final Verdict originalVerdict = offence.getVerdict();

        final Optional<VerdictType> fullVerdictType = verdictTypes.stream()
                .filter(verdictType -> verdictType.getId().equals(originalVerdict.getVerdictType().getId()))
                .findFirst();

        fullVerdictType.ifPresent(verdictType -> offence.setVerdict(Verdict.verdict()
                .withVerdictType(verdictType)
                .withJurors(originalVerdict.getJurors())
                .withOffenceId(originalVerdict.getOffenceId())
                .withVerdictDate(originalVerdict.getVerdictDate())
                .withLesserOrAlternativeOffence(originalVerdict.getLesserOrAlternativeOffence())
                .withOriginatingHearingId(originalVerdict.getOriginatingHearingId())
                .build()));
    }

    /**
     * Updates each offence fact with additional fields from reference data (such as alcohol level
     * method description).
     *
     * @param offence             - the offence to be updated.
     * @param alcoholLevelMethods - the full set of alcohol level methods from refrencedata.
     */
    private void populateAlcoholLevelMethodData(final Offence offence, final List<AlcoholLevelMethod> alcoholLevelMethods) {
        final OffenceFacts originalOffenceFacts = offence.getOffenceFacts();

        final Optional<AlcoholLevelMethod> fullAlcoholLevelMethod = alcoholLevelMethods.stream()
                .filter(alm -> alm.getMethodCode().equals(originalOffenceFacts.getAlcoholReadingMethodCode()))
                .findFirst();

        fullAlcoholLevelMethod.ifPresent(alcoholLevelMethod -> offence.setOffenceFacts(OffenceFacts.offenceFacts()
                .withAlcoholReadingMethodDescription(alcoholLevelMethod.getMethodDescription())
                .withAlcoholReadingAmount(originalOffenceFacts.getAlcoholReadingAmount())
                .withAlcoholReadingMethodCode(originalOffenceFacts.getAlcoholReadingMethodCode())
                .withVehicleCode(originalOffenceFacts.getVehicleCode())
                .withVehicleMake(originalOffenceFacts.getVehicleMake())
                .withVehicleRegistration(originalOffenceFacts.getVehicleRegistration())
                .build()));
    }

    private List<UUID> getOffenceShadowListedForMagistratesNextHearing(final ResultsShared resultsShared) {
        if (isNotEmpty(resultsShared.getHearing().getProsecutionCases()) && resultsShared.getHearing().getProsecutionCases().stream().flatMap(x -> x.getDefendants().stream())
                .flatMap(def -> def.getOffences() != null ? def.getOffences().stream() : Stream.empty())
                .flatMap(off -> off.getJudicialResults() != null ? off.getJudicialResults().stream() : Stream.empty())
                .map(JudicialResult::getNextHearing)
                .filter(Objects::nonNull).anyMatch(nh -> MAGISTRATES == nh.getJurisdictionType())) {
            return resultsShared.getTargets().stream().filter(t -> TRUE.equals(t.getShadowListed())).map(Target::getOffenceId).collect(Collectors.toList());
        }
        return emptyList();
    }

    private List<UUID> getOffenceShadowListedForMagistratesNextHearing(final ResultsSharedV2 resultsShared) {
        if (isNotEmpty(resultsShared.getHearing().getProsecutionCases()) && resultsShared.getHearing().getProsecutionCases().stream().flatMap(x -> x.getDefendants().stream())
                .flatMap(def -> def.getOffences() != null ? def.getOffences().stream() : Stream.empty())
                .flatMap(off -> off.getJudicialResults() != null ? off.getJudicialResults().stream() : Stream.empty())
                .filter(jr -> jr.getNextHearing() != null)
                .map(JudicialResult::getNextHearing).anyMatch(nh -> MAGISTRATES == nh.getJurisdictionType())) {
            return resultsShared.getTargets().stream().filter(t -> TRUE.equals(t.getShadowListed())).map(Target::getOffenceId).collect(Collectors.toList());
        }
        return emptyList();
    }

    private void mapDefendantLevelDDCHJudicialResults(final ResultsShared resultsShared, final ResultDefinition resultDefinition, Optional<LocalDate> orderedDate) {
        final Stream<ProsecutionCase> prosecutionCaseStream = ofNullable(resultsShared.getHearing().getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty);
        prosecutionCaseStream
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .forEach(defendant -> {
                    if (resultsShared.getDefendantDetailsChanged().contains(defendant.getId())) {
                        final JudicialResult judicialResult = createDDCHJudicialResult(resultsShared.getHearing().getId(), resultDefinition, orderedDate);
                        judicialResult.setJudicialResultPrompts(null);
                        judicialResult.setNextHearing(null);
                        final List<JudicialResult> judicialResults = defendant.getDefendantCaseJudicialResults();
                        if (isEmpty(judicialResults)) {
                            defendant.setDefendantCaseJudicialResults(singletonList(judicialResult));
                        } else {
                            judicialResults.add(judicialResult);
                            defendant.setDefendantCaseJudicialResults(judicialResults);
                        }
                    }
                });
    }

    private void mapDefendantLevelDDCHJudicialResults(final ResultsSharedV2 resultsShared, final ResultDefinition resultDefinition, Optional<LocalDate> orderedDate) {
        resultsShared.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .forEach(defendant -> {
                    if (resultsShared.getDefendantDetailsChanged().contains(defendant.getId())) {
                        final JudicialResult judicialResult = createDDCHJudicialResult(resultsShared.getHearing().getId(), resultDefinition, orderedDate);
                        judicialResult.setJudicialResultPrompts(null);
                        judicialResult.setNextHearing(null);
                        final List<JudicialResult> judicialResults = defendant.getDefendantCaseJudicialResults();
                        if (isEmpty(judicialResults)) {
                            defendant.setDefendantCaseJudicialResults(singletonList(judicialResult));
                        } else {
                            judicialResults.add(judicialResult);
                            defendant.setDefendantCaseJudicialResults(judicialResults);
                        }
                    }
                });
    }

    private JudicialResult createDDCHJudicialResult(final UUID hearingId, final ResultDefinition resultDefinition, Optional<LocalDate> orderedDate) {
        return JudicialResult.judicialResult()
                .withJudicialResultId(randomUUID())
                .withJudicialResultTypeId(resultDefinition.getId())
                .withCategory(getCategory(resultDefinition.getCategory()))
                .withCjsCode(resultDefinition.getCjsCode())
                .withIsAdjournmentResult(resultDefinition.isAdjournment())
                .withIsAvailableForCourtExtract(resultDefinition.getIsAvailableForCourtExtract())
                .withIsConvictedResult(resultDefinition.isConvicted())
                .withIsFinancialResult(ResultDefinition.YES.equalsIgnoreCase(resultDefinition.getFinancial()))
                .withIsUnscheduled(resultDefinition.getUnscheduled())
                .withLabel(resultDefinition.getLabel())
                .withLastSharedDateTime(LocalDate.now().toString())
                .withOrderedDate(orderedDate.orElse(LocalDate.now()))
                .withOrderedHearingId(hearingId)
                .withRank(isNull(resultDefinition.getRank()) ? BigDecimal.ZERO : new BigDecimal(resultDefinition.getRank()))
                .withUsergroups(resultDefinition.getUserGroups())
                .withWelshLabel(resultDefinition.getWelshLabel())
                .withResultText(resultDefinition.getLabel())
                .withLifeDuration(getBooleanValue(resultDefinition.getLifeDuration(), false))
                .withResultDefinitionGroup(resultDefinition.getResultDefinitionGroup())
                .withTerminatesOffenceProceedings(getBooleanValue(resultDefinition.getTerminatesOffenceProceedings(), false))
                .withPublishedAsAPrompt(getBooleanValue(resultDefinition.getPublishedAsAPrompt(), false))
                .withExcludedFromResults(getBooleanValue(resultDefinition.getExcludedFromResults(), false))
                .withAlwaysPublished(getBooleanValue(resultDefinition.getAlwaysPublished(), false))
                .withUrgent(getBooleanValue(resultDefinition.getUrgent(), false))
                .withD20(getBooleanValue(resultDefinition.getD20(), false))
                .withRollUpPrompts(resultDefinition.getRollUpPrompts())
                .withPublishedForNows(resultDefinition.getPublishedForNows())
                .withResultWording(resultDefinition.getResultWording())
                .withWelshResultWording(resultDefinition.getWelshResultWording())
                .build();
    }

    private void mapApplicationLevelJudicialResults(final ResultsSharedV2 resultsShared, final List<TreeNode<ResultLine>> results) {

        if (nonNull(resultsShared.getHearing().getCourtApplications())) {
            resultsShared.getHearing().getCourtApplications()
                    .forEach(courtApplication -> {
                        updateApplicationLevelJudicialResults(results, courtApplication);

                        ofNullable(courtApplication.getCourtApplicationCases()).map(Collection::stream).orElseGet(Stream::empty)
                                .flatMap(courtApplicationCase -> ofNullable(courtApplicationCase.getOffences()).map(Collection::stream).orElseGet(Stream::empty))
                                .forEach(courtApplicationOffence -> {
                                    final Optional<Offence> offenceOptional = ofNullable(courtApplicationOffence);
                                    if (offenceOptional.isPresent()) {
                                        final Offence offence = offenceOptional.get();
                                        updateApplicationOffenceJudicialResults(results, courtApplication, offence);
                                    }
                                });

                        if (nonNull(courtApplication.getCourtOrder())) {
                            ofNullable(courtApplication.getCourtOrder().getCourtOrderOffences()).map(Collection::stream).orElseGet(Stream::empty)
                                    .forEach(courtOrderOffence -> {
                                        final Offence offence = courtOrderOffence.getOffence();
                                        updateApplicationOffenceJudicialResults(results, courtApplication, offence);
                                    });
                        }
                    });
        }
    }

    private void mapApplicationLevelJudicialResults(final ResultsShared resultsShared, final List<TreeNode<ResultLine>> results) {
        if (nonNull(resultsShared.getHearing().getCourtApplications())) {
            resultsShared.getHearing().getCourtApplications()
                    .forEach(courtApplication -> {
                        updateApplicationLevelJudicialResults(results, courtApplication);

                        ofNullable(courtApplication.getCourtApplicationCases()).map(Collection::stream).orElseGet(Stream::empty)
                                .flatMap(courtApplicationCase -> ofNullable(courtApplicationCase.getOffences()).map(Collection::stream).orElseGet(Stream::empty))
                                .forEach(courtApplicationOffence -> {
                                    final Optional<Offence> offenceOptional = ofNullable(courtApplicationOffence);
                                    if (offenceOptional.isPresent()) {
                                        final Offence offence = offenceOptional.get();
                                        updateApplicationOffenceJudicialResults(results, courtApplication, offence);
                                    }
                                });

                        if (nonNull(courtApplication.getCourtOrder())) {
                            ofNullable(courtApplication.getCourtOrder().getCourtOrderOffences()).map(Collection::stream).orElseGet(Stream::empty)
                                    .forEach(courtOrderOffence -> {
                                        final Offence offence = courtOrderOffence.getOffence();
                                        updateApplicationOffenceJudicialResults(results, courtApplication, offence);
                                    });
                        }
                    });
        }
    }

    private void updateApplicationOffenceJudicialResults(List<TreeNode<ResultLine>> results, CourtApplication courtApplication, Offence offence) {
        final List<JudicialResult> applicationOffenceJudicialResults = getApplicationOffenceJudicialResults(results, courtApplication.getId(), offence.getId());
        if (isNotEmpty(applicationOffenceJudicialResults)) {
            setPromptsAsNullIfEmpty(applicationOffenceJudicialResults);
            offence.setJudicialResults(applicationOffenceJudicialResults);
        }
    }

    private void updateApplicationLevelJudicialResults(List<TreeNode<ResultLine>> results, CourtApplication courtApplication) {
        final List<JudicialResult> judicialResults = getApplicationLevelJudicialResults(results, courtApplication.getId());
        if (isNotEmpty(judicialResults)) {
            setPromptsAsNullIfEmpty(judicialResults);
            courtApplication.setJudicialResults(judicialResults);
        }
    }

    private List<JudicialResult> getApplicationOffenceJudicialResults(List<TreeNode<ResultLine>> results, final UUID applicationId, final UUID offenceId) {
        return results.stream()
                .filter(node -> nonNull(node.getApplicationId()) && applicationId.equals(node.getApplicationId()) && nonNull(node.getOffenceId()) && offenceId.equals(node.getOffenceId()))
                .map(TreeNode::getJudicialResult)
                .collect(toList());
    }


    private List<JudicialResult> getApplicationLevelJudicialResults(final List<TreeNode<ResultLine>> results, final UUID id) {
        return results.stream()
                .filter(node -> nonNull(node.getApplicationId()) && id.equals(node.getApplicationId()) && isNull(node.getOffenceId()))
                .map(TreeNode::getJudicialResult).collect(toList());
    }

    private void mapDefendantCaseLevelJudicialResults(final ResultsShared resultsShared, final List<TreeNode<ResultLine>> results) {
        final Stream<ProsecutionCase> prosecutionCaseStream = ofNullable(resultsShared.getHearing().getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty);
        prosecutionCaseStream.flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream()).forEach(defendant -> {
            final List<JudicialResult> judicialResults = getDefendantCaseJudicialResults(results, defendant.getId());
            if (isNotEmpty(judicialResults)) {
                setPromptsAsNullIfEmpty(judicialResults);
                defendant.setDefendantCaseJudicialResults(judicialResults);
            } else {
                defendant.setDefendantCaseJudicialResults(null);
            }
        });
    }

    private void mapDefendantCaseLevelJudicialResults(final ResultsSharedV2 resultsShared, final List<TreeNode<ResultLine>> results) {
        final Stream<ProsecutionCase> prosecutionCaseStream = ofNullable(resultsShared.getHearing().getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty);
        prosecutionCaseStream.flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream()).forEach(defendant -> {
            final List<JudicialResult> judicialResults = getDefendantCaseJudicialResults(results, defendant.getId());
            if (isNotEmpty(judicialResults)) {
                setPromptsAsNullIfEmpty(judicialResults);
                defendant.setDefendantCaseJudicialResults(judicialResults);
            } else {
                defendant.setDefendantCaseJudicialResults(null);
            }
        });
    }

    private void mapDefendantLevelJudicialResults(final ResultsShared resultsShared, final List<TreeNode<ResultLine>> results) {
        final Stream<ProsecutionCase> prosecutionCaseStream = ofNullable(resultsShared.getHearing().getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty);
        final List<DefendantJudicialResult> defendantJudicialResults = prosecutionCaseStream.flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream()).map(defendant -> {
            final List<JudicialResult> judicialResults = getDefendantJudicialResults(results, defendant.getId());
            if (isNotEmpty(judicialResults)) { //so that judicialResults doesn't have empty tag
                setPromptsAsNullIfEmpty(judicialResults);
                return buildDefendantJudicialResults(defendant.getMasterDefendantId(), judicialResults);
            }
            return null;
        })
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(toList());

        if (isNotEmpty(defendantJudicialResults)) {
            setDefendantJudicialResultPromptsAsNullIfEmpty(defendantJudicialResults);
            resultsShared.getHearing().setDefendantJudicialResults(defendantJudicialResults);
        } else {
            resultsShared.getHearing().setDefendantJudicialResults(null);
        }
    }

    private void mapDefendantLevelJudicialResults(final ResultsSharedV2 resultsShared, final List<TreeNode<ResultLine>> results) {
        final Stream<ProsecutionCase> prosecutionCaseStream = ofNullable(resultsShared.getHearing().getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty);
        final List<DefendantJudicialResult> defendantJudicialResults = prosecutionCaseStream.flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .map(defendant -> {
                    final List<JudicialResult> judicialResults = getDefendantJudicialResults(results, defendant.getId());
                    if (isNotEmpty(judicialResults)) { //so that judicialResults doesn't have empty tag
                        setPromptsAsNullIfEmpty(judicialResults);
                        return buildDefendantJudicialResults(defendant.getMasterDefendantId(), judicialResults);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(toList());

        if (isNotEmpty(defendantJudicialResults)) {
            setDefendantJudicialResultPromptsAsNullIfEmpty(defendantJudicialResults);
            resultsShared.getHearing().setDefendantJudicialResults(defendantJudicialResults);
        } else {
            resultsShared.getHearing().setDefendantJudicialResults(null);
        }
    }

    private void setDefendantJudicialResultPromptsAsNullIfEmpty(List<DefendantJudicialResult> defendantJudicialResults) {
        if (isNotEmpty(defendantJudicialResults)) {
            for (final DefendantJudicialResult defendantJudicialResult : defendantJudicialResults) {
                setJudicialResultPromptsAsNull(defendantJudicialResult.getJudicialResult());
            }
        }
    }

    private List<DefendantJudicialResult> buildDefendantJudicialResults(UUID masterDefendantId, List<JudicialResult> judicialResults) {
        return judicialResults.stream().map(judicialResult -> DefendantJudicialResult.defendantJudicialResult()
                .withJudicialResult(judicialResult)
                .withMasterDefendantId(masterDefendantId)
                .build()).collect(toList());
    }

    private List<JudicialResult> getDefendantJudicialResults(final List<TreeNode<ResultLine>> results, final UUID id) {
        return results.stream()
                .filter(node -> node.getLevel() == DEFENDANT)
                .filter(node -> nonNull(node.getDefendantId()) && id.equals(node.getDefendantId()))
                .map(node -> node.getJudicialResult().setOffenceId(node.getOffenceId()))
                .collect(toList());
    }

    private List<JudicialResult> getDefendantCaseJudicialResults(final List<TreeNode<ResultLine>> results, final UUID id) {
        return results.stream()
                .filter(node -> node.getLevel() == CASE)
                .filter(node -> nonNull(node.getDefendantId()) && id.equals(node.getDefendantId()))
                .map(node -> node.getJudicialResult().setOffenceId(node.getOffenceId()))
                .collect(toList());
    }

    private void mapOffenceLevelJudicialResults(final ResultsShared resultsShared, final List<TreeNode<ResultLine>> results) {
        ofNullable(resultsShared.getHearing().getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty)
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream()).forEach(d -> filterDuplicateOffencesById(d.getOffences()));

        final List<Offence> offences = ofNullable(resultsShared.getHearing().getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty)
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .flatMap(defendant -> defendant.getOffences().stream()).collect(toList());

        offences.forEach(offence -> {

            final List<JudicialResult> judicialResults = getOffenceLevelJudicialResults(results, offence.getId());
            final List<ReportingRestriction> restrictions = new ArrayList<>();

            if (!judicialResults.isEmpty()) { //so that judicialResults doesn't have empty tag
                setPromptsAsNullIfEmpty(judicialResults);
                offence.setJudicialResults(judicialResults);
                judicialResults.forEach(result -> {
                    if (PRESS_ON.equalsIgnoreCase(result.getResultDefinitionGroup())) {
                        final ReportingRestriction reportingRestriction = ReportingRestriction.reportingRestriction()
                                .withId(UUID.randomUUID())
                                .withJudicialResultId(result.getJudicialResultId())
                                .withLabel(result.getLabel())
                                .withOrderedDate(result.getOrderedDate())
                                .build();
                        restrictions.add(reportingRestriction);

                    }
                });

                if (isNotEmpty(offence.getReportingRestrictions())) {
                    restrictions.addAll(offence.getReportingRestrictions());
                }

                if (!restrictions.isEmpty()) {
                    offence.setReportingRestrictions(dedupReportingRestrictions(restrictions));
                }

            } else {
                offence.setJudicialResults(null);
            }
        });
    }

    private void mapOffenceLevelJudicialResults(final ResultsSharedV2 resultsShared, final List<TreeNode<ResultLine>> results) {
        final List<Offence> offences = ofNullable(resultsShared.getHearing().getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty)
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .flatMap(defendant -> defendant.getOffences().stream()).collect(toList());

        offences.forEach(offence -> {
                    final List<JudicialResult> judicialResults = getOffenceLevelJudicialResults(results, offence.getId());
                    final List<ReportingRestriction> restrictions = new ArrayList<>();

                    if (!judicialResults.isEmpty()) { //so that judicialResults doesn't have empty tag
                        setPromptsAsNullIfEmpty(judicialResults);
                        offence.setJudicialResults(judicialResults);
                        judicialResults.forEach(result -> {
                            if (PRESS_ON.equalsIgnoreCase(result.getResultDefinitionGroup())) {
                                final ReportingRestriction reportingRestriction = ReportingRestriction.reportingRestriction()
                                        .withId(UUID.randomUUID())
                                        .withJudicialResultId(result.getJudicialResultId())
                                        .withLabel(result.getLabel())
                                        .withOrderedDate(result.getOrderedDate())
                                        .build();
                                restrictions.add(reportingRestriction);

                            }
                        });

                        if (isNotEmpty(offence.getReportingRestrictions())) {
                            restrictions.addAll(offence.getReportingRestrictions());
                        }

                        if (!restrictions.isEmpty()) {
                            offence.setReportingRestrictions(dedupReportingRestrictions(restrictions));
                        }
                    } else {
                        offence.setJudicialResults(null);
                    }
                });
    }


    private void setPromptsAsNullIfEmpty(final List<JudicialResult> judicialResults) {
        if (isNotEmpty(judicialResults)) {
            for (final JudicialResult judicialResult : judicialResults) {
                if (isEmpty(judicialResult.getJudicialResultPrompts())) {
                    judicialResult.setJudicialResultPrompts(null);
                }
            }
        }
    }

    private void setJudicialResultPromptsAsNull(JudicialResult judicialResult) {
        if (isEmpty(judicialResult.getJudicialResultPrompts())) {
            judicialResult.setJudicialResultPrompts(null);
        }
    }

    private List<JudicialResult> getOffenceLevelJudicialResults(final List<TreeNode<ResultLine>> results, final UUID id) {
        return results.stream()
                .filter(node -> node.getLevel() == OFFENCE)
                .filter(node -> nonNull(node.getOffenceId()) && id.equals(node.getOffenceId()))
                .map(TreeNode::getJudicialResult).collect(toList());
    }

    private void mapAcquittalDate(final ResultsShared resultsShared) {
        final Set<String> guiltyPleaTypes = referenceDataService.retrieveGuiltyPleaTypes();
        final List<Offence> offences = getOffencesFromHearing(resultsShared.getHearing());

        offences.stream()
                .filter(offence -> isValidToSetAcquittalDate(offence, guiltyPleaTypes))
                .forEach(offence -> getMaxOrderDate(offence.getJudicialResults()).ifPresent(offence::setAquittalDate));
    }

    private void mapAcquittalDate(final ResultsSharedV2 resultsShared) {
        final Set<String> guiltyPleaTypes = referenceDataService.retrieveGuiltyPleaTypes();
        final List<Offence> offences = getOffencesFromHearing(resultsShared.getHearing());

        offences.stream()
                .filter(offence -> isValidToSetAcquittalDate(offence, guiltyPleaTypes))
                .forEach(offence -> getMaxOrderDate(offence.getJudicialResults()).ifPresent(offence::setAquittalDate));
    }

    private boolean isValidToSetAcquittalDate(final Offence offence, final Set<String> guiltyPleaTypes) {
        return isNull(offence.getAquittalDate()) &&
                isNotGuiltyPlea(offence, guiltyPleaTypes) &&
                hasFinalResult(offence.getJudicialResults()) &&
                isNull(offence.getConvictionDate());
    }

    private boolean isNotGuiltyPlea(final Offence offence, final Set<String> guiltyPleaTypes) {
        return nonNull(offence.getPlea()) && !guiltyPleaTypes.contains(offence.getPlea().getPleaValue());
    }

    private Optional<LocalDate> getMaxOrderDate(final List<JudicialResult> judicialResults) {
        return judicialResults.stream()
                .filter(judicialResult -> judicialResult.getCategory() == JudicialResultCategory.FINAL)
                .map(JudicialResult::getOrderedDate)
                .max(Comparator.naturalOrder());
    }

    private boolean hasFinalResult(final List<JudicialResult> judicialResults) {
        return judicialResults != null && judicialResults.stream().filter(Objects::nonNull).anyMatch(result -> JudicialResultCategory.FINAL == result.getCategory());
    }

    private Optional<LocalDate> getMaxOrderedDate(final List<Target> targets) {
        if(nonNull(targets)) {
            return targets.stream().filter(target -> nonNull(target.getResultLines()))
                    .flatMap(target -> target.getResultLines().stream())
                    .filter(resultLine -> !getBooleanValue(resultLine.getIsDeleted(), false))
                    .map(ResultLine::getOrderedDate)
                    .max(Comparator.naturalOrder());
        }else{
            return Optional.empty();
        }
    }

}