package uk.gov.justice.progression.events;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.defendant.Defendant;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"squid:S1188"})
@Event("public.progression.case-defendant-changed")
public class CaseDefendantDetails {

    private UUID caseId;

    private List<Defendant> defendants;

    public UUID getCaseId() {
        return caseId;
    }

    public List<Defendant> getDefendants() {
        return new ArrayList<>(defendants);
    }

    public CaseDefendantDetails setCaseId(UUID caseId) {
        this.caseId = caseId;
        return this;
    }

    public CaseDefendantDetails setDefendants(List<Defendant> defendants) {
        this.defendants = defendants;
        return this;
    }

    public static CaseDefendantDetails caseDefendantDetails() {
        return new CaseDefendantDetails();
    }

}