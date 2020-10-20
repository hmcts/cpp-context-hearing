package uk.gov.moj.cpp.hearing.event;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.event.Framework5Fix.toJsonEnvelope;
import static uk.gov.moj.cpp.hearing.event.NowsTemplates.resultsSharedTemplate;
import static uk.gov.moj.cpp.hearing.event.NowsTemplates.resultsSharedTemplateForSendingResultSharedForOffence;

import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.LjaDetails;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.hearing.courts.referencedata.Address;
import uk.gov.justice.hearing.courts.referencedata.OrganisationalUnit;
import uk.gov.justice.hearing.courts.referencedata.Prosecutor;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.domain.OffenceResult;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.delegates.PublishResultsDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.UpdateDefendantWithApplicationDetailsDelegate;
import uk.gov.moj.cpp.hearing.event.delegates.UpdateResultLineStatusDelegate;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.relist.ResultsSharedFilter;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PublishResultsEventProcessorTest {

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter = new JsonObjectConvertersFactory().objectToJsonObjectConverter();

    @Spy
    private ObjectToJsonValueConverter objectToJsonValueConverter = new JsonObjectConvertersFactory().objectToJsonValueConverter();

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Mock
    private Sender sender;

    @Mock
    private UpdateResultLineStatusDelegate updateResultLineStatusDelegate;

    @Mock
    private PublishResultsDelegate publishResultsDelegate;

    @Mock
    private UpdateDefendantWithApplicationDetailsDelegate updateDefendantWithApplicationDetailsDelegate;

    @Mock
    private ResultsSharedFilter resultsSharedFilter;

    @Mock
    private ReferenceDataService referenceDataService;

    @InjectMocks
    private PublishResultsEventProcessor publishResultsEventProcessor;

    @Captor
    private ArgumentCaptor<Sender> senderArgumentCaptor;

    @Captor
    private ArgumentCaptor<JsonEnvelope> eventArgumentCaptor;

    @Captor
    private ArgumentCaptor<ResultsShared> publishResultDelegateCaptor;

    @Captor
    private ArgumentCaptor<List<Target>> targetsArgumentCaptor;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldShareResultCorrectly() {

        final ResultsShared resultsShared = resultsSharedTemplate();
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));

        when(referenceDataService.getProsecutorById(eq(event), eq(resultsShared.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityId())))
                .thenReturn(prosecutorTemplate());
        when(referenceDataService.getOrganisationUnitById(eq(event), eq(resultsShared.getHearing().getCourtCentre().getId())))
                .thenReturn(OrganisationalUnit.organisationalUnit()
                        .withOucode("123ABCD")
                        .withIsWelsh(true)
                        .withOucodeL3WelshName("Welsh Court Centre")
                        .withWelshAddress1("Welsh 1")
                        .withWelshAddress2("Welsh 2")
                        .withWelshAddress3("Welsh 3")
                        .withWelshAddress4("Welsh 4")
                        .withWelshAddress5("Welsh 5")
                        .withPostcode("LL55 2DF")
                        .build());
        when(jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ResultsShared.class)).thenReturn(resultsShared);

        when(resultsSharedFilter.filterTargets(any(), any())).thenReturn(resultsShared);

        publishResultsEventProcessor.resultsShared(event);

        verify(updateDefendantWithApplicationDetailsDelegate, times(1)).execute(sender, event, resultsShared);

        verify(publishResultsDelegate).shareResults(event, sender, resultsShared);

        verify(updateResultLineStatusDelegate).updateResultLineStatus(sender, event, resultsShared);

        verify(this.sender).send(this.eventArgumentCaptor.capture());

        //TODO the toJsonEnvelope serves to eliminate a framework issue with FW5
        assertThat(toJsonEnvelope(this.eventArgumentCaptor.getValue()),
                jsonEnvelope(metadata().withName("hearing.command.handler.update-offence-results"), payloadIsJson(CoreMatchers.allOf(
                        withJsonPath("$.caseId", is(resultsShared.getHearing().getProsecutionCases().get(0).getId().toString()))))));
    }

    @Test
    public void shouldShareResultWithNoNewNows() {

        final ResultsShared resultsShared = resultsSharedTemplate();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));
        when(referenceDataService.getProsecutorById(eq(event), eq(resultsShared.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityId())))
                .thenReturn(prosecutorTemplate());
        when(referenceDataService.getOrganisationUnitById(eq(event), eq(resultsShared.getHearing().getCourtCentre().getId())))
                .thenReturn(OrganisationalUnit.organisationalUnit()
                        .withOucode("123ABCD")
                        .withIsWelsh(true)
                        .withOucodeL3WelshName("Welsh Court Centre")
                        .withWelshAddress1("Welsh 1")
                        .withWelshAddress2("Welsh 2")
                        .withWelshAddress3("Welsh 3")
                        .withWelshAddress4("Welsh 4")
                        .withWelshAddress5("Welsh 5")
                        .withPostcode("LL55 2DF")
                        .build());
        when(jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ResultsShared.class)).thenReturn(resultsShared);
        when(resultsSharedFilter.filterTargets(any(), any())).thenReturn(resultsShared);

        publishResultsEventProcessor.resultsShared(event);

        verify(publishResultsDelegate).shareResults(event, sender, resultsShared);

        verify(updateResultLineStatusDelegate).updateResultLineStatus(sender, event, resultsShared);
    }

    @Test
    public void shouldShareResultsFinancialCrownCourtCorrectly() {
        resultsSharedFinancial(true);
    }

    @Test
    public void shouldShareResultForFinancialMagistratesCourt() {
        resultsSharedFinancial(false);
    }

    public void resultsSharedFinancial(boolean crownCourt) {

        final ResultsShared resultsShared = resultsSharedTemplate();
        resultsShared.getHearing().setJurisdictionType(crownCourt ? JurisdictionType.CROWN : JurisdictionType.MAGISTRATES);

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));

        when(referenceDataService.getProsecutorById(eq(event), eq(resultsShared.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityId())))
                .thenReturn(prosecutorTemplate());
        when(referenceDataService.getOrganisationUnitById(eq(event), eq(resultsShared.getHearing().getCourtCentre().getId())))
                .thenReturn(OrganisationalUnit.organisationalUnit()
                        .withOucode("123ABCD")
                        .withIsWelsh(true)
                        .withOucodeL3WelshName("Welsh Court Centre")
                        .withWelshAddress1("Welsh 1")
                        .withWelshAddress2("Welsh 2")
                        .withWelshAddress3("Welsh 3")
                        .withWelshAddress4("Welsh 4")
                        .withWelshAddress5("Welsh 5")
                        .withPostcode("LL55 2DF")
                        .build());
        when(jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ResultsShared.class)).thenReturn(resultsShared);

        when(resultsSharedFilter.filterTargets(any(), any())).thenReturn(resultsShared);

        publishResultsEventProcessor.resultsShared(event);

        verify(publishResultsDelegate).shareResults(event, sender, resultsShared);

        verify(updateResultLineStatusDelegate).updateResultLineStatus(sender, event, resultsShared);
    }

    @Test
    public void shouldShareResultForOffenceWhenOffenceIsDismissedAndExpectDismissedResultLabelInPayload() {

        final UUID dismissedResultDefinitionId = UUID.fromString("14d66587-8fbe-424f-a369-b1144f1684e3");

        final ResultsShared resultsShared = resultsSharedTemplateForSendingResultSharedForOffence(dismissedResultDefinitionId);

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));
        when(referenceDataService.getProsecutorById(eq(event), eq(resultsShared.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityId())))
                .thenReturn(prosecutorTemplate());
        when(referenceDataService.getOrganisationUnitById(eq(event), eq(resultsShared.getHearing().getCourtCentre().getId())))
                .thenReturn(OrganisationalUnit.organisationalUnit()
                        .withOucode("123ABCD")
                        .withIsWelsh(true)
                        .withOucodeL3WelshName("Welsh Court Centre")
                        .withWelshAddress1("Welsh 1")
                        .withWelshAddress2("Welsh 2")
                        .withWelshAddress3("Welsh 3")
                        .withWelshAddress4("Welsh 4")
                        .withWelshAddress5("Welsh 5")
                        .withPostcode("LL55 2DF")
                        .build());
        when(jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ResultsShared.class)).thenReturn(resultsShared);

        final ResultLine resultLine = resultsShared.getTargets().stream()
                .flatMap(target -> target.getResultLines().stream())
                .filter(ResultLine::getIsComplete)
                .collect(Collectors.toList()).get(0);

        when(resultsSharedFilter.filterTargets(any(), any())).thenReturn(resultsShared);

        when(referenceDataService.getResultDefinitionById(any(), any(), any())).thenReturn(getResultDefinitionForOffenceResult(resultLine, "F"));

        publishResultsEventProcessor.resultsShared(event);

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
    public void shouldShareResultForOffenceWhenOffenceisWithDrawnAndExpectWithdrawnResultLabelInPayload() {

        final UUID dismissedResultDeifinitionId = UUID.fromString("16feb0f2e-8d1e-40c7-af2c-05b28c69e5fc");

        final ResultsShared resultsShared = resultsSharedTemplateForSendingResultSharedForOffence(dismissedResultDeifinitionId);

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));
        when(referenceDataService.getProsecutorById(eq(event), eq(resultsShared.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityId())))
                .thenReturn(prosecutorTemplate());
        when(referenceDataService.getOrganisationUnitById(eq(event), eq(resultsShared.getHearing().getCourtCentre().getId())))
                .thenReturn(OrganisationalUnit.organisationalUnit()
                        .withOucode("123ABCD")
                        .withIsWelsh(true)
                        .withOucodeL3WelshName("Welsh Court Centre")
                        .withWelshAddress1("Welsh 1")
                        .withWelshAddress2("Welsh 2")
                        .withWelshAddress3("Welsh 3")
                        .withWelshAddress4("Welsh 4")
                        .withWelshAddress5("Welsh 5")
                        .withPostcode("LL55 2DF")
                        .build());
        when(jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ResultsShared.class)).thenReturn(resultsShared);

        final ResultLine resultLine = resultsShared.getTargets().stream()
                .flatMap(target -> target.getResultLines().stream())
                .filter(ResultLine::getIsComplete)
                .collect(Collectors.toList()).get(0);

        when(resultsSharedFilter.filterTargets(any(), any())).thenReturn(resultsShared);

        when(referenceDataService.getResultDefinitionById(any(), any(), any())).thenReturn(getResultDefinitionForOffenceResult(resultLine, "F"));

        publishResultsEventProcessor.resultsShared(event);

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
    public void shouldShareResultForOffenceWhenOffenceisGuiltyAndExpectGuiltyResultLabelInPayload() {

        final UUID dismissedResultDeifinitionId = UUID.randomUUID();

        final ResultsShared resultsShared = resultsSharedTemplateForSendingResultSharedForOffence(dismissedResultDeifinitionId);

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));
        when(referenceDataService.getProsecutorById(eq(event), eq(resultsShared.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityId())))
                .thenReturn(prosecutorTemplate());
        when(referenceDataService.getOrganisationUnitById(eq(event), eq(resultsShared.getHearing().getCourtCentre().getId())))
                .thenReturn(OrganisationalUnit.organisationalUnit()
                        .withOucode("123ABCD")
                        .withIsWelsh(true)
                        .withOucodeL3WelshName("Welsh Court Centre")
                        .withWelshAddress1("Welsh 1")
                        .withWelshAddress2("Welsh 2")
                        .withWelshAddress3("Welsh 3")
                        .withWelshAddress4("Welsh 4")
                        .withWelshAddress5("Welsh 5")
                        .withPostcode("LL55 2DF")
                        .build());
        when(jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ResultsShared.class)).thenReturn(resultsShared);

        final ResultLine resultLine = resultsShared.getTargets().stream()
                .flatMap(target -> target.getResultLines().stream())
                .filter(ResultLine::getIsComplete)
                .collect(Collectors.toList()).get(0);

        when(resultsSharedFilter.filterTargets(any(), any())).thenReturn(resultsShared);

        when(referenceDataService.getResultDefinitionById(any(), any(), any())).thenReturn(getResultDefinitionForOffenceResult(resultLine, "F"));

        publishResultsEventProcessor.resultsShared(event);

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
    public void shouldShareResultForOffenceWhenOffenceisAdjournedAndExpectAdjournedResultLabelInPayload() {

        final UUID dismissedResultDeifinitionId = UUID.randomUUID();

        final ResultsShared resultsShared = resultsSharedTemplateForSendingResultSharedForOffence(dismissedResultDeifinitionId);

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));
        when(referenceDataService.getProsecutorById(eq(event), eq(resultsShared.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityId())))
                .thenReturn(prosecutorTemplate());
        when(referenceDataService.getOrganisationUnitById(eq(event), eq(resultsShared.getHearing().getCourtCentre().getId())))
                .thenReturn(OrganisationalUnit.organisationalUnit()
                        .withOucode("123ABCD")
                        .withIsWelsh(true)
                        .withOucodeL3WelshName("Welsh Court Centre")
                        .withWelshAddress1("Welsh 1")
                        .withWelshAddress2("Welsh 2")
                        .withWelshAddress3("Welsh 3")
                        .withWelshAddress4("Welsh 4")
                        .withWelshAddress5("Welsh 5")
                        .withPostcode("LL55 2DF")
                        .build());
        when(jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ResultsShared.class)).thenReturn(resultsShared);

        final ResultLine resultLine = resultsShared.getTargets().stream()
                .flatMap(target -> target.getResultLines().stream())
                .filter(ResultLine::getIsComplete)
                .collect(Collectors.toList()).get(0);

        when(resultsSharedFilter.filterTargets(any(), any())).thenReturn(resultsShared);

        when(referenceDataService.getResultDefinitionById(any(), any(), any())).thenReturn(getResultDefinitionForOffenceResult(resultLine, "A"));

        publishResultsEventProcessor.resultsShared(event);

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

    private ResultDefinition getResultDefinitionForOffenceResult(final ResultLine resultLine, final String category) {
        final ResultDefinition resultDefinition = ResultDefinition.resultDefinition()
                .setPrompts(asList(
                        Prompt.prompt()
                                .setId(resultLine.getPrompts().get(0).getId())

                ))
                .setId(resultLine.getResultDefinitionId())
                .setCategory(category);
        return resultDefinition;
    }

    @Test
    public void shouldNotSendDirectlyWhenNowsIsAttachmentOfEarnings() {

        final ResultsShared resultsShared = resultsSharedTemplate();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));
        when(referenceDataService.getProsecutorById(eq(event), eq(resultsShared.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityId())))
                .thenReturn(prosecutorTemplate());
        when(referenceDataService.getOrganisationUnitById(eq(event), eq(resultsShared.getHearing().getCourtCentre().getId())))
                .thenReturn(OrganisationalUnit.organisationalUnit()
                        .withOucode("123ABCD")
                        .withIsWelsh(true)
                        .withOucodeL3WelshName("Welsh Court Centre")
                        .withWelshAddress1("Welsh 1")
                        .withWelshAddress2("Welsh 2")
                        .withWelshAddress3("Welsh 3")
                        .withWelshAddress4("Welsh 4")
                        .withWelshAddress5("Welsh 5")
                        .withPostcode("LL55 2DF")
                        .build());
        when(resultsSharedFilter.filterTargets(any(), any())).thenReturn(resultsShared);

        publishResultsEventProcessor.resultsShared(event);

    }

    @Test
    public void shouldShareResultAndPopulateProsecutorInformation() {

        final ResultsShared resultsShared = resultsSharedTemplate();
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));
        when(referenceDataService.getProsecutorById(eq(event), eq(resultsShared.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityId())))
                .thenReturn(prosecutorTemplate());
        when(referenceDataService.getOrganisationUnitById(eq(event), eq(resultsShared.getHearing().getCourtCentre().getId())))
                .thenReturn(OrganisationalUnit.organisationalUnit()
                        .withOucode("123ABCD")
                        .withIsWelsh(true)
                        .withOucodeL3WelshName("Welsh Court Centre")
                        .withWelshAddress1("Welsh 1")
                        .withWelshAddress2("Welsh 2")
                        .withWelshAddress3("Welsh 3")
                        .withWelshAddress4("Welsh 4")
                        .withWelshAddress5("Welsh 5")
                        .withPostcode("LL55 2DF")
                        .build());
        when(jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ResultsShared.class)).thenReturn(resultsShared);
        when(resultsSharedFilter.filterTargets(any(), any())).thenReturn(resultsShared);

        publishResultsEventProcessor.resultsShared(event);

        verify(updateDefendantWithApplicationDetailsDelegate, times(1)).execute(sender, event, resultsShared);
        verify(this.publishResultsDelegate).shareResults(eventArgumentCaptor.capture(), senderArgumentCaptor.capture(), this.publishResultDelegateCaptor.capture());
        verify(updateResultLineStatusDelegate).updateResultLineStatus(sender, event, resultsShared);
        verify(this.sender).send(this.eventArgumentCaptor.capture());
        verifyProsecutionCaseIdentifier(this.publishResultDelegateCaptor.getValue());
    }

    private Prosecutor prosecutorTemplate() {
        return Prosecutor.prosecutor()
                .withId(UUID.randomUUID().toString())
                .withFullName("Full Name")
                .withOucode("OU code")
                .withAddress(Address.address()
                        .withAddress1("Address line 1")
                        .withAddress2("Address line 2")
                        .withAddress3("Address line 3")
                        .withAddress4("Address line 4")
                        .withAddress5("Address line 5")
                        .withPostcode("MK9 2BQ")
                        .build())
                .withMajorCreditorCode("TFL2")
                .withInformantEmailAddress("informant@email.com")
                .build();
    }

    @Test
    public void shouldShareResultsAndPopulateOrganisationalUnitInformation() {

        final ResultsShared resultsShared = resultsSharedTemplate();
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));
        when(referenceDataService.getProsecutorById(eq(event), eq(resultsShared.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityId())))
                .thenReturn(prosecutorTemplate());
        when(referenceDataService.getOrganisationUnitById(eq(event), eq(resultsShared.getHearing().getCourtCentre().getId())))
                .thenReturn(OrganisationalUnit.organisationalUnit()
                        .withOucode("123ABCD")
                        .withIsWelsh(true)
                        .withOucodeL3WelshName("Welsh Court Centre")
                        .withWelshAddress1("Welsh 1")
                        .withWelshAddress2("Welsh 2")
                        .withWelshAddress3("Welsh 3")
                        .withWelshAddress4("Welsh 4")
                        .withWelshAddress5("Welsh 5")
                        .withPostcode("LL55 2DF")
                        .build());

        when(referenceDataService.getLjaDetails(eq(event), eq(resultsShared.getHearing().getCourtCentre().getId())))
                .thenReturn(LjaDetails.ljaDetails()
                        .withWelshLjaName("Welsh LJA Name")
                        .build());

        when(jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ResultsShared.class)).thenReturn(resultsShared);
        when(resultsSharedFilter.filterTargets(any(), any())).thenReturn(resultsShared);

        publishResultsEventProcessor.resultsShared(event);

        verify(updateDefendantWithApplicationDetailsDelegate, times(1)).execute(sender, event, resultsShared);
        verify(this.publishResultsDelegate).shareResults(eventArgumentCaptor.capture(), senderArgumentCaptor.capture(), this.publishResultDelegateCaptor.capture());
        verify(updateResultLineStatusDelegate).updateResultLineStatus(sender, event, resultsShared);
        verify(this.sender).send(this.eventArgumentCaptor.capture());
        verifyOrganisationalUnitInformation(this.publishResultDelegateCaptor.getValue());
    }

    private void verifyProsecutionCaseIdentifier(ResultsShared resultsShared) {
        final ProsecutionCaseIdentifier prosecutionCaseIdentifier = resultsShared.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier();
        final Prosecutor expectedProsecutor = prosecutorTemplate();
        assertThat(expectedProsecutor.getFullName(), equalTo(prosecutionCaseIdentifier.getProsecutionAuthorityName()));
        assertThat(expectedProsecutor.getOucode(), equalTo(prosecutionCaseIdentifier.getProsecutionAuthorityOUCode()));
        assertThat(expectedProsecutor.getMajorCreditorCode(), equalTo(prosecutionCaseIdentifier.getMajorCreditorCode()));
        assertThat(expectedProsecutor.getAddress().getAddress1(), equalTo(prosecutionCaseIdentifier.getAddress().getAddress1()));
        assertThat(expectedProsecutor.getAddress().getAddress2(), equalTo(prosecutionCaseIdentifier.getAddress().getAddress2()));
        assertThat(expectedProsecutor.getAddress().getAddress3(), equalTo(prosecutionCaseIdentifier.getAddress().getAddress3()));
        assertThat(expectedProsecutor.getAddress().getAddress4(), equalTo(prosecutionCaseIdentifier.getAddress().getAddress4()));
        assertThat(expectedProsecutor.getAddress().getAddress5(), equalTo(prosecutionCaseIdentifier.getAddress().getAddress5()));
        assertThat(expectedProsecutor.getAddress().getPostcode(), equalTo(prosecutionCaseIdentifier.getAddress().getPostcode()));
        assertThat(expectedProsecutor.getInformantEmailAddress(), equalTo(prosecutionCaseIdentifier.getContact().getPrimaryEmail()));
    }

    private void verifyOrganisationalUnitInformation(ResultsShared resultsShared) {

        assertThat("123ABCD", equalTo(resultsShared.getHearing().getCourtCentre().getCode()));
        assertThat("Welsh 1", equalTo(resultsShared.getHearing().getCourtCentre().getWelshAddress().getAddress1()));
        assertThat("Welsh 2", equalTo(resultsShared.getHearing().getCourtCentre().getWelshAddress().getAddress2()));
        assertThat("Welsh 3", equalTo(resultsShared.getHearing().getCourtCentre().getWelshAddress().getAddress3()));
        assertThat("Welsh 4", equalTo(resultsShared.getHearing().getCourtCentre().getWelshAddress().getAddress4()));
        assertThat("Welsh 5", equalTo(resultsShared.getHearing().getCourtCentre().getWelshAddress().getAddress5()));
        assertThat("LL55 2DF", equalTo(resultsShared.getHearing().getCourtCentre().getWelshAddress().getPostcode()));
        assertThat("Welsh LJA Name", equalTo(resultsShared.getHearing().getCourtCentre().getLja().getWelshLjaName()));
        assertTrue(resultsShared.getHearing().getCourtCentre().getWelshCourtCentre());
    }
}
