package uk.gov.moj.cpp.hearing.command.handler.service.validation;

import static java.util.stream.Collectors.toList;

import uk.gov.justice.core.courts.BailStatus;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.CourtOrder;
import uk.gov.justice.core.courts.CourtOrderOffence;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.DefendantCase;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.MasterDefendant;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ValidationRequestMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationRequestMapper.class);

    public DraftValidationRequest toValidationRequest(final ShareDaysResultsCommand command, final Hearing hearing) {

        final Map<String, DefendantDto> defendants = new LinkedHashMap<>();
        final Map<String, OffenceDto> offences = new LinkedHashMap<>();
        mapProsecutionCases(hearing, defendants, offences);
        mapCourtApplications(hearing, defendants, offences);

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
                .offences(new ArrayList<>(offences.values()))
                .defendants(new ArrayList<>(defendants.values()));
    }

    private void mapProsecutionCases(final Hearing hearing,
                                     final Map<String, DefendantDto> defendants,
                                     final Map<String, OffenceDto> offences) {
        if (hearing.getProsecutionCases() == null) {
            return;
        }
        hearing.getProsecutionCases()
                .forEach(prosecutionCase -> mapProsecutionCase(prosecutionCase, defendants, offences));
    }

    private void mapCourtApplications(final Hearing hearing,
                                      final Map<String, DefendantDto> defendants,
                                      final Map<String, OffenceDto> offences) {
        if (hearing.getCourtApplications() == null) {
            return;
        }
        hearing.getCourtApplications().stream()
                .filter(Objects::nonNull)
                .forEach(courtApplication -> mapCourtApplication(courtApplication, defendants, offences));
    }

    private void mapCourtApplication(final CourtApplication courtApplication,
                                     final Map<String, DefendantDto> defendants,
                                     final Map<String, OffenceDto> offences) {
        final MasterDefendant masterDefendant = extractMasterDefendant(courtApplication.getSubject());
        if (masterDefendant == null) {
            return;
        }
        final DefendantCase defendantCase = extractFirstDefendantCase(masterDefendant);
        final String defendantId = defendantCase != null ? uuidToString(defendantCase.getDefendantId()) : null;
        final String caseUrn = defendantCase != null ? defendantCase.getCaseReference() : null;

        defendants.putIfAbsent(defendantId, toDefendantDto(masterDefendant, defendantId));
        extractApplicationOffences(courtApplication)
                .forEach(offence -> addOffence(offence, caseUrn, defendantId, offences));
    }

    private List<Offence> extractApplicationOffences(final CourtApplication courtApplication) {
        final List<Offence> offences = new ArrayList<>();
        if (courtApplication.getCourtApplicationCases() != null) {
            courtApplication.getCourtApplicationCases().stream()
                    .filter(Objects::nonNull)
                    .map(CourtApplicationCase::getOffences)
                    .filter(Objects::nonNull)
                    .forEach(offences::addAll);
        }
        final CourtOrder courtOrder = courtApplication.getCourtOrder();
        if (courtOrder != null && courtOrder.getCourtOrderOffences() != null) {
            courtOrder.getCourtOrderOffences().stream()
                    .filter(Objects::nonNull)
                    .map(CourtOrderOffence::getOffence)
                    .forEach(offences::add);
        }
        return offences.stream().filter(Objects::nonNull).collect(toList());
    }

    private DefendantCase extractFirstDefendantCase(final MasterDefendant masterDefendant) {
        final List<DefendantCase> defendantCases = masterDefendant.getDefendantCase();
        return defendantCases != null && !defendantCases.isEmpty() ? defendantCases.get(0) : null;
    }

    private DefendantDto toDefendantDto(final MasterDefendant masterDefendant, final String defendantId) {
        final PersonDefendant personDefendant = masterDefendant.getPersonDefendant();
        final Person personDetails = personDefendant != null ? personDefendant.getPersonDetails() : null;
        return new DefendantDto()
                .defendantId(defendantId)
                .firstName(personDetails != null ? personDetails.getFirstName() : null)
                .lastName(personDetails != null ? personDetails.getLastName() : null)
                .dateOfBirth(personDetails != null ? personDetails.getDateOfBirth() : null)
                .masterDefendantId(uuidToString(masterDefendant.getMasterDefendantId()));
    }

    private void mapProsecutionCase(final ProsecutionCase prosecutionCase,
                                    final Map<String, DefendantDto> defendants,
                                    final Map<String, OffenceDto> offences) {
        if (prosecutionCase.getDefendants() == null) {
            return;
        }
        final String caseUrn = extractCaseUrn(prosecutionCase);
        prosecutionCase.getDefendants()
                .forEach(defendant -> {
                    final String defendantId = uuidToString(defendant.getId());
                    defendants.putIfAbsent(defendantId, toDefendantDto(defendant));
                    if (defendant.getOffences() != null) {
                        defendant.getOffences()
                                .forEach(offence -> addOffence(offence, caseUrn, defendantId, offences));
                    }
                });
    }

    private DefendantDto toDefendantDto(final Defendant defendant) {
        final Person personDetails = extractPersonDetails(defendant);
        return new DefendantDto()
                .defendantId(uuidToString(defendant.getId()))
                .firstName(personDetails != null ? personDetails.getFirstName() : null)
                .lastName(personDetails != null ? personDetails.getLastName() : null)
                .dateOfBirth(personDetails != null ? personDetails.getDateOfBirth() : null)
                .masterDefendantId(uuidToString(defendant.getMasterDefendantId()));
    }

    private void addOffence(final Offence offence, final String caseUrn, final String defendantId,
                            final Map<String, OffenceDto> offences) {
        offences.putIfAbsent(uuidToString(offence.getId()), toOffenceDto(offence, caseUrn, defendantId));
    }

    private OffenceDto toOffenceDto(final Offence offence, final String caseUrn, final String defendantId) {
        return new OffenceDto()
                .offenceId(uuidToString(offence.getId()))
                .offenceCode(offence.getOffenceCode())
                .offenceTitle(offence.getOffenceTitle())
                .orderIndex(offence.getOrderIndex())
                .caseUrn(caseUrn)
                .defendantId(defendantId)
                .bailStatus(toBailStatus(offence.getBailStatus()))
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

    private static OffenceDto.BailStatusEnum toBailStatus(final BailStatus bailStatus) {
        if (bailStatus == null || bailStatus.getCode() == null) {
            return null;
        }
        try {
            return OffenceDto.BailStatusEnum.fromValue(bailStatus.getCode());
        } catch (final IllegalArgumentException ex) {
            LOGGER.warn("Unrecognised bail status '{}' for results validation, sending null", bailStatus.getCode());
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

    private MasterDefendant extractMasterDefendant(final CourtApplicationParty subject) {
        return subject != null ? subject.getMasterDefendant() : null;
    }

    private String extractCaseUrn(final ProsecutionCase prosecutionCase) {
        return extractCaseUrn(prosecutionCase.getProsecutionCaseIdentifier());
    }

    private String extractCaseUrn(final ProsecutionCaseIdentifier identifier) {
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
