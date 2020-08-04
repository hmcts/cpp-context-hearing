package uk.gov.moj.cpp.hearing.event.delegates;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static uk.gov.justice.core.courts.JurisdictionType.MAGISTRATES;
import static uk.gov.justice.core.courts.Level.CASE;
import static uk.gov.justice.core.courts.Level.DEFENDANT;
import static uk.gov.justice.core.courts.Level.OFFENCE;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.CategoryEnumUtils.getCategory;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.TypeUtils.getBooleanValue;

import uk.gov.justice.core.courts.ApplicationStatus;
import uk.gov.justice.core.courts.DefendantJudicialResult;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.OffenceFacts;
import uk.gov.justice.core.courts.PleaValue;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.justice.core.courts.VerdictType;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.PublicHearingResulted;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.delegates.helper.BailConditionsHelper;
import uk.gov.moj.cpp.hearing.event.delegates.helper.BailStatusHelper;
import uk.gov.moj.cpp.hearing.event.delegates.helper.BailStatusReasonHelper;
import uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.RestructuringHelper;
import uk.gov.moj.cpp.hearing.event.helper.ResultsSharedHelper;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.alcohollevel.AlcoholLevelMethod;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.relist.RelistReferenceDataService;
import uk.gov.moj.cpp.hearing.event.relist.ResultsSharedFilter;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S1188", "squid:S1612"})
public class PublishResultsDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishResultsDelegate.class.getName());
    private static final String DDCH = "DDCH";

    private final Enveloper enveloper;

    private final ObjectToJsonObjectConverter objectToJsonObjectConverter;

    private final ReferenceDataService referenceDataService;

    private final RelistReferenceDataService relistReferenceDataService;

    private final BailStatusHelper bailStatusHelper;

    private final CustodyTimeLimitCalculator custodyTimeLimitCalculator;

    private final ResultsSharedFilter resultsSharedFilter = new ResultsSharedFilter();

    private final RestructuringHelper restructuringHelper;

    @Inject
    public PublishResultsDelegate(final Enveloper enveloper, final ObjectToJsonObjectConverter objectToJsonObjectConverter,
                                  final ReferenceDataService referenceDataService, final RelistReferenceDataService relistReferenceDataService,
                                  final CustodyTimeLimitCalculator custodyTimeLimitCalculator,
                                  final BailStatusHelper bailStatusHelper,
                                  final RestructuringHelper restructuringHelper) {
        this.enveloper = enveloper;
        this.objectToJsonObjectConverter = objectToJsonObjectConverter;
        this.referenceDataService = referenceDataService;
        this.relistReferenceDataService = relistReferenceDataService;
        this.custodyTimeLimitCalculator = custodyTimeLimitCalculator;
        this.bailStatusHelper = bailStatusHelper;
        this.restructuringHelper = restructuringHelper;
    }

    public void shareResults(final JsonEnvelope context, final Sender sender, final ResultsShared resultsShared) {

        final Optional<LocalDate> appealDate = findCourtOfAppealOrderDate(context, resultsShared);

        appealDate.ifPresent(localDate -> updateResultLineOrderDate(resultsShared, localDate));

        final List<TreeNode<ResultLine>> restructuredResults = this.restructuringHelper.restructure(context, resultsShared);
        mapApplicationLevelJudicialResults(resultsShared, restructuredResults);

        if (resultsShared.getHearing().getProsecutionCases() != null && !resultsSharedFilter.filterTargets(resultsShared, t -> t.getApplicationId() == null).getTargets().isEmpty()) {

            enrichOffenceVerdictTypesData(context, resultsShared.getHearing());

            enrichOffenceFactsAlcoholLevelsData(context, resultsShared.getHearing());

            mapAcquittalDate(context, resultsShared);

            mapDefendantLevelJudicialResults(resultsShared, restructuredResults);

            mapDefendantCaseLevelJudicialResults(resultsShared, restructuredResults);

            mapOffenceLevelJudicialResults(resultsShared, restructuredResults);

            bailStatusHelper.mapBailStatuses(context, resultsShared);

            this.custodyTimeLimitCalculator.calculate(resultsShared.getHearing());

            new ResultsSharedHelper().setIsDisposedFlagOnOffence(resultsShared);
            new BailStatusReasonHelper().setReason(resultsShared);
            new BailConditionsHelper().setBailConditions(resultsShared);
            if (!isEmpty(resultsShared.getDefendantDetailsChanged())) {
                mapDefendantLevelDDCHJudicialResults(resultsShared, relistReferenceDataService.getResults(context, DDCH));
            }
        }
        final PublicHearingResulted hearingResulted = PublicHearingResulted.publicHearingResulted()
                        .setHearing(resultsShared.getHearing())
                        .setSharedTime(resultsShared.getSharedTime())
                        .setShadowListedOffences(getOffenceShadowListedForMagistratesNextHearing(resultsShared));

        final JsonObject jsonObject = this.objectToJsonObjectConverter.convert(hearingResulted);

        final JsonEnvelope jsonEnvelope = this.enveloper.withMetadataFrom(context, "public.hearing.resulted").apply(jsonObject);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Payload for event 'public.hearing.resulted': \n{}", jsonEnvelope.toObfuscatedDebugString());
        }
        sender.send(jsonEnvelope);
    }

    private List<UUID> getOffenceShadowListedForMagistratesNextHearing(final ResultsShared resultsShared) {
        if(resultsShared.getHearing().getProsecutionCases().stream().flatMap(x -> x.getDefendants().stream())
                .flatMap(def ->  def.getOffences() != null ? def.getOffences().stream() : Stream.empty())
                .flatMap(off -> off.getJudicialResults() != null ? off.getJudicialResults().stream() :Stream.empty())
                .filter(jr -> jr.getNextHearing() != null)
                .map(jr -> jr.getNextHearing()).anyMatch(nh -> MAGISTRATES == nh.getJurisdictionType())) {
            return  resultsShared.getTargets().stream().filter(t -> TRUE.equals(t.getShadowListed())).map(x -> x.getOffenceId()).collect(Collectors.toList());
        }
        return emptyList();
    }
    private void enrichOffenceVerdictTypesData(final JsonEnvelope context, final Hearing hearing) {
        final List<VerdictType> verdictTypes = referenceDataService.getVerdictTypes(context);

        hearing.getProsecutionCases()
                .forEach(prosecutionCase -> prosecutionCase.getDefendants().
                        forEach(defendant -> defendant.getOffences().stream().filter(offence -> offence.getVerdict() != null)
                                .forEach(offence -> populateFullVerdictTypeData(offence, verdictTypes))));
    }

    private void enrichOffenceFactsAlcoholLevelsData(final JsonEnvelope context, final Hearing hearing) {
        final List<AlcoholLevelMethod> alcoholLevelMethods = referenceDataService.getAlcoholLevelMethods(context);

        hearing.getProsecutionCases()
                .forEach(prosecutionCase -> prosecutionCase.getDefendants().
                        forEach(defendant -> defendant.getOffences().stream().filter(offence -> offence.getOffenceFacts() != null)
                                .forEach(offence -> populateAlcoholLevelMethodData(offence, alcoholLevelMethods))));
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

    private void mapDefendantLevelDDCHJudicialResults(final ResultsShared resultsShared, final ResultDefinition resultDefinition) {
        resultsShared.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .forEach(defendant -> {
                    if (resultsShared.getDefendantDetailsChanged().contains(defendant.getId())) {
                        final JudicialResult judicialResult = createDDCHJudicialResult(resultsShared.getHearing().getId(), resultDefinition);
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

    private JudicialResult createDDCHJudicialResult(final UUID hearingId, final ResultDefinition resultDefinition) {
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
                .withOrderedDate(LocalDate.now())
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

    private void mapApplicationLevelJudicialResults(final ResultsShared resultsShared, final List<TreeNode<ResultLine>> results) {

        if (nonNull(resultsShared.getHearing().getCourtApplications())) {
            resultsShared.getHearing().getCourtApplications()
                    .forEach(courtApplication -> {
                        final List<JudicialResult> judicialResults = getApplicationLevelJudicialResults(results, courtApplication.getId());
                        if (!judicialResults.isEmpty()) { //so that judicialResults doesn't have empty tag
                            setPromptsAsNullIfEmpty(judicialResults);
                            courtApplication.setJudicialResults(judicialResults);
                            courtApplication.setApplicationStatus(ApplicationStatus.FINALISED);
                        }
                    });
        }
    }

    private List<JudicialResult> getApplicationLevelJudicialResults(final List<TreeNode<ResultLine>> results, final UUID id) {
        return results.stream()
                .filter(node -> nonNull(node.getApplicationId()) && id.equals(node.getApplicationId()))
                .map(TreeNode::getJudicialResult).collect(toList());
    }

    private void mapDefendantCaseLevelJudicialResults(final ResultsShared resultsShared, final List<TreeNode<ResultLine>> results) {
        resultsShared.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .forEach(defendant -> {
                    final List<JudicialResult> judicialResults = getDefendantCaseJudicialResults(results, defendant.getId());
                    if (!judicialResults.isEmpty()) { //so that judicialResults doesn't have empty tag
                        setPromptsAsNullIfEmpty(judicialResults);
                        defendant.setDefendantCaseJudicialResults(judicialResults);
                    }
                });
    }

    private void mapDefendantLevelJudicialResults(final ResultsShared resultsShared, final List<TreeNode<ResultLine>> results) {
        final List<DefendantJudicialResult> defendantJudicialResults = resultsShared.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .map(defendant -> {
                    final List<JudicialResult> judicialResults = getDefendantJudicialResults(results, defendant.getId());
                    if (!judicialResults.isEmpty()) { //so that judicialResults doesn't have empty tag
                        setPromptsAsNullIfEmpty(judicialResults);
                        return buildDefendantJudicialResults(defendant.getMasterDefendantId(), judicialResults);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(toList());

        if (!defendantJudicialResults.isEmpty()) {
            setDefendantJudicialResultPromptsAsNullIfEmpty(defendantJudicialResults);
            resultsShared.getHearing().setDefendantJudicialResults(defendantJudicialResults);
        }
    }

    private void setDefendantJudicialResultPromptsAsNullIfEmpty(List<DefendantJudicialResult> defendantJudicialResults) {
        if (CollectionUtils.isNotEmpty(defendantJudicialResults)) {
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
                .map(TreeNode::getJudicialResult).collect(toList());
    }

    private List<JudicialResult> getDefendantCaseJudicialResults(final List<TreeNode<ResultLine>> results, final UUID id) {
        return results.stream()
                .filter(node -> node.getLevel() == CASE)
                .filter(node -> nonNull(node.getDefendantId()) && id.equals(node.getDefendantId()))
                .map(TreeNode::getJudicialResult).collect(toList());
    }

    private void mapOffenceLevelJudicialResults(final ResultsShared resultsShared, final List<TreeNode<ResultLine>> results) {
        resultsShared.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .flatMap(defendant -> defendant.getOffences().stream())
                .forEach(offence -> {

                    final List<JudicialResult> judicialResults = getOffenceLevelJudicialResults(results, offence.getId());

                    if (!judicialResults.isEmpty()) { //so that judicialResults doesn't have empty tag
                        setPromptsAsNullIfEmpty(judicialResults);
                        offence.setJudicialResults(judicialResults);
                    }
                });
    }

    private void setPromptsAsNullIfEmpty(final List<JudicialResult> judicialResults) {
        if (CollectionUtils.isNotEmpty(judicialResults)) {
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

    private void mapAcquittalDate(final JsonEnvelope event, final ResultsShared resultsShared) {
        final LocalDate on = resultsShared.getHearing().getHearingDays().stream()
                .map(HearingDay::getSittingDay)
                .map(ZonedDateTime::toLocalDate)
                .min(comparing(LocalDate::toEpochDay))
                .orElse(LocalDate.now());

        //Get all withdrawn result definitions
        final List<UUID> withdrawnResultDefinitionUuid = relistReferenceDataService.getWithdrawnResultDefinitionUuids(event, on);

        //Set Acquittals (to support court extract) default is set to null
        resultsShared.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .flatMap(defendant -> defendant.getOffences().stream())
                .forEach(offence -> {
                    if (nonNull(offence.getPlea()) &&
                            offence.getPlea().getPleaValue() == PleaValue.NOT_GUILTY &&
                            isResultLineFinal(withdrawnResultDefinitionUuid, resultsShared.getTargets(), offence.getId()) &&
                            isNull(offence.getConvictionDate())) {
                        final LocalDate orderDate = getOrderDate(withdrawnResultDefinitionUuid, resultsShared.getTargets(), offence.getId());
                        offence.setAquittalDate(orderDate);
                    }
                });
    }

    private LocalDate getOrderDate(final List<UUID> withdrawnResultDefinitionUuid, final List<Target> targets, final UUID offenceId) {
        return targets.stream()
                .filter(target -> target.getOffenceId().equals(offenceId))
                .flatMap(target -> target.getResultLines().stream())
                .filter(resultLine -> withdrawnResultDefinitionUuid.contains(resultLine.getResultDefinitionId()))
                .map(ResultLine::getOrderedDate)
                .collect(toList())
                .get(0);
    }

    private boolean isResultLineFinal(final List<UUID> withdrawnResultDefinitionUuid, final List<Target> targets, final UUID offenceId) {
        return targets.stream()
                .filter(target -> target.getApplicationId() == null)
                .filter(target -> target.getOffenceId().equals(offenceId))
                .flatMap(target -> target.getResultLines().stream())
                .anyMatch(resultLine -> withdrawnResultDefinitionUuid.contains(resultLine.getResultDefinitionId()));
    }

    /**
     * get court of appeal results order date which will be entered as a prompt if any.
     *
     * @param context       - Json Envelope
     * @param resultsShared - shared results line from UI
     * @return - returns court of appeal results order date
     */
    private Optional<LocalDate> findCourtOfAppealOrderDate(final JsonEnvelope context, final ResultsShared resultsShared) {

        final List<ResultLine> resultLines = resultsShared.getTargets().stream().flatMap(target -> target.getResultLines().stream())
                .filter(r -> (isNull(r.getIsDeleted()) || !r.getIsDeleted()))
                .collect(toList());

        final List<List<uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt>> promptRefsList = resultLines.stream().map(resultLine -> {

            final ResultDefinition resultDefinition = this.referenceDataService.getResultDefinitionById(context, resultLine.getOrderedDate(), resultLine.getResultDefinitionId());

            return getResultDefinitionPrompts(resultDefinition);

        }).collect(toList());

        return resultLines.stream()
                .filter(resultLine -> isNull(resultLine.getIsDeleted()) || !resultLine.getIsDeleted())
                .filter(resultLine -> nonNull(resultLine.getPrompts()))
                .flatMap(resultLine -> resultLine.getPrompts().stream())
                .filter(prompt -> promptRefsList.stream().flatMap(Collection::stream).anyMatch(p -> prompt.getId().equals(p.getId())))
                .filter(prompt -> nonNull(prompt.getValue()))
                .map(Prompt::getValue)
                .map(LocalDate::parse)
                .findFirst();
    }

    /**
     * Get Result Definition Prompts where prompt reference has "CADATE"
     *
     * @param resultDefinition - Result Definition
     * @return - List of Prompts
     */
    private List<uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt> getResultDefinitionPrompts(final ResultDefinition resultDefinition) {
        return ofNullable(resultDefinition).map(rd -> rd.getPrompts().stream().filter(prompt -> {
            final String courtOfAppealDateRef = "CADATE";
            return ofNullable(prompt.getReference()).map(ref -> ref.equalsIgnoreCase(courtOfAppealDateRef)).orElse(false);
        }).collect(toList())).orElse(new ArrayList<>());
    }

    /**
     * Update result line order date to the new given date. The new given date will be court of
     * appeal results order date
     *
     * @param resultsShared - shared results line from UI
     * @param newOrderDate  - new order date
     */
    private void updateResultLineOrderDate(final ResultsShared resultsShared, final LocalDate newOrderDate) {
        resultsShared.getTargets().stream().flatMap(target -> target.getResultLines().stream()).forEach(resultLine -> resultLine.setOrderedDate(newOrderDate));
    }
}