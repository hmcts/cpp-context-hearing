package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.core.courts.Defendant.defendant;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.LegalEntityDefendant;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Organisation;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.PersonDefendant;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class DefendantDetailsUtilsTest {

    private UUID offenceId;
    private UUID prosecutionCaseId;
    private UUID defendantId;

    @DataPoints
    public static Address[] CHANNELS = {

            Address.address().withAddress1("address11").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build(), //Line 1
            Address.address().withAddress1("address1").withAddress2("address22").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build(), //Line 2
            Address.address().withAddress1("address1").withAddress2("address2").withAddress3("address33").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build(), //Line 3
            Address.address().withAddress1("address1").withAddress2("address2").withAddress3("address3").withAddress4("address44").withAddress5("address5").withPostcode("xyz").build(), //Line 4
            Address.address().withAddress1("address1").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address55").withPostcode("xyz").build(), //Line 5
            Address.address().withAddress1("address1").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz1").build(), //Post Code
            Address.address().withAddress1("address1").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").withWelshAddress1("address 1").build(), //Welsh Line 1
            Address.address().withAddress1("address1").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").withWelshAddress2("address 2").build(), //Welsh Line 2
            Address.address().withAddress1("address1").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").withWelshAddress3("address 3").build(), //Welsh Line 3
            Address.address().withAddress1("address1").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").withWelshAddress4("address 4").build(), //Welsh Line 4
            Address.address().withAddress1("address1").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").withWelshAddress5("address 5").build(), //Welsh Line 5
            Address.address().withAddress1("address11").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address55").withPostcode("xyz").build(), //Line 1 and 5
    };

    @BeforeEach
    public void init() {
        offenceId = randomUUID();
        prosecutionCaseId = randomUUID();
        defendantId = randomUUID();
    }

    @Theory
    public void whenThereIsAChangeInAddressForDefendantHearingShouldListDefendantForDDCH(final Address changedAddress) {

        final Address address = Address.address().withAddress1("address1").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build();

        Defendant defendant = createIndividualDefendant(prosecutionCaseId, offenceId, defendantId, "Tim", null, "Karke", getDate("2015-09-08"), "UK", address);

        DefendantDetailsUtils defendantDetailsUtils = new DefendantDetailsUtils();

        final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendantDetails = createUpdatedDefendantDetails(prosecutionCaseId, defendantId, "Tim", null, "Karke",
                getDate("2015-09-08"), "UK", changedAddress);

        assertThat(defendantDetailsUtils.verifyDDCHOnRequiredAttributes(defendant, updatedDefendantDetails), is(false));
    }


    @Test
    public void shouldListDefendantForDDCHWhenThereIsAChangeInFirstNameForDefendantHearing() {

        final Address address =Address.address().withAddress1("address11").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build();
        Defendant defendant = createIndividualDefendant(prosecutionCaseId, offenceId, defendantId, "Tim", null, "Karke", getDate("2015-09-08"), "UK", address);

        DefendantDetailsUtils defendantDetailsUtils = new DefendantDetailsUtils();

        final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendantDetails = createUpdatedDefendantDetails(prosecutionCaseId, defendantId, "Tim1", null, "Karke",
                getDate("2015-09-08"), "UK", address);

        assertThat(defendantDetailsUtils.verifyDDCHOnRequiredAttributes(defendant, updatedDefendantDetails), is(false));
    }

    @Test
    public void shouldListDefendantForDDCHWhenThereIsAChangeInLastNameForDefendantHearing() {
        final Address address = Address.address().withAddress1("address11").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build();
        Defendant defendant = createIndividualDefendant(prosecutionCaseId, offenceId, defendantId, "Tim", null, "Karke", getDate("2015-09-08"), "UK", address);


        DefendantDetailsUtils defendantDetailsUtils = new DefendantDetailsUtils();

        final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendantDetails = createUpdatedDefendantDetails(prosecutionCaseId, defendantId, "Tim", null, "Karke1",
                getDate("2015-09-08"), "UK", address);

        assertThat(defendantDetailsUtils.verifyDDCHOnRequiredAttributes(defendant, updatedDefendantDetails), is(false));
    }

    @Test
    public void shouldListDefendantForDDCHWhenThereIsAChangeInMiddleNameForDefendantHearing() {

        final Address address = Address.address().withAddress1("address11").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build();
        Defendant defendant = createIndividualDefendant(prosecutionCaseId, offenceId, defendantId, "Tim", null, "Karke", getDate("2015-09-08"), "UK", address);
        DefendantDetailsUtils defendantDetailsUtils = new DefendantDetailsUtils();
        final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendantDetails = createUpdatedDefendantDetails(prosecutionCaseId, defendantId, "Tim", "middle", "Karke",
                getDate("2015-09-08"), "UK", address);
        assertThat(defendantDetailsUtils.verifyDDCHOnRequiredAttributes(defendant, updatedDefendantDetails), is(false));
    }

    @Test
    public void shouldListDefendantForDDCHWhenThereIsAChangeInDateOfBirthForDefendantHearing() {

        final Address address = Address.address().withAddress1("address11").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build();
        Defendant defendant = createIndividualDefendant(prosecutionCaseId, offenceId, defendantId, "Tim", null, "Karke", getDate("2015-09-08"), "UK", address);

        DefendantDetailsUtils defendantDetailsUtils = new DefendantDetailsUtils();

        final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendantDetails = createUpdatedDefendantDetails(prosecutionCaseId, defendantId, "Tim", null, "Karke",
                getDate("1965-09-08"), "UK", address);
        assertThat(defendantDetailsUtils.verifyDDCHOnRequiredAttributes(defendant, updatedDefendantDetails), is(false));
    }

    @Test
    public void shouldListDefendantForDDCHWhenThereIsAChangeInNullDateOfBirthForDefendantHearing() {

        final Address address = Address.address().withAddress1("address11").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build();
        Defendant defendant = createIndividualDefendant(prosecutionCaseId, offenceId, defendantId, "Tim", null, "Karke", null, "UK", address);

        DefendantDetailsUtils defendantDetailsUtils = new DefendantDetailsUtils();

        final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendantDetails = createUpdatedDefendantDetails(prosecutionCaseId, defendantId, "Tim", null, "Karke",
                getDate("1965-09-08"), "UK", address);
        assertThat(defendantDetailsUtils.verifyDDCHOnRequiredAttributes(defendant, updatedDefendantDetails), is(false));
    }

    @Test
    public void shouldListDefendantForDDCHWhenThereIsAChangeInNationalityForDefendantHearing() {

        final Address address = Address.address().withAddress1("address11").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build();
        Defendant defendant = createIndividualDefendant(prosecutionCaseId, offenceId, defendantId, "Tim", null, "Karke", getDate("2015-09-08"), "UK", address);

        DefendantDetailsUtils defendantDetailsUtils = new DefendantDetailsUtils();
        final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendantDetails = createUpdatedDefendantDetails(prosecutionCaseId, defendantId, "Tim", null, "Karke",
                getDate("2015-09-08"), "USA", address);
        assertThat(defendantDetailsUtils.verifyDDCHOnRequiredAttributes(defendant, updatedDefendantDetails), is(false));
    }

    @Test
    public void shouldNotListDefendantForDDCHWhenThereIsANoChangeForDefendantHearing() {

        final Address address =Address.address().withAddress1("address11").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build();
        Defendant defendant = createIndividualDefendant(prosecutionCaseId, offenceId, defendantId, "Tim", null, "Karke", getDate("2015-09-08"), "UK", address);
        DefendantDetailsUtils defendantDetailsUtils = new DefendantDetailsUtils();

        final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendantDetails = createUpdatedDefendantDetails(prosecutionCaseId, defendantId, "Tim", null, "Karke",
                getDate("2015-09-08"), "UK", address);

        assertThat(defendantDetailsUtils.verifyDDCHOnRequiredAttributes(defendant, updatedDefendantDetails), is(true));
    }

    @Test
    public void shouldListDefendantForDDCHWhenThereIsAChangeInOrganisationNameForDefendantHearing() {

        final Address address = Address.address().withAddress1("address11").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build();
        Defendant defendant = createOrganisationDefendant(prosecutionCaseId, offenceId, defendantId, "Versu Ltd", address);

        DefendantDetailsUtils defendantDetailsUtils = new DefendantDetailsUtils();

        final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendantDetails = createUpdatedDefendantDetailsForOrganisation(prosecutionCaseId, defendantId, "Tim Ltd", address);

        assertThat(defendantDetailsUtils.verifyDDCHOnRequiredAttributes(defendant, updatedDefendantDetails), is(false));
    }

    @Theory
    public void shouldListDefendantForDDCHWhenThereIsAChangeInOrganisationAddressForDefendantHearing(final Address changedAddress) {

        final Address address = Address.address().withAddress1("address1").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build();
        Defendant defendant = createOrganisationDefendant(prosecutionCaseId, offenceId, defendantId, "Versu Ltd", address);


        DefendantDetailsUtils defendantDetailsUtils = new DefendantDetailsUtils();

        final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendantDetails = createUpdatedDefendantDetailsForOrganisation(prosecutionCaseId, defendantId, "Versu Ltd", changedAddress);

        assertThat(defendantDetailsUtils.verifyDDCHOnRequiredAttributes(defendant, updatedDefendantDetails), is(false));
    }

    @Test
    public void shouldListDefendantForDDCHWhenThereIsANullOrganisationAddressForDefendantHearing() {

        Defendant defendant = createOrganisationDefendant(prosecutionCaseId, offenceId, defendantId, "Versu Ltd", null);

        DefendantDetailsUtils defendantDetailsUtils = new DefendantDetailsUtils();

        final Address updatedAddress = Address.address().withAddress1("address2").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build();

        final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendantDetails = createUpdatedDefendantDetailsForOrganisation(prosecutionCaseId, defendantId, "Versu Ltd", updatedAddress);

        assertThat(defendantDetailsUtils.verifyDDCHOnRequiredAttributes(defendant, updatedDefendantDetails), is(false));
    }

    @Test
    public void whenThereIsNoChangeInOrganisationDetailsForDefendantHearingShouldNotListDefendantForDDCH() {

        final Address address = Address.address().withAddress1("address11").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build();
        Defendant defendant = createOrganisationDefendant(prosecutionCaseId, offenceId, defendantId, "Versu Ltd", address);


        DefendantDetailsUtils defendantDetailsUtils = new DefendantDetailsUtils();


        final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendantDetails = createUpdatedDefendantDetailsForOrganisation(prosecutionCaseId, defendantId, "Versu Ltd", address);

        assertThat(defendantDetailsUtils.verifyDDCHOnRequiredAttributes(defendant, updatedDefendantDetails), is(true));
    }

    @Test
    public void shouldListDefendantForDDCHWhenThereIsAChangeInOrganisationAddressForDefendantHearingWithNoPreviousAddress() {

        final Address address = Address.address().withAddress1("address1").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build();
        Defendant defendant = createOrganisationDefendant(prosecutionCaseId, offenceId, defendantId, "Versu Ltd", null);


        DefendantDetailsUtils defendantDetailsUtils = new DefendantDetailsUtils();

        final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendantDetails = createUpdatedDefendantDetailsForOrganisation(prosecutionCaseId, defendantId, "Versu Ltd", address);

        assertThat(defendantDetailsUtils.verifyDDCHOnRequiredAttributes(defendant, updatedDefendantDetails), is(false));
    }

    private uk.gov.moj.cpp.hearing.command.defendant.Defendant createUpdatedDefendantDetailsForOrganisation(final UUID prosecutionCaseId,final UUID defendantId, final String organisationName, final Address address) {

        uk.gov.moj.cpp.hearing.command.defendant.Defendant defendant = new uk.gov.moj.cpp.hearing.command.defendant.Defendant();
        defendant.setId(defendantId);
        defendant.setLegalEntityDefendant(LegalEntityDefendant.legalEntityDefendant().withOrganisation(Organisation.organisation().withName(organisationName).withAddress(address).build()).build());
        defendant.setProsecutionCaseId(prosecutionCaseId);
        return defendant;

    }

    private uk.gov.moj.cpp.hearing.command.defendant.Defendant createUpdatedDefendantDetails(final UUID prosecutionCaseId, final UUID defendantId, final String firstName, final String middleName, final String lastName, final LocalDate dateOfBirth, final String nationality, final Address address) {

        uk.gov.moj.cpp.hearing.command.defendant.Defendant defendant = new uk.gov.moj.cpp.hearing.command.defendant.Defendant();

        Person person = Person.person()
                .withFirstName(firstName)
                .withMiddleName(middleName)
                .withLastName(lastName)
                .withNationalityCode(nationality)
                .withDateOfBirth(dateOfBirth)
                .withAddress(address)
                .build();
        PersonDefendant personDefendant = PersonDefendant.personDefendant().withPersonDetails(person).build();
        defendant.setId(defendantId);
        defendant.setPersonDefendant(personDefendant);
        defendant.setProsecutionCaseId(prosecutionCaseId);
        return defendant;

    }

    private Defendant createIndividualDefendant(final UUID prosecutionCaseId, final UUID offenceId, final UUID defendantId, final String firstName, final String middleName, final String lastName, final LocalDate dateOfBirth, final String nationality, final Address address) {
        return defendant()
                .withId(defendantId)
                .withProsecutionCaseId(prosecutionCaseId)
                .withPersonDefendant(
                        PersonDefendant.personDefendant()
                                .withPersonDetails(Person.person()
                                        .withFirstName(firstName)
                                        .withLastName(lastName)
                                        .withMiddleName(middleName)
                                        .withNationalityCode(nationality)
                                        .withDateOfBirth(dateOfBirth)
                                        .withAddress(address)
                                        .build())
                                .build()
                )
                .withOffences(asList(Offence.offence().withId(offenceId).build()))
                .build();
    }

    private Defendant createOrganisationDefendant(final UUID prosecutionCaseId, final UUID offenceId, final UUID defendantId, final String organisationName, final Address address) {
        return defendant()
                .withId(defendantId)
                .withProsecutionCaseId(prosecutionCaseId)
                .withLegalEntityDefendant(LegalEntityDefendant.legalEntityDefendant().withOrganisation(Organisation.organisation()
                        .withName(organisationName)
                        .withAddress(address)
                        .build()).build())
                .withOffences(asList(Offence.offence().withId(offenceId).build()))
                .build();
    }

    private LocalDate getDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter. ofPattern( "yyyy-MM-dd" );
        return LocalDate. parse( date , formatter);
    }

}