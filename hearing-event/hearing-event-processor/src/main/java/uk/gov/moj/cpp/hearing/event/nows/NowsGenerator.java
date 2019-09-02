package uk.gov.moj.cpp.hearing.event.nows;

import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.EMPLOYER_ORGANISATION_ADDRESS1_PROMPT_REFERENCE;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.EMPLOYER_ORGANISATION_ADDRESS2_PROMPT_REFERENCE;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.EMPLOYER_ORGANISATION_ADDRESS3_PROMPT_REFERENCE;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.EMPLOYER_ORGANISATION_ADDRESS4_PROMPT_REFERENCE;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.EMPLOYER_ORGANISATION_ADDRESS5_PROMPT_REFERENCE;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.EMPLOYER_ORGANISATION_NAME_PROMPT_REFERENCE;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.EMPLOYER_ORGANISATION_POST_CODE_PROMPT_REFERENCE;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.EMPLOYER_ORGANISATION_REFERENCE_NUMBER_PROMPT_REFERENCE;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_NON_STANDARD_REASON;
import static uk.gov.moj.cpp.hearing.event.nows.PromptUtil.extractByPromptReference;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.ATTACHMENT_OF_EARNINGS_NOW_DEFINITION_ID;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.ATTACHMENT_OF_EARNINGS_RESULT_DEFINITION_ID;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.BENEFIT_DEDUCTIONS_RESULT_DEFINITION_ID;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.NOTICE_OF_FINANCIAL_PENALTY_NOW_DEFINITION_ID;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.DocumentationLanguage;
import uk.gov.justice.core.courts.FinancialOrderDetails;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.LjaDetails;
import uk.gov.justice.core.courts.NextHearing;
import uk.gov.justice.core.courts.NextHearingCourtDetails;
import uk.gov.justice.core.courts.Now;
import uk.gov.justice.core.courts.NowVariant;
import uk.gov.justice.core.courts.NowVariantAddressee;
import uk.gov.justice.core.courts.NowVariantDefendant;
import uk.gov.justice.core.courts.NowVariantKey;
import uk.gov.justice.core.courts.NowVariantResult;
import uk.gov.justice.core.courts.NowVariantResultText;
import uk.gov.justice.core.courts.Organisation;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.HearingAdjourned;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowResultDefinitionRequirement;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S1188", "squid:S2384", "squid:S1135", "squid:S2259", "squid:S1612", "squid:S134",
        "squid:S1172", "squid:S3400", "squid:S00112", "squid:S3776", "squid:S3864"})
public class NowsGenerator {

    public static final String INITIAL_MATERIAL_STATUS = "requesting";
    public static final String NEXT_HEARING_START_DATE_FORMAT = "yyyy-MM-dd";
    public static final String NEXT_HEARING_START_TIME_FORMAT = "HH:mm";
    public static final String EUROPE_LONDON = "Europe/London";
    private static final String ADJOURNMENT_REASON = "adjournmentReason";
    private static final Logger LOGGER = LoggerFactory.getLogger(NowsGenerator.class.getName());
    private static final List<String> ATTACHMENT_OF_EARNINGS_PROMPT_REFERENCES = asList(EMPLOYER_ORGANISATION_NAME_PROMPT_REFERENCE,
            EMPLOYER_ORGANISATION_ADDRESS1_PROMPT_REFERENCE,
            EMPLOYER_ORGANISATION_ADDRESS2_PROMPT_REFERENCE,
            EMPLOYER_ORGANISATION_ADDRESS3_PROMPT_REFERENCE,
            EMPLOYER_ORGANISATION_ADDRESS4_PROMPT_REFERENCE,
            EMPLOYER_ORGANISATION_ADDRESS5_PROMPT_REFERENCE,
            EMPLOYER_ORGANISATION_POST_CODE_PROMPT_REFERENCE,
            EMPLOYER_ORGANISATION_REFERENCE_NUMBER_PROMPT_REFERENCE);
    private final ReferenceDataService referenceDataService;
    private final FinancialResultCalculator financialResultCalculator;
    private final PaymentTermsCalculator paymentTermsCalculator;

    @Inject
    public NowsGenerator(final ReferenceDataService referenceDataService, final FinancialResultCalculator financialResultCalculator,
                         PaymentTermsCalculator paymentTermsCalculator) {
        this.referenceDataService = referenceDataService;
        this.financialResultCalculator = financialResultCalculator;
        this.paymentTermsCalculator = paymentTermsCalculator;
    }

    private static List<ResultLine> uncompletedResultLinesForDefendant(final ResultsShared resultsShared, final Defendant defendant) {
        return resultsShared.getTargets().stream()
                .filter(target -> target.getDefendantId() != null)
                .filter(target -> target.getDefendantId().equals(defendant.getId()))
                .flatMap(target -> target.getResultLines().stream())
                .filter(resultLine -> !resultLine.getIsComplete())
                .collect(Collectors.toList());
    }

    private static List<NowResultDefinitionRequirement> mandatoryResultLineNotPresent(final Set<UUID> completedResultDefinitionIds, final NowDefinition nowDefinition) {
        return nowDefinition.getResultDefinitions().stream().filter(resultDefinition -> resultDefinition.getMandatory() &&
                !completedResultDefinitionIds.contains(resultDefinition.getId())).collect(Collectors.toList());
    }

    protected static NowVariantResultText nowVariantResultText(final NowResultDefinitionRequirement nowsRequirementRow, final Map<UUID, Prompt> id2PromptRef, final ResultLine resultLine) {

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(String.format("nowVariantResultText_nowReference: '%s'  text: '%s' ",
                    nowsRequirementRow.getNowReference(), nowsRequirementRow.getText()));
        }

        if (nowsRequirementRow.getNowReference() != null && nowsRequirementRow.getNowReference().trim().length() > 0) {
            final NowVariantResultText.Builder builder = NowVariantResultText.nowVariantResultText();
            String text = isNull(nowsRequirementRow.getText()) ? "" : nowsRequirementRow.getText();

            if (nowsRequirementRow.getNowReference().equalsIgnoreCase(ADJOURNMENT_REASON)) {
                text = text + extractByPromptReference(id2PromptRef, P_NON_STANDARD_REASON, Collections.singletonList(resultLine)).orElse("");
            }
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(String.format("**** nowVariantResultText nowReference: '%s'  text: '%s' ",
                        nowsRequirementRow.getNowReference(), nowsRequirementRow.getText()));
            }

            builder
                    .withAdditionalProperty(nowsRequirementRow.getNowReference(), text)
                    .withAdditionalProperty(nowsRequirementRow.getNowReference() + ".welsh", nowsRequirementRow.getWelshText() != null ? nowsRequirementRow.getWelshText() : text);
            return builder.build();
        }

        return null;
    }

    private static Address getAddress(final Defendant defendant) {
        if (defendant.getPersonDefendant() != null && defendant.getPersonDefendant().getPersonDetails() != null) {
            return defendant.getPersonDefendant().getPersonDetails().getAddress();
        } else if (defendant.getLegalEntityDefendant() != null) {
            return defendant.getLegalEntityDefendant().getOrganisation().getAddress();
        } else {
            return null;
        }
    }

    private static String getName(final Defendant defendant) {
        if (defendant.getPersonDefendant() != null && defendant.getPersonDefendant().getPersonDetails() != null) {
            final Person person = defendant.getPersonDefendant().getPersonDetails();
            return person.getFirstName() + (person.getMiddleName() == null ? "" : (" " + person.getMiddleName())) +
                    " " + person.getLastName();
        }
        if (defendant.getLegalEntityDefendant() != null) {
            return defendant.getLegalEntityDefendant().getOrganisation().getName();
        }
        return null;
    }

    private static LocalDate getDateOfBirth(final Defendant defendant) {
        if (defendant.getPersonDefendant() != null && defendant.getPersonDefendant().getPersonDetails() != null) {
            final Person person = defendant.getPersonDefendant().getPersonDetails();
            return person.getDateOfBirth();
        } else {
            return null;
        }

    }

    private static boolean isJurisdictionMatch(final NowDefinition nowDefinition, final JurisdictionType jurisdictionType) {

        String nowJurisdiction = nowDefinition.getJurisdiction();
        if (nowDefinition.getJurisdiction() == null) {
            //GPE-7138 empty jurisdiction is allowed
            return true;
        }
        nowJurisdiction = nowJurisdiction.toUpperCase();

        if (nowJurisdiction.startsWith("M")) {
            return jurisdictionType == JurisdictionType.MAGISTRATES;
        }
        if (nowJurisdiction.startsWith("C")) {
            return jurisdictionType == JurisdictionType.CROWN;
        }
        if (nowJurisdiction.startsWith("B")) {
            return true;
        }
        //TODO GPE-7138 blow up if there is a reference data error !
        throw new RuntimeException(String.format("nowDefinition %s %s has unexpected jurisdiction \"%s\"",
                nowDefinition.getName(), nowDefinition.getId(), nowDefinition.getJurisdiction()));
    }

    public List<Now> createNows(final JsonEnvelope context, final ResultsShared resultsShared, final HearingAdjourned hearingAdjourned) {

        final List<Now> nows = new ArrayList<>();

        final Map<UUID, List<ResultLine>> resultLinesByDefendant = groupResultLinesByDefendant(resultsShared);

        final GenerateVariantDecisionMakerFactory generateVariantDecisionMakerFactory = new GenerateVariantDecisionMakerFactory()
                .setVariantDirectory(resultsShared.getVariantDirectory())
                .setCompletedResultLineStatuses(resultsShared.getCompletedResultLinesStatus())
                .setTargets(resultsShared.getTargets());

        resultLinesByDefendant.forEach((defendantId, completedResultLines4Defendant)
                -> nows.addAll(createNowsForDefendant(context, resultsShared.getHearing(), defendantId, completedResultLines4Defendant, generateVariantDecisionMakerFactory, hearingAdjourned)));
        nows.forEach(now ->
                now.setDocumentationLanguage(documentationLanguage(resultsShared.getHearing().getHearingLanguage()))
        );

        return nows;
    }

    private boolean isHearingAdjourned(final List<ResultLine> resultLines4Now) {
        return resultLines4Now.stream()
                .anyMatch(rl -> rl.getResultDefinitionId().equals(ResultDefinitionsConstant.NEXT_HEARING_IN_MAGISTRATES_COURT_RESULT_DEFINITION_ID));
    }

    private DocumentationLanguage documentationLanguage(final HearingLanguage hearingLanguage) {
        if (HearingLanguage.WELSH.equals(hearingLanguage)) {
            return DocumentationLanguage.WELSH;
        } else {
            return DocumentationLanguage.ENGLISH;
        }
    }

    private Map<UUID, List<ResultLine>> groupResultLinesByDefendant(ResultsShared resultsShared) {

        final Map<UUID, List<ResultLine>> completedResultLines4DefendantMap = new HashMap<>();

        if (resultsShared.getHearing().getProsecutionCases() != null) {
            resultsShared.getHearing().getProsecutionCases().stream().flatMap(pc -> pc.getDefendants().stream()).collect(toList()).forEach(defendant -> {

                final List<ResultLine> uncompletedResultLinesForDefendant = uncompletedResultLinesForDefendant(resultsShared, defendant);
                if (!uncompletedResultLinesForDefendant.isEmpty()) {
                    if (LOGGER.isInfoEnabled()) {
                        final StringBuilder sbError = new StringBuilder();
                        sbError.append(String.format("aborting NOWs generation for defendant %s as there are uncompleted result lines:", defendant.getId()));
                        uncompletedResultLinesForDefendant.forEach(
                                resultLine -> sbError.append(String.format(", %s", resultLine.toString()))
                        );
                        LOGGER.info(sbError.toString());
                    }
                    return; //we don't generate any NOW for the defendant if they have any uncompleted result lines.
                }

                final List<ResultLine> completedResultLines4Defendant = resultsShared.getTargets().stream()
                        .filter(target -> target.getDefendantId() != null)
                        .filter(target -> target.getDefendantId().equals(defendant.getId()))
                        .flatMap(target -> target.getResultLines().stream())
                        .filter(ResultLine::getIsComplete)
                        .filter(r -> (isNull(r.getIsDeleted()) || !r.getIsDeleted()))
                        .collect(toList());

                completedResultLines4DefendantMap.put(defendant.getId(), completedResultLines4Defendant);
            });
        }

        return completedResultLines4DefendantMap;
    }

    private List<Now> createNowsForDefendant(final JsonEnvelope context, final Hearing hearing, final UUID defendantId,
                                             final List<ResultLine> resultLines,
                                             final GenerateVariantDecisionMakerFactory generateVariantDecisionMakerFactory,
                                             final HearingAdjourned hearingAdjourned
    ) {

        final Set<UUID> completedResultDefinitionIds = resultLines.stream()
                .map(ResultLine::getResultDefinitionId)
                .collect(toSet());

        final List<Now> results = new ArrayList<>();

        final Set<NowDefinition> candidateNowDefinitions = findNowDefinitions(context, hearing.getJurisdictionType(), resultLines);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(String.format("considering these nows for hearing %s defendant %s : %s", hearing.getId(),
                    defendantId, candidateNowDefinitions.stream().map(nd -> "" + nd.getId() + "/" + nd.getName()).collect(Collectors.joining(","))
            ));
        }


        for (final NowDefinition nowDefinition : candidateNowDefinitions) {

            final List<NowResultDefinitionRequirement> mandatoryResultsNotPresent = mandatoryResultLineNotPresent(completedResultDefinitionIds, nowDefinition);

            if (!mandatoryResultsNotPresent.isEmpty()) {
                if (LOGGER.isInfoEnabled()) {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(String.format("aborting NOW generation for defendant: %s nowDefinition: %s as not all mandatory results are present", defendantId, nowDefinition.getId()));
                    mandatoryResultsNotPresent.forEach(
                            r -> sb.append(String.format(", id: %s %s %s %s", r.getId(), r.getNowReference(), r.getPrimary(), r.getText()))
                    );
                    LOGGER.info(sb.toString());
                }
                return Collections.emptyList();
            }

            final Set<UUID> resultDefinitionIds4Now = nowDefinition.getResultDefinitions().stream()
                    .map(NowResultDefinitionRequirement::getId)
                    .collect(toSet());

            final List<ResultLine> resultLines4Now = resultLines.stream()
                    .filter(l -> resultDefinitionIds4Now.contains(l.getResultDefinitionId()))
                    .collect(toList());

            final Now now = createNow(context, nowDefinition, resultLines4Now, hearing, defendantId,
                    generateVariantDecisionMakerFactory.buildFor(defendantId,
                            nowDefinition));

            if (!now.getRequestedMaterials().isEmpty()) {
                results.add(now);
                insertAdjournData(now, resultLines4Now, hearingAdjourned);
            } else {
                LOGGER.info("aborting NOW generation {} for defendant {} as no requested materials present", defendantId, nowDefinition.getId());
            }
        }

        if (results.isEmpty()) {
            LOGGER.error("No Now's Meta data found for Result Lines - {}", resultLines.stream().map(ResultLine::getResultDefinitionId).collect(Collectors.toList()));
        }

        return results;
    }

    private void insertAdjournData(final Now now, final List<ResultLine> resultLines4Now, final HearingAdjourned hearingAdjourned) {
        //check for hearing adourned
        if (hearingAdjourned != null && isHearingAdjourned(resultLines4Now)) {
            final NextHearing nextHearing = hearingAdjourned.getNextHearings().get(0);
            final ZonedDateTime localDateTime = ZonedDateTime.ofInstant(nextHearing.getListedStartDateTime().toInstant(), ZoneOffset.UTC)
                    .withZoneSameInstant(ZoneId.of(EUROPE_LONDON));
            now.setNextHearingCourtDetails(NextHearingCourtDetails.nextHearingCourtDetails()
                    .withCourtCentre(nextHearing.getCourtCentre())
                    .withHearingDate(localDateTime.format(DateTimeFormatter.ofPattern(NEXT_HEARING_START_DATE_FORMAT)))
                    .withHearingTime(localDateTime.format(DateTimeFormatter.ofPattern(NEXT_HEARING_START_TIME_FORMAT)))
                    .build());
        }
    }

    private Map<UUID, Prompt> id2PromptRef(JsonEnvelope context, List<ResultLine> resultLines4NowIn) {
        final Map<UUID, Prompt> id2PromptRef = new HashMap<>();
        for (final ResultLine resultLine : resultLines4NowIn) {
            final ResultDefinition resultDefinition = referenceDataService.getResultDefinitionById(context, resultLine.getOrderedDate(),
                    resultLine.getResultDefinitionId());
            if (resultDefinition != null && resultDefinition.getPrompts() != null) {
                for (final Prompt p : resultDefinition.getPrompts()) {
                    id2PromptRef.put(p.getId(), p);
                }
            }
        }
        return id2PromptRef;
    }

    private LjaDetails ljaDetails(final JsonEnvelope context, final Hearing hearing) {
        if (JurisdictionType.CROWN.equals(hearing.getJurisdictionType())) {
            return null;
        } else {
            return referenceDataService.getLjaDetailsByCourtCentreId(context, hearing.getCourtCentre().getId());
        }
    }

    private Now createNow(final JsonEnvelope context, final NowDefinition nowDefinition,
                          List<ResultLine> resultLines4NowIn, final Hearing hearing, final UUID defendantId,
                          final GenerateVariantDecisionMaker generateVariantDecisionMaker) {

        //The userGroups of the prompts are not bounded by the userGroups of the resultDefinition.  I'm told that the userGroups
        //of the resultDefinition is more of a default for when no prompts are present.

        final Map<NowVariantInternal, List<String>> variantToUserGroupsMappings = calculateVariants(context, resultLines4NowIn);

        final List<NowVariant> materials = new ArrayList<>();

        final List<ResultDefinition> financialResultDefinitionList = new ArrayList<>();

        final Map<UUID, Prompt> id2PromptRef = id2PromptRef(context, resultLines4NowIn);

        final boolean isNowNoticeOfFinancialPenalty = nowDefinition.getId().equals(NOTICE_OF_FINANCIAL_PENALTY_NOW_DEFINITION_ID);
        final boolean isAttachmentOfEarningsOrder = nowDefinition.getId().equals(ATTACHMENT_OF_EARNINGS_NOW_DEFINITION_ID);
        final boolean hasDeductionsOfBenefitsResult = resultLines4NowIn.stream()
                .anyMatch(resultLine -> resultLine.getResultDefinitionId().equals(BENEFIT_DEDUCTIONS_RESULT_DEFINITION_ID));
        final boolean hasAttachmentOfEarningsResult = resultLines4NowIn.stream()
                .anyMatch(resultLine -> resultLine.getResultDefinitionId().equals(ATTACHMENT_OF_EARNINGS_RESULT_DEFINITION_ID));

        final boolean removePaymentTerms = (isNowNoticeOfFinancialPenalty || isAttachmentOfEarningsOrder) && (hasAttachmentOfEarningsResult || hasDeductionsOfBenefitsResult);

        final List<ResultLine> resultLines4Now = resultLines4NowIn;

        final boolean isCrownCourt = hearing.getJurisdictionType() == JurisdictionType.CROWN;

        final FinancialOrderDetails.Builder financialOrderDetailsBuilder = financialOrderDetails(id2PromptRef, hasAttachmentOfEarningsResult, resultLines4Now)
                .withIsCrownCourt(isCrownCourt);

        final Set<UUID> promptIdsToExclude = hasAttachmentOfEarningsResult ? findAttachmentOfEarningsPrompts(resultLines4Now, id2PromptRef) : new HashSet<>();

        if (removePaymentTerms) {
            financialOrderDetailsBuilder.withPaymentTerms(null);
        }

        final Defendant defendant = hearing.getProsecutionCases().stream().flatMap(pc -> pc.getDefendants().stream())
                .filter(d -> d.getId().equals(defendantId)).findFirst().orElse(null);

        final NowVariantDefendant.Builder nowVariantDefendant = NowVariantDefendant.nowVariantDefendant();
        nowVariantDefendant.withAddress(getAddress(defendant));
        nowVariantDefendant.withName(getName(defendant));
        nowVariantDefendant.withDateOfBirth(getDateOfBirth(defendant));

        final NowVariantAddressee.Builder nowVariantAddressee = nowVariantAddressee((isAttachmentOfEarningsOrder && hasAttachmentOfEarningsResult), financialOrderDetailsBuilder, defendant);

        final int variantCount = variantToUserGroupsMappings.size();

        variantToUserGroupsMappings.forEach((variant, userGroups) -> {

            final GenerateVariantDecisionMaker.Decision decision = generateVariantDecisionMaker.decide(userGroups);

            if (!decision.isShouldGenerate()) {
                LOGGER.info("NOW variant is not generated on direction of decision maker");
                return;
            }
            final Set<ResultLine> resultLines4Variant = resultLines4Now.stream()
                    .filter(resultLine -> variant.getResultDefinitionsIds().contains(resultLine.getResultDefinitionId()))
                    .collect(toSet());

            final List<NowVariantResult> nowResults = resultLines4Variant.stream()
                    .map(resultLine -> map(resultLine, context, nowDefinition, variant,
                            id2PromptRef, promptIdsToExclude, financialResultDefinitionList))
                    .collect(toList());

            materials.add(NowVariant.nowVariant()
                    .withIsRemotePrintingRequired(variantCount == 1 && Boolean.TRUE.equals(nowDefinition.getRemotePrintingRequired()) && !isCrownCourt)
                    .withKey(NowVariantKey.nowVariantKey()
                            .withDefendantId(defendantId)
                            .withHearingId(hearing.getId())
                            .withNowsTypeId(nowDefinition.getId())
                            .withUsergroups(userGroups)
                            .build())
                    .withNowResults(nowResults.isEmpty() ? null : nowResults)
                    .withMaterialId(UUID.randomUUID())
                    .withIsAmended(decision.isAmended())
                    .withTemplateName(nowDefinition.getTemplateName())
                    .withSubTemplateName(nowDefinition.getSubTemplateName())
                    .withDescription(nowDefinition.getName())
                    .withStatus(INITIAL_MATERIAL_STATUS)
                    .withNowVariantAddressee(nowVariantAddressee.build())
                    .withNowVariantDefendant(nowVariantDefendant.build())
                    .build()
            );
        });

        //lets make the materials always return in a fixed order
        materials.sort((m1, m2) -> {
            final String ug1 = m1.getKey().getUsergroups().stream().sorted().collect(Collectors.joining(""));
            final String ug2 = String.join("", m2.getKey().getUsergroups());
            return ug1.compareTo(ug2);
        });

        final boolean isFinancial = !financialResultDefinitionList.isEmpty();
        final Optional<String> accountNumber = !isFinancial ? Optional.empty() : Optional.of("TBC");
        financialOrderDetailsBuilder.withAccountReference(accountNumber.orElse(null)).build();

        return Now.now()
                .withId(UUID.randomUUID())
                .withDefendantId(defendantId)
                .withNowsTypeId(nowDefinition.getId())
                .withRequestedMaterials(materials)
                .withFinancialOrders(isFinancial ? financialOrderDetailsBuilder.build() : null)
                .withReferenceDate(resultLines4NowIn.stream().map(ResultLine::getOrderedDate).findFirst().orElse(LocalDate.now()))
                .withLjaDetails(ljaDetails(context, hearing))
                .build();
    }

    private NowVariantResult map(final ResultLine resultLine, JsonEnvelope context, final NowDefinition nowDefinition,
                                 final NowVariantInternal variant, final Map<UUID, Prompt> id2PromptRef,
                                 final Set<UUID> promptIdsToExclude, final List<ResultDefinition> financialResultDefinitionList) {
        final ResultDefinition resultDefinition = referenceDataService.getResultDefinitionById(context, resultLine.getOrderedDate(),
                resultLine.getResultDefinitionId());
        final NowResultDefinitionRequirement now2ResultDefinitionRelation = nowDefinition.getResultDefinitions()
                .stream()
                .filter(rel -> rel.getId().equals(resultDefinition.getId()))
                .findFirst().orElse(null);
        final List<UUID> promptRefs = resultDefinition.getPrompts().stream()
                .filter(prompt -> variant.getResultPromptIds().contains(prompt.getId())) //filter out prompts that this variant should not have.
                .peek(promptDef -> id2PromptRef.put(promptDef.getId(), promptDef))
                .map(Prompt::getId)
                .filter(id -> !promptIdsToExclude.contains(id))
                .collect(toList());

        if (resultDefinition.isFinancial()) {
            financialResultDefinitionList.add(resultDefinition);
        }

        return NowVariantResult.nowVariantResult()
                .withSharedResultId(resultLine.getResultLineId())
                .withSequence(resultDefinition.getRank())
                .withNowVariantResultText(nowVariantResultText(now2ResultDefinitionRelation, id2PromptRef, resultLine))
                .withPromptRefs(promptRefs.isEmpty() ? null : promptRefs)
                .build();

    }

    private NowVariantAddressee.Builder nowVariantAddressee(final boolean hasAttachmentOfEarningsResult,
                                                            final FinancialOrderDetails.Builder financialOrderDetailsBuilder, final Defendant defendant
    ) {
        final FinancialOrderDetails financialOrderDetails = financialOrderDetailsBuilder.build();
        final NowVariantAddressee.Builder nowVariantAddressee = NowVariantAddressee.nowVariantAddressee();
        //should be for now attachment of earnings order only
        if (hasAttachmentOfEarningsResult) {
            nowVariantAddressee
                    .withAddress(financialOrderDetails.getEmployerOrganisation().getAddress())
                    .withName(financialOrderDetails.getEmployerOrganisation().getName());
        } else {
            nowVariantAddressee
                    .withAddress(getAddress(defendant))
                    .withName(getName(defendant));

        }
        return nowVariantAddressee;
    }

    private Set<UUID> findAttachmentOfEarningsPrompts(final List<ResultLine> resultLines4Now, final Map<UUID, Prompt> id2PromptRef) {

        return resultLines4Now.stream().flatMap(resultLine -> resultLine.getPrompts().stream())
                .filter(prompt -> {
                    final String reference = id2PromptRef.containsKey(prompt.getId()) ? id2PromptRef.get(prompt.getId()).getReference() : null;
                    return reference != null && ATTACHMENT_OF_EARNINGS_PROMPT_REFERENCES.contains(reference);
                })
                .map(uk.gov.justice.core.courts.Prompt::getId)
                .collect(toSet());
    }

    private FinancialOrderDetails.Builder financialOrderDetails(final Map<UUID, Prompt> id2PromptRef, boolean isAttachmentOfEarningsOrder, final List<ResultLine> resultLines4Now) {
        final FinancialOrderDetails.Builder result = FinancialOrderDetails.financialOrderDetails();
        final FinancialResultCalculator.FinancialResult financialResult = financialResultCalculator.calculate(id2PromptRef, resultLines4Now);
        result.withTotalAmountImposed(financialResult.getTotalAmountImposed());
        result.withTotalBalance(financialResult.getTotalBalance());
        result.withPaymentTerms(paymentTermsCalculator.calculatePaymentTerms(id2PromptRef, resultLines4Now));

        if (!isAttachmentOfEarningsOrder) {
            return result;
        }

        final String organisationName = extractByPromptReference(id2PromptRef, EMPLOYER_ORGANISATION_NAME_PROMPT_REFERENCE, resultLines4Now).orElse(null);

        final String address1 = extractByPromptReference(id2PromptRef, EMPLOYER_ORGANISATION_ADDRESS1_PROMPT_REFERENCE, resultLines4Now).orElse(null);

        if (isNull(organisationName) && isNull(address1)) {
            return result;
        }

        return result
                .withEmployerPayrollReference(extractByPromptReference(id2PromptRef, EMPLOYER_ORGANISATION_REFERENCE_NUMBER_PROMPT_REFERENCE, resultLines4Now).orElse(null))
                .withEmployerOrganisation(Organisation.organisation()
                        .withName(organisationName)
                        .withAddress(Address.address()
                                .withAddress1(address1)
                                .withAddress2(extractByPromptReference(id2PromptRef, EMPLOYER_ORGANISATION_ADDRESS2_PROMPT_REFERENCE, resultLines4Now).orElse(null))
                                .withAddress3(extractByPromptReference(id2PromptRef, EMPLOYER_ORGANISATION_ADDRESS3_PROMPT_REFERENCE, resultLines4Now).orElse(null))
                                .withAddress4(extractByPromptReference(id2PromptRef, EMPLOYER_ORGANISATION_ADDRESS4_PROMPT_REFERENCE, resultLines4Now).orElse(null))
                                .withAddress5(extractByPromptReference(id2PromptRef, EMPLOYER_ORGANISATION_ADDRESS5_PROMPT_REFERENCE, resultLines4Now).orElse(null))
                                .withPostcode(extractByPromptReference(id2PromptRef, EMPLOYER_ORGANISATION_POST_CODE_PROMPT_REFERENCE, resultLines4Now).orElse(null))
                                .build())
                        .build());
    }

    private Map<NowVariantInternal, List<String>> calculateVariants(final JsonEnvelope context, final List<ResultLine> resultLines4Now) {
        final Map<NowVariantInternal, List<String>> variantToUserGroupsMappings = new HashMap<>();

        for (final String userGroup : extractUserGroupsFromResultLinesAndPrompts(context, resultLines4Now)) {

            final Set<UUID> resultDefinitionsIds4UserGroup = resultLines4Now.stream().map(
                    resultLine -> referenceDataService.getResultDefinitionById(context, resultLine.getOrderedDate(), resultLine.getResultDefinitionId()))
                    .filter(resultDefinition -> resultDefinition.getUserGroups().stream().anyMatch(ug -> ug.equals(userGroup)) ||
                            resultDefinition.getPrompts().stream().anyMatch(p -> p.getUserGroups().stream().anyMatch(ug -> ug.equals(userGroup)))
                    )
                    .map(ResultDefinition::getId)
                    .collect(toSet());

            final Set<UUID> resultPromptIds4UserGroup = resultLines4Now.stream()
                    .map(resultLine -> referenceDataService.getResultDefinitionById(context, resultLine.getOrderedDate(), resultLine.getResultDefinitionId()))
                    .flatMap(resultDefinition -> resultDefinition.getPrompts().stream())
                    .filter(resultPrompt -> resultPrompt.getUserGroups().stream().anyMatch(ug -> ug.equals(userGroup)))
                    .map(Prompt::getId)
                    .collect(toSet());

            variantToUserGroupsMappings.computeIfAbsent(new NowVariantInternal(resultDefinitionsIds4UserGroup, resultPromptIds4UserGroup), v -> new ArrayList<>())
                    .add(userGroup);
        }
        return variantToUserGroupsMappings;
    }

    private Set<String> extractUserGroupsFromResultLinesAndPrompts(final JsonEnvelope context,
                                                                   final List<ResultLine> resultLines) {

        return resultLines.stream()
                .flatMap(resultLine -> {
                            final ResultDefinition resultDefinition = referenceDataService
                                    .getResultDefinitionById(context, resultLine.getOrderedDate(),
                                            resultLine.getResultDefinitionId());
                            if (resultDefinition == null) {
                                throw new RuntimeException(String.format("failed to getResultDefinitionById orderDate: %s, resultDefinitionID: %s",
                                        resultLine.getOrderedDate(),
                                        resultLine.getResultDefinitionId()));
                            }
                            return Stream.concat(
                                    resultDefinition.getUserGroups().stream(),
                                    resultDefinition.getPrompts().stream().flatMap(prompt -> prompt.getUserGroups().stream())

                            );
                        }
                ).collect(toSet());
    }

    private Set<NowDefinition> findNowDefinitions(final JsonEnvelope context, final JurisdictionType jurisdictionType, final List<ResultLine> resultLines) {
        return resultLines.stream()
                .map(resultLine -> referenceDataService
                        .getNowDefinitionByPrimaryResultDefinitionId(context,
                                resultLine.getOrderedDate(),
                                resultLine.getResultDefinitionId()))
                .flatMap(Collection::stream)
                .filter(nowDefinition -> isJurisdictionMatch(nowDefinition, jurisdictionType))
                .collect(toSet());
    }

    private static class NowVariantInternal {
        private final Set<UUID> resultDefinitionsIds;
        private final Set<UUID> resultPromptIds;

        NowVariantInternal(final Set<UUID> resultDefinitionsIds, final Set<UUID> resultPromptIds) {
            this.resultDefinitionsIds = new HashSet<>(resultDefinitionsIds);
            this.resultPromptIds = new HashSet<>(resultPromptIds);
        }

        Set<UUID> getResultDefinitionsIds() {
            return resultDefinitionsIds;
        }

        Set<UUID> getResultPromptIds() {
            return resultPromptIds;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final NowVariantInternal that = (NowVariantInternal) o;
            return Objects.equals(resultDefinitionsIds, that.resultDefinitionsIds) &&
                    Objects.equals(resultPromptIds, that.resultPromptIds);
        }

        @Override
        public int hashCode() {
            return Objects.hash(resultDefinitionsIds, resultPromptIds);
        }
    }
}
