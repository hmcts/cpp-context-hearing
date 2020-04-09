package uk.gov.moj.cpp.hearing.event.nows.mapper;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_DEFAULT_DAYS_IN_JAIL_PROMPT_REFERENCE;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_PARENT_GUARDIAN_TOPAY_PROMPT_REFERENCE;
import static uk.gov.moj.cpp.hearing.event.nows.PromptTypesConstant.P_PAYMENT_CARD_REQUIRED_PROMPT_REFERENCE;
import static uk.gov.moj.cpp.hearing.event.nows.ResultDefinitionsConstant.RD_LUMSI;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.generateNowsRequestTemplate;

import uk.gov.justice.core.courts.CreateNowsRequest;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.DefendantAlias;
import uk.gov.justice.core.courts.NowVariantResult;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.ResultPrompt;
import uk.gov.justice.core.courts.SharedResultLine;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.json.schemas.staging.Aliases;
import uk.gov.justice.json.schemas.staging.EnforceFinancialImposition;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.nows.events.PendingNowsRequested;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnforceFinancialImpositionMapperTest {

    private EnforceFinancialImpositionMapper mapper;

    private CreateNowsRequest nowsRequest;

    private UUID defendantId = randomUUID();

    private UUID requestId = UUID.randomUUID();

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @InjectMocks
    private StringToJsonObjectConverter stringToJsonObjectConverter;

    private static final FileAsStringReader fileAsStringReader = new FileAsStringReader();

    private List<Target> targets = asList();

    @Before
    public void setup() {

        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());

        nowsRequest = generateNowsRequestTemplate(defendantId);

        mapper = new EnforceFinancialImpositionMapper(requestId, nowsRequest, targets);
    }

    @Test
    public void testInstalmentAmountAndLumpSumAmount() {

        String payload = payload("/data/hearing.events.pending-nows-requested.json");

        final PendingNowsRequested pendingNowsRequested = this.jsonObjectToObjectConverter.convert(stringToJsonObjectConverter.convert(payload), PendingNowsRequested.class);

        final EnforceFinancialImposition enforceFinancialImposition = new EnforceFinancialImpositionMapper(requestId, pendingNowsRequested.getCreateNowsRequest(), pendingNowsRequested.getTargets()).map();

        assertEquals(new BigDecimal("19.90"), enforceFinancialImposition.getPaymentTerms().getInstalmentAmount());

        assertEquals(new BigDecimal("200"), enforceFinancialImposition.getPaymentTerms().getLumpSumAmount());
    }

    @Test
    public void testPaymentCardRequiredOld() {

        String payload = payload("/data/hearing.events.pending-nows-requested.json");

        final PendingNowsRequested pendingNowsRequested = this.jsonObjectToObjectConverter.convert(stringToJsonObjectConverter.convert(payload), PendingNowsRequested.class);

        ResultLine lumpSumResultLine = pendingNowsRequested.getTargets().stream()
                .flatMap(t->t.getResultLines().stream())
                .filter(rl->rl.getResultDefinitionId().equals(RD_LUMSI)).findFirst().orElseThrow(
                        () ->new RuntimeException("cant find lump sum result line")
                );

        UUID paymentCardRequiredPromptId = UUID.randomUUID();

        lumpSumResultLine.getPrompts().add(
                Prompt.prompt()
                        .withId(paymentCardRequiredPromptId)
                        .withLabel("Payment Card Required")
                        .withValue("Y")
                        .build()
        );

        List<NowVariantResult> nowVariantResults = pendingNowsRequested.getCreateNowsRequest().getNows().get(0).getRequestedMaterials().get(0).getNowResults();
        NowVariantResult nowVariantResult = nowVariantResults.stream().filter(nvr->nvr.getSharedResultId()
                .equals(lumpSumResultLine.getResultLineId())).findFirst()
                .orElseThrow(() -> new RuntimeException("cant find result for " + lumpSumResultLine.getResultLineId()));

        nowVariantResult.getPromptRefs().add(paymentCardRequiredPromptId);


        SharedResultLine sharedResultLine = pendingNowsRequested.getCreateNowsRequest().getSharedResultLines().stream()
                .filter((srl->srl.getId().equals(lumpSumResultLine.getResultLineId()))).findFirst().orElseThrow(
                        () -> new RuntimeException("cant find sharedResultLine:" + lumpSumResultLine.getResultLineId())
                );
        sharedResultLine.getPrompts().add(
                ResultPrompt.resultPrompt()
                        .withValue("Y")
                        .withPromptReference(P_PAYMENT_CARD_REQUIRED_PROMPT_REFERENCE)
                        .withId(paymentCardRequiredPromptId)
                        .build()
        );



        final EnforceFinancialImposition enforceFinancialImposition = new EnforceFinancialImpositionMapper(requestId, pendingNowsRequested.getCreateNowsRequest(), pendingNowsRequested.getTargets()).map();

        assertEquals("Y", enforceFinancialImposition.getPaymentTerms().getPaymentCardRequired());

    }

    private void setPromptValue(PendingNowsRequested pendingNowsRequested,
        UUID resultDefinitionId, String promptReference, String promptLabel,
        String promptValue) {

        ResultLine lumpSumResultLine = pendingNowsRequested.getTargets().stream()
                .flatMap(t->t.getResultLines().stream())
                .filter(rl->rl.getResultDefinitionId().equals(resultDefinitionId)).findFirst().orElseThrow(
                        () ->new RuntimeException("cant find lump sum result line")
                );

        UUID paymentCardRequiredPromptId = UUID.randomUUID();

        lumpSumResultLine.getPrompts().add(
                Prompt.prompt()
                        .withId(paymentCardRequiredPromptId)
                        .withLabel(promptLabel)
                        .withValue(promptValue)
                        .build()
        );

        List<NowVariantResult> nowVariantResults = pendingNowsRequested.getCreateNowsRequest().getNows().get(0).getRequestedMaterials().get(0).getNowResults();
        NowVariantResult nowVariantResult = nowVariantResults.stream().filter(nvr->nvr.getSharedResultId()
                .equals(lumpSumResultLine.getResultLineId())).findFirst()
                .orElseThrow(() -> new RuntimeException("cant find result for " + lumpSumResultLine.getResultLineId()));

        nowVariantResult.getPromptRefs().add(paymentCardRequiredPromptId);


        SharedResultLine sharedResultLine = pendingNowsRequested.getCreateNowsRequest().getSharedResultLines().stream()
                .filter((srl->srl.getId().equals(lumpSumResultLine.getResultLineId()))).findFirst().orElseThrow(
                        () -> new RuntimeException("cant find sharedResultLine:" + lumpSumResultLine.getResultLineId())
                );
        sharedResultLine.getPrompts().add(
                ResultPrompt.resultPrompt()
                        .withValue(promptValue)
                        .withLabel(promptLabel)
                        .withPromptReference(promptReference)
                        .withId(paymentCardRequiredPromptId)
                        .build()
        );

    }

    @Test
    public void testCeResultingExtraFields() {

        String payload = payload("/data/hearing.events.pending-nows-requested.json");

        PendingNowsRequested pendingNowsRequested = this.jsonObjectToObjectConverter.convert(stringToJsonObjectConverter.convert(payload), PendingNowsRequested.class);

        UUID resultDefinitionID = RD_LUMSI;

        Integer defaultDaysInJail = new Integer(123);

        setPromptValue(pendingNowsRequested, resultDefinitionID, P_PARENT_GUARDIAN_TOPAY_PROMPT_REFERENCE, "Parent Guadian to pay",  "N");
        setPromptValue(pendingNowsRequested, resultDefinitionID, P_DEFAULT_DAYS_IN_JAIL_PROMPT_REFERENCE, "Default days in jail",   defaultDaysInJail.toString());

        final EnforceFinancialImposition enforceFinancialImposition = new EnforceFinancialImpositionMapper(requestId, pendingNowsRequested.getCreateNowsRequest(), pendingNowsRequested.getTargets()).map();

        assertEquals("Y", enforceFinancialImposition.getPaymentTerms().getPaymentCardRequired());
        assertEquals(Boolean.FALSE, enforceFinancialImposition.getPaymentTerms().getParentGuardianToPay());
        assertEquals(new Integer(123), enforceFinancialImposition.getPaymentTerms().getDefaultDaysInJail());

    }

    @Test
    public void testCeResultingDwAwpTest() {

        String payload = payload("/data/hearing.events.pending-nows-requested-dwp-ap.json");

        PendingNowsRequested pendingNowsRequested = this.jsonObjectToObjectConverter.convert(stringToJsonObjectConverter.convert(payload), PendingNowsRequested.class);

        final EnforceFinancialImposition enforceFinancialImposition = new EnforceFinancialImpositionMapper(requestId, pendingNowsRequested.getCreateNowsRequest(), pendingNowsRequested.getTargets()).map();

        assertEquals("APITEST_DWP123456789. Disability allowance MC100 on case", enforceFinancialImposition.getDwpApNumber());

    }

    @Test
    public void testEnforceFinancialImposition() {

        final Defendant defendant = nowsRequest.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .filter(d -> d.getId().equals(defendantId))
                .findFirst().orElse(null);
        DefendantAlias aliasIn = DefendantAlias.defendantAlias()
                .withFirstName("trevor")
                .withMiddleName("tony")
                .withLastName("tickle")
                .build();
        defendant.setAliases(asList(aliasIn));

        EnforceFinancialImposition enforceFinancialImposition = mapper.map();

        assertNotNull(enforceFinancialImposition);



        assertEquals(requestId, enforceFinancialImposition.getRequestId());
        assertEquals("Courts", enforceFinancialImposition.getOriginator());
        assertEquals(nowsRequest.getHearing().getCourtCentre().getId(), enforceFinancialImposition.getImposingCourt());

        assertEquals(nowsRequest.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN(), enforceFinancialImposition.getProsecutionCaseReference());

        assertEquals(defendant.getPersonDefendant().getPersonDetails().getAddress().getAddress1(), enforceFinancialImposition.getDefendant().getAddress1());
        assertEquals(defendant.getPersonDefendant().getPersonDetails().getAddress().getAddress2(), enforceFinancialImposition.getDefendant().getAddress2());
        assertEquals(defendant.getPersonDefendant().getPersonDetails().getAddress().getAddress3(), enforceFinancialImposition.getDefendant().getAddress3());
        assertEquals(defendant.getPersonDefendant().getPersonDetails().getAddress().getAddress4(), enforceFinancialImposition.getDefendant().getAddress4());
        assertEquals(defendant.getPersonDefendant().getPersonDetails().getAddress().getAddress5(), enforceFinancialImposition.getDefendant().getAddress5());
        assertEquals(defendant.getPersonDefendant().getPersonDetails().getAddress().getPostcode(), enforceFinancialImposition.getDefendant().getPostcode());
        assertEquals(defendant.getPersonDefendant().getPersonDetails().getDateOfBirth(), enforceFinancialImposition.getDefendant().getDateOfBirth());
        assertNull(enforceFinancialImposition.getDefendant().getBenefitsTypes());
        assertNull(enforceFinancialImposition.getDefendant().getCompanyName());
        assertNull(enforceFinancialImposition.getDefendant().getDateOfSentence());
        assertEquals(nowsRequest.getNows().get(0).getDocumentationLanguage().toString(), enforceFinancialImposition.getDefendant().getDocumentLanguage().toString());
        assertEquals(defendant.getPersonDefendant().getPersonDetails().getContact().getPrimaryEmail(), enforceFinancialImposition.getDefendant().getEmailAddress1());
        assertEquals(defendant.getPersonDefendant().getPersonDetails().getContact().getSecondaryEmail(), enforceFinancialImposition.getDefendant().getEmailAddress2());
        assertEquals(defendant.getPersonDefendant().getPersonDetails().getFirstName() + " " + defendant.getPersonDefendant().getPersonDetails().getMiddleName(), enforceFinancialImposition.getDefendant().getForenames());
        assertEquals(nowsRequest.getHearing().getHearingLanguage().toString(), enforceFinancialImposition.getDefendant().getHearingLanguage().toString());
        assertEquals(defendant.getPersonDefendant().getPersonDetails().getNationalInsuranceNumber(), enforceFinancialImposition.getDefendant().getNationalInsuranceNumber());
        assertNull(enforceFinancialImposition.getDefendant().getStatementOfMeansProvided());
        assertEquals(defendant.getPersonDefendant().getPersonDetails().getLastName(), enforceFinancialImposition.getDefendant().getSurname());
        assertEquals(defendant.getPersonDefendant().getPersonDetails().getContact().getWork(), enforceFinancialImposition.getDefendant().getTelephoneNumberBusiness());
        assertEquals(defendant.getPersonDefendant().getPersonDetails().getContact().getMobile(), enforceFinancialImposition.getDefendant().getTelephoneNumberMobile());
        assertEquals(defendant.getPersonDefendant().getPersonDetails().getContact().getHome(), enforceFinancialImposition.getDefendant().getTelephoneNumberHome());
        assertEquals(defendant.getPersonDefendant().getPersonDetails().getTitle().toString().toLowerCase(), enforceFinancialImposition.getDefendant().getTitle().toString().toLowerCase());
        assertEquals(defendant.getOffences().get(0).getOffenceFacts().getVehicleRegistration(),enforceFinancialImposition.getDefendant().getVehicleRegistrationMark());
        assertEquals(defendant.getOffences().get(0).getOffenceFacts().getVehicleMake(),enforceFinancialImposition.getDefendant().getVehicleMake());

        assertNull(enforceFinancialImposition.getDefendant().getWeeklyIncome());

        assertNull(enforceFinancialImposition.getParentGuardian());

        assertEquals(false, enforceFinancialImposition.getPlea().getIncludesGuilty());
        assertEquals(false, enforceFinancialImposition.getPlea().getIncludesOnline());

        assertEquals(nowsRequest.getNows().get(0).getFinancialOrders().getEmployerOrganisation().getName(), enforceFinancialImposition.getEmployer().getEmployerCompanyName());
        assertEquals(nowsRequest.getNows().get(0).getFinancialOrders().getEmployerOrganisation().getAddress().getAddress1(), enforceFinancialImposition.getEmployer().getEmployerAddress1());
        assertEquals(nowsRequest.getNows().get(0).getFinancialOrders().getEmployerOrganisation().getAddress().getAddress2(), enforceFinancialImposition.getEmployer().getEmployerAddress2());
        assertEquals(nowsRequest.getNows().get(0).getFinancialOrders().getEmployerOrganisation().getAddress().getAddress3(), enforceFinancialImposition.getEmployer().getEmployerAddress3());
        assertEquals(nowsRequest.getNows().get(0).getFinancialOrders().getEmployerOrganisation().getAddress().getAddress4(), enforceFinancialImposition.getEmployer().getEmployerAddress4());
        assertEquals(nowsRequest.getNows().get(0).getFinancialOrders().getEmployerOrganisation().getAddress().getAddress5(), enforceFinancialImposition.getEmployer().getEmployerAddress5());
        assertEquals(nowsRequest.getNows().get(0).getFinancialOrders().getEmployerOrganisation().getAddress().getPostcode(), enforceFinancialImposition.getEmployer().getEmployerPostcode());
        assertNull(enforceFinancialImposition.getMinorCreditor());
        assertEquals(nowsRequest.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityCode(), enforceFinancialImposition.getProsecutionAuthorityCode());

        Aliases aliasOut = enforceFinancialImposition.getDefendant().getAliases().get(0);
        assertEquals(aliasIn.getFirstName(), aliasOut.getFornames());
        assertEquals(aliasIn.getLastName(), aliasOut.getSurname());
        assertEquals(aliasIn.getMiddleName().substring(0,1).toUpperCase(), aliasOut.getInitials().substring(0, 1).toUpperCase());


        assertNull(enforceFinancialImposition.getDwpApNumber());


    }

    private static String payload(final String fileName) {
        return fileAsStringReader.readFile(fileName);
    }
}