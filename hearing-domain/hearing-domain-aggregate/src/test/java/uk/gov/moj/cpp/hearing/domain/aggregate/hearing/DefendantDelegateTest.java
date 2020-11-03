package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.core.courts.Defendant.defendant;
import static uk.gov.justice.core.courts.Hearing.hearing;
import static uk.gov.justice.core.courts.ProsecutionCase.prosecutionCase;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.AssociatedDefenceOrganisation;
import uk.gov.justice.core.courts.DefenceOrganisation;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.LegalEntityDefendant;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Organisation;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;


@RunWith(Theories.class)
public class DefendantDelegateTest {

    private UUID offenceId;
    private UUID prosecutionCaseId;
    private UUID hearingId;
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

    @Before
    public void init() {
        offenceId = randomUUID();
        prosecutionCaseId = randomUUID();
        hearingId = randomUUID();
        defendantId = randomUUID();
    }

    @Theory
    public void shouldListDefendantForDDCHWhenThereIsAChangeInAddressForDefendantHearing(final Address changedAddress) {

        HearingAggregateMomento memento = new HearingAggregateMomento();
        final Address address = Address.address().withAddress1("address1").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build();

        Defendant defendant = createIndividualDefendant(prosecutionCaseId, offenceId, defendantId, "Tim", null, "Karke", getDate("2015-09-08"), "UK", address);
        memento.setHearing(createHearing(prosecutionCaseId, hearingId, defendant));


        DefendantDelegate defendantDelegate = new DefendantDelegate(memento);


        final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendantDetails = createUpdatedDefendantDetails(prosecutionCaseId, defendantId, "Tim", null, "Karke",
                getDate("2015-09-08"), "UK", changedAddress);
        defendantDelegate.handleDefendantDetailsUpdated(createDefendantDetailsUpdated(hearingId, updatedDefendantDetails));

        assertThat(defendantDelegate.getDefendantDetailsChanged().contains(defendantId), is(true));
    }


    @Test
    public void shouldListDefendantForDDCHWhenThereIsAChangeInFirstNameForDefendantHearing() {

        HearingAggregateMomento memento = new HearingAggregateMomento();
        final Address address = Address.address().withAddress1("address11").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build();
        Defendant defendant = createIndividualDefendant(prosecutionCaseId, offenceId, defendantId, "Tim", null, "Karke", getDate("2015-09-08"), "UK", address);
        memento.setHearing(createHearing(prosecutionCaseId, hearingId, defendant));


        DefendantDelegate defendantDelegate = new DefendantDelegate(memento);


        final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendantDetails = createUpdatedDefendantDetails(prosecutionCaseId, defendantId, "Tim1", null, "Karke",
                getDate("2015-09-08"), "UK", address);
        defendantDelegate.handleDefendantDetailsUpdated(createDefendantDetailsUpdated(hearingId, updatedDefendantDetails));

        assertThat(defendantDelegate.getDefendantDetailsChanged().contains(defendantId), is(true));
    }

    @Test
    public void shouldListDefendantForDDCHWhenThereIsAChangeInLastNameForDefendantHearing() {

        HearingAggregateMomento memento = new HearingAggregateMomento();
        final Address address = Address.address().withAddress1("address11").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build();
        Defendant defendant = createIndividualDefendant(prosecutionCaseId, offenceId, defendantId, "Tim", null, "Karke", getDate("2015-09-08"), "UK", address);
        memento.setHearing(createHearing(prosecutionCaseId, hearingId, defendant));


        DefendantDelegate defendantDelegate = new DefendantDelegate(memento);


        final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendantDetails = createUpdatedDefendantDetails(prosecutionCaseId, defendantId, "Tim", null, "Karke1",
                getDate("2015-09-08"), "UK", address);
        defendantDelegate.handleDefendantDetailsUpdated(createDefendantDetailsUpdated(hearingId, updatedDefendantDetails));

        assertThat(defendantDelegate.getDefendantDetailsChanged().contains(defendantId), is(true));
    }

    @Test
    public void shouldListDefendantForDDCHWhenThereIsAChangeInMiddleNameForDefendantHearing() {

        HearingAggregateMomento memento = new HearingAggregateMomento();
        final Address address = Address.address().withAddress1("address11").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build();
        Defendant defendant = createIndividualDefendant(prosecutionCaseId, offenceId, defendantId, "Tim", null, "Karke", getDate("2015-09-08"), "UK", address);
        memento.setHearing(createHearing(prosecutionCaseId, hearingId, defendant));


        DefendantDelegate defendantDelegate = new DefendantDelegate(memento);


        final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendantDetails = createUpdatedDefendantDetails(prosecutionCaseId, defendantId, "Tim", "middle", "Karke",
                getDate("2015-09-08"), "UK", address);
        defendantDelegate.handleDefendantDetailsUpdated(createDefendantDetailsUpdated(hearingId, updatedDefendantDetails));

        assertThat(defendantDelegate.getDefendantDetailsChanged().contains(defendantId), is(true));
    }

    @Test
    public void shouldListDefendantForDDCHWhenThereIsAChangeInDateOfBirthForDefendantHearing() {

        HearingAggregateMomento memento = new HearingAggregateMomento();
        final Address address = Address.address().withAddress1("address11").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build();
        Defendant defendant = createIndividualDefendant(prosecutionCaseId, offenceId, defendantId, "Tim", null, "Karke", getDate("2015-09-08"), "UK", address);
        memento.setHearing(createHearing(prosecutionCaseId, hearingId, defendant));


        DefendantDelegate defendantDelegate = new DefendantDelegate(memento);

        final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendantDetails = createUpdatedDefendantDetails(prosecutionCaseId, defendantId, "Tim", null, "Karke",
                getDate("1965-09-08"), "UK", address);
        defendantDelegate.handleDefendantDetailsUpdated(createDefendantDetailsUpdated(hearingId, updatedDefendantDetails));

        assertThat(defendantDelegate.getDefendantDetailsChanged().contains(defendantId), is(true));
    }

    @Test
    public void shouldClearDefendantDetailsChanged() {

        HearingAggregateMomento memento = new HearingAggregateMomento();
        final Address address = Address.address().withAddress1("address11").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build();
        Defendant defendant = createIndividualDefendant(prosecutionCaseId, offenceId, defendantId, "Tim", null, "Karke", getDate("2015-09-08"), "UK", address);
        memento.setHearing(createHearing(prosecutionCaseId, hearingId, defendant));

        DefendantDelegate defendantDelegate = new DefendantDelegate(memento);

        final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendantDetails = createUpdatedDefendantDetails(prosecutionCaseId, defendantId, "Tim", null, "Karke",
                getDate("1965-09-08"), "UK", address);
        defendantDelegate.handleDefendantDetailsUpdated(createDefendantDetailsUpdated(hearingId, updatedDefendantDetails));

        assertThat(defendantDelegate.getDefendantDetailsChanged().contains(defendantId), is(true));

        defendantDelegate.clearDefendantDetailsChanged();

        assertThat(defendantDelegate.getDefendantDetailsChanged().isEmpty(), is(true));
    }

    @Test
    public void shouldListDefendantForDDCHWhenThereIsAChangeInNationalityForDefendantHearing() {

        HearingAggregateMomento memento = new HearingAggregateMomento();
        final Address address = Address.address().withAddress1("address11").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build();
        Defendant defendant = createIndividualDefendant(prosecutionCaseId, offenceId, defendantId, "Tim", null, "Karke", getDate("2015-09-08"), "UK", address);
        memento.setHearing(createHearing(prosecutionCaseId, hearingId, defendant));


        DefendantDelegate defendantDelegate = new DefendantDelegate(memento);

        final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendantDetails = createUpdatedDefendantDetails(prosecutionCaseId, defendantId, "Tim", null, "Karke",
                null, "UK", address);
        defendantDelegate.handleDefendantDetailsUpdated(createDefendantDetailsUpdated(hearingId, updatedDefendantDetails));

        assertThat(defendantDelegate.getDefendantDetailsChanged().contains(defendantId), is(true));
    }

    @Test
    public void whenThereIsAChangeInDateOfBirthFromNullForDefendantHearingShouldListDefendantForDDCH() {

        HearingAggregateMomento memento = new HearingAggregateMomento();
        final Address address = Address.address().withAddress1("address11").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build();
        Defendant defendant = createIndividualDefendant(prosecutionCaseId, offenceId, defendantId, "Tim", null, "Karke", null, "UK", address);
        memento.setHearing(createHearing(prosecutionCaseId, hearingId, defendant));

        DefendantDelegate defendantDelegate = new DefendantDelegate(memento);
        final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendantDetails = createUpdatedDefendantDetails(prosecutionCaseId, defendantId, "Tim", null, "Karke",
                getDate("1965-09-08"), "UK", address);
        defendantDelegate.handleDefendantDetailsUpdated(createDefendantDetailsUpdated(hearingId, updatedDefendantDetails));

        assertThat(defendantDelegate.getDefendantDetailsChanged().contains(defendantId), is(true));
    }

    @Test
    public void shouldNotListDefendantForDDCHWhenThereIsANoChangeForDefendantHearing() {

        HearingAggregateMomento memento = new HearingAggregateMomento();
        final Address address = Address.address().withAddress1("address11").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build();
        Defendant defendant = createIndividualDefendant(prosecutionCaseId, offenceId, defendantId, "Tim", null, "Karke", getDate("2015-09-08"), "UK", address);
        memento.setHearing(createHearing(prosecutionCaseId, hearingId, defendant));


        DefendantDelegate defendantDelegate = new DefendantDelegate(memento);


        final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendantDetails = createUpdatedDefendantDetails(prosecutionCaseId, defendantId, "Tim", null, "Karke",
                getDate("2015-09-08"), "UK", address);
        defendantDelegate.handleDefendantDetailsUpdated(createDefendantDetailsUpdated(hearingId, updatedDefendantDetails));

        assertThat(defendantDelegate.getDefendantDetailsChanged().contains(defendantId), is(false));
    }

    @Test
    public void shouldListDefendantForDDCHWhenThereIsAChangeInOrganisationNameForDefendantHearing() {

        HearingAggregateMomento memento = new HearingAggregateMomento();
        final Address address = Address.address().withAddress1("address11").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build();
        Defendant defendant = createOrganisationDefendant(prosecutionCaseId, offenceId, defendantId, "Versu Ltd", address);
        memento.setHearing(createHearing(prosecutionCaseId, hearingId, defendant));


        DefendantDelegate defendantDelegate = new DefendantDelegate(memento);


        final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendantDetails = createUpdatedDefendantDetailsForOrganisation(prosecutionCaseId, defendantId, "Tim Ltd", address);
        defendantDelegate.handleDefendantDetailsUpdated(createDefendantDetailsUpdated(hearingId, updatedDefendantDetails));

        assertThat(defendantDelegate.getDefendantDetailsChanged().contains(defendantId), is(true));
    }

    @Theory
    public void shouldListDefendantForDDCHWhenThereIsAChangeInOrganisationAddressForDefendantHearing(final Address changedAddress) {

        HearingAggregateMomento memento = new HearingAggregateMomento();
        final Address address = Address.address().withAddress1("address1").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build();
        Defendant defendant = createOrganisationDefendant(prosecutionCaseId, offenceId, defendantId, "Versu Ltd", address);
        memento.setHearing(createHearing(prosecutionCaseId, hearingId, defendant));


        DefendantDelegate defendantDelegate = new DefendantDelegate(memento);


        final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendantDetails = createUpdatedDefendantDetailsForOrganisation(prosecutionCaseId, defendantId, "Versu Ltd", changedAddress);
        defendantDelegate.handleDefendantDetailsUpdated(createDefendantDetailsUpdated(hearingId, updatedDefendantDetails));

        assertThat(defendantDelegate.getDefendantDetailsChanged().contains(defendantId), is(true));
    }

    @Theory
    public void shouldNotListDefendantForDDCHWhenThereIsAnoChangeInOrganisationDetailsForDefendantHearing(final Address changedAddress) {

        HearingAggregateMomento memento = new HearingAggregateMomento();
        final Address address = Address.address().withAddress1("address11").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build();
        Defendant defendant = createOrganisationDefendant(prosecutionCaseId, offenceId, defendantId, "Versu Ltd", address);
        memento.setHearing(createHearing(prosecutionCaseId, hearingId, defendant));


        DefendantDelegate defendantDelegate = new DefendantDelegate(memento);


        final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendantDetails = createUpdatedDefendantDetailsForOrganisation(prosecutionCaseId, defendantId, "Versu Ltd", address);
        defendantDelegate.handleDefendantDetailsUpdated(createDefendantDetailsUpdated(hearingId, updatedDefendantDetails));

        assertThat(defendantDelegate.getDefendantDetailsChanged().contains(defendantId), is(false));
    }


    @Test
    public void shouldHearingAggregateHaveDefenceOrganisationWhenThereIsAnDefenceAssociate() {

        HearingAggregateMomento memento = new HearingAggregateMomento();
        final Address address = Address.address().withAddress1("address11").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build();
        Defendant defendant = createIndividualDefendant(prosecutionCaseId, offenceId, defendantId, "Tim", null, "Karke", getDate("2015-09-08"), "UK", address);
        memento.setHearing(createHearing(prosecutionCaseId, hearingId, defendant));


        DefendantDelegate defendantDelegate = new DefendantDelegate(memento);


        final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendantDetails = createUpdatedDefendantDetailsWithDefenceOrganisation(prosecutionCaseId, defendantId, "Tim", null, "Karke",
                getDate("2015-09-08"), "UK", address);
        defendantDelegate.handleDefendantDetailsUpdated(createDefendantDetailsUpdated(hearingId, updatedDefendantDetails));

        assertThat(defendantDelegate.getMomento().getHearing().getProsecutionCases().get(0).getDefendants().get(0).getAssociatedDefenceOrganisation(), notNullValue());
    }

    @Test
    public void shouldSetMasterDefendantIdFromAggregateWhenMasterDefendantIdDoesNotPresentInTheEvent() {

        HearingAggregateMomento memento = new HearingAggregateMomento();
        final Address address = Address.address().withAddress1("address11").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build();
        Defendant defendant = createIndividualDefendant(prosecutionCaseId, offenceId, defendantId, "Tim", null, "Karke", getDate("2015-09-08"), "UK", address);
        memento.setHearing(createHearing(prosecutionCaseId, hearingId, defendant));
        DefendantDelegate defendantDelegate = new DefendantDelegate(memento);

        final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendantDetails = createUpdatedDefendantDetailsWithDefenceOrganisation(prosecutionCaseId, defendantId, "Tim", null, "Karke",
                getDate("2015-09-08"), "UK", address);

        defendantDelegate.handleDefendantDetailsUpdated(createDefendantDetailsUpdated(hearingId, updatedDefendantDetails));

        Defendant defendantInAggregate = defendantDelegate.getMomento().getHearing().getProsecutionCases().get(0).getDefendants().get(0);
        assertThat(defendantInAggregate.getMasterDefendantId(), is(defendantInAggregate.getId()));

    }

    @Test
    public void shouldSetMasterDefendantIdFromEventWhenMasterDefendantIdPresentsInTheEvent() {

        HearingAggregateMomento memento = new HearingAggregateMomento();
        final Address address = Address.address().withAddress1("address11").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build();
        Defendant defendant = createIndividualDefendant(prosecutionCaseId, offenceId, defendantId, "Tim", null, "Karke", getDate("2015-09-08"), "UK", address);
        memento.setHearing(createHearing(prosecutionCaseId, hearingId, defendant));
        DefendantDelegate defendantDelegate = new DefendantDelegate(memento);

        final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendantDetails = createUpdatedDefendantDetailsWithDefenceOrganisation(prosecutionCaseId, defendantId, "Tim", null, "Karke",
                getDate("2015-09-08"), "UK", address);
        updatedDefendantDetails.setMasterDefendantId(randomUUID());

        defendantDelegate.handleDefendantDetailsUpdated(createDefendantDetailsUpdated(hearingId, updatedDefendantDetails));

        assertThat(defendantDelegate.getMomento().getHearing().getProsecutionCases().get(0).getDefendants().get(0).getMasterDefendantId(), is(updatedDefendantDetails.getMasterDefendantId()));

    }

    private uk.gov.moj.cpp.hearing.command.defendant.Defendant createUpdatedDefendantDetailsForOrganisation(final UUID prosecutionCaseId, final UUID defendantId, final String organisationName, final Address address) {

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
        PersonDefendant personDefendant = new PersonDefendant(null, null, null, null, null, null,
                null, null, null, null, null, null, person, null);
        defendant.setId(defendantId);
        defendant.setPersonDefendant(personDefendant);
        defendant.setProsecutionCaseId(prosecutionCaseId);
        return defendant;

    }


    private uk.gov.moj.cpp.hearing.command.defendant.Defendant createUpdatedDefendantDetailsWithDefenceOrganisation(final UUID prosecutionCaseId, final UUID defendantId, final String firstName, final String middleName, final String lastName, final LocalDate dateOfBirth, final String nationality, final Address address) {

        uk.gov.moj.cpp.hearing.command.defendant.Defendant defendant = new uk.gov.moj.cpp.hearing.command.defendant.Defendant();

        Person person = Person.person()
                .withFirstName(firstName)
                .withMiddleName(middleName)
                .withLastName(lastName)
                .withNationalityCode(nationality)
                .withDateOfBirth(dateOfBirth)
                .withAddress(address)
                .build();
        PersonDefendant personDefendant = new PersonDefendant(null, null, null, null, null, null,
                null, null, null, null, null, null, person, null);
        defendant.setId(defendantId);
        defendant.setPersonDefendant(personDefendant);
        defendant.setProsecutionCaseId(prosecutionCaseId);

        defendant.setAssociatedDefenceOrganisation(AssociatedDefenceOrganisation.associatedDefenceOrganisation()
                .withApplicationReference(randomUUID().toString())
                .withDefenceOrganisation(DefenceOrganisation.defenceOrganisation()
                        .withLaaContractNumber("laaContactNumber")
                        .build()).build());
        return defendant;

    }

    private DefendantDetailsUpdated createDefendantDetailsUpdated(final UUID hearingId, uk.gov.moj.cpp.hearing.command.defendant.Defendant defendant) {
        return DefendantDetailsUpdated.defendantDetailsUpdated().setDefendant(defendant).setHearingId(hearingId);
    }

    private Hearing createHearing(final UUID prosecutionCaseId, final UUID hearingId, final Defendant defendant) {
        final ProsecutionCase prosecutionCase = prosecutionCase()
                .withId(prosecutionCaseId)
                .withDefendants(asList(defendant))
                .build();

        return hearing()
                .withId(hearingId)
                .withProsecutionCases(asList(prosecutionCase))
                .build();
    }

    private Defendant createIndividualDefendant(final UUID prosecutionCaseId, final UUID offenceId, final UUID defendantId, final String firstName, final String middleName, final String lastName, final LocalDate dateOfBirth, final String nationality, final Address address) {
        return defendant()
                .withId(defendantId)
                .withMasterDefendantId(defendantId)
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(date, formatter);
    }

}