package uk.gov.moj.cpp.hearing.it;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.CoreTemplateArguments.toMap;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.associatedPerson;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.defaultArguments;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.defendant;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;
import uk.gov.moj.cpp.hearing.test.Pair;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"squid:S2699"})
public class AddDefendantIT extends AbstractIT {

    @Disabled("GPE-13308")
    @Test
    public void shouldAddNewDefendant() throws Exception {

        InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        HearingDay hearingDay = initiateHearingCommand.getHearing().getHearingDays().get(0);
        hearingDay.setSittingDay(ZonedDateTime.now().plusDays(1));
        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), initiateHearingCommand));

        UUID newDefendantId = randomUUID();
        CoreTestTemplates.CoreTemplateArguments args = defaultArguments();
        args.setStructure(toMap(newDefendantId, toMap(randomUUID(), asList(randomUUID()))));
        args.setCourtProceedingsInitiated(ZonedDateTime.now(ZoneOffset.UTC));
        Defendant addNewDefendant = defendant(hearingOne.getFirstCase().getId(), args,
                new Pair<>(newDefendantId, asList(randomUUID())), false)
                .withAssociatedPersons(asList(associatedPerson(defaultArguments()).build()))
                .withProsecutionCaseId(hearingOne.getFirstDefendantForFirstCase().getProsecutionCaseId())
                .build();

        UseCases.addDefendant(addNewDefendant);

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                        .with(ProsecutionCase::getId, is(hearingOne.getFirstCase().getId()))
                                        .with(ProsecutionCase::getDefendants, hasItem(isBean(Defendant.class)
                                                .with(Defendant::getMasterDefendantId, is(addNewDefendant.getMasterDefendantId()))
                                                .with(Defendant::getCourtProceedingsInitiated, is(addNewDefendant.getCourtProceedingsInitiated().withZoneSameLocal(ZoneId.of("UTC"))))
                                        ))
                                )
                        )));
    }
}