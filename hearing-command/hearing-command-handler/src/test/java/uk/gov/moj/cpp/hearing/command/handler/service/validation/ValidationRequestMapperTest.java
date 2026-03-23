package uk.gov.moj.cpp.hearing.command.handler.service.validation;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.moj.cpp.hearing.command.result.ShareDaysResultsCommand;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandPrompt;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLineV2;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ValidationRequestMapperTest {

    private final ValidationRequestMapper mapper = new ValidationRequestMapper();

    @Test
    void shouldMapHearingIdAndHearingDayFromCommand() {
        final UUID hearingId = randomUUID();
        final LocalDate hearingDay = LocalDate.of(2026, 3, 16);

        final ShareDaysResultsCommand command = buildCommand(hearingId, hearingDay, emptyList());

        final Hearing hearing = Hearing.hearing().build();

        final ValidationRequest request = mapper.toValidationRequest(command, hearing);

        assertThat(request.getHearingId(), is(hearingId.toString()));
        assertThat(request.getHearingDay(), is(hearingDay));
    }

    @Test
    void shouldMapCourtTypeFromHearingJurisdictionType() {
        final ShareDaysResultsCommand command = buildCommand(randomUUID(), LocalDate.now(), emptyList());

        final Hearing hearing = Hearing.hearing()
                .withJurisdictionType(JurisdictionType.MAGISTRATES)
                .build();

        final ValidationRequest request = mapper.toValidationRequest(command, hearing);

        assertThat(request.getCourtType(), is("MAGISTRATES"));
    }

    @Test
    void shouldMapDefendantsFromHearingProsecutionCases() {
        final UUID defendantId = randomUUID();

        final Defendant defendant = Defendant.defendant()
                .withId(defendantId)
                .build();

        final ProsecutionCase prosecutionCase = ProsecutionCase.prosecutionCase()
                .withDefendants(List.of(defendant))
                .build();

        final Hearing hearing = Hearing.hearing()
                .withProsecutionCases(List.of(prosecutionCase))
                .build();

        final ShareDaysResultsCommand command = buildCommand(randomUUID(), LocalDate.now(), emptyList());

        final ValidationRequest request = mapper.toValidationRequest(command, hearing);

        assertThat(request.getDefendants(), hasSize(1));
        assertThat(request.getDefendants().get(0).getId(), is(defendantId.toString()));
    }

    @Test
    void shouldMapDefendantFirstNameAndLastNameFromPersonDetails() {
        final UUID defendantId = randomUUID();

        final Person person = Person.person()
                .withFirstName("John")
                .withLastName("Smith")
                .build();

        final PersonDefendant personDefendant = PersonDefendant.personDefendant()
                .withPersonDetails(person)
                .build();

        final Defendant defendant = Defendant.defendant()
                .withId(defendantId)
                .withPersonDefendant(personDefendant)
                .build();

        final ProsecutionCase prosecutionCase = ProsecutionCase.prosecutionCase()
                .withDefendants(List.of(defendant))
                .build();

        final Hearing hearing = Hearing.hearing()
                .withProsecutionCases(List.of(prosecutionCase))
                .build();

        final ShareDaysResultsCommand command = buildCommand(randomUUID(), LocalDate.now(), emptyList());

        final ValidationRequest request = mapper.toValidationRequest(command, hearing);

        assertThat(request.getDefendants(), hasSize(1));
        assertThat(request.getDefendants().get(0).getFirstName(), is("John"));
        assertThat(request.getDefendants().get(0).getLastName(), is("Smith"));
    }

    @Test
    void shouldHandleNullPersonDefendantGracefully() {
        final UUID defendantId = randomUUID();

        final Defendant defendant = Defendant.defendant()
                .withId(defendantId)
                .build();

        final ProsecutionCase prosecutionCase = ProsecutionCase.prosecutionCase()
                .withDefendants(List.of(defendant))
                .build();

        final Hearing hearing = Hearing.hearing()
                .withProsecutionCases(List.of(prosecutionCase))
                .build();

        final ShareDaysResultsCommand command = buildCommand(randomUUID(), LocalDate.now(), emptyList());

        final ValidationRequest request = mapper.toValidationRequest(command, hearing);

        assertThat(request.getDefendants(), hasSize(1));
        assertThat(request.getDefendants().get(0).getFirstName(), is(nullValue()));
        assertThat(request.getDefendants().get(0).getLastName(), is(nullValue()));
    }

    @Test
    void shouldMapOffencesFromHearing() {
        final UUID offenceId = randomUUID();

        final Offence offence = Offence.offence()
                .withId(offenceId)
                .withOffenceCode("TH68001")
                .withOffenceTitle("Theft")
                .withOrderIndex(1)
                .build();

        final Defendant defendant = Defendant.defendant()
                .withId(randomUUID())
                .withOffences(List.of(offence))
                .build();

        final ProsecutionCase prosecutionCase = ProsecutionCase.prosecutionCase()
                .withDefendants(List.of(defendant))
                .build();

        final Hearing hearing = Hearing.hearing()
                .withProsecutionCases(List.of(prosecutionCase))
                .build();

        final ShareDaysResultsCommand command = buildCommand(randomUUID(), LocalDate.now(), emptyList());

        final ValidationRequest request = mapper.toValidationRequest(command, hearing);

        assertThat(request.getOffences(), hasSize(1));
        assertThat(request.getOffences().get(0).getId(), is(offenceId.toString()));
        assertThat(request.getOffences().get(0).getOffenceCode(), is("TH68001"));
        assertThat(request.getOffences().get(0).getOffenceTitle(), is("Theft"));
        assertThat(request.getOffences().get(0).getOrderIndex(), is(1));
    }

    @Test
    void shouldMapCaseUrnFromProsecutionCaseIdentifier() {
        final UUID offenceId = randomUUID();
        final String caseUrn = "32AH9105826";

        final Offence offence = Offence.offence()
                .withId(offenceId)
                .withOffenceCode("TH68001")
                .withOffenceTitle("Theft")
                .withOrderIndex(1)
                .build();

        final Defendant defendant = Defendant.defendant()
                .withId(randomUUID())
                .withOffences(List.of(offence))
                .build();

        final ProsecutionCase prosecutionCase = ProsecutionCase.prosecutionCase()
                .withProsecutionCaseIdentifier(ProsecutionCaseIdentifier.prosecutionCaseIdentifier()
                        .withCaseURN(caseUrn)
                        .build())
                .withDefendants(List.of(defendant))
                .build();

        final Hearing hearing = Hearing.hearing()
                .withProsecutionCases(List.of(prosecutionCase))
                .build();

        final ShareDaysResultsCommand command = buildCommand(randomUUID(), LocalDate.now(), emptyList());

        final ValidationRequest request = mapper.toValidationRequest(command, hearing);

        assertThat(request.getOffences(), hasSize(1));
        assertThat(request.getOffences().get(0).getCaseUrn(), is(caseUrn));
    }

    @Test
    void shouldMapResultLinesFromCommand() {
        final UUID resultLineId = randomUUID();
        final UUID offenceId = randomUUID();
        final UUID defendantId = randomUUID();

        final SharedResultsCommandResultLineV2 resultLine = SharedResultsCommandResultLineV2
                .sharedResultsCommandResultLine()
                .withResultLineId(resultLineId)
                .withShortCode("IMP")
                .withResultLabel("Imprisonment")
                .withDefendantId(defendantId)
                .withOffenceId(offenceId)
                .build();

        final ShareDaysResultsCommand command = buildCommand(randomUUID(), LocalDate.now(), List.of(resultLine));

        final Hearing hearing = Hearing.hearing().build();

        final ValidationRequest request = mapper.toValidationRequest(command, hearing);

        assertThat(request.getResultLines(), hasSize(1));
        assertThat(request.getResultLines().get(0).getId(), is(resultLineId.toString()));
        assertThat(request.getResultLines().get(0).getShortCode(), is("IMP"));
        assertThat(request.getResultLines().get(0).getLabel(), is("Imprisonment"));
        assertThat(request.getResultLines().get(0).getDefendantId(), is(defendantId.toString()));
        assertThat(request.getResultLines().get(0).getOffenceId(), is(offenceId.toString()));
    }

    @Test
    void shouldHandleNullProsecutionCasesGracefully() {
        final ShareDaysResultsCommand command = buildCommand(randomUUID(), LocalDate.now(), emptyList());

        final Hearing hearing = Hearing.hearing().build();

        final ValidationRequest request = mapper.toValidationRequest(command, hearing);

        assertThat(request.getDefendants(), is(empty()));
        assertThat(request.getOffences(), is(empty()));
    }

    @Test
    void shouldHandleNullJurisdictionTypeGracefully() {
        final ShareDaysResultsCommand command = buildCommand(randomUUID(), LocalDate.now(), emptyList());

        final Hearing hearing = Hearing.hearing().build();

        final ValidationRequest request = mapper.toValidationRequest(command, hearing);

        assertThat(request.getCourtType(), is(nullValue()));
    }

    @Test
    void shouldMapIsConcurrentFromPrompts() {
        final SharedResultsCommandPrompt concurrentPrompt = new SharedResultsCommandPrompt(
                randomUUID(), "Concurrent", null, "true", null, null, "concurrent");

        final SharedResultsCommandResultLineV2 resultLine = SharedResultsCommandResultLineV2
                .sharedResultsCommandResultLine()
                .withResultLineId(randomUUID())
                .withShortCode("IMP")
                .withDefendantId(randomUUID())
                .withOffenceId(randomUUID())
                .withPrompts(List.of(concurrentPrompt))
                .build();

        final ShareDaysResultsCommand command = buildCommand(randomUUID(), LocalDate.now(), List.of(resultLine));
        final ValidationRequest request = mapper.toValidationRequest(command, Hearing.hearing().build());

        assertThat(request.getResultLines().get(0).getIsConcurrent(), is(true));
        assertThat(request.getResultLines().get(0).getConsecutiveToOffence(), is(nullValue()));
    }

    @Test
    void shouldMapConsecutiveToOffenceFromPrompts() {
        final String consecutiveOffenceId = randomUUID().toString();
        final SharedResultsCommandPrompt consecutivePrompt = new SharedResultsCommandPrompt(
                randomUUID(), "Consecutive to", null, consecutiveOffenceId, null, null, "consecutiveToOffenceNumber");

        final SharedResultsCommandResultLineV2 resultLine = SharedResultsCommandResultLineV2
                .sharedResultsCommandResultLine()
                .withResultLineId(randomUUID())
                .withShortCode("IMP")
                .withDefendantId(randomUUID())
                .withOffenceId(randomUUID())
                .withPrompts(List.of(consecutivePrompt))
                .build();

        final ShareDaysResultsCommand command = buildCommand(randomUUID(), LocalDate.now(), List.of(resultLine));
        final ValidationRequest request = mapper.toValidationRequest(command, Hearing.hearing().build());

        assertThat(request.getResultLines().get(0).getIsConcurrent(), is(nullValue()));
        assertThat(request.getResultLines().get(0).getConsecutiveToOffence(), is(consecutiveOffenceId));
    }

    @Test
    void shouldMapBothConcurrentAndConsecutiveFromPrompts() {
        final String consecutiveOffenceId = randomUUID().toString();
        final SharedResultsCommandPrompt concurrentPrompt = new SharedResultsCommandPrompt(
                randomUUID(), "Concurrent", null, "false", null, null, "concurrent");
        final SharedResultsCommandPrompt consecutivePrompt = new SharedResultsCommandPrompt(
                randomUUID(), "Consecutive to", null, consecutiveOffenceId, null, null, "consecutiveToOffenceNumber");

        final SharedResultsCommandResultLineV2 resultLine = SharedResultsCommandResultLineV2
                .sharedResultsCommandResultLine()
                .withResultLineId(randomUUID())
                .withShortCode("IMP")
                .withDefendantId(randomUUID())
                .withOffenceId(randomUUID())
                .withPrompts(List.of(concurrentPrompt, consecutivePrompt))
                .build();

        final ShareDaysResultsCommand command = buildCommand(randomUUID(), LocalDate.now(), List.of(resultLine));
        final ValidationRequest request = mapper.toValidationRequest(command, Hearing.hearing().build());

        assertThat(request.getResultLines().get(0).getIsConcurrent(), is(false));
        assertThat(request.getResultLines().get(0).getConsecutiveToOffence(), is(consecutiveOffenceId));
    }

    @Test
    void shouldHandleNullPromptsGracefully() {
        final SharedResultsCommandResultLineV2 resultLine = SharedResultsCommandResultLineV2
                .sharedResultsCommandResultLine()
                .withResultLineId(randomUUID())
                .withShortCode("IMP")
                .withDefendantId(randomUUID())
                .withOffenceId(randomUUID())
                .build();

        final ShareDaysResultsCommand command = buildCommand(randomUUID(), LocalDate.now(), List.of(resultLine));
        final ValidationRequest request = mapper.toValidationRequest(command, Hearing.hearing().build());

        assertThat(request.getResultLines().get(0).getIsConcurrent(), is(nullValue()));
        assertThat(request.getResultLines().get(0).getConsecutiveToOffence(), is(nullValue()));
    }

    @Test
    void shouldHandleEmptyPromptsGracefully() {
        final SharedResultsCommandResultLineV2 resultLine = SharedResultsCommandResultLineV2
                .sharedResultsCommandResultLine()
                .withResultLineId(randomUUID())
                .withShortCode("IMP")
                .withDefendantId(randomUUID())
                .withOffenceId(randomUUID())
                .withPrompts(emptyList())
                .build();

        final ShareDaysResultsCommand command = buildCommand(randomUUID(), LocalDate.now(), List.of(resultLine));
        final ValidationRequest request = mapper.toValidationRequest(command, Hearing.hearing().build());

        assertThat(request.getResultLines().get(0).getIsConcurrent(), is(nullValue()));
        assertThat(request.getResultLines().get(0).getConsecutiveToOffence(), is(nullValue()));
    }

    private ShareDaysResultsCommand buildCommand(final UUID hearingId, final LocalDate hearingDay,
                                                  final List<SharedResultsCommandResultLineV2> resultLines) {
        final ShareDaysResultsCommand command = ShareDaysResultsCommand.shareResultsCommand()
                .setHearingId(hearingId)
                .setResultLines(resultLines);
        command.setHearingDay(hearingDay);
        return command;
    }
}
