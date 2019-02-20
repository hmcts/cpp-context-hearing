package uk.gov.moj.cpp.hearing.event.nows.mapper;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.generateNowsRequestTemplate;

import uk.gov.justice.core.courts.CreateNowsRequest;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.json.schemas.staging.EnforceFinancialImposition;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class EnforceFinancialImpositionMapperTest {

    private EnforceFinancialImpositionMapper mapper;

    private CreateNowsRequest nowsRequest;

    private UUID defendantId = randomUUID();

    private UUID requestId = UUID.randomUUID();

    @Before
    public void setup() {

        nowsRequest = generateNowsRequestTemplate(defendantId);

        mapper = new EnforceFinancialImpositionMapper(requestId, nowsRequest);
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

        assertEquals(defendant.getPersonDefendant().getEmployerPayrollReference(), enforceFinancialImposition.getEmployer().getEmployerReference());
        assertEquals(defendant.getPersonDefendant().getEmployerOrganisation().getName(), enforceFinancialImposition.getEmployer().getEmployerCompanyName());
        assertEquals(defendant.getPersonDefendant().getEmployerOrganisation().getAddress().getAddress1(), enforceFinancialImposition.getEmployer().getEmployerAddress1());
        assertEquals(defendant.getPersonDefendant().getEmployerOrganisation().getAddress().getAddress2(), enforceFinancialImposition.getEmployer().getEmployerAddress2());
        assertEquals(defendant.getPersonDefendant().getEmployerOrganisation().getAddress().getAddress3(), enforceFinancialImposition.getEmployer().getEmployerAddress3());
        assertEquals(defendant.getPersonDefendant().getEmployerOrganisation().getAddress().getAddress4(), enforceFinancialImposition.getEmployer().getEmployerAddress4());
        assertEquals(defendant.getPersonDefendant().getEmployerOrganisation().getAddress().getAddress5(), enforceFinancialImposition.getEmployer().getEmployerAddress5());
        assertEquals(defendant.getPersonDefendant().getEmployerOrganisation().getAddress().getPostcode(), enforceFinancialImposition.getEmployer().getEmployerPostcode());
        assertEquals(defendant.getPersonDefendant().getEmployerOrganisation().getContact().getWork(), enforceFinancialImposition.getEmployer().getEmployerTelephoneNumber());
        assertEquals(defendant.getPersonDefendant().getEmployerOrganisation().getContact().getPrimaryEmail(), enforceFinancialImposition.getEmployer().getEmployerEmailAddress());

        assertNull(enforceFinancialImposition.getMinorCreditor());

        assertEquals(nowsRequest.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityCode(), enforceFinancialImposition.getProsecutionAuthorityCode());


    }
}