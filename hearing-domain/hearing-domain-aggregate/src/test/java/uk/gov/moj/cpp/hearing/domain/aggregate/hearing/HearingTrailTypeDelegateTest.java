package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.HearingTrialVacated;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

public class HearingTrailTypeDelegateTest {

    private final HearingAggregateMomento momento = new HearingAggregateMomento();
    private final HearingDelegate hearingDelegate = new HearingDelegate(momento);
    private final HearingTrialTypeDelegate hearingTrialTypeDelegate = new HearingTrialTypeDelegate(momento);

    @Test
    public void shouldGetHasInterpreterTrueForDefendantWithLanguageNeeds() {
        // Given
        final UUID hearingId = UUID.randomUUID();
        final UUID vacatedTrialReasonId = UUID.randomUUID();
        final CommandHelpers.InitiateHearingCommandHelper hearing = h(standardInitiateHearingTemplate());
        hearingDelegate.handleHearingInitiated(new HearingInitiated(hearing.getHearing()));

        final Person personWithLanguageNeed = getPerson("FRENCH");
        final Person personWithOutLanguageNeed = getPerson("ENGLISH");
        final PersonDefendant personDefendantWithLanguageNeed = PersonDefendant.personDefendant().withPersonDetails(personWithLanguageNeed).build();
        final PersonDefendant personDefendantWithOutLanguageNeed = PersonDefendant.personDefendant().withPersonDetails(personWithOutLanguageNeed).build();
        setProsecutionCasesForMomento(personDefendantWithLanguageNeed, personDefendantWithOutLanguageNeed);
        final HearingTrialVacated hearingTrialVacated = new HearingTrialVacated(hearingId, vacatedTrialReasonId, "A", "Vacated", "Vacated Trial", hearing.getCourtCentre().getId(), false, null, new ArrayList<>(), new ArrayList<>(), null);

        // when
        hearingTrialTypeDelegate.setTrialType(hearingTrialVacated);

        // Then
        assertThat(hearingTrialVacated.getHasInterpreter(), is(true));
    }

    @Test
    public void shouldGetHasInterpreterFalseForDefendantWithOutLanguageNeeds() {
        // Given
        final UUID hearingId = UUID.randomUUID();
        final UUID vacatedTrialReasonId = UUID.randomUUID();
        final CommandHelpers.InitiateHearingCommandHelper hearing = h(standardInitiateHearingTemplate());
        hearingDelegate.handleHearingInitiated(new HearingInitiated(hearing.getHearing()));

        final Person personWithOutLanguageNeed = getPerson(null);
        final PersonDefendant personDefendantWithOutLanguageNeed = PersonDefendant.personDefendant().withPersonDetails(personWithOutLanguageNeed).build();
        setProsecutionCasesForMomento(personDefendantWithOutLanguageNeed, personDefendantWithOutLanguageNeed);
        final HearingTrialVacated hearingTrialVacated = new HearingTrialVacated(hearingId, vacatedTrialReasonId, "A", "Vacated", "Vacated Trial", hearing.getCourtCentre().getId(), false, null, new ArrayList<>(), new ArrayList<>(), null);

        // when
        hearingTrialTypeDelegate.setTrialType(hearingTrialVacated);

        // Then
        assertThat(hearingTrialVacated.getHasInterpreter(), is(false));
    }

    @Test
    public void shouldHandleHearingTrialVacated() {
        final UUID hearingId = UUID.randomUUID();
        final UUID vacatedTrialReasonId = UUID.randomUUID();
        final String code = "A";
        final String type = "Vacated";
        final String description = "Vacated Trial";
        final CommandHelpers.InitiateHearingCommandHelper hearing = h(standardInitiateHearingTemplate());
        hearingDelegate.handleHearingInitiated(new HearingInitiated(hearing.getHearing()));

        final HearingTrialVacated hearingTrialVacatedIn = new HearingTrialVacated(hearingId, vacatedTrialReasonId, code, type, description, hearing.getCourtCentre().getId(), false, null, new ArrayList<>(), new ArrayList<>(), null);

        final List<Object> eventStream = hearingTrialTypeDelegate.setTrialType(hearingTrialVacatedIn).collect(toList());

        assertThat(eventStream.size(), is(1));

        final HearingTrialVacated hearingTrialVacated = (HearingTrialVacated) eventStream.get(0);
        assertThat(hearingTrialVacated.getHearingId(), is(hearingId));
        assertThat(hearingTrialVacated.getVacatedTrialReasonId(), is(vacatedTrialReasonId));
        assertThat(hearingTrialVacated.getCode(), is(code));
        assertThat(hearingTrialVacated.getType(), is(type));
        assertThat(hearingTrialVacated.getDescription(), is(description));
        assertThat(hearingTrialVacated.getCourtCentreId(), is(hearing.getCourtCentre().getId()));
    }


    private LocalDate getDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(date, formatter);
    }

    private Person getPerson(final String language) {
        final LocalDate dateOfBirth = getDate("2015-09-08");
        final Address address = Address.address().withAddress1("address1").withAddress2("address2").withAddress3("address3").withAddress4("address4").withAddress5("address5").withPostcode("xyz").build();

        return Person.person()
                .withFirstName("firstName")
                .withMiddleName("middleName")
                .withLastName("lastName")
                .withNationalityCode("nationality")
                .withDateOfBirth(dateOfBirth)
                .withAddress(address)
                .withInterpreterLanguageNeeds(language)
                .build();
    }

    private void setProsecutionCasesForMomento(final PersonDefendant personDefendant1, final PersonDefendant personDefendant2) {
        final UUID prosecutionCaseId1 = UUID.randomUUID();
        final UUID prosecutionCaseId2 = UUID.randomUUID();
        final UUID defendantId1 = UUID.randomUUID();
        final UUID defendantId2 = UUID.randomUUID();
        final UUID defendantId3 = UUID.randomUUID();
        final UUID defendantId4 = UUID.randomUUID();
        momento.getHearing().setProsecutionCases(
                asList(
                        ProsecutionCase.prosecutionCase()
                                .withId(prosecutionCaseId1)
                                .withProsecutionCaseIdentifier(ProsecutionCaseIdentifier.prosecutionCaseIdentifier()
                                        .withCaseURN("caseURN")
                                        .withProsecutionAuthorityId(randomUUID())
                                        .withProsecutionAuthorityCode("ABC")
                                        .build())
                                .withDefendants(
                                        asList(
                                                Defendant.defendant()
                                                        .withId(defendantId1)
                                                        .withPersonDefendant(personDefendant1)
                                                        .build(),
                                                Defendant.defendant()
                                                        .withId(defendantId2)
                                                        .withPersonDefendant(personDefendant1)
                                                        .build()
                                        )
                                )
                                .build(),
                        ProsecutionCase.prosecutionCase()
                                .withId(prosecutionCaseId2)
                                .withProsecutionCaseIdentifier(ProsecutionCaseIdentifier.prosecutionCaseIdentifier()
                                        .withCaseURN("caseURN")
                                        .withProsecutionAuthorityId(randomUUID())
                                        .withProsecutionAuthorityCode("ABC")
                                        .build())
                                .withDefendants(
                                        asList(
                                                Defendant.defendant()
                                                        .withId(defendantId3)
                                                        .withPersonDefendant(personDefendant1)
                                                        .build(),
                                                Defendant.defendant()
                                                        .withId(defendantId4)
                                                        .withPersonDefendant(personDefendant2)
                                                        .build()
                                        )
                                )
                                .build()));
    }
}