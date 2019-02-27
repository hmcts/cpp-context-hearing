package uk.gov.moj.cpp.hearing.event.nows;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsIn.isIn;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.FUTURE_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.ATTACHMENT_OF_EARNINGS_NOW_DEFINITION_ID;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.ATTACHMENT_OF_EARNINGS_RESULT_DEFINITION_ID;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.EMPLOYER_ORGANISATION_ADDRESS1_PROMPT_REFERENCE;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.EMPLOYER_ORGANISATION_ADDRESS2_PROMPT_REFERENCE;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.EMPLOYER_ORGANISATION_ADDRESS3_PROMPT_REFERENCE;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.EMPLOYER_ORGANISATION_ADDRESS4_PROMPT_REFERENCE;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.EMPLOYER_ORGANISATION_ADDRESS5_PROMPT_REFERENCE;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.EMPLOYER_ORGANISATION_NAME_PROMPT_REFERENCE;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.EMPLOYER_ORGANISATION_REFERENCE_NUMBER_PROMPT_REFERENCE;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_AEOC;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.target;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CompletedResultLineStatusTemplates.completedResultLineStatus;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.second;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.FinancialOrderDetails;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.LjaDetails;
import uk.gov.justice.core.courts.NextHearing;
import uk.gov.justice.core.courts.NextHearingCourtDetails;
import uk.gov.justice.core.courts.Now;
import uk.gov.justice.core.courts.NowVariant;
import uk.gov.justice.core.courts.NowVariantAddressee;
import uk.gov.justice.core.courts.NowVariantDefendant;
import uk.gov.justice.core.courts.NowVariantResult;
import uk.gov.justice.core.courts.NowVariantResultText;
import uk.gov.justice.core.courts.Organisation;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.ResultLineReference;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantKey;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantValue;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.domain.event.HearingAdjourned;
import uk.gov.moj.cpp.hearing.event.NowsTemplates;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowResultDefinitionRequirement;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;
import uk.gov.moj.cpp.hearing.test.Pair;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({"squid:S1607"})
public class NowsGeneratorTest {

    private final ResultDefinition resultDefinition = ResultDefinition.resultDefinition()
            .setId(randomUUID())
            .setPrompts(singletonList(Prompt.prompt()
                    .setId(randomUUID())
                    .setLabel("Lock him up")
                    .setUserGroups(singletonList("Court Clerk"))
            ));
    private final NowDefinition nowDefinition = NowDefinition.now()
            .setId(UUID.randomUUID())
            .setResultDefinitions(singletonList(
                    NowResultDefinitionRequirement.resultDefinitions()
                            .setId(resultDefinition.getId())
                            .setMandatory(true)
            ));

    private final Organisation attachmentOfEarningsExpectedOrganisation = Organisation.organisation()
            .withName(STRING.next())
            .withAddress(Address.address()
                    .withAddress1(STRING.next())
                    .withAddress2(STRING.next())
                    .withAddress3(STRING.next())
                    .withAddress4(STRING.next())
                    .withAddress5(STRING.next())
                    .build())
            .build();

    private final ResultDefinition adjournmentResultDefinition = ResultDefinition.resultDefinition()
            .setId(ResultDefinitionsConstant.NEXT_HEARING_IN_MAGISTRATES_COURT_RESULT_DEFINITION_ID)
            .setPrompts(singletonList(Prompt.prompt()
                    .setId(randomUUID())
                    .setLabel("Lock him up")
                    .setUserGroups(singletonList("Court Clerk"))
            ));
    private final NowDefinition adjournmentNowDefinition = NowDefinition.now()
            .setId(UUID.randomUUID())
            .setResultDefinitions(singletonList(
                    NowResultDefinitionRequirement.resultDefinitions()
                            .setId(adjournmentResultDefinition.getId())
                            .setMandatory(true)
            ));


    private UUID namePromptUUID = UUID.randomUUID();
    private UUID address1PromptUUID = UUID.randomUUID();
    private UUID address2PromptUUID = UUID.randomUUID();
    private UUID address3PromptUUID = UUID.randomUUID();
    private UUID address4PromptUUID = UUID.randomUUID();
    private UUID address5PromptUUID = UUID.randomUUID();
    private UUID employeePayRollRefUUID = UUID.randomUUID();
    private String employeePayRolReference = STRING.next();
    private UUID totalAmountPromptId = UUID.randomUUID();

    private List<String> userGroupList = asList("Court Clerk");

    private Prompt prompt(UUID id, String label, String reference, List<String> userGroupList) {
        return Prompt.prompt()
                .setId(id)
                .setLabel(label)
                .setReference(reference)
                .setUserGroups(userGroupList);
    }

    private final ResultDefinition attachmentOfEarningsResultDefinition = ResultDefinition.resultDefinition()
            .setId(ATTACHMENT_OF_EARNINGS_RESULT_DEFINITION_ID)
            .setFinancial("Y")
            //TODO GPE-7138 expand this to the list in FinancialOrderDetailsConstants
            .setPrompts(asList(Prompt.prompt()
                            .setId(randomUUID())
                            .setLabel("Lock him up")
                            .setUserGroups(userGroupList),
                    // prompt(totalAmountPromptId, "totalAmount", null, userGroupList),
                    prompt(employeePayRollRefUUID, "employerPayrollRef", EMPLOYER_ORGANISATION_REFERENCE_NUMBER_PROMPT_REFERENCE, userGroupList),
                    prompt(namePromptUUID, "name", EMPLOYER_ORGANISATION_NAME_PROMPT_REFERENCE, userGroupList),
                    prompt(address1PromptUUID, "addr1", EMPLOYER_ORGANISATION_ADDRESS1_PROMPT_REFERENCE, userGroupList),
                    prompt(address2PromptUUID, "addr2", EMPLOYER_ORGANISATION_ADDRESS2_PROMPT_REFERENCE, userGroupList),
                    prompt(address3PromptUUID, "addr3", EMPLOYER_ORGANISATION_ADDRESS3_PROMPT_REFERENCE, userGroupList),
                    prompt(address4PromptUUID, "addr4", EMPLOYER_ORGANISATION_ADDRESS4_PROMPT_REFERENCE, userGroupList),
                    prompt(address5PromptUUID, "addr5", EMPLOYER_ORGANISATION_ADDRESS5_PROMPT_REFERENCE, userGroupList)
            ));


    private final NowDefinition attachmentOfEarningsNowDefinition(final UUID id) {
        final List<NowResultDefinitionRequirement> nowResultDefinitionRequirements = new ArrayList<>();

        nowResultDefinitionRequirements.add(
                NowResultDefinitionRequirement.resultDefinitions()
                        .setId(attachmentOfEarningsResultDefinition.getId())
                        .setMandatory(true)
                        .setText(STRING.next())
                        .setWelshText(STRING.next())
                        .setNowReference(STRING.next())
        );
        // add payment details
        for (final UUID resultDefinitionId : ResultDefinitionsConstant.PAYMENT_TERMS_RESULT_DEFINITIONS) {
            nowResultDefinitionRequirements.add(
                    NowResultDefinitionRequirement.resultDefinitions()
                            .setId(resultDefinitionId)
                            .setMandatory(false)
            );
        }

        return NowDefinition.now()
                .setId(id)
                .setJurisdiction("C")
                .setResultDefinitions(nowResultDefinitionRequirements);
    }

    @Mock
    private ReferenceDataService referenceDataService;

    @Mock
    private FinancialResultCalculator financialResultCalculator;

    @Mock
    private PaymentTermsCalculator paymentTermsCalculator;

    @InjectMocks
    private NowsGenerator target;

    private FinancialResultCalculator.FinancialResult financialResult;

    @Before
    public void setup() {
        financialResult = new FinancialResultCalculator.FinancialResult();
        financialResult.setTotalBalance("9870");
        financialResult.setTotalBalance("9876");

        when(financialResultCalculator.calculate(any(), any())).thenReturn(financialResult);
    }

    @Test
    public void createNows_generateSingleNowForSingleDefendant() {

        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(with(NowsTemplates.resultsSharedTemplate(),
                event ->
                {
                    h(event).getFirstCompletedResultLine().setResultDefinitionId(resultDefinition.getId());
                    h(event).getFirstCompletedResultLineFirstPrompt().setId(resultDefinition.getPrompts().get(0).getId());
                }
        ));

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(resultDefinition.getId()))).thenReturn(new HashSet<>(asList(nowDefinition)));

        when(referenceDataService.getResultDefinitionById(any(), any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        UUID promptId = resultsShared.getFirstCompletedResultLineFirstPrompt().getId();

        List<Now> nows = target.createNows(null, resultsShared.it(), null);

        assertThat(nows, first(isBean(Now.class)
                .with(Now::getDefendantId, is(resultsShared.getFirstDefendant().getId()))
                .with(Now::getNowsTypeId, is(nowDefinition.getId()))
                .with(Now::getRequestedMaterials, first(isBean(NowVariant.class)
                        .with(NowVariant::getMaterialId, is(not(nullValue())))
                        .withValue(m -> m.getKey().getUsergroups().get(0), "Court Clerk"
                        )
                        .with(NowVariant::getNowResults, first(isBean(NowVariantResult.class)
                                .with(NowVariantResult::getSharedResultId, is(resultsShared.getFirstCompletedResultLine().getResultLineId()))
                                .with(nvr -> nvr.getPromptRefs().get(0), is(promptId))
                        ))
                ))
        ));
    }

    @Test
    public void createNows_Adjournment() {

        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(with(NowsTemplates.resultsSharedTemplate(),
                event ->
                {
                    h(event).getFirstCompletedResultLine().setResultDefinitionId(adjournmentResultDefinition.getId());
                    h(event).getFirstCompletedResultLineFirstPrompt().setId(adjournmentResultDefinition.getPrompts().get(0).getId());
                }
        ));

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(adjournmentResultDefinition.getId()))).thenReturn(new HashSet<>(asList(adjournmentNowDefinition)));

        when(referenceDataService.getResultDefinitionById(any(), any(), eq(adjournmentResultDefinition.getId()))).thenReturn(adjournmentResultDefinition);

        UUID promptId = resultsShared.getFirstCompletedResultLineFirstPrompt().getId();

        final Address courtCentreAddress = Address.address()
                .withPostcode("AA1 1AA")
                .withAddress1("aaa1")
                .withAddress2("aaa2")
                .withAddress3("aaa3")
                .withAddress4("aaa4")
                .withAddress5("aaa5")
                .build();
        final CourtCentre nextCourtCentre = CourtCentre.courtCentre()
                .withAddress(courtCentreAddress)
                .withRoomName("room1")
                .withWelshRoomName("wRoom1")
                .withRoomId(UUID.randomUUID())
                .withName("Court of Courts")
                .withWelshName("W court")
                .build();
        final NextHearing nextHearing = NextHearing.nextHearing()
                .withCourtCentre(nextCourtCentre)
                .withEarliestStartDateTime(ZonedDateTime.now())
                .withEstimatedMinutes(123)
                .build();

        final HearingAdjourned hearingAdjourned = new HearingAdjourned(UUID.randomUUID(), asList(nextHearing));

        List<Now> nows = target.createNows(null, resultsShared.it(), hearingAdjourned);

        final String strEarliestStartDateTime = nextHearing.getEarliestStartDateTime().format(DateTimeFormatter.ofPattern(NowsGenerator.NEXT_HEARING_START_DATE_FORMAT));

        assertThat(nows, first(isBean(Now.class)
                .with(Now::getNextHearingCourtDetails, isBean(NextHearingCourtDetails.class)
                        .withValue(NextHearingCourtDetails::getHearingDate, strEarliestStartDateTime)
                        .withValue(NextHearingCourtDetails::getHearingTime, nextHearing.getEarliestStartDateTime().format(DateTimeFormatter.ofPattern(NowsGenerator.NEXT_HEARING_START_TIME_FORMAT)))
                        .with(NextHearingCourtDetails::getCourtCentre, is(nextCourtCentre))
                )
                .with(Now::getDefendantId, is(resultsShared.getFirstDefendant().getId()))
                .with(Now::getNowsTypeId, is(adjournmentNowDefinition.getId()))
                .with(Now::getRequestedMaterials, first(isBean(NowVariant.class)
                        .with(NowVariant::getMaterialId, is(not(nullValue())))
                        .withValue(m -> m.getKey().getUsergroups().get(0), "Court Clerk"
                        )
                        .with(NowVariant::getNowResults, first(isBean(NowVariantResult.class)
                                .with(NowVariantResult::getSharedResultId, is(resultsShared.getFirstCompletedResultLine().getResultLineId()))
                                .with(nvr -> nvr.getPromptRefs().get(0), is(promptId))
                        ))
                ))
        ));
    }

    private uk.gov.justice.core.courts.Prompt resultPrompt(final UUID id, final String label, final String value) {
        return uk.gov.justice.core.courts.Prompt.prompt()
                .withId(id)
                .withLabel(label)
                .withValue(value)
                .build();
    }

    @Test
    public void createNows_generateAttachmentOfEarningsNowForSingleDefendant() {
        createNows_generateAttachmentOfEarningsNowForSingleDefendant(JurisdictionType.CROWN, randomUUID());
    }

    @Ignore  //NOT sure if in this case financial order details should be provided
    @Test
    public void createNows_generateAttachmentOfEarningsNowForSingleDefendantAttachmentOfEarningNow() {
        createNows_generateAttachmentOfEarningsNowForSingleDefendant(JurisdictionType.CROWN, ATTACHMENT_OF_EARNINGS_NOW_DEFINITION_ID);
    }

    @Test
    public void createNows_generateAttachmentOfEarningsNowForSingleDefendantWrongJurisdictionForNowDef() {
        createNows_generateAttachmentOfEarningsNowForSingleDefendant(JurisdictionType.MAGISTRATES, randomUUID());
    }

    public void createNows_generateAttachmentOfEarningsNowForSingleDefendant(final JurisdictionType jurisdictionType, final UUID nowDefinitionId) {

        boolean expectPaymentTermsRemoved =
                nowDefinitionId.equals(ResultDefinitionsConstant.ATTACHMENT_OF_EARNINGS_NOW_DEFINITION_ID) ||
                        nowDefinitionId.equals(ResultDefinitionsConstant.BENEFIT_DEDUCTIONS_RESULT_DEFINITION_ID);

        final NowDefinition attachmentOfEarningsNowDefinition = attachmentOfEarningsNowDefinition(nowDefinitionId);

        final NowResultDefinitionRequirement attachmentOfEarningRequirement = attachmentOfEarningsNowDefinition.getResultDefinitions().stream()
                .filter(req -> req.getId().equals(attachmentOfEarningsResultDefinition.getId())).findAny().orElseThrow(() -> new RuntimeException("attachment of earnings requirement not found"));

        final LocalDate orderDate = LocalDate.now();

        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(with(NowsTemplates.resultsSharedTemplate(),
                event ->
                {
                    h(event).getFirstCompletedResultLine().setResultDefinitionId(attachmentOfEarningsResultDefinition.getId());
                    h(event).getFirstCompletedResultLineFirstPrompt().setId(attachmentOfEarningsResultDefinition.getPrompts().get(0).getId());
                    h(event).getFirstCompletedResultLine().getPrompts().addAll(
                            asList(
                                    resultPrompt(employeePayRollRefUUID, EMPLOYER_ORGANISATION_REFERENCE_NUMBER_PROMPT_REFERENCE, employeePayRolReference),
                                    resultPrompt(namePromptUUID, EMPLOYER_ORGANISATION_NAME_PROMPT_REFERENCE, attachmentOfEarningsExpectedOrganisation.getName()),
                                    resultPrompt(address1PromptUUID, EMPLOYER_ORGANISATION_ADDRESS1_PROMPT_REFERENCE, attachmentOfEarningsExpectedOrganisation.getAddress().getAddress1()),
                                    resultPrompt(address2PromptUUID, EMPLOYER_ORGANISATION_ADDRESS2_PROMPT_REFERENCE, attachmentOfEarningsExpectedOrganisation.getAddress().getAddress2()),
                                    resultPrompt(address3PromptUUID, EMPLOYER_ORGANISATION_ADDRESS3_PROMPT_REFERENCE, attachmentOfEarningsExpectedOrganisation.getAddress().getAddress3()),
                                    resultPrompt(address4PromptUUID, EMPLOYER_ORGANISATION_ADDRESS4_PROMPT_REFERENCE, attachmentOfEarningsExpectedOrganisation.getAddress().getAddress4()),
                                    resultPrompt(address5PromptUUID, EMPLOYER_ORGANISATION_ADDRESS5_PROMPT_REFERENCE, attachmentOfEarningsExpectedOrganisation.getAddress().getAddress5())
                            ));
                    List<ResultLine> resultLinesToAdd =
                            ResultDefinitionsConstant.PAYMENT_TERMS_RESULT_DEFINITIONS.stream()
                                    .filter(strUuid -> !RD_AEOC.equals(strUuid))
                                    .map(strUuid -> ResultLine.resultLine()
                                            .withIsComplete(true)
                                            .withResultDefinitionId(strUuid)
                                            .withResultLineId(UUID.randomUUID())
                                            .withPrompts(asList())
                                            .build())
                                    .collect(Collectors.toList());

                    h(event).getFirstTarget().getResultLines().addAll(resultLinesToAdd);
                    h(event).getFirstTarget().getResultLines().forEach(
                            rl -> rl.setOrderedDate(orderDate)
                    );
                }
        ));

        final Hearing hearing = resultsShared.getHearing();
        hearing.setJurisdictionType(jurisdictionType);

        final JsonEnvelope event = Mockito.mock(JsonEnvelope.class);

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(event, orderDate,
                attachmentOfEarningsResultDefinition.getId())).thenReturn(new HashSet<>(asList(attachmentOfEarningsNowDefinition)));

        for (final UUID uuid : ResultDefinitionsConstant.PAYMENT_TERMS_RESULT_DEFINITIONS) {
            ResultDefinition resultDefinition = ResultDefinition.resultDefinition().setId(uuid).setUserGroups(asList("Court Clerk"));
            when(referenceDataService.getResultDefinitionById(event, orderDate, uuid)).thenReturn(resultDefinition);
        }

        when(referenceDataService.getResultDefinitionById(event, orderDate, attachmentOfEarningsResultDefinition.getId())).thenReturn(attachmentOfEarningsResultDefinition);

        final LjaDetails ljaDetails = JurisdictionType.CROWN.equals(jurisdictionType) ? null : ljaDetails();

        when(referenceDataService.getLjaDetailsByCourtCentreId(event, hearing.getCourtCentre().getId())).thenReturn(ljaDetails);

        when(referenceDataService.getLjaDetailsByCourtCentreId(event, hearing.getCourtCentre().getId())).thenReturn(ljaDetails);

        final UUID promptId = resultsShared.getFirstCompletedResultLineFirstPrompt().getId();

        List<Now> nows = target.createNows(event, resultsShared.it(), null);

        boolean jurisdictionMatch = JurisdictionType.CROWN == jurisdictionType && attachmentOfEarningsNowDefinition.getJurisdiction().equals("C");


        if (!jurisdictionMatch) {
            assertThat(nows.size(), is(0));
        } else {
            final Address expectedAdresseeAddress = attachmentOfEarningsExpectedOrganisation.getAddress();
            final Person defendantPersonDetails = hearing.getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getPersonDetails();
            final Address defendantAddress = defendantPersonDetails.getAddress();

            final String expectedAddressName = !expectPaymentTermsRemoved ? attachmentOfEarningsExpectedOrganisation.getName() :
                    "hmmmmm";

            BeanMatcher<FinancialOrderDetails> financialOrderMatcher = isBean(FinancialOrderDetails.class);
            if (expectPaymentTermsRemoved) {
                financialOrderMatcher.withValue(FinancialOrderDetails::getEmployerPayrollReference, null);
            } else {
                financialOrderMatcher
                        .withValue(FinancialOrderDetails::getEmployerPayrollReference, employeePayRolReference)
                        .withValue(FinancialOrderDetails::getIsCrownCourt, true)
                        .withValue(FinancialOrderDetails::getTotalAmountImposed, financialResult.getTotalAmountImposed())
                        .withValue(FinancialOrderDetails::getTotalBalance, financialResult.getTotalBalance())
                        .with(FinancialOrderDetails::getEmployerOrganisation, isBean(Organisation.class)
                                .withValue(Organisation::getName, attachmentOfEarningsExpectedOrganisation.getName())
                                .with(Organisation::getAddress, isBean(Address.class)
                                        .withValue(Address::getAddress1, attachmentOfEarningsExpectedOrganisation.getAddress().getAddress1())
                                        .withValue(Address::getAddress2, attachmentOfEarningsExpectedOrganisation.getAddress().getAddress2())
                                        .withValue(Address::getAddress3, attachmentOfEarningsExpectedOrganisation.getAddress().getAddress3())
                                        .withValue(Address::getAddress4, attachmentOfEarningsExpectedOrganisation.getAddress().getAddress4())
                                        .withValue(Address::getAddress5, attachmentOfEarningsExpectedOrganisation.getAddress().getAddress5())
                                )
                        );
            }

            final BeanMatcher<Address> defendantAddressMatcher = isBean(Address.class)
                    .withValue(Address::getAddress1, defendantAddress.getAddress1())
                    .withValue(Address::getAddress2, defendantAddress.getAddress2())
                    .withValue(Address::getAddress3, defendantAddress.getAddress3())
                    .withValue(Address::getAddress4, defendantAddress.getAddress4())
                    .withValue(Address::getAddress5, defendantAddress.getAddress5());

            final BeanMatcher<NowVariantAddressee> variantAddresseeMatcher = isBean(NowVariantAddressee.class);
            if (!expectPaymentTermsRemoved) {
                variantAddresseeMatcher.withValue(NowVariantAddressee::getName, attachmentOfEarningsExpectedOrganisation.getName())
                        .with(NowVariantAddressee::getAddress, isBean(Address.class)
                                .withValue(Address::getAddress1, expectedAdresseeAddress.getAddress1())
                                .withValue(Address::getAddress2, expectedAdresseeAddress.getAddress2())
                                .withValue(Address::getAddress3, expectedAdresseeAddress.getAddress3())
                                .withValue(Address::getAddress4, expectedAdresseeAddress.getAddress4())
                                .withValue(Address::getAddress5, expectedAdresseeAddress.getAddress5())
                        );
            } else {
                variantAddresseeMatcher.withValue(nva -> flattenName(nva.getName()), flattenName(defendantPersonDetails));
                variantAddresseeMatcher.with(NowVariantAddressee::getAddress, defendantAddressMatcher);
            }

            final BeanMatcher<NowVariant> variantMatcher = isBean(NowVariant.class)
                    .with(NowVariant::getMaterialId, is(not(nullValue())))
                    .with(NowVariant::getNowVariantAddressee, variantAddresseeMatcher
                    )
                    .with(NowVariant::getNowVariantDefendant, isBean(NowVariantDefendant.class)
                            .withValue(nvd -> flattenName(nvd.getName()), flattenName(defendantPersonDetails))
                            .with(NowVariantDefendant::getAddress, defendantAddressMatcher
                            )
                    )

                    .withValue(m -> m.getKey().getUsergroups().get(0), "Court Clerk");
            if (!expectPaymentTermsRemoved) {
                variantMatcher
                        .withValue(nv -> nv.getNowResults().size(), expectPaymentTermsRemoved ? 0 : resultsShared.getFirstTarget().getResultLines().size())
                        .with(NowVariant::getNowResults, hasItem(isBean(NowVariantResult.class)
                                .with(NowVariantResult::getSharedResultId, is(resultsShared.getFirstCompletedResultLine().getResultLineId()))
                                .with(nvr -> nvr.getPromptRefs().get(0), is(promptId))
                                .with(NowVariantResult::getNowVariantResultText, isBean(NowVariantResultText.class)
                                        .withValue(rt -> rt.getAdditionalProperties().size(), 2)
                                        .withValue(rt -> rt.getAdditionalProperties().get(attachmentOfEarningRequirement.getNowReference()), attachmentOfEarningRequirement.getText())
                                        .withValue(rt -> rt.getAdditionalProperties().get(attachmentOfEarningRequirement.getNowReference() + ".welsh"), attachmentOfEarningRequirement.getWelshText())
                                )
                                //check the attachment od earnings prompts have been removed
                                .withValue(nvr -> nvr.getPromptRefs().size(), 1)
                        ));
            } else {
                variantMatcher.withValue(nv -> nv.getNowResults().size(), 0);
            }

            assertThat(nows, first(isBean(Now.class)
                    .with(Now::getFinancialOrders, financialOrderMatcher)
                    .with(Now::getDefendantId, is(resultsShared.getFirstDefendant().getId()))
                    .with(Now::getNowsTypeId, is(attachmentOfEarningsNowDefinition.getId()))
//                    .with(Now::getRequestedMaterials, first(variantMatcher))
                    .withValue(Now::getLjaDetails, ljaDetails)
            ));
        }
    }

    private LjaDetails ljaDetails() {
        return LjaDetails.ljaDetails()
                .withAccountDivisionCode(STRING.next())
                .withEnforcementAddress(Address.address()
                        .withAddress1(STRING.next())
                        .withAddress2(STRING.next())
                        .withAddress3(STRING.next())
                        .withAddress4(STRING.next())
                        .withPostcode(STRING.next())
                        .build())
                .withBacsBankName(STRING.next())
                .withBacsSortCode(STRING.next())
                .withEnforcementPhoneNumber(STRING.next())
                .withLjaName(STRING.next())
                .withEnforcementEmail(STRING.next())
                .build();
    }

    private static String flattenName(final String name) {
        return name.replaceAll(" ", "").toLowerCase();
    }

    private static String flattenName(final Person person) {
        return (person.getFirstName() + person.getMiddleName() + person.getLastName()).toLowerCase();
    }

    @Test
    public void createNows_generateANowForEachDefendantMagsRemote() {
        createNows_generateANowForEachDefendant(true, JurisdictionType.MAGISTRATES);
    }

    @Test
    public void createNows_generateANowForEachDefendantCrownRemote() {
        createNows_generateANowForEachDefendant(true, JurisdictionType.CROWN);
    }

    @Test
    public void createNows_generateANowForEachDefendantMagsNonRemote() {
        createNows_generateANowForEachDefendant(false, JurisdictionType.MAGISTRATES);
    }

    public void createNows_generateANowForEachDefendant(final boolean nowRequiresRemotePrinting, final JurisdictionType jurisdictionType) {

        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(with(NowsTemplates.resultsSharedTemplate(), event -> {
            CommandHelpers.ResultsSharedEventHelper helper = h(event);

            UUID secondDefendantId = randomUUID();
            UUID secondOffenceId = randomUUID();
            helper.getFirstCase().getDefendants().add(CoreTestTemplates.defendant(helper.getFirstCase().getId(),
                    CoreTestTemplates.defaultArguments(),
                    new Pair<>(secondDefendantId, Collections.singletonList(secondOffenceId))
            ).build());

            helper.getFirstCompletedResultLine().setResultDefinitionId(resultDefinition.getId());

            helper.getHearing().getTargets().add(with(target(helper.getHearingId(), secondDefendantId, secondOffenceId, randomUUID()).build(),
                    target -> target.getResultLines().get(0).setResultDefinitionId(resultDefinition.getId())));

            helper.getHearing().setJurisdictionType(jurisdictionType);
        }));

        nowDefinition.setRemotePrintingRequired(nowRequiresRemotePrinting);

        final boolean expectedRemotePrinting = jurisdictionType != JurisdictionType.CROWN && nowRequiresRemotePrinting;

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(resultDefinition.getId()))).thenReturn(new HashSet<>(asList(nowDefinition)));

        when(referenceDataService.getResultDefinitionById(any(), any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        final List<Now> nows = target.createNows(null, resultsShared.it(), null);

        final List<UUID> defendantIds = asList(resultsShared.getFirstDefendant().getId(), resultsShared.getSecondDefendant().getId());

        final UUID firstResultLineId = resultsShared.getHearing().getTargets().stream()
                .filter(target -> target.getDefendantId().equals(nows.get(0).getDefendantId()))
                .flatMap(target -> target.getResultLines().stream())
                .filter(ResultLine::getIsComplete)
                .collect(Collectors.toList()).get(0).getResultLineId();

        assertThat(nows, first(isBean(Now.class)
                .with(Now::getDefendantId, isIn(defendantIds))
                .with(Now::getRequestedMaterials, first(isBean(NowVariant.class)
                        .withValue(NowVariant::getIsRemotePrintingRequired, expectedRemotePrinting)
                        .with(NowVariant::getNowResults, first(isBean(NowVariantResult.class)
                                        .with(NowVariantResult::getSharedResultId, is(firstResultLineId))
                                )
                        )
                ))
        ));

        final UUID secondResultLineId = resultsShared.getHearing().getTargets().stream()
                .filter(target -> target.getDefendantId().equals(nows.get(1).getDefendantId()))
                .flatMap(target -> target.getResultLines().stream())
                .filter(ResultLine::getIsComplete)
                .collect(Collectors.toList()).get(0).getResultLineId();

        assertThat(nows, second(isBean(Now.class)
                .with(Now::getDefendantId, isIn(defendantIds))
                .with(Now::getRequestedMaterials, first(isBean(NowVariant.class)
                        .with(NowVariant::getNowResults, first(isBean(NowVariantResult.class)
                                        .with(NowVariantResult::getSharedResultId, is(secondResultLineId))
                                )
                        )
                ))
        ));
    }

    @Test
    public void createNows_withAResultLineThatIsNotRelatedToANow() {

        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(NowsTemplates.resultsSharedTemplate());

        List<Now> nows = target.createNows(null, resultsShared.it(), null);

        assertThat(nows.size(), is(0));
    }

    @Test
    public void createNows_whenIncompleteLineIsPresent_noNowGenerated() {

        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(with(NowsTemplates.resultsSharedTemplate(), event -> {
            CommandHelpers.ResultsSharedEventHelper helper = h(event);


            helper.getFirstCompletedResultLine().setResultDefinitionId(resultDefinition.getId());

            helper.getHearing().getTargets().add(with(target(helper.getHearingId(), helper.getFirstDefendant().getId(), helper.getFirstDefendantFirstOffence().getId(), randomUUID()).build(), target -> {
                target.getResultLines().get(0).setResultDefinitionId(resultDefinition.getId());
                target.getResultLines().get(0).setIsComplete(false);
            }));
        }));

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(resultDefinition.getId()))).thenReturn(new HashSet<>(asList(nowDefinition)));
        when(referenceDataService.getResultDefinitionById(any(), any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Now> nows = target.createNows(null, resultsShared.it(), null);

        assertThat(nows.size(), is(0));
    }

    @Test
    public void createNows_whenIncompleteLineIsPresentForADifferentDefendant_NowsAreGenerated() {

        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(with(NowsTemplates.resultsSharedTemplate(), event -> {
            CommandHelpers.ResultsSharedEventHelper helper = h(event);

            UUID secondDefendantId = randomUUID();
            UUID secondOffenceId = randomUUID();
            helper.getFirstCase().getDefendants().add(CoreTestTemplates.defendant(helper.getFirstCase().getId(),
                    CoreTestTemplates.defaultArguments(),
                    new Pair<>(secondDefendantId, Collections.singletonList(secondOffenceId))
            ).build());

            helper.getFirstCompletedResultLine().setResultDefinitionId(resultDefinition.getId());

            helper.getHearing().getTargets().add(with(target(helper.getHearingId(), secondDefendantId, secondOffenceId, randomUUID()).build(), target -> {
                target.getResultLines().get(0).setResultDefinitionId(resultDefinition.getId());
                target.getResultLines().get(0).setIsComplete(false);
            }));
        }));

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(resultDefinition.getId()))).thenReturn(new HashSet<>(asList(nowDefinition)));

        when(referenceDataService.getResultDefinitionById(any(), any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Now> nows = target.createNows(null, resultsShared.it(), null);

        assertThat(nows.size(), is(1));
    }

    @Test
    public void createNows_whenNonMandatoryLineIsNotPresent_NowsAreGenerated() {

        final NowDefinition nowDefinition = NowDefinition.now()
                .setId(UUID.randomUUID())
                .setResultDefinitions(asList(
                        NowResultDefinitionRequirement.resultDefinitions()
                                .setId(resultDefinition.getId())
                                .setMandatory(true)
                                .setPrimary(true),
                        NowResultDefinitionRequirement.resultDefinitions()
                                .setId(randomUUID())
                                .setMandatory(false)
                                .setPrimary(false)
                ));


        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(with(NowsTemplates.resultsSharedTemplate(),
                event -> h(event).getFirstCompletedResultLine().setResultDefinitionId(resultDefinition.getId())));

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(resultDefinition.getId()))).thenReturn(new HashSet<>(asList(nowDefinition)));

        when(referenceDataService.getResultDefinitionById(any(), any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Now> nows = target.createNows(null, resultsShared.it(), null);

        assertThat(nows.size(), is(1));
    }

    @Test
    public void createNows_whenMandatoryLineIsNotPresent_NowsAreNotGenerated() {

        final NowDefinition nowDefinition = NowDefinition.now()
                .setId(UUID.randomUUID())
                .setResultDefinitions(asList(
                        NowResultDefinitionRequirement.resultDefinitions()
                                .setId(resultDefinition.getId())
                                .setMandatory(true),
                        NowResultDefinitionRequirement.resultDefinitions()
                                .setId(randomUUID())
                                .setMandatory(true)
                ));

        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(with(NowsTemplates.resultsSharedTemplate(),
                event -> h(event).getFirstCompletedResultLine().setResultDefinitionId(resultDefinition.getId())));

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(resultDefinition.getId()))).thenReturn(new HashSet<>(asList(nowDefinition)));

        when(referenceDataService.getResultDefinitionById(any(), any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Now> nows = target.createNows(null, resultsShared.it(), null);

        assertThat(nows.size(), is(0));
    }

    @Test
    public void createNows_generateMultipleVariants_forDifferentPrompts() {
        final UUID lockUpPromptId = UUID.randomUUID();
        final UUID setFreePromptId = UUID.randomUUID();
        final ResultDefinition resultDefinition = ResultDefinition.resultDefinition()
                .setId(randomUUID())
                .setPrompts(asList(Prompt.prompt()
                                .setId(lockUpPromptId)
                                .setLabel("Lock him up")
                                .setUserGroups(singletonList("Court Clerk")),
                        Prompt.prompt()
                                .setId(setFreePromptId)
                                .setLabel("Set him free")
                                .setUserGroups(singletonList("Listing Officer"))
                ));

        final NowDefinition nowDefinition = NowDefinition.now()
                .setId(UUID.randomUUID())
                .setResultDefinitions(singletonList(
                        NowResultDefinitionRequirement.resultDefinitions()
                                .setId(resultDefinition.getId())
                                .setMandatory(true)
                ));

        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(with(NowsTemplates.resultsSharedTemplate(),
                event -> h(event).getFirstCompletedResultLine().setResultDefinitionId(resultDefinition.getId())));

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(resultDefinition.getId()))).thenReturn(new HashSet<>(asList(nowDefinition)));

        when(referenceDataService.getResultDefinitionById(any(), any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        final List<Now> nows = target.createNows(null, resultsShared.it(), null);

        assertThat(nows.size(), is(1));

        final Now now = nows.get(0);

        assertThat(now.getRequestedMaterials(), hasSize(2));

        assertThat(new ArrayList<>(now.getRequestedMaterials().get(0).getKey().getUsergroups()), containsInAnyOrder("Court Clerk"));
        assertThat(now.getRequestedMaterials().get(0).getNowResults().get(0).getPromptRefs(), hasSize(1));
        assertThat(now.getRequestedMaterials().get(0).getNowResults().get(0).getPromptRefs().get(0), is(lockUpPromptId));

        assertThat(new ArrayList<>(now.getRequestedMaterials().get(1).getKey().getUsergroups()), containsInAnyOrder("Listing Officer"));
        assertThat(now.getRequestedMaterials().get(1).getNowResults().get(0).getPromptRefs(), hasSize(1));
        assertThat(now.getRequestedMaterials().get(1).getNowResults().get(0).getPromptRefs().get(0), is(setFreePromptId));
    }

    @Test
    public void createNows_generateSingleVariant_forMultipleUserGroups() {
        final UUID lockUpPromptId = UUID.randomUUID();
        final ResultDefinition resultDefinition = ResultDefinition.resultDefinition()
                .setId(lockUpPromptId)
                .setPrompts(singletonList(Prompt.prompt()
                        .setId(lockUpPromptId)
                        .setLabel("Lock him up")
                        .setUserGroups(asList("Court Clerk", "Listing Officer"))
                ));

        final NowDefinition nowDefinition = NowDefinition.now()
                .setId(UUID.randomUUID())
                .setResultDefinitions(singletonList(
                        NowResultDefinitionRequirement.resultDefinitions()
                                .setId(resultDefinition.getId())
                                .setMandatory(true)
                ));

        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(with(NowsTemplates.resultsSharedTemplate(),
                event -> h(event).getFirstCompletedResultLine().setResultDefinitionId(resultDefinition.getId())));

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(resultDefinition.getId()))).thenReturn(new HashSet<>(asList(nowDefinition)));

        when(referenceDataService.getResultDefinitionById(any(), any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Now> nows = target.createNows(null, resultsShared.it(), null);

        assertThat(nows.size(), is(1));

        final Now now = nows.get(0);

        assertThat(now.getRequestedMaterials(), hasSize(1));

        assertThat(new ArrayList<>(now.getRequestedMaterials().get(0).getKey().getUsergroups()), containsInAnyOrder("Court Clerk", "Listing Officer"));
        assertThat(now.getRequestedMaterials().get(0).getNowResults().get(0).getPromptRefs(), hasSize(1));
        assertThat(now.getRequestedMaterials().get(0).getNowResults().get(0).getPromptRefs().get(0), is(lockUpPromptId));
    }


    @Test
    public void createNows_generateVariant_ResultDefinitionWithoutPrompts() {
        final String prisonAdminUserGroup = "Prison Admin";
        final ResultDefinition resultDefinition = ResultDefinition.resultDefinition()
                .setId(randomUUID())
                .setUserGroups(singletonList(prisonAdminUserGroup));

        final NowDefinition nowDefinition = NowDefinition.now()
                .setId(UUID.randomUUID())
                .setResultDefinitions(singletonList(
                        NowResultDefinitionRequirement.resultDefinitions()
                                .setId(resultDefinition.getId())
                                .setMandatory(true)
                ));

        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(with(NowsTemplates.resultsSharedTemplate(),
                event -> h(event).getFirstCompletedResultLine().setResultDefinitionId(resultDefinition.getId())));

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(resultDefinition.getId()))).thenReturn(new HashSet<>(asList(nowDefinition)));

        when(referenceDataService.getResultDefinitionById(any(), any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Now> nows = target.createNows(null, resultsShared.it(), null);

        assertThat(nows.size(), is(1));

        final Now now = nows.get(0);

        assertThat(now.getRequestedMaterials(), hasSize(1));

        assertThat(new ArrayList<>(now.getRequestedMaterials().get(0).getKey().getUsergroups()), containsInAnyOrder(prisonAdminUserGroup));
        assertThat(now.getRequestedMaterials().get(0).getNowResults().get(0).getPromptRefs(), is((List) null));
    }


    @Test
    public void createNows_generateMultipleVariants_forDifferentPromptsAndAdditionalUserGroups() {
        final UUID lockUpPromptId = randomUUID();
        final UUID setFreePromptId = randomUUID();

        final ResultDefinition resultDefinition = ResultDefinition.resultDefinition()
                .setId(randomUUID())
                .setUserGroups(asList("Prison Admin", "LLA User"))
                .setPrompts(asList(Prompt.prompt()
                                .setId(lockUpPromptId)
                                .setLabel("Lock him up")
                                .setUserGroups(asList("Court Clerk", "Judge")),
                        Prompt.prompt()
                                .setId(setFreePromptId)
                                .setLabel("Set him free")
                                .setUserGroups(asList("Listing Officer", "Judge"))
                ));

        final NowDefinition nowDefinition = NowDefinition.now()
                .setId(UUID.randomUUID())
                .setResultDefinitions(singletonList(
                        NowResultDefinitionRequirement.resultDefinitions()
                                .setId(resultDefinition.getId())
                                .setMandatory(true)
                ));


        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(with(NowsTemplates.resultsSharedTemplate(),
                event -> h(event).getFirstCompletedResultLine().setResultDefinitionId(resultDefinition.getId())));

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(resultDefinition.getId()))).thenReturn(new HashSet<>(asList(nowDefinition)));

        when(referenceDataService.getResultDefinitionById(any(), any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Now> nows = target.createNows(null, resultsShared.it(), null);

        assertThat(nows.size(), is(1));

        final Now now = nows.get(0);

        assertThat(now.getRequestedMaterials(), hasSize(4));

        assertThat(new ArrayList<>(now.getRequestedMaterials().get(0).getKey().getUsergroups()), containsInAnyOrder("Court Clerk"));
        assertThat(now.getRequestedMaterials().get(0).getNowResults().get(0).getPromptRefs(), hasSize(1));
        assertThat(now.getRequestedMaterials().get(0).getNowResults().get(0).getPromptRefs().get(0), is(lockUpPromptId));

        assertThat(new ArrayList<>(now.getRequestedMaterials().get(1).getKey().getUsergroups()), containsInAnyOrder("Judge"));
        assertThat(now.getRequestedMaterials().get(1).getNowResults().get(0).getPromptRefs(), hasSize(2));
        assertThat(now.getRequestedMaterials().get(1).getNowResults().get(0).getPromptRefs().get(1), is(setFreePromptId));
        assertThat(now.getRequestedMaterials().get(1).getNowResults().get(0).getPromptRefs().get(0), is(lockUpPromptId));

        assertThat(new ArrayList<>(now.getRequestedMaterials().get(2).getKey().getUsergroups()), containsInAnyOrder("LLA User", "Prison Admin"));
        assertThat(now.getRequestedMaterials().get(2).getNowResults().get(0).getPromptRefs(), is((List) null));

        assertThat(new ArrayList<>(now.getRequestedMaterials().get(3).getKey().getUsergroups()), containsInAnyOrder("Listing Officer"));
        assertThat(now.getRequestedMaterials().get(3).getNowResults().get(0).getPromptRefs(), hasSize(1));
        assertThat(now.getRequestedMaterials().get(3).getNowResults().get(0).getPromptRefs().get(0), is(setFreePromptId));
    }


    @Test
    public void createNows_givenPreviouslyGenerated_noNowIsGenerated() {

        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(with(NowsTemplates.resultsSharedTemplate(), event -> {
            CommandHelpers.ResultsSharedEventHelper helper = h(event);

            CompletedResultLineStatus completedResultLineStatus = completedResultLineStatus(helper.getFirstCompletedResultLine().getResultLineId());

            helper.it().getCompletedResultLinesStatus().put(helper.getFirstCompletedResultLine().getResultLineId(), completedResultLineStatus);

            helper.getFirstCompletedResultLine().setResultDefinitionId(resultDefinition.getId());

            helper.it().setVariantDirectory(singletonList(
                    Variant.variant()
                            .setKey(VariantKey.variantKey()
                                    .setDefendantId(helper.getFirstDefendant().getId())
                                    .setUsergroups(singletonList("Court Clerk"))
                                    .setNowsTypeId(nowDefinition.getId())
                            )
                            .setValue(VariantValue.variantValue()
                                    .setResultLines(singletonList(ResultLineReference.resultLineReference()
                                            .setResultLineId(helper.getFirstCompletedResultLine().getResultLineId())
                                            .setLastSharedTime(completedResultLineStatus.getLastSharedDateTime())
                                    ))
                            )
            ));
        }));

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(resultDefinition.getId()))).thenReturn(new HashSet<>(asList(nowDefinition)));

        when(referenceDataService.getResultDefinitionById(any(), any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        final List<Now> nows = target.createNows(null, resultsShared.it(), null);

        assertThat(nows, empty());
    }

    @Test
    public void createNows_givenPreviouslyGeneratedButWeNowHaveANewLine_aNowIsGenerated() {

        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(with(NowsTemplates.resultsSharedTemplate(), event -> {
            CommandHelpers.ResultsSharedEventHelper helper = h(event);

            CompletedResultLineStatus completedResultLineStatus = completedResultLineStatus(helper.getFirstCompletedResultLine().getResultLineId());

            helper.it().getCompletedResultLinesStatus().put(helper.getFirstCompletedResultLine().getResultLineId(), completedResultLineStatus);

            helper.getFirstCompletedResultLine().setResultDefinitionId(resultDefinition.getId());

            helper.it().setVariantDirectory(singletonList(
                    Variant.variant()
                            .setKey(VariantKey.variantKey()
                                    .setDefendantId(helper.getFirstDefendant().getId())
                                    .setUsergroups(singletonList("Court Clerk"))
                                    .setNowsTypeId(nowDefinition.getId())
                            )
                            .setValue(VariantValue.variantValue()
                                    .setResultLines(singletonList(ResultLineReference.resultLineReference()
                                            .setResultLineId(helper.getFirstCompletedResultLine().getResultLineId())
                                            .setLastSharedTime(FUTURE_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC")))
                                    ))
                            )
            ));
        }));

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(resultDefinition.getId()))).thenReturn(new HashSet<>(asList(nowDefinition)));

        when(referenceDataService.getResultDefinitionById(any(), any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Now> nows = target.createNows(null, resultsShared.it(), null);

        assertThat(nows, hasSize(1));

        assertThat(nows.get(0).getRequestedMaterials(), hasSize(1));

        assertThat(nows.get(0).getRequestedMaterials().get(0).getIsAmended(), is(true));
    }
}
