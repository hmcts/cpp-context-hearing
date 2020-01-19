package uk.gov.moj.cpp.hearing.event.delegates;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.CreateNowsRequest;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Level;
import uk.gov.justice.core.courts.Now;
import uk.gov.justice.core.courts.NowVariant;
import uk.gov.justice.core.courts.NowVariantResult;
import uk.gov.justice.core.courts.NowVariantResultText;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.ResultPrompt;
import uk.gov.justice.core.courts.SharedResultLine;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.core.courts.notification.EmailChannel;
import uk.gov.justice.core.courts.nowdocument.DefendantCaseOffence;
import uk.gov.justice.core.courts.nowdocument.Prompt;
import uk.gov.justice.core.courts.nowdocument.Result;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.order.Prompts;
import uk.gov.moj.cpp.hearing.nces.Defendant;
import uk.gov.moj.cpp.hearing.nces.DocumentContent;
import uk.gov.moj.cpp.hearing.nces.FinancialOrderForDefendant;
import uk.gov.moj.cpp.hearing.nces.UpdateDefendantWithFinancialOrderDetails;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NcesNotificationDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(NcesNotificationDelegate.class);

    private static final String OFFENCE = "OFFENCE";
    private static final UUID EMAIL_NOTIF_TEMPLATE_ID = fromString("e1da43dc-da04-4adb-b6ff-47748749932a");

    private final Enveloper enveloper;
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Inject
    public NcesNotificationDelegate(final Enveloper enveloper,
                                    final ObjectToJsonObjectConverter objectToJsonObjectConverter) {
        this.enveloper = enveloper;
        this.objectToJsonObjectConverter = objectToJsonObjectConverter;
    }

    private static <T> T expectOrThrowIllegalArgumentException(T optional, final String message, final Object... args) {
        if (isNull(optional)) {
            throw new IllegalArgumentException(String.format(message, args));
        }
        return optional;
    }

    public void updateDefendantWithFinancialOrder(final Sender sender, final JsonEnvelope event, final NowsRequested nowsRequested, final UUID requestId) {
        final Optional<Now> matchingNow = nowsRequested.getCreateNowsRequest().getNows()
                .stream()
                .filter(now -> now.getId().equals(requestId))
                .findFirst();

        if (matchingNow.isPresent()) {
            final Now now = matchingNow.get();
            LOGGER.info("updating defendant '{}' with financial order for request with ID '{}'", now.getDefendantId(), requestId);
            sender.sendAsAdmin(this.enveloper.withMetadataFrom(event, "hearing.command.update-defendant-with-financial-order")
                    .apply(this.objectToJsonObjectConverter.convert(createCommandToUpdateDefendant(nowsRequested, now))));
        } else {
            LOGGER.warn("No matching now found for request with ID '{}'", requestId);
        }

    }

    private UpdateDefendantWithFinancialOrderDetails createCommandToUpdateDefendant(NowsRequested nowsRequested, Now now) {
        final ProsecutionCase prosecutionCase = findProsecutionCaseByDefendantId(nowsRequested, now.getDefendantId());
        return UpdateDefendantWithFinancialOrderDetails.newBuilder()
                .withFinancialOrderForDefendant(createFinancialOrderForDefendant(nowsRequested, now, prosecutionCase))
                .build();
    }

    private FinancialOrderForDefendant createFinancialOrderForDefendant(NowsRequested nowsRequested,
                                                                        Now now,
                                                                        ProsecutionCase prosecutionCase) {
        return FinancialOrderForDefendant.newBuilder()
                .withCaseId(prosecutionCase.getId())//case
                .withDefendantId(now.getDefendantId())
                .withHearingId(nowsRequested.getCreateNowsRequest().getHearing().getId())
                .withMaterialId(UUID.randomUUID())
                .withResultDefinitionIds(extractResultsDefinitionIds(nowsRequested.getTargets(), now.getDefendantId()))
                .withDocumentContent(createDocumentContent(nowsRequested, now, prosecutionCase))
                .withEmailNotifications(singletonList(createEmailNotif(now)))
                .build();
    }

    private EmailChannel createEmailNotif(Now now) {
        return EmailChannel.emailChannel()
                .withSendToAddress(now.getLjaDetails().getEnforcementEmail())
                .withTemplateId(EMAIL_NOTIF_TEMPLATE_ID)
                .build();
    }

    private ProsecutionCase findProsecutionCaseByDefendantId(NowsRequested nowsRequested, UUID defendantId) {
        return nowsRequested.getCreateNowsRequest().getHearing().getProsecutionCases().stream()
                .filter(s -> s.getDefendants()
                        .stream()
                        .anyMatch(d -> d.getId().equals(defendantId)))
                .findFirst()
                .orElseThrow(() -> noDefendantIdInProsecutionCasesException(defendantId));
    }

    private DocumentContent createDocumentContent(NowsRequested nowsRequested, Now now, ProsecutionCase prosecutionCase) {
        return DocumentContent.documentContent()
                .withCourtCentreName(getCourtCentreName(nowsRequested))
                .withUrn(getUrn(prosecutionCase))//case
                .withAdjustmentDetails("") // TODO : No mapping found
                .withAmendmentType("NCES Notification") // TODO : Email Subject based on business condition
                .withDivisionCode(now.getLjaDetails().getAccountDivisionCode())
                .withGobAccountNumber(nowsRequested.getAccountNumber())
                .withAmendmentDate(LocalDate.now())
                .withDefendantCaseOffences(getDefendantCaseOffences(nowsRequested, now))
                .withDefendant(convertToDefendant(findPersonByDefendantId(prosecutionCase, now.getDefendantId())))
                .build();
    }

    private String getUrn(ProsecutionCase prosecutionCase) {
        ProsecutionCaseIdentifier prosecutionCaseIdentifier = prosecutionCase.getProsecutionCaseIdentifier();
        return Optional.ofNullable(prosecutionCaseIdentifier.getCaseURN())
                .orElse(prosecutionCaseIdentifier.getProsecutionAuthorityReference());
    }

    private List<DefendantCaseOffence> getDefendantCaseOffences(NowsRequested nowsRequested, Now now) {
        return now.getRequestedMaterials()
                .stream()
                .map(material -> getDefendantCaseOffencesPerMaterial(nowsRequested.getCreateNowsRequest(), material))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private String getCourtCentreName(NowsRequested nowsRequested) {
        return Optional.of(nowsRequested)
                .map(NowsRequested::getCreateNowsRequest)
                .map(CreateNowsRequest::getHearing)
                .map(Hearing::getCourtCentre)
                .map(CourtCentre::getName)
                .orElse("");
    }

    private Defendant convertToDefendant(Person person) {
        return Defendant.defendant()
                .withName(String.format("%s %s %s", person.getFirstName(), person.getMiddleName(), person.getLastName()))
                .withDateOfBirth(person.getDateOfBirth())
                .build();
    }

    private Person findPersonByDefendantId(ProsecutionCase prosecutionCase, UUID defendantId) {
        return prosecutionCase.getDefendants()
                .stream()
                .filter(s -> s.getId().equals(defendantId))
                .findFirst().orElseThrow(() -> noDefendantIdInProsecutionCasesException(defendantId))
                .getPersonDefendant().getPersonDetails();
    }

    private RuntimeException noDefendantIdInProsecutionCasesException(UUID defendantId) {
        return new RuntimeException(
                String.format("There is no defendant in the prosecution cases with id : %s",
                        defendantId));
    }

    @SuppressWarnings({"squid:S1188"})
    private List<DefendantCaseOffence> getDefendantCaseOffencesPerMaterial(final CreateNowsRequest nowsRequested, final NowVariant selectedMaterial) {

        if (isNull(selectedMaterial.getNowResults())) {
            return new ArrayList<>();
        }

        final List<DefendantCaseOffence> defendantCaseOffences = new ArrayList<>();
        final List<String> caseRefs = new ArrayList<>();
        final Map<String, Set<DefendantCaseOffence>> defendantCaseOffencesMap = new HashMap<>();
        final Map<UUID, List<Result>> offenceResults = new HashMap<>();
        final Map<String, List<Offence>> caseOffencesMap = new HashMap<>();

        for (final NowVariantResult selectedNowResult : selectedMaterial.getNowResults()) {
            nowsRequested.getSharedResultLines().stream()
                    .filter(sharedResultLine -> sharedResultLine.getId().equals(selectedNowResult.getSharedResultId()))
                    .findAny()
                    .ifPresent(sharedResultLine -> {
                        final List<Prompts> orderPrompts = preparePrompts(selectedNowResult, sharedResultLine);
                        final ProsecutionCase prosecutionCase = findProsecutionCaseByDefendantId(nowsRequested, sharedResultLine.getDefendantId());
                        final String caseRef = prosecutionCaseRef(prosecutionCase.getProsecutionCaseIdentifier());

                        if (!caseRefs.contains(caseRef)) {
                            caseRefs.add(caseRef);
                        }
                        defendantCaseOffencesMap.putIfAbsent(caseRef, new HashSet<>());

                        if (isOffenceLevel(sharedResultLine)) {
                            prosecutionCase.getDefendants().stream()
                                    .filter(d -> d.getId().equals(sharedResultLine.getDefendantId()))
                                    .flatMap(d -> d.getOffences().stream())
                                    .filter(o -> o.getId().equals(sharedResultLine.getOffenceId()))
                                    .findAny()
                                    .ifPresent(offence -> {
                                        final Result results = getResults(selectedNowResult, sharedResultLine.getLabel(), orderPrompts);
                                        addOffenceToTheCase(caseOffencesMap, caseRef, offence);
                                        addResultsToTheOffence(offenceResults, results, offence);
                                    });
                        }
                    });
        }

        caseRefs.forEach(caseRef -> {
            final Set<DefendantCaseOffence> defendantCaseOffencesByCaseRef = defendantCaseOffencesMap.get(caseRef);
            final List<Offence> offences = caseOffencesMap.get(caseRef);
            if (nonNull(offences)) {
                offences.forEach(offence -> {
                    List<Result> results = offenceResults.get(offence.getId());
                    defendantCaseOffencesByCaseRef.add(createDefendantCaseOffence(results, offence));
                });
            }
            defendantCaseOffences.addAll(defendantCaseOffencesByCaseRef);
        });

        return defendantCaseOffences;
    }

    private void addResultsToTheOffence(Map<UUID, List<Result>> offenceResults, Result results, Offence offence) {
        final List<Result> resultsList = offenceResults.getOrDefault(offence.getId(), new ArrayList<>());
        resultsList.add(results);
        offenceResults.put(offence.getId(), resultsList);
    }

    private void addOffenceToTheCase(Map<String, List<Offence>> caseOffencesMap, String caseRef, Offence offence) {
        final List<Offence> offenceList = caseOffencesMap.getOrDefault(caseRef, new ArrayList<>());
        offenceList.add(offence);
        caseOffencesMap.put(caseRef, offenceList);
    }

    private List<Prompt> getPrompts(List<Prompts> orderPrompts) {
        return orderPrompts.stream()
                .map(prompts -> new Prompt(prompts.getLabel(), prompts.getValue(), prompts.getWelshLabel(), prompts.getWelshValue()))
                .collect(Collectors.toList());
    }

    private boolean isOffenceLevel(SharedResultLine sharedResultLine) {
        return OFFENCE.equalsIgnoreCase(sharedResultLine.getLevel());
    }

    private DefendantCaseOffence createDefendantCaseOffence(List<Result> results, Offence offence) {
        LocalDate convictionDate = offence.getConvictionDate();
        LocalDate startDate = offence.getStartDate();
        return DefendantCaseOffence.defendantCaseOffence()
                .withConvictionDate(nonNull(convictionDate) ? convictionDate.toString() : "")
                .withStartDate(nonNull(startDate) ? startDate.toString() : "")
                .withWording(offence.getWording())
                .withResults(results)
                .build();
    }

    private ProsecutionCase findProsecutionCaseByDefendantId(CreateNowsRequest createNowsRequested, UUID defendantId) {
        return createNowsRequested.getHearing().getProsecutionCases().stream()
                .filter(isCaseBelongingToDefendant(expectOrThrowIllegalArgumentException(defendantId, "empty defendant id")))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("invalid data, could not find case for defendant supplied by result line"));

    }

    private List<Prompts> preparePrompts(final NowVariantResult selectedNowResult, final SharedResultLine sharedResultLine) {
        final List<ResultPrompt> nowResultPrompts = getMatchingPrompts(selectedNowResult, sharedResultLine);
        return nowResultPrompts.stream()
                .map(prompt -> new Prompts(prompt.getLabel(), prompt.getValue(), prompt.getWelshLabel(), prompt.getWelshValue()))
                .collect(toList());
    }

    private List<ResultPrompt> getMatchingPrompts(final NowVariantResult selectedNowResult, final SharedResultLine sharedResultLine) {
        if (selectedNowResult.getPromptRefs() == null || sharedResultLine.getPrompts() == null) {
            return emptyList();
        } else {
            final List<UUID> nowResultPromptLabels = new ArrayList<>(selectedNowResult.getPromptRefs());
            return sharedResultLine.getPrompts().stream()
                    .filter(prompt -> nowResultPromptLabels.contains(prompt.getId()))
                    .collect(toList());
        }
    }

    private Predicate<ProsecutionCase> isCaseBelongingToDefendant(final UUID defendantId) {
        return c -> c.getDefendants().stream().anyMatch(d -> d.getId().equals(defendantId));
    }

    private String prosecutionCaseRef(final ProsecutionCaseIdentifier id) {
        return isEmpty(id.getCaseURN()) ? id.getProsecutionAuthorityReference() : id.getCaseURN();
    }

    private Result getResults(final NowVariantResult selectedNowResult, final String label, final List<Prompts> orderPrompts) {

        final NowVariantResultText nowVariantResultTextOptional = selectedNowResult.getNowVariantResultText();

        final Result results = Result.result()
                .withLabel(label)
                .withPrompts(getPrompts(orderPrompts))
                .build();

        addPropertiesIfNowVariantResultTextIsPresent(nowVariantResultTextOptional, results);
        return results;
    }

    private void addPropertiesIfNowVariantResultTextIsPresent(NowVariantResultText nowVariantResultTextOptional, Result results) {
        if (nonNull(nowVariantResultTextOptional)) {
            final Map<String, Object> additionalProperties = nowVariantResultTextOptional.getAdditionalProperties();
            additionalProperties.forEach((k, v) -> results.getAdditionalProperties().put(k, v));
        }
    }

    private List<UUID> extractResultsDefinitionIds(List<Target> targets, UUID defendantId) {
        return targets.stream()
                .filter(target -> Objects.equals(target.getDefendantId(), defendantId)
                        && target.getResultLines() != null
                )
                .flatMap(sharedResultLine -> sharedResultLine.getResultLines().stream())
                .filter(resultLine -> Level.OFFENCE == resultLine.getLevel())
                .map(ResultLine::getResultDefinitionId)
                .collect(toList());
    }
}