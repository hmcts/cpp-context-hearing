package uk.gov.moj.cpp.hearing.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.justice.progression.events.CaseDefendantDetails;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.command.defendant.Address;
import uk.gov.moj.cpp.hearing.command.defendant.Interpreter;
import uk.gov.moj.cpp.hearing.command.defendant.Person;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;

import javax.json.JsonObject;
import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.makeCommand;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.initiateHearingCommandTemplateWithOnlyMandatoryFields;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.publicEvents;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.sendMessage;

public class CaseDefendantDetailsChangeIT extends AbstractIT {

    @SuppressWarnings("unchecked")
    @Test
    public void updateCaseDefendantDetals_shouldUpdateDefendant_givenResultNotShared() throws Exception {

        final InitiateHearingCommand initiateHearing = initiateHearingCommandTemplateWithOnlyMandatoryFields().build();

        final Hearing hearing = initiateHearing.getHearing();

        final TestUtilities.EventListener publicHearingEventListener = listenFor("public.hearing.initiated")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearing.getId().toString()))));

        makeCommand(requestSpec, "hearing.initiate")
                .ofType("application/vnd.hearing.initiate+json")
                .withPayload(initiateHearing)
                .executeSuccessfully();

        publicHearingEventListener.waitFor();

        final CaseDefendantDetails caseDefendantDetails = createCaseDefendantDetailsCommand(
                initiateHearing.getCases().get(0).getCaseId(),
                hearing.getDefendants());

        final String eventName = "public.progression.case-defendant-changed";

        final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();

        final JsonObject jsonObject = mapper.readValue(mapper.writeValueAsString(caseDefendantDetails), JsonObject.class);

        sendMessage(
                publicEvents.createProducer(),
                eventName,
                jsonObject,
                metadataOf(caseDefendantDetails.getDefendants().get(0).getId(), eventName).withUserId(randomUUID().toString()).build());

        final String queryAPIEndPoint = MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"), hearing.getId());

        final String url = getBaseUri() + "/" + queryAPIEndPoint;

        final String responseType = "application/vnd.hearing.get.hearing.v2+json";

        poll(requestParams(url, responseType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(initiateHearing.getHearing().getId().toString())),
                                withJsonPath("$.cases[0].caseId", is(initiateHearing.getCases().get(0).getCaseId().toString())),
                                withJsonPath("$.cases[0].caseUrn", equalStr(initiateHearing, "cases[0].urn")),
                                withJsonPath("$.cases[0].defendants[0].defendantId", is(caseDefendantDetails.getDefendants().get(0).getId().toString())),
                                withJsonPath("$.cases[0].defendants[0].firstName", equalStr(caseDefendantDetails, "defendants[0].firstName")),
                                withJsonPath("$.cases[0].defendants[0].lastName", equalStr(caseDefendantDetails, "defendants[0].lastName")),
                                withJsonPath("$.cases[0].defendants[0].address.address1", equalStr(caseDefendantDetails, "defendants[0].address.address1")),
                                withJsonPath("$.cases[0].defendants[0].address.address2", equalStr(caseDefendantDetails, "defendants[0].address.address2")),
                                withJsonPath("$.cases[0].defendants[0].address.address3", equalStr(caseDefendantDetails, "defendants[0].address.address3")),
                                withJsonPath("$.cases[0].defendants[0].address.address4", equalStr(caseDefendantDetails, "defendants[0].address.address4")),
                                withJsonPath("$.cases[0].defendants[0].address.postCode", equalStr(caseDefendantDetails, "defendants[0].address.postCode"))
                        )));
    }

    private CaseDefendantDetails createCaseDefendantDetailsCommand(UUID caseId, List<uk.gov.moj.cpp.hearing.command.initiate.Defendant> defendants) {

        Function<uk.gov.moj.cpp.hearing.command.initiate.Defendant, uk.gov.moj.cpp.hearing.command.defendant.Defendant> mapDefendant = d -> uk.gov.moj.cpp.hearing.command.defendant.Defendant.builder()
                .withId(d.getId())
                .withPerson(Person.builder().withId(d.getPersonId())
                .withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withNationality(STRING.next())
                .withGender(STRING.next())
                .withAddress(Address.address()
                        .withAddress1(STRING.next())
                        .withAddress2(STRING.next())
                        .withAddress3(STRING.next())
                        .withAddress4(STRING.next())
                        .withPostcode(STRING.next()))
                .withDateOfBirth(PAST_LOCAL_DATE.next()))
                .withBailStatus(STRING.next())
                .withCustodyTimeLimitDate(PAST_LOCAL_DATE.next())
                .withDefenceOrganisation(STRING.next())
                .withInterpreter(Interpreter.builder(STRING.next()))
                .build();

        return CaseDefendantDetails.builder()
                .withCaseId(caseId)
                .addDefendants(defendants.stream().map(mapDefendant).collect(Collectors.toList()))
                .build();
    }
}