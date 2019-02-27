package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.AddProsecutionCounselCommandTemplates.addProsecutionCounselCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdateProsecutionCounselCommandTemplates.updateProsecutionCounselCommandTemplate;

import uk.gov.justice.hearing.courts.AddProsecutionCounsel;
import uk.gov.justice.hearing.courts.RemoveProsecutionCounsel;
import uk.gov.justice.hearing.courts.UpdateProsecutionCounsel;
import uk.gov.justice.core.courts.ProsecutionCounsel;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

@SuppressWarnings("unchecked")
public class ProsecutionCounselIT extends AbstractIT {

    @Test
    public void addProsecutionCounsel_shouldAdd() throws Exception {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        ProsecutionCounsel firstProsecutionCounsel = createFirstProsecutionCounsel(hearingOne);

        ProsecutionCounsel secondProsecutionCounsel = createSecondProsecutionCounsel(hearingOne, firstProsecutionCounsel);


        //Adding same prosecution counsel should be ignored
        final String currentLastNameValueForFprFirstPC = firstProsecutionCounsel.getLastName();
        firstProsecutionCounsel.setLastName("DummyLastName");
        final AddProsecutionCounsel firstProsecutionCounselReAddCommand = UseCases.addProsecutionCounsel(requestSpec, hearingOne.getHearingId(),
                addProsecutionCounselCommandTemplate(hearingOne.getHearingId(), firstProsecutionCounsel)
        );
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .timeout(30, TimeUnit.SECONDS)
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
                                withJsonPath("$.hearing.prosecutionCounsels.[0].prosecutionCases.[0]", is(firstProsecutionCounsel.getProsecutionCases().get(0).toString()))
                        )));

    }

    private ProsecutionCounsel createSecondProsecutionCounsel(final InitiateHearingCommandHelper hearingOne, final ProsecutionCounsel firstProsecutionCounsel) {
        final AddProsecutionCounsel secondProsecutionCounselCommand = UseCases.addProsecutionCounsel(requestSpec, hearingOne.getHearingId(),
                addProsecutionCounselCommandTemplate(hearingOne.getHearingId())
        );
        ProsecutionCounsel secondProsecutionCounsel = secondProsecutionCounselCommand.getProsecutionCounsel();
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
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

    public static ProsecutionCounsel createFirstProsecutionCounsel(final InitiateHearingCommandHelper hearingOne) {
        final AddProsecutionCounsel firstProsecutionCounselCommand = UseCases.addProsecutionCounsel(requestSpec, hearingOne.getHearingId(),
                addProsecutionCounselCommandTemplate(hearingOne.getHearingId())
        );
        ProsecutionCounsel firstProsecutionCounsel = firstProsecutionCounselCommand.getProsecutionCounsel();
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .timeout(30, TimeUnit.SECONDS)
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
    public void removeProsecutionCounsel_shouldRemove() throws Exception {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        ProsecutionCounsel firstProsecutionCounsel = createFirstProsecutionCounsel(hearingOne);
        //remove first PC
        UseCases.removeProsecutionCounsel(requestSpec, hearingOne.getHearingId(),
                new RemoveProsecutionCounsel(hearingOne.getHearingId(), firstProsecutionCounsel.getId())
        );
        createSecondProsecutionCounsel(hearingOne);


    }

    @Test
    public void updateProsecutionCounsel_shouldUpdate() throws Exception {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        ProsecutionCounsel firstProsecutionCounsel = createFirstProsecutionCounsel(hearingOne);


        //Updating Prosecution counsel
        firstProsecutionCounsel.setFirstName("DummyFirstName");
        firstProsecutionCounsel.setLastName("DummyLastName");
        firstProsecutionCounsel.setStatus("DummyStatus");
        firstProsecutionCounsel.setTitle("UpdateTitle");
        firstProsecutionCounsel.setMiddleName("DummyMiddleName");
        firstProsecutionCounsel.setAttendanceDays(Arrays.asList(LocalDate.now().plusDays(1)));

        final UpdateProsecutionCounsel firstProsecutionCounselUpdateCommand = UseCases.updateProsecutionCounsel(requestSpec, hearingOne.getHearingId(),
                updateProsecutionCounselCommandTemplate(hearingOne.getHearingId(), firstProsecutionCounsel)
        );

        ProsecutionCounsel firstProsecutionCounselUpdated = firstProsecutionCounselUpdateCommand.getProsecutionCounsel();
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .timeout(30, TimeUnit.SECONDS)
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
                                withJsonPath("$.hearing.prosecutionCounsels.[0].prosecutionCases.[0]", is(firstProsecutionCounsel.getProsecutionCases().get(0).toString()))
                        )));

    }

    @Test
    public void testUpdateProsecutionCounselWhenProsecutionCounselIsRemovedThenProsecutionCounselShouldNotBeUpdated() throws Exception {
        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        ProsecutionCounsel firstProsecutionCounsel = createFirstProsecutionCounsel(hearingOne);

        ProsecutionCounsel secondProsecutionCounsel = createSecondProsecutionCounsel(hearingOne, firstProsecutionCounsel);


        UseCases.removeProsecutionCounsel(requestSpec, hearingOne.getHearingId(),
                new RemoveProsecutionCounsel(hearingOne.getHearingId(), firstProsecutionCounsel.getId())
        );

        firstProsecutionCounsel.setLastName("DummyLastName");
        final UpdateProsecutionCounsel firstProsecutionCounselUpdatedCommand = UseCases.updateProsecutionCounsel(requestSpec, hearingOne.getHearingId(),
                updateProsecutionCounselCommandTemplate(hearingOne.getHearingId(), firstProsecutionCounsel)
        );
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
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
        final AddProsecutionCounsel secondProsecutionCounselCommand = UseCases.addProsecutionCounsel(requestSpec, hearingOne.getHearingId(),
                addProsecutionCounselCommandTemplate(hearingOne.getHearingId())
        );
        ProsecutionCounsel secondProsecutionCounsel = secondProsecutionCounselCommand.getProsecutionCounsel();
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
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
}
