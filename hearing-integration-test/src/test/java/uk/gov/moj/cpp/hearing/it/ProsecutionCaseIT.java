package uk.gov.moj.cpp.hearing.it;

import static java.time.ZonedDateTime.now;
import static org.hamcrest.CoreMatchers.is;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.updateProsecutor;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.justice.core.courts.Prosecutor;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.domain.event.CpsProsecutorUpdated;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

public class ProsecutionCaseIT extends AbstractIT {

    @Test
    public void shouldUpdateProsecutionCase() throws Exception {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final HearingDay hearingDay = initiateHearingCommand.getHearing().getHearingDays().get(0);
        hearingDay.setSittingDay(now().plusDays(1));
        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), initiateHearingCommand));
        final List<UUID> hearingIds = new ArrayList<>();
        hearingIds.add(initiateHearingCommand.getHearing().getId());
        final UUID prosecutionCaseId = initiateHearingCommand.getHearing().getProsecutionCases().get(0).getId();


        final CpsProsecutorUpdated cpsProsecutorUpdated = CpsProsecutorUpdated.cpsProsecutorUpdated()
                .setProsecutionCaseId(prosecutionCaseId)
                .setProsecutionAuthorityId(UUID.randomUUID())
                .setProsecutionAuthorityCode(STRING.next())
                .setProsecutionAuthorityName(STRING.next())
                .setProsecutionAuthorityReference(STRING.next())
                .setAddress(Address.address().withAddress1(STRING.next()).withPostcode("MK9 2BQ").build())
                .setCaseURN(STRING.next());

        updateProsecutor(prosecutionCaseId, hearingIds, cpsProsecutorUpdated);

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getProsecutor, isBean(Prosecutor.class)
                                        .with(Prosecutor::getProsecutorId, is(cpsProsecutorUpdated.getProsecutionAuthorityId()))
                                        .with(Prosecutor::getProsecutorCode, is(cpsProsecutorUpdated.getProsecutionAuthorityCode()))
                                )
                        ))
                ));
    }

    @Test
    public void shouldNotUpdateProsecutionCaseWithoutHearings() throws Exception {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final HearingDay hearingDay = initiateHearingCommand.getHearing().getHearingDays().get(0);
        hearingDay.setSittingDay(now().plusDays(1));
        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), initiateHearingCommand));
        final List<UUID> hearingIds = new ArrayList<>();
        final UUID prosecutionCaseId = initiateHearingCommand.getHearing().getProsecutionCases().get(0).getId();


        final CpsProsecutorUpdated cpsProsecutorUpdated = CpsProsecutorUpdated.cpsProsecutorUpdated()
                .setProsecutionCaseId(prosecutionCaseId)
                .setProsecutionAuthorityId(UUID.randomUUID())
                .setProsecutionAuthorityCode(STRING.next())
                .setProsecutionAuthorityName(STRING.next())
                .setProsecutionAuthorityReference(STRING.next())
                .setAddress(Address.address().withAddress1(STRING.next()).withPostcode("MK9 2BQ").build())
                .setCaseURN(STRING.next());

        updateProsecutor(prosecutionCaseId, hearingIds, cpsProsecutorUpdated);

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getProsecutionCaseIdentifier, isBean(ProsecutionCaseIdentifier.class)
                                        .with(ProsecutionCaseIdentifier::getProsecutionAuthorityId, is(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityId()))
                                        .with(ProsecutionCaseIdentifier::getProsecutionAuthorityCode, is(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityCode()))
                                        .with(ProsecutionCaseIdentifier::getProsecutionAuthorityReference, is(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityReference()))
                                        .with(ProsecutionCaseIdentifier::getCaseURN, is(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN()))
                                )
                        ))));
    }
}