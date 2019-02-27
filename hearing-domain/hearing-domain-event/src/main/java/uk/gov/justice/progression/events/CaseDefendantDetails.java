package uk.gov.justice.progression.events;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.command.defendant.Defendant;

import java.util.ArrayList;
import java.util.List;

@Event("public.progression.case-defendant-changed")
public class CaseDefendantDetails {

    private List<Defendant> defendants;

    public static CaseDefendantDetails caseDefendantDetails() {
        return new CaseDefendantDetails();
    }

    public List<Defendant> getDefendants() {
        return new ArrayList<>(defendants);
    }

    public CaseDefendantDetails setDefendants(List<Defendant> defendants) {
        this.defendants = defendants;
        return this;
    }
}