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
        final List<DefendantDto> defendants = new ArrayList<>();
        final List<OffenceDto> offences = new ArrayList<>();

        if (hearing.getProsecutionCases() != null) {
            hearing.getProsecutionCases()
                    .forEach(prosecutionCase -> processProsecutionCase(prosecutionCase, defendants, offences));
        }

        return new ValidationRequest(
                uuidToString(command.getHearingId()),
                command.getHearingDay(),
                extractCourtType(hearing),
                extractCaseId(command.getResultLines()),
                mapResultLines(command.getResultLines()),
                offences,
                defendants);
    }

    private void processProsecutionCase(final ProsecutionCase prosecutionCase,
                                        final List<DefendantDto> defendants,
                                        final List<OffenceDto> offences) {
        final String caseUrn = extractCaseUrn(prosecutionCase);
        if (prosecutionCase.getDefendants() != null) {
            prosecutionCase.getDefendants()
                    .forEach(defendant -> processDefendant(defendant, caseUrn, defendants, offences));
        }
    }

    private void processDefendant(final Defendant defendant, final String caseUrn,
                                   final List<DefendantDto> defendants, final List<OffenceDto> offences) {
        defendants.add(toDefendantDto(defendant));
        if (defendant.getOffences() != null) {
            defendant.getOffences()
                    .forEach(offence -> offences.add(toOffenceDto(offence, caseUrn)));
        }
    }

    private DefendantDto toDefendantDto(final Defendant defendant) {
        final Person personDetails = extractPersonDetails(defendant);
        return DefendantDto.builder()
                .withId(uuidToString(defendant.getId()))
                .withFirstName(personDetails != null ? personDetails.getFirstName() : null)
                .withLastName(personDetails != null ? personDetails.getLastName() : null)
                .withMasterDefendantId(uuidToString(defendant.getMasterDefendantId()))
                .build();
    }

    private OffenceDto toOffenceDto(final Offence offence, final String caseUrn) {
        return new OffenceDto.Builder()
                .id(uuidToString(offence.getId()))
                .offenceCode(offence.getOffenceCode())
                .offenceTitle(offence.getOffenceTitle())
                .orderIndex(offence.getOrderIndex())
                .caseUrn(caseUrn)
                .build();
    }

    private String extractCaseId(final List<SharedResultsCommandResultLineV2> resultLines) {
        if (resultLines == null) {
            return null;
        }
        return resultLines.stream()
                .filter(line -> line.getCaseId() != null)
                .findFirst()
                .map(line -> line.getCaseId().toString())
                .orElse(null);
    }

    private List<ResultLineDto> mapResultLines(final List<SharedResultsCommandResultLineV2> resultLines) {
        if (resultLines == null) {
            return new ArrayList<>();
        }
        return resultLines.stream()
                .map(this::toResultLineDto)
                .toList();
    }

    private ResultLineDto toResultLineDto(final SharedResultsCommandResultLineV2 line) {
        return new ResultLineDto.Builder()
                .id(uuidToString(line.getResultLineId()))
                .shortCode(line.getShortCode())
                .label(line.getResultLabel())
                .defendantId(uuidToString(line.getDefendantId()))
                .offenceId(uuidToString(line.getOffenceId()))
                .consecutiveToOffence(extractConsecutiveToOffence(line.getPrompts()))
                .isConcurrent(extractIsConcurrent(line.getPrompts()))
                .category(line.getCategory())
                .prompts(mapPrompts(line.getPrompts()))
                .build();
    }

    private String extractCourtType(final Hearing hearing) {
        return hearing.getJurisdictionType() != null ? hearing.getJurisdictionType().name() : null;
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

    private List<PromptDto> mapPrompts(final List<SharedResultsCommandPrompt> prompts) {
        if (prompts == null) {
            return null;
        }
        return prompts.stream()
                .map(p -> new PromptDto(p.getPromptRef(), p.getValue()))
                .toList();
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
