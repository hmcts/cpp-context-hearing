package uk.gov.moj.cpp.hearing.event;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.event.NowsTemplates.resultsSharedV3Template;

import uk.gov.justice.hearing.courts.referencedata.OrganisationalUnit;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.common.ReferenceDataLoader;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedSuccess;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV3;
import uk.gov.moj.cpp.hearing.event.delegates.PublishResultsDelegateV3;
import uk.gov.moj.cpp.hearing.event.delegates.UpdateDefendantWithApplicationDetailsDelegateV3;
import uk.gov.moj.cpp.hearing.event.delegates.UpdateResultLineStatusDelegateV3;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import java.util.UUID;

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
public class PublishResultsV3EventProcessorTest {

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter = new JsonObjectConvertersFactory().objectToJsonObjectConverter();

    @Mock
    private Sender sender;

    @Mock
    private UpdateResultLineStatusDelegateV3 updateResultLineStatusDelegate;

    @Mock
    private PublishResultsDelegateV3 publishResultsDelegate;

    @Mock
    private UpdateDefendantWithApplicationDetailsDelegateV3 updateDefendantWithApplicationDetailsDelegate;

    @Mock
    private ReferenceDataService referenceDataService;

    @Mock
    private ReferenceDataLoader referenceDataLoader;

    @Mock
    private NewTargetToLegacyTargetConverter newTargetToLegacyTargetConverter;

    @InjectMocks
    private PublishResultsV3EventProcessor publishResultsEventProcessor;

    @Captor
    private ArgumentCaptor<Sender> senderArgumentCaptor;

    @Captor
    private ArgumentCaptor<JsonEnvelope> eventArgumentCaptor;


    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldHandleShareResult() {
        final ResultsSharedV3 resultsSharedV3 = resultsSharedV3Template();
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.events.results-shared-v3"),
                objectToJsonObjectConverter.convert(resultsSharedV3));

        final UUID resultDefinitionId = randomUUID();
        final ResultDefinition resultDefinition = new ResultDefinition();
        final TreeNode resultDefinitionTreeNode = new TreeNode(resultDefinitionId, resultDefinition);

        when(referenceDataLoader.getOrganisationUnitById(eq(resultsSharedV3.getHearing().getCourtCentre().getId()))).thenReturn(buildOrganisationalUnit());
        when(referenceDataService.getResultDefinitionTreeNodeById(any(), any(), any())).thenReturn(resultDefinitionTreeNode);
        when(jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ResultsSharedV3.class)).thenReturn(resultsSharedV3);
        when(newTargetToLegacyTargetConverter.convert(any())).thenReturn((resultsSharedV3.getTargets()));
        publishResultsEventProcessor.resultsShared(event);

        verify(updateResultLineStatusDelegate).updateDaysResultLineStatus(sender, event, resultsSharedV3);
        verify(publishResultsDelegate).shareResults(any(), any(), any(), any());

    }

    @Test
    public void shouldHandleShareResultSuccess() {
        final UUID hearingId = randomUUID();
        final ResultsSharedSuccess resultsSharedSuccess = ResultsSharedSuccess.builder()
                .withHearingId(hearingId).build();
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.events.results-shared-success"),
                objectToJsonObjectConverter.convert(resultsSharedSuccess));
        publishResultsEventProcessor.resultsSharedSuccess(event);

        verify(sender).send(eventArgumentCaptor.capture());
        final JsonEnvelope envelopeOut = this.eventArgumentCaptor.getValue();
        assertThat(envelopeOut.metadata().name(), CoreMatchers.is("public.events.hearing.hearing-resulted-success"));

    }

    private OrganisationalUnit buildOrganisationalUnit() {
        return OrganisationalUnit.organisationalUnit()
                .withOucode("123ABCD")
                .withIsWelsh(true)
                .withOucodeL3WelshName("Welsh Court Centre")
                .withWelshAddress1("Welsh 1")
                .withWelshAddress2("Welsh 2")
                .withWelshAddress3("Welsh 3")
                .withWelshAddress4("Welsh 4")
                .withWelshAddress5("Welsh 5")
                .withPostcode("LL55 2DF")
                .build();
    }
}
