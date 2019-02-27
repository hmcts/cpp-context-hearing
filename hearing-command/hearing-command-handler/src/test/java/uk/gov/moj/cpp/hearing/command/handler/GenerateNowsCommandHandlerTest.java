package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.generateNowsRequestTemplate;

import uk.gov.justice.core.courts.CreateNowsRequest;
import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.dispatcher.SystemUserProvider;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.enforcement.Acknowledgement;
import uk.gov.moj.cpp.hearing.command.enforcement.EnforceFinancialImpositionAcknowledgement;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.ResultLineReference;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.SaveNowsVariantsCommand;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantKey;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantValue;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.NowsVariantsSavedEvent;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.PendingNowsRequestedCommand;
import uk.gov.moj.cpp.hearing.message.shareResults.VariantStatus;
import uk.gov.moj.cpp.hearing.nows.events.EnforcementError;
import uk.gov.moj.cpp.hearing.nows.events.NowsMaterialStatusUpdated;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;
import uk.gov.moj.cpp.hearing.nows.events.PendingNowsRequested;
import uk.gov.moj.cpp.systemidmapper.client.SystemIdMapperClient;
import uk.gov.moj.cpp.systemidmapper.client.SystemIdMapping;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GenerateNowsCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            NowsRequested.class,
            NowsMaterialStatusUpdated.class,
            NowsVariantsSavedEvent.class,
            PendingNowsRequested.class,
            EnforcementError.class
    );

    @Mock
    private SystemUserProvider userProvider;

    @Mock
    private SystemIdMapperClient idMapperClient;

    @Mock
    private EventStream eventStream;
    @Mock
    private EventSource eventSource;
    @Mock
    private AggregateService aggregateService;
    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;
    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;
    @InjectMocks
    private GenerateNowsCommandHandler generateNowsCommandHandler;

    private final String HEARING_ID = "cpp.hearing.hearingId";
    private final String REQUEST_ID = "cpp.hearing.requestId";

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void saveVariantsTest() throws Throwable {

        final SaveNowsVariantsCommand command = createSampleNowsVariantsCommand();

        setupMockedEventStream(command.getHearingId(), this.eventStream, new HearingAggregate());

        final JsonEnvelope commandEnvelope = envelopeFrom(metadataWithRandomUUID("hearing.command.save-nows-variants"), objectToJsonObjectConverter.convert(command));

        this.generateNowsCommandHandler.saveNowsVariants(commandEnvelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(commandEnvelope)
                                .withName("hearing.nows-variants-saved"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", equalTo(command.getHearingId().toString())),
                                withJsonPath("$.variants.[0].key.defendantId", is(command.getVariants().get(0).getKey().getDefendantId().toString()))
                        )))
        ));

    }

    @Test
    public void pendingNowsRequested() throws Throwable {

        final UUID defendantId = UUID.randomUUID();

        final CreateNowsRequest nowsRequest = generateNowsRequestTemplate(defendantId);

        final UUID hearingId = nowsRequest.getHearing().getId();

        final PendingNowsRequestedCommand command = PendingNowsRequestedCommand.pendingNowsRequestedCommand().setCreateNowsRequest(nowsRequest);

        setupMockedEventStream(hearingId, this.eventStream, new HearingAggregate());

        final JsonEnvelope commandEnvelope = envelopeFrom(metadataWithRandomUUID("hearing.command.pending-nows-requested"), objectToJsonObjectConverter.convert(command));

        this.generateNowsCommandHandler.pendingNowsRequested(commandEnvelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(commandEnvelope)
                                .withName("hearing.events.pending-nows-requested"),
                        payloadIsJson(allOf(
                                withJsonPath("$.createNowsRequest.hearing.id", equalTo(command.getCreateNowsRequest().getHearing().getId().toString()))
                        )))
        ));

    }

    @Test
    public void applyEnforcementAcknowledgementTest() throws Exception {


        final UUID defendantId = UUID.randomUUID();
        final CreateNowsRequest nowsRequest = generateNowsRequestTemplate(defendantId);
        final UUID requestId = nowsRequest.getNows().get(0).getId();
        final String accountNumber = "201366829";

        final EnforceFinancialImpositionAcknowledgement command = createApplyEnforcementAcknowledgementCommand(requestId, accountNumber);

        final HearingAggregate hearingAggregate = new HearingAggregate();

        hearingAggregate.apply(new PendingNowsRequested(nowsRequest));

        final UUID contextSystemUserId = randomUUID();

        final SystemIdMapping systemIdMapping = mock(SystemIdMapping.class);

        final UUID hearingId = nowsRequest.getHearing().getId();

        setupMockedEventStream(hearingId, eventStream, hearingAggregate);

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.apply-enforcement-acknowledgement"), objectToJsonObjectConverter.convert(command));

        when(userProvider.getContextSystemUserId()).thenReturn(of(contextSystemUserId));

        when(idMapperClient.findBy(requestId.toString(), REQUEST_ID, HEARING_ID, contextSystemUserId)).thenReturn(of(systemIdMapping));

        when(systemIdMapping.getTargetId()).thenReturn(hearingId);

        generateNowsCommandHandler.applyEnforcementAcknowledgement(envelope);

        verify(userProvider).getContextSystemUserId();

        final List<JsonEnvelope> eventEnvelopes = verifyAppendAndGetArgumentFrom(eventStream).collect(Collectors.toList());
        assertThat(eventEnvelopes.size(), is(1));
        assertThat(eventEnvelopes.get(0).metadata().name(), is("hearing.events.nows-requested"));
        final CreateNowsRequest createNowsRequest = jsonObjectToObjectConverter.convert(eventEnvelopes.get(0).payloadAsJsonObject().getJsonObject("createNowsRequest"), CreateNowsRequest.class);
        assertThat(requestId.toString(), is(command.getRequestId().toString()));
        assertThat(createNowsRequest.getHearing().getId(), is(nowsRequest.getHearing().getId()));
        assertThat(createNowsRequest.getNowTypes().get(0).getId(), is(nowsRequest.getNowTypes().get(0).getId()));
        assertThat(createNowsRequest.getSharedResultLines().get(0).getId().toString(), is(nowsRequest.getSharedResultLines().get(0).getId().toString()));
        assertThat(createNowsRequest.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId().toString(), is(defendantId.toString()));
    }

    @Test
    public void enforcementAcknowledgementErrorTest() throws Exception {
        final UUID defendantId = UUID.randomUUID();
        final CreateNowsRequest nowsRequest = generateNowsRequestTemplate(defendantId);
        final EnforceFinancialImpositionAcknowledgement command = createEnforcementAcknowledgementErrorCommand(nowsRequest.getNows().get(0).getId());
        final HearingAggregate hearingAggregate = new HearingAggregate();

        final UUID contextSystemUserId = randomUUID();

        final SystemIdMapping systemIdMapping = mock(SystemIdMapping.class);

        final UUID hearingId = nowsRequest.getHearing().getId();

        final UUID requestId = nowsRequest.getNows().get(0).getId();

        setupMockedEventStream(hearingId, eventStream, hearingAggregate);

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.apply-enforcement-acknowledgement"),
                objectToJsonObjectConverter.convert(command));

        when(userProvider.getContextSystemUserId()).thenReturn(of(contextSystemUserId));

        when(idMapperClient.findBy(requestId.toString(), REQUEST_ID, HEARING_ID, contextSystemUserId)).thenReturn(of(systemIdMapping));

        when(systemIdMapping.getTargetId()).thenReturn(hearingId);

        generateNowsCommandHandler.enforcementAcknowledgementError(envelope);

        verify(userProvider).getContextSystemUserId();

        final List<JsonEnvelope> eventEnvelopes = verifyAppendAndGetArgumentFrom(eventStream).collect(Collectors.toList());
        assertThat(eventEnvelopes.size(), is(1));
        assertThat(eventEnvelopes.get(0).metadata().name(), is("hearing.events.enforcement-error"));

    }


    private <T extends Aggregate> void setupMockedEventStream(UUID id, EventStream eventStream, T aggregate) {
        when(this.eventSource.getStreamById(id)).thenReturn(eventStream);
        Class<T> clz = (Class<T>) aggregate.getClass();
        when(this.aggregateService.get(eventStream, clz)).thenReturn(aggregate);
    }

    private EnforceFinancialImpositionAcknowledgement createApplyEnforcementAcknowledgementCommand(final UUID nowsId, final String accountNumber) {
        return EnforceFinancialImpositionAcknowledgement.enforceFinancialImpositionAcknowledgement()
                .withOriginator("courts")
                .withRequestId(nowsId)
                .withAcknowledgement(Acknowledgement.acknowledgement().withAccountNumber(accountNumber).build())
                .build();
    }

    private EnforceFinancialImpositionAcknowledgement createEnforcementAcknowledgementErrorCommand(final UUID nowsId) {
        return EnforceFinancialImpositionAcknowledgement.enforceFinancialImpositionAcknowledgement()
                .withOriginator("courts")
                .withRequestId(nowsId)
                .withAcknowledgement(Acknowledgement.acknowledgement().withErrorCode(STRING.next()).withErrorMessage(STRING.next()).build())
                .build();
    }

    public SaveNowsVariantsCommand createSampleNowsVariantsCommand() {
        final UUID hearingId = UUID.randomUUID();
        final UUID defendantId = UUID.randomUUID();
        final UUID nowsTypeId = UUID.randomUUID();
        return SaveNowsVariantsCommand.saveNowsVariantsCommand()
                .setHearingId(hearingId)
                .setVariants(
                        Arrays.asList(
                                Variant.variant()
                                        .setKey(
                                                VariantKey.variantKey()
                                                        .setHearingId(hearingId)
                                                        .setDefendantId(defendantId)
                                                        .setNowsTypeId(nowsTypeId)
                                                        .setUsergroups(Arrays.asList("Listings Officers", "Court Clerks"))
                                        )
                                        .setValue(
                                                VariantValue.variantValue()
                                                        .setMaterialId(UUID.randomUUID())
                                                        .setStatus(VariantStatus.BUILDING)
                                                        .setResultLines(
                                                                Arrays.asList(
                                                                        ResultLineReference.resultLineReference()
                                                                                .setLastSharedTime(ZonedDateTime.now())
                                                                                .setResultLineId(UUID.randomUUID()),
                                                                        ResultLineReference.resultLineReference()
                                                                                .setLastSharedTime(ZonedDateTime.now())
                                                                                .setResultLineId(UUID.randomUUID())
                                                                )
                                                        )
                                        ),
                                Variant.variant()
                                        .setKey(
                                                VariantKey.variantKey()
                                                        .setHearingId(hearingId)
                                                        .setDefendantId(defendantId)
                                                        .setNowsTypeId(nowsTypeId)
                                                        .setUsergroups(Arrays.asList("System Users"))
                                        )
                                        .setValue(
                                                VariantValue.variantValue()
                                                        .setMaterialId(UUID.randomUUID())
                                                        .setStatus(VariantStatus.BUILDING)
                                                        .setResultLines(
                                                                Arrays.asList(
                                                                        ResultLineReference.resultLineReference()
                                                                                .setLastSharedTime(null)
                                                                                .setResultLineId(UUID.randomUUID()),
                                                                        ResultLineReference.resultLineReference()
                                                                                .setLastSharedTime(ZonedDateTime.now())
                                                                                .setResultLineId(UUID.randomUUID())
                                                                )
                                                        )
                                        )

                        )
                );
    }
}