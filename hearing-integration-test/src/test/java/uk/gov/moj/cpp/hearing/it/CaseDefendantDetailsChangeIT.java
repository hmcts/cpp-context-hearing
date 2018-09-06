package uk.gov.moj.cpp.hearing.it;

import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CaseDefendantDetailsChangedCommandTemplates.caseDefendantDetailsChangedCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import org.junit.Test;
import uk.gov.justice.json.schemas.core.Address;
import uk.gov.justice.json.schemas.core.Defendant;
import uk.gov.justice.json.schemas.core.Hearing;
import uk.gov.justice.json.schemas.core.Person;
import uk.gov.justice.json.schemas.core.PersonDefendant;
import uk.gov.justice.json.schemas.core.ProsecutionCase;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

public class CaseDefendantDetailsChangeIT extends AbstractIT {

    @Test
    public void updateCaseDefendantDetails_shouldUpdateDefendant_givenResultNotShared() throws Exception {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        final CommandHelpers.CaseDefendantDetailsHelper defendantUpdates = h(UseCases.updateDefendants(caseDefendantDetailsChangedCommandTemplate(
                hearingOne.getFirstCase().getId(),
                hearingOne.getFirstDefendantForFirstCase().getId()
        )));

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getId, is(hearingOne.getFirstCase().getId()))
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getId, is(hearingOne.getFirstDefendantForFirstCase().getId()))
                                        .with(Defendant::getPersonDefendant, isBean(PersonDefendant.class)
                                                .with(PersonDefendant::getPersonDetails, isBean(Person.class)
                                                        .with(Person::getFirstName, is(defendantUpdates.getFirstDefendant().getPerson().getFirstName()))
                                                        .with(Person::getLastName, is(defendantUpdates.getFirstDefendant().getPerson().getLastName()))
                                                        .with(Person::getAddress, isBean(Address.class)
                                                                .with(Address::getAddress1, is(defendantUpdates.getFirstDefendant().getPerson().getAddress().getAddress1()))
                                                                .with(Address::getAddress2, is(defendantUpdates.getFirstDefendant().getPerson().getAddress().getAddress2()))
                                                                .with(Address::getAddress3, is(defendantUpdates.getFirstDefendant().getPerson().getAddress().getAddress3()))
                                                                .with(Address::getAddress4, is(defendantUpdates.getFirstDefendant().getPerson().getAddress().getAddress4()))
                                                                .with(Address::getPostcode, is(defendantUpdates.getFirstDefendant().getPerson().getAddress().getPostCode()))
                                                        )
                                                )
                                        )
                                ))
                        ))
                )
        );
    }
}