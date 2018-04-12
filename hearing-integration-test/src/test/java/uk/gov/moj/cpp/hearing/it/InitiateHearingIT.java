package uk.gov.moj.cpp.hearing.it;


import org.junit.Test;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.plea.HearingUpdatePleaCommand;
import uk.gov.moj.cpp.hearing.command.plea.Plea;

import java.text.MessageFormat;
import java.util.UUID;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.makeCommand;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.initiateHearingCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.initiateHearingCommandTemplateWithOnlyMandatoryFields;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.updatePleaTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

public class InitiateHearingIT extends AbstractIT {

    @Test
    public void initiateHearing_withOnlyMandatoryFields(){

        InitiateHearingCommand initiateHearing = initiateHearingCommandTemplateWithOnlyMandatoryFields().build();

        final Hearing hearing = initiateHearing.getHearing();

        TestUtilities.EventListener publicEventTopic = listenFor("public.hearing.initiated")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearing.getId().toString()))));

        makeCommand(requestSpec, "hearing.initiate")
                .ofType("application/vnd.hearing.initiate+json")
                .withPayload(initiateHearing)
                .executeSuccessfully();

        publicEventTopic.waitFor();

        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"), initiateHearing.getHearing().getId());
        final String url = getBaseUri() + "/" + queryAPIEndPoint;

        final String responseType = "application/vnd.hearing.get.hearing.v2+json";

        poll(requestParams(url, responseType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(initiateHearing.getHearing().getId().toString())),
                                withJsonPath("$.hearingType", equalStr(hearing, "type")),
                                withJsonPath("$.courtCentreName", equalStr(hearing, "courtCentreName")),
                                withJsonPath("$.roomName", equalStr(hearing, "courtRoomName")),
                                withJsonPath("$.roomId", equalStr(hearing, "courtRoomId")),
                                withJsonPath("$.courtCentreId", equalStr(hearing, "courtCentreId")),
                                withJsonPath("$.judge.id", equalStr(hearing, "judge.id")),
                                withJsonPath("$.judge.title", equalStr(hearing, "judge.title")),
                                withJsonPath("$.judge.firstName", equalStr(hearing, "judge.firstName")),
                                withJsonPath("$.judge.lastName", equalStr(hearing, "judge.lastName")),
                                withJsonPath("$.cases[0].caseId", equalStr(initiateHearing, "cases[0].caseId")),
                                withJsonPath("$.cases[0].caseUrn", equalStr(initiateHearing, "cases[0].urn")),
                                withJsonPath("$.cases[0].defendants[0].defendantId", equalStr(hearing, "defendants[0].id")),
                                withJsonPath("$.cases[0].defendants[0].firstName", equalStr(hearing, "defendants[0].firstName")),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id", equalStr(hearing, "defendants[0].offences[0].id"))
                        )));

    }

    @Test
    public void initiateHearing_shouldInitiateHearing_whenInitiateHearingCommandIsMade() {

        InitiateHearingCommand initiateHearing = initiateHearingCommandTemplate().build();

        final Hearing hearing = initiateHearing.getHearing();

        TestUtilities.EventListener publicEventTopic = listenFor("public.hearing.initiated")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearing.getId().toString()))));

        makeCommand(requestSpec, "hearing.initiate")
                .ofType("application/vnd.hearing.initiate+json")
                .withPayload(initiateHearing)
                .executeSuccessfully();

        publicEventTopic.waitFor();

        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"), initiateHearing.getHearing().getId());
        final String url = getBaseUri() + "/" + queryAPIEndPoint;

        final String responseType = "application/vnd.hearing.get.hearing.v2+json";

        poll(requestParams(url, responseType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(initiateHearing.getHearing().getId().toString())),
                                withJsonPath("$.hearingType", equalStr(hearing, "type")),
                                withJsonPath("$.courtCentreName", equalStr(hearing, "courtCentreName")),
                                withJsonPath("$.roomName", equalStr(hearing, "courtRoomName")),
                                withJsonPath("$.roomId", equalStr(hearing, "courtRoomId")),
                                withJsonPath("$.courtCentreId", equalStr(hearing, "courtCentreId")),
                                withJsonPath("$.judge.id", equalStr(hearing, "judge.id")),
                                withJsonPath("$.judge.title", equalStr(hearing, "judge.title")),
                                withJsonPath("$.judge.firstName", equalStr(hearing, "judge.firstName")),
                                withJsonPath("$.judge.lastName", equalStr(hearing, "judge.lastName")),
                                withJsonPath("$.cases[0].caseId", equalStr(initiateHearing, "cases[0].caseId")),
                                withJsonPath("$.cases[0].caseUrn", equalStr(initiateHearing, "cases[0].urn")),
                                withJsonPath("$.cases[0].defendants[0].defendantId", equalStr(hearing, "defendants[0].id")),
                                withJsonPath("$.cases[0].defendants[0].firstName", equalStr(hearing, "defendants[0].firstName")),
                                withJsonPath("$.cases[0].defendants[0].lastName", equalStr(hearing, "defendants[0].lastName")),
                                withJsonPath("$.cases[0].defendants[0].address.formattedAddress", equalStr(hearing, ' ', "defendants[0].address.address1", "defendants[0].address.address2", "defendants[0].address.address3", "defendants[0].address.address4", "defendants[0].address.postCode")),
                                withJsonPath("$.cases[0].defendants[0].address.address1", equalStr(hearing, "defendants[0].address.address1")),
                                withJsonPath("$.cases[0].defendants[0].address.address2", equalStr(hearing, "defendants[0].address.address2")),
                                withJsonPath("$.cases[0].defendants[0].address.address3", equalStr(hearing, "defendants[0].address.address3")),
                                withJsonPath("$.cases[0].defendants[0].address.address4", equalStr(hearing, "defendants[0].address.address4")),
                                withJsonPath("$.cases[0].defendants[0].address.postCode", equalStr(hearing, "defendants[0].address.postCode")),
                                withJsonPath("$.cases[0].defendants[0].dateOfBirth", equalStr(hearing, "defendants[0].dateOfBirth", ISO_LOCAL_DATE)),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id", equalStr(hearing, "defendants[0].offences[0].id")),
                                withJsonPath("$.cases[0].defendants[0].offences[0].count", equalInt(hearing, "defendants[0].offences[0].count")),
                                withJsonPath("$.cases[0].defendants[0].offences[0].wording", equalStr(hearing, "defendants[0].offences[0].wording")),
                                withJsonPath("$.cases[0].defendants[0].offences[0].title", equalStr(hearing, "defendants[0].offences[0].title")),
                                withJsonPath("$.cases[0].defendants[0].offences[0].legislation", equalStr(hearing, "defendants[0].offences[0].legislation")),
                                withJsonPath("$.cases[0].defendants[0].bailStatus", is(hearing.getDefendants().get(0).getDefendantCases().get(0).getBailStatus())),

                                withJsonPath("$.cases[0].witnesses[0].type", equalStr(hearing, "witnesses[0].type")),
                                withJsonPath("$.cases[0].witnesses[0].classification", equalStr(hearing, "witnesses[0].classification")),
                                withJsonPath("$.cases[0].witnesses[0].caseId", equalStr(hearing, "witnesses[0].caseId")),
                                withJsonPath("$.cases[0].witnesses[0].title", equalStr(hearing, "witnesses[0].title")),
                                withJsonPath("$.cases[0].witnesses[0].firstName", equalStr(hearing, "witnesses[0].firstName")),
                                withJsonPath("$.cases[0].witnesses[0].lastName", equalStr(hearing, "witnesses[0].lastName")),
                                withJsonPath("$.cases[0].witnesses[0].nationality", equalStr(hearing, "witnesses[0].nationality")),
                                withJsonPath("$.cases[0].witnesses[0].dateOfBirth", equalStr(hearing, "witnesses[0].dateOfBirth", ISO_LOCAL_DATE)),
                                withJsonPath("$.cases[0].witnesses[0].gender", equalStr(hearing, "witnesses[0].gender")),
                                withJsonPath("$.cases[0].witnesses[0].homeTelephone", equalStr(hearing, "witnesses[0].homeTelephone")),
                                withJsonPath("$.cases[0].witnesses[0].workTelephone", equalStr(hearing, "witnesses[0].workTelephone")),
                                withJsonPath("$.cases[0].witnesses[0].fax", equalStr(hearing, "witnesses[0].fax")),
                                withJsonPath("$.cases[0].witnesses[0].mobile", equalStr(hearing, "witnesses[0].mobile"))

                        )));
    }


    @Test
    public void initiateHearing_shouldInitiateHearing_whenThereIsAPreviousPlea() throws Exception {

        UUID hearingId = randomUUID();

        HearingUpdatePleaCommand updatePleaCommand = updatePleaTemplate().build();

        UUID offenceId = updatePleaCommand.getDefendants().get(0).getOffences().get(0).getId();

        makeCommand(requestSpec, "hearing.update-plea")
                .withArgs(hearingId.toString())
                .ofType("application/vnd.hearing.update-plea+json")
                .withPayload(updatePleaCommand)
                .executeSuccessfully();

        //TODO - GPE-3032 - need to be able to test that this happened.
        Thread.sleep(10000);

        InitiateHearingCommand initiateHearing = with(initiateHearingCommandTemplate(), command -> {
            command.getHearing().withId(hearingId);
            command.getHearing().getDefendants().get(0).getOffences().get(0).withId(offenceId);
        }).build();

        makeCommand(requestSpec, "hearing.initiate")
                .ofType("application/vnd.hearing.initiate+json")
                .withPayload(initiateHearing)
                .executeSuccessfully();

    }
}