package uk.gov.moj.cpp.hearing.command.api;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.stream;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerClassMatcher.isHandlerClass;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.command.api.service.ReferenceDataService;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.json.JsonObject;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class HearingCommandApiTest {

    private static final String PATH_TO_RAML = "src/raml/hearing-command-api.raml";
    private static final String NAME = "name:";

    private static final String COMMAND_SHARE_RESULTS = "hearing.command.share-results";

    private static final List<String> NON_PASS_THROUGH_METHODS = newArrayList("shareResults", "shareResultsV2", "logHearingEvent",
            "correctEvent", "updatePlea", "updateVerdict", "addWitness", "generateNows", "updateNowsMaterialStatus", "addDefenceCounsel",
            "addProsecutionCounsel", "removeProsecutionCounsel", "updateProsecutionCounsel", "removeDefenceCounsel", "updateDefenceCounsel", "initiateHearing", "saveDraftResult", "applicationDraftResult", "saveHearingCaseNote",
            "updateHearingEvents", "generateNowsV2", "deleteAttendee", "uploadSubscriptions", "saveNowsVariants", "updateDefendantAttendance", "saveApplicationResponse",
            "addRespondentCounsel", "updateRespondentCounsel", "removeRespondentCounsel", "addCompanyRepresentative", "updateCompanyRepresentative", "removeCompanyRepresentative",
            "addApplicantCounsel", "updateApplicantCounsel", "removeApplicantCounsel", "addInterpreterIntermediary",
            "removeInterpreterIntermediary", "updateInterpreterIntermediary", "setTrialType", "publishCourtList", "publishHearingListsForCrownCourts",
            "computeOutstandingFines", "addRequestForOutstandingFines", "recordSessionTime", "bookProvisionalHearingSlots", "removeTargets", "updateHearingDetails", "addMasterDefendantIdToDefendant", "cancelAmendments",
            "correctHearingDaysWithoutCourtCentre", "requestApproval", "validateResultAmendments", "markAsDuplicateHearing", "saveMultipleDraftResult", "updateResultLineSharedDates", "reusableInfo", "hearing.youth-court-defendants",
            "updateRelatedHearing", "shareResultsForHearingDay", "saveDraftResultForHearingDay", "saveDraftResultsForHearingDay", "saveDraftResultV2","deleteDraftResultV2","amendHearing");

    private static final String JSON_HEARING_INITIATE_DDCH = "json/hearing-initiate-ddch.json";
    private static final String JSON_HEARING_INITIATE = "json/hearing-initiate.json";
    private static final String JSON_EXPECTED_HEARING_INITIATE_DDCH = "json/expected-hearing-initiate-ddch.json";
    private static final String HEARING_INITIATE = "hearing.initiate";

    private static final String DUMMY_FIELD = "dummyField";
    private static final String DUMMY_FIELD_VALUE = "dummyFieldValue";

    private Map<String, String> apiMethodsToHandlerNames;
    private Map<String, String> eventApiMethodsToHandlerNames;
    private Map<String, String> notificationApiMethodsToHandlerNames;
    private Map<String, String> outstandingFinesCommandApiMethodsToHandlerNames;
    private Map<String, String> sessionTimeApiMethodsToHandlerNames;
    private Map<String, String> reusableInfoApiMethodsToHandlerNames;
    private Map<String, String> youthCourtDefendantsApiMethodsToHandlerNames;

    @Spy
    private JsonObjectToObjectConverter jsonObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter = new JsonObjectConvertersFactory().objectToJsonObjectConverter();

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<JsonEnvelope> senderArgumentCaptor;

    @Mock
    private ReferenceDataService referenceDataService;

    @Captor
    private ArgumentCaptor<Envelope<JsonObject>> senderArgumentCaptor1;

    @InjectMocks
    private HearingCommandApi hearingCommandApi;

    @Before
    public void setup() {
        apiMethodsToHandlerNames = apiMethodsToHandlerNames(HearingCommandApi.class);
        eventApiMethodsToHandlerNames = apiMethodsToHandlerNames(HearingEventCommandApi.class);
        notificationApiMethodsToHandlerNames = apiMethodsToHandlerNames(NotificationCommandApi.class);
        outstandingFinesCommandApiMethodsToHandlerNames = apiMethodsToHandlerNames(OutstandingFinesCommandApi.class);
        sessionTimeApiMethodsToHandlerNames = apiMethodsToHandlerNames(SessionTimeCommandApi.class);
        reusableInfoApiMethodsToHandlerNames = apiMethodsToHandlerNames(ReusableInfoCommandApi.class);
        youthCourtDefendantsApiMethodsToHandlerNames = apiMethodsToHandlerNames(YouthCourtDefendantsCommandApi.class);

    }

    @Test
    public void shouldCheckActionNameAndHandlerNameAreSame() throws Exception {
        final List<String> allLines = FileUtils.readLines(new File(PATH_TO_RAML));
        final List<String> ramlActionNames = allLines.stream()
                .filter(action -> !action.isEmpty())
                .filter(line -> line.contains(NAME))
                .map(line -> line.replaceAll(NAME, "").trim())
                .collect(toList());

        final List<String> allHandlerNames = Stream.of(
                apiMethodsToHandlerNames.values().stream(),
                eventApiMethodsToHandlerNames.values().stream(),
                notificationApiMethodsToHandlerNames.values().stream(),
                outstandingFinesCommandApiMethodsToHandlerNames.values().stream(),
                sessionTimeApiMethodsToHandlerNames.values().stream(),
                reusableInfoApiMethodsToHandlerNames.values().stream(),
                youthCourtDefendantsApiMethodsToHandlerNames.values().stream())
                .reduce(Stream::concat)
                .orElseGet(Stream::empty)
                .collect(toList());

        assertThat(allHandlerNames, containsInAnyOrder(ramlActionNames.toArray()));
    }

    @Test
    public void shouldInitiateHearingWithDDCHJudiciaryResult() {
        final JsonObject jsonObjectPayload = CommandAPITestBase.readJson(JSON_HEARING_INITIATE_DDCH, JsonObject.class);
        final Metadata metadata = CommandAPITestBase.metadataFor(HEARING_INITIATE, randomUUID().toString());
        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(metadata, jsonObjectPayload);

        when(referenceDataService.getResults(envelope, "DDCH")).thenReturn(ResultDefinition.resultDefinition().setId(fromString("8c67b30a-418c-11e8-842f-0ed5f89f718b")));

        hearingCommandApi.initiateHearing(envelope);

        verify(sender).send(senderArgumentCaptor1.capture());

        final Envelope<JsonObject> jsonEnvelopOut = senderArgumentCaptor1.getValue();

        final JsonObject result = CommandAPITestBase.readJson(JSON_EXPECTED_HEARING_INITIATE_DDCH, JsonObject.class);

        assertThat(jsonEnvelopOut.metadata().name(), is("hearing.initiate"));
        assertThat(jsonEnvelopOut.payload(), is(result));
    }

    @Test
    public void shouldInitiateHearingWithOutDDCHJudiciaryResult() {
        final JsonObject jsonObjectPayload = CommandAPITestBase.readJson(JSON_HEARING_INITIATE, JsonObject.class);
        final Metadata metadata = CommandAPITestBase.metadataFor(HEARING_INITIATE, randomUUID().toString());
        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(metadata, jsonObjectPayload);

        hearingCommandApi.initiateHearing(envelope);

        verify(sender).send(senderArgumentCaptor1.capture());

        final Envelope<JsonObject> jsonEnvelopOut = senderArgumentCaptor1.getValue();

        assertThat(jsonEnvelopOut.metadata().name(), is("hearing.initiate"));
        assertThat(jsonEnvelopOut.payload(), is(jsonObjectPayload));
    }


    @Test
    public void shouldCheckHandlerNamesPassThroughSender() {
        assertHandlerMethodsArePassThrough(HearingCommandApi.class, apiMethodsToHandlerNames.keySet().stream()
                .filter(methodName -> !NON_PASS_THROUGH_METHODS.contains(methodName))
                .collect(toMap(identity(), apiMethodsToHandlerNames::get)));
        assertHandlerMethodsArePassThrough(HearingEventCommandApi.class, eventApiMethodsToHandlerNames.keySet().stream()
                .filter(methodName -> !NON_PASS_THROUGH_METHODS.contains(methodName))
                .collect(toMap(identity(), eventApiMethodsToHandlerNames::get)));
        assertHandlerMethodsArePassThrough(NotificationCommandApi.class, notificationApiMethodsToHandlerNames.keySet().stream()
                .filter(methodName -> !NON_PASS_THROUGH_METHODS.contains(methodName))
                .collect(toMap(identity(), notificationApiMethodsToHandlerNames::get)));
        assertHandlerMethodsArePassThrough(OutstandingFinesCommandApi.class, outstandingFinesCommandApiMethodsToHandlerNames.keySet().stream()
                .filter(methodName -> !NON_PASS_THROUGH_METHODS.contains(methodName))
                .collect(toMap(identity(), outstandingFinesCommandApiMethodsToHandlerNames::get)));
    }

    @Test
    public void shouldPassThroughShareResultCommandToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.share-results");

        hearingCommandApi.shareResults(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), COMMAND_SHARE_RESULTS);
    }

    @Test
    public void shouldPassThroughShareResultV2CommandToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.share-results-v2");

        hearingCommandApi.shareResultsV2(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.share-results-v2");
    }

    @Test
    public void shouldPassThroughShareDaysResultCommandToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.share-days-results");

        hearingCommandApi.shareResultsForHearingDay(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.share-days-results");
    }

    @Test
    public void shouldPassThroughSetTrialTypeRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.set-trial-type");

        hearingCommandApi.setTrialType(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.set-trial-type");
    }

    @Test
    public void shouldPassThroughAddCompanyRepresentativeRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.add-company-representative");

        hearingCommandApi.addCompanyRepresentative(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.add-company-representative");
    }

    @Test
    public void shouldPassThroughComputeOutstandingFinesRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.compute-outstanding-fines");

        hearingCommandApi.computeOutstandingFines(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.compute-outstanding-fines");
    }

    @Test
    public void shouldPassThroughBookProvisionalHearingSlotsRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.book-provisional-hearing-slots");

        hearingCommandApi.bookProvisionalHearingSlots(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.book-provisional-hearing-slots");
    }

    @Test
    public void shouldPassThroughUpdateCompanyRepresentativeRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.update-company-representative");

        hearingCommandApi.updateCompanyRepresentative(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.update-company-representative");
    }

    @Test
    public void shouldPassRemoveCompanyRepresentativeThroughRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.remove-company-representative");

        hearingCommandApi.removeCompanyRepresentative(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.remove-company-representative");
    }

    @Test
    public void shouldPassThroughPublishCourtListRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.publish-court-list");

        hearingCommandApi.publishCourtList(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.publish-court-list");
    }

    @Test
    public void shouldPassThroughPublishHearingListsForCrownCourtsRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.publish-hearing-lists-for-crown-courts");

        hearingCommandApi.publishHearingListsForCrownCourts(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.publish-hearing-lists-for-crown-courts");
    }

    @Test
    public void shouldPassThroughAddInterpreterIntermediaryRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.add-interpreter-intermediary");

        hearingCommandApi.addInterpreterIntermediary(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.add-interpreter-intermediary");
    }

    @Test
    public void shouldPassThroughFemoveInterpreterIntermediaryRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.remove-interpreter-intermediary");

        hearingCommandApi.removeInterpreterIntermediary(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.remove-interpreter-intermediary");
    }

    @Test
    public void shouldPassThroughUpdateInterpreterIntermediaryRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.update-interpreter-intermediary");

        hearingCommandApi.updateInterpreterIntermediary(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.update-interpreter-intermediary");
    }

    @Test
    public void shouldPassThroughRemoveApplicantCounselRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.remove-applicant-counsel");

        hearingCommandApi.removeApplicantCounsel(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.remove-applicant-counsel");
    }

    @Test
    public void shouldPassThroughAddApplicantCounselRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.add-applicant-counsel");

        hearingCommandApi.addApplicantCounsel(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.add-applicant-counsel");
    }

    @Test
    public void shouldPassThroughUpdateApplicantCounselRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.update-applicant-counsel");

        hearingCommandApi.updateApplicantCounsel(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.update-applicant-counsel");
    }

    @Test
    public void shouldPassThroughRemoveRespondentCounselRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.remove-respondent-counsel");

        hearingCommandApi.removeRespondentCounsel(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.remove-respondent-counsel");
    }

    @Test
    public void shouldPassThroughUpdateRespondentCounselRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.update-respondent-counsel");

        hearingCommandApi.updateRespondentCounsel(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.update-respondent-counsel");
    }

    @Test
    public void shouldPassThroughAddRespondentCounselRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.add-respondent-counsel");

        hearingCommandApi.addRespondentCounsel(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.add-respondent-counsel");
    }

    @Test
    public void shouldPassThroughUpdateDefendantAttendanceRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.update-defendant-attendance-on-hearing-day");

        hearingCommandApi.updateDefendantAttendance(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.update-defendant-attendance-on-hearing-day");
    }

    @Test
    public void shouldPassThroughSaveHearingCaseNoteRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.save-hearing-case-note");

        hearingCommandApi.saveHearingCaseNote(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.save-hearing-case-note");
    }

    @Test
    public void shouldPassThroughUpdateDefenceCounselRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.update-defence-counsel");

        hearingCommandApi.updateDefenceCounsel(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.update-defence-counsel");
    }

    @Test
    public void shouldPassThroughAddDefenceCounselRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.add-defence-counsel");

        hearingCommandApi.addDefenceCounsel(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.add-defence-counsel");
    }

    @Test
    public void shouldPassThroughRemoveDefenceCounselRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.remove-defence-counsel");

        hearingCommandApi.removeDefenceCounsel(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.remove-defence-counsel");
    }

    @Test
    public void shouldPassThroughUpdatePleaRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.update-plea");

        hearingCommandApi.updatePlea(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.hearing-offence-plea-update");
    }

    @Test
    public void shouldPassThroughGenerateNOWsRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.generate-nows");

        hearingCommandApi.generateNows(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.generate-nows");
    }

    @Test
    public void shouldPassThroughUpdateVerdictRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.update-verdict");

        hearingCommandApi.updateVerdict(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.update-verdict");
    }

    @Test
    public void shouldPassThroughAddProsecutionCounselRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.add-prosecution-counsel");

        hearingCommandApi.addProsecutionCounsel(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.add-prosecution-counsel");
    }

    @Test
    public void shouldPassThroughRemoveProsecutionCounselRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.remove-prosecution-counsel");

        hearingCommandApi.removeProsecutionCounsel(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.remove-prosecution-counsel");
    }

    @Test
    public void shouldPassThroughUpdateProsecutionCounselRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.update-prosecution-counsel");

        hearingCommandApi.updateProsecutionCounsel(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.update-prosecution-counsel");
    }

    @Test
    public void shouldPassThroughSaveDraftResultRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.save-draft-result");

        hearingCommandApi.saveDraftResult(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.save-draft-result");
    }

    @Test
    public void shouldPassThroughDeleteDraftResultV2RequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.delete-draft-result-v2");

        hearingCommandApi.deleteDraftResultV2(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.delete-draft-result-v2");
    }

    @Test
    public void shouldPassThroughSaveDaysDraftResultRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.save-days-draft-result");

        hearingCommandApi.saveDraftResultForHearingDay(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.save-days-draft-result");
    }

    @Test
    public void shouldPassThroughSaveDaysDraftResultsRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.save-days-draft-results");

        hearingCommandApi.saveDraftResultsForHearingDay(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.save-days-draft-results");
    }

    @Test
    public void shouldPassThroughRequestApprovalCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.request-approval");

        hearingCommandApi.requestApproval(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.request-approval");
    }

    @Test
    public void shouldPassThroughValidateResultAmendmentsCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.validate-result-amendments");

        hearingCommandApi.validateResultAmendments(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.validate-result-amendments");
    }

    @Test
    public void shouldPassThroughRemoveDraftTargetRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.remove-targets");

        hearingCommandApi.removeTargets(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.remove-targets");
    }

    @Test
    public void shouldPassThroughAddMasterDefendantIdRequestToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.add-master-defendant-id-to-defendant");

        hearingCommandApi.addMasterDefendantIdToDefendant(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.add-master-defendant-id-to-defendant");
    }

    @Test
    public void shouldPassThroughMarkAsDuplicateToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.mark-as-duplicate");

        hearingCommandApi.markAsDuplicateHearing(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.mark-as-duplicate");
    }

    @Test
    public void shouldPassThroughUpdateRelatedHearingToCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.update-related-hearing");

        hearingCommandApi.updateRelatedHearing(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.update-related-hearing");
    }

    @Test
    public void shouldPassThroughUpdateResultLineSharedDatesCommandHandler() {
        final JsonEnvelope jsonRequestEnvelope = buildDummyJsonRequestEnvelopeWithName("hearing.update-resultline-shared-dates");

        hearingCommandApi.updateResultLineSharedDates(jsonRequestEnvelope);

        assertEnvelopeIsPassedThroughWithName(jsonRequestEnvelope.payloadAsJsonObject(), "hearing.command.update-resultline-shared-dates");
    }

    private JsonEnvelope buildDummyJsonRequestEnvelopeWithName(final String name) {
        return envelopeFrom(metadataWithRandomUUID(name).withCausation(randomUUID())
                        .build(),
                createObjectBuilder()
                        .add(DUMMY_FIELD, DUMMY_FIELD_VALUE)
                        .build());
    }

    private void assertEnvelopeIsPassedThroughWithName(final JsonObject originalPayload,
                                                       final String expectedName) {
        verify(sender).send(senderArgumentCaptor1.capture());

        final Envelope<JsonObject> actualSentEnvelope = senderArgumentCaptor1.getValue();
        assertThat(actualSentEnvelope.metadata().name(), is(expectedName));
        assertThat(actualSentEnvelope.payload(), is(originalPayload));
    }

    private <T> void assertHandlerMethodsArePassThrough(final Class<T> commandApiClass,
                                                        final Map<String, String> methodsToHandlerNamesMap) {
        for (final Map.Entry<String, String> entry : methodsToHandlerNamesMap.entrySet()) {
            assertThat(commandApiClass, isHandlerClass(COMMAND_API)
                    .with(method(entry.getKey())
                            .thatHandles(entry.getValue())
                            .withSenderPassThrough()));
        }
    }

    private Map<String, String> apiMethodsToHandlerNames(final Class<?> clazz) {
        return stream(clazz.getMethods())
                .filter(method -> method.getAnnotation(Handles.class) != null)
                .collect(toMap(Method::getName, method -> method.getAnnotation(Handles.class).value()));
    }
}
