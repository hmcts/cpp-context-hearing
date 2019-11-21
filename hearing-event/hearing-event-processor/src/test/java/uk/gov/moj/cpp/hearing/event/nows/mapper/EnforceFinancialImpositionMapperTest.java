package uk.gov.moj.cpp.hearing.event.nows.mapper;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.generateNowsRequestTemplate;

import uk.gov.justice.core.courts.CreateNowsRequest;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Target;
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
    public void testEnforceFinancialImposition() {

        EnforceFinancialImposition enforceFinancialImposition = mapper.map();

        assertNotNull(enforceFinancialImposition);

        final Defendant defendant = nowsRequest.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .filter(d -> d.getId().equals(defendantId))
                .findFirst().orElse(null);

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
        assertNull(enforceFinancialImposition.getDefendant().getVehicleMake());
        assertNull(enforceFinancialImposition.getDefendant().getVehicleRegistrationMark());
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


    }

    private static String payload(final String fileName) {
        return fileAsStringReader.readFile(fileName);
    }
}