package uk.gov.moj.cpp.hearing.command.handler.service.validation;

import static java.util.stream.Collectors.toList;

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
import uk.gov.moj.cpp.hearing.domain.common.resultsvalidator.DefendantDto;
import uk.gov.moj.cpp.hearing.domain.common.resultsvalidator.DraftValidationRequest;
import uk.gov.moj.cpp.hearing.domain.common.resultsvalidator.OffenceDto;
import uk.gov.moj.cpp.hearing.domain.common.resultsvalidator.Prompt;
import uk.gov.moj.cpp.hearing.domain.common.resultsvalidator.ResultLineDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ValidationRequestMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationRequestMapper.class);

    public DraftValidationRequest toValidationRequest(final ShareDaysResultsCommand command, final Hearing hearing) {

        final List<DefendantDto> defendants = new ArrayList<>();
        final List<OffenceDto> offences = new ArrayList<>();
        mapProsecutionCases(hearing, defendants, offences);

        final List<SharedResultsCommandResultLineV2> commandLines =
                command.getResultLines() != null ? command.getResultLines() : List.of();

        final List<ResultLineDto> resultLines = commandLines.stream()
                .map(this::toResultLineDto)
                .collect(toList());

        return new DraftValidationRequest()
                .hearingId(uuidToString(command.getHearingId()))
                .hearingDay(command.getHearingDay())
                .courtType(toCourtType(hearing))
                .caseId(extractCaseId(commandLines))
                .resultLines(resultLines)
                .offences(offences)
                .defendants(defendants);
    }

    private void mapProsecutionCases(final Hearing hearing,
                                     final List<DefendantDto> defendants,
                                     final List<OffenceDto> offences) {
        if (hearing.getProsecutionCases() == null) {
            return;
        }
        hearing.getProsecutionCases()
                .forEach(prosecutionCase -> mapProsecutionCase(prosecutionCase, defendants, offences));
    }

    private void mapProsecutionCase(final ProsecutionCase prosecutionCase,
                                    final List<DefendantDto> defendants,
                                    final List<OffenceDto> offences) {
        if (prosecutionCase.getDefendants() == null) {
            return;
        }
        final String caseUrn = extractCaseUrn(prosecutionCase);
        prosecutionCase.getDefendants()
                .forEach(defendant -> {
                    defendants.add(toDefendantDto(defendant));
                    mapOffences(defendant, caseUrn, offences);
                });
    }

    private DefendantDto toDefendantDto(final Defendant defendant) {
        final Person personDetails = extractPersonDetails(defendant);
        return new DefendantDto()
                .defendantId(uuidToString(defendant.getId()))
                .firstName(personDetails != null ? personDetails.getFirstName() : null)
                .lastName(personDetails != null ? personDetails.getLastName() : null)
                .masterDefendantId(uuidToString(defendant.getMasterDefendantId()));
    }

    private void mapOffences(final Defendant defendant, final String caseUrn, final List<OffenceDto> offences) {
        if (defendant.getOffences() == null) {
            return;
        }
        defendant.getOffences()
                .forEach(offence -> offences.add(toOffenceDto(offence, caseUrn)));
    }

    private OffenceDto toOffenceDto(final Offence offence, final String caseUrn) {
        return new OffenceDto()
                .offenceId(uuidToString(offence.getId()))
                .offenceCode(offence.getOffenceCode())
                .offenceTitle(offence.getOffenceTitle())
                .orderIndex(offence.getOrderIndex())
                .caseUrn(caseUrn)
                .isConvicted(offence.getConvictionDate() != null)
                .hasExistingCtlRecord(hasExistingCustodyTimeLimit(offence));
    }

    private boolean hasExistingCustodyTimeLimit(final Offence offence) {
        return offence.getCustodyTimeLimit() != null
                && offence.getCustodyTimeLimit().getTimeLimit() != null;
    }

    private ResultLineDto toResultLineDto(final SharedResultsCommandResultLineV2 line) {
        return new ResultLineDto()
                .resultLineId(uuidToString(line.getResultLineId()))
                .shortCode(line.getShortCode())
                .label(line.getResultLabel())
                .defendantId(uuidToString(line.getDefendantId()))
                .offenceId(uuidToString(line.getOffenceId()))
                .consecutiveToOffence(extractConsecutiveToOffence(line.getPrompts()))
                .category(toCategory(line.getCategory()))
                .isConcurrent(extractIsConcurrent(line.getPrompts()))
                .prompts(mapPrompts(line.getPrompts()));
    }

    private List<Prompt> mapPrompts(final List<SharedResultsCommandPrompt> prompts) {
        if (prompts == null) {
            return null;
        }
        return prompts.stream()
                .map(p -> new Prompt().promptRef(p.getPromptRef()).promptValue(p.getValue()))
                .toList();
    }


    private String extractCaseId(final List<SharedResultsCommandResultLineV2> lines) {
        return lines.stream()
                .map(SharedResultsCommandResultLineV2::getCaseId)
                .filter(Objects::nonNull)
                .findFirst()
                .map(Object::toString)
                .orElse(null);
    }

    private static DraftValidationRequest.CourtTypeEnum toCourtType(final Hearing hearing) {
        return hearing.getJurisdictionType() != null
                ? toCourtType(hearing.getJurisdictionType().name())
                : null;
    }

    private static DraftValidationRequest.CourtTypeEnum toCourtType(final String courtType) {
        try {
            return DraftValidationRequest.CourtTypeEnum.fromValue(courtType);
        } catch (final IllegalArgumentException ex) {
            LOGGER.warn("Unrecognised court type '{}' for results validation, sending null", courtType);
            return null;
        }
    }

    private static ResultLineDto.CategoryEnum toCategory(final String category) {
        if (category == null) {
            return null;
        }
        try {
            return ResultLineDto.CategoryEnum.fromValue(category);
        } catch (final IllegalArgumentException ex) {
            LOGGER.warn("Unrecognised result line category '{}' for results validation, sending null", category);
            return null;
        }
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
