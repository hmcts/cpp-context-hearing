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
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.delegates.GenerateNowsDelegate;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Address;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Attendees;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Cases;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Defendants;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.GenerateNowsCommand;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Hearing;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Interpreter;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Material;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.NowResult;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Nows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Person;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.PromptRef;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.SharedResultLines;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.ResultsSharedEventHelper;

import java.util.List;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.event.NowsTemplates.basicNowsTemplate;
import static uk.gov.moj.cpp.hearing.event.NowsTemplates.resultsSharedTemplate;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.print;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.second;
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

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));

        target.generateNows(sender, event, nows, resultsShared.it());

        verify(sender).send(envelopeArgumentCaptor.capture());

        final List<JsonEnvelope> outgoingMessages = envelopeArgumentCaptor.getAllValues();

        final JsonEnvelope createNowsMessage = outgoingMessages.get(0);

        assertThat(createNowsMessage, convertTo(GenerateNowsCommand.class, isBean(GenerateNowsCommand.class)
                .with(GenerateNowsCommand::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(resultsShared.getHearingId()))
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
                                                .with(Address::getPostCode, is(resultsShared.getFirstDefendant().getAddress().getPostCode()))
                                        )
                                )
                                .with(Defendants::getInterpreter, isBean(Interpreter.class)
                                        //TODO - other properties on interpreter?
                                        .with(Interpreter::getLanguage, is(resultsShared.getFirstDefendant().getInterpreter().getLanguage()))
                                )
                                .with(Defendants::getCases, first(isBean(Cases.class)
                                        .with(Cases::getId, is(resultsShared.getFirstDefendantCase().getCaseId()))
                                ))

                        ))
                        .with(Hearing::getSharedResultLines, first(isBean(SharedResultLines.class)
                                .with(SharedResultLines::getId, is(resultsShared.getFirstCompletedResultLine().getId()))
                                .with(SharedResultLines::getDefendantId, is(resultsShared.getFirstCompletedResultLine().getDefendantId()))
                                .with(SharedResultLines::getCaseId, is(resultsShared.getFirstCompletedResultLine().getCaseId()))
                                .with(SharedResultLines::getOffenceId, is(resultsShared.getFirstCompletedResultLine().getOffenceId()))
                                .with(SharedResultLines::getLevel, is(resultsShared.getFirstCompletedResultLine().getLevel().toString()))
                                .with(SharedResultLines::getLabel, is(resultsShared.getFirstCompletedResultLine().getResultLabel()))
                                //TODO - prompts?
                        ))
                        .with(Hearing::getNows, first(isBean(Nows.class)
                                .with(Nows::getId, is(nows.get(0).getId()))
                                .with(Nows::getNowsTypeId, is(nows.get(0).getNowsTypeId()))
                                .with(Nows::getDefendantId, is(nows.get(0).getDefendantId()))
                                .with(Nows::getMaterials, first(isBean(Material.class)
                                        .with(Material::getId, is(nows.get(0).getMaterials().get(0).getId()))
                                        .with(Material::isAmended, is(nows.get(0).getMaterials().get(0).isAmended()))
                                        .with(Material::getNowResult, first(isBean(NowResult.class)
                                                .with(NowResult::getSharedResultId, is(nows.get(0).getMaterials().get(0).getNowResult().get(0).getSharedResultId()))
                                                .with(NowResult::getSequence, is(nows.get(0).getMaterials().get(0).getNowResult().get(0).getSequence()))
                                                .with(NowResult::getPrompts, first(isBean(PromptRef.class)
                                                        .with(PromptRef::getLabel, is(nows.get(0).getMaterials().get(0).getNowResult().get(0).getPrompts().get(0).getLabel()))
                                                        //TODO - prompt id
                                                ))
                                        ))
                                ))
                                //TODO - templateName
                        ))
                )
        ));

        //TODO - rewrite this - expand on the assertions
        assertThat(createNowsMessage, jsonEnvelope(
                metadata().withName("hearing.command.generate-nows"),
                payloadIsJson(print())));
    }
}

/*
{
    "hearing":{

        "nows":[
            {
                "id":"573fc42e-50f0-4ad2-86a0-ac017bc1afe1",
                "nowsTypeId":"57c2bf65-745c-465f-aaf0-f4c388c960bc",
                "defendantId":"5bb4a8ff-078e-43ff-b931-507cc7aa876b",
                "materials":[
                    {
                        "id":"83ad6620-d93d-464f-afed-2f121cbd5e07",
                        "nowResult":[
                            {
                                "sharedResultId":"99748042-b33e-4c1a-b32a-9a2bb8e428cd",
                                "sequence":123,
                                "prompts":[
                                    {
                                        "label":"label1"
                                    }
                                ]
                            }
                        ],
                        "amended":false
                    }
                ]
            }
        ],
        "nowTypes":[
        ]
    },
    "_metadata":{
        "id":"86541566-b020-420c-bf14-f9466d06242b",
        "name":"hearing.command.generate-nows",
        "causation":[
            "bd73d01a-05e9-4fec-9a42-659cac67e080"
        ],
        "createdAt":"2018-07-02T11:00:35.900Z"
    }
}
 */