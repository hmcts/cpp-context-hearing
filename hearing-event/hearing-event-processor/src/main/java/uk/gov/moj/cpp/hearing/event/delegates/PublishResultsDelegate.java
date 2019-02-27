package uk.gov.moj.cpp.hearing.event.delegates;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

import uk.gov.justice.core.courts.CourtClerk;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.Key;
import uk.gov.justice.core.courts.PleaValue;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ResultPrompt;
import uk.gov.justice.core.courts.SharedHearing;
import uk.gov.justice.core.courts.SharedResultLine;
import uk.gov.justice.core.courts.SharedVariant;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantKey;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.domain.event.result.PublicHearingResulted;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.relist.RelistReferenceDataService;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.JsonObject;

@SuppressWarnings({"squid:S1188", "squid:S1612"})
public class PublishResultsDelegate {

    private final Enveloper enveloper;

    private final ObjectToJsonObjectConverter objectToJsonObjectConverter;

    private final ReferenceDataService referenceDataService;

    private final RelistReferenceDataService relistReferenceDataService;

    @Inject
    public PublishResultsDelegate(final Enveloper enveloper, final ObjectToJsonObjectConverter objectToJsonObjectConverter,
                                  final ReferenceDataService referenceDataService, final RelistReferenceDataService relistReferenceDataService) {
        this.enveloper = enveloper;
        this.objectToJsonObjectConverter = objectToJsonObjectConverter;
        this.referenceDataService = referenceDataService;
        this.relistReferenceDataService = relistReferenceDataService;
    }

    private static <T> Function<T, T> replaceWithInputs(final Collection<T> input) {
        return v -> input.stream().filter(p -> p.equals(v)).findFirst().orElse(v);
    }

    private static <T> Predicate<T> isNotInSet(final Collection<T> input) {
        return v -> input.stream().noneMatch(p -> p.equals(v));
    }

    @SuppressWarnings({"squid:S1135"})
    private List<ResultPrompt> mapPrompts(final ResultDefinition resultDefinition, final List<Prompt> prompts) {
        List<ResultPrompt> promptList = prompts.stream().map(
                prompt -> {

                    final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt promptDefinition = resultDefinition.getPrompts().stream().filter(
                            promptDef -> promptDef.getId().equals(prompt.getId()))
                            .findFirst().orElseThrow(() -> new RuntimeException(String.format("no prompt definition found for prompt id: %s label: %s value: %s ", prompt.getId(), prompt.getLabel(), prompt.getValue())));

                    return ResultPrompt.resultPrompt()
                            .withFixedListCode(prompt.getFixedListCode())
                            .withId(prompt.getId())
                            .withUsergroups(promptDefinition.getUserGroups())
                            .withPromptSequence(promptDefinition.getSequence() == null ? null : BigDecimal.valueOf(promptDefinition.getSequence()))
                            .withIsAvailableForCourtExtract(resultDefinition.getIsAvailableForCourtExtract())
                            .withWelshLabel(prompt.getWelshValue())
                            .withValue(prompt.getValue())
                            .withLabel(prompt.getLabel())
                            .withWelshValue(prompt.getWelshValue())
                            .build();
                }
        )
                .collect(Collectors.toList());
        return promptList.isEmpty() ? null : promptList;
    }

    private CourtClerk getOrDefaultCourtClerk(final Map<UUID, CompletedResultLineStatus> completedResultLinesStatus, final CourtClerk defaultCourtClerk, final UUID resultLineId) {
        if (completedResultLinesStatus.containsKey(resultLineId)) {
            return completedResultLinesStatus.get(resultLineId).getCourtClerk();
        } else {
            return defaultCourtClerk;
        }
    }

    private DelegatedPowers getOrDefaultCourtClerkAsDelegatePowers(final Map<UUID, CompletedResultLineStatus> completedResultLinesStatus, final CourtClerk defaultCourtClerk, final UUID resultLineId) {
        final CourtClerk courtClerk = getOrDefaultCourtClerk(completedResultLinesStatus, defaultCourtClerk, resultLineId);
        return DelegatedPowers.delegatedPowers()
                .withUserId(courtClerk.getId())
                .withFirstName(courtClerk.getFirstName())
                .withLastName(courtClerk.getLastName())
                .build();
    }

    @SuppressWarnings({"squid:S00112", "squid:S1135"})
    private Stream<SharedResultLine> extractSharedResultLines(final JsonEnvelope context, final Target target, final UUID prosecutionCaseId, final CourtClerk courtClerk, Map<UUID, CompletedResultLineStatus> completedResultLinesStatus) {
        return target.getResultLines().stream()
                .map(rl -> {
                            final ResultDefinition resultDefinition = this.referenceDataService.getResultDefinitionById(context, rl.getOrderedDate(), rl.getResultDefinitionId());
                            if (resultDefinition == null) {
                                throw new RuntimeException(String.format(
                                        "resultDefinition not found for resultLineId: %s, resultDefinitionId: %s, targetId: %s, hearingId: %s orderedDate: %s",
                                        rl.getResultLineId(), rl.getResultDefinitionId(), target.getTargetId(), target.getHearingId(), rl.getOrderedDate()));
                            }

                            return SharedResultLine.sharedResultLine()
                                    .withDefendantId(target.getDefendantId())
                                    .withCourtClerk(getOrDefaultCourtClerkAsDelegatePowers(completedResultLinesStatus, courtClerk, rl.getResultLineId()))
                                    .withDelegatedPowers(rl.getDelegatedPowers())
                                    .withId(rl.getResultLineId())
                                    .withIsAvailableForCourtExtract(resultDefinition.getIsAvailableForCourtExtract())
                                    .withWelshLabel(resultDefinition.getWelshLabel())
                                    .withProsecutionCaseId(prosecutionCaseId)
                                    .withRank(resultDefinition.getRank() == null ? null : BigDecimal.valueOf(resultDefinition.getRank()))
                                    .withLabel(rl.getResultLabel() == null ? resultDefinition.getLabel() : rl.getResultLabel())
                                    .withLevel(rl.getLevel().name())
                                    .withOffenceId(target.getOffenceId())
                                    .withOrderedDate(rl.getOrderedDate())
                                    .withLastSharedDateTime(rl.getSharedDate() != null ? rl.getSharedDate().toString() : LocalDate.now().toString())
                                    .withPrompts(mapPrompts(resultDefinition, rl.getPrompts()))
                                    .build();

                        }
                );
    }

    private List<SharedResultLine> extractSharedResultLines(final JsonEnvelope context, final List<Target> targets, final List<ProsecutionCase> prosecutionCases, final CourtClerk courtClerk, final Map<UUID, CompletedResultLineStatus> completedResultLinesStatus) {

        return targets.stream().flatMap(target ->
                {
                    final UUID prosecutionCaseId = prosecutionCases.stream().filter(pc -> pc.getDefendants().stream()
                            .anyMatch(d -> target.getDefendantId().equals(d.getId())))
                            .findFirst().map(ProsecutionCase::getId).orElseThrow(
                                    () -> new RuntimeException(String.format("cant find defendant %s in hearing %s for target %s",
                                            target.getDefendantId(), target.getHearingId(), target.getTargetId())));

                    return extractSharedResultLines(context, target, prosecutionCaseId, courtClerk, completedResultLinesStatus);
                }
        ).collect(Collectors.toList());
    }

    private JurisdictionType translateJurisdictionType(uk.gov.justice.core.courts.JurisdictionType from) {
        return JurisdictionType.valueOf(from.name());
    }

    public void shareResults(JsonEnvelope context, final Sender sender, final JsonEnvelope event, final ResultsShared resultsShared, final List<Variant> newVariants) {

        final List<Variant> variants = Stream.concat(
                resultsShared.getVariantDirectory().stream().map(replaceWithInputs(newVariants)),
                newVariants.stream().filter(isNotInSet(resultsShared.getVariantDirectory()))
        ).collect(toList());

        final Hearing hearingIn = resultsShared.getHearing();

        final LocalDate orderedDate = resultsShared.getHearing().getHearingDays().stream()
                .map(HearingDay::getSittingDay)
                .map(ZonedDateTime::toLocalDate)
                .min(Comparator.comparing(LocalDate::toEpochDay))
                .orElse(LocalDate.now());

        final List<UUID> withdrawnResultDefinitionUuid = relistReferenceDataService.getWithdrawnResultDefinitionUuids(event, orderedDate);

        //Set Acquittals (to support court extract) default is set to null
        hearingIn.getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .flatMap(defendant -> defendant.getOffences().stream())
                .forEach(offence -> {
                    if (nonNull(offence.getPlea()) &&
                            offence.getPlea().getPleaValue() == PleaValue.NOT_GUILTY &&
                            isResultLineFinal(withdrawnResultDefinitionUuid, hearingIn.getTargets(), offence.getId()) &&
                            isNull(offence.getConvictionDate())) {
                        offence.setIsAcquitted(true);
                    }

                    if(nonNull(offence.getConvictionDate())) {
                        offence.setIsAcquitted(false);
                    }
                });

        final PublicHearingResulted shareResultsMessage = PublicHearingResulted.publicHearingResulted()
                .setHearing(
                        SharedHearing.sharedHearing()
                                .withId(hearingIn.getId())
                                .withType(hearingIn.getType())
                                .withJurisdictionType(translateJurisdictionType(hearingIn.getJurisdictionType()))
                                .withCourtCentre(hearingIn.getCourtCentre())
                                .withDefendantAttendance(hearingIn.getDefendantAttendance())
                                .withDefenceCounsels(hearingIn.getDefenceCounsels())
                                .withProsecutionCases(hearingIn.getProsecutionCases())
                                .withHearingDays(hearingIn.getHearingDays())
                                .withHearingLanguage(hearingIn.getHearingLanguage())
                                .withType(hearingIn.getType())
                                .withJudiciary(hearingIn.getJudiciary())
                                .withProsecutionCounsels(hearingIn.getProsecutionCounsels())
                                .withSharedResultLines(extractSharedResultLines(context, hearingIn.getTargets(), hearingIn.getProsecutionCases(), resultsShared.getCourtClerk(), resultsShared.getCompletedResultLinesStatus()))
                                .withDefenceCounsels(hearingIn.getDefenceCounsels())
                                .withHasSharedResults(hearingIn.getHasSharedResults())
                                .build()
                )
                .setVariants(mapVariantDirectory(context, variants))
                .setSharedTime(resultsShared.getSharedTime());

        final JsonObject jsonObject = this.objectToJsonObjectConverter.convert(shareResultsMessage);

        sender.send(this.enveloper.withMetadataFrom(event, "public.hearing.resulted")
                .apply(jsonObject));
    }

    private boolean isResultLineFinal(final List<UUID> withdrawnResultDefinitionUuid, final List<Target> targets, final UUID offenceId) {
        return targets.stream()
                .filter(target -> target.getOffenceId().equals(offenceId))
                .flatMap(target -> target.getResultLines().stream())
                .anyMatch(resultLine -> withdrawnResultDefinitionUuid.contains(resultLine.getResultDefinitionId()));
    }

    private Key mapVariantKey(final VariantKey keyIn) {
        return Key.key()
                .withDefendantId(keyIn.getDefendantId())
                .withHearingId(keyIn.getHearingId())
                .withNowsTypeId(keyIn.getNowsTypeId())
                .withUsergroups(keyIn.getUsergroups())
                .build();
    }

    private List<SharedVariant> mapVariantDirectory(JsonEnvelope context, final List<Variant> updatedVariantDirectory) {
        List<SharedVariant> sharedVariants = updatedVariantDirectory.stream()
                .map(variant -> {
                            final NowDefinition nowDefinition = referenceDataService.getNowDefinitionById(context, variant.getReferenceDate(), variant.getKey().getNowsTypeId());
                            return SharedVariant.sharedVariant()
                                    .withKey(mapVariantKey(variant.getKey()))
                                    .withStatus(variant.getValue().getStatus().toString())
                                    .withMaterialId(variant.getValue().getMaterialId())
                                    .withDescription(nowDefinition.getName())
                                    .withTemplateName(nowDefinition.getTemplateName())
                                    .build();
                        }
                )
                .collect(toList());
        return sharedVariants.isEmpty() ? null : sharedVariants;
    }
}
