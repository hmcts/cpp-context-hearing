package uk.gov.moj.cpp.hearing.command.handler.service.validation;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.CourtOrder;
import uk.gov.justice.core.courts.CourtOrderOffence;
import uk.gov.justice.core.courts.CustodyTimeLimit;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.DefendantCase;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.command.result.ShareDaysResultsCommand;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandPrompt;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLineV2;
import uk.gov.moj.cpp.hearing.domain.common.resultsvalidator.DraftValidationRequest;
import uk.gov.moj.cpp.hearing.domain.common.resultsvalidator.ResultLineDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class ValidationRequestMapperTest {

    private final ValidationRequestMapper mapper = new ValidationRequestMapper();
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Test
    void shouldMapHearingIdAndHearingDayFromCommand() {
        final UUID hearingId = randomUUID();
        final LocalDate hearingDay = LocalDate.of(2026, 3, 16);

        final ShareDaysResultsCommand command = buildCommand(hearingId, hearingDay, emptyList());

        final Hearing hearing = Hearing.hearing().build();

        final DraftValidationRequest request = mapper.toValidationRequest(command, hearing);

        assertThat(request.getHearingId(), is(hearingId.toString()));
        assertThat(request.getHearingDay(), is(hearingDay));
    }

    @Test
    void shouldMapCourtTypeFromHearingJurisdictionType() {
        final ShareDaysResultsCommand command = buildCommand(randomUUID(), LocalDate.now(), emptyList());

        final Hearing hearing = Hearing.hearing()
                .withJurisdictionType(JurisdictionType.MAGISTRATES)
                .build();

        final DraftValidationRequest request = mapper.toValidationRequest(command, hearing);

        assertThat(request.getCourtType(), is(DraftValidationRequest.CourtTypeEnum.MAGISTRATES));
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

        final DraftValidationRequest request = mapper.toValidationRequest(command, hearing);

        assertThat(request.getDefendants(), hasSize(1));
        assertThat(request.getDefendants().get(0).getDefendantId(), is(defendantId.toString()));
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

        final DraftValidationRequest request = mapper.toValidationRequest(command, hearing);

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

        final DraftValidationRequest request = mapper.toValidationRequest(command, hearing);

        assertThat(request.getDefendants(), hasSize(1));
        assertThat(request.getDefendants().get(0).getFirstName(), is(nullValue()));
        assertThat(request.getDefendants().get(0).getLastName(), is(nullValue()));
        assertThat(request.getDefendants().get(0).getDateOfBirth(), is(nullValue()));
    }

    @Test
    void shouldMapDefendantDateOfBirthFromPersonDetails() throws Exception {
        final UUID defendantId = randomUUID();
        final LocalDate dateOfBirth = LocalDate.of(1990, 5, 20);

        final Person person = Person.person()
                .withDateOfBirth(dateOfBirth)
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

        final DraftValidationRequest request = mapper.toValidationRequest(command, hearing);

        assertThat(request.getDefendants(), hasSize(1));
        assertThat(request.getDefendants().get(0).getDateOfBirth(), is(dateOfBirth));

        // The validator spec declares dateOfBirth as format: date, so the outbound JSON
        // must serialise it as an ISO yyyy-MM-dd string rather than an epoch/array form.
        final String json = objectMapper.writeValueAsString(request);
        assertThat(json, containsString("\"dateOfBirth\":\"1990-05-20\""));
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

        final DraftValidationRequest request = mapper.toValidationRequest(command, hearing);

        assertThat(request.getOffences(), hasSize(1));
        assertThat(request.getOffences().get(0).getOffenceId(), is(offenceId.toString()));
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

        final DraftValidationRequest request = mapper.toValidationRequest(command, hearing);

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

        final DraftValidationRequest request = mapper.toValidationRequest(command, hearing);

        assertThat(request.getResultLines(), hasSize(1));
        assertThat(request.getResultLines().get(0).getResultLineId(), is(resultLineId.toString()));
        assertThat(request.getResultLines().get(0).getShortCode(), is("IMP"));
        assertThat(request.getResultLines().get(0).getLabel(), is("Imprisonment"));
        assertThat(request.getResultLines().get(0).getDefendantId(), is(defendantId.toString()));
        assertThat(request.getResultLines().get(0).getOffenceId(), is(offenceId.toString()));
    }

    @Test
    void shouldMapCategoryFromResultLine() {
        final SharedResultsCommandResultLineV2 resultLine = SharedResultsCommandResultLineV2
                .sharedResultsCommandResultLine()
                .withResultLineId(randomUUID())
                .withShortCode("IMP")
                .withDefendantId(randomUUID())
                .withOffenceId(randomUUID())
                .withCategory("F")
                .build();

        final ShareDaysResultsCommand command = buildCommand(randomUUID(), LocalDate.now(), List.of(resultLine));
        final DraftValidationRequest request = mapper.toValidationRequest(command, Hearing.hearing().build());

        assertThat(request.getResultLines().get(0).getCategory(), is(ResultLineDto.CategoryEnum.F));
    }

    @Test
    void shouldMapNullCategoryFromResultLine() {
        final SharedResultsCommandResultLineV2 resultLine = SharedResultsCommandResultLineV2
                .sharedResultsCommandResultLine()
                .withResultLineId(randomUUID())
                .withShortCode("IMP")
                .withDefendantId(randomUUID())
                .withOffenceId(randomUUID())
                .build();

        final ShareDaysResultsCommand command = buildCommand(randomUUID(), LocalDate.now(), List.of(resultLine));
        final DraftValidationRequest request = mapper.toValidationRequest(command, Hearing.hearing().build());

        assertThat(request.getResultLines().get(0).getCategory(), is(nullValue()));
    }

    @Test
    void shouldHandleNullProsecutionCasesGracefully() {
        final ShareDaysResultsCommand command = buildCommand(randomUUID(), LocalDate.now(), emptyList());

        final Hearing hearing = Hearing.hearing().build();

        final DraftValidationRequest request = mapper.toValidationRequest(command, hearing);

        assertThat(request.getDefendants(), is(empty()));
        assertThat(request.getOffences(), is(empty()));
    }

    @Test
    void shouldHandleNullJurisdictionTypeGracefully() {
        final ShareDaysResultsCommand command = buildCommand(randomUUID(), LocalDate.now(), emptyList());

        final Hearing hearing = Hearing.hearing().build();

        final DraftValidationRequest request = mapper.toValidationRequest(command, hearing);

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
        final DraftValidationRequest request = mapper.toValidationRequest(command, Hearing.hearing().build());

        assertThat(request.getResultLines().get(0).getIsConcurrent(), is(true));
        assertThat(request.getResultLines().get(0).getConsecutiveToOffence(), is(nullValue()));
        assertThat(request.getResultLines().get(0).getPrompts(), hasSize(1));
        assertThat(request.getResultLines().get(0).getPrompts().get(0).getPromptRef(), is("concurrent"));
        assertThat(request.getResultLines().get(0).getPrompts().get(0).getPromptValue(), is("true"));
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
        final DraftValidationRequest request = mapper.toValidationRequest(command, Hearing.hearing().build());

        assertThat(request.getResultLines().get(0).getIsConcurrent(), is(nullValue()));
        assertThat(request.getResultLines().get(0).getConsecutiveToOffence(), is(consecutiveOffenceId));
        assertThat(request.getResultLines().get(0).getPrompts(), hasSize(1));
        assertThat(request.getResultLines().get(0).getPrompts().get(0).getPromptRef(), is("consecutiveToOffenceNumber"));
        assertThat(request.getResultLines().get(0).getPrompts().get(0).getPromptValue(), is(consecutiveOffenceId));
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
        final DraftValidationRequest request = mapper.toValidationRequest(command, Hearing.hearing().build());

        assertThat(request.getResultLines().get(0).getIsConcurrent(), is(false));
        assertThat(request.getResultLines().get(0).getConsecutiveToOffence(), is(consecutiveOffenceId));
        assertThat(request.getResultLines().get(0).getPrompts(), hasSize(2));
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
        final DraftValidationRequest request = mapper.toValidationRequest(command, Hearing.hearing().build());

        assertThat(request.getResultLines().get(0).getIsConcurrent(), is(nullValue()));
        assertThat(request.getResultLines().get(0).getConsecutiveToOffence(), is(nullValue()));
        assertThat(request.getResultLines().get(0).getPrompts(), is(nullValue()));
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
        final DraftValidationRequest request = mapper.toValidationRequest(command, Hearing.hearing().build());

        assertThat(request.getResultLines().get(0).getIsConcurrent(), is(nullValue()));
        assertThat(request.getResultLines().get(0).getConsecutiveToOffence(), is(nullValue()));
        assertThat(request.getResultLines().get(0).getPrompts(), is(empty()));
    }

    @Test
    void shouldSetIsConvictedTrueWhenConvictionDateIsPresent() {
        final Offence offence = Offence.offence()
                .withId(randomUUID())
                .withConvictionDate(LocalDate.of(2025, 1, 10))
                .build();

        final Defendant defendant = Defendant.defendant()
                .withId(randomUUID())
                .withOffences(List.of(offence))
                .build();

        final Hearing hearing = Hearing.hearing()
                .withProsecutionCases(List.of(
                        ProsecutionCase.prosecutionCase()
                                .withDefendants(List.of(defendant))
                                .build()))
                .build();

        final DraftValidationRequest request = mapper.toValidationRequest(
                buildCommand(randomUUID(), LocalDate.now(), emptyList()), hearing);

        assertThat(request.getOffences().get(0).getIsConvicted(), is(true));
    }

    @Test
    void shouldSetIsConvictedFalseWhenConvictionDateIsAbsent() {
        final Offence offence = Offence.offence()
                .withId(randomUUID())
                .build();

        final Defendant defendant = Defendant.defendant()
                .withId(randomUUID())
                .withOffences(List.of(offence))
                .build();

        final Hearing hearing = Hearing.hearing()
                .withProsecutionCases(List.of(
                        ProsecutionCase.prosecutionCase()
                                .withDefendants(List.of(defendant))
                                .build()))
                .build();

        final DraftValidationRequest request = mapper.toValidationRequest(
                buildCommand(randomUUID(), LocalDate.now(), emptyList()), hearing);

        assertThat(request.getOffences().get(0).getIsConvicted(), is(false));
    }

    @Test
    void shouldSetHasExistingCtlRecordTrueWhenCustodyTimeLimitIsPresent() {
        final CustodyTimeLimit custodyTimeLimit = CustodyTimeLimit.custodyTimeLimit()
                .withTimeLimit(LocalDate.of(2026, 6, 1))
                .build();

        final Offence offence = Offence.offence()
                .withId(randomUUID())
                .withCustodyTimeLimit(custodyTimeLimit)
                .build();

        final Defendant defendant = Defendant.defendant()
                .withId(randomUUID())
                .withOffences(List.of(offence))
                .build();

        final Hearing hearing = Hearing.hearing()
                .withProsecutionCases(List.of(
                        ProsecutionCase.prosecutionCase()
                                .withDefendants(List.of(defendant))
                                .build()))
                .build();

        final DraftValidationRequest request = mapper.toValidationRequest(
                buildCommand(randomUUID(), LocalDate.now(), emptyList()), hearing);

        assertThat(request.getOffences().get(0).getHasExistingCtlRecord(), is(true));
    }

    @Test
    void shouldSetHasExistingCtlRecordFalseWhenTimeLimitIsNull() {
        final CustodyTimeLimit custodyTimeLimit = CustodyTimeLimit.custodyTimeLimit()
                .build();

        final Offence offence = Offence.offence()
                .withId(randomUUID())
                .withCustodyTimeLimit(custodyTimeLimit)
                .build();

        final Defendant defendant = Defendant.defendant()
                .withId(randomUUID())
                .withOffences(List.of(offence))
                .build();

        final Hearing hearing = Hearing.hearing()
                .withProsecutionCases(List.of(
                        ProsecutionCase.prosecutionCase()
                                .withDefendants(List.of(defendant))
                                .build()))
                .build();

        final DraftValidationRequest request = mapper.toValidationRequest(
                buildCommand(randomUUID(), LocalDate.now(), emptyList()), hearing);

        assertThat(request.getOffences().get(0).getHasExistingCtlRecord(), is(false));
    }

    @Test
    void shouldSetHasExistingCtlRecordFalseWhenCustodyTimeLimitIsAbsent() {
        final Offence offence = Offence.offence()
                .withId(randomUUID())
                .build();

        final Defendant defendant = Defendant.defendant()
                .withId(randomUUID())
                .withOffences(List.of(offence))
                .build();

        final Hearing hearing = Hearing.hearing()
                .withProsecutionCases(List.of(
                        ProsecutionCase.prosecutionCase()
                                .withDefendants(List.of(defendant))
                                .build()))
                .build();

        final DraftValidationRequest request = mapper.toValidationRequest(
                buildCommand(randomUUID(), LocalDate.now(), emptyList()), hearing);

        assertThat(request.getOffences().get(0).getHasExistingCtlRecord(), is(false));
    }

    @Test
    void shouldMapDefendantAndOffencesFromCourtApplicationWhenNoProsecutionCases() {
        final UUID defendantId = randomUUID();
        final UUID masterDefendantId = randomUUID();
        final UUID offenceId = randomUUID();
        final String caseUrn = "32AH9105826";

        final Hearing hearing = Hearing.hearing()
                .withCourtApplications(List.of(buildCourtApplication(defendantId, masterDefendantId, offenceId, caseUrn)))
                .build();

        final DraftValidationRequest request = mapper.toValidationRequest(
                buildCommand(randomUUID(), LocalDate.now(), emptyList()), hearing);

        assertThat(request.getDefendants(), hasSize(1));
        assertThat(request.getDefendants().get(0).getDefendantId(), is(defendantId.toString()));
        assertThat(request.getDefendants().get(0).getMasterDefendantId(), is(masterDefendantId.toString()));
        assertThat(request.getDefendants().get(0).getFirstName(), is("Jane"));
        assertThat(request.getDefendants().get(0).getLastName(), is("Doe"));
        assertThat(request.getDefendants().get(0).getDateOfBirth(), is(LocalDate.of(1945, 2, 18)));
        assertThat(request.getOffences(), hasSize(1));
        assertThat(request.getOffences().get(0).getOffenceId(), is(offenceId.toString()));
        assertThat(request.getOffences().get(0).getOffenceCode(), is("TH68001"));
        assertThat(request.getOffences().get(0).getCaseUrn(), is(caseUrn));
        assertThat(request.getOffences().get(0).getDefendantId(), is(defendantId.toString()));
    }

    @Test
    void shouldMapCourtApplicationWhenProsecutionCasesAreEmpty() {
        final Hearing hearing = Hearing.hearing()
                .withProsecutionCases(emptyList())
                .withCourtApplications(List.of(buildCourtApplication(randomUUID(), randomUUID(), randomUUID(), null)))
                .build();

        final DraftValidationRequest request = mapper.toValidationRequest(
                buildCommand(randomUUID(), LocalDate.now(), emptyList()), hearing);

        assertThat(request.getDefendants(), hasSize(1));
        assertThat(request.getOffences(), hasSize(1));
    }

    @Test
    void shouldMapProsecutionCasesAndCourtApplicationsTogether() {
        final UUID caseDefendantId = randomUUID();
        final UUID caseOffenceId = randomUUID();
        final UUID applicationDefendantId = randomUUID();
        final UUID applicationOffenceId = randomUUID();

        final Hearing hearing = Hearing.hearing()
                .withProsecutionCases(List.of(ProsecutionCase.prosecutionCase()
                        .withDefendants(List.of(Defendant.defendant()
                                .withId(caseDefendantId)
                                .withOffences(List.of(Offence.offence().withId(caseOffenceId).build()))
                                .build()))
                        .build()))
                .withCourtApplications(List.of(buildCourtApplication(applicationDefendantId, randomUUID(), applicationOffenceId, null)))
                .build();

        final DraftValidationRequest request = mapper.toValidationRequest(
                buildCommand(randomUUID(), LocalDate.now(), emptyList()), hearing);

        assertThat(request.getDefendants(), hasSize(2));
        assertThat(request.getDefendants().get(0).getDefendantId(), is(caseDefendantId.toString()));
        assertThat(request.getDefendants().get(1).getDefendantId(), is(applicationDefendantId.toString()));
        assertThat(request.getOffences(), hasSize(2));
        assertThat(request.getOffences().get(0).getOffenceId(), is(caseOffenceId.toString()));
        assertThat(request.getOffences().get(0).getDefendantId(), is(caseDefendantId.toString()));
        assertThat(request.getOffences().get(1).getOffenceId(), is(applicationOffenceId.toString()));
        assertThat(request.getOffences().get(1).getDefendantId(), is(applicationDefendantId.toString()));
    }

    @Test
    void shouldDeduplicateDefendantsAndOffencesSharedByProsecutionCaseAndCourtApplication() {
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();

        final Hearing hearing = Hearing.hearing()
                .withProsecutionCases(List.of(ProsecutionCase.prosecutionCase()
                        .withProsecutionCaseIdentifier(ProsecutionCaseIdentifier.prosecutionCaseIdentifier()
                                .withCaseURN("CASE-URN")
                                .build())
                        .withDefendants(List.of(Defendant.defendant()
                                .withId(defendantId)
                                .withOffences(List.of(Offence.offence().withId(offenceId).build()))
                                .build()))
                        .build()))
                .withCourtApplications(List.of(buildCourtApplication(defendantId, randomUUID(), offenceId, "APPLICATION-URN")))
                .build();

        final DraftValidationRequest request = mapper.toValidationRequest(
                buildCommand(randomUUID(), LocalDate.now(), emptyList()), hearing);

        assertThat(request.getDefendants(), hasSize(1));
        assertThat(request.getOffences(), hasSize(1));
        assertThat(request.getOffences().get(0).getCaseUrn(), is("CASE-URN"));
    }

    @Test
    void shouldMapSubjectWithoutOffencesWhenCourtApplicationHasNoCasesOrCourtOrder() {
        final UUID defendantId = randomUUID();
        final CourtApplication courtApplication = CourtApplication.courtApplication()
                .withValuesFrom(buildCourtApplication(defendantId, randomUUID(), randomUUID(), null))
                .withCourtApplicationCases(null)
                .withCourtOrder(CourtOrder.courtOrder().withId(randomUUID()).build())
                .build();

        final DraftValidationRequest request = mapper.toValidationRequest(
                buildCommand(randomUUID(), LocalDate.now(), emptyList()),
                Hearing.hearing().withCourtApplications(List.of(courtApplication)).build());

        assertThat(request.getDefendants(), hasSize(1));
        assertThat(request.getDefendants().get(0).getDefendantId(), is(defendantId.toString()));
        assertThat(request.getOffences(), is(empty()));
    }

    @Test
    void shouldIgnoreCourtApplicationWhenSubjectHasNoMasterDefendant() {
        final Hearing hearing = Hearing.hearing()
                .withCourtApplications(List.of(CourtApplication.courtApplication()
                        .withId(randomUUID())
                        .withSubject(CourtApplicationParty.courtApplicationParty().withId(randomUUID()).build())
                        .withCourtApplicationCases(List.of(CourtApplicationCase.courtApplicationCase()
                                .withOffences(List.of(Offence.offence().withId(randomUUID()).build()))
                                .build()))
                        .build()))
                .build();

        final DraftValidationRequest request = mapper.toValidationRequest(
                buildCommand(randomUUID(), LocalDate.now(), emptyList()), hearing);

        assertThat(request.getDefendants(), is(empty()));
        assertThat(request.getOffences(), is(empty()));
    }

    @Test
    void shouldLeaveDefendantIdAndCaseUrnNullWhenMasterDefendantHasNoDefendantCase() {
        final UUID masterDefendantId = randomUUID();
        final CourtApplication template = buildCourtApplication(randomUUID(), masterDefendantId, randomUUID(), null);
        final CourtApplication courtApplication = CourtApplication.courtApplication()
                .withValuesFrom(template)
                .withSubject(CourtApplicationParty.courtApplicationParty()
                        .withValuesFrom(template.getSubject())
                        .withMasterDefendant(MasterDefendant.masterDefendant()
                                .withValuesFrom(template.getSubject().getMasterDefendant())
                                .withDefendantCase(null)
                                .build())
                        .build())
                .build();

        final DraftValidationRequest request = mapper.toValidationRequest(
                buildCommand(randomUUID(), LocalDate.now(), emptyList()),
                Hearing.hearing().withCourtApplications(List.of(courtApplication)).build());

        assertThat(request.getDefendants(), hasSize(1));
        assertThat(request.getDefendants().get(0).getDefendantId(), is(nullValue()));
        assertThat(request.getDefendants().get(0).getMasterDefendantId(), is(masterDefendantId.toString()));
        assertThat(request.getOffences(), hasSize(1));
        assertThat(request.getOffences().get(0).getCaseUrn(), is(nullValue()));
        assertThat(request.getOffences().get(0).getDefendantId(), is(nullValue()));
    }

    @Test
    void shouldSkipNullCourtApplicationsAndNullCourtApplicationCases() {
        final UUID offenceId = randomUUID();
        final List<CourtApplicationCase> courtApplicationCases = new ArrayList<>();
        courtApplicationCases.add(null);
        courtApplicationCases.add(CourtApplicationCase.courtApplicationCase()
                .withOffences(List.of(Offence.offence().withId(offenceId).build()))
                .build());
        final List<CourtApplication> courtApplications = new ArrayList<>();
        courtApplications.add(null);
        courtApplications.add(CourtApplication.courtApplication()
                .withValuesFrom(buildCourtApplication(randomUUID(), randomUUID(), randomUUID(), null))
                .withCourtApplicationCases(courtApplicationCases)
                .build());

        final Hearing hearing = Hearing.hearing()
                .withCourtApplications(courtApplications)
                .build();

        final DraftValidationRequest request = mapper.toValidationRequest(
                buildCommand(randomUUID(), LocalDate.now(), emptyList()), hearing);

        assertThat(request.getDefendants(), hasSize(1));
        assertThat(request.getOffences(), hasSize(1));
        assertThat(request.getOffences().get(0).getOffenceId(), is(offenceId.toString()));
    }

    @Test
    void shouldMapOffencesFromCourtOrderWhenCourtApplicationCasesAreEmpty() {
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();
        final String caseUrn = "28DI5750788";

        final CourtApplication courtApplication = CourtApplication.courtApplication()
                .withValuesFrom(buildCourtApplication(defendantId, randomUUID(), randomUUID(), caseUrn))
                .withCourtApplicationCases(emptyList())
                .withCourtOrder(buildCourtOrder(offenceId, "SX03191"))
                .build();

        final DraftValidationRequest request = mapper.toValidationRequest(
                buildCommand(randomUUID(), LocalDate.now(), emptyList()),
                Hearing.hearing().withCourtApplications(List.of(courtApplication)).build());

        assertThat(request.getDefendants(), hasSize(1));
        assertThat(request.getDefendants().get(0).getDefendantId(), is(defendantId.toString()));
        assertThat(request.getDefendants().get(0).getFirstName(), is("Jane"));
        assertThat(request.getOffences(), hasSize(1));
        assertThat(request.getOffences().get(0).getOffenceId(), is(offenceId.toString()));
        assertThat(request.getOffences().get(0).getOffenceCode(), is("SX03191"));
        assertThat(request.getOffences().get(0).getCaseUrn(), is(caseUrn));
        assertThat(request.getOffences().get(0).getDefendantId(), is(defendantId.toString()));
    }

    @Test
    void shouldMapOffencesFromBothCourtApplicationCasesAndCourtOrder() {
        final UUID applicationOffenceId = randomUUID();
        final UUID courtOrderOffenceId = randomUUID();

        final CourtApplication courtApplication = CourtApplication.courtApplication()
                .withValuesFrom(buildCourtApplication(randomUUID(), randomUUID(), applicationOffenceId, null))
                .withCourtOrder(buildCourtOrder(courtOrderOffenceId, "SX03191"))
                .build();

        final DraftValidationRequest request = mapper.toValidationRequest(
                buildCommand(randomUUID(), LocalDate.now(), emptyList()),
                Hearing.hearing().withCourtApplications(List.of(courtApplication)).build());

        assertThat(request.getDefendants(), hasSize(1));
        assertThat(request.getOffences(), hasSize(2));
        assertThat(request.getOffences().get(0).getOffenceId(), is(applicationOffenceId.toString()));
        assertThat(request.getOffences().get(1).getOffenceId(), is(courtOrderOffenceId.toString()));
    }

    @Test
    void shouldSkipNullCourtOrderOffencesAndCourtOrderOffencesWithoutOffence() {
        final UUID offenceId = randomUUID();
        final List<CourtOrderOffence> courtOrderOffences = new ArrayList<>();
        courtOrderOffences.add(null);
        courtOrderOffences.add(CourtOrderOffence.courtOrderOffence().build());
        courtOrderOffences.add(CourtOrderOffence.courtOrderOffence()
                .withOffence(Offence.offence().withId(offenceId).build())
                .build());

        final CourtApplication courtApplication = CourtApplication.courtApplication()
                .withValuesFrom(buildCourtApplication(randomUUID(), randomUUID(), randomUUID(), null))
                .withCourtApplicationCases(null)
                .withCourtOrder(CourtOrder.courtOrder()
                        .withId(randomUUID())
                        .withCourtOrderOffences(courtOrderOffences)
                        .build())
                .build();

        final DraftValidationRequest request = mapper.toValidationRequest(
                buildCommand(randomUUID(), LocalDate.now(), emptyList()),
                Hearing.hearing().withCourtApplications(List.of(courtApplication)).build());

        assertThat(request.getOffences(), hasSize(1));
        assertThat(request.getOffences().get(0).getOffenceId(), is(offenceId.toString()));
    }

    private CourtOrder buildCourtOrder(final UUID offenceId, final String offenceCode) {
        return CourtOrder.courtOrder()
                .withId(randomUUID())
                .withCourtOrderOffences(List.of(CourtOrderOffence.courtOrderOffence()
                        .withOffence(Offence.offence()
                                .withId(offenceId)
                                .withOffenceCode(offenceCode)
                                .build())
                        .build()))
                .build();
    }

    private CourtApplication buildCourtApplication(final UUID defendantId, final UUID masterDefendantId,
                                                   final UUID offenceId, final String caseUrn) {
        return CourtApplication.courtApplication()
                .withId(randomUUID())
                .withSubject(CourtApplicationParty.courtApplicationParty()
                        .withId(randomUUID())
                        .withMasterDefendant(MasterDefendant.masterDefendant()
                                .withMasterDefendantId(masterDefendantId)
                                .withDefendantCase(List.of(DefendantCase.defendantCase()
                                        .withDefendantId(defendantId)
                                        .withCaseId(randomUUID())
                                        .withCaseReference(caseUrn)
                                        .build()))
                                .withPersonDefendant(PersonDefendant.personDefendant()
                                        .withPersonDetails(Person.person()
                                                .withFirstName("Jane")
                                                .withLastName("Doe")
                                                .withDateOfBirth(LocalDate.of(1945, 2, 18))
                                                .build())
                                        .build())
                                .build())
                        .build())
                .withCourtApplicationCases(List.of(CourtApplicationCase.courtApplicationCase()
                        .withOffences(List.of(Offence.offence()
                                .withId(offenceId)
                                .withOffenceCode("TH68001")
                                .build()))
                        .build()))
                .build();
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
