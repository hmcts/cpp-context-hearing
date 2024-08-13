package uk.gov.moj.cpp.hearing.event;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Collections.singletonList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingWithApplicationTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.createCourtApplicationCases;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.CourtApplicationType;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

@SuppressWarnings({"unchecked", "unused"})
@RunWith(DataProviderRunner.class)
public class InitiateHearingEventProcessorTest {
    private static final UUID APPEARANCE_TO_MAKE_STATUTORY_DECLARATION_ID = fromString("f3a6e917-7cc8-3c66-83dd-d958abd6a6e4");
    private static final UUID APPEARANCE_TO_MAKE_STATUTORY_DECLARATION_SJP_CASE_ID = fromString("7375727f-30fc-3f55-99f3-36adc4f0e70e");
    private static final UUID APPLICATION_TO_REOPEN_CASE_ID = fromString("44c238d9-3bc2-3cf3-a2eb-a7d1437b8383");
    private static final UUID APPEAL_AGAINST_CONVICTION_ID = fromString("57810183-a5c2-3195-8748-c6b97eda1ebd");
    private static final UUID APPEAL_AGAINST_SENTENCE_ID = fromString("beb08419-0a9a-3119-b3ec-038d56c8a718");
    private static final UUID APPEAL_AGAINST_CONVICTION_AND_SENTENCE_ID = fromString("36f3b0c3-9f75-31aa-a226-cfee69216160");

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final List<String> APPLICATION_TYPE_LIST = Arrays.asList(APPEARANCE_TO_MAKE_STATUTORY_DECLARATION_ID.toString(), APPEARANCE_TO_MAKE_STATUTORY_DECLARATION_SJP_CASE_ID.toString(),
            APPLICATION_TO_REOPEN_CASE_ID.toString(), APPEAL_AGAINST_CONVICTION_ID.toString(), APPEAL_AGAINST_SENTENCE_ID.toString(), APPEAL_AGAINST_CONVICTION_AND_SENTENCE_ID.toString());

    @DataProvider
    public static Object[] applicationTypes() {
        return new String[][]{
                {APPEARANCE_TO_MAKE_STATUTORY_DECLARATION_ID.toString(), "STAT_DEC"},
                {APPEARANCE_TO_MAKE_STATUTORY_DECLARATION_SJP_CASE_ID.toString(), "STAT_DEC"},
                {APPLICATION_TO_REOPEN_CASE_ID.toString(), "REOPEN"},
                {APPEAL_AGAINST_CONVICTION_ID.toString(), "APPEAL"},
                {APPEAL_AGAINST_SENTENCE_ID.toString(), "APPEAL"},
                {APPEAL_AGAINST_CONVICTION_AND_SENTENCE_ID.toString(), "APPEAL"}
        };
    }

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Spy
    private JsonObjectToObjectConverter jsonObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter = new JsonObjectConvertersFactory().objectToJsonObjectConverter();

    @InjectMocks
    private InitiateHearingEventProcessor initiateHearingEventProcessor;
    @Mock
    private Sender sender;
    @Mock
    private Requester InitiateHearingEventProcessorTestrequester;
    @Mock
    private JsonEnvelope responseEnvelope;
    @Captor
    private ArgumentCaptor<Envelope<JsonObject>> envelopeArgumentCaptor;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Captor
    protected ArgumentCaptor<JsonEnvelope> jsonEnvelopeArgumentCaptor;

    @Test
    public void publishHearingInitiatedEvent() {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        publishHearingInitiatedEvent(initiateHearingCommand);
    }

    @Test
    public void publishHearingInitiatedEventNoCases() {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        initiateHearingCommand.getHearing().setProsecutionCases(null);
        publishHearingInitiatedEvent(initiateHearingCommand);
        verify(sender, times(1)).send(jsonEnvelopeArgumentCaptor.capture());

        final Metadata metadata = jsonEnvelopeArgumentCaptor.getValue().metadata();
        Assert.assertThat(metadata.name(), is("public.hearing.initiated"));

        final JsonObject jsonObject = jsonEnvelopeArgumentCaptor.getValue().asJsonObject();

        final JsonArray applicationDetailsList = jsonObject.getJsonArray("applicationDetails");
        final JsonObject applicationDetails = (JsonObject) applicationDetailsList.get(0);
        final JsonObject subject = applicationDetails.getJsonObject("subject");

        Assert.assertThat(jsonObject.getString("hearingId"), notNullValue());
        Assert.assertThat(jsonObject.getJsonArray("cases").size(), is(0));
        Assert.assertThat(jsonObject.getString("hearingDateTime"), notNullValue());
        Assert.assertThat(jsonObject.getJsonArray("caseDetails").size(), is(0));
        Assert.assertThat(jsonObject.getString("jurisdictionType"), is("CROWN"));
        Assert.assertThat(subject.getString("defendantFirstName"), is("Lauren"));
        Assert.assertThat(subject.getString("defendantLastName"), is("Michelle"));
    }

    @UseDataProvider("applicationTypes")
    @Test
    public void shouldRaiseEventForEmailWhenApplicationTypeMatches(final String applicationTypeId, final String applicationType) {
        final UUID masterDefendantId = randomUUID();
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingWithApplicationTemplate(singletonList(CourtApplication.courtApplication()
                .withType(CourtApplicationType.courtApplicationType()
                        .withId(fromString(applicationTypeId)).build())
                .withCourtApplicationCases(createCourtApplicationCases())
                .withSubject(CourtApplicationParty.courtApplicationParty()
                        .withMasterDefendant(MasterDefendant.masterDefendant()
                                .withMasterDefendantId(masterDefendantId)
                                .withPersonDefendant(PersonDefendant.personDefendant()
                                        .withPersonDetails(Person.person()
                                                .withFirstName("John")
                                                .withLastName("Doe")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build()));

        publishHearingInitiatedEvent(initiateHearingCommand);
        final Envelope<JsonObject> event = this.envelopeArgumentCaptor.getAllValues().get(4);

        final JsonEnvelope allValues = envelopeFrom(event.metadata(), event.payload());
        assertThat(allValues,
                jsonEnvelope(
                        metadata().withName("public.hearing.nces-email-notification-for-application"),
                        payloadIsJson(allOf(
                                withJsonPath("$.applicationType", is(applicationType)),
                                withJsonPath("$.masterDefendantId", is(masterDefendantId.toString())),
                                withJsonPath("$.listingDate", is(dateTimeFormatter.format(initiateHearingCommand.getHearing().getHearingDays().get(0).getSittingDay()))),
                                withJsonPath("$.caseUrns[0]", is("caseURN1")),
                                withJsonPath("$.caseUrns[1]", is("caseURN2")),
                                withJsonPath("$.hearingCourtCentreName", is(notNullValue()))
                        ))));

    }


    private void publishHearingInitiatedEvent(final InitiateHearingCommand initiateHearingCommand) {

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.initiated"),
                objectToJsonObjectConverter.convert(initiateHearingCommand));

        this.initiateHearingEventProcessor.hearingInitiated(event);

        int caseCount = initiateHearingCommand.getHearing().getProsecutionCases() == null ? 0 : initiateHearingCommand.getHearing().getProsecutionCases().size();
        int expectedInvocations = 1 + (3 * caseCount);
        if (initiateHearingCommand.getHearing().getCourtApplications() != null && APPLICATION_TYPE_LIST.contains(initiateHearingCommand.getHearing().getCourtApplications().get(0).getType().getId().toString())) {
            expectedInvocations++;
        }


        verify(this.sender, times(expectedInvocations)).send(this.envelopeArgumentCaptor.capture());

        final List<Envelope<JsonObject>> envelopes = this.envelopeArgumentCaptor.getAllValues();

        final List<UUID> prosecutionCaseIds = new ArrayList<>();
        final List<UUID> defendantIds = new ArrayList<>();
        final List<UUID> offenceIds = new ArrayList<>();

        if (caseCount > 0) {
            initiateHearingCommand.getHearing().getProsecutionCases().forEach(prosecutionCase -> {
                prosecutionCaseIds.add(prosecutionCase.getId());
                prosecutionCase.getDefendants().forEach(defendant -> {
                    defendantIds.add(defendant.getId());
                    defendant.getOffences().forEach(offence -> offenceIds.add(offence.getId()));
                });
            });
        }

        if (caseCount > 0) {
            assertThat(
                    envelopeFrom(envelopes.get(0).metadata(), objectToJsonObjectConverter.convert(envelopes.get(0).payload())), is(jsonEnvelope(
                            metadata().withName("hearing.command.register-hearing-against-defendant"),
                            payloadIsJson(allOf(
                                    withJsonPath("$.defendantId", is(defendantIds.get(0).toString())),
                                    withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString())))))
                    )
            );

            assertThat(
                    envelopeFrom(envelopes.get(1).metadata(), objectToJsonObjectConverter.convert(envelopes.get(1).payload())), jsonEnvelope(
                            metadata().withName("hearing.command.register-hearing-against-offence"),
                            payloadIsJson(allOf(
                                    withJsonPath("$.offenceId", is(offenceIds.get(0).toString())),
                                    withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString()))))));


            assertThat(
                    envelopeFrom(envelopes.get(2).metadata(), objectToJsonObjectConverter.convert(envelopes.get(2).payload())), jsonEnvelope(
                            metadata().withName("hearing.command.register-hearing-against-case"),
                            payloadIsJson(allOf(
                                    withJsonPath("$.caseId", is(prosecutionCaseIds.get(0).toString())),
                                    withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString())))))
                            .thatMatchesSchema()
            );
        }

        assertThat(
                envelopeFrom(envelopes.get(caseCount * 3).metadata(), envelopes.get(caseCount * 3).payload()), jsonEnvelope(
                        metadata().withName("public.hearing.initiated"),
                        payloadIsJson(withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString()))))
                        .thatMatchesSchema()
        );
    }
}