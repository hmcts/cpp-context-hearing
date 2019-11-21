package uk.gov.moj.cpp.hearing.event;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.event.Framework5Fix.toJsonEnvelope;
import static uk.gov.moj.cpp.hearing.event.NowsTemplates.basicNowsTemplate;
import static uk.gov.moj.cpp.hearing.event.NowsTemplates.resultsSharedTemplate;
import static uk.gov.moj.cpp.hearing.event.NowsTemplates.resultsSharedTemplateForSendingResultSharedForOffence;

import uk.gov.justice.core.courts.CreateNowsRequest;
import uk.gov.justice.core.courts.FinancialOrderDetails;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.Now;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.OffenceResult;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.delegates.AdjournHearingDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.NowsDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.PublishResultsDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.SaveNowVariantsDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.UpdateResultLineStatusDelegate;
import uk.gov.moj.cpp.hearing.event.nows.NowsGenerator;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.relist.ResultsSharedFilter;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class PublishResultsEventProcessorTest {

    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Spy
    @InjectMocks
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter();

    @Spy
    @InjectMocks
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Mock
    private Sender sender;

    @Mock
    private NowsGenerator nowsGenerator;

    @Mock
    private NowsDelegate nowsDelegate;

    @Mock
    private AdjournHearingDelegate adjournHearingDelegate;

    @Mock
    private SaveNowVariantsDelegate saveNowVariantsDelegate;

    @Mock
    private UpdateResultLineStatusDelegate updateResultLineStatusDelegate;

    @Mock
    private PublishResultsDelegate publishResultsDelegate;

    @Mock
    private ResultsSharedFilter resultsSharedFilter;

    @Mock
    private ReferenceDataService referenceDataService;

    @InjectMocks
    private PublishResultsEventProcessor publishResultsEventProcessor;

    @Captor
    private ArgumentCaptor<CreateNowsRequest> createNowsRequestArgumentCaptor;

    @Captor
    private ArgumentCaptor<Sender> senderArgumentCaptor;

    @Captor
    private ArgumentCaptor<JsonEnvelope> eventArgumentCaptor;

    @Captor
    private ArgumentCaptor<List<Target>> targetsArgumentCaptor;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void resultsShared() {

        final ResultsShared resultsShared = resultsSharedTemplate();

        final List<Now> nows = basicNowsTemplate();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));

        when(nowsGenerator.createNows(eq(event), Mockito.any(), Mockito.any())).thenReturn(nows);

        when(jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ResultsShared.class)).thenReturn(resultsShared);

        when(saveNowVariantsDelegate.saveNowsVariants(sender, event, nows, resultsShared)).thenReturn(resultsShared.getVariantDirectory());

        when(nowsDelegate.generateNows(event, nows, resultsShared)).thenReturn(CreateNowsRequest.createNowsRequest().withNows(nows).build());

        when(resultsSharedFilter.filterTargets(any(), any())).thenReturn(resultsShared);

        publishResultsEventProcessor.resultsShared(event);

        verify(nowsDelegate).generateNows(event, nows, resultsShared);

        verify(publishResultsDelegate).shareResults(event, sender, resultsShared);

        verify(updateResultLineStatusDelegate).updateResultLineStatus(sender, event, resultsShared);

        verify(this.sender).send(this.eventArgumentCaptor.capture());

        //TODO the toJsonEnvelope serves to eliminate a framework issue with FW5
        assertThat(toJsonEnvelope(this.eventArgumentCaptor.getValue()),
                jsonEnvelope(metadata().withName("hearing.command.handler.update-offence-results"), payloadIsJson(CoreMatchers.allOf(
                        withJsonPath("$.caseId", is(resultsShared.getHearing().getProsecutionCases().get(0).getId().toString()))))));
    }

    @Test
    public void resultsShared_withNoNewNows() {

        final ResultsShared resultsShared = resultsSharedTemplate();

        final List<Now> nows = Collections.emptyList();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));

        when(nowsGenerator.createNows(eq(event), Mockito.any(), Mockito.any())).thenReturn(nows);

        when(jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ResultsShared.class)).thenReturn(resultsShared);

        when(saveNowVariantsDelegate.saveNowsVariants(sender, event, nows, resultsShared)).thenReturn(resultsShared.getVariantDirectory());

        when(resultsSharedFilter.filterTargets(any(), any())).thenReturn(resultsShared);

        publishResultsEventProcessor.resultsShared(event);

        verifyNoMoreInteractions(nowsDelegate, saveNowVariantsDelegate);

        verify(publishResultsDelegate).shareResults(event, sender, resultsShared);

        verify(updateResultLineStatusDelegate).updateResultLineStatus(sender, event, resultsShared);

        verify(adjournHearingDelegate).execute(resultsShared, event);
    }

    @Test
    public void resultsSharedFinancialCrownCourt() {
        resultsSharedFinancial(true);
    }

    @Test
    public void resultsSharedFinancialMagistratesCourt() {
        resultsSharedFinancial(false);
    }

    public void resultsSharedFinancial(boolean crownCourt) {

        final ResultsShared resultsShared = resultsSharedTemplate();
        resultsShared.getHearing().setJurisdictionType(crownCourt ? JurisdictionType.CROWN : JurisdictionType.MAGISTRATES);

        final List<Now> nows = basicNowsTemplate();
        nows.get(0).setFinancialOrders(FinancialOrderDetails.financialOrderDetails()
                .withAccountReference("TBA")
                .build());

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));

        when(nowsGenerator.createNows(eq(event), Mockito.any(), Mockito.any())).thenReturn(nows);

        when(jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ResultsShared.class)).thenReturn(resultsShared);

        when(saveNowVariantsDelegate.saveNowsVariants(sender, event, nows, resultsShared)).thenReturn(resultsShared.getVariantDirectory());

        when(nowsDelegate.generateNows(event, nows, resultsShared)).thenReturn(CreateNowsRequest.createNowsRequest().withNows(nows).build());

        when(resultsSharedFilter.filterTargets(any(), any())).thenReturn(resultsShared);

        publishResultsEventProcessor.resultsShared(event);

        verify(nowsDelegate).generateNows(event, nows, resultsShared);

        verify(publishResultsDelegate).shareResults(event, sender, resultsShared);

        verify(updateResultLineStatusDelegate).updateResultLineStatus(sender, event, resultsShared);

        if (crownCourt) {
            verify(nowsDelegate).sendNows(
                    senderArgumentCaptor.capture(),
                    eventArgumentCaptor.capture(),
                    createNowsRequestArgumentCaptor.capture(),
                    targetsArgumentCaptor.capture());
        } else {
            verify(nowsDelegate).sendPendingNows(
                    senderArgumentCaptor.capture(),
                    eventArgumentCaptor.capture(),
                    createNowsRequestArgumentCaptor.capture(),
                    targetsArgumentCaptor.capture());
        }
    }

    @Test
    public void resultsSharedForOffence_whenOffenceIsDismissed_expectDismissedResultLabelInPayload() {

        final UUID dismissedResultDefinitionId = UUID.fromString("14d66587-8fbe-424f-a369-b1144f1684e3");

        final ResultsShared resultsShared = resultsSharedTemplateForSendingResultSharedForOffence(dismissedResultDefinitionId);

        final List<Now> nows = basicNowsTemplate();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));

        when(nowsGenerator.createNows(eq(event), Mockito.any(), Mockito.any())).thenReturn(nows);

        when(jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ResultsShared.class)).thenReturn(resultsShared);

        when(saveNowVariantsDelegate.saveNowsVariants(sender, event, nows, resultsShared)).thenReturn(resultsShared.getVariantDirectory());

        when(nowsDelegate.generateNows(event, nows, resultsShared)).thenReturn(CreateNowsRequest.createNowsRequest().withNows(nows).build());

        final ResultLine resultLine = resultsShared.getTargets().stream()
                .flatMap(target -> target.getResultLines().stream())
                .filter(ResultLine::getIsComplete)
                .collect(Collectors.toList()).get(0);

        when(resultsSharedFilter.filterTargets(any(), any())).thenReturn(resultsShared);

        when(referenceDataService.getResultDefinitionById(any(), any(), any())).thenReturn(getResultDefinitionForOffecneResult(resultLine, "F"));

        publishResultsEventProcessor.resultsShared(event);

        verify(nowsDelegate).generateNows(event, nows, resultsShared);

        verify(publishResultsDelegate).shareResults(event, sender, resultsShared);

        verify(updateResultLineStatusDelegate).updateResultLineStatus(sender, event, resultsShared);

        verify(this.sender).send(this.eventArgumentCaptor.capture());

        //TODO the toJsonEnvelope serves to eliminate a framework issue with FW5
        assertThat(toJsonEnvelope(this.eventArgumentCaptor.getValue()),
                jsonEnvelope(metadata().withName("hearing.command.handler.update-offence-results"), payloadIsJson(CoreMatchers.allOf(
                        withJsonPath("$.caseId", is(resultsShared.getHearing().getProsecutionCases().get(0).getId().toString()))
                        , withJsonPath("$.hearingId", is(resultsShared.getHearing().getId().toString()))
                        , withJsonPath("$.resultedOffences[0].offenceId", is(resultsShared.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getId().toString()))
                        , withJsonPath("$.resultedOffences[0].offenceResult", is(OffenceResult.DISMISSED.name()))
                ))));

    }

    @Test
    public void resultsSharedForOffence_whenOffenceisWithDrawn_expectWithdrawnResultLabelInPayload() {

        final UUID dismissedResultDeifinitionId = UUID.fromString("16feb0f2e-8d1e-40c7-af2c-05b28c69e5fc");

        final ResultsShared resultsShared = resultsSharedTemplateForSendingResultSharedForOffence(dismissedResultDeifinitionId);

        final List<Now> nows = basicNowsTemplate();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));

        when(nowsGenerator.createNows(eq(event), Mockito.any(), Mockito.any())).thenReturn(nows);

        when(jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ResultsShared.class)).thenReturn(resultsShared);

        when(saveNowVariantsDelegate.saveNowsVariants(sender, event, nows, resultsShared)).thenReturn(resultsShared.getVariantDirectory());

        when(nowsDelegate.generateNows(event, nows, resultsShared)).thenReturn(CreateNowsRequest.createNowsRequest().withNows(nows).build());

        final ResultLine resultLine = resultsShared.getTargets().stream()
                .flatMap(target -> target.getResultLines().stream())
                .filter(ResultLine::getIsComplete)
                .collect(Collectors.toList()).get(0);

        when(resultsSharedFilter.filterTargets(any(), any())).thenReturn(resultsShared);

        when(referenceDataService.getResultDefinitionById(any(), any(), any())).thenReturn(getResultDefinitionForOffecneResult(resultLine, "F"));

        publishResultsEventProcessor.resultsShared(event);

        verify(nowsDelegate).generateNows(event, nows, resultsShared);

        verify(publishResultsDelegate).shareResults(event, sender, resultsShared);

        verify(updateResultLineStatusDelegate).updateResultLineStatus(sender, event, resultsShared);

        verify(this.sender).send(this.eventArgumentCaptor.capture());

        //TODO the toJsonEnvelope serves to eliminate a framework issue with FW5
        assertThat(toJsonEnvelope(this.eventArgumentCaptor.getValue()),
                jsonEnvelope(metadata().withName("hearing.command.handler.update-offence-results"), payloadIsJson(CoreMatchers.allOf(
                        withJsonPath("$.caseId", is(resultsShared.getHearing().getProsecutionCases().get(0).getId().toString()))
                        , withJsonPath("$.hearingId", is(resultsShared.getHearing().getId().toString()))
                        , withJsonPath("$.resultedOffences[0].offenceId", is(resultsShared.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getId().toString()))
                        , withJsonPath("$.resultedOffences[0].offenceResult", is(OffenceResult.WITHDRAWN.name()))
                ))));

    }

    @Test
    public void resultsSharedForOffence_whenOffenceisGuilty_expectGuiltyResultLabelInPayload() {

        final UUID dismissedResultDeifinitionId = UUID.randomUUID();

        final ResultsShared resultsShared = resultsSharedTemplateForSendingResultSharedForOffence(dismissedResultDeifinitionId);

        final List<Now> nows = basicNowsTemplate();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));

        when(nowsGenerator.createNows(eq(event), Mockito.any(), Mockito.any())).thenReturn(nows);

        when(jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ResultsShared.class)).thenReturn(resultsShared);

        when(saveNowVariantsDelegate.saveNowsVariants(sender, event, nows, resultsShared)).thenReturn(resultsShared.getVariantDirectory());

        when(nowsDelegate.generateNows(event, nows, resultsShared)).thenReturn(CreateNowsRequest.createNowsRequest().withNows(nows).build());

        final ResultLine resultLine = resultsShared.getTargets().stream()
                .flatMap(target -> target.getResultLines().stream())
                .filter(ResultLine::getIsComplete)
                .collect(Collectors.toList()).get(0);

        when(resultsSharedFilter.filterTargets(any(), any())).thenReturn(resultsShared);

        when(referenceDataService.getResultDefinitionById(any(), any(), any())).thenReturn(getResultDefinitionForOffecneResult(resultLine, "F"));

        publishResultsEventProcessor.resultsShared(event);

        verify(nowsDelegate).generateNows(event, nows, resultsShared);

        verify(publishResultsDelegate).shareResults(event, sender, resultsShared);

        verify(updateResultLineStatusDelegate).updateResultLineStatus(sender, event, resultsShared);

        verify(this.sender).send(this.eventArgumentCaptor.capture());

        //TODO the toJsonEnvelope serves to eliminate a framework issue with FW5
        assertThat(toJsonEnvelope(this.eventArgumentCaptor.getValue()),
                jsonEnvelope(metadata().withName("hearing.command.handler.update-offence-results"), payloadIsJson(CoreMatchers.allOf(
                        withJsonPath("$.caseId", is(resultsShared.getHearing().getProsecutionCases().get(0).getId().toString()))
                        , withJsonPath("$.hearingId", is(resultsShared.getHearing().getId().toString()))
                        , withJsonPath("$.resultedOffences[0].offenceId", is(resultsShared.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getId().toString()))
                        , withJsonPath("$.resultedOffences[0].offenceResult", is(OffenceResult.GUILTY.name()))
                ))));

    }

    @Test
    public void resultsSharedForOffence_whenOffenceisAdjourned_expectAdjournedResultLabelInPayload() {

        final UUID dismissedResultDeifinitionId = UUID.randomUUID();

        final ResultsShared resultsShared = resultsSharedTemplateForSendingResultSharedForOffence(dismissedResultDeifinitionId);

        final List<Now> nows = basicNowsTemplate();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));

        when(nowsGenerator.createNows(eq(event), Mockito.any(), Mockito.any())).thenReturn(nows);

        when(jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ResultsShared.class)).thenReturn(resultsShared);

        when(saveNowVariantsDelegate.saveNowsVariants(sender, event, nows, resultsShared)).thenReturn(resultsShared.getVariantDirectory());

        when(nowsDelegate.generateNows(event, nows, resultsShared)).thenReturn(CreateNowsRequest.createNowsRequest().withNows(nows).build());

        final ResultLine resultLine = resultsShared.getTargets().stream()
                .flatMap(target -> target.getResultLines().stream())
                .filter(ResultLine::getIsComplete)
                .collect(Collectors.toList()).get(0);

        when(resultsSharedFilter.filterTargets(any(), any())).thenReturn(resultsShared);

        when(referenceDataService.getResultDefinitionById(any(), any(), any())).thenReturn(getResultDefinitionForOffecneResult(resultLine, "A"));

        publishResultsEventProcessor.resultsShared(event);

        verify(nowsDelegate).generateNows(event, nows, resultsShared);

        verify(publishResultsDelegate).shareResults(event, sender, resultsShared);

        verify(updateResultLineStatusDelegate).updateResultLineStatus(sender, event, resultsShared);

        verify(this.sender).send(this.eventArgumentCaptor.capture());

        //TODO the toJsonEnvelope serves to eliminate a framework issue with FW5
        assertThat(toJsonEnvelope(this.eventArgumentCaptor.getValue()),
                jsonEnvelope(metadata().withName("hearing.command.handler.update-offence-results"), payloadIsJson(CoreMatchers.allOf(
                        withJsonPath("$.caseId", is(resultsShared.getHearing().getProsecutionCases().get(0).getId().toString()))
                        , withJsonPath("$.hearingId", is(resultsShared.getHearing().getId().toString()))
                        , withJsonPath("$.resultedOffences[0].offenceId", is(resultsShared.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getId().toString()))
                        , withJsonPath("$.resultedOffences[0].offenceResult", is(OffenceResult.ADJOURNED.name()))
                ))));

    }

    private ResultDefinition getResultDefinitionForOffecneResult(final ResultLine resultLine, final String category) {
        final ResultDefinition resultDefinition = ResultDefinition.resultDefinition()
                .setPrompts(asList(
                        Prompt.prompt()
                                .setId(resultLine.getPrompts().get(0).getId())

                ))
                .setId(resultLine.getResultDefinitionId())
                .setCategory(category);

        return resultDefinition;

    }

}