package uk.gov.moj.cpp.hearing.it;

import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;
import static uk.gov.justice.services.messaging.JsonMetadata.ID;
import static uk.gov.justice.services.messaging.JsonMetadata.NAME;
import static uk.gov.justice.services.messaging.JsonMetadata.USER_ID;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.makeCommand;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimumInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UploadSubscriptionsCommandTemplates.buildUploadSubscriptionsCommand;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

import com.jayway.awaitility.Awaitility;
import org.junit.After;
import org.junit.Test;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.command.nows.UpdateNowsMaterialStatusCommand;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.utils.DocumentGeneratorStub;
import uk.gov.moj.cpp.hearing.utils.NotifyStub;
import uk.gov.moj.cpp.hearing.utils.QueueUtil;
import uk.gov.moj.cpp.hearing.utils.WireMockStubUtils;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.json.Json;
import javax.json.JsonObject;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@SuppressWarnings("unchecked")
public class GenerateNowsIT extends AbstractIT {

    private static final MessageProducer messageProducerClientPublic = QueueUtil.publicEvents.createProducer();
    private static final String USERGROUP1 = "courtAdmin";
    private static final String USERGROUP2 = "defence";
    private static final String CASEURN = "CASE12345";
    private static final String ORIGINATOR = "originator";
    private static final String ORIGINATOR_VALUE = "court";
    private static final String DOCUMENT_TEXT = STRING.next();

    @Test
    public void shouldAddUpdateNows() throws IOException {

        final CommandHelpers.UploadSubscriptionsCommandHelper subscriptions = h(UseCases.uploadSubscriptions(requestSpec, with(buildUploadSubscriptionsCommand(), subs -> {
            CommandHelpers.UploadSubscriptionsCommandHelper h = h(subs);
            h.getFirstSubscription().setChannel("email");
            h.getFirstSubscription().setDestination("generatenows@test.com");
            h.getFirstSubscription().setUserGroups(singletonList(USERGROUP1));
        })));

        final InitiateHearingCommandHelper hearing = h(UseCases.initiateHearing(requestSpec, with(minimumInitiateHearingTemplate(), i -> {
            i.getHearing().getCourtCentre().setId(subscriptions.getFirstSubscription().getCourtCentreIds().get(0));
        })));

        final String userId = randomUUID().toString();
        final String materialId = randomUUID().toString();
        final String nowsId = randomUUID().toString();
        final String sharedResultId = randomUUID().toString();

        NotifyStub.stubNotifications();
        DocumentGeneratorStub.stubDocumentCreate(DOCUMENT_TEXT);

        final TestUtilities.EventListener nowsRequestedEventListener = listenFor("public.hearing.events.nows-requested")
                .withFilter(isJson(withJsonPath("$.hearing.id", is(hearing.getHearingId().toString()))));

        makeCommand(requestSpec, "hearing.generate-nows")
                .ofType("application/vnd.hearing.generate-nows+json")
                .withPayload(getGenerateNowsCommand(
                        hearing.getHearingId().toString(),
                        hearing.getFirstDefendantForFirstCase().getId().toString(),
                        materialId,
                        nowsId,
                        subscriptions.getFirstSubscription().getNowTypeIds().get(0).toString(),
                        sharedResultId,
                        USERGROUP1,
                        USERGROUP2,
                        subscriptions.getFirstSubscription().getCourtCentreIds().get(0).toString(),
                        CASEURN))
                .executeSuccessfully();

        // ensure upload material and update status called
        Awaitility.await().atMost(60, TimeUnit.SECONDS).until(() -> uploadMaterialCalled(materialId));

        sendMaterialFileUploadedPublicEvent(materialId, userId);

        Awaitility.await().atMost(60, TimeUnit.SECONDS).until(() -> updateMaterialStatusHmpsCalled(materialId));

        sendHearingHmpsMaterialStatusUpdatedPublicMessage(materialId, userId, hearing.getHearingId().toString());

        nowsRequestedEventListener.waitFor();

        poll(requestParams(getURL("hearing.get.nows", hearing.getHearingId().toString()), "application/vnd.hearing.get.nows+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.nows[0].id", is(nowsId)),
                                withJsonPath("$.nows[0].defendantId", is(hearing.getFirstDefendantForFirstCase().getId().toString())),
                                withJsonPath("$.nows[0].nowsTypeId", is(subscriptions.getFirstSubscription().getNowTypeIds().get(0).toString())),
                                withJsonPath("$.nows[0].material[0].id", is(materialId)),
                                withJsonPath("$.nows[0].material[0].status", is("generated")),
                                withJsonPath("$.nows[0].material[0].nowResult[0].sharedResultId", is(sharedResultId)),
                                withJsonPath("$.nows[0].material[0].nowResult[0].sequence", is(1))
                        )));

        poll(requestParams(getURL("hearing.query.search-by-material-id", materialId), "application/vnd.hearing.query.search-by-material-id+json")
                .withHeader(CPP_UID_HEADER_AS_ADMIN.getName(), CPP_UID_HEADER_AS_ADMIN.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.allowedUserGroups[0]", is("courtAdmin")),
                                withJsonPath("$.allowedUserGroups[1]", is("defence"))
                        )));

        NotifyStub.verifyNotification(subscriptions.getFirstSubscription(), Arrays.asList(CASEURN));
        DocumentGeneratorStub.verifyCreate(Arrays.asList(materialId));

    }

    @Test
    public void shouldUpdateNowsMaterialStatusToGenerated() throws IOException {

        final InitiateHearingCommandHelper hearing = h(UseCases.initiateHearing(requestSpec, minimumInitiateHearingTemplate()));

        final UUID materialId = randomUUID();
        final String nowsId = randomUUID().toString();
        final String nowsTypeId = randomUUID().toString();
        final String sharedResultId = randomUUID().toString();

        DocumentGeneratorStub.stubDocumentCreate(DOCUMENT_TEXT);

        final TestUtilities.EventListener nowsRequestedEventListener = listenFor("public.hearing.events.nows-requested")
                .withFilter(isJson(withJsonPath("$.hearing.id", is(hearing.getHearingId().toString()))));

        makeCommand(requestSpec, "hearing.generate-nows")
                .ofType("application/vnd.hearing.generate-nows+json")
                .withPayload(getGenerateNowsCommand(hearing.getHearingId().toString(),
                        hearing.getFirstDefendantForFirstCase().getId().toString(),
                        materialId.toString(),
                        nowsId,
                        nowsTypeId,
                        sharedResultId,
                        USERGROUP1,
                        USERGROUP2,
                        randomUUID().toString(),
                        CASEURN))
                .executeSuccessfully();

        nowsRequestedEventListener.waitFor();

        poll(requestParams(getURL("hearing.get.nows", hearing.getHearingId().toString()), "application/vnd.hearing.get.nows+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(withJsonPath("$.nows[0].id", is(nowsId)),
                                withJsonPath("$.nows[0].material[0].id", is(materialId.toString())),
                                withJsonPath("$.nows[0].material[0].status", is("requested")))));

        final TestUtilities.EventListener nowsMaterialStatusUpdatedEventListener = listenFor("public.hearing.events.nows-material-status-updated")
                .withFilter(isJson(withJsonPath("$.materialId", is(materialId.toString()))));

        makeCommand(requestSpec, "hearing.update-nows-material-status")
                .withArgs(hearing.getHearingId().toString(), nowsId)
                .ofType("application/vnd.hearing.update-nows-material-status+json")
                .withPayload(UpdateNowsMaterialStatusCommand.builder()
                        .withMaterialId(materialId)
                        .withStatus("generated")
                        .build())
                .executeSuccessfully();

        poll(requestParams(getURL("hearing.get.nows", hearing.getHearingId().toString()), "application/vnd.hearing.get.nows+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(withJsonPath("$.nows[0].id", is(nowsId)),
                                withJsonPath("$.nows[0].material[0].id", is(materialId.toString())),
                                withJsonPath("$.nows[0].material[0].status", is("generated")))));

        nowsMaterialStatusUpdatedEventListener.waitFor();
    }


    private static String getGenerateNowsCommand(final String hearingId, final String defendantId, final String materialId,
                                                 final String nowsId, final String nowsTypeId, final String sharedResultId,
                                                 final String usergroup1, final String usergroup2, final String courtCentreId, final String caseURN) throws IOException {
        return getStringFromResource("hearing.generate-nows.json")
                .replace("HEARING_ID", hearingId)
                .replace("DEFENDANT_ID", defendantId)
                .replace("NOW_ID", nowsId)
                .replace("MATERIAL_ID", materialId)
                .replace("NOWTYPE_ID", nowsTypeId)
                .replace("USERGROUP1", usergroup1)
                .replace("USERGROUP2", usergroup2)
                .replace("RESULT_ID", sharedResultId)
                .replace("COURTCENTRE_ID", courtCentreId)
                .replace("CASEURN", caseURN);
    }

    private boolean uploadMaterialCalled(String materialId) {
        return findAll(postRequestedFor(urlPathMatching(WireMockStubUtils.MATERIAL_UPLOAD_COMMAND)))
                .stream()
                .anyMatch(log -> log.getBodyAsString().contains(materialId));
    }

    private boolean updateMaterialStatusHmpsCalled(String materialId) {
        return findAll(postRequestedFor(urlPathMatching(WireMockStubUtils.MATERIAL_STATUS_UPLOAD_COMMAND)))
                .stream()
                .anyMatch(log -> log.getBodyAsString().contains(materialId));
    }

    private void sendMaterialFileUploadedPublicEvent(final String materialId, final String userId) {
        final String commandName = "material.material-added";
        final Metadata metadata = getMetadataFrom(userId, commandName);
        final JsonObject payload = Json.createObjectBuilder().add("materialId", materialId).add(
                "fileDetails",
                Json.createObjectBuilder().add("alfrescoAssetId", "aGVsbG8=")
                        .add("mimeType", "text/plain").add("fileName", "file.txt"))
                .add("materialAddedDate", "2016-04-26T13:01:787.345").build();
        QueueUtil.sendMessage(messageProducerClientPublic, commandName, payload, metadata);
    }

    private void sendHearingHmpsMaterialStatusUpdatedPublicMessage(final String materialId,
                                                                   final String userId, final String hearingId) {
        final String commandName = "public.resultinghmps.event.nows-material-status-updated";
        final Metadata metadata = getMetadataFrom(userId, commandName);
        final JsonObject payload = Json.createObjectBuilder().add("materialId", materialId)
                .add("hearingId", hearingId).add("status", "generated").build();
        QueueUtil.sendMessage(messageProducerClientPublic, commandName, payload, metadata);
    }

    private Metadata getMetadataFrom(final String userId, final String commandName) {
        return metadataFrom(Json.createObjectBuilder()
                .add(ORIGINATOR, ORIGINATOR_VALUE)
                .add(ID, randomUUID().toString())
                .add(USER_ID, userId)
                .add(NAME, commandName)
                .build()).build();
    }

    @After
    public void tearDown() throws JMSException {
        messageProducerClientPublic.close();
    }

}