package uk.gov.moj.cpp.hearing.event.delegates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.core.courts.ApplicationStatus;
import uk.gov.justice.core.courts.Category;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.Level;
import uk.gov.justice.core.courts.PleaValue;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.domain.event.result.PublicHearingResulted;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.delegates.exception.ResultDefinitionNotFoundException;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.relist.RelistReferenceDataService;
import uk.gov.moj.cpp.hearing.event.relist.ResultsSharedFilter;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import javax.inject.Inject;
import javax.json.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.justice.core.courts.Level.CASE;
import static uk.gov.justice.core.courts.Level.DEFENDANT;
import static uk.gov.justice.core.courts.Level.OFFENCE;

@SuppressWarnings({"squid:S1188", "squid:S1612"})
public class PublishResultsDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishResultsDelegate.class.getName());

    private final Enveloper enveloper;

    private final ObjectToJsonObjectConverter objectToJsonObjectConverter;

    private final ReferenceDataService referenceDataService;

    private final RelistReferenceDataService relistReferenceDataService;

    private final CustodyTimeLimitCalculator custodyTimeLimitCalculator;

    private ResultsSharedFilter resultsSharedFilter = new ResultsSharedFilter();

    @Inject
    public PublishResultsDelegate(final Enveloper enveloper, final ObjectToJsonObjectConverter objectToJsonObjectConverter,
                                  final ReferenceDataService referenceDataService, final RelistReferenceDataService relistReferenceDataService,
                                  final CustodyTimeLimitCalculator custodyTimeLimitCalculator) {
        this.enveloper = enveloper;
        this.objectToJsonObjectConverter = objectToJsonObjectConverter;
        this.referenceDataService = referenceDataService;
        this.relistReferenceDataService = relistReferenceDataService;
        this.custodyTimeLimitCalculator = custodyTimeLimitCalculator;

    }

    public void shareResults(final JsonEnvelope context, final Sender sender, final ResultsShared resultsShared) {

        final Optional<LocalDate> appealDate = findCourtOfAppealOrderDate(context, resultsShared);

        appealDate.ifPresent(localDate -> updateResultLineOrderDate(resultsShared, localDate));

        buildApplicationJudicialResults(context, resultsShared);
        if (resultsShared.getHearing().getProsecutionCases() != null) {

            final ResultsShared filterCaseResults = resultsSharedFilter.filterTargets(resultsShared, target -> target.getApplicationId() == null);

            mapAcquittalDate(context, filterCaseResults);

            buildDefendantJudicialResults(context, filterCaseResults);

            buildOffenceJudicialResults(context, resultsShared);

            this.custodyTimeLimitCalculator.calculate(resultsShared.getHearing());

        }

        final PublicHearingResulted hearingResulted =
                PublicHearingResulted.publicHearingResulted()
                        .setHearing(resultsShared.getHearing())
                        .setSharedTime(resultsShared.getSharedTime());

        final JsonObject jsonObject = this.objectToJsonObjectConverter.convert(hearingResulted);

        final JsonEnvelope jsonEnvelope = this.enveloper.withMetadataFrom(context, "public.hearing.resulted").apply(jsonObject);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("public.hearing.resulted payload {}", jsonEnvelope.toObfuscatedDebugString());
        }
        sender.send(jsonEnvelope);
    }

    /**
     * Build Offence level judicialResults for result line level is 'Offence'
     *
     * @param context       - Json envelope to call reference data
     * @param resultsShared - Results shared
     */
    private void buildOffenceJudicialResults(final JsonEnvelope context, final ResultsShared resultsShared) {
        resultsShared.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .flatMap(defendant -> defendant.getOffences().stream())
                .forEach(offence -> {
                    final List<JudicialResult> judicialResults = buildJudicialResults(context,
                            resultsShared.getHearing().getId(),
                            getResultLines(resultsShared.getTargets(), offence.getId(), OFFENCE),
                            resultsShared.getCourtClerk(),
                            resultsShared.getCompletedResultLinesStatus());

                    if (!judicialResults.isEmpty()) { //so that judicialResults doesn't have empty tag
                        offence.setJudicialResults(judicialResults);
                    }
                });
    }


    /**
     * build case or defendant level judicialResults for result line level is equal to 'Case' and
     * 'Defendant'
     *
     * @param context       Json envelope to call reference data
     * @param resultsShared Results shared
     */
    private void buildDefendantJudicialResults(final JsonEnvelope context, final ResultsShared resultsShared) {
        resultsShared.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .forEach(defendant -> {
                    final List<JudicialResult> judicialResults = buildJudicialResults(context,
                            resultsShared.getHearing().getId(),
                            getResultLines(resultsShared.getTargets(), defendant.getId(), DEFENDANT),
                            resultsShared.getCourtClerk(),
                            resultsShared.getCompletedResultLinesStatus());

                    if (!judicialResults.isEmpty()) { //so that judicialResults doesn't have empty tag
                        defendant.setJudicialResults(judicialResults);
                    }
                });
    }

    /**
     * build application judicialResults for all result line
     *
     * @param context       Json envelope to call reference data
     * @param resultsShared Results shared
     */
    private void buildApplicationJudicialResults(final JsonEnvelope context, final ResultsShared resultsShared) {
        if (nonNull(resultsShared.getHearing().getCourtApplications())) {
            resultsShared.getHearing().getCourtApplications()
                    .forEach(courtApplication -> {
                        final List<JudicialResult> judicialResults = buildJudicialResults(context,
                                resultsShared.getHearing().getId(),
                                getResultLines(resultsShared.getTargets(), courtApplication.getId(), null),
                                resultsShared.getCourtClerk(),
                                resultsShared.getCompletedResultLinesStatus());

                        if (!judicialResults.isEmpty()) { //so that judicialResults doesn't have empty tag
                            courtApplication.setJudicialResults(judicialResults);
                            courtApplication.setApplicationStatus(ApplicationStatus.FINALISED);
                        }
                    });
        }
    }

    private void mapAcquittalDate(final JsonEnvelope event, final ResultsShared resultsShared) {
        final LocalDate on = resultsShared.getHearing().getHearingDays().stream()
                .map(HearingDay::getSittingDay)
                .map(ZonedDateTime::toLocalDate)
                .min(Comparator.comparing(LocalDate::toEpochDay))
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

    private DelegatedPowers getOrDefaultCourtClerk(final Map<UUID, CompletedResultLineStatus> completedResultLinesStatus, final DelegatedPowers defaultCourtClerk, final UUID resultLineId) {
        if (completedResultLinesStatus.containsKey(resultLineId)) {
            return completedResultLinesStatus.get(resultLineId).getCourtClerk();
        } else {
            return defaultCourtClerk;
        }
    }

    private DelegatedPowers getOrDefaultCourtClerkAsDelegatePowers(final Map<UUID, CompletedResultLineStatus> completedResultLinesStatus, final DelegatedPowers defaultCourtClerk, final UUID resultLineId) {
        final DelegatedPowers courtClerk = getOrDefaultCourtClerk(completedResultLinesStatus, defaultCourtClerk, resultLineId);
        return DelegatedPowers.delegatedPowers()
                .withUserId(courtClerk.getUserId())
                .withFirstName(courtClerk.getFirstName())
                .withLastName(courtClerk.getLastName())
                .build();
    }

    private List<JudicialResult> buildJudicialResults(final JsonEnvelope context,
                                                      final UUID hearingId,
                                                      final List<ResultLine> resultLines,
                                                      final DelegatedPowers courtClerk,
                                                      final Map<UUID, CompletedResultLineStatus> completedResultLinesStatus) {

        return resultLines.stream()
                .map(resultLine -> {

                            final ResultDefinition resultDefinition = this.referenceDataService.getResultDefinitionById(context, resultLine.getOrderedDate(), resultLine.getResultDefinitionId());

                            if (resultDefinition == null) {
                                throw new ResultDefinitionNotFoundException(String.format(
                                        "resultDefinition not found for resultLineId: %s, resultDefinitionId: %s, hearingId: %s orderedDate: %s",
                                        resultLine.getResultLineId(), resultLine.getResultDefinitionId(), hearingId, resultLine.getOrderedDate()));
                            }

                            return JudicialResult.judicialResult()
                                    .withJudicialResultId(resultLine.getResultLineId())
                                    .withAmendmentDate(resultLine.getAmendmentDate())
                                    .withAmendmentReason(resultLine.getAmendmentReason())
                                    .withAmendmentReasonId(resultLine.getAmendmentReasonId())
                                    .withApprovedDate(resultLine.getApprovedDate())
                                    .withCategory(getCategory(resultDefinition))
                                    .withCjsCode(resultDefinition.getCjsCode())
                                    .withCourtClerk(getOrDefaultCourtClerkAsDelegatePowers(completedResultLinesStatus, courtClerk, resultLine.getResultLineId()))
                                    .withDelegatedPowers(resultLine.getDelegatedPowers())
                                    .withFourEyesApproval(resultLine.getFourEyesApproval())
                                    .withIsAdjournmentResult(resultDefinition.isAdjournment())
                                    .withIsAvailableForCourtExtract(resultDefinition.getIsAvailableForCourtExtract())
                                    .withIsConvictedResult(resultDefinition.isConvicted())
                                    .withIsFinancialResult(ResultDefinition.YES.equalsIgnoreCase(resultDefinition.getFinancial()))
                                    .withJudicialResultPrompts(buildJudicialResultPrompt(resultDefinition, resultLine.getPrompts()))
                                    .withLabel(resultDefinition.getLabel())
                                    .withLastSharedDateTime(resultLine.getSharedDate() != null ? resultLine.getSharedDate().toString() : LocalDate.now().toString())
                                    .withOrderedDate(resultLine.getOrderedDate())
                                    .withOrderedHearingId(hearingId)
                                    .withRank(isNull(resultDefinition.getRank()) ? BigDecimal.ZERO : new BigDecimal(resultDefinition.getRank()))
                                    .withUsergroups(resultDefinition.getUserGroups())
                                    .withWelshLabel(resultDefinition.getWelshLabel())
                                    .withIsDeleted(resultLine.getIsDeleted())
                                    .withQualifier(resultDefinition.getQualifier())
                                    .build();
                        }
                ).collect(Collectors.toList());
    }

    private Category getCategory(final ResultDefinition resultDefinition) {
        Category category = null;

        if (nonNull(resultDefinition) && nonNull(resultDefinition.getCategory())) {

            switch (resultDefinition.getCategory()) {
                case "A":
                    category = Category.ANCILLARY;
                    break;
                case "F":
                    category = Category.FINAL;
                    break;
                case "I":
                    category = Category.INTERMEDIARY;
                    break;
                default:
                    category = null;
            }
        }

        return category;
    }

    private List<JudicialResultPrompt> buildJudicialResultPrompt(final ResultDefinition resultDefinition, final List<Prompt> prompts) {

        final List<JudicialResultPrompt> promptList = prompts.stream()
                .map(prompt -> {

                            final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt promptDefinition = resultDefinition.getPrompts().stream().filter(
                                    promptDef -> promptDef.getId().equals(prompt.getId()))
                                    .findFirst().orElseThrow(() -> new RuntimeException(String.format("no prompt definition found for prompt id: %s label: %s value: %s ", prompt.getId(), prompt.getLabel(), prompt.getValue())));

                            return JudicialResultPrompt.judicialResultPrompt()
                                    .withIsAvailableForCourtExtract(resultDefinition.getIsAvailableForCourtExtract())
                                    .withLabel(prompt.getLabel())
                                    .withPromptReference(promptDefinition.getReference())
                                    .withPromptSequence(promptDefinition.getSequence() == null ? null : BigDecimal.valueOf(promptDefinition.getSequence()))
                                    .withUsergroups(promptDefinition.getUserGroups())
                                    .withValue(PublishResultUtil.reformatValue(prompt.getValue(), promptDefinition))
                                    .withWelshLabel(prompt.getWelshValue())
                                    .build();
                        }
                )
                .collect(Collectors.toList());

        return promptList.isEmpty() ? null : promptList;
    }


    private List<ResultLine> getResultLines(final List<Target> targets, final UUID id, Level level) {

        List<ResultLine> resultLines;

        final Predicate<UUID> predicate = uuid -> uuid != null && uuid.equals(id);

        if (CASE == level || DEFENDANT == level) {
            resultLines = targets.stream()
                    .filter(target -> predicate.test(target.getDefendantId()))
                    .flatMap(target -> target.getResultLines().stream())
                    .filter(resultLine -> resultLine.getLevel() == CASE || resultLine.getLevel() == DEFENDANT)
                    .collect(Collectors.toList());

        } else if (OFFENCE == level) {
            resultLines = targets.stream()
                    .filter(target -> predicate.test(target.getOffenceId()))
                    .flatMap(target -> target.getResultLines().stream())
                    .filter(resultLine -> resultLine.getLevel() == OFFENCE)
                    .collect(Collectors.toList());
        } else {

            resultLines = targets.stream()
                    .filter(target -> nonNull(target.getApplicationId()) && predicate.test(target.getApplicationId()))
                    .flatMap(target -> target.getResultLines().stream())
                    .collect(Collectors.toList());
        }

        return resultLines;
    }


    private LocalDate getOrderDate(final List<UUID> withdrawnResultDefinitionUuid, final List<Target> targets, final UUID offenceId) {
        return targets.stream()
                .filter(target -> target.getOffenceId().equals(offenceId))
                .flatMap(target -> target.getResultLines().stream())
                .filter(resultLine -> withdrawnResultDefinitionUuid.contains(resultLine.getResultDefinitionId()))
                .map(ResultLine::getOrderedDate)
                .collect(Collectors.toList())
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
                .collect(Collectors.toList());

        final List<List<uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt>> promptRefsList = resultLines.stream().map(resultLine -> {

            final ResultDefinition resultDefinition = this.referenceDataService.getResultDefinitionById(context, resultLine.getOrderedDate(), resultLine.getResultDefinitionId());

            return getResultDefinitionPrompts(resultDefinition);

        }).collect(Collectors.toList());

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
        return Optional.ofNullable(resultDefinition).map(rd -> rd.getPrompts().stream().filter(prompt -> {
            final String courtOfAppealDateRef = "CADATE";
            return Optional.ofNullable(prompt.getReference()).map(ref -> ref.equalsIgnoreCase(courtOfAppealDateRef)).orElse(false);
        }).collect(Collectors.toList())).orElse(new ArrayList<>());
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