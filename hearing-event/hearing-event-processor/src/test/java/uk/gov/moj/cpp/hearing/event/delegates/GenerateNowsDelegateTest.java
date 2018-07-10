package uk.gov.moj.cpp.hearing.event.delegates;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Address;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Attendees;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Cases;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.CourtCentre;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Defendants;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.GenerateNowsCommand;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Hearing;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Interpreter;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Material;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.NowResult;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.NowTypes;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Nows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Offences;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Person;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.PromptRef;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Prompts;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.SharedResultLines;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.UserGroups;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;
import uk.gov.moj.cpp.hearing.nows.events.NowType;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.ResultsSharedEventHelper;

import java.util.List;

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
import static uk.gov.moj.cpp.hearing.test.TestTemplates.NowDefinitionTemplates.standardNowDefinition;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.print;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.second;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.third;
import static uk.gov.moj.cpp.hearing.test.matchers.MappedToBeanMatcher.convertTo;

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
    private GenerateNowsDelegate target;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGenerateNows() {

        final ResultsSharedEventHelper resultsShared = h(resultsSharedTemplate());

        final List<Nows> nows = basicNowsTemplate();

        final NowDefinition nowDefinition = standardNowDefinition();

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), eq(resultsShared.getFirstCompletedResultLine().getResultDefinitionId())))
                .thenReturn(nowDefinition);

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));

        target.generateNows(sender, event, nows, resultsShared.it());

        verify(sender).send(envelopeArgumentCaptor.capture());

        final JsonEnvelope createNowsMessage = envelopeArgumentCaptor.getValue();

        assertThat(createNowsMessage, jsonEnvelope(metadata().withName("hearing.command.generate-nows"), payloadIsJson(print())));

        assertThat(createNowsMessage, convertTo(GenerateNowsCommand.class, isBean(GenerateNowsCommand.class)
                .with(GenerateNowsCommand::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(resultsShared.getHearingId()))
                        .with(Hearing::getHearingDates, first(is(resultsShared.getFirstHearingDay())))
                        .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                .with(CourtCentre::getCourtCentreId, is(resultsShared.getHearing().getCourtCentreId()))
                                .with(CourtCentre::getCourtCentreName, is(resultsShared.getHearing().getCourtCentreName()))
                                .with(CourtCentre::getCourtRoomId, is(resultsShared.getHearing().getCourtRoomId()))
                                .with(CourtCentre::getCourtRoomName, is(resultsShared.getHearing().getCourtRoomName()))
                        )
                        .with(Hearing::getAttendees, first(isBean(Attendees.class)
                                .with(Attendees::getLastName, is(resultsShared.getFirstDefenseCounsel().getLastName()))
                                .with(Attendees::getFirstName, is(resultsShared.getFirstDefenseCounsel().getFirstName()))
                                .with(Attendees::getType, is("DefenseCounsel"))
                        ))
                        .with(Hearing::getAttendees, second(isBean(Attendees.class)
                                .with(Attendees::getLastName, is(resultsShared.getFirstProsecutionCounsel().getLastName()))
                                .with(Attendees::getFirstName, is(resultsShared.getFirstProsecutionCounsel().getFirstName()))
                                .with(Attendees::getType, is("ProsecutionCounsel"))
                        ))
                        .with(Hearing::getAttendees, third(isBean(Attendees.class)
                                .with(Attendees::getLastName, is(resultsShared.getCourtClerk().getLastName()))
                                .with(Attendees::getFirstName, is(resultsShared.getCourtClerk().getFirstName()))
                                .with(Attendees::getType, is("CourtClerk"))
                        ))
                        .with(Hearing::getDefendants, first(isBean(Defendants.class)
                                .with(Defendants::getId, is(resultsShared.getFirstDefendant().getId()))
                                .with(Defendants::getPerson, isBean(Person.class)
                                        .with(Person::getId, is(resultsShared.getFirstDefendant().getPersonId()))
                                        .with(Person::getFirstName, is(resultsShared.getFirstDefendant().getFirstName()))
                                        .with(Person::getLastName, is(resultsShared.getFirstDefendant().getLastName()))
                                        .with(Person::getDateOfBirth, is(resultsShared.getFirstDefendant().getDateOfBirth().toString()))
                                        .with(Person::getNationality, is(resultsShared.getFirstDefendant().getNationality()))
                                        .with(Person::getGender, is(resultsShared.getFirstDefendant().getGender()))
                                        .with(Person::getAddress, isBean(Address.class)
                                                .with(Address::getAddress1, is(resultsShared.getFirstDefendant().getAddress().getAddress1()))
                                                .with(Address::getAddress2, is(resultsShared.getFirstDefendant().getAddress().getAddress2()))
                                                .with(Address::getAddress3, is(resultsShared.getFirstDefendant().getAddress().getAddress3()))
                                                .with(Address::getAddress4, is(resultsShared.getFirstDefendant().getAddress().getAddress4()))
                                                .with(Address::getPostCode, is(resultsShared.getFirstDefendant().getAddress().getPostCode()))
                                        )
                                )
                                .with(Defendants::getInterpreter, isBean(Interpreter.class)
                                        .with(Interpreter::getLanguage, is(resultsShared.getFirstDefendant().getInterpreter().getLanguage()))
                                )
                                .with(Defendants::getCases, first(isBean(Cases.class)
                                        .with(Cases::getId, is(resultsShared.getFirstDefendantCase().getCaseId()))
                                        .with(Cases::getUrn, is(resultsShared.getFirstCase().getUrn()))
                                        .with(Cases::getBailStatus, is(resultsShared.getFirstDefendantCase().getBailStatus()))
                                        .with(Cases::getCustodyTimeLimitDate, is(resultsShared.getFirstDefendantCase().getCustodyTimeLimitDate()))
                                        .with(Cases::getOffences, first(isBean(Offences.class)
                                                .with(Offences::getId, is(resultsShared.getFirstDefendantFirstOffence().getId()))
                                                .with(Offences::getCode, is(resultsShared.getFirstDefendantFirstOffence().getOffenceCode()))
                                                .with(Offences::getStartDate, is(resultsShared.getFirstDefendantFirstOffence().getStartDate()))
                                        ))
                                ))
                        ))
                        .with(Hearing::getSharedResultLines, first(isBean(SharedResultLines.class)
                                .with(SharedResultLines::getId, is(resultsShared.getFirstCompletedResultLine().getId()))
                                .with(SharedResultLines::getDefendantId, is(resultsShared.getFirstCompletedResultLine().getDefendantId()))
                                .with(SharedResultLines::getCaseId, is(resultsShared.getFirstCompletedResultLine().getCaseId()))
                                .with(SharedResultLines::getOffenceId, is(resultsShared.getFirstCompletedResultLine().getOffenceId()))
                                .with(SharedResultLines::getLevel, is(resultsShared.getFirstCompletedResultLine().getLevel().toString()))
                                .with(SharedResultLines::getLabel, is(resultsShared.getFirstCompletedResultLine().getResultLabel()))
                                .with(SharedResultLines::getPrompts, first(isBean(Prompts.class)
                                        .with(Prompts::getId, is(resultsShared.getFirstCompletedResultLineFirstPrompt().getId()))
                                        .with(Prompts::getLabel, is(resultsShared.getFirstCompletedResultLineFirstPrompt().getLabel()))
                                        .with(Prompts::getValue, is(resultsShared.getFirstCompletedResultLineFirstPrompt().getValue()))
                                ))
                                .with(SharedResultLines::getSharedDate, is(resultsShared.getFirstCompletedResultLineStatus().getLastSharedDateTime()))
                                .with(SharedResultLines::getOrderedDate, is(resultsShared.getFirstCompletedResultLineStatus().getLastSharedDateTime()))//TODO - to change at some point
                        ))
                        .with(Hearing::getNows, first(isBean(Nows.class)
                                .with(Nows::getId, is(nows.get(0).getId()))
                                .with(Nows::getNowsTypeId, is(nows.get(0).getNowsTypeId()))
                                .with(Nows::getNowsTemplateName, is(nows.get(0).getNowsTemplateName()))
                                .with(Nows::getDefendantId, is(nows.get(0).getDefendantId()))
                                .with(Nows::getMaterials, first(isBean(Material.class)
                                        .with(Material::getId, is(nows.get(0).getMaterials().get(0).getId()))
                                        .with(Material::isAmended, is(nows.get(0).getMaterials().get(0).isAmended()))
                                        .with(Material::getNowResult, first(isBean(NowResult.class)
                                                .with(NowResult::getSharedResultId, is(nows.get(0).getMaterials().get(0).getNowResult().get(0).getSharedResultId()))
                                                .with(NowResult::getSequence, is(nows.get(0).getMaterials().get(0).getNowResult().get(0).getSequence()))
                                                .with(NowResult::getPrompts, first(isBean(PromptRef.class)
                                                        .with(PromptRef::getId, is(nows.get(0).getMaterials().get(0).getNowResult().get(0).getPrompts().get(0).getId()))
                                                        .with(PromptRef::getLabel, is(nows.get(0).getMaterials().get(0).getNowResult().get(0).getPrompts().get(0).getLabel()))
                                                ))
                                        ))
                                        .with(Material::getUserGroups, first(isBean(UserGroups.class)
                                                .with(UserGroups::getGroup, is(nows.get(0).getMaterials().get(0).getUserGroups().get(0).getGroup()))
                                        ))
                                ))
                        ))
                        .with(Hearing::getNowTypes, first(isBean(NowTypes.class)
                                .with(NowTypes::getId, is(nowDefinition.getId()))
                                .with(NowTypes::getDescription, is(nowDefinition.getName()))
                                .with(NowTypes::getJurisdiction, is(nowDefinition.getJurisdiction()))
                                .with(NowTypes::getPriority, is(nowDefinition.getUrgentTimeLimitInMinutes().toString()))
                                .with(NowTypes::getTemplateName, is(nowDefinition.getTemplateName()))
                                .with(NowTypes::getRank, is(nowDefinition.getRank()))
                                .with(NowTypes::getStaticText, is(nowDefinition.getNowText() + "\n" + nowDefinition.getResultDefinitions().get(0).getNowText()))
                        ))
                )
        ));
    }

    @Test
    public void testGenerateNows_withNullNowText() {
        final ResultsSharedEventHelper resultsShared = h(resultsSharedTemplate());

        final List<Nows> nows = basicNowsTemplate();

        final NowDefinition nowDefinition = with(standardNowDefinition(), d -> {
            d.setNowText(null);
        });

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), eq(resultsShared.getFirstCompletedResultLine().getResultDefinitionId())))
                .thenReturn(nowDefinition);

        target.generateNows(sender, envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared)), nows, resultsShared.it());

        verify(sender).send(envelopeArgumentCaptor.capture());

        assertThat( envelopeArgumentCaptor.getValue(), convertTo(GenerateNowsCommand.class, isBean(GenerateNowsCommand.class)
                .with(GenerateNowsCommand::getHearing, isBean(Hearing.class)
                        .with(Hearing::getNowTypes, first(isBean(NowTypes.class)
                                .with(NowTypes::getStaticText, is(nowDefinition.getResultDefinitions().get(0).getNowText()))
                        ))
                )
        ));
    }

    @Test
    public void testGenerateNows_withNullNowText_AndNullResultDefinitionNowText() {
        final ResultsSharedEventHelper resultsShared = h(resultsSharedTemplate());

        final List<Nows> nows = basicNowsTemplate();

        final NowDefinition nowDefinition = with(standardNowDefinition(), d -> {
            d.setNowText(null);
            d.getResultDefinitions().forEach(l -> l.setNowText(null));
        });

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), eq(resultsShared.getFirstCompletedResultLine().getResultDefinitionId())))
                .thenReturn(nowDefinition);

        target.generateNows(sender, envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared)), nows, resultsShared.it());

        verify(sender).send(envelopeArgumentCaptor.capture());

        assertThat( envelopeArgumentCaptor.getValue(), convertTo(GenerateNowsCommand.class, isBean(GenerateNowsCommand.class)
                .with(GenerateNowsCommand::getHearing, isBean(Hearing.class)
                        .with(Hearing::getNowTypes, first(isBean(NowTypes.class)
                                .with(NowTypes::getStaticText, is(""))
                        ))
                )
        ));
    }
}
