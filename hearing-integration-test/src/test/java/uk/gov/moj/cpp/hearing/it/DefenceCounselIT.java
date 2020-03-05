package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
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
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsADefenceCounsel;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.AddDefenceCounselCommandTemplates.addDefenceCounselCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.AddDefenceCounselCommandTemplates.addDefenceCounselCommandTemplateWithoutMiddleName;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateForMagistrates;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdateDefenceCounselCommandTemplates.updateDefenceCounselCommandTemplate;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.poll;

import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.hearing.courts.AddDefenceCounsel;
import uk.gov.justice.hearing.courts.RemoveDefenceCounsel;
import uk.gov.justice.hearing.courts.UpdateDefenceCounsel;
import uk.gov.justice.services.common.http.HeaderConstants;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.jayway.restassured.path.json.JsonPath;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class DefenceCounselIT extends AbstractIT {

    private static final ZonedDateTime EVENT_TIME = PAST_ZONED_DATE_TIME.next().withZoneSameLocal(ZoneId.of("UTC"));

    public static DefenceCounsel createFirstDefenceCounsel(final InitiateHearingCommandHelper hearingOne) {
        final Utilities.EventListener publicDefenceCounselAdded = listenFor("public.hearing.defence-counsel-added")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString()))));

        final AddDefenceCounsel firstDefenceCounselCommand = UseCases.addDefenceCounsel(getRequestSpec(), hearingOne.getHearingId(),
                addDefenceCounselCommandTemplate(hearingOne.getHearingId())
        );

        publicDefenceCounselAdded.waitFor();
        DefenceCounsel firstDefenceCounsel = firstDefenceCounselCommand.getDefenceCounsel();

        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.defenceCounsels.[0].status", is(firstDefenceCounsel.getStatus())),
                                withJsonPath("$.hearing.defenceCounsels.[0].firstName", is(firstDefenceCounsel.getFirstName())),
                                withJsonPath("$.hearing.defenceCounsels.[0].lastName", is(firstDefenceCounsel.getLastName())),
                                withJsonPath("$.hearing.defenceCounsels.[0].title", is(firstDefenceCounsel.getTitle())),
                                withJsonPath("$.hearing.defenceCounsels.[0].middleName", is(firstDefenceCounsel.getMiddleName())),
                                withJsonPath("$.hearing.defenceCounsels.[0].attendanceDays.[0]", is(firstDefenceCounsel.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.defenceCounsels.[0].defendants.[0]", is(firstDefenceCounsel.getDefendants().get(0).toString()))
                        )));
        return firstDefenceCounsel;
    }

    @Test
    public void addDefenceCounsel_shouldAdd() throws Exception {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        DefenceCounsel firstDefenceCounsel = createFirstDefenceCounsel(hearingOne);

        DefenceCounsel secondDefenceCounsel = createSecondDefenceCounsel(hearingOne, firstDefenceCounsel);


        //Adding same defence counsel should be ignored
        final String currentLastNameValueForFprFirstPC = firstDefenceCounsel.getLastName();
        firstDefenceCounsel.setLastName("DummyLastName");

        final Utilities.EventListener publicDefenceCounselAdded = listenFor("public.hearing.defence-counsel-change-ignored")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString()))));

        final AddDefenceCounsel firstProsecutionCounselReAddCommand = UseCases.addDefenceCounsel(getRequestSpec(), hearingOne.getHearingId(),
                addDefenceCounselCommandTemplate(hearingOne.getHearingId(), firstDefenceCounsel)
        );

        publicDefenceCounselAdded.waitFor();

        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.defenceCounsels.[0].id", is(firstDefenceCounsel.getId().toString())),
                                withJsonPath("$.hearing.defenceCounsels.[0].status", is(firstDefenceCounsel.getStatus())),
                                withJsonPath("$.hearing.defenceCounsels.[0].firstName", is(firstDefenceCounsel.getFirstName())),
                                withJsonPath("$.hearing.defenceCounsels.[0].lastName", is(currentLastNameValueForFprFirstPC)),
                                withJsonPath("$.hearing.defenceCounsels.[0].title", is(firstDefenceCounsel.getTitle())),
                                withJsonPath("$.hearing.defenceCounsels.[0].middleName", is(firstDefenceCounsel.getMiddleName())),
                                withJsonPath("$.hearing.defenceCounsels.[0].attendanceDays.[0]", is(firstDefenceCounsel.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.defenceCounsels.[0].defendants.[0]", is(firstDefenceCounsel.getDefendants().get(0).toString())),
                                withJsonPath("$.hearing.defenceCounsels.[0].userId", is(firstDefenceCounsel.getUserId().toString()))
                        )));

    }

    @Test
    public void removeDefenceCounsel_shouldRemove() throws Exception {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        final AddDefenceCounsel firstDefenceCounselCommand = UseCases.addDefenceCounsel(getRequestSpec(), hearingOne.getHearingId(),
                addDefenceCounselCommandTemplateWithoutMiddleName(hearingOne.getHearingId())
        );
        DefenceCounsel firstDefenceCounsel = firstDefenceCounselCommand.getDefenceCounsel();
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.defenceCounsels.[0].status", is(firstDefenceCounsel.getStatus())),
                                withJsonPath("$.hearing.defenceCounsels.[0].firstName", is(firstDefenceCounsel.getFirstName())),
                                withJsonPath("$.hearing.defenceCounsels.[0].lastName", is(firstDefenceCounsel.getLastName())),
                                withJsonPath("$.hearing.defenceCounsels.[0].title", is(firstDefenceCounsel.getTitle())),
                                withoutJsonPath("$.hearing.defenceCounsels.[0].middleName"),
                                withJsonPath("$.hearing.defenceCounsels.[0].attendanceDays.[0]", is(firstDefenceCounsel.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.defenceCounsels.[0].defendants.[0]", is(firstDefenceCounsel.getDefendants().get(0).toString()))
                        )));

        //remove first DC
        UseCases.removeDefenceCounsel(getRequestSpec(), hearingOne.getHearingId(),
                new RemoveDefenceCounsel(hearingOne.getHearingId(), firstDefenceCounsel.getId())
        );
        final AddDefenceCounsel secondDefenceCounselCommand = UseCases.addDefenceCounsel(getRequestSpec(), hearingOne.getHearingId(),
                addDefenceCounselCommandTemplate(hearingOne.getHearingId())
        );
        DefenceCounsel secondDefenceCounsel = secondDefenceCounselCommand.getDefenceCounsel();
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.defenceCounsels", hasSize(1)),
                                withJsonPath("$.hearing.defenceCounsels.[0].status", is(secondDefenceCounsel.getStatus())),
                                withJsonPath("$.hearing.defenceCounsels.[0].firstName", is(secondDefenceCounsel.getFirstName())),
                                withJsonPath("$.hearing.defenceCounsels.[0].lastName", is(secondDefenceCounsel.getLastName())),
                                withJsonPath("$.hearing.defenceCounsels.[0].title", is(secondDefenceCounsel.getTitle())),
                                withJsonPath("$.hearing.defenceCounsels.[0].middleName", is(secondDefenceCounsel.getMiddleName())),
                                withJsonPath("$.hearing.defenceCounsels.[0].attendanceDays.[0]", is(secondDefenceCounsel.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.defenceCounsels.[0].defendants.[0]", is(secondDefenceCounsel.getDefendants().get(0).toString()))
                        )));


    }

    @Test
    public void updateDefenceCounsel_shouldUpdate() throws Exception {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        DefenceCounsel firstDefenceCounsel = createFirstDefenceCounsel(hearingOne);


        //Updating defence counsel
        firstDefenceCounsel.setFirstName("DummyFirstName");
        firstDefenceCounsel.setLastName("DummyLastName");
        firstDefenceCounsel.setStatus("DummyStatus");
        firstDefenceCounsel.setTitle("UpdateTitle");
        firstDefenceCounsel.setMiddleName("DummyMiddleName");
        firstDefenceCounsel.setAttendanceDays(Arrays.asList(LocalDate.now().plusDays(1)));

        final UpdateDefenceCounsel firstDefenceCounselReAddCommand = UseCases.updateDefenceCounsel(getRequestSpec(), hearingOne.getHearingId(),
                updateDefenceCounselCommandTemplate(hearingOne.getHearingId(), firstDefenceCounsel)
        );

        DefenceCounsel firstDefenceCounselUpdated = firstDefenceCounselReAddCommand.getDefenceCounsel();
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.defenceCounsels.[0].id", is(firstDefenceCounsel.getId().toString())),
                                withJsonPath("$.hearing.defenceCounsels.[0].status", is(firstDefenceCounselUpdated.getStatus())),
                                withJsonPath("$.hearing.defenceCounsels.[0].firstName", is(firstDefenceCounselUpdated.getFirstName())),
                                withJsonPath("$.hearing.defenceCounsels.[0].lastName", is(firstDefenceCounselUpdated.getLastName())),
                                withJsonPath("$.hearing.defenceCounsels.[0].title", is(firstDefenceCounselUpdated.getTitle())),
                                withJsonPath("$.hearing.defenceCounsels.[0].middleName", is(firstDefenceCounselUpdated.getMiddleName())),
                                withJsonPath("$.hearing.defenceCounsels.[0].attendanceDays.[0]", is(firstDefenceCounsel.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.defenceCounsels.[0].defendants.[0]", is(firstDefenceCounsel.getDefendants().get(0).toString())),
                                withJsonPath("$.hearing.defenceCounsels.[0].userId", is(firstDefenceCounsel.getUserId().toString()))
                        )));

    }

    @Test
    public void testUpdateDefenceCounselWhenDefenceCounselIsRemovedThenDefenceCounselShouldNotBeUpdated() throws Exception {
        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        DefenceCounsel firstDefenceCounsel = createFirstDefenceCounsel(hearingOne);

        DefenceCounsel secondDefenceCounsel = createSecondDefenceCounsel(hearingOne, firstDefenceCounsel);


        UseCases.removeDefenceCounsel(getRequestSpec(), hearingOne.getHearingId(),
                new RemoveDefenceCounsel(hearingOne.getHearingId(), firstDefenceCounsel.getId())
        );

        firstDefenceCounsel.setLastName("DummyLastName");
        final UpdateDefenceCounsel firstDefenceCounselUpdatedCommand = UseCases.updateDefenceCounsel(getRequestSpec(), hearingOne.getHearingId(),
                updateDefenceCounselCommandTemplate(hearingOne.getHearingId(), firstDefenceCounsel)
        );
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.defenceCounsels", hasSize(1)),
                                withJsonPath("$.hearing.defenceCounsels.[0].status", is(secondDefenceCounsel.getStatus())),
                                withJsonPath("$.hearing.defenceCounsels.[0].firstName", is(secondDefenceCounsel.getFirstName())),
                                withJsonPath("$.hearing.defenceCounsels.[0].lastName", is(secondDefenceCounsel.getLastName())),
                                withJsonPath("$.hearing.defenceCounsels.[0].title", is(secondDefenceCounsel.getTitle())),
                                withJsonPath("$.hearing.defenceCounsels.[0].middleName", is(secondDefenceCounsel.getMiddleName())),
                                withJsonPath("$.hearing.defenceCounsels.[0].attendanceDays.[0]", is(secondDefenceCounsel.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.defenceCounsels.[0].defendants.[0]", is(secondDefenceCounsel.getDefendants().get(0).toString())))));


    }

    private DefenceCounsel createSecondDefenceCounsel(final InitiateHearingCommandHelper hearingOne, final DefenceCounsel firstDefenceCounsel) {
        final AddDefenceCounsel secondDefenceCounselCommand = UseCases.addDefenceCounsel(getRequestSpec(), hearingOne.getHearingId(),
                addDefenceCounselCommandTemplate(hearingOne.getHearingId())
        );
        DefenceCounsel secondDefenceCounsel = secondDefenceCounselCommand.getDefenceCounsel();
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.defenceCounsels.[0].status", is(firstDefenceCounsel.getStatus())),
                                withJsonPath("$.hearing.defenceCounsels.[0].firstName", is(firstDefenceCounsel.getFirstName())),
                                withJsonPath("$.hearing.defenceCounsels.[0].lastName", is(firstDefenceCounsel.getLastName())),
                                withJsonPath("$.hearing.defenceCounsels.[0].title", is(firstDefenceCounsel.getTitle())),
                                withJsonPath("$.hearing.defenceCounsels.[0].middleName", is(firstDefenceCounsel.getMiddleName())),
                                withJsonPath("$.hearing.defenceCounsels.[0].attendanceDays.[0]", is(firstDefenceCounsel.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.defenceCounsels.[0].defendants.[0]", is(firstDefenceCounsel.getDefendants().get(0).toString())),

                                withJsonPath("$.hearing.defenceCounsels.[1].status", is(secondDefenceCounsel.getStatus())),
                                withJsonPath("$.hearing.defenceCounsels.[1].firstName", is(secondDefenceCounsel.getFirstName())),
                                withJsonPath("$.hearing.defenceCounsels.[1].lastName", is(secondDefenceCounsel.getLastName())),
                                withJsonPath("$.hearing.defenceCounsels.[1].title", is(secondDefenceCounsel.getTitle())),
                                withJsonPath("$.hearing.defenceCounsels.[1].middleName", is(secondDefenceCounsel.getMiddleName())),
                                withJsonPath("$.hearing.defenceCounsels.[1].attendanceDays.[0]", is(secondDefenceCounsel.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.defenceCounsels.[1].defendants.[0]", is(secondDefenceCounsel.getDefendants().get(0).toString()))
                        )));
        return secondDefenceCounsel;
    }

    @Test
    public void testUpdateDefenceCounselWithPreviouslySetValues() throws Exception {
        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        DefenceCounsel firstDefenceCounsel = createFirstDefenceCounsel(hearingOne);

        String tempFirstDefenceCounsel = firstDefenceCounsel.getFirstName();

        //Updating defence counsel first time
        firstDefenceCounsel.setFirstName("DummyFirstName");
        firstDefenceCounsel.setLastName("DummyLastName");
        firstDefenceCounsel.setStatus("DummyStatus");
        firstDefenceCounsel.setTitle("UpdateTitle");
        firstDefenceCounsel.setMiddleName("DummyMiddleName");
        firstDefenceCounsel.setAttendanceDays(Arrays.asList(LocalDate.now().plusDays(1)));

        final UpdateDefenceCounsel firstDefenceCounselReAddCommand = UseCases.updateDefenceCounsel(getRequestSpec(), hearingOne.getHearingId(),
                updateDefenceCounselCommandTemplate(hearingOne.getHearingId(), firstDefenceCounsel)
        );

        DefenceCounsel firstDefenceCounselUpdated = firstDefenceCounselReAddCommand.getDefenceCounsel();
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.defenceCounsels.[0].id", is(firstDefenceCounselUpdated.getId().toString())),
                                withJsonPath("$.hearing.defenceCounsels.[0].status", is(firstDefenceCounselUpdated.getStatus())),
                                withJsonPath("$.hearing.defenceCounsels.[0].firstName", is(firstDefenceCounselUpdated.getFirstName())),
                                withJsonPath("$.hearing.defenceCounsels.[0].lastName", is(firstDefenceCounselUpdated.getLastName())),
                                withJsonPath("$.hearing.defenceCounsels.[0].title", is(firstDefenceCounselUpdated.getTitle())),
                                withJsonPath("$.hearing.defenceCounsels.[0].middleName", is(firstDefenceCounselUpdated.getMiddleName())),
                                withJsonPath("$.hearing.defenceCounsels.[0].attendanceDays.[0]", is(firstDefenceCounselUpdated.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.defenceCounsels.[0].defendants.[0]", is(firstDefenceCounselUpdated.getDefendants().get(0).toString()))
                        )));
        //UpdateFirstDefenceCounsel second time with Original values
        firstDefenceCounselUpdated.setFirstName(tempFirstDefenceCounsel);

        final UpdateDefenceCounsel thirdTimeUpdateCommand = UseCases.updateDefenceCounsel(getRequestSpec(), hearingOne.getHearingId(),
                updateDefenceCounselCommandTemplate(hearingOne.getHearingId(), firstDefenceCounselUpdated));


        DefenceCounsel thirdUpdateWithFirst = thirdTimeUpdateCommand.getDefenceCounsel();

        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.defenceCounsels.[0].id", is(firstDefenceCounselUpdated.getId().toString())),
                                withJsonPath("$.hearing.defenceCounsels.[0].status", is(firstDefenceCounselUpdated.getStatus())),
                                withJsonPath("$.hearing.defenceCounsels.[0].firstName", is(thirdUpdateWithFirst.getFirstName())),
                                withJsonPath("$.hearing.defenceCounsels.[0].lastName", is(firstDefenceCounselUpdated.getLastName())),
                                withJsonPath("$.hearing.defenceCounsels.[0].title", is(firstDefenceCounselUpdated.getTitle())),
                                withJsonPath("$.hearing.defenceCounsels.[0].middleName", is(firstDefenceCounselUpdated.getMiddleName())),
                                withJsonPath("$.hearing.defenceCounsels.[0].attendanceDays.[0]", is(firstDefenceCounselUpdated.getAttendanceDays().get(0).toString())),
                                withJsonPath("$.hearing.defenceCounsels.[0].defendants.[0]", is(firstDefenceCounselUpdated.getDefendants().get(0).toString()))
                        )));
    }

    @Test
    public void addDefenceCounsel_failedCheckin_SPICases_whereCaseURNisPopulated() throws Exception {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        givenAUserHasLoggedInAsADefenceCounsel(randomUUID());

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel(RECORDED_LABEL_END_HEARING);

        final LogEventCommand logEventCommand = logEvent(randomUUID(), getRequestSpec(), asDefault(), hearingOne.it(),
                hearingEventDefinition.getId(), false, randomUUID(), EVENT_TIME, RECORDED_LABEL_END_HEARING);

        //Add Defence Counsel
        final Utilities.EventListener publicDefenceCounselAdded = listenFor("public.hearing.defence-counsel-change-ignored")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString()))));

        final AddDefenceCounsel firstDefenceCounselCommand = UseCases.addDefenceCounsel(getRequestSpec(), hearingOne.getHearingId(),
                addDefenceCounselCommandTemplate(hearingOne.getHearingId())
        );

        JsonPath jsonPath = publicDefenceCounselAdded.waitFor();
        String caseURN = hearingOne.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN();

        assertThat(jsonPath.getString("caseURN"), is(caseURN));
        assertThat(jsonPath.getString("prosecutionAuthorityReference"), isEmptyOrNullString());
    }

    @Test
    public void addDefenceCounsel_failedCheckin_SJPCases_wherePARisPopulated() throws Exception {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), initiateHearingTemplateForMagistrates()));

        givenAUserHasLoggedInAsADefenceCounsel(randomUUID());

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel(RECORDED_LABEL_END_HEARING);

        final LogEventCommand logEventCommand = logEvent(randomUUID(),requestSpec, asDefault(), hearingOne.it(),
                hearingEventDefinition.getId(), false, randomUUID(), EVENT_TIME, RECORDED_LABEL_END_HEARING);

        //Add Defence Counsel
        final Utilities.EventListener publicDefenceCounselAdded = listenFor("public.hearing.defence-counsel-change-ignored")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString()))));

        final AddDefenceCounsel firstDefenceCounselCommand = UseCases.addDefenceCounsel(getRequestSpec(), hearingOne.getHearingId(),
                addDefenceCounselCommandTemplate(hearingOne.getHearingId())
        );

        JsonPath jsonPath = publicDefenceCounselAdded.waitFor();
        String caseURN = hearingOne.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN();
        String prosecutionAuthorityReference = hearingOne.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityReference();

        assertThat(jsonPath.getString("caseURN"), is(prosecutionAuthorityReference));
        assertThat(caseURN, isEmptyOrNullString());
    }
}