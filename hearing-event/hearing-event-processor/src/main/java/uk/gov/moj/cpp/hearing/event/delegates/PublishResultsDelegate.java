package uk.gov.moj.cpp.hearing.event.delegates;

import static java.util.stream.Collectors.toList;
import uk.gov.justice.json.schemas.core.CourtClerk;
import uk.gov.justice.json.schemas.core.Hearing;
import uk.gov.justice.json.schemas.core.Prompt;
import uk.gov.justice.json.schemas.core.ProsecutionCase;
import uk.gov.justice.json.schemas.core.Target;
import uk.gov.justice.json.schemas.core.publichearingresulted.JurisdictionType;
import uk.gov.justice.json.schemas.core.publichearingresulted.Key;
import uk.gov.justice.json.schemas.core.publichearingresulted.SharedHearing;
import uk.gov.justice.json.schemas.core.publichearingresulted.SharedPrompt;
import uk.gov.justice.json.schemas.core.publichearingresulted.SharedResultLine;
import uk.gov.justice.json.schemas.core.publichearingresulted.SharedVariant;
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
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import javax.inject.Inject;
import javax.json.JsonObject;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"squid:S1188", "squid:S1612"})
public class PublishResultsDelegate {

    private final Enveloper enveloper;

    private final ObjectToJsonObjectConverter objectToJsonObjectConverter;

    private final ReferenceDataService referenceDataService;

    @Inject
    public PublishResultsDelegate(final Enveloper enveloper, final ObjectToJsonObjectConverter objectToJsonObjectConverter,
                                  final ReferenceDataService referenceDataService) {
        this.enveloper = enveloper;
        this.objectToJsonObjectConverter = objectToJsonObjectConverter;
        this.referenceDataService = referenceDataService;
    }

    @SuppressWarnings({"squid:S1135"})
    private List<SharedPrompt> mapPrompts(final ResultDefinition resultDefinition, final List<Prompt> prompts) {
        return prompts.stream().map(
                prompt -> {
                    // TODO GPE-5483 should match prompt defintions on ids but promptdefinition id not available in core data model for Prompt
                    final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt promptDefinition = resultDefinition.getPrompts().stream().filter(
                            promptDef -> promptDef.getLabel().equals(prompt.getLabel()))
                            .findFirst().orElseThrow(() -> new RuntimeException(String.format("no prompt definition found for prompt label: %s value: %s ", prompt.getLabel(), prompt.getValue())));

                    return SharedPrompt.sharedPrompt()
                            .withFixedListCode(prompt.getFixedListCode())
                            .withId(prompt.getId())
                            .withUsergroups(promptDefinition.getUserGroups())
                            .withPromptSequence(promptDefinition.getSequence()==null?null:BigDecimal.valueOf(promptDefinition.getSequence()))
                            // TODO GPE-5483
//                         .withIsAvailableForCourtExtract(prompt.get)
//                         .withPromptSequence(prompt.get)
//                         .withWelshLabel(prompt.getWelshValue())
                            .withValue(prompt.getValue())
                            .withLabel(prompt.getLabel())
                            .build();
                }
        )
                .collect(Collectors.toList());
    }

    private CourtClerk getOrDefaultCourtClerk(final Map<UUID, CompletedResultLineStatus> completedResultLinesStatus, final CourtClerk defaultCourtClerk, final UUID resultLineId) {
        if (completedResultLinesStatus.containsKey(resultLineId)) {
            return completedResultLinesStatus.get(resultLineId).getCourtClerk();
        } else {
            return defaultCourtClerk;
        }
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
                                    .withCourtClerk(getOrDefaultCourtClerk(completedResultLinesStatus, courtClerk, rl.getResultLineId()))
                                    .withDelegatedPowers(rl.getDelegatedPowers())
                                    .withId(rl.getResultLineId())
                                    // TODO GPE-5483
                                    //.withIsAvailableForCourtExtract(rl.get)
                                    //.withWelshLabel(resultDefinition.get)
                                    .withProsecutionCaseId(prosecutionCaseId)
                                    .withRank(resultDefinition.getRank()==null?null:BigDecimal.valueOf(resultDefinition.getRank()))
                                    .withLabel(rl.getResultLabel())
                                    .withLevel(rl.getLevel().name())
                                    .withOffenceId(target.getOffenceId())
                                    .withOrderedDate(rl.getOrderedDate())
                                    .withLastSharedDateTime(rl.getSharedDate().toString())
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

    private JurisdictionType translateJurisdictionType(uk.gov.justice.json.schemas.core.JurisdictionType from) {
        return JurisdictionType.valueOf(from.name());
    }

    public void shareResults(JsonEnvelope context, final Sender sender, final JsonEnvelope event, final ResultsShared resultsShared, final List<Variant> newVariants) {

        final List<Variant> variants = Stream.concat(
                resultsShared.getVariantDirectory().stream().map(replaceWithInputs(newVariants)),
                newVariants.stream().filter(isNotInSet(resultsShared.getVariantDirectory()))
        ).collect(toList());

        final Hearing hearingIn = resultsShared.getHearing();

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
                                .withHearingLanguage(Optional.ofNullable(hearingIn.getHearingLanguage()).map(hearingLanguage -> hearingLanguage.name()).orElse(null))
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

    Key mapVariantKey(final VariantKey keyIn) {
        return Key.key()
                .withDefendantId(keyIn.getDefendantId())
                .withHearingId(keyIn.getHearingId())
                .withNowsTypeId(keyIn.getNowsTypeId())
                .withUsergroups(keyIn.getUsergroups())
                .build();
    }

    private List<SharedVariant> mapVariantDirectory(JsonEnvelope context, final List<Variant> updatedVariantDirectory) {
        return updatedVariantDirectory.stream()
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
    }

    private static <T> Function<T, T> replaceWithInputs(final Collection<T> input) {
        return v -> input.stream().filter(p -> p.equals(v)).findFirst().orElse(v);
    }

    private static <T> Predicate<T> isNotInSet(final Collection<T> input) {
        return v -> input.stream().noneMatch(p -> p.equals(v));
    }
}
