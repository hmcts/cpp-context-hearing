package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.moj.cpp.hearing.it.UseCases.asDefault;
import static uk.gov.moj.cpp.hearing.it.UseCases.logEvent;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.RECORDED_LABEL_END_HEARING;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.findEventDefinitionWithActionLabel;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsAProsecutionCounsel;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.AddProsecutionCounselCommandTemplates.addProsecutionCounselCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateForMagistrates;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdateProsecutionCounselCommandTemplates.updateProsecutionCounselCommandTemplate;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.poll;

import uk.gov.justice.core.courts.ProsecutionCounsel;
import uk.gov.justice.hearing.courts.AddProsecutionCounsel;
import uk.gov.justice.hearing.courts.RemoveProsecutionCounsel;
import uk.gov.justice.hearing.courts.UpdateProsecutionCounsel;
import uk.gov.justice.services.common.http.HeaderConstants;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import com.jayway.restassured.path.json.JsonPath;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class ProsecutionCounselIT extends AbstractIT {

    private static final ZonedDateTime EVENT_TIME = PAST_ZONED_DATE_TIME.next().withZoneSameLocal(ZoneId.of("UTC"));

    private static final Logger LOGGER = LoggerFactory.getLogger(ProsecutionCounselIT.class);

    public static ProsecutionCounsel createFirstProsecutionCounsel(final InitiateHearingCommandHelper hearingOne) {
        final Utilities.EventListener publicProsecutionCounselAdded = listenFor("public.hearing.prosecution-counsel-added")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString()))));

        final AddProsecutionCounsel firstProsecutionCounselCommand = UseCases.addProsecutionCounsel(getRequestSpec(), hearingOne.getHearingId(),
                addProsecutionCounselCommandTemplate(hearingOne.getHearingId())
        );

        publicProsecutionCounselAdded.waitFor();

        ProsecutionCounsel firstProsecutionCounsel = firstProsecutionCounselCommand.getProsecutionCounsel();
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.prosecutionCounsels.[0].status", is(firstProsecutionCounsel.getStatus())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].firstName", is(firstProsecutionCounsel.getFirstName())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].lastName", is(firstProsecutionCounsel.getLastName())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].title", is(firstProsecutionCounsel.getTitle())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].middleName", is(firstProsecutionCounsel.getMiddleName())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].attendanceDays.[0]", is(firstProsecutionCounsel.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].prosecutionCases.[0]", is(firstProsecutionCounsel.getProsecutionCases().get(0).toString()))
                        )));
        return firstProsecutionCounsel;
    }

    @Test
    public void addProsecutionCounsel_shouldAdd() {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        ProsecutionCounsel firstProsecutionCounsel = createFirstProsecutionCounsel(hearingOne);

        ProsecutionCounsel secondProsecutionCounsel = createSecondProsecutionCounsel(hearingOne, firstProsecutionCounsel);


        //Adding same prosecution counsel should be ignored
        final String currentLastNameValueForFprFirstPC = firstProsecutionCounsel.getLastName();
        firstProsecutionCounsel.setLastName("DummyLastName");

        final Utilities.EventListener publicProsecutionCounselAdded = listenFor("public.hearing.prosecution-counsel-change-ignored")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString()))));

        final AddProsecutionCounsel firstProsecutionCounselReAddCommand = UseCases.addProsecutionCounsel(getRequestSpec(), hearingOne.getHearingId(),
                addProsecutionCounselCommandTemplate(hearingOne.getHearingId(), firstProsecutionCounsel)
        );

        publicProsecutionCounselAdded.waitFor();

        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.prosecutionCounsels.[0].id", is(firstProsecutionCounsel.getId().toString())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].status", is(firstProsecutionCounsel.getStatus())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].firstName", is(firstProsecutionCounsel.getFirstName())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].lastName", is(currentLastNameValueForFprFirstPC)),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].title", is(firstProsecutionCounsel.getTitle())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].middleName", is(firstProsecutionCounsel.getMiddleName())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].attendanceDays.[0]", is(firstProsecutionCounsel.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].prosecutionCases.[0]", is(firstProsecutionCounsel.getProsecutionCases().get(0).toString())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].userId", is(firstProsecutionCounsel.getUserId().toString()))
                        )));

    }

    private ProsecutionCounsel createSecondProsecutionCounsel(final InitiateHearingCommandHelper hearingOne, final ProsecutionCounsel firstProsecutionCounsel) {
        final AddProsecutionCounsel secondProsecutionCounselCommand = UseCases.addProsecutionCounsel(getRequestSpec(), hearingOne.getHearingId(),
                addProsecutionCounselCommandTemplate(hearingOne.getHearingId())
        );
        ProsecutionCounsel secondProsecutionCounsel = secondProsecutionCounselCommand.getProsecutionCounsel();
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.prosecutionCounsels.[0].status", is(firstProsecutionCounsel.getStatus())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].firstName", is(firstProsecutionCounsel.getFirstName())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].lastName", is(firstProsecutionCounsel.getLastName())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].title", is(firstProsecutionCounsel.getTitle())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].middleName", is(firstProsecutionCounsel.getMiddleName())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].attendanceDays.[0]", is(firstProsecutionCounsel.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].prosecutionCases.[0]", is(firstProsecutionCounsel.getProsecutionCases().get(0).toString())),

                                withJsonPath("$.hearing.prosecutionCounsels.[1].status", is(secondProsecutionCounsel.getStatus())),
                                withJsonPath("$.hearing.prosecutionCounsels.[1].firstName", is(secondProsecutionCounsel.getFirstName())),
                                withJsonPath("$.hearing.prosecutionCounsels.[1].lastName", is(secondProsecutionCounsel.getLastName())),
                                withJsonPath("$.hearing.prosecutionCounsels.[1].title", is(secondProsecutionCounsel.getTitle())),
                                withJsonPath("$.hearing.prosecutionCounsels.[1].middleName", is(secondProsecutionCounsel.getMiddleName())),
                                withJsonPath("$.hearing.prosecutionCounsels.[1].attendanceDays.[0]", is(secondProsecutionCounsel.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.prosecutionCounsels.[1].prosecutionCases.[0]", is(secondProsecutionCounsel.getProsecutionCases().get(0).toString()))
                        )));
        return secondProsecutionCounsel;
    }

    @Test
    public void removeProsecutionCounsel_shouldRemove() {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        final Utilities.EventListener publicProsecutionCounselAdded = listenFor("public.hearing.prosecution-counsel-added")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString()))));

        final Utilities.EventListener publicProsecutionCounselRemoved = listenFor("public.hearing.prosecution-counsel-removed")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString()))));

        ProsecutionCounsel firstProsecutionCounsel = createFirstProsecutionCounsel(hearingOne);

        publicProsecutionCounselAdded.waitFor();

        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.prosecutionCounsels", hasSize(1)))));


        //remove PC
        UseCases.removeProsecutionCounsel(getRequestSpec(), hearingOne.getHearingId(),
                new RemoveProsecutionCounsel(hearingOne.getHearingId(), firstProsecutionCounsel.getId())
        );

        publicProsecutionCounselRemoved.waitFor();

        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.prosecutionCounsels", empty()))));

    }

    @Test
    public void updateProsecutionCounsel_shouldUpdate() {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        final Utilities.EventListener publicProsecutionCounselUpdated = listenFor("public.hearing.prosecution-counsel-updated")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString()))));

        ProsecutionCounsel firstProsecutionCounsel = createFirstProsecutionCounsel(hearingOne);

        //Updating Prosecution counsel
        firstProsecutionCounsel.setFirstName("DummyFirstName");
        firstProsecutionCounsel.setLastName("DummyLastName");
        firstProsecutionCounsel.setStatus("DummyStatus");
        firstProsecutionCounsel.setTitle("UpdateTitle");
        firstProsecutionCounsel.setMiddleName("DummyMiddleName");
        firstProsecutionCounsel.setAttendanceDays(singletonList(LocalDate.now().plusDays(1)));

        final UpdateProsecutionCounsel firstProsecutionCounselUpdateCommand = UseCases.updateProsecutionCounsel(getRequestSpec(), hearingOne.getHearingId(),
                updateProsecutionCounselCommandTemplate(hearingOne.getHearingId(), firstProsecutionCounsel)
        );

        ProsecutionCounsel firstProsecutionCounselUpdated = firstProsecutionCounselUpdateCommand.getProsecutionCounsel();

        publicProsecutionCounselUpdated.waitFor();

        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.prosecutionCounsels.[0].id", is(firstProsecutionCounsel.getId().toString())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].status", is(firstProsecutionCounselUpdated.getStatus())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].firstName", is(firstProsecutionCounselUpdated.getFirstName())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].lastName", is(firstProsecutionCounselUpdated.getLastName())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].title", is(firstProsecutionCounselUpdated.getTitle())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].middleName", is(firstProsecutionCounselUpdated.getMiddleName())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].attendanceDays.[0]", is(firstProsecutionCounsel.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].prosecutionCases.[0]", is(firstProsecutionCounsel.getProsecutionCases().get(0).toString())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].userId", is(firstProsecutionCounsel.getUserId().toString()))
                        )));

    }

    @Test
    public void testUpdateProsecutionCounselWhenProsecutionCounselIsRemovedThenProsecutionCounselShouldNotBeUpdated() {
        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        ProsecutionCounsel firstProsecutionCounsel = createFirstProsecutionCounsel(hearingOne);

        ProsecutionCounsel secondProsecutionCounsel = createSecondProsecutionCounsel(hearingOne, firstProsecutionCounsel);


        UseCases.removeProsecutionCounsel(getRequestSpec(), hearingOne.getHearingId(),
                new RemoveProsecutionCounsel(hearingOne.getHearingId(), firstProsecutionCounsel.getId())
        );

        firstProsecutionCounsel.setLastName("DummyLastName");
        final UpdateProsecutionCounsel firstProsecutionCounselUpdatedCommand = UseCases.updateProsecutionCounsel(getRequestSpec(), hearingOne.getHearingId(),
                updateProsecutionCounselCommandTemplate(hearingOne.getHearingId(), firstProsecutionCounsel)
        );
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.prosecutionCounsels", hasSize(1)),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].status", is(secondProsecutionCounsel.getStatus())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].firstName", is(secondProsecutionCounsel.getFirstName())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].lastName", is(secondProsecutionCounsel.getLastName())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].title", is(secondProsecutionCounsel.getTitle())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].middleName", is(secondProsecutionCounsel.getMiddleName())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].attendanceDays.[0]", is(secondProsecutionCounsel.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].prosecutionCases.[0]", is(secondProsecutionCounsel.getProsecutionCases().get(0).toString())))));

    }

    private void createSecondProsecutionCounsel(final InitiateHearingCommandHelper hearingOne) {
        final AddProsecutionCounsel secondProsecutionCounselCommand = UseCases.addProsecutionCounsel(getRequestSpec(), hearingOne.getHearingId(),
                addProsecutionCounselCommandTemplate(hearingOne.getHearingId())
        );
        ProsecutionCounsel secondProsecutionCounsel = secondProsecutionCounselCommand.getProsecutionCounsel();
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.prosecutionCounsels", hasSize(1)),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].status", is(secondProsecutionCounsel.getStatus())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].firstName", is(secondProsecutionCounsel.getFirstName())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].lastName", is(secondProsecutionCounsel.getLastName())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].title", is(secondProsecutionCounsel.getTitle())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].middleName", is(secondProsecutionCounsel.getMiddleName())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].attendanceDays.[0]", is(secondProsecutionCounsel.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.prosecutionCounsels.[0].prosecutionCases.[0]", is(secondProsecutionCounsel.getProsecutionCases().get(0).toString()))
                        )));
    }

    @Test
    public void addProsecutionCounsel_failedCheckin_SPICases_whereCaseURNisPopulated() {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        givenAUserHasLoggedInAsAProsecutionCounsel(randomUUID());

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel(RECORDED_LABEL_END_HEARING);

        final LogEventCommand logEventCommand = logEvent(randomUUID(), requestSpec, asDefault(), hearingOne.it(),
                hearingEventDefinition.getId(), false, randomUUID(), EVENT_TIME, RECORDED_LABEL_END_HEARING);

        //Add Prosecution Counsel
        final Utilities.EventListener publicProsecutionCounselAdded = listenFor("public.hearing.prosecution-counsel-change-ignored")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString()))));

        final AddProsecutionCounsel firstProsecutionCounselReAddCommand = UseCases.addProsecutionCounsel(getRequestSpec(), hearingOne.getHearingId(),
                addProsecutionCounselCommandTemplate(hearingOne.getHearingId())
        );

        JsonPath jsonPath = publicProsecutionCounselAdded.waitFor();
        String caseURN = hearingOne.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN();

        assertThat(jsonPath.getString("caseURN"), is(caseURN));
        assertThat(jsonPath.getString("prosecutionAuthorityReference"), isEmptyOrNullString());
    }

    @Test
    public void addProsecutionCounsel_failedCheckin_SJPCases_wherePARisPopulated() {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), initiateHearingTemplateForMagistrates()));

        givenAUserHasLoggedInAsAProsecutionCounsel(randomUUID());

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel(RECORDED_LABEL_END_HEARING);

        final LogEventCommand logEventCommand = logEvent(randomUUID(), getRequestSpec(), asDefault(), hearingOne.it(),
                hearingEventDefinition.getId(), false, randomUUID(), EVENT_TIME, RECORDED_LABEL_END_HEARING);

        //Add Prosecution Counsel
        final Utilities.EventListener publicProsecutionCounselAdded = listenFor("public.hearing.prosecution-counsel-change-ignored")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString()))));

        final AddProsecutionCounsel firstProsecutionCounselReAddCommand = UseCases.addProsecutionCounsel(getRequestSpec(), hearingOne.getHearingId(),
                addProsecutionCounselCommandTemplate(hearingOne.getHearingId())
        );

        JsonPath jsonPath = publicProsecutionCounselAdded.waitFor();

        String caseURN = hearingOne.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN();
        String prosecutionAuthorityReference = hearingOne.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityReference();

        assertThat(jsonPath.getString("caseURN"), is(prosecutionAuthorityReference));
        assertThat(caseURN, isEmptyOrNullString());
    }

}
