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

import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.JudicialRole;
import uk.gov.justice.core.courts.JudicialRoleType;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.NextHearing;
import uk.gov.justice.core.courts.NextHearingDefendant;
import uk.gov.justice.core.courts.NextHearingOffence;
import uk.gov.justice.core.courts.NextHearingProsecutionCase;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.external.domain.progression.relist.AdjournHearing;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.HearingAdjourned;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

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
        NextHearing hearing = adjournHearing.getNextHearings().get(0);
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
                .with(HearingAdjourned::getNextHearings, first(isBean(NextHearing.class)
                        .with(NextHearing::getType, isBean(HearingType.class)
                                .with(HearingType::getId, is(hearing.getType().getId()))
                                .with(HearingType::getDescription, is(hearing.getType().getDescription())))
                        .with(NextHearing::getJurisdictionType, is(hearing.getJurisdictionType()))
                        .with(NextHearing::getReportingRestrictionReason, is(hearing.getReportingRestrictionReason()))
                        .with(NextHearing::getHearingLanguage, is(hearingObject.getHearing().getHearingLanguage()))
                        .with(NextHearing::getEstimatedMinutes, is(hearing.getEstimatedMinutes()))
                        .with(NextHearing::getCourtCentre, isBean(CourtCentre.class))
                        .with(NextHearing::getJudiciary, first(isBean(JudicialRole.class)
                                .with(JudicialRole::getJudicialId, is(hearing.getJudiciary().get(0).getJudicialId()))
                                .with(JudicialRole::getJudicialRoleType, is(hearing.getJudiciary().get(0).getJudicialRoleType()))))
                        .with(NextHearing::getNextHearingProsecutionCases, first(isBean(NextHearingProsecutionCase.class)
                                .with(NextHearingProsecutionCase::getId, is(hearing.getNextHearingProsecutionCases().get(0).getId()))
                                .with(NextHearingProsecutionCase::getDefendants, first(isBean(NextHearingDefendant.class)
                                        .with(NextHearingDefendant::getId, is(hearing.getNextHearingProsecutionCases().get(0).getDefendants().get(0).getId()))
                                        .with(NextHearingDefendant::getOffences, first(isBean(NextHearingOffence.class)
                                                .with(NextHearingOffence::getId, is(hearing.getNextHearingProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getId())))
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

    private List<NextHearing> createNextHearings() {
        return Arrays.asList(NextHearing.nextHearing()
                .withHearingLanguage(HearingLanguage.ENGLISH)
                .withCourtCentre(CourtCentre.courtCentre()
                        .withId(UUID.randomUUID())
                        .withRoomId(UUID.randomUUID())
                        .build()
                )
                .withJudiciary(Arrays.asList(JudicialRole.judicialRole()
                                .withJudicialId(UUID.randomUUID())
                                .withJudicialRoleType(CoreTestTemplates.circuitJudge())
                                .build()
                        )
                )
                .withJurisdictionType(JurisdictionType.CROWN)
                .withReportingRestrictionReason(STRING.next())
                .withType(HearingType.hearingType()
                        .withId(UUID.randomUUID())
                        .withDescription("SENTENCING")
                        .build())
                .withEstimatedMinutes(100)
                .withNextHearingProsecutionCases(Arrays.asList(
                        NextHearingProsecutionCase.nextHearingProsecutionCase()
                                .withId(UUID.randomUUID())
                                .withDefendants(Arrays.asList(
                                        NextHearingDefendant.nextHearingDefendant()
                                                .withId(UUID.randomUUID())
                                                .withOffences(Arrays.asList(
                                                        NextHearingOffence.nextHearingOffence()
                                                                .withId(UUID.randomUUID())
                                                                .build()
                                                ))
                                                .build()

                                ))


                                .build()
                ))

                .build());
    }
}