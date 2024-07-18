package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.command.handler.util.EventStreamHelperUtil.verifyAndGetEvents;
import static uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.justice.core.courts.ProsecutionCounsel;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.CasesUpdatedAfterCaseRemovedFromGroupCases;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselUpdated;

import java.util.List;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateHearingAfterCaseRemovedFromGroupCasesCommandHandlerTest {
    private static final UUID HEARING1_ID = randomUUID();
    private static final UUID GROUP_ID = randomUUID();
    private static final UUID CASE1_ID = randomUUID();
    private static final UUID CASE2_ID = randomUUID();
    private static final UUID CASE3_ID = randomUUID();

    @InjectMocks
    private UpdateHearingAfterCaseRemovedFromGroupCasesCommandHandler handler;

    @Mock
    private EventSource eventSource;

    @Mock
    private EventStream eventStream;

    @Mock
    private AggregateService aggregateService;

    @Spy
    private final Enveloper enveloper = EnveloperFactory
            .createEnveloperWithEvents(ProsecutionCounselUpdated.class, CasesUpdatedAfterCaseRemovedFromGroupCases.class);

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    private HearingAggregate hearingAggregate;

    @Before
    public void setup() {
        hearingAggregate = new HearingAggregate();
        when(eventSource.getStreamById(any())).thenReturn(eventStream);
        when(aggregateService.get(eventStream, HearingAggregate.class)).thenReturn(hearingAggregate);
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldCreateEventsAndUpdateAggregate_WhenMemberCaseRemovedFromGroupCases() throws EventStreamException {
        final ProsecutionCounsel prosecutionCounsel = getProsecutionCounsel(CASE1_ID);
        setInitialDataIntoHearingAggregate(HEARING1_ID, GROUP_ID, prosecutionCounsel, CASE1_ID);

        handler.updateHearingAfterCaseRemovedFromGroupCases(getJsonEnvelopeForRemoveCommand(HEARING1_ID, GROUP_ID,
                getProsecutionCase(GROUP_ID, CASE2_ID, Boolean.FALSE, Boolean.FALSE),
                null));

        final List<JsonEnvelope> events = verifyAndGetEvents(eventStream, 2);
        final ProsecutionCounselUpdated prosecutionCounselUpdated = asPojo(events.get(0), ProsecutionCounselUpdated.class);
        final CasesUpdatedAfterCaseRemovedFromGroupCases casesUpdated = asPojo(events.get(1), CasesUpdatedAfterCaseRemovedFromGroupCases.class);

        assertProsecutionCounselUpdated(prosecutionCounselUpdated, asList(CASE1_ID, CASE2_ID));
        assertCasesUpdated(casesUpdated, GROUP_ID, CASE2_ID, null);
        assertHearingAggregateValues(GROUP_ID, asList(CASE1_ID, CASE2_ID), CASE1_ID);
    }

    @Test
    public void shouldCreateEventsAndUpdateAggregate_WhenMasterCaseRemovedFromGroupCases() throws EventStreamException {
        final ProsecutionCounsel prosecutionCounsel = getProsecutionCounsel(CASE1_ID);
        setInitialDataIntoHearingAggregate(HEARING1_ID, GROUP_ID, prosecutionCounsel, CASE1_ID);

        handler.updateHearingAfterCaseRemovedFromGroupCases(getJsonEnvelopeForRemoveCommand(HEARING1_ID, GROUP_ID,
                getProsecutionCase(GROUP_ID, CASE1_ID, Boolean.FALSE, Boolean.FALSE),
                getProsecutionCase(GROUP_ID, CASE3_ID, Boolean.TRUE, Boolean.TRUE)));

        final List<JsonEnvelope> events = verifyAndGetEvents(eventStream, 2);
        final ProsecutionCounselUpdated prosecutionCounselUpdated = asPojo(events.get(0), ProsecutionCounselUpdated.class);
        final CasesUpdatedAfterCaseRemovedFromGroupCases casesUpdated = asPojo(events.get(1), CasesUpdatedAfterCaseRemovedFromGroupCases.class);

        assertProsecutionCounselUpdated(prosecutionCounselUpdated, asList(CASE1_ID, CASE3_ID));
        assertCasesUpdated(casesUpdated, GROUP_ID, CASE1_ID, CASE3_ID);
        assertHearingAggregateValues(GROUP_ID, asList(CASE1_ID, CASE3_ID), CASE3_ID);
    }

    private void assertProsecutionCounselUpdated(final ProsecutionCounselUpdated prosecutionCounselUpdated, final List<UUID> caseIds) {
        assertThat(prosecutionCounselUpdated.getHearingId(), is(HEARING1_ID));
        assertThat(prosecutionCounselUpdated.getProsecutionCounsel().getProsecutionCases().size(), equalTo(caseIds.size()));
        prosecutionCounselUpdated.getProsecutionCounsel().getProsecutionCases().forEach(pcId -> {
            assertThat(caseIds.contains(pcId), equalTo(Boolean.TRUE));
        });
    }

    private void assertCasesUpdated(final CasesUpdatedAfterCaseRemovedFromGroupCases casesUpdated,
                                    final UUID groupId, final UUID removedCaseId, final UUID newGroupMasterId) {
        assertThat(casesUpdated.getGroupId(), is(GROUP_ID));
        assertThat(casesUpdated.getRemovedCase().getId(), equalTo(removedCaseId));
        assertThat(casesUpdated.getRemovedCase().getGroupId(), equalTo(groupId));
        assertThat(casesUpdated.getRemovedCase().getIsCivil(), equalTo(Boolean.TRUE));
        assertThat(casesUpdated.getRemovedCase().getIsGroupMember(), equalTo(Boolean.FALSE));
        assertThat(casesUpdated.getRemovedCase().getIsGroupMaster(), equalTo(Boolean.FALSE));
        if (nonNull(newGroupMasterId)) {
            assertThat(casesUpdated.getNewGroupMaster().getId(), equalTo(newGroupMasterId));
            assertThat(casesUpdated.getNewGroupMaster().getGroupId(), equalTo(groupId));
            assertThat(casesUpdated.getNewGroupMaster().getIsCivil(), equalTo(Boolean.TRUE));
            assertThat(casesUpdated.getNewGroupMaster().getIsGroupMember(), equalTo(Boolean.TRUE));
            assertThat(casesUpdated.getNewGroupMaster().getIsGroupMaster(), equalTo(Boolean.TRUE));
        }
    }

    private void assertHearingAggregateValues(final UUID groupId, final List<UUID> caseIds, final UUID groupMaster) {
        final ProsecutionCounsel prosecutionCounsel = hearingAggregate.getProsecutionCounsels().values().stream().findFirst().get();
        assertThat(prosecutionCounsel.getProsecutionCases().size(), equalTo(caseIds.size()));
        prosecutionCounsel.getProsecutionCases().forEach(pcId -> {
            assertThat(caseIds.contains(pcId), equalTo(Boolean.TRUE));
        });

        assertThat(hearingAggregate.getGroupAndMaster().get(groupId), equalTo(groupMaster));

        assertThat(hearingAggregate.getHearing().getProsecutionCases().size(), equalTo(caseIds.size()));
        hearingAggregate.getHearing().getProsecutionCases().forEach(pc -> {
            assertThat(pc.getIsCivil(), equalTo(Boolean.TRUE));
            assertThat(pc.getGroupId(), equalTo(groupId));
            if (pc.getId().equals(groupMaster)) {
                assertThat(pc.getIsGroupMember(), equalTo(Boolean.TRUE));
                assertThat(pc.getIsGroupMaster(), equalTo(Boolean.TRUE));
            } else {
                assertThat(pc.getIsGroupMember(), equalTo(Boolean.FALSE));
                assertThat(pc.getIsGroupMaster(), equalTo(Boolean.FALSE));
            }
        });
    }

    private JsonEnvelope getJsonEnvelopeForRemoveCommand(final UUID hearingId, final UUID groupId,
                                                         final ProsecutionCase removedCase, final ProsecutionCase newGroupMaster) {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("groupId", groupId.toString())
                .add("removedCase", objectToJsonObjectConverter.convert(removedCase));

        if (nonNull(newGroupMaster)) {
            builder.add("newGroupMaster", objectToJsonObjectConverter.convert(newGroupMaster));
        }

        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(
                metadataWithRandomUUID("hearing.command.update-hearing-after-case-removed-from-group-cases"),
                builder.build());

        return envelope;
    }

    private HearingAggregate setInitialDataIntoHearingAggregate(final UUID hearingId, final UUID groupId,
                                                                final ProsecutionCounsel prosecutionCounsel,
                                                                final UUID groupMaster) {
        hearingAggregate.initiate(Hearing.hearing()
                .withId(hearingId)
                .withIsGroupProceedings(Boolean.TRUE)
                .withProsecutionCases(asList(getProsecutionCase(groupId, groupMaster, true, true)))
                .withProsecutionCounsels(asList(getProsecutionCounsel(groupMaster)))
                .build());

        hearingAggregate.addProsecutionCounsel(prosecutionCounsel, hearingId);

        assertHearingAndProsecutionCases(groupId, groupMaster);
        assertGroupAndMaster(groupId, groupMaster, 1);
        assertProsecutionCounsel(prosecutionCounsel, 1);

        return hearingAggregate;
    }

    private void assertHearingAndProsecutionCases(final UUID groupId, final UUID groupMaster) {
        assertThat(hearingAggregate.getHearing().getIsGroupProceedings(), equalTo(Boolean.TRUE));
        assertThat(hearingAggregate.getHearing().getProsecutionCases().size(), equalTo(1));
        hearingAggregate.getHearing().getProsecutionCases().forEach(pc -> {
            assertThat(pc.getIsCivil(), equalTo(Boolean.TRUE));
            assertThat(pc.getGroupId(), equalTo(groupId));
            assertThat(pc.getIsGroupMember(), equalTo(Boolean.TRUE));
            if (pc.getId().equals(groupMaster)) {
                assertThat(pc.getIsGroupMaster(), equalTo(Boolean.TRUE));
            } else {
                assertThat(pc.getIsGroupMaster(), equalTo(null));
            }
        });
    }

    private void assertGroupAndMaster(final UUID groupId, final UUID groupMaster, final int size) {
        assertThat(hearingAggregate.getGroupAndMaster().size(), equalTo(size));
        assertThat(hearingAggregate.getGroupAndMaster().get(groupId), equalTo(groupMaster));
    }

    private void assertProsecutionCounsel(final ProsecutionCounsel prosecutionCounsel, final int size) {
        assertThat(hearingAggregate.getProsecutionCounsels().size(), equalTo(size));
        assertThat(hearingAggregate.getProsecutionCounsels().get(prosecutionCounsel.getId()), equalTo(prosecutionCounsel));
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

    private ProsecutionCounsel getProsecutionCounsel(final UUID caseID) {
        final ProsecutionCounsel prosecutionCounsel = ProsecutionCounsel.prosecutionCounsel()
                .withId(randomUUID())
                .withFirstName("randomName")
                .withProsecutionCases(asList(caseID))
                .build();

        return prosecutionCounsel;
    }
}