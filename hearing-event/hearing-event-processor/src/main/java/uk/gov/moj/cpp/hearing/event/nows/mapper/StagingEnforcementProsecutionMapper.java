package uk.gov.moj.cpp.hearing.event.nows.mapper;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.ProsecutionCase;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

class StagingEnforcementProsecutionMapper {

    private final ProsecutionCase prosecutionCase;

    StagingEnforcementProsecutionMapper(final UUID defendantId, final Hearing hearing) {

        prosecutionCase = hearing.getProsecutionCases().stream()
                .filter(pcase -> isDefendantExist(defendantId, pcase.getDefendants())).findFirst().orElse(null);
    }

    String getCaseReference() {
        if (nonNull(prosecutionCase)) {
            return Stream.of(prosecutionCase.getProsecutionCaseIdentifier().getCaseURN(),
                    prosecutionCase.getProsecutionCaseIdentifier().getProsecutionAuthorityReference())
                    .filter(s -> nonNull(s) && !s.isEmpty())
                    .collect(joining(","));
        }
        return null;
    }

    private boolean isDefendantExist(final UUID defendantId, List<Defendant> defendants) {
        return defendants.stream().anyMatch(defendant -> defendant.getId().equals(defendantId));
    }

    String getAuthorityCode() {

        if (nonNull(prosecutionCase)) {
            return prosecutionCase.getProsecutionCaseIdentifier().getProsecutionAuthorityCode();
        }

        return null;
    }
}
