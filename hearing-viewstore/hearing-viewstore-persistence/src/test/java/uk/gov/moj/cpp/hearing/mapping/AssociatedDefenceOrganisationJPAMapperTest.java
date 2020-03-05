package uk.gov.moj.cpp.hearing.mapping;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.core.courts.AssociatedDefenceOrganisation.associatedDefenceOrganisation;
import static uk.gov.justice.core.courts.DefenceOrganisation.defenceOrganisation;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.FundingType;
import uk.gov.justice.core.courts.Organisation;
import uk.gov.moj.cpp.hearing.persist.entity.ha.AssociatedDefenceOrganisation;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Contact;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DefenceOrganisation;

import java.time.LocalDate;

import org.junit.Test;

public class AssociatedDefenceOrganisationJPAMapperTest {
    private static final String LAA_CONTRACT_NUMBER = "ABC LTD";
    private static final String ORGANISATION_NAME = "Org name";
    private static final LocalDate associationEndDate = LocalDate.of(2020, 12, 2);
    private static final LocalDate associationStartDate = LocalDate.of(2019, 12, 2);
    private static final String INCORPORATION_NUMBER = "123123";
    private static final String REGISTERED_CHARITY_NUMBER = "charity001";
    private static final String ADDRESS_1 = "Address 1";
    private static final String ADDRESS_2 = "Address 2";
    private static final String ADDRESS_3 = "Address 3";
    private static final String ADDRESS_4 = "Address 4";
    private static final String ADDRESS_5 = "Address 5";
    private static final String POST_CODE = "IG11NL";
    private static final String APPLICATION_REFERENCE = "application-reference";

    private final AssociatedDefenceOrganisationJPAMapper mapper = JPACompositeMappers.ASSOCIATED_DEFENCE_ORGANISATION_JPA_MAPPER;


    @Test
    public void toJPA() {
        final uk.gov.justice.core.courts.DefenceOrganisation defenceOrganisation = defenceOrganisation()
                .withLaaContractNumber(LAA_CONTRACT_NUMBER)
                .withOrganisation(Organisation.organisation()
                        .withName(ORGANISATION_NAME)
                        .withAddress(Address.address()
                                .withPostcode(POST_CODE)
                                .withAddress1(ADDRESS_1)
                                .withAddress2(ADDRESS_2)
                                .withAddress3(ADDRESS_3)
                                .withAddress4(ADDRESS_4)
                                .withAddress5(ADDRESS_5)
                                .build())
                        .build())
                .build();
        final AssociatedDefenceOrganisation associatedDefenceOrganisationJpa = mapper.toJPA(getAssociatedDefenceOrganisation(defenceOrganisation));

        assertThat(associatedDefenceOrganisationJpa.getApplicationReference(), is(APPLICATION_REFERENCE));
        assertThat(associatedDefenceOrganisationJpa.getAssociatedByLAA(), is(true));
        assertThat(associatedDefenceOrganisationJpa.getAssociationEndDate(), is(associationEndDate));
        assertThat(associatedDefenceOrganisationJpa.getAssociationStartDate(), is(associationStartDate));
        assertThat(associatedDefenceOrganisationJpa.getFundingType(), is(FundingType.REPRESENTATION_ORDER));

        final DefenceOrganisation defenceOrganisationJpa = associatedDefenceOrganisationJpa.getDefenceOrganisation();

        assertThat(defenceOrganisationJpa, notNullValue());
        assertThat(defenceOrganisationJpa.getLaaContractNumber(), is(LAA_CONTRACT_NUMBER));
        assertThat(defenceOrganisationJpa.getName(), is(ORGANISATION_NAME));
        assertThat(defenceOrganisationJpa.getAddress().getPostCode(), is(POST_CODE));
        assertThat(defenceOrganisationJpa.getAddress().getAddress1(), is(ADDRESS_1));
        assertThat(defenceOrganisationJpa.getAddress().getAddress2(), is(ADDRESS_2));
        assertThat(defenceOrganisationJpa.getAddress().getAddress3(), is(ADDRESS_3));
        assertThat(defenceOrganisationJpa.getAddress().getAddress4(), is(ADDRESS_4));
        assertThat(defenceOrganisationJpa.getAddress().getAddress5(), is(ADDRESS_5));

    }

    @Test
    public void fromJPA() {
        AssociatedDefenceOrganisation associatedDefenceOrganisationJpa = getAssociatedDefenceOrganisation();

        uk.gov.justice.core.courts.AssociatedDefenceOrganisation associatedDefenceOrganisation = mapper.fromJPA(associatedDefenceOrganisationJpa);

        assertThat(associatedDefenceOrganisation, notNullValue());
        assertThat(associatedDefenceOrganisationJpa.getApplicationReference(), is(APPLICATION_REFERENCE));
        assertThat(associatedDefenceOrganisationJpa.getFundingType(), is(FundingType.REPRESENTATION_ORDER));
        assertThat(associatedDefenceOrganisationJpa.getAssociationStartDate(), is(associationStartDate));
        assertThat(associatedDefenceOrganisationJpa.getAssociationEndDate(), is(associationEndDate));
        assertThat(associatedDefenceOrganisation.getIsAssociatedByLAA(), is(true));

        final uk.gov.justice.core.courts.DefenceOrganisation defenceOrganisation = associatedDefenceOrganisation.getDefenceOrganisation();
        assertThat(defenceOrganisation, notNullValue());
        assertThat(defenceOrganisation.getLaaContractNumber(), is(LAA_CONTRACT_NUMBER));
        assertThat(defenceOrganisation.getOrganisation().getName(), is(ORGANISATION_NAME));
        assertThat(defenceOrganisation.getOrganisation().getIncorporationNumber(), is(INCORPORATION_NUMBER));
        assertThat(defenceOrganisation.getOrganisation().getRegisteredCharityNumber(), is(REGISTERED_CHARITY_NUMBER));

        assertThat(defenceOrganisation.getOrganisation().getAddress().getPostcode(), is(POST_CODE));
        assertThat(defenceOrganisation.getOrganisation().getAddress().getAddress1(), is(ADDRESS_1));
        assertThat(defenceOrganisation.getOrganisation().getAddress().getAddress2(), is(ADDRESS_2));
        assertThat(defenceOrganisation.getOrganisation().getAddress().getAddress3(), is(ADDRESS_3));
        assertThat(defenceOrganisation.getOrganisation().getAddress().getAddress4(), is(ADDRESS_4));
        assertThat(defenceOrganisation.getOrganisation().getAddress().getAddress5(), is(ADDRESS_5));

        assertThat(defenceOrganisation.getOrganisation().getContact().getPrimaryEmail(), is("test@test.com"));


    }

    private AssociatedDefenceOrganisation getAssociatedDefenceOrganisation() {
        final DefenceOrganisation defenceOrganisationJpa = new DefenceOrganisation();
        defenceOrganisationJpa.setLaaContractNumber(LAA_CONTRACT_NUMBER);
        defenceOrganisationJpa.setIncorporationNumber(INCORPORATION_NUMBER);
        defenceOrganisationJpa.setRegisteredCharityNumber(REGISTERED_CHARITY_NUMBER);
        defenceOrganisationJpa.setName(ORGANISATION_NAME);

        final Contact contact = new Contact();
        contact.setWork("020 7777777");
        contact.setFax("020 11111111");
        contact.setHome("020 5555555");
        contact.setMobile("07511201630");
        contact.setPrimaryEmail("test@test.com");
        contact.setSecondaryEmail("test-sec@test.com");
        defenceOrganisationJpa.setContact(contact);

        final uk.gov.moj.cpp.hearing.persist.entity.ha.Address addressJpa = new uk.gov.moj.cpp.hearing.persist.entity.ha.Address();
        addressJpa.setPostCode(POST_CODE);
        addressJpa.setAddress1(ADDRESS_1);
        addressJpa.setAddress2(ADDRESS_2);
        addressJpa.setAddress3(ADDRESS_3);
        addressJpa.setAddress4(ADDRESS_4);
        addressJpa.setAddress5(ADDRESS_5);

        defenceOrganisationJpa.setAddress(addressJpa);

        AssociatedDefenceOrganisation associatedDefenceOrganisationJpa = new AssociatedDefenceOrganisation();
        associatedDefenceOrganisationJpa.setApplicationReference(APPLICATION_REFERENCE);
        associatedDefenceOrganisationJpa.setFundingType(FundingType.REPRESENTATION_ORDER);
        associatedDefenceOrganisationJpa.setAssociatedByLAA(true);
        associatedDefenceOrganisationJpa.setAssociationEndDate(associationEndDate);
        associatedDefenceOrganisationJpa.setAssociationStartDate(associationStartDate);
        associatedDefenceOrganisationJpa.setDefenceOrganisation(defenceOrganisationJpa);
        return associatedDefenceOrganisationJpa;
    }

    private uk.gov.justice.core.courts.AssociatedDefenceOrganisation getAssociatedDefenceOrganisation(
            uk.gov.justice.core.courts.DefenceOrganisation defenceOrganisation) {

        return associatedDefenceOrganisation()
                .withApplicationReference(APPLICATION_REFERENCE)
                .withIsAssociatedByLAA(true)
                .withAssociationEndDate(associationEndDate)
                .withAssociationStartDate(associationStartDate)
                .withFundingType(FundingType.REPRESENTATION_ORDER)
                .withDefenceOrganisation(
                        defenceOrganisation)
                .build();
    }
}