package uk.gov.moj.cpp.hearing.event;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.json.Json;

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
public class CaseRemovedFromGroupCasesEventProcessorTest {

    private static final UUID HEARING_ID = randomUUID();
    private static final UUID MASTER_CASE_ID = randomUUID();
    private static final UUID CASE_ID = randomUUID();
    private static final UUID GROUP_ID = randomUUID();
    private static final UUID NEW_GROUP_MASTER = randomUUID();

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @InjectMocks
    private CaseRemovedFromGroupCasesEventProcessor processor;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void processPublicEventCaseRemovedFromGroup() {
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("public.progression.case-removed-from-group-cases"),
                Json.createObjectBuilder()
                        .add("groupId", GROUP_ID.toString())
                        .add("masterCaseId", MASTER_CASE_ID.toString())
                        .add("removedCase", objectToJsonObjectConverter.convert(getProsecutionCase(GROUP_ID, CASE_ID, Boolean.FALSE, Boolean.FALSE)))
                        .add("newGroupMaster", objectToJsonObjectConverter.convert(getProsecutionCase(GROUP_ID, NEW_GROUP_MASTER, Boolean.TRUE, Boolean.TRUE)))
                        .build());

        processor.processPublicProgressionCaseRemovedFromGroupCases(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(envelopeArgumentCaptor.getValue(),
                jsonEnvelope(metadata().withName("hearing.command.remove-case-from-group-cases"), payloadIsJson(allOf(
                        withJsonPath("$.groupId", is(GROUP_ID.toString())),
                        withJsonPath("$.masterCaseId", is(MASTER_CASE_ID.toString())),
                        withJsonPath("$.removedCase.id", is(CASE_ID.toString())),
                        withJsonPath("$.removedCase.groupId", is(GROUP_ID.toString())),
                        withJsonPath("$.removedCase.isCivil", is(true)),
                        withJsonPath("$.removedCase.isGroupMember", is(false)),
                        withJsonPath("$.removedCase.isGroupMaster", is(false)),
                        withJsonPath("$.newGroupMaster.id", is(NEW_GROUP_MASTER.toString())),
                        withJsonPath("$.newGroupMaster.groupId", is(GROUP_ID.toString())),
                        withJsonPath("$.newGroupMaster.isCivil", is(true)),
                        withJsonPath("$.newGroupMaster.isGroupMember", is(true)),
                        withJsonPath("$.newGroupMaster.isGroupMaster", is(true))
                ))));
    }

    @Test
    public void processPublicEventCaseRemovedFromGroup_WithOnlyMandatoryFields() {
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("public.progression.case-removed-from-group-cases"),
                Json.createObjectBuilder()
                        .add("groupId", GROUP_ID.toString())
                        .add("masterCaseId", MASTER_CASE_ID.toString())
                        .add("removedCase", objectToJsonObjectConverter.convert(getProsecutionCase(GROUP_ID, CASE_ID, Boolean.FALSE, Boolean.FALSE)))
                        .build());

        processor.processPublicProgressionCaseRemovedFromGroupCases(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(envelopeArgumentCaptor.getValue(),
                jsonEnvelope(metadata().withName("hearing.command.remove-case-from-group-cases"), payloadIsJson(allOf(
                        withJsonPath("$.groupId", is(GROUP_ID.toString())),
                        withJsonPath("$.masterCaseId", is(MASTER_CASE_ID.toString())),
                        withJsonPath("$.removedCase.id", is(CASE_ID.toString())),
                        withJsonPath("$.removedCase.groupId", is(GROUP_ID.toString())),
                        withJsonPath("$.removedCase.isCivil", is(true)),
                        withJsonPath("$.removedCase.isGroupMember", is(false)),
                        withJsonPath("$.removedCase.isGroupMaster", is(false)),
                        withoutJsonPath("$.newGroupMaster")))));
    }

    @Test
    public void processHearingEventCaseRemovedFromGroup() {
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.events.case-removed-from-group-cases"),
                Json.createObjectBuilder()
                        .add("hearingId", HEARING_ID.toString())
                        .add("groupId", GROUP_ID.toString())
                        .add("removedCase", objectToJsonObjectConverter.convert(getProsecutionCase(GROUP_ID, CASE_ID, Boolean.FALSE, Boolean.FALSE)))
                        .add("newGroupMaster", objectToJsonObjectConverter.convert(getProsecutionCase(GROUP_ID, NEW_GROUP_MASTER, Boolean.TRUE, Boolean.TRUE)))
                        .build());

        processor.processHearingEventsCaseRemovedFromGroupCases(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(envelopeArgumentCaptor.getValue(),
                jsonEnvelope(metadata().withName("hearing.command.update-hearing-after-case-removed-from-group-cases"), payloadIsJson(allOf(
                        withJsonPath("$.hearingId", is(HEARING_ID.toString())),
                        withJsonPath("$.groupId", is(GROUP_ID.toString())),
                        withJsonPath("$.removedCase.id", is(CASE_ID.toString())),
                        withJsonPath("$.removedCase.groupId", is(GROUP_ID.toString())),
                        withJsonPath("$.removedCase.isCivil", is(true)),
                        withJsonPath("$.removedCase.isGroupMember", is(false)),
                        withJsonPath("$.removedCase.isGroupMaster", is(false)),
                        withJsonPath("$.newGroupMaster.id", is(NEW_GROUP_MASTER.toString())),
                        withJsonPath("$.newGroupMaster.groupId", is(GROUP_ID.toString())),
                        withJsonPath("$.newGroupMaster.isCivil", is(true)),
                        withJsonPath("$.newGroupMaster.isGroupMember", is(true)),
                        withJsonPath("$.newGroupMaster.isGroupMaster", is(true))
                ))));
    }

    @Test
    public void processHearingEventCaseRemovedFromGroup_WithOnlyMandatoryFields() {
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.events.case-removed-from-group-cases"),
                Json.createObjectBuilder()
                        .add("hearingId", HEARING_ID.toString())
                        .add("groupId", GROUP_ID.toString())
                        .add("removedCase", objectToJsonObjectConverter.convert(getProsecutionCase(GROUP_ID, CASE_ID, Boolean.FALSE, Boolean.FALSE)))
                        .build());

        processor.processHearingEventsCaseRemovedFromGroupCases(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(envelopeArgumentCaptor.getValue(),
                jsonEnvelope(metadata().withName("hearing.command.update-hearing-after-case-removed-from-group-cases"), payloadIsJson(allOf(
                        withJsonPath("$.hearingId", is(HEARING_ID.toString())),
                        withJsonPath("$.groupId", is(GROUP_ID.toString())),
                        withJsonPath("$.removedCase.id", is(CASE_ID.toString())),
                        withJsonPath("$.removedCase.groupId", is(GROUP_ID.toString())),
                        withJsonPath("$.removedCase.isCivil", is(true)),
                        withJsonPath("$.removedCase.isGroupMember", is(false)),
                        withJsonPath("$.removedCase.isGroupMaster", is(false)),
                        withoutJsonPath("$.newGroupMaster")
                ))));
    }

    private ProsecutionCase getProsecutionCase(final UUID groupId, final UUID caseId, final Boolean isGroupMember, final Boolean isGroupMaster) {
        return ProsecutionCase.prosecutionCase()
                .withId(caseId)
                .withIsCivil(Boolean.TRUE)
                .withGroupId(groupId)
                .withIsGroupMember(isGroupMember)
                .withIsGroupMaster(isGroupMaster)
                .withProsecutionCaseIdentifier(ProsecutionCaseIdentifier.prosecutionCaseIdentifier()
                        .withCaseURN(randomUUID().toString())
                        .build())
                .withDefendants(asList(Defendant.defendant()
                        .withId(randomUUID())
                        .withOffences(asList(Offence.offence()
                                .withId(randomUUID())
                                .build()))
                        .build()))
                .build();
    }
}