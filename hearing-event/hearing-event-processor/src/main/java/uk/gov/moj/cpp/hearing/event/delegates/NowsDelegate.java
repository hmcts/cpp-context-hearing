package uk.gov.moj.cpp.hearing.event.delegates;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;

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
import uk.gov.justice.hearing.courts.referencedata.CourtCentreOrganisationUnit;
import uk.gov.justice.hearing.courts.referencedata.Elements;
import uk.gov.justice.hearing.courts.referencedata.FixedListCollection;
import uk.gov.justice.hearing.courts.referencedata.FixedListResult;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.NowsRequestedToDocumentConverter;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.GenerateNowsCommand;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.PendingNowsRequestedCommand;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.service.CourtHouseReverseLookup;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S1188", "squid:S1602", "squid:S1135", "squid:S00112", "squid:S1612", "squid:S1166"})
public class NowsDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(NowsDelegate.class.getName());

    public static final String INCOMING_PROMPT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String OUTGOING_PROMPT_DATE_FORMAT = "dd MMM yyyy";
    public static final String DATE_PROMPT_TYPE = "DATE";
    public static final String CURRENCY_PROMPT_TYPE = "CURR";

    /*
    Following welsh values are borrowed from ref data.  This is a temporary solution. we should get the values from  event payload in future.
     */
    private static final String YEARS_IN_WELSH = "Flynedd";
    private static final String MONTHS_IN_WELSH = "Mis";
    private static final String WEEKS_IN_WELSH = "Wythnos";
    private static final String DAYS_IN_WELSH = "Niwrnod";

    private static final String YEARS_IN_ENGLISH = "Years";
    private static final String MONTHS_IN_ENGLISH = "Months";
    private static final String WEEKS_IN_ENGLISH = "Weeks";
    private static final String DAYS_IN_ENGLISH = "Days";
    public static final String PERIOD_OF_CONDITIONAL_DISCHARGE = "Period of conditional discharge";
    public static final String COURTHOUSE_NAME = "Courthouse name";

    private final CourtHouseReverseLookup courtHouseReverseLookup;

    private final ReferenceDataService referenceDataService;

    private final ObjectToJsonObjectConverter objectToJsonObjectConverter;

    private final NowsRequestedToDocumentConverter nowsRequestedToDocumentConverter;

    @Inject
    public NowsDelegate(final ObjectToJsonObjectConverter objectToJsonObjectConverter,
                        final ReferenceDataService referenceDataService,
                        final NowsRequestedToDocumentConverter nowsRequestedToDocumentConverter,
                        final CourtHouseReverseLookup courtHouseReverseLookup) {
        this.objectToJsonObjectConverter = objectToJsonObjectConverter;
        this.referenceDataService = referenceDataService;
        this.nowsRequestedToDocumentConverter = nowsRequestedToDocumentConverter;
        this.courtHouseReverseLookup = courtHouseReverseLookup;
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

    private NowType nowType(final NowDefinition nowDefinition, final boolean isWelsh) {

        final String nowText = nowDefinition.getText() == null ? "" : nowDefinition.getText();
        final String welshText = nowDefinition.getWelshText() == null ? "" : nowDefinition.getWelshText();

        return NowType.nowType()
                .withId(nowDefinition.getId())
                .withStaticText(nowText)
                .withWelshStaticText(welshText)
                .withDescription(nowDefinition.getName())
                .withWelshDescription(nowDefinition.getWelshName())
                .withJurisdiction(nowDefinition.getJurisdiction())
                .withPriority(ofNullable(nowDefinition.getUrgentTimeLimitInMinutes()).map(Object::toString).orElse(null))
                .withRank(nowDefinition.getRank())
                .withTemplateName(isWelsh ? nowDefinition.getBilingualTemplateName() : nowDefinition.getTemplateName())
                .withSubTemplateName(nowDefinition.getSubTemplateName())
                .withRequiresBulkPrinting(nowDefinition.getRemotePrintingRequired())
                .withRequiresEnforcement(false)
                .build();
    }

    private Optional<uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt> getPrompt(final ResultDefinition resultDefinition, final UUID pInId, final String pInLabel) {

        final Optional<uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt> promptRef = resultDefinition.getPrompts().stream()
                .filter(p -> p.getId().equals(pInId))
                .findFirst();

        if (!promptRef.isPresent() && LOGGER.isErrorEnabled()) {
            LOGGER.error(format("unknown prompt ref %s %s ", pInId, pInLabel));
        }

        return promptRef;
    }


    private ResultPrompt mapPrompt(final ResultDefinition resultDefinition, Prompt prompt) {

        final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt promptRef =
                getPrompt(resultDefinition, prompt.getId(), prompt.getLabel()).orElse(null);

        return ResultPrompt.resultPrompt()
                .withId(prompt.getId())
                .withLabel(prompt.getLabel())
                .withValue(prompt.getValue())
                .withWelshLabel(prompt.getWelshLabel())
                .withWelshValue(prompt.getWelshValue())
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
                                throw new RuntimeException(format("failed to find resultdefinition %s for orderedDate %s", line.getResultDefinitionId(), line.getOrderedDate()));
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
                                    .withWelshLabel(resultDefinition.getWelshLabel()).withPrompts(prompts)
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

        final boolean isWelsh = isCourtWelshOrganisationUnit(event, hearing);

        return CreateNowsRequest.createNowsRequest().build()
                .setHearing(hearing)
                .setNows(nows)
                .setSharedResultLines(sharedResultLines)
                .setCourtClerk(delegatedPowers(resultsShared.getCourtClerk()))
                .setNowTypes(findNowDefinitions(event, getCompletedResultLines(resultsShared))
                        .stream()
                        .map(nowDefinition -> nowType(nowDefinition, isWelsh))
                        .collect(toList())
                );
    }

    protected boolean isCourtWelshOrganisationUnit(final JsonEnvelope event, final Hearing hearing) {
        final Optional<CourtCentreOrganisationUnit> courtCentreOrganisationUnit = courtHouseReverseLookup.getCourtCentreById(event, hearing.getCourtCentre().getId());

        return ofNullable(courtCentreOrganisationUnit.orElseThrow(() -> new IllegalStateException(format("No Court Centre For Hearing with court centre id %s.", hearing.getCourtCentre().getId()))).getIsWelsh()).orElse(false);
    }

    private Set<NowDefinition> findNowDefinitions(final JsonEnvelope context, final List<ResultLine> resultLines) {
        return resultLines.stream()
                .map(resultLine -> referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(context,
                        resultLine.getOrderedDate(),
                        resultLine.getResultDefinitionId()))
                .flatMap(Collection::stream)
                .collect(toSet());
    }

    private void getPromptReferenceData(final JsonEnvelope context, final List<Target> targets, final List<SharedResultLine> sharedResultLines) {
        final Map<UUID, UUID> resultLineId2ResultDefinitionId = new HashMap<>();
        if (sharedResultLines == null) {
            return;
        }
        targets.stream().flatMap(t -> t.getResultLines().stream()).forEach(rl -> {
            resultLineId2ResultDefinitionId.put(rl.getResultLineId(), rl.getResultDefinitionId());
        });
        sharedResultLines.forEach(
                sharedResultLine -> {
                    if (sharedResultLine.getPrompts() == null) {
                        return;
                    }
                    final UUID resultDefinitionId = resultLineId2ResultDefinitionId.get(sharedResultLine.getId());
                    final ResultDefinition rd = referenceDataService.getResultDefinitionById(context, sharedResultLine.getOrderedDate(), resultDefinitionId);
                    sharedResultLine.getPrompts().forEach(resultPrompt -> {
                        final Optional<uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt> promptRef = getPrompt(rd, resultPrompt.getId(), resultPrompt.getLabel());
                        promptRef.ifPresent(prompt -> {
                            resultPrompt.setValue(PublishResultUtil.reformatValue(resultPrompt.getValue(), prompt));
                            resultPrompt.setWelshLabel(prompt.getWelshLabel());
                            resultPrompt.setWelshValue(enrichWithWelshValue(context, prompt, resultPrompt.getValue()));
                        });
                    });
                }
        );
    }

    private String enrichWithWelshValue(final JsonEnvelope context,
                                        final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt prompt,
                                        final String promptValue) {

        if (prompt.getLabel().equalsIgnoreCase(PERIOD_OF_CONDITIONAL_DISCHARGE)) {
            return replaceDurationInEngToWelsh(promptValue);
        }
        if (prompt.getLabel().equalsIgnoreCase(COURTHOUSE_NAME)) {
            final Optional<CourtCentreOrganisationUnit> courtCentreOrgOptional = courtHouseReverseLookup.getCourtCentreByName(context, COURTHOUSE_NAME);
            if (courtCentreOrgOptional.isPresent()) {
                return courtCentreOrgOptional.get().getOucodeL3WelshName();
            }
        }
        final FixedListResult allFixedLists = referenceDataService.getAllFixedLists(context);
        if (prompt.getFixedListId() != null) {
            final List<FixedListCollection> fixedListCollectionList = allFixedLists.getFixedListCollection().stream()
                    .filter(fixedList -> fixedList.getId().equals(prompt.getFixedListId()))
                    .collect(toList());
            if (!fixedListCollectionList.isEmpty()) {
                if (promptValue.contains(",")) {
                    return Stream.of(promptValue.split(",")).map(
                            s -> getWelshValueFromFixedListElementList(s, fixedListCollectionList))
                            .collect(Collectors.joining(","));
                } else {
                    return getWelshValueFromFixedListElementList(promptValue, fixedListCollectionList);
                }
            }
        }

        if (prompt.getLabel().toLowerCase().contains("date")) {
            return welshDatepromptValue(promptValue);
        }

        return promptValue;
    }

    private String welshDatepromptValue(final String date) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(OUTGOING_PROMPT_DATE_FORMAT);
        final DateTimeFormatter welshFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String welshDate = date;
        try {
            welshDate = LocalDate.parse(date, formatter).format(welshFormatter);
        } catch (DateTimeParseException ex) {
            //do nothing
        }
        return welshDate;
    }

    private String getWelshValueFromFixedListElementList(final String promptValue, final List<FixedListCollection> fixedListsList) {
        final FixedListCollection fixedList = fixedListsList.get(0);
        final List<Elements> elementsList = fixedList.getElements().stream().filter(element -> element.getValue().equalsIgnoreCase(promptValue)).collect(toList());
        if (!elementsList.isEmpty()) {
            final Elements resultElement = elementsList.get(0);
            return resultElement.getWelshValue();
        }
        return "";
    }

    private String replaceDurationInEngToWelsh(final String date) {
        final String replacedYears = date.replace(YEARS_IN_ENGLISH, YEARS_IN_WELSH);
        final String replacedMonths = replacedYears.replace(MONTHS_IN_ENGLISH, MONTHS_IN_WELSH);
        final String replacedWeeks = replacedMonths.replace(WEEKS_IN_ENGLISH, WEEKS_IN_WELSH);
        return replacedWeeks.replace(DAYS_IN_ENGLISH, DAYS_IN_WELSH);
    }

    public void sendNows(final Sender sender, final JsonEnvelope event, final CreateNowsRequest nowsRequest, final List<Target> targets) {

        final GenerateNowsCommand generateNowsCommand = new GenerateNowsCommand();

        getPromptReferenceData(event, targets, nowsRequest.getSharedResultLines());

        generateNowsCommand.setCreateNowsRequest(nowsRequest);

        final List<NowDocumentRequest> nowDocumentRequests = nowsRequestedToDocumentConverter.convert(event, nowsRequest);

        nowDocumentRequests.forEach(nowDocumentRequest -> {
            final JsonObject nowsDocumentOrderJson = objectToJsonObjectConverter.convert(nowDocumentRequest);

            final Envelope<JsonObject> envelope = Enveloper.envelop(nowsDocumentOrderJson)
                    .withName("public.hearing.now-document-requested")
                    .withMetadataFrom(event);
            LOGGER.info("Raising public.hearing.now-document-requested {}", nowsDocumentOrderJson);
            sender.sendAsAdmin(envelopeFrom(envelope.metadata(), envelope.payload()));
        });
    }

    public void sendPendingNows(final Sender sender, final JsonEnvelope event, final CreateNowsRequest nowsRequest, final List<Target> targets) {
        final PendingNowsRequestedCommand pendingNowsRequestedCommand = new PendingNowsRequestedCommand(nowsRequest, targets);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("sending hearing.command.pending-nows-requested {} ", this.objectToJsonObjectConverter.convert(pendingNowsRequestedCommand));
        }
        final Envelope<JsonObject> envelope = Enveloper.envelop(this.objectToJsonObjectConverter.convert(pendingNowsRequestedCommand))
                .withName("hearing.command.pending-nows-requested")
                .withMetadataFrom(event);

        sender.send(envelopeFrom(envelope.metadata(), envelope.payload()));
    }

}
