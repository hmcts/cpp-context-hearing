package uk.gov.moj.cpp.hearing.event.delegates;

import static java.util.stream.Collectors.toList;
import static uk.gov.moj.cpp.hearing.message.shareResults.Variant.variant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.json.schemas.core.CourtClerk;
import uk.gov.justice.json.schemas.core.Hearing;
import uk.gov.justice.json.schemas.core.Prompt;
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
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;
import uk.gov.moj.cpp.hearing.domain.event.result.PublicHearingResulted;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.SharedResultLines;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;


import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

    @Inject
    public PublishResultsDelegate(final Enveloper enveloper, final ObjectToJsonObjectConverter objectToJsonObjectConverter,
                                  final ReferenceDataService referenceDataService) {
        this.enveloper = enveloper;
        this.objectToJsonObjectConverter = objectToJsonObjectConverter;
        this.referenceDataService = referenceDataService;
    }

    List<SharedPrompt> mapPrompts(final List<Prompt> prompts) {
        return prompts.stream().map(
                 prompt-> SharedPrompt.sharedPrompt()
                         .withFixedListCode(prompt.getFixedListCode())
                         .withId(prompt.getId())
// GPE-5480 where do therse come from ?
//                         .withUsergroups(prompt.get)
//                         .withIsAvailableForCourtExtract(prompt.get)
//                         .withPromptSequence(prompt.get)
//                         .withWelshLabel(prompt.getWelshValue())
                         .withValue(prompt.getValue())
                         .withLabel(prompt.getLabel())
                         .build()
                )
                .collect(Collectors.toList());
    }

    private Stream<SharedResultLine> extractSharedResultLines(final Target target, final CourtClerk courtClerk) {
        return target.getResultLines().stream()
                .map(rl->SharedResultLine.sharedResultLine()
                        .withDefendantId(target.getDefendantId())
                        .withCourtClerk(courtClerk)
                        .withDelegatedPowers(rl.getDelegatedPowers())
                        .withId(rl.getResultLineId())
 // TODO GPE-5480 where do these come from ?
 //                .withIsAvailableForCourtExtract(rl.get)
//                        .withProsecutionCaseId(target.get)
//                        .withRank(rl.get)
//                        .withWelshLabel(rl.get)
                        .withLabel(rl.getResultLabel())
                        .withLevel(rl.getLevel().name())
                        .withOffenceId(target.getOffenceId())
                        .withOrderedDate(rl.getOrderedDate())
                        .withLastSharedDateTime(rl.getSharedDate().toString())
                        .withPrompts(mapPrompts(rl.getPrompts()))
                  .build());
    }


    private List<SharedResultLine> extractSharedResultLines(final List<Target> targets, final CourtClerk courtClerk) {
        return targets.stream().flatMap(target->extractSharedResultLines(target, courtClerk))
                 .collect(Collectors.toList());
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
                                   .withHearingLanguage(Optional.ofNullable(hearingIn.getHearingLanguage()).map(hearingLanguage->hearingLanguage.name()).orElse(null))
                                   .withType(hearingIn.getType())
                                   .withJudiciary(hearingIn.getJudiciary())
                                   .withProsecutionCounsels(hearingIn.getProsecutionCounsels())
                                   .withSharedResultLines(extractSharedResultLines(hearingIn.getTargets(), resultsShared.getCourtClerk()))
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
                                    .withKey( mapVariantKey(variant.getKey()))
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
