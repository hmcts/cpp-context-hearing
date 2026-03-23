package uk.gov.moj.cpp.hearing.command.handler.service.validation;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.moj.cpp.hearing.command.result.ShareDaysResultsCommand;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandPrompt;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLineV2;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ValidationRequestMapper {

    public ValidationRequest toValidationRequest(final ShareDaysResultsCommand command, final Hearing hearing) {

        final String courtType = hearing.getJurisdictionType() != null
                ? hearing.getJurisdictionType().name()
                : null;

        final List<ValidationRequest.DefendantDto> defendants = new ArrayList<>();
        final List<ValidationRequest.OffenceDto> offences = new ArrayList<>();
        String caseId = null;

        if (hearing.getProsecutionCases() != null) {
            for (final ProsecutionCase prosecutionCase : hearing.getProsecutionCases()) {
                final String caseUrn = extractCaseUrn(prosecutionCase);

                if (prosecutionCase.getDefendants() != null) {
                    for (final Defendant defendant : prosecutionCase.getDefendants()) {
                        final Person personDetails = extractPersonDetails(defendant);
                        defendants.add(new ValidationRequest.DefendantDto(
                                uuidToString(defendant.getId()),
                                personDetails != null ? personDetails.getFirstName() : null,
                                personDetails != null ? personDetails.getLastName() : null,
                                uuidToString(defendant.getMasterDefendantId())));

                        if (defendant.getOffences() != null) {
                            for (final Offence offence : defendant.getOffences()) {
                                offences.add(new ValidationRequest.OffenceDto(
                                        uuidToString(offence.getId()),
                                        offence.getOffenceCode(),
                                        offence.getOffenceTitle(),
                                        offence.getOrderIndex(),
                                        caseUrn));
                            }
                        }
                    }
                }
            }
        }

        final List<ValidationRequest.ResultLineDto> resultLines = new ArrayList<>();
        if (command.getResultLines() != null) {
            for (final SharedResultsCommandResultLineV2 line : command.getResultLines()) {
                if (caseId == null && line.getCaseId() != null) {
                    caseId = line.getCaseId().toString();
                }
                resultLines.add(new ValidationRequest.ResultLineDto(
                        uuidToString(line.getResultLineId()),
                        line.getShortCode(),
                        line.getResultLabel(),
                        uuidToString(line.getDefendantId()),
                        uuidToString(line.getOffenceId()),
                        extractIsConcurrent(line.getPrompts()),
                        extractConsecutiveToOffence(line.getPrompts())));
            }
        }

        return new ValidationRequest(
                uuidToString(command.getHearingId()),
                command.getHearingDay(),
                courtType,
                caseId,
                resultLines,
                offences,
                defendants);
    }

    private Person extractPersonDetails(final Defendant defendant) {
        final PersonDefendant personDefendant = defendant.getPersonDefendant();
        return personDefendant != null ? personDefendant.getPersonDetails() : null;
    }

    private String extractCaseUrn(final ProsecutionCase prosecutionCase) {
        final ProsecutionCaseIdentifier identifier = prosecutionCase.getProsecutionCaseIdentifier();
        return identifier != null ? identifier.getCaseURN() : null;
    }

    private String uuidToString(final UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    private Boolean extractIsConcurrent(final List<SharedResultsCommandPrompt> prompts) {
        if (prompts == null) {
            return null;
        }
        return prompts.stream()
                .filter(p -> "concurrent".equals(p.getPromptRef()))
                .findFirst()
                .map(p -> "true".equalsIgnoreCase(p.getValue()))
                .orElse(null);
    }

    private String extractConsecutiveToOffence(final List<SharedResultsCommandPrompt> prompts) {
        if (prompts == null) {
            return null;
        }
        return prompts.stream()
                .filter(p -> "consecutiveToOffenceNumber".equals(p.getPromptRef()))
                .findFirst()
                .map(SharedResultsCommandPrompt::getValue)
                .filter(v -> v != null && !v.isBlank())
                .orElse(null);
    }
}
