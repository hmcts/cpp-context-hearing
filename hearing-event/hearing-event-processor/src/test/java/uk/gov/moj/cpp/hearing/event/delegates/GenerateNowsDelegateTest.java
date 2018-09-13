package uk.gov.moj.cpp.hearing.event.delegates;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.event.NowsTemplates.basicNowsTemplate;
import static uk.gov.moj.cpp.hearing.event.NowsTemplates.resultsSharedTemplate;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.NowDefinitionTemplates.standardNowDefinition;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.print;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import uk.gov.justice.json.schemas.core.CourtClerk;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.GenerateNowsCommand;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Material;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.NowResult;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.NowTypes;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Nows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.PromptRef;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Prompts;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.SharedResultLines;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.UserGroups;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.ResultsSharedEventHelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class GenerateNowsDelegateTest {

    @Mock
    private ReferenceDataService referenceDataService;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @Mock
    private Sender sender;

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Spy
    @InjectMocks
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();

    @InjectMocks
    private GenerateNowsDelegate generateNowsDelegate;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGenerateNows() {

        final ResultsSharedEventHelper resultsShared = h(resultsSharedTemplate());

        final CommandHelpers.NowsHelper nows = h(basicNowsTemplate());

        final NowDefinition nowDefinition = standardNowDefinition();

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(resultsShared.getFirstCompletedResultLine().getResultDefinitionId())))
                .thenReturn(nowDefinition);

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));

        generateNowsDelegate.generateNows(sender, event, nows.it(), resultsShared.it());

        verify(sender).send(envelopeArgumentCaptor.capture());

        final JsonEnvelope createNowsMessage = envelopeArgumentCaptor.getValue();

        assertThat(createNowsMessage, jsonEnvelope(metadata().withName("hearing.command.generate-nows"), payloadIsJson(print())));

        assertThat(asPojo(createNowsMessage, GenerateNowsCommand.class), isBean(GenerateNowsCommand.class)
                .with(GenerateNowsCommand::getHearing, isBean(uk.gov.justice.json.schemas.core.Hearing.class))

                .with(GenerateNowsCommand::getCourtClerk, isBean(CourtClerk.class)
                        .with(CourtClerk::getId, is(resultsShared.getCourtClerk().getId()))
                        .with(CourtClerk::getLastName, is(resultsShared.getCourtClerk().getLastName()))
                        .with(CourtClerk::getFirstName, is(resultsShared.getCourtClerk().getFirstName()))
                )
                .with(GenerateNowsCommand::getNows, first(isBean(Nows.class)
                        .with(Nows::getId, is(nows.getFirstNow().getId()))
                        .with(Nows::getNowsTypeId, is(nows.getFirstNow().getNowsTypeId()))
                        .with(Nows::getNowsTemplateName, is(nows.getFirstNow().getNowsTemplateName()))
                        .with(Nows::getDefendantId, is(nows.getFirstNow().getDefendantId()))
                        .with(Nows::getMaterials, first(isBean(Material.class)
                                .with(Material::getId, is(nows.getFirstMaterial().getId()))
                                .with(Material::isAmended, is(nows.getFirstMaterial().isAmended()))
                                .with(Material::getNowResult, first(isBean(NowResult.class)
                                        .with(NowResult::getSharedResultId, is(nows.getFirstNowsResult().getSharedResultId()))
                                        .with(NowResult::getSequence, is(nows.getFirstNowsResult().getSequence()))
                                        .with(NowResult::getPrompts, first(isBean(PromptRef.class)
                                                .with(PromptRef::getId, is(nows.getFirstPrompt().getId()))
                                                .with(PromptRef::getLabel, is(nows.getFirstPrompt().getLabel()))
                                        ))
                                ))
                                .with(Material::getUserGroups, first(isBean(UserGroups.class)
                                        .with(UserGroups::getGroup, is(nows.getFirstUserGroup().getGroup()))
                                ))
                        ))
                ))
                .with(GenerateNowsCommand::getSharedResultLines, first(isBean(SharedResultLines.class)
                        .with(SharedResultLines::getId, is(resultsShared.getHearing().getTargets().get(0).getResultLines().get(0).getResultLineId()))
                        .with(SharedResultLines::getId, is(resultsShared.getFirstCompletedResultLine().getResultLineId()))
                        .with(SharedResultLines::getDefendantId, is(resultsShared.getFirstTarget().getDefendantId()))
                        .with(SharedResultLines::getCaseId, is(resultsShared.getFirstCase().getId()))
                        .with(SharedResultLines::getOffenceId, is(resultsShared.getFirstTarget().getOffenceId()))
                        .with(SharedResultLines::getLevel, is(resultsShared.getFirstCompletedResultLine().getLevel().toString()))
                        .with(SharedResultLines::getLabel, is(resultsShared.getFirstCompletedResultLine().getResultLabel()))
                        .with(SharedResultLines::getPrompts, first(isBean(Prompts.class)
                                .with(Prompts::getId, is(resultsShared.getFirstCompletedResultLineFirstPrompt().getId()))
                                .with(Prompts::getLabel, is(resultsShared.getFirstCompletedResultLineFirstPrompt().getLabel()))
                                .with(Prompts::getValue, is(resultsShared.getFirstCompletedResultLineFirstPrompt().getValue()))
                        ))
                        .with(SharedResultLines::getSharedDate, is(resultsShared.getFirstCompletedResultLineStatus().getLastSharedDateTime()))
                        .with(SharedResultLines::getOrderedDate, is(resultsShared.getFirstCompletedResultLine().getOrderedDate()))
                ))
                .with(GenerateNowsCommand::getNowTypes, first(isBean(NowTypes.class)
                        .with(NowTypes::getId, is(nowDefinition.getId()))
                        .with(NowTypes::getDescription, is(nowDefinition.getName()))
                        .with(NowTypes::getJurisdiction, is(nowDefinition.getJurisdiction()))
                        .with(NowTypes::getPriority, is(nowDefinition.getUrgentTimeLimitInMinutes().toString()))
                        .with(NowTypes::getTemplateName, is(nowDefinition.getTemplateName()))
                        .with(NowTypes::getRank, is(nowDefinition.getRank()))
                        .with(NowTypes::getStaticText, is(nowDefinition.getText() + "\n" + nowDefinition.getResultDefinitions().get(0).getText()))
                        .with(NowTypes::getWelshStaticText, is(nowDefinition.getWelshText() + "\n" + nowDefinition.getResultDefinitions().get(0).getWelshText()))
                        .with(NowTypes::getWelshDescription, is(nowDefinition.getWelshName()))
                        .with(NowTypes::getBilingualTemplateName, is(nowDefinition.getBilingualTemplateName()))
                        .with(NowTypes::getRemotePrintingRequired, is(nowDefinition.getRemotePrintingRequired()))
                ))
        );
    }

    @Test
    public void testGenerateNows_withNullNowText() {

        final ResultsSharedEventHelper resultsShared = h(resultsSharedTemplate());

        final CommandHelpers.NowsHelper nows = h(basicNowsTemplate());

        final NowDefinition nowDefinition = with(standardNowDefinition(), d -> {
            d.setText(null);
            d.setWelshText(null);
        });

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(resultsShared.getFirstCompletedResultLine().getResultDefinitionId())))
                .thenReturn(nowDefinition);

        generateNowsDelegate.generateNows(sender, envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared)), nows.it(), resultsShared.it());

        verify(sender).send(envelopeArgumentCaptor.capture());

        final JsonEnvelope createNowsMessage = envelopeArgumentCaptor.getValue();

        assertThat(asPojo(createNowsMessage, GenerateNowsCommand.class), isBean(GenerateNowsCommand.class)
                .with(GenerateNowsCommand::getHearing, isBean(uk.gov.justice.json.schemas.core.Hearing.class))

                .with(GenerateNowsCommand::getNowTypes, first(isBean(NowTypes.class)

                        .with(NowTypes::getStaticText, is(nowDefinition.getResultDefinitions().get(0).getText()))
                        .with(NowTypes::getWelshStaticText, is(nowDefinition.getResultDefinitions().get(0).getWelshText()))

                ))
        );
    }

    @Test
    public void testGenerateNows_withNullNowText_AndNullResultDefinitionNowText() {
        final ResultsSharedEventHelper resultsShared = h(resultsSharedTemplate());

        final CommandHelpers.NowsHelper nows = h(basicNowsTemplate());

        final NowDefinition nowDefinition = with(standardNowDefinition(), d -> {
            d.setText(null);
            d.getResultDefinitions().get(0).setText(null);
            d.setWelshText(null);
            d.getResultDefinitions().get(0).setWelshText(null);
        });

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(resultsShared.getFirstCompletedResultLine().getResultDefinitionId())))
                .thenReturn(nowDefinition);

        generateNowsDelegate.generateNows(sender, envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared)), nows.it(), resultsShared.it());

        verify(sender).send(envelopeArgumentCaptor.capture());

        final JsonEnvelope createNowsMessage = envelopeArgumentCaptor.getValue();

        assertThat(asPojo(createNowsMessage, GenerateNowsCommand.class), isBean(GenerateNowsCommand.class)
                .with(GenerateNowsCommand::getHearing, isBean(uk.gov.justice.json.schemas.core.Hearing.class))
                .with(GenerateNowsCommand::getNowTypes, first(isBean(NowTypes.class)
                        .with(NowTypes::getStaticText, is(""))
                        .with(NowTypes::getWelshStaticText, is(""))
                ))
        );
    }
}
