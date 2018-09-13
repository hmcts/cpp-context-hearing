package uk.gov.moj.cpp.hearing.event.delegates;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import uk.gov.justice.json.schemas.core.ResultLine;
import uk.gov.justice.json.schemas.core.Target;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.GenerateNowsCommand;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.NowTypes;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Nows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Prompts;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.SharedResultLines;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.ResultDefinitions;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"squid:S1188", "squid:S1602"})
public class GenerateNowsDelegate {

    private final Enveloper enveloper;

    private final ObjectToJsonObjectConverter objectToJsonObjectConverter;

    private final ReferenceDataService referenceDataService;

    @Inject
    public GenerateNowsDelegate(final Enveloper enveloper,
                                final ObjectToJsonObjectConverter objectToJsonObjectConverter,
                                final ReferenceDataService referenceDataService) {
        this.enveloper = enveloper;
        this.objectToJsonObjectConverter = objectToJsonObjectConverter;
        this.referenceDataService = referenceDataService;
    }

    private List<ResultLine> getCompletedResultLines(final ResultsShared resultsShared) {
        return resultsShared.getHearing().getTargets().stream()
                .flatMap(target->target.getResultLines().stream())
                .filter(ResultLine::getIsComplete)
                .collect(Collectors.toList());
    }

    public void generateNows(final Sender sender, final JsonEnvelope event, final List<Nows> nows, final ResultsShared resultsShared) {

        List<SharedResultLines> sharedResultLines = new ArrayList<>();

        resultsShared.getHearing().getTargets().forEach(target -> {

            sharedResultLines.addAll(target.getResultLines().stream()
                    .filter(ResultLine::getIsComplete)
                    .map(line -> {

                        //This is a potential hack. - we are getting the first case id that the defendant belongs to.
                        UUID caseId = resultsShared.getHearing().getProsecutionCases().stream()
                                .filter(prosecutionCase -> prosecutionCase.getDefendants().stream().anyMatch(d -> d.getId().equals(target.getDefendantId())))
                                .findAny()
                                .get()
                                .getId();


                        return SharedResultLines.sharedResultLines()
                                .setId(line.getResultLineId())
                                .setCaseId(caseId)
                                .setOffenceId(target.getOffenceId())
                                .setDefendantId(target.getDefendantId())
                                .setOrderedDate(line.getOrderedDate())
                                .setLevel(line.getLevel().name())
                                .setLabel(line.getResultLabel())

                                .setPrompts(
                                        line.getPrompts().stream()
                                                .map(pIn -> Prompts.prompts()
                                                        .setId(pIn.getId())
                                                        .setLabel(pIn.getLabel())
                                                        .setValue(pIn.getValue())
                                                ).collect(Collectors.toList())
                                )
                                .setSharedDate(ofNullable(resultsShared.getCompletedResultLinesStatus()
                                        .get(line.getResultLineId()))
                                        .map(CompletedResultLineStatus::getLastSharedDateTime)
                                        .orElse(null)
                                );
                    })
                    .collect(Collectors.toList()));
        });


        final GenerateNowsCommand generateNowsCommand = new GenerateNowsCommand()
                .setHearing(resultsShared.getHearing())
                .setNows(nows)
                .setSharedResultLines(sharedResultLines)
                .setCourtClerk(resultsShared.getCourtClerk())
                .setNowTypes(findNowDefinitions(event, getCompletedResultLines(resultsShared))
                        .stream()
                        .map(nowDefinition -> {

                            String nowText = Stream.concat(
                                    Stream.of(nowDefinition.getText()),
                                    nowDefinition.getResultDefinitions().stream()
                                            .map(ResultDefinitions::getText)
                                            .distinct()
                            )
                                    .filter(Objects::nonNull)
                                    .filter(s -> !s.isEmpty())
                                    .collect(Collectors.joining("\n"));

                            String welshText = Stream.concat(
                                    Stream.of(nowDefinition.getWelshText()),
                                    nowDefinition.getResultDefinitions().stream()
                                            .map(ResultDefinitions::getWelshText)
                                            .distinct()
                            )
                                    .filter(Objects::nonNull)
                                    .filter(s -> !s.isEmpty())
                                    .collect(Collectors.joining("\n"));

                            return NowTypes.nowTypes()
                                    .setId(nowDefinition.getId())
                                    .setStaticText(nowText)
                                    .setWelshStaticText(welshText)
                                    .setDescription(nowDefinition.getName())
                                    .setJurisdiction(nowDefinition.getJurisdiction())
                                    .setPriority(ofNullable(nowDefinition.getUrgentTimeLimitInMinutes()).map(Object::toString).orElse(null))
                                    .setRank(nowDefinition.getRank())
                                    .setTemplateName(nowDefinition.getTemplateName())
                                    .setBilingualTemplateName(nowDefinition.getBilingualTemplateName())
                                    .setWelshDescription(nowDefinition.getWelshName())
                                    .setRemotePrintingRequired(nowDefinition.getRemotePrintingRequired());
                        })
                        .collect(toList())
                );

        sender.send(this.enveloper.withMetadataFrom(event, "hearing.command.generate-nows")
                .apply(this.objectToJsonObjectConverter.convert(generateNowsCommand)));
    }

    private Set<NowDefinition> findNowDefinitions(final JsonEnvelope context, final List<ResultLine> resultLines) {
        return resultLines.stream()
                .map(resultLine -> referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(context,
                        resultLine.getOrderedDate(),
                        resultLine.getResultDefinitionId()))
                .filter(Objects::nonNull)
                .collect(toSet());
    }
}
