package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.makeCommand;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.initiateHearingCommandTemplate;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.Test;

import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.plea.HearingUpdatePleaCommand;
import uk.gov.moj.cpp.hearing.it.TestUtilities.EventListener;

@SuppressWarnings("unchecked")
public class NewOffencePleaUpdateIT extends AbstractIT {

    enum PleaValueType {GUILTY, NOT_GUILTY};

    @Test
    public void updatePlea_toGuilty_shouldHaveConvictionDate() throws Throwable {

        final InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplate().build();
        
        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final UUID caseId = initiateHearingCommand.getCases().get(0).getCaseId();
        final UUID defendantId = initiateHearingCommand.getHearing().getDefendants().get(0).getId();
        final UUID offenceId = initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId();
        final LocalDate pleaDate = LocalDate.now();
        
        final EventListener publicEventHearingInitiatedListener = listenFor("public.hearing.initiated")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingId.toString()))));

        makeCommand(requestSpec, "hearing.initiate")
                .ofType("application/vnd.hearing.initiate+json")
                .withPayload(initiateHearingCommand)
                .executeSuccessfully();

        publicEventHearingInitiatedListener.waitFor();

        final String hearingDetailsQueryURL = getURL("hearing.get.hearing.v2", hearingId);

        poll(requestParameters(hearingDetailsQueryURL, "application/vnd.hearing.get.hearing.v2+json"))
                .until(
                    status().is(OK),
                    payload().isJson(allOf(withJsonPath("$.hearingId", is(hearingId.toString())),
                            withJsonPath("$.cases[0].caseId", is(caseId.toString())),
                            withJsonPath("$.cases[0].defendants[0].defendantId", is(defendantId.toString())),
                            withJsonPath("$.cases[0].defendants[0].offences[0].id", is(offenceId.toString())),
                            hasNoJsonPath("$.cases[0].defendants[0].offences[0].plea"),
                            hasNoJsonPath("$.cases[0].defendants[0].offences[0].plea.pleaDate"),
                            hasNoJsonPath("$.cases[0].defendants[0].offences[0].plea.value")
        )));

        final EventListener publicEventPleaUpdatedListener = listenFor("public.hearing.plea-updated")
                .withFilter(isJson(withJsonPath("$.offenceId", is(offenceId.toString()))));
        
        final EventListener publicEventOffenceConvictionDateChangedListener = listenFor("public.hearing.offence-conviction-date-changed")
        		.withFilter(isJson(withJsonPath("$.offenceId", is(offenceId.toString()))));

        final HearingUpdatePleaCommand updatePleaCommand = HearingUpdatePleaCommand.builder()
                .withCaseId(caseId)
                .addDefendant(uk.gov.moj.cpp.hearing.command.plea.Defendant.builder()
                        .withId(defendantId)
                        .addOffence(uk.gov.moj.cpp.hearing.command.plea.Offence.builder()
                                .withId(offenceId)
                                .withPlea(uk.gov.moj.cpp.hearing.command.plea.Plea.builder()
                                        .withId(UUID.randomUUID())
                                        .withPleaDate(pleaDate)
                                        .withValue(PleaValueType.GUILTY.name()))))
                .build();
        
        makeCommand(requestSpec, "hearing.update-plea")
            .withArgs(hearingId)
            .ofType("application/vnd.hearing.update-plea+json")
            .withPayload(updatePleaCommand)
            .executeSuccessfully();

        publicEventPleaUpdatedListener.waitFor();
        publicEventOffenceConvictionDateChangedListener.waitFor();

        poll(requestParameters(hearingDetailsQueryURL, "application/vnd.hearing.get.hearing.v2+json"))
                .until(
                    status().is(OK),
                    payload().isJson(allOf(withJsonPath("$.hearingId", is(hearingId.toString())),
                            withJsonPath("$.cases[0].caseId", is(caseId.toString())),
                            withJsonPath("$.cases[0].defendants[0].defendantId", is(defendantId.toString())),
                            withJsonPath("$.cases[0].defendants[0].offences[0].convictionDate", equalDate(pleaDate)),
                            withJsonPath("$.cases[0].defendants[0].offences[0].id", is(offenceId.toString())),
                            withJsonPath("$.cases[0].defendants[0].offences[0].plea.pleaDate", equalDate(pleaDate)),
                            withJsonPath("$.cases[0].defendants[0].offences[0].plea.value", equalEnum(PleaValueType.GUILTY))
                    )));
    }

    @Test
    public void updatePlea_toNotGuilty_shouldNotHaveConvictionDate() throws Throwable {

        final InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplate().build();
        
        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final UUID caseId = initiateHearingCommand.getCases().get(0).getCaseId();
        final UUID defendantId = initiateHearingCommand.getHearing().getDefendants().get(0).getId();
        final UUID offenceId = initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId();
        final LocalDate pleaDate = LocalDate.now();
        
        final EventListener publicEventHearingInitiatedListener = listenFor("public.hearing.initiated")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingId.toString()))));

        makeCommand(requestSpec, "hearing.initiate")
                .ofType("application/vnd.hearing.initiate+json")
                .withPayload(initiateHearingCommand)
                .executeSuccessfully();

        publicEventHearingInitiatedListener.waitFor();

        final String hearingDetailsQueryURL = getURL("hearing.get.hearing.v2", hearingId);

        poll(requestParameters(hearingDetailsQueryURL, "application/vnd.hearing.get.hearing.v2+json"))
                .until(
                    status().is(OK),
                    payload().isJson(allOf(withJsonPath("$.hearingId", is(hearingId.toString())),
                            withJsonPath("$.cases[0].caseId", is(caseId.toString())),
                            withJsonPath("$.cases[0].defendants[0].defendantId", is(defendantId.toString())),
                            withJsonPath("$.cases[0].defendants[0].offences[0].id", is(offenceId.toString())),
                            hasNoJsonPath("$.cases[0].defendants[0].offences[0].plea"),
                            hasNoJsonPath("$.cases[0].defendants[0].offences[0].plea.pleaDate"),
                            hasNoJsonPath("$.cases[0].defendants[0].offences[0].plea.value")
        )));

        final EventListener publicEventPleaUpdatedListener = listenFor("public.hearing.plea-updated")
                .withFilter(isJson(withJsonPath("$.offenceId", is(offenceId.toString()))));
        
        final EventListener publicEventOffenceConvictionDateRemovedListener = listenFor("public.hearing.offence-conviction-date-removed")
        		.withFilter(isJson(withJsonPath("$.offenceId", is(offenceId.toString()))));
        
        final HearingUpdatePleaCommand updatePleaCommand = HearingUpdatePleaCommand.builder()
                .withCaseId(caseId)
                .addDefendant(uk.gov.moj.cpp.hearing.command.plea.Defendant.builder()
                        .withId(defendantId)
                        .addOffence(uk.gov.moj.cpp.hearing.command.plea.Offence.builder()
                                .withId(offenceId)
                                .withPlea(uk.gov.moj.cpp.hearing.command.plea.Plea.builder()
                                        .withId(UUID.randomUUID())
                                        .withPleaDate(pleaDate)
                                        .withValue(PleaValueType.NOT_GUILTY.name()))))
                .build();
        
        makeCommand(requestSpec, "hearing.update-plea")
            .withArgs(hearingId)
            .ofType("application/vnd.hearing.update-plea+json")
            .withPayload(updatePleaCommand)
            .executeSuccessfully();

        publicEventPleaUpdatedListener.waitFor();
        publicEventOffenceConvictionDateRemovedListener.waitFor();

        poll(requestParameters(hearingDetailsQueryURL, "application/vnd.hearing.get.hearing.v2+json"))
                .until(
                    status().is(OK),
                    payload().isJson(allOf(withJsonPath("$.hearingId", is(hearingId.toString())),
                            withJsonPath("$.cases[0].caseId", is(caseId.toString())),
                            withJsonPath("$.cases[0].defendants[0].defendantId", is(defendantId.toString())),
                            hasNoJsonPath("$.cases[0].defendants[0].offences[0].convictionDate"),
                            withJsonPath("$.cases[0].defendants[0].offences[0].id", is(offenceId.toString())),
                            withJsonPath("$.cases[0].defendants[0].offences[0].plea.pleaDate", equalDate(pleaDate)),
                            withJsonPath("$.cases[0].defendants[0].offences[0].plea.value", equalEnum(PleaValueType.NOT_GUILTY))
                    )));
    }
}