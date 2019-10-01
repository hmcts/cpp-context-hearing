package uk.gov.moj.cpp.hearing.event.delegates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.core.courts.CreateNowsRequest;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Now;
import uk.gov.justice.core.courts.NowType;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.ResultPrompt;
import uk.gov.justice.core.courts.SharedResultLine;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.core.courts.nowdocument.NowDocumentRequest;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.NowsRequestedToDocumentConverter;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.GenerateNowsCommand;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.PendingNowsRequestedCommand;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import javax.inject.Inject;
import javax.json.JsonObject;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@SuppressWarnings({"squid:S1188", "squid:S1602", "squid:S1135", "squid:S00112", "squid:S1612"})
public class NowsDelegate {

    private final Enveloper enveloper;

    private final ObjectToJsonObjectConverter objectToJsonObjectConverter;

    private final ReferenceDataService referenceDataService;

    private final NowsRequestedToDocumentConverter nowsRequestedToDocumentConverter;

    private static final Logger LOGGER = LoggerFactory.getLogger(NowsDelegate.class.getName());

    public static final String INCOMING_PROMPT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String OUTGOING_PROMPT_DATE_FORMAT = "dd MMM yyyy";
    public static final String DATE_PROMPT_TYPE = "DATE";
    public static final String CURRENCY_PROMPT_TYPE = "CURR";

    @Inject
    public NowsDelegate(final Enveloper enveloper,
                        final ObjectToJsonObjectConverter objectToJsonObjectConverter,
                        final ReferenceDataService referenceDataService,
                        final NowsRequestedToDocumentConverter nowsRequestedToDocumentConverter) {
        this.enveloper = enveloper;
        this.objectToJsonObjectConverter = objectToJsonObjectConverter;
        this.referenceDataService = referenceDataService;
        this.nowsRequestedToDocumentConverter = nowsRequestedToDocumentConverter;
    }

    private List<ResultLine> getCompletedResultLines(final ResultsShared resultsShared) {
        return resultsShared.getTargets().stream()
                .flatMap(target -> target.getResultLines().stream())
                .filter(ResultLine::getIsComplete)
                .collect(Collectors.toList());
    }

    private DelegatedPowers delegatedPowers(final DelegatedPowers courtClerk) {
        return DelegatedPowers.delegatedPowers()
                .withLastName(courtClerk.getLastName())
                .withFirstName(courtClerk.getFirstName())
                .withUserId(courtClerk.getUserId())
                .build();
    }

    private NowType nowType(final NowDefinition nowDefinition) {

        final String nowText = nowDefinition.getText() == null ? "" : nowDefinition.getText();
        final String welshText = nowDefinition.getWelshText() == null ? "" : nowDefinition.getWelshText();

        return NowType.nowType()
                .withId(nowDefinition.getId())
                .withStaticText(nowText)
                .withWelshStaticText(welshText)
                .withDescription(nowDefinition.getName())
                .withJurisdiction(nowDefinition.getJurisdiction())
                .withPriority(ofNullable(nowDefinition.getUrgentTimeLimitInMinutes()).map(Object::toString).orElse(null))
                .withRank(nowDefinition.getRank())
                .withTemplateName(nowDefinition.getTemplateName())
                .withSubTemplateName(nowDefinition.getSubTemplateName())
                //TODO GPE-6313 what about these ?
                //.setBilingualTemplateName(nowDefinition.getBilingualTemplateName())
                //.setWelshDescription(nowDefinition.getWelshName())
                // is this mapping correct ?
                .withRequiresBulkPrinting(nowDefinition.getRemotePrintingRequired())
                .withRequiresEnforcement(false)
                .build();
    }

    private Optional<uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt> getPrompt(final ResultDefinition resultDefinition, final UUID pInId, final String pInLabel) {

        Optional<uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt> promptRef = resultDefinition.getPrompts().stream()
                .filter(p -> p.getId().equals(pInId))
                .findFirst();

        if (!promptRef.isPresent() && LOGGER.isErrorEnabled()) {
            LOGGER.error(String.format("unknown prompt ref %s %s ", pInId, pInLabel));
        }

        return promptRef;
    }


    private ResultPrompt mapPrompt(final ResultDefinition resultDefinition, Prompt prompt) {

        uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt promptRef =
                getPrompt(resultDefinition, prompt.getId(), prompt.getLabel()).orElse(null);

        return ResultPrompt.resultPrompt()
                .withId(prompt.getId())
                .withLabel(prompt.getLabel())
                .withValue(prompt.getValue())
                .withIsAvailableForCourtExtract(false)
                .withPromptReference(promptRef == null ? null : promptRef.getReference())
                .build();
    }


    public CreateNowsRequest generateNows(final JsonEnvelope event, final List<Now> nows, final ResultsShared resultsShared) {

        final List<SharedResultLine> sharedResultLines = new ArrayList<>();

        resultsShared.getTargets().forEach(target ->
                sharedResultLines.addAll(target.getResultLines().stream()
                        .filter(ResultLine::getIsComplete)
                        .map(line -> {
                            //This is a potential hack. - we are getting the first case id that the defendant belongs to.
                            final UUID caseId = resultsShared.getHearing().getProsecutionCases().stream()
                                    .filter(prosecutionCase -> prosecutionCase.getDefendants().stream().anyMatch(d -> d.getId().equals(target.getDefendantId())))
                                    .findAny()
                                    .get()
                                    .getId();

                            //Check if the result line has
                            final ResultDefinition resultDefinition =
                                    referenceDataService.getResultDefinitionById(event, line.getOrderedDate(), line.getResultDefinitionId());
                            if (resultDefinition == null) {
                                throw new RuntimeException(String.format("failed to find resultdefinition %s for orderedDate %s", line.getResultDefinitionId(), line.getOrderedDate()));
                            }


                            List<ResultPrompt> prompts = line.getPrompts().stream()
                                    .map(pIn -> mapPrompt(resultDefinition, pIn)
                                    ).collect(Collectors.toList());

                            if (prompts.isEmpty()) {
                                prompts = null;
                            }

                            return SharedResultLine.sharedResultLine()
                                    .withId(line.getResultLineId())
                                    .withProsecutionCaseId(caseId)
                                    //TODO GPE-7138 set courtClerk
                                    .withOffenceId(target.getOffenceId())
                                    .withDefendantId(target.getDefendantId())
                                    .withOrderedDate(line.getOrderedDate())
                                    .withLevel(line.getLevel().name())
                                    .withLabel(line.getResultLabel() != null ? line.getResultLabel() : "")
                                    .withPrompts(prompts)
                                    .withCourtClerk(line.getDelegatedPowers() != null ? line.getDelegatedPowers() :
                                            DelegatedPowers.delegatedPowers()
                                                    .withFirstName(resultsShared.getCourtClerk().getFirstName())
                                                    .withLastName(resultsShared.getCourtClerk().getLastName())
                                                    .withUserId(resultsShared.getCourtClerk().getUserId())
                                                    .build())
                                    .withIsAvailableForCourtExtract(resultDefinition.getIsAvailableForCourtExtract())//TODO GPE-6752
                                    .withLastSharedDateTime(ofNullable(resultsShared.getCompletedResultLinesStatus()
                                            .get(line.getResultLineId()))
                                            .map(CompletedResultLineStatus::getLastSharedDateTime)
                                            .map(ZonedDateTime::toLocalDate)
                                            .map(LocalDate::toString)
                                            .orElse(null)
                                    )
                                    .withAmendmentDate(line.getAmendmentDate())
                                    .withAmendmentReasonId(line.getAmendmentReasonId())
                                    .withApprovedDate(line.getApprovedDate())
                                    .withFourEyesApproval(line.getFourEyesApproval())
                                    .build();
                        })
                        .collect(Collectors.toList())));

        final Hearing hearing = resultsShared.getHearing();
        //TODO bring hearing.json schema updto date with global so this is not necessary
        return CreateNowsRequest.createNowsRequest().build()
                .setHearing(hearing)
                .setNows(nows)
                .setSharedResultLines(sharedResultLines)
                .setCourtClerk(delegatedPowers(resultsShared.getCourtClerk()))
                .setNowTypes(findNowDefinitions(event, getCompletedResultLines(resultsShared))
                        .stream()
                        .map(nowDefinition -> nowType(nowDefinition))
                        .collect(toList())
                );
    }

    private Set<NowDefinition> findNowDefinitions(final JsonEnvelope context, final List<ResultLine> resultLines) {
        return resultLines.stream()
                .map(resultLine -> referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(context,
                        resultLine.getOrderedDate(),
                        resultLine.getResultDefinitionId()))
                .flatMap(rds -> rds.stream())
                .collect(toSet());
    }

    private void formatPrompts(final JsonEnvelope context, final List<Target> targets, final List<SharedResultLine> sharedResultLines) {
        final Map<UUID, UUID> resultLineId2ResultDefinitionId = new HashMap<>();
        if (sharedResultLines == null) {
            return;
        }
        targets.stream().flatMap(t -> t.getResultLines().stream()).forEach(rl -> {
            resultLineId2ResultDefinitionId.put(rl.getResultLineId(), rl.getResultDefinitionId());
        });
        sharedResultLines.forEach(
                rl -> {
                    if (rl.getPrompts() == null) {
                        return;
                    }
                    final UUID resultDefinitionId = resultLineId2ResultDefinitionId.get(rl.getId());
                    final ResultDefinition rd = referenceDataService.getResultDefinitionById(context, rl.getOrderedDate(), resultDefinitionId);
                    rl.getPrompts().forEach(p -> {
                        final Optional<uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt> promptRef = getPrompt(rd, p.getId(), p.getLabel());
                        if (promptRef.isPresent()) {
                            p.setValue(PublishResultUtil.reformatValue(p.getValue(), promptRef.get()));
                        }
                    });
                }
        );
    }

    public void sendNows(final Sender sender, final JsonEnvelope event, final CreateNowsRequest nowsRequest, final List<Target> targets) {

        final GenerateNowsCommand generateNowsCommand = new GenerateNowsCommand();

        formatPrompts(event, targets, nowsRequest.getSharedResultLines());

        generateNowsCommand.setCreateNowsRequest(nowsRequest);

        final List<NowDocumentRequest> nowDocumentRequests = nowsRequestedToDocumentConverter.convert(event, nowsRequest);

        nowDocumentRequests.forEach(nowDocumentRequest -> {
            final JsonObject nowsDocumentOrderJson = objectToJsonObjectConverter.convert(nowDocumentRequest);

            sender.sendAsAdmin(this.enveloper.withMetadataFrom(event, "public.hearing.now-document-requested")
                    .apply(this.objectToJsonObjectConverter.convert(nowsDocumentOrderJson)));
        });
    }

    public void sendPendingNows(final Sender sender, final JsonEnvelope event, final CreateNowsRequest nowsRequest, final List<Target> targets) {
        final PendingNowsRequestedCommand pendingNowsRequestedCommand = new PendingNowsRequestedCommand(nowsRequest, targets);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("sending hearing.command.pending-nows-requested {} ", this.objectToJsonObjectConverter.convert(pendingNowsRequestedCommand));
        }
        sender.send(this.enveloper.withMetadataFrom(event, "hearing.command.pending-nows-requested")
                .apply(this.objectToJsonObjectConverter.convert(pendingNowsRequestedCommand)));
    }

}
