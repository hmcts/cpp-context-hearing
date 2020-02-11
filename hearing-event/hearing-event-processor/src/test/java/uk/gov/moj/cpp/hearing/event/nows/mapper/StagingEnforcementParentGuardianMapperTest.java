package uk.gov.moj.cpp.hearing.event.nows.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.AssociatedPerson;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.json.schemas.staging.ParentGuardian;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StagingEnforcementParentGuardianMapperTest {


    @Test
    public void testCreatesNoParentGuardian_withNull() {
        Defendant defendant = Defendant.defendant().withAssociatedPersons(null).build();

        ParentGuardian parentGuardian = new StagingEnforcementParentGuardianMapper(defendant).map();

        assertNull(parentGuardian);
    }

    @Test
    public void testCreatesNoParentGuardian_withEmpty() {
        Defendant defendant = Defendant.defendant().withAssociatedPersons(Collections.emptyList()).build();

        ParentGuardian parentGuardian = new StagingEnforcementParentGuardianMapper(defendant).map();

        assertNull(parentGuardian);
    }

    @Test
    public void testCreatesNoParentGuardian_withInvalidAssociatedPersons() {
        Defendant defendant = Defendant.defendant().withAssociatedPersons(Arrays.asList(
                AssociatedPerson.associatedPerson().withRole("COURTCLERK").withPerson(createTestPerson("0")).build(),
                AssociatedPerson.associatedPerson().withRole("adasd").withPerson(createTestPerson("1")).build(),
                AssociatedPerson.associatedPerson().withRole("awewe").withPerson(createTestPerson("2")).build()
        )).build();
        ParentGuardian parentGuardian = new StagingEnforcementParentGuardianMapper(defendant).map();

        assertNull(parentGuardian);
    }

    @Test
    public void testCreatesParentGuardianFromFirstParent() {
        Defendant defendant = Defendant.defendant().withAssociatedPersons(Arrays.asList(
                AssociatedPerson.associatedPerson().withRole("COURTCLERK").withPerson(createTestPerson("0")).build(),
                AssociatedPerson.associatedPerson().withRole("guardian").withPerson(createTestPerson("1")).build(),
                AssociatedPerson.associatedPerson().withRole("PARENT").withPerson(createTestPerson("2")).build(),
                AssociatedPerson.associatedPerson().withRole("parent").withPerson(createTestPerson("3")).build(),
                AssociatedPerson.associatedPerson().withRole("some").withPerson(createTestPerson("4")).build()
        )).build();

        ParentGuardian parentGuardian = new StagingEnforcementParentGuardianMapper(defendant).map();

        verifyParentGuardian(parentGuardian, defendant.getAssociatedPersons().get(2));
    }

    @Test
    public void testCreatesParentGuardianFromFirstParent_SkipsNullPerson() {
        Defendant defendant = Defendant.defendant().withAssociatedPersons(Arrays.asList(
                AssociatedPerson.associatedPerson().withRole("COURTCLERK").withPerson(createTestPerson("0")).build(),
                AssociatedPerson.associatedPerson().withRole("guardian").withPerson(createTestPerson("1")).build(),
                AssociatedPerson.associatedPerson().withRole("PARENT").build(),
                AssociatedPerson.associatedPerson().withRole("parent").withPerson(createTestPerson("3")).build(),
                AssociatedPerson.associatedPerson().withRole("some").withPerson(createTestPerson("4")).build()
        )).build();

        ParentGuardian parentGuardian = new StagingEnforcementParentGuardianMapper(defendant).map();

        verifyParentGuardian(parentGuardian, defendant.getAssociatedPersons().get(3));
    }

    @Test
    public void testCreatesParentGuardianFromFirstGuardian_WithNoParent() {
        Defendant defendant = Defendant.defendant().withAssociatedPersons(Arrays.asList(
                AssociatedPerson.associatedPerson().withRole("COURTCLERK").withPerson(createTestPerson("0")).build(),
                AssociatedPerson.associatedPerson().withRole("GUARDIAN").withPerson(createTestPerson("1")).build(),
                AssociatedPerson.associatedPerson().withRole("some").withPerson(createTestPerson("3")).build(),
                AssociatedPerson.associatedPerson().withRole("guardian").withPerson(createTestPerson("1")).build()
        )).build();

        ParentGuardian parentGuardian = new StagingEnforcementParentGuardianMapper(defendant).map();

        verifyParentGuardian(parentGuardian, defendant.getAssociatedPersons().get(1));
    }

    private void verifyParentGuardian(final ParentGuardian parentGuardian, final AssociatedPerson associatedPerson) {
        Person person = associatedPerson.getPerson();
        assertEquals(parentGuardian.getName(), person.getFirstName() + " " + person.getMiddleName() + " " + person.getLastName());
        assertEquals(parentGuardian.getAddress1(), person.getAddress().getAddress1());
        assertEquals(parentGuardian.getAddress2(), person.getAddress().getAddress2());
        assertEquals(parentGuardian.getAddress3(), person.getAddress().getAddress3());
        assertEquals(parentGuardian.getAddress4(), person.getAddress().getAddress4());
        assertEquals(parentGuardian.getAddress5(), person.getAddress().getAddress5());
        assertEquals(parentGuardian.getPostcode(), person.getAddress().getPostcode());
    }

    private Person createTestPerson(final String suffix) {
        return Person.person()
                .withFirstName("firstName" + suffix)
                .withMiddleName("middleName" + suffix)
                .withLastName("lastName1")
                .withAddress(Address.address()
                        .withAddress1("address1-" + suffix)
                        .withAddress2("address2-" + suffix)
                        .withAddress3("address3-" + suffix)
                        .withAddress4("address4-" + suffix)
                        .withAddress5("address5-" + suffix)
                        .withPostcode("postCode-" + suffix)
                        .build())
                .build();
    }
}