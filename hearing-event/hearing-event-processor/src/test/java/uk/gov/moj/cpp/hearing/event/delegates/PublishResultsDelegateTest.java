package uk.gov.moj.cpp.hearing.event.delegates;

import static java.util.Collections.singletonList;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.event.NowsTemplates.resultsSharedTemplate;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.NowDefinitionTemplates.standardNowDefinition;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.VariantDirectoryTemplates.standardVariantTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.print;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.second;

import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.CourtClerk;
import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.core.courts.DefendantAttendance;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.JudicialRole;
import uk.gov.justice.core.courts.Key;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.ResultPrompt;
import uk.gov.justice.core.courts.SharedHearing;
import uk.gov.justice.core.courts.SharedResultLine;
import uk.gov.justice.core.courts.SharedVariant;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.PublicHearingResulted;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.relist.RelistReferenceDataService;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class PublishResultsDelegateTest {

    @Spy
    private final Enveloper enveloper = createEnveloper();
    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
    @Spy
    @InjectMocks
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter();
    @Spy
    @InjectMocks
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();
    @Mock
    private ReferenceDataService referenceDataService;
    @Mock
    private RelistReferenceDataService relistReferenceDataService;
    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @InjectMocks
    private PublishResultsDelegate publishResultsDelegate;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shareResults() {

        final NowDefinition nowDefinition = standardNowDefinition();

        final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt promptReferenceData =
                uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt.prompt()
                        .setId(UUID.randomUUID())
                        .setLabel("promptReferenceData0")
                        .setUserGroups(Arrays.asList("usergroup0", "usergroup1"));

        final Prompt prompt0 = Prompt.prompt()
                .withLabel(promptReferenceData.getLabel())
                .withValue("promptValue0")
                .withId(promptReferenceData.getId())
                .withFixedListCode("fixedListCode0")
                .build();

        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(with(resultsSharedTemplate(), r -> {
            r.getVariantDirectory().get(0).getKey().setNowsTypeId(nowDefinition.getId());
            r.getHearing().getTargets().get(0).getResultLines().get(0).setPrompts(singletonList(prompt0));
            r.getHearing().setDefenceCounsels(
                    singletonList(DefenceCounsel.defenceCounsel().withId(UUID.randomUUID()).build()));
            r.getHearing().setDefendantAttendance(
                    singletonList(DefendantAttendance.defendantAttendance().withDefendantId(UUID.randomUUID()).build()));
        }));

        final List<uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant> newVariants = singletonList(
                standardVariantTemplate(nowDefinition.getId(), resultsShared.getHearingId(), resultsShared.getFirstDefendant().getId()).setReferenceDate(LocalDate.now())
        );

        final LocalDate referenceDate = newVariants.get(0).getReferenceDate();
        when(referenceDataService.getNowDefinitionById(
                null, referenceDate,
                nowDefinition.getId())).thenReturn(nowDefinition);

        final ResultLine resultLine = resultsShared.getFirstTarget().getResultLines().get(0);

        resultLine.setDelegatedPowers(
                DelegatedPowers.delegatedPowers().withUserId(UUID.randomUUID()).build()
        );

        final ResultDefinition resultLineDefinition = ResultDefinition.resultDefinition()
                .setPrompts(Arrays.asList(promptReferenceData))
                .setId(resultLine.getResultDefinitionId());

        when(referenceDataService.getResultDefinitionById(null, resultLine.getOrderedDate(), resultLineDefinition.getId())).thenReturn(resultLineDefinition);

        when(relistReferenceDataService.getWithdrawnResultDefinitionUuids(Mockito.any(JsonEnvelope.class), Mockito.any(LocalDate.class))).thenReturn(new ArrayList<>());

        //the actual test !!!
        publishResultsDelegate.shareResults(null, sender,
                envelopeFrom(metadataWithRandomUUID("hearing.results-shared"), objectToJsonObjectConverter.convert(resultsShared.it())),
                resultsShared.it(), newVariants);

        verify(sender).send(envelopeArgumentCaptor.capture());

        final JsonEnvelope sharedResultsMessage = envelopeArgumentCaptor.getValue();

        assertThat(sharedResultsMessage, jsonEnvelope(metadata().withName("public.hearing.resulted"), payloadIsJson(print())));

        final PublicHearingResulted publicHearingResulted = jsonObjectToObjectConverter.convert(sharedResultsMessage.payloadAsJsonObject(), PublicHearingResulted.class);

        final Hearing hearingIn = resultsShared.getHearing();

        final CourtClerk expectedCourtClerk0 = resultsShared.it().getCompletedResultLinesStatus().get(resultLine.getResultLineId()).getCourtClerk();

        assertThat(publicHearingResulted, isBean(PublicHearingResulted.class)
                .withValue(PublicHearingResulted::getSharedTime, resultsShared.it().getSharedTime())
                .with(PublicHearingResulted::getHearing, isBean(SharedHearing.class)
                        .withValue(SharedHearing::getId, hearingIn.getId())
                        .withValue(sh -> sh.getJurisdictionType().name(), hearingIn.getJurisdictionType().name())
                        .withValue(sh -> sh.getHearingDays().size(), hearingIn.getHearingDays().size())
                        .with(SharedHearing::getHearingDays, first(isBean(HearingDay.class)))
                        .with(SharedHearing::getCourtCentre, isBean(CourtCentre.class)
                                .withValue(CourtCentre::getId, hearingIn.getCourtCentre().getId())
                                .withValue(CourtCentre::getName, hearingIn.getCourtCentre().getName())
                                .withValue(CourtCentre::getRoomId, hearingIn.getCourtCentre().getRoomId())
                                .withValue(CourtCentre::getRoomName, hearingIn.getCourtCentre().getRoomName())
                        )
                        // no nested or detailed check because shareResults just copies the array references
                        .withValue(sh -> sh.getJudiciary().size(), hearingIn.getJudiciary().size())
                        .with(SharedHearing::getJudiciary, first(isBean(JudicialRole.class)
                                .withValue(JudicialRole::getJudicialId, hearingIn.getJudiciary().get(0).getJudicialId())
                        ))
                        // no nested or detailed check because shareResults just copies the array references
                        .withValue(sh -> sh.getDefenceCounsels().size(), hearingIn.getDefenceCounsels().size())
                        .with(SharedHearing::getDefenceCounsels, first(isBean(DefenceCounsel.class)
                                .withValue(DefenceCounsel::getId, hearingIn.getDefenceCounsels().get(0).getId())
                        ))
                        // no nested or detailed check because shareResults just copies the array references
                        .withValue(sh -> sh.getProsecutionCases().size(), hearingIn.getProsecutionCases().size())
                        .with(SharedHearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .withValue(ProsecutionCase::getId, hearingIn.getProsecutionCases().get(0).getId())
                        ))
                        .withValue(sh -> sh.getDefendantAttendance().size(), hearingIn.getDefendantAttendance().size())
                        .with(SharedHearing::getDefendantAttendance, first(isBean(DefendantAttendance.class)
                                .withValue(DefendantAttendance::getDefendantId, hearingIn.getDefendantAttendance().get(0).getDefendantId())
                        ))
                        .withValue(sh -> sh.getSharedResultLines().size(), 1)
                        .with(SharedHearing::getSharedResultLines, first(isBean(SharedResultLine.class)
                                .withValue(SharedResultLine::getId, resultLine.getResultLineId())
                                //.withValue(SharedResultLine::getCourtClerk, resultLine.getResultLineId())
                                .withValue(SharedResultLine::getDefendantId, resultsShared.getFirstDefendant().getId())
                                .withValue(SharedResultLine::getLabel, resultLine.getResultLabel())
                                .withValue(SharedResultLine::getLevel, resultLine.getLevel().name())
                                //TODO GPE-5483
                                //  .withValue(SharedResultLine::getIsAvailableForCourtExtract, resultLineDefinition.get)
                                //.withValue(SharedResultLine::getWelshLabel, resultLine.getW)
                                .withValue(SharedResultLine::getLastSharedDateTime, resultLine.getSharedDate().toString())
                                .with(SharedResultLine::getCourtClerk, isBean(DelegatedPowers.class)
                                        .withValue(DelegatedPowers::getFirstName, expectedCourtClerk0.getFirstName())
                                        .withValue(DelegatedPowers::getLastName, expectedCourtClerk0.getLastName())
                                        .withValue(DelegatedPowers::getUserId, expectedCourtClerk0.getId())
                                )
                                .withValue(rl -> rl.getDelegatedPowers().getUserId(), resultLine.getDelegatedPowers().getUserId())
                                .withValue(rl -> rl.getPrompts().size(), 1)
                                .with(SharedResultLine::getPrompts, first(isBean(ResultPrompt.class)
                                        .withValue(ResultPrompt::getFixedListCode, prompt0.getFixedListCode())
                                        .withValue(ResultPrompt::getId, prompt0.getId())
                                        .withValue(ResultPrompt::getLabel, prompt0.getLabel())
                                        .withValue(ResultPrompt::getValue, prompt0.getValue())
                                        .withValue(ResultPrompt::getFixedListCode, prompt0.getFixedListCode())
                                        //TODO GPE-6752
                                        //.withValue(SharedPrompt::getIsAvailableForCourtExtract, prompt0.get())
                                        //.withValue(SharedPrompt::getWelshLabel, prompt0.get())
                                        //.withValue(SharedPrompt::getWelshValue, prompt0.get())
                                        //.withValue(SharedPrompt::getIsAvailableForCourtExtract, prompt0.get())
                                        .withValue(ResultPrompt::getPromptSequence, promptReferenceData.getSequence())
                                        .withValue(ResultPrompt::getUsergroups, promptReferenceData.getUserGroups())
                                ))
                        ))
                )
                .withValue(phr -> phr.getVariants().size(), 2)
                .with(PublicHearingResulted::getVariants, first(isBean(SharedVariant.class)
                        .with(SharedVariant::getKey, isBean(Key.class)
                                .withValue(Key::getDefendantId, resultsShared.getFirstVariant().getKey().getDefendantId())
                                .withValue(Key::getNowsTypeId, resultsShared.getFirstVariant().getKey().getNowsTypeId())
                                .with(Key::getUsergroups, containsInAnyOrder(resultsShared.getFirstVariant().getKey().getUsergroups().toArray()))
                                .withValue(Key::getHearingId, resultsShared.getFirstVariant().getKey().getHearingId())
                        )
                        .withValue(SharedVariant::getMaterialId, resultsShared.getFirstVariant().getValue().getMaterialId())
                        .withValue(SharedVariant::getDescription, nowDefinition.getName())
                        .withValue(SharedVariant::getTemplateName, nowDefinition.getTemplateName())
                ))
                .with(PublicHearingResulted::getVariants, second(isBean(SharedVariant.class)
                        .with(SharedVariant::getKey, isBean(Key.class)
                                .withValue(Key::getDefendantId, newVariants.get(0).getKey().getDefendantId())
                                .withValue(Key::getNowsTypeId, newVariants.get(0).getKey().getNowsTypeId())
                                .with(Key::getUsergroups, containsInAnyOrder(newVariants.get(0).getKey().getUsergroups().toArray()))
                                .withValue(Key::getHearingId, newVariants.get(0).getKey().getHearingId())
                        )
                        .withValue(SharedVariant::getMaterialId, newVariants.get(0).getValue().getMaterialId())
                        .withValue(SharedVariant::getDescription, nowDefinition.getName())
                        .withValue(SharedVariant::getTemplateName, nowDefinition.getTemplateName())
                ))
        );

    }

}
