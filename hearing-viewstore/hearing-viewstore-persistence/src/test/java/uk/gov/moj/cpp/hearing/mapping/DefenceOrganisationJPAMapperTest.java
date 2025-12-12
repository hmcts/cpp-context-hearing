package uk.gov.moj.cpp.hearing.mapping;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.core.courts.ContactNumber;
import uk.gov.justice.core.courts.Organisation;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Address;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Contact;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DefenceOrganisation;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

public class DefenceOrganisationJPAMapperTest {

    private final DefenceOrganisationJPAMapper defenceOrganisationJPAMapper = JPACompositeMappers.DEFENCE_ORGANISATION_JPA_MAPPER;

    @Test
    public void toJPA() {
        final uk.gov.justice.core.courts.Address address = new uk.gov.justice.core.courts.Address(
                "Address 1", "Address 2", "Address 3",
                "Address 4", "Address 5", "IG11NL","Address 1", "Address 2", "Address 3",
                "Address 4", "Address 5");

        final ContactNumber contactNumber = new ContactNumber("020 11111111", "020 88888888", "07411222111",
                "test@test.com", "test-sec@test.com", "020 3333333");

        final Organisation organisation = Organisation.organisation()
                .withName("ABC LTD")
                .withAddress(address)
                .withContact(contactNumber)
                .build();

        final uk.gov.justice.core.courts.DefenceOrganisation defenceOrganisationPojo = new uk.gov.justice.core.courts.DefenceOrganisation("LAA123", organisation);
        final DefenceOrganisation defenceOrganisation = defenceOrganisationJPAMapper.toJPA(defenceOrganisationPojo);

        assertThat(defenceOrganisation, CoreMatchers.notNullValue());
        assertThat(defenceOrganisation.getLaaContractNumber(), is("LAA123"));

        Address addressResulted = defenceOrganisation.getAddress();
        assertThat(defenceOrganisation.getName(), is("ABC LTD"));
        assertThat(addressResulted.getAddress1(), is("Address 1"));
        assertThat(addressResulted.getAddress2(), is("Address 2"));
        assertThat(addressResulted.getAddress3(), is("Address 3"));
        assertThat(addressResulted.getAddress4(), is("Address 4"));
        assertThat(addressResulted.getAddress5(), is("Address 5"));
        assertThat(addressResulted.getPostCode(), is("IG11NL"));

        Contact contactResulted = defenceOrganisation.getContact();
        assertThat(contactResulted.getFax(), is("020 11111111"));
        assertThat(contactResulted.getHome(), is("020 88888888"));
        assertThat(contactResulted.getMobile(), is("07411222111"));
        assertThat(contactResulted.getPrimaryEmail(), is("test@test.com"));
        assertThat(contactResulted.getSecondaryEmail(), is("test-sec@test.com"));
        assertThat(contactResulted.getWork(), is("020 3333333"));
    }

    @Test
    public void fromJPA() {

        final DefenceOrganisation defenceOrganisation = getDefenceOrganisationJPA();
        final uk.gov.justice.core.courts.DefenceOrganisation defenceOrganisationPojo = defenceOrganisationJPAMapper.fromJPA(defenceOrganisation);

        assertThat(defenceOrganisationPojo, CoreMatchers.notNullValue());
        assertThat(defenceOrganisationPojo.getLaaContractNumber(), is("LAA123"));
        assertThat(defenceOrganisationPojo.getOrganisation().getName(), is("ABC LTD"));
        assertThat(defenceOrganisationPojo.getOrganisation().getIncorporationNumber(), is("Inc00001"));
        assertThat(defenceOrganisationPojo.getOrganisation().getRegisteredCharityNumber(), is("Reg000001"));

        final uk.gov.justice.core.courts.Address addressResulted = defenceOrganisationPojo.getOrganisation().getAddress();
        assertThat(addressResulted.getAddress1(), is("Address 1"));
        assertThat(addressResulted.getAddress2(), is("Address 2"));
        assertThat(addressResulted.getAddress3(), is("Address 3"));
        assertThat(addressResulted.getAddress4(), is("Address 4"));
        assertThat(addressResulted.getAddress5(), is("Address 5"));
        assertThat(addressResulted.getPostcode(), is("IG11NL"));

        ContactNumber contactResulted = defenceOrganisationPojo.getOrganisation().getContact();

        assertThat(contactResulted.getHome(), is("020 88888888"));
        assertThat(contactResulted.getFax(), is("020 11111111"));
        assertThat(contactResulted.getMobile(), is("07411222111"));
        assertThat(contactResulted.getPrimaryEmail(), is("test@test.com"));
        assertThat(contactResulted.getSecondaryEmail(), is("test-sec@test.com"));
    }

    private DefenceOrganisation getDefenceOrganisationJPA() {
        final DefenceOrganisation defenceOrganisation = new DefenceOrganisation();
        defenceOrganisation.setLaaContractNumber("LAA123");
        defenceOrganisation.setName("ABC LTD");
        defenceOrganisation.setRegisteredCharityNumber("Reg000001");
        defenceOrganisation.setIncorporationNumber("Inc00001");

        final Address address = new Address();
        address.setPostCode("IG11NL");
        address.setAddress1("Address 1");
        address.setAddress2("Address 2");
        address.setAddress3("Address 3");
        address.setAddress4("Address 4");
        address.setAddress5("Address 5");
        defenceOrganisation.setAddress(address);

        final Contact contact = new Contact();
        contact.setPrimaryEmail("test@test.com");
        contact.setSecondaryEmail("test-sec@test.com");
        contact.setMobile("07411222111");
        contact.setHome("020 88888888");
        contact.setFax("020 11111111");
        contact.setWork("020 3333333");
        defenceOrganisation.setContact(contact);
        return defenceOrganisation;
    }
}