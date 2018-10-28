package uk.gov.moj.cpp.hearing.command.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.json.schemas.core.HearingLanguage;
import uk.gov.justice.json.schemas.core.HearingType;
import uk.gov.justice.json.schemas.core.JudicialRoleType;
import uk.gov.justice.json.schemas.core.JurisdictionType;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.external.domain.progression.relist.AdjournHearing;
import uk.gov.moj.cpp.external.domain.progression.relist.CourtCentre;
import uk.gov.moj.cpp.external.domain.progression.relist.Defendant;
import uk.gov.moj.cpp.external.domain.progression.relist.Hearing;
import uk.gov.moj.cpp.external.domain.progression.relist.JudicialRole;
import uk.gov.moj.cpp.external.domain.progression.relist.Offence;
import uk.gov.moj.cpp.external.domain.progression.relist.ProsecutionCase;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.HearingAdjourned;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.class)
public class AdjournHearingCommandHandlerTest {

    private static final String HEARING_ADJOURN_HEARING = "hearing.adjourn-hearing";

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            HearingAdjourned.class
    );

    @Mock
    private EventStream hearingEventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @InjectMocks
    private AdjournHearingCommandHandler adjournHearingCommandHandler;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void eventHearingAdjournedShouldCreated() throws Exception {

        CommandHelpers.InitiateHearingCommandHelper hearingObject = CommandHelpers.h(standardInitiateHearingTemplate());
        final UUID hearingId = hearingObject.getHearingId();

        AdjournHearing adjournHearing = createAdjournHearing(hearingId);
        Hearing hearing = adjournHearing.getNextHearings().get(0);
        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearingObject.getHearing()));
        }};

        when(this.eventSource.getStreamById(hearingObject.getHearingId())).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);

        final JsonEnvelope jsonEnvelope = envelopeFrom(metadataWithRandomUUID(HEARING_ADJOURN_HEARING), objectToJsonObjectConverter.convert(adjournHearing));

        adjournHearingCommandHandler.adjournHearing(jsonEnvelope);

        final List<JsonEnvelope> events = verifyAppendAndGetArgumentFrom(this.hearingEventStream).collect(Collectors.toList());

        assertThat(asPojo(events.get(0), HearingAdjourned.class), isBean(HearingAdjourned.class)
                .with(HearingAdjourned::getAdjournedHearing, Matchers.is(hearingId))
                .with(HearingAdjourned::getNextHearings, first(isBean(Hearing.class)
                        .with(Hearing::getType, isBean(HearingType.class)
                                .with(HearingType::getId, is(hearing.getType().getId()))
                                .with(HearingType::getDescription, is(hearing.getType().getDescription())))
                        .with(Hearing::getJurisdictionType, is(hearing.getJurisdictionType()))
                        .with(Hearing::getReportingRestrictionReason, is(hearing.getReportingRestrictionReason()))
                        .with(Hearing::getHearingLanguage, is(hearing.getHearingLanguage()))
                        .with(Hearing::getEstimatedMinutes, is(hearing.getEstimatedMinutes()))
                        .with(Hearing::getCourtCentre, isBean(CourtCentre.class))
                        .with(Hearing::getJudiciary, first(isBean(JudicialRole.class)
                                .with(JudicialRole::getJudicialId, is(hearing.getJudiciary().get(0).getJudicialId()))
                                .with(JudicialRole::getJudicialRoleType, is(hearing.getJudiciary().get(0).getJudicialRoleType()))))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getId, is(hearing.getProsecutionCases().get(0).getId()))
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getId, is(hearing.getProsecutionCases().get(0).getDefendants().get(0).getId()))
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getId())))
                                        )
                                ))
                        ))
                ))
        );
    }

    private AdjournHearing createAdjournHearing(UUID hearingId) {
        return AdjournHearing.adjournHearing()
                .setAdjournedHearing(hearingId)
                .setNextHearings(createNextHearings());
    }

    private List<Hearing> createNextHearings() {
        return Arrays.asList(Hearing.hearing()
                .setHearingLanguage(HearingLanguage.ENGLISH)
                .setCourtCentre(CourtCentre.courtCentre()
                        .setId(UUID.randomUUID())
                        .setRoomId(UUID.randomUUID()))
                .setJudiciary(Arrays.asList(JudicialRole.judicialRole()
                        .setJudicialId(UUID.randomUUID())
                        .setJudicialRoleType(JudicialRoleType.CIRCUIT_JUDGE)))
                .setJurisdictionType(JurisdictionType.CROWN)
                .setReportingRestrictionReason(STRING.next())
                .setType(HearingType.hearingType()
                        .withId(UUID.randomUUID())
                        .withDescription("SENTENCING")
                        .build())
                .setEstimatedMinutes(100)
                .setProsecutionCases(Arrays.asList(ProsecutionCase.prosecutionCase()
                        .setId(UUID.randomUUID())
                        .setDefendants(Arrays.asList(Defendant.defendant()
                                .setId(UUID.randomUUID())
                                .setOffences(Arrays.asList(Offence.offence()
                                        .setId(UUID.randomUUID()))))))));
    }
}