package uk.gov.moj.cpp.hearing.event;


import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import uk.gov.justice.core.courts.CreateNowsRequest;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.LjaDetails;
import uk.gov.justice.core.courts.Now;
import uk.gov.justice.core.courts.NowType;
import uk.gov.justice.core.courts.NowVariant;
import uk.gov.justice.core.courts.NowVariantAddressee;
import uk.gov.justice.core.courts.NowVariantDefendant;
import uk.gov.justice.core.courts.NowVariantResult;
import uk.gov.justice.core.courts.NowVariantResultText;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Personalisation;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.justice.core.courts.ResultPrompt;
import uk.gov.justice.core.courts.SharedResultLine;
import uk.gov.justice.core.courts.notification.EmailChannel;
import uk.gov.justice.core.courts.nowdocument.DefendantCaseOffence;
import uk.gov.justice.core.courts.nowdocument.FinancialOrderDetails;
import uk.gov.justice.core.courts.nowdocument.NextHearingCourtDetails;
import uk.gov.justice.core.courts.nowdocument.NowDocumentContent;
import uk.gov.justice.core.courts.nowdocument.NowDocumentRequest;
import uk.gov.justice.core.courts.nowdocument.NowResultDefinitionsText;
import uk.gov.justice.core.courts.nowdocument.Nowaddress;
import uk.gov.justice.core.courts.nowdocument.Nowdefendant;
import uk.gov.justice.core.courts.nowdocument.OrderAddressee;
import uk.gov.justice.core.courts.nowdocument.Prompt;
import uk.gov.justice.core.courts.nowdocument.Result;
import uk.gov.justice.hearing.courts.referencedata.CourtCentreOrganisationUnit;
import uk.gov.justice.hearing.courts.referencedata.LocalJusticeAreas;
import uk.gov.justice.services.common.configuration.Value;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.notification.Subscription;
import uk.gov.moj.cpp.hearing.event.nows.EmailNowNotificationChannel;
import uk.gov.moj.cpp.hearing.event.nows.InvalidNotificationException;
import uk.gov.moj.cpp.hearing.event.nows.NowsNotificationDocumentState;
import uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant;
import uk.gov.moj.cpp.hearing.event.nows.SubscriptionClient;
import uk.gov.moj.cpp.hearing.event.order.Prompts;
import uk.gov.moj.cpp.hearing.event.service.CourtHouseReverseLookup;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SuppressWarnings({"squid:S1188", "squid:S1192", "squid:S00112", "squid:S1125", "squid:S2589"})
public class NowsRequestedToDocumentConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(NowsRequestedToDocumentConverter.class);

    private static final String CASE = "CASE";
    private static final String OFFENCE = "OFFENCE";
    private static final String DEFENDANT = "DEFENDANT";
    private static final String TEMPLATE_ID_PROPERTY_NAME = "templateId";
    private static final String FROM_ADDRESS_PROPERTY_NAME = "fromAddress";
    private static final String CASE_URNS_PERSONALISATION_KEY = "caseUrns";
    private static final String COURT_CLERK_NAME_PERSONALISATION_KEY = "courtClerkName";
    private static final String SUBJECT_PERSONALISATION_KEY = "subject";
    private static final String STATUS_MESSAGE_PERSONALISATION_KEY = "statusMessage";
    private static final String STATUS_PERSONALISATION_KEY = "status";
    private static final String NOWS_TYPE_NAME_PERSONALISATION_KEY = "nowsTypeName";
    private static final String NOWS_GENERATED_DATE_PERSONALISATION_KEY = "nowsGeneratedDate";
    private static final String NOWS_GENERATED_TIME_PERSONALISATION_KEY = "nowsGeneratedTime";
    private static final String COURT_CENTRE_NAME_PERSONALISATION_KEY = "courtCentreName";
    private static final String NOWS_URL_PERSONALISATION_KEY = "nowsUrl";
    private static final String NOWS_PDF_URL = "(%s/material-query-api/query/api/rest/material/material/%s?requestPdf=true)";
    private static final String URGENT = "STATUS: URGENT";
    private static final String EMPTY = "";
    private static final String URGENT_MESSAGE = "The listed notice, order or warrant has been marked as URGENT and requires your immediate attention.";
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    public static final String WELSH_LJA_NAME = "welshName";
    public static final String WELSH = "welsh";

    public final Set<String> excludedPromptRefs = new HashSet(Arrays.asList(
            PromptTypesConstant.P_PAYMENT_CARD_REQUIRED_PROMPT_REFERENCE, PromptTypesConstant.P_PARENT_GUARDIAN_TOPAY_PROMPT_REFERENCE));

    private final SubscriptionClient subscriptionClient;
    private final CourtHouseReverseLookup courtHouseReverseLookup;
    private final ReferenceDataService referenceDataService;

    @Inject
    @Value(key = "materialExternalWebLinkBaseUrl")
    private String materialExternalWebLinkBaseUrl;

    @Inject
    public NowsRequestedToDocumentConverter(final SubscriptionClient subscriptionClient, final CourtHouseReverseLookup courtHouseReverseLookup, final ReferenceDataService referenceDataService) {
        this.subscriptionClient = subscriptionClient;
        this.courtHouseReverseLookup = courtHouseReverseLookup;
        this.referenceDataService = referenceDataService;
    }

    public List<NowDocumentRequest> convert(final JsonEnvelope context, final CreateNowsRequest nowsRequested) {

        final List<NowDocumentRequest> nowDocumentRequests = new ArrayList<>();

        final boolean isCrownCourt = JurisdictionType.CROWN.equals(nowsRequested.getHearing().getJurisdictionType());

        nowsRequested.getNows().forEach(selectedNow -> {

            final NowType matchingNowType = getNowType(nowsRequested, selectedNow);

            List<Subscription> subscriptions = new ArrayList<>();

            if (nonNull(context)) {
                subscriptions = subscriptionClient.getAll(context, matchingNowType.getId(), LocalDate.now()).getSubscriptions();

                if (subscriptions.isEmpty()) {
                    LOGGER.error("No subscription retrieved for nowTypeId - {}", matchingNowType.getId());
                } else {
                    LOGGER.info("Displaying subscription for nowTypeId - {}", matchingNowType.getId());
                    subscriptions.forEach(subscription -> LOGGER.info(subscription.toString()));
                }
            }
            for (final NowVariant selectedNowMaterial : selectedNow.getRequestedMaterials()) {
                nowDocumentRequests.add(mapVariant(context, nowsRequested, selectedNowMaterial, selectedNow, matchingNowType, isCrownCourt, subscriptions));
            }
        });

        return nowDocumentRequests;
    }

    private NowDocumentRequest mapVariant(final JsonEnvelope context, final CreateNowsRequest nowsRequested,
                                          final NowVariant selectedNowMaterial,
                                          final Now selectedNow,
                                          final NowType matchingNowType,
                                          final boolean isCrownCourt,
                                          final List<Subscription> subscriptions) {

        final String courtClerkName = format("%s %s", nowsRequested.getCourtClerk().getFirstName(), nowsRequested.getCourtClerk().getLastName());

        final Nowdefendant nowdefendant = getNowdefendant(selectedNowMaterial.getNowVariantDefendant());

        final List<String> caseUrns = getCaseUrns(nowsRequested, selectedNow);

        final String courtCentreName = nowsRequested.getHearing().getCourtCentre().getName();


        final String defendantName = nowdefendant != null ? nowdefendant.getName() : null;

        final List<uk.gov.justice.core.courts.nowdocument.ProsecutionCase> cases = getOrderCases(nowsRequested, selectedNowMaterial);

        final Optional<uk.gov.justice.core.courts.FinancialOrderDetails> financialOrderDetailsOptional =
                isNull(selectedNow.getFinancialOrders()) ? empty()
                        : Optional.of(selectedNow.getFinancialOrders());

        final FinancialOrderDetails financialOrderDetails =
                getFinancialOrderDetails(financialOrderDetailsOptional.orElse(null), selectedNow.getLjaDetails());

        final String subTemplateName = matchingNowType.getSubTemplateName();

        final Optional<UUID> prosecutionCaseOptional = nowsRequested.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .filter(defendant -> defendant.getId().equals(selectedNow.getDefendantId()))
                .map(Defendant::getProsecutionCaseId)
                .findFirst();

        final UUID prosecutionCaseId = prosecutionCaseOptional.orElseThrow(() -> new RuntimeException("Prosecution Case Id not found"));

        final NowsNotificationDocumentState nowsNotificationDocumentState = new NowsNotificationDocumentState()
                .setUsergroups(new ArrayList<>(selectedNowMaterial.getKey().getUsergroups()))
                .setOriginatingCourtCentreId(nowsRequested.getHearing().getCourtCentre().getId())
                .setDefendantName(defendantName)
                .setCourtClerkName(courtClerkName)
                .setCaseUrns(caseUrns)
                .setNowsTypeId(matchingNowType.getId())
                .setJurisdiction(matchingNowType.getJurisdiction())
                .setCourtCentreName(courtCentreName)
                .setOrderName(matchingNowType.getDescription())
                .setPriority(matchingNowType.getPriority())
                .setMaterialId(selectedNowMaterial.getMaterialId())
                .setIsRemotePrintingRequired(selectedNowMaterial.getIsRemotePrintingRequired());

        String ljaCode = null;
        if (nonNull(selectedNow.getLjaDetails())) {
            ljaCode = selectedNow.getLjaDetails().getLjaCode();
        }

        String ljaDetails = null;
        if (nonNull(selectedNow.getLjaDetails())) {
            ljaDetails = selectedNow.getLjaDetails().getLjaName();
        }
        String welshLjaName = "";
        String courtWelshName = "";
        final CourtCentreOrganisationUnit courtCentreOrganisationUnit = getCourtCentreOrganisationUnit(context, nowsRequested.getHearing())
                .orElseThrow(() -> new RuntimeException(format("No Court Centre For Hearing with court centre id %s.", nowsRequested.getHearing().getCourtCentre().getId())));
        if (courtCentreOrganisationUnit.getIsWelsh() != null && courtCentreOrganisationUnit.getIsWelsh()) {
            welshLjaName = getWelshLjaName(context, ljaCode);
            courtWelshName = courtCentreOrganisationUnit.getOucodeL3WelshName();
        }

        final List<EmailChannel> emailChannels = buildEmailNotifications(nowsNotificationDocumentState, subscriptions);

        final NowDocumentRequest.Builder builder = NowDocumentRequest
                .nowDocumentRequest()
                .withHearingId(nowsRequested.getHearing().getId())
                .withCaseId(prosecutionCaseId)
                .withDefendantId(selectedNow.getDefendantId())
                .withIsRemotePrintingRequired(selectedNowMaterial.getIsRemotePrintingRequired())
                .withMaterialId(selectedNowMaterial.getMaterialId())
                .withNowContent(
                        NowDocumentContent.nowDocumentContent()
                                .withCases(cases)
                                .withCaseUrns(caseUrns)
                                .withCourtCentreName(courtCentreName)
                                .withWelshCourtCentreName(courtWelshName)
                                .withCourtClerkName(courtClerkName)
                                .withDefendant(nowdefendant)
                                .withFinancialOrderDetails(financialOrderDetails)
                                .withLjaCode(ljaCode)
                                .withLjaName(ljaDetails)
                                .withWelshLjaName(welshLjaName)
                                .withNextHearingCourtDetails(createNextHearingCourtDetails(selectedNow.getNextHearingCourtDetails()))
                                .withNowResultDefinitionsText(createAdditionalPropertiesForNowResultDefinitionsText(cases))
                                .withNowText(matchingNowType.getStaticText())
                                .withWelshNowText(matchingNowType.getWelshStaticText())
                                .withOrderAddressee(getOrderAddressee(selectedNowMaterial.getNowVariantAddressee()))
                                .withOrderDate(findOrderDate(nowsRequested.getSharedResultLines(), selectedNowMaterial, selectedNow.getReferenceDate()))
                                .withOrderName(matchingNowType.getDescription())
                                .withWelshOrderName(matchingNowType.getWelshDescription())
                                .withSubTemplateName(subTemplateName)
                                .withIsCrownCourt(isCrownCourt)
                                .withAmendmentDate(findAmendmentDate(nowsRequested.getSharedResultLines(), selectedNowMaterial))
                                .build())
                .withNowTypeId(matchingNowType.getId())
                .withTemplateName(matchingNowType.getTemplateName())
                .withPriority(matchingNowType.getPriority())
                .withUsergroups(selectedNowMaterial.getKey().getUsergroups());

        if (!emailChannels.isEmpty()) {
            builder.withEmailNotifications(emailChannels);
        }

        return builder.build();
    }

    private String getWelshLjaName(final JsonEnvelope context, final String ljaCode) {
        String welshLjaName = "";
        if (nonNull(ljaCode)) {
            final LocalJusticeAreas ljaByNationalCourtCode = referenceDataService.getLJAByNationalCourtCode(context, ljaCode);
            if (ljaByNationalCourtCode != null
                    && ljaByNationalCourtCode.getWelshName() != null) {
                welshLjaName = ljaByNationalCourtCode.getWelshName();
            }
        }
        return welshLjaName;
    }

    private Optional<CourtCentreOrganisationUnit> getCourtCentreOrganisationUnit(final JsonEnvelope event, final Hearing hearing) {
        return courtHouseReverseLookup.getCourtCentreById(event, hearing.getCourtCentre().getId());
    }

    private String findAmendmentDate(final List<SharedResultLine> sharedResultLines, final NowVariant selectedNowMaterial) {
        final List<LocalDate> finalAmendmentDates = new ArrayList<>();

        for (final NowVariantResult selectedNowResult : selectedNowMaterial.getNowResults()) {
            final List<LocalDate> amendmentDates = sharedResultLines.stream()
                    .filter(sharedResultLine -> sharedResultLine.getId().equals(selectedNowResult.getSharedResultId()))
                    .filter(sharedResultLine -> nonNull(sharedResultLine.getAmendmentDate()))
                    .map(SharedResultLine::getAmendmentDate)
                    .collect(Collectors.toList());

            finalAmendmentDates.addAll(amendmentDates);
        }

        if (CollectionUtils.isNotEmpty(finalAmendmentDates)) {
            finalAmendmentDates.sort(Comparator.reverseOrder());

            return finalAmendmentDates.get(0).toString();
        }

        return null;
    }

    private List<EmailChannel> buildEmailNotifications(final NowsNotificationDocumentState nowsNotificationDocumentState, final List<Subscription> subscriptions) {

        return subscriptions.stream()
                .filter(subscription -> userGroupMatch(subscription, nowsNotificationDocumentState.getUsergroups()))
                .filter(subscription -> courtCentreMatch(subscription, nowsNotificationDocumentState.getOriginatingCourtCentreId()))
                .map(subscription -> {
                    if (EmailNowNotificationChannel.EMAIL_TYPE.equals(subscription.getChannel())) {
                        return buildEmailChannel(subscription.getDestination(), subscription.getChannelProperties(), nowsNotificationDocumentState);
                    } else {
                        throw new IllegalArgumentException("invalid channel type: " + subscription.getChannel());
                    }
                })
                .collect(Collectors.toList());
    }

    private NowType getNowType(final CreateNowsRequest nowsRequested, final Now selectedNow) {
        return nowsRequested.getNowTypes().stream()
                .filter(nowType -> nowType.getId().equals(selectedNow.getNowsTypeId()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("invalid now type id supplied."));
    }

    private NextHearingCourtDetails createNextHearingCourtDetails(final uk.gov.justice.core.courts.NextHearingCourtDetails nextHearingCourtDetails) {

        NextHearingCourtDetails nextHearingCourtDetails1 = null;

        if (nonNull(nextHearingCourtDetails)) {

            nextHearingCourtDetails1 = NextHearingCourtDetails.nextHearingCourtDetails()
                    .withCourtAddress(toNowAddress(nextHearingCourtDetails.getCourtCentre().getAddress()))
                    .withCourtName(nextHearingCourtDetails.getCourtCentre().getName())
                    .withWelshCourtName(nextHearingCourtDetails.getCourtCentre().getWelshName())
                    .withHearingDate(nextHearingCourtDetails.getHearingDate())
                    .withHearingTime(nextHearingCourtDetails.getHearingTime())
                    .build();
        }

        return nextHearingCourtDetails1;
    }

    private NowResultDefinitionsText createAdditionalPropertiesForNowResultDefinitionsText(final List<uk.gov.justice.core.courts.nowdocument.ProsecutionCase> cases) {

        final NowResultDefinitionsText nowResultDefinitionsText = NowResultDefinitionsText.nowResultDefinitionsText().build();

        final List<Result> defendantCaseOffenceResults = cases.stream()
                .flatMap(caseOffence -> caseOffence.getDefendantCaseOffences().stream())
                .filter(caseOffence -> nonNull(caseOffence.getResults()))
                .flatMap(defendantCaseOffence -> defendantCaseOffence.getResults().stream())
                .collect(toList());

        final List<Result> defendantCaseResults = cases.stream()
                .filter(caseResults -> nonNull(caseResults.getDefendantCaseResults()))
                .flatMap(c -> c.getDefendantCaseResults().stream())
                .collect(toList());

        setAdditionalPropertyForResults(nowResultDefinitionsText, defendantCaseOffenceResults);

        setAdditionalPropertyForResults(nowResultDefinitionsText, defendantCaseResults);

        return nowResultDefinitionsText;
    }

    private void setAdditionalPropertyForResults(final NowResultDefinitionsText nowResultDefinitionsText, final List<Result> results) {
        results.forEach(result -> {
            final Map<String, Object> resultAdditionalProperties = result.getAdditionalProperties();
            final Set<String> keys = resultAdditionalProperties.keySet();
            keys.forEach(key -> {
                        nowResultDefinitionsText.setAdditionalProperty(key, resultAdditionalProperties.get(key));
                        final String labelKey = additionalPropertyLabelKey(key);
                        if (labelKey.toLowerCase().contains(WELSH)) {
                            nowResultDefinitionsText.setAdditionalProperty(labelKey, result.getWelshLabel());
                        } else {
                            nowResultDefinitionsText.setAdditionalProperty(labelKey, result.getLabel());
                        }

                    }
            );
        });
    }

    private String additionalPropertyLabelKey(String key) {
        return ("equivPlea".equals(key) ? "adjournmentReason" : key) + "Label";
    }

    private FinancialOrderDetails getFinancialOrderDetails(final uk.gov.justice.core.courts.FinancialOrderDetails financialOrdersDetails, final LjaDetails ljaDetails) {
        if (nonNull(financialOrdersDetails)) {
            final FinancialOrderDetails.Builder financialOrderDetailsBuilder =
                    FinancialOrderDetails.financialOrderDetails()
                            .withAccountPaymentReference(financialOrdersDetails.getAccountReference())
                            .withTotalAmountImposed(financialOrdersDetails.getTotalAmountImposed())
                            .withTotalBalance(financialOrdersDetails.getTotalBalance())
                            .withIsCrownCourt(financialOrdersDetails.getIsCrownCourt())
                            .withPaymentTermsWelsh(financialOrdersDetails.getPaymentTermsWelsh())
                            .withPaymentTerms(financialOrdersDetails.getPaymentTerms());

            if (ljaDetails != null) {
                financialOrderDetailsBuilder.withAccountingDivisionCode(ljaDetails.getAccountDivisionCode())
                        .withBacsAccountNumber(ljaDetails.getBacsAccountNumber())
                        .withBacsBankName(ljaDetails.getBacsBankName())
                        .withBacsSortCode(ljaDetails.getBacsSortCode())
                        .withEnforcementAddress(toNowAddress(ljaDetails.getEnforcementAddress()))
                        .withEnforcementEmail(ljaDetails.getEnforcementEmail())
                        .withEnforcementPhoneNumber(ljaDetails.getEnforcementPhoneNumber());
            }
            return financialOrderDetailsBuilder.build();
        }
        return null;
    }

    private OrderAddressee getOrderAddressee(final NowVariantAddressee nowVariantAddressee) {
        return
                OrderAddressee.orderAddressee()
                        .withName(nowVariantAddressee.getName())
                        .withAddress(toNowAddress(nowVariantAddressee.getAddress())).build();
    }

    private Nowaddress toNowAddress(final uk.gov.justice.core.courts.Address address) {
        return Nowaddress.nowaddress()
                .withLine1(address.getAddress1())
                .withLine2(address.getAddress2())
                .withLine3(address.getAddress3())
                .withLine4(address.getAddress4())
                .withLine5(address.getAddress5())
                .withPostCode(address.getPostcode())
                .withWelshLine1(address.getWelshAddress1())
                .withWelshLine2(address.getWelshAddress2())
                .withWelshLine3(address.getWelshAddress3())
                .withWelshLine4(address.getWelshAddress4())
                .withWelshLine5(address.getWelshAddress5())
                .build();
    }

    private static <T> T expectOrThrow(T optional, final String message, final Object... args) {
        if (isNull(optional)) {
            throw new RuntimeException(String.format(message, args));
        }
        return optional;
    }


    private String findOrderDate(final List<SharedResultLine> sharedResultLines, final NowVariant material, final LocalDate referenceDate) {

        if (material.getNowResults() != null) {

            for (final NowVariantResult nowResult : material.getNowResults()) {

                final Optional<LocalDate> orderDate = sharedResultLines.stream()
                        .filter(l -> l.getId().equals(nowResult.getSharedResultId()))
                        .map(SharedResultLine::getOrderedDate)
                        .findAny();

                if (orderDate.isPresent()) {
                    return orderDate.get().toString();
                }
            }
        }

        if (nonNull(referenceDate)) {
            return referenceDate.toString();
        }

        return EMPTY;
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

    private String prosecutionCaseRef(final ProsecutionCaseIdentifier id) {
        return isEmpty(id.getCaseURN()) ? id.getProsecutionAuthorityReference() : id.getCaseURN();
    }

    private List<String> getCaseUrns(final CreateNowsRequest nowsRequested, final Now now) {
        return nowsRequested.getHearing().getProsecutionCases().stream()
                .filter(isCaseBelongingToDefendant(now.getDefendantId()))
                .map(c -> prosecutionCaseRef(c.getProsecutionCaseIdentifier()))
                .collect(toList());
    }

    private Predicate<ProsecutionCase> isCaseBelongingToDefendant(final UUID defendantId) {
        return c -> c.getDefendants().stream().anyMatch(d -> d.getId().equals(defendantId));
    }

    private Nowdefendant getNowdefendant(final NowVariantDefendant nowVariantDefendant) {
        final Nowdefendant.Builder builder = Nowdefendant.nowdefendant()
                .withName(nowVariantDefendant.getName())
                .withAddress(toNowAddress(nowVariantDefendant.getAddress()));

        if (nonNull(nowVariantDefendant.getDateOfBirth())) {
            builder.withDateOfBirth(nowVariantDefendant.getDateOfBirth().toString());
        }
        return builder.build();
    }

    @SuppressWarnings({"squid:S3776", "squid:S134"})
    private List<uk.gov.justice.core.courts.nowdocument.ProsecutionCase> getOrderCases(final CreateNowsRequest nowsRequested, final NowVariant selectedMaterial) {

        if (isNull(selectedMaterial.getNowResults())) {
            return new ArrayList<>();
        }

        final Map<String, uk.gov.justice.core.courts.nowdocument.ProsecutionCase> orderCasesMap = new HashMap<>();

        final List<String> caseRefs = new ArrayList<>();

        final Map<String, Set<Result>> defendantCaseResultsMap = new HashMap<>();

        final Map<String, Set<DefendantCaseOffence>> defendantCaseOffencesMap = new HashMap<>();

        final Map<UUID, List<Result>> offenceResults = new HashMap<>();

        final Map<String, List<Offence>> caseOffencesMap = new HashMap<>();

        for (final NowVariantResult selectedNowResult : selectedMaterial.getNowResults()) {

            nowsRequested.getSharedResultLines().stream()
                    .filter(sharedResultLine -> sharedResultLine.getId().equals(selectedNowResult.getSharedResultId()))
                    .findAny()
                    .ifPresent(sharedResultLine -> {

                        final List<Prompts> orderPrompts = preparePrompts(selectedNowResult, sharedResultLine);

                        final ProsecutionCase prosecutionCase = nowsRequested.getHearing().getProsecutionCases().stream()
                                .filter(isCaseBelongingToDefendant(expectOrThrow(sharedResultLine.getDefendantId(), "empty defendant id")))
                                .findAny()
                                .orElseThrow(() -> new IllegalArgumentException("invalid data, could not find case for defendant supplied by result line"));

                        final String caseRef = prosecutionCaseRef(prosecutionCase.getProsecutionCaseIdentifier());

                        if (!caseRefs.contains(caseRef)) {
                            caseRefs.add(caseRef);
                        }

                        defendantCaseResultsMap.putIfAbsent(caseRef, new HashSet<>());

                        defendantCaseOffencesMap.putIfAbsent(caseRef, new HashSet<>());

                        if (CASE.equalsIgnoreCase(sharedResultLine.getLevel()) || DEFENDANT.equalsIgnoreCase(sharedResultLine.getLevel())) {

                            final List<Prompt> promptList = orderPrompts.stream()
                                    .map(prompts ->
                                            Prompt.prompt()
                                                    .withLabel(prompts.getLabel())
                                                    .withValue(prompts.getValue())
                                                    .withWelshLabel(prompts.getWelshLabel())
                                                    .withWelshValue(prompts.getWelshValue())
                                                    .build())
                                    .collect(Collectors.toList());

                            final Result defendantCaseResults = Result.result()
                                    .withWelshLabel(sharedResultLine.getWelshLabel())
                                    .withLabel(sharedResultLine.getLabel())
                                    .withPrompts(promptList)
                                    .build();

                            final NowVariantResultText nowVariantResultTextOptional = selectedNowResult.getNowVariantResultText();

                            if (nonNull(nowVariantResultTextOptional)) {

                                final Map<String, Object> additionalProperties = nowVariantResultTextOptional.getAdditionalProperties();

                                additionalProperties.forEach((k, v) -> defendantCaseResults.getAdditionalProperties().put(k, v));
                            }

                            defendantCaseResultsMap.get(caseRef).add(defendantCaseResults);
                        }

                        if (OFFENCE.equalsIgnoreCase(sharedResultLine.getLevel())) {

                            final Result results = getResults(selectedNowResult, sharedResultLine.getLabel(), sharedResultLine.getWelshLabel(), orderPrompts);

                            prosecutionCase.getDefendants().stream()
                                    .filter(d -> d.getId().equals(sharedResultLine.getDefendantId()))
                                    .flatMap(d -> d.getOffences().stream())
                                    .filter(o -> o.getId().equals(sharedResultLine.getOffenceId()))
                                    .findAny()
                                    .ifPresent(offence -> {

                                        final List<Offence> offenceList = caseOffencesMap.getOrDefault(caseRef, new ArrayList<>());

                                        offenceList.add(offence);

                                        caseOffencesMap.put(caseRef, offenceList);

                                        final List<Result> resultsList = offenceResults.getOrDefault(offence.getId(), new ArrayList<>());

                                        resultsList.add(results);

                                        offenceResults.put(offence.getId(), resultsList);
                                    });
                        }
                    });
        }

        caseRefs.forEach(caseRef -> {

            final Set<Result> defendantCaseResults = defendantCaseResultsMap.get(caseRef);

            final Set<DefendantCaseOffence> defendantCaseOffences = defendantCaseOffencesMap.get(caseRef);

            final List<Offence> offences = caseOffencesMap.get(caseRef);
            if (nonNull(offences)) {
                offences.forEach(offence -> {
                    final LocalDate startDate = offence.getStartDate();
                    final LocalDate convictionDate = offence.getConvictionDate();
                    defendantCaseOffences.add(DefendantCaseOffence.defendantCaseOffence()
                            .withConvictionDate(nonNull(convictionDate) ? convictionDate.toString() : "")
                            .withStartDate(nonNull(startDate) ? startDate.toString() : "")
                            .withTitle(offence.getOffenceTitle())
                            .withWelshTitle(offence.getOffenceTitleWelsh())
                            .withWording(offence.getWording())
                            .withWelshWording(ofNullable(offence.getWordingWelsh()).filter(a -> isNotBlank(a) && !"N/A".equalsIgnoreCase(a)).orElse(offence.getWording()))
                            .withResults(offenceResults.get(offence.getId()))
                            .build());
                });
            }
            orderCasesMap.put(caseRef, uk.gov.justice.core.courts.nowdocument.ProsecutionCase.prosecutionCase()
                    .withUrn(caseRef)
                    .withDefendantCaseResults(new ArrayList<>(defendantCaseResults))
                    .withDefendantCaseOffences(new ArrayList<>(defendantCaseOffences)).build());
        });

        return new ArrayList<>(orderCasesMap.values());
    }

    private Result getResults(final NowVariantResult selectedNowResult, final String label, final String welshLabel, final List<Prompts> orderPrompts) {

        final NowVariantResultText nowVariantResultTextOptional = selectedNowResult.getNowVariantResultText();

        final List<Prompt> promptList = orderPrompts.stream()
                .map(prompts ->
                        Prompt.prompt()
                                .withLabel(prompts.getLabel())
                                .withValue(prompts.getValue())
                                .withWelshLabel(prompts.getWelshLabel())
                                .withWelshValue(prompts.getWelshValue())
                                .build())
                .collect(Collectors.toList());

        if (nonNull(nowVariantResultTextOptional)) {

            final Map<String, Object> additionalProperties = nowVariantResultTextOptional.getAdditionalProperties();

            final Result results = Result.result()
                    .withLabel(label)
                    .withWelshLabel(welshLabel)
                    .withPrompts(promptList)
                    .build();

            additionalProperties.forEach((k, v) -> results.getAdditionalProperties().put(k, v));

            return results;

        } else {
            return Result.result()
                    .withLabel(label)
                    .withWelshLabel(welshLabel)
                    .withPrompts(promptList)
                    .build();
        }
    }

    public final Set<String> EXCLUDED_PROMPT_REFS = new HashSet(Arrays.asList(
            PromptTypesConstant.P_PAYMENT_CARD_REQUIRED_PROMPT_REFERENCE, PromptTypesConstant.P_PARENT_GUARDIAN_TOPAY_PROMPT_REFERENCE));

    private List<Prompts> preparePrompts(final NowVariantResult selectedNowResult, final SharedResultLine sharedResultLine) {
        final List<ResultPrompt> nowResultPrompts = getMatchingPrompts(selectedNowResult, sharedResultLine);
        return nowResultPrompts.stream()
                .filter(prompt->isEmpty(prompt.getPromptReference()) || !excludedPromptRefs.contains(prompt.getPromptReference()))
                .map(prompt -> new Prompts(prompt.getLabel(), prompt.getValue(), prompt.getWelshLabel(), prompt.getWelshValue()))
                .collect(toList());
    }

    private boolean userGroupMatch(final Subscription subscription, final List<String> usergroups) {
        if (subscription.getUserGroups() == null || subscription.getUserGroups().isEmpty()) {
            return true;
        } else {
            return usergroups.stream().anyMatch(ug -> subscription.getUserGroups().contains(ug));
        }
    }

    private boolean courtCentreMatch(final Subscription subscription, final UUID courtCentreId) {
        if (subscription.getCourtCentreIds() == null || subscription.getCourtCentreIds().isEmpty()) {
            return true;
        } else {
            return subscription.getCourtCentreIds().contains(courtCentreId);
        }
    }

    private EmailChannel buildEmailChannel(final String destination, final Map<String, String> properties, final NowsNotificationDocumentState nowsNotificationDocumentState) {

        final EmailChannel emailNotification = EmailChannel.emailChannel().build();

        emailNotification.setSendToAddress(destination);
        final String templateId = properties.get(TEMPLATE_ID_PROPERTY_NAME);

        if (isNull(templateId)) {
            throw new InvalidNotificationException(String.format("Null template id for \"%s\"", emailNotification.getSendToAddress()));
        }

        final boolean urgent = isNowTypeUrgent(nowsNotificationDocumentState.getPriority());

        final String caseUrns = String.join(",", nowsNotificationDocumentState.getCaseUrns());

        final String nowTypeName = nowsNotificationDocumentState.getOrderName();

        final ZonedDateTime now = ZonedDateTime.now();

        final Map<String, Object> personalisation = new HashMap<>();
        personalisation.put(CASE_URNS_PERSONALISATION_KEY, caseUrns);
        personalisation.put(COURT_CLERK_NAME_PERSONALISATION_KEY, nowsNotificationDocumentState.getCourtClerkName());
        personalisation.put(SUBJECT_PERSONALISATION_KEY, createSubject(nowTypeName, caseUrns, urgent));
        personalisation.put(STATUS_MESSAGE_PERSONALISATION_KEY, urgent ? URGENT_MESSAGE : EMPTY);
        personalisation.put(STATUS_PERSONALISATION_KEY, urgent ? URGENT : EMPTY);
        personalisation.put(NOWS_TYPE_NAME_PERSONALISATION_KEY, nowsNotificationDocumentState.getOrderName());
        personalisation.put(NOWS_GENERATED_DATE_PERSONALISATION_KEY, now.format(dateFormatter));
        personalisation.put(NOWS_GENERATED_TIME_PERSONALISATION_KEY, now.format(timeFormatter));
        personalisation.put(COURT_CENTRE_NAME_PERSONALISATION_KEY, nowsNotificationDocumentState.getCourtCentreName());
        personalisation.put(NOWS_URL_PERSONALISATION_KEY, getNowsPdfFileUrl(nowsNotificationDocumentState.getMaterialId()));
        emailNotification.setPersonalisation(new Personalisation(personalisation));

        try {
            emailNotification.setTemplateId(UUID.fromString(templateId));
        } catch (IllegalArgumentException ex) {
            throw new InvalidNotificationException(String.format("cant notify %s invalid template id: \"%s\"", emailNotification.getSendToAddress(), templateId), ex);
        }

        final String replyToAddress = properties.get(FROM_ADDRESS_PROPERTY_NAME);

        emailNotification.setReplyToAddress(replyToAddress);

        return emailNotification;
    }

    private boolean isNowTypeUrgent(final String priority) {
        final int URGENT_PRIORITY = 30;
        try {
            return nonNull(priority) && Integer.parseInt(priority) <= URGENT_PRIORITY;
        } catch (NumberFormatException ex) {
            LOGGER.error("Exception occurred while converting priority - {} ", priority, ex);
        }
        return false;
    }

    private String createSubject(final String nowTypeName, final String caseUrns, final boolean urgent) {
        return urgent ?
                String.format("%s - %s - %s", URGENT, nowTypeName, caseUrns) :
                String.format("%s - %s", nowTypeName, caseUrns);
    }

    private String getNowsPdfFileUrl(final UUID materialId) {
        return String.format(NOWS_PDF_URL, materialExternalWebLinkBaseUrl, materialId.toString());
    }
}
