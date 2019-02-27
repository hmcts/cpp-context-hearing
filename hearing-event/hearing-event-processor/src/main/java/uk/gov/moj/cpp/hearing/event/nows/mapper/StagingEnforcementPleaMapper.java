package uk.gov.moj.cpp.hearing.event.nows.mapper;

import static java.util.Objects.nonNull;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.PleaValue;
import uk.gov.justice.json.schemas.staging.Plea;

public class StagingEnforcementPleaMapper {

    private final Defendant defendant;

    StagingEnforcementPleaMapper(final Defendant defendant) {
        this.defendant = defendant;
    }

    public Plea map() {
        return Plea.plea()
                .withIncludesGuilty(checkIfAnyOffenceHasGuiltyPlea())
                .withIncludesOnline(false)
                .build();
    }

    private boolean checkIfAnyOffenceHasGuiltyPlea() {
        return defendant.getOffences().stream()
                .filter(offence -> nonNull(offence.getPlea()))
                .anyMatch(offence -> offence.getPlea().getPleaValue() == PleaValue.GUILTY);
    }
}
