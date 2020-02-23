package uk.gov.moj.cpp.hearing.event.delegates;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static io.github.benas.randombeans.FieldDefinitionBuilder.field;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.event.NowsTemplates.basicNowsTemplate;
import static uk.gov.moj.cpp.hearing.event.NowsTemplates.resultsSharedTemplate;
import static uk.gov.moj.cpp.hearing.event.delegates.PublishResultUtil.POUND_CURRENCY_LABEL;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.NowDefinitionTemplates.standardNowDefinition;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.generateFullNowsRequestTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.second;

import uk.gov.justice.core.courts.CreateNowsRequest;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Now;
import uk.gov.justice.core.courts.NowType;
import uk.gov.justice.core.courts.NowVariant;
import uk.gov.justice.core.courts.NowVariantKey;
import uk.gov.justice.core.courts.NowVariantResult;
import uk.gov.justice.core.courts.Personalisation;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.ResultPrompt;
import uk.gov.justice.core.courts.SharedResultLine;
import uk.gov.justice.core.courts.notification.EmailChannel;
import uk.gov.justice.core.courts.nowdocument.NowDocumentRequest;
import uk.gov.justice.hearing.courts.referencedata.CourtCentreOrganisationUnit;
import uk.gov.justice.hearing.courts.referencedata.Courtrooms;
import uk.gov.justice.hearing.courts.referencedata.FixedListCollection;
import uk.gov.justice.hearing.courts.referencedata.FixedListResult;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.NowsRequestedToDocumentConverter;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.service.CourtHouseReverseLookup;
import uk.gov.moj.cpp.hearing.event.service.NowsReferenceDataServiceImpl;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.ResultsSharedEventHelper;
import uk.gov.moj.cpp.hearing.test.TestTemplates;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.JsonObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.benas.randombeans.EnhancedRandomBuilder;
import io.github.benas.randombeans.FieldDefinition;
import io.github.benas.randombeans.api.EnhancedRandom;
import io.github.benas.randombeans.api.Randomizer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NowsDelegateTest {
    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
    @Spy
    @InjectMocks
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();
    @Mock
    private NowsReferenceDataServiceImpl referenceDataService;
    @Mock
    private CourtHouseReverseLookup courtHouseReverseLookup;
    @Captor
    private ArgumentCaptor<Envelope<JsonObject>> envelopeArgumentCaptor;
    @Mock
    private Sender sender;
    @InjectMocks
    private NowsDelegate nowsDelegate;
    @Mock
    private NowsRequestedToDocumentConverter nowsRequestedToDocumentConverter;

    @Test
    public void testGenerateEnglishNows() {
        stubEnglishCourtHouseLookUp();
        final NowDefinition nowDefinition = standardNowDefinition();
        testGenerateNows(nowDefinition, nowDefinition.getTemplateName());
    }

    @Test
    public void testGenerateWelshNows() {
        stubWelshCourtHouseLookUp();
        final NowDefinition nowDefinition = standardNowDefinition();
        testGenerateNows(nowDefinition, nowDefinition.getBilingualTemplateName());
    }

    @Test
    public void testGenerateNowsMultiPrimaries() {
        stubEnglishCourtHouseLookUp();
        final NowDefinition nowDefinition = TestTemplates.multiPrimaryNowDefinition();
        testGenerateNows(nowDefinition, nowDefinition.getTemplateName());
    }

    @Test
    public void testGenerateNows_withNullNowText() {
        stubEnglishCourtHouseLookUp();

        final ResultsSharedEventHelper resultsShared = h(resultsSharedTemplate());

        final CommandHelpers.NowsHelper nows = h(basicNowsTemplate());

        final NowDefinition nowDefinition = with(standardNowDefinition(), nd -> {
            nd.setText(STRING.next());
            nd.setWelshText(STRING.next());
        });

        final UUID resultDefinitionId = resultsShared.getFirstCompletedResultLine().getResultDefinitionId();

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(resultDefinitionId)))
                .thenReturn(new HashSet<>(asList(nowDefinition)));

        // GPE-6752 bulk input resultDefinition, add prompt references
        final ResultDefinition resultDefinition = ResultDefinition.resultDefinition()
                .setPrompts(
                        asList(
                                Prompt.prompt()
                                        .setId(resultsShared.getFirstCompletedResultLineFirstPrompt().getId())
                        )
                )
                .setId(resultDefinitionId);
        when(referenceDataService.getResultDefinitionById(any(), any(), eq(resultDefinitionId)))
                .thenReturn(resultDefinition);

        final CreateNowsRequest nowsRequest = nowsDelegate.generateNows(envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared.it())), nows.it(), resultsShared.it());

        assertThat(nowsRequest, isBean(CreateNowsRequest.class)
                .with(gnc -> gnc.getHearing(), isBean(uk.gov.justice.core.courts.Hearing.class))
                .with(gnc -> gnc.getNowTypes(), first(isBean(NowType.class)
                        .with(NowType::getStaticText, is(nowDefinition.getText()))
                        .with(NowType::getWelshStaticText, is(nowDefinition.getWelshText()))
                ))
        );
    }

    @Test
    public void testGenerateNows_withNullNowText_AndNullResultDefinitionNowText() {
        final ResultsSharedEventHelper resultsShared = h(resultsSharedTemplate());

        final CommandHelpers.NowsHelper nows = h(basicNowsTemplate());

        stubResultDefinition(resultsShared);
        stubEnglishCourtHouseLookUp();

        final CreateNowsRequest nowsRequest = nowsDelegate.generateNows(envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared.it())), nows.it(), resultsShared.it());

        assertThat(nowsRequest, isBean(CreateNowsRequest.class)
                .with(request -> request.getHearing(), isBean(uk.gov.justice.core.courts.Hearing.class))
                .with(request -> request.getNowTypes(), first(isBean(NowType.class)
                        .with(NowType::getStaticText, is(""))
                        .with(NowType::getWelshStaticText, is(""))
                )));
    }

    @Test
    public void testSendNows_shouldSendProgressionGenerateNowsEventEnglish() {
        sendNows_shouldSendProgressionGenerateNowsEvent();
    }

    @Test
    public void testSendNows_shouldSendProgressionGenerateNowsEventWelsh() {
        sendNows_shouldSendProgressionGenerateNowsEvent();
    }

    public void sendNows_shouldSendProgressionGenerateNowsEvent() {
        final UUID defendantId = UUID.randomUUID();
        final TestTemplates.FullNowsRequest fullNowsRequest = generateFullNowsRequestTemplate(defendantId);
        final CreateNowsRequest nowsRequest = fullNowsRequest.getCreateNowsRequest();

        final JsonEnvelope envelope = envelopeFrom(
                metadataWithRandomUUID("hearing.events.nows-requested"),
                objectToJsonObjectConverter.convert(nowsRequest)
        );


        final List<ResultLine> resultLines = fullNowsRequest.getTargets().get(0).getResultLines();

        Map<UUID, Prompt> id2PromptRef = new HashMap<>();
        Map<UUID, uk.gov.justice.core.courts.Prompt> id2Prompt = new HashMap<>();
        Map<UUID, ResultLine> id2ResultLine = new HashMap<>();

        final LocalDate localDate = LocalDate.now();
        final String currencyValue = "12.34";
        final String stringValue = "abcdef";

        resultLines.forEach(
                rl -> {
                    id2ResultLine.put(rl.getResultLineId(), rl);
                    rl.setPrompts(
                            asList(uk.gov.justice.core.courts.Prompt.prompt()
                                            .withId(UUID.randomUUID())
                                            .withValue(" " + localDate.format(DateTimeFormatter.ofPattern(NowsDelegate.INCOMING_PROMPT_DATE_FORMAT)))
                                            .withLabel("a date")
                                            .build(),
                                    uk.gov.justice.core.courts.Prompt.prompt()
                                            .withId(UUID.randomUUID())
                                            .withValue(" " + currencyValue)
                                            .withLabel("the money")
                                            .build(),
                                    uk.gov.justice.core.courts.Prompt.prompt()
                                            .withId(UUID.randomUUID())
                                            .withValue(stringValue)
                                            .withLabel("the string")
                                            .build()

                            )
                    );
                    rl.getPrompts().forEach(p -> {
                        id2Prompt.put(p.getId(), p);
                    });
                    final ResultDefinition resultDefinition = ResultDefinition.resultDefinition()
                            .setPrompts(asList(
                                    Prompt.prompt()
                                            .setId(rl.getPrompts().get(0).getId())
                                            .setLabel("a date")
                                            .setType(NowsDelegate.DATE_PROMPT_TYPE),
                                    Prompt.prompt()
                                            .setId(rl.getPrompts().get(1).getId())
                                            .setLabel("the money")
                                            .setType(NowsDelegate.CURRENCY_PROMPT_TYPE),
                                    Prompt.prompt()
                                            .setId(rl.getPrompts().get(2).getId())
                                            .setLabel("the string")

                            ))
                            .setId(rl.getResultDefinitionId());
                    resultDefinition.getPrompts().forEach(p -> {
                        id2PromptRef.put(p.getId(), p);
                    });

                    doReturn(resultDefinition).when(referenceDataService).getResultDefinitionById(any(), any(), eq(resultDefinition.getId()));

                }
        );

        List<FixedListCollection> fixedLists = new ArrayList<>();
        FixedListCollection fixedListsItem = new FixedListCollection("cjseQualifier", new ArrayList<>(), null, UUID.randomUUID(), null, null);
        fixedLists.add(fixedListsItem);
        FixedListResult fixedListResult = FixedListResult.fixedListResult().withFixedListCollection(fixedLists).build();

        doReturn(fixedListResult).when(referenceDataService).getAllFixedLists(any());


        nowsRequest.getNows().get(0).getRequestedMaterials().stream().flatMap(rm -> rm.getNowResults().stream()).forEach(
                nr -> {
                    ResultLine sr = id2ResultLine.get(nr.getSharedResultId());
                    System.out.println("setting prompts");
                    nr.setPromptRefs(sr.getPrompts().stream().map(p -> p.getId()).collect(Collectors.toList()));
                }
        );

        Map<UUID, ResultPrompt> id2ResultPrompt = new HashMap<>();

        nowsRequest.getSharedResultLines().forEach(
                sharedResultLine -> {
                    ResultLine sr = id2ResultLine.get(sharedResultLine.getId());
                    sharedResultLine.setPrompts(
                            sr.getPrompts().stream().map(
                                    p -> ResultPrompt.resultPrompt()
                                            .withValue(p.getValue())
                                            .withLabel(p.getLabel())
                                            .withId(p.getId()).build()
                            ).peek(
                                    rp -> id2ResultPrompt.put(rp.getId(), rp)
                            )
                                    .collect(Collectors.toList())
                    );
                }
        );

        final EnhancedRandomBuilder enhancedRandomBuilder = EnhancedRandomBuilder.aNewEnhancedRandomBuilder();

        enhancedRandomBuilder
                .randomize(Map.class, (Randomizer<Map>) () -> {
                    final Map<String, String> properties = new HashMap<>();
                    properties.put("ABC", "XYZ");
                    return properties;
                })
                .collectionSizeRange(1, 1)
                .build();

        final EnhancedRandom randomEmailChannelGenerator = enhancedRandomBuilder
                .randomize(Personalisation.class, (Randomizer<Personalisation>) () -> Personalisation.personalisation().withAdditionalProperty("ABC", "XYZ").build())
                .collectionSizeRange(1, 1)
                .build();

        final EmailChannel emailChannel = randomEmailChannelGenerator.nextObject(EmailChannel.class);

        final FieldDefinition<?, ?> emailNotifications = field().named("emailNotifications").ofType(List.class).inClass(NowDocumentRequest.class).get();

        final EnhancedRandom randomGenerator = enhancedRandomBuilder
                .randomize(emailNotifications, (Randomizer<List>) () -> {
                    List<EmailChannel> emailChannels = new ArrayList<>();
                    emailChannels.add(emailChannel);
                    return emailChannels;
                })
                .collectionSizeRange(1, 1)
                .build();

        NowDocumentRequest nowDocumentRequest = randomGenerator.nextObject(NowDocumentRequest.class);

        doReturn(asList(nowDocumentRequest)).when(nowsRequestedToDocumentConverter).convert(any(), any());

        nowsDelegate.sendNows(sender, envelope, nowsRequest, fullNowsRequest.getTargets());

        verify(sender).sendAsAdmin(envelopeArgumentCaptor.capture());

        final List<Envelope<JsonObject>> outgoingMessages = envelopeArgumentCaptor.getAllValues();

        final JsonEnvelope nowDocumentRequestPayload = envelopeFrom(outgoingMessages.get(0).metadata(), outgoingMessages.get(0).payload());

        Assert.assertThat(nowDocumentRequestPayload, jsonEnvelope(
                metadata().withName("public.hearing.now-document-requested"),
                payloadIsJson(allOf(
                        withJsonPath("$.applicationId", equalTo(nowDocumentRequest.getApplicationId().toString())),
                        withJsonPath("$.caseId", equalTo(nowDocumentRequest.getCaseId().toString())),
                        withJsonPath("$.hearingId", equalTo(nowDocumentRequest.getHearingId().toString())),
                        withJsonPath("$.materialId", equalTo(nowDocumentRequest.getMaterialId().toString())),
                        withJsonPath("$.isRemotePrintingRequired", equalTo(nowDocumentRequest.getIsRemotePrintingRequired()))))
        ));

        //check that the prompts were formatted
        id2ResultPrompt.forEach(
                (pid, p) ->
                {
                    Prompt promptRef = id2PromptRef.get(pid);
                    String expectedValue = null;
                    if (NowsDelegate.CURRENCY_PROMPT_TYPE.equals(promptRef.getType())) {
                        expectedValue = POUND_CURRENCY_LABEL + currencyValue.trim();
                    } else if (NowsDelegate.DATE_PROMPT_TYPE.equals(promptRef.getType())) {
                        expectedValue = localDate.format(DateTimeFormatter.ofPattern(NowsDelegate.OUTGOING_PROMPT_DATE_FORMAT));
                    } else {
                        expectedValue = stringValue;
                    }
                    Assert.assertEquals(expectedValue, p.getValue());
                }

        );


    }

    private void testGenerateNows(final NowDefinition nowDefinition, final String templateName) {

        final ResultsSharedEventHelper sharedEventHelper = h(
                resultsSharedTemplate());

        LocalDate localDate = LocalDate.now();
        uk.gov.justice.core.courts.Prompt datePrompt = uk.gov.justice.core.courts.Prompt.prompt()
                .withId(UUID.randomUUID())
                .withValue(localDate.format(DateTimeFormatter.ofPattern(NowsDelegate.INCOMING_PROMPT_DATE_FORMAT)))
                .build();

        final CommandHelpers.NowsHelper nows = h(basicNowsTemplate());

        doReturn(new HashSet<>(asList(nowDefinition))).when(referenceDataService).getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(sharedEventHelper.getFirstCompletedResultLine().getResultDefinitionId()));

        final UUID resultDefinitionId = sharedEventHelper.getFirstCompletedResultLine().getResultDefinitionId();

        final UUID promptId = sharedEventHelper.getFirstCompletedResultLineFirstPrompt().getId();

        sharedEventHelper.getFirstCompletedResultLine().getPrompts().add(datePrompt);

        // GPE-6752 bulk input resultDefinition, add prompt references
        final ResultDefinition resultDefinition = ResultDefinition.resultDefinition()
                .setId(resultDefinitionId)
                .setPrompts(asList(Prompt.prompt().setId(promptId),
                        Prompt.prompt().setId(datePrompt.getId()).setType(NowsDelegate.DATE_PROMPT_TYPE)));

        when(referenceDataService.getResultDefinitionById(any(), any(), eq(resultDefinitionId)))
                .thenReturn(resultDefinition);

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(sharedEventHelper.it()));

        final List<Now> nowsList = nows.it();
        final Now now0 = nowsList.get(0);
        List<UUID> newPromptRefs = new ArrayList<>(now0.getRequestedMaterials().get(0).getNowResults().get(0).getPromptRefs());
        newPromptRefs.add(datePrompt.getId());

        now0.getRequestedMaterials().get(0).getNowResults().get(0).setPromptRefs(newPromptRefs);

        final CreateNowsRequest nowsRequest = nowsDelegate.generateNows(event, nowsList, sharedEventHelper.it());

        assertThat(nowsRequest, isBean(CreateNowsRequest.class)
                .with(CreateNowsRequest::getHearing, isBean(Hearing.class))
                .with(CreateNowsRequest::getCourtClerk, isBean(DelegatedPowers.class)
                        .with(DelegatedPowers::getUserId, is(sharedEventHelper.getCourtClerk().getUserId()))
                        .with(DelegatedPowers::getLastName, is(sharedEventHelper.getCourtClerk().getLastName()))
                        .with(DelegatedPowers::getFirstName, is(sharedEventHelper.getCourtClerk().getFirstName()))
                )
                .withValue(req -> req.getNows().size(), 1)
                .with(CreateNowsRequest::getNows, first(isBean(Now.class)
                        .with(Now::getId, is(nows.getFirstNow().getId()))
                        .with(Now::getNowsTypeId, is(nows.getFirstNow().getNowsTypeId()))
                        .with(Now::getDefendantId, is(nows.getFirstNow().getDefendantId()))
                        .with(Now::getRequestedMaterials, first(isBean(NowVariant.class)
                                .with(NowVariant::getMaterialId, is(nows.getFirstMaterial().getMaterialId()))
                                .with(NowVariant::getIsAmended, is(nows.getFirstMaterial().getIsAmended()))
                                .with(NowVariant::getNowResults, first(isBean(NowVariantResult.class)
                                        .with(NowVariantResult::getSharedResultId, is(nows.getFirstNowsResult().getSharedResultId()))
                                        .with(NowVariantResult::getSequence, is(nows.getFirstNowsResult().getSequence()))
                                        .with(nvr -> nvr.getPromptRefs().get(0), is(nows.getFirstPrompt()))
                                ))
                                .with(NowVariant::getKey, isBean(NowVariantKey.class)
                                        .withValue(key -> key.getUsergroups().get(0), nows.getFirstUserGroup())
                                )
                        ))
                ))
                .with(CreateNowsRequest::getSharedResultLines, first(isBean(SharedResultLine.class)
                        .with(SharedResultLine::getId, is(sharedEventHelper.getFirstTarget().getResultLines().get(0).getResultLineId()))
                        .with(SharedResultLine::getId, is(sharedEventHelper.getFirstCompletedResultLine().getResultLineId()))
                        .with(SharedResultLine::getDefendantId, is(sharedEventHelper.getFirstTarget().getDefendantId()))
                        .with(SharedResultLine::getProsecutionCaseId, is(sharedEventHelper.getFirstCase().getId()))
                        .with(SharedResultLine::getOffenceId, is(sharedEventHelper.getFirstTarget().getOffenceId()))
                        .with(SharedResultLine::getLevel, is(sharedEventHelper.getFirstCompletedResultLine().getLevel().toString()))
                        .with(SharedResultLine::getLabel, is(sharedEventHelper.getFirstCompletedResultLine().getResultLabel()))
                        .with(SharedResultLine::getPrompts, first(isBean(ResultPrompt.class)
                                .with(ResultPrompt::getId, is(sharedEventHelper.getFirstCompletedResultLineFirstPrompt().getId()))
                                .with(ResultPrompt::getLabel, is(sharedEventHelper.getFirstCompletedResultLineFirstPrompt().getLabel()))
                                .with(ResultPrompt::getValue, is(sharedEventHelper.getFirstCompletedResultLineFirstPrompt().getValue()))
                        ))
                        .with(SharedResultLine::getPrompts, second(isBean(ResultPrompt.class)
                                .withValue(ResultPrompt::getId, datePrompt.getId())
                                .withValue(ResultPrompt::getValue, localDate.format(DateTimeFormatter.ofPattern(NowsDelegate.INCOMING_PROMPT_DATE_FORMAT)))
                        ))
                        .with(SharedResultLine::getLastSharedDateTime, is(sharedEventHelper.getFirstCompletedResultLineStatus().getLastSharedDateTime().toLocalDate().toString()))
                        .with(SharedResultLine::getOrderedDate, is(sharedEventHelper.getFirstCompletedResultLine().getOrderedDate()))
                ))
                .with(CreateNowsRequest::getNowTypes, first(isBean(NowType.class)
                        .with(NowType::getId, is(nowDefinition.getId()))
                        .with(NowType::getDescription, is(nowDefinition.getName()))
                        .with(NowType::getJurisdiction, is(nowDefinition.getJurisdiction()))
                        .withValue(NowType::getPriority, nowDefinition.getUrgentTimeLimitInMinutes().toString())
                        .withValue(NowType::getTemplateName, templateName)
                        .withValue(NowType::getRank, nowDefinition.getRank())
                        .withValue(NowType::getStaticText, nowDefinition.getText())
                        .withValue(NowType::getWelshDescription, nowDefinition.getWelshName())
                        //this are changing .with(NowType::getStaticText, is(exp))
                        //this is changing .with(NowType::getWelshStaticText, is(nowDefinition.getWelshText() + "\n" + nowDefinition.getResultDefinitions().get(0).getWelshText()))
                        //TODO GPE-6313 resolve these 2
                        //.with(NowTypes::getWelshDescription, is(nowDefinition.getWelshName()))
                        //.with(NowTypes::getBilingualTemplateName, is(nowDefinition.getBilingualTemplateName()))
                        .with(NowType::getRequiresBulkPrinting, is(nowDefinition.getRemotePrintingRequired()))
                ))
        );
    }

    private ResultDefinition stubResultDefinition(ResultsSharedEventHelper resultsShared) {
        UUID resultDefinitionId = resultsShared.getFirstCompletedResultLine().getResultDefinitionId();
        // GPE-6752 bulk input resultDefinition, add prompt references

        final NowDefinition nowDefinition = with(standardNowDefinition(), d -> {
            d.setText(null);
            d.getResultDefinitions().get(0).setText(null);
            d.setWelshText(null);
            d.getResultDefinitions().get(0).setWelshText(null);
        });

        final ResultDefinition resultDefinition = ResultDefinition.resultDefinition()
                .setPrompts(asList(
                        Prompt.prompt()
                                .setId(resultsShared.getFirstCompletedResultLineFirstPrompt().getId())

                ))
                .setId(resultDefinitionId);

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(resultsShared.getFirstCompletedResultLine().getResultDefinitionId())))
                .thenReturn(new HashSet<>(asList(nowDefinition)));
        when(referenceDataService.getResultDefinitionById(any(), any(), eq(resultDefinitionId)))
                .thenReturn(resultDefinition);
        return resultDefinition;

    }


    private void stubEnglishCourtHouseLookUp() {

        final Courtrooms englishCourtRoom = Courtrooms.courtrooms()
                .withCourtroomId(54321)
                .withCourtroomName("English Court Room")
                .withId(UUID.randomUUID())
                .build();

        final CourtCentreOrganisationUnit englishCourtCentreOrganisationunits = CourtCentreOrganisationUnit.courtCentreOrganisationUnit()
                .withOucodeL3Name("English Court Room")
                .withId(UUID.randomUUID().toString())
                .withCourtrooms(asList(englishCourtRoom))
                .withPostcode("AA1 1AA")
                .withAddress1("address1")
                .withAddress2("address2")
                .withAddress3("address3")
                .withAddress4("address4")
                .withAddress5("address5")
                .withIsWelsh(false)
                .build();

        doReturn(Optional.of(englishCourtCentreOrganisationunits)).when(courtHouseReverseLookup).getCourtCentreById(any(JsonEnvelope.class), any());
    }

    private void stubWelshCourtHouseLookUp() {

        final Courtrooms welshCourtRooom = Courtrooms.courtrooms()
                .withCourtroomId(12345)
                .withCourtroomName("Welsh Court Room")
                .withWelshCourtroomName("Welsh Court Room")
                .withId(UUID.randomUUID())
                .build();

        final CourtCentreOrganisationUnit welshCourtCentreOrganisationunits = CourtCentreOrganisationUnit.courtCentreOrganisationUnit()
                .withOucodeL3Name("Welsh Court Room")
                .withOucodeL3WelshName("Welsh Court Room")
                .withId(UUID.randomUUID().toString())
                .withCourtrooms(asList(welshCourtRooom))
                .withPostcode("AA1 1AA")
                .withAddress1("address1")
                .withAddress2("address2")
                .withAddress3("address3")
                .withAddress4("address4")
                .withAddress5("address5")
                .withWelshAddress1("welsh address1")
                .withWelshAddress2("welsh address2")
                .withWelshAddress3("welsh address3")
                .withWelshAddress4("welsh address4")
                .withWelshAddress4("welsh address5")
                .withIsWelsh(true)
                .build();

        doReturn(Optional.of(welshCourtCentreOrganisationunits)).when(courtHouseReverseLookup).getCourtCentreById(any(JsonEnvelope.class), any());
    }

}
