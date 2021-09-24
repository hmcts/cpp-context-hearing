package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.JudicialRole;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.MetadataBuilder;
import uk.gov.moj.cpp.hearing.command.hearing.details.HearingAmendCommand;
import uk.gov.moj.cpp.hearing.command.hearing.details.HearingDetailsUpdateCommand;
import uk.gov.moj.cpp.hearing.domain.HearingState;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.HearingChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingDetailChanged;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;

import java.time.ZoneId;
import java.time.ZonedDateTime;
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
public class HearingDetailChangeCommandHandlerTest {

    private static final String PRIVATE_HEARING_COMMAND_HEARING_DETAIL_CHANGE = "hearing.change-hearing-detail";

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            HearingDetailChanged.class,
            HearingChangeIgnored.class
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
    private HearingDetailChangeCommandHandler handler;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void eventHearingDetailChangedShouldBeCreated() throws Exception {

        CommandHelpers.InitiateHearingCommandHelper hearing = CommandHelpers.h(standardInitiateHearingTemplate());
        final UUID hearingId = hearing.getHearingId();

        CourtCentre courtCentre = createCourtCentre();
        HearingType hearingType = createHearingType();
        List<JudicialRole> judicialRoles = createJudicialRoles();
        List<HearingDay> hearingDays = createHearingDays();

        uk.gov.moj.cpp.hearing.command.hearing.details.Hearing hearingDetail = createHearing(hearingId, courtCentre, hearingType, judicialRoles, hearingDays);

        HearingDetailsUpdateCommand hearingDetailsUpdateCommand = HearingDetailsUpdateCommand.hearingDetailsUpdateCommand()
                .setHearing(hearingDetail);

        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearing.getHearing()));
        }};

        when(eventSource.getStreamById(hearing.getHearingId())).thenReturn(hearingEventStream);
        when(aggregateService.get(hearingEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);

        final JsonEnvelope jsonEnvelope = envelopeFrom(metadataWithRandomUUID(PRIVATE_HEARING_COMMAND_HEARING_DETAIL_CHANGE), objectToJsonObjectConverter.convert(hearingDetailsUpdateCommand));

        handler.changeHearingDetail(jsonEnvelope);

        verify(eventSource).getStreamById(hearing.getHearingId());
        verify(aggregateService).get(hearingEventStream, HearingAggregate.class);

        final List<JsonEnvelope> events = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList());

        assertThat(asPojo(events.get(0), HearingDetailChanged.class), isBean(HearingDetailChanged.class)
                .with(HearingDetailChanged::getId, Matchers.is(hearingId))
                .with(HearingDetailChanged::getCourtCentre, isBean(CourtCentre.class)
                        .with(CourtCentre::getId, Matchers.is(courtCentre.getId()))
                        .with(CourtCentre::getName, Matchers.is(courtCentre.getName()))
                        .with(CourtCentre::getRoomId, Matchers.is(courtCentre.getRoomId()))
                        .with(CourtCentre::getRoomName, Matchers.is(courtCentre.getRoomName()))
                        .with(CourtCentre::getWelshName, Matchers.is(courtCentre.getWelshName()))
                        .with(CourtCentre::getWelshRoomName, Matchers.is(courtCentre.getWelshRoomName())))
                .with(HearingDetailChanged::getType, isBean(HearingType.class)
                        .with(HearingType::getId, Matchers.is(hearingType.getId()))
                        .with(HearingType::getDescription, Matchers.is(hearingType.getDescription())))
                .with(HearingDetailChanged::getHearingDays, first(isBean(HearingDay.class)
                        .with(HearingDay::getSittingDay, is(hearingDays.get(0).getSittingDay().withZoneSameLocal(ZoneId.of("UTC"))))
                        .with(HearingDay::getListedDurationMinutes, is(hearingDays.get(0).getListedDurationMinutes()))
                        .with(HearingDay::getListingSequence, is(hearingDays.get(0).getListingSequence()))))
                .with(HearingDetailChanged::getJudiciary, first(isBean(JudicialRole.class)
                        .with(JudicialRole::getJudicialId, is(judicialRoles.get(0).getJudicialId()))
                        .with(JudicialRole::getTitle, is(judicialRoles.get(0).getTitle()))
                        .with(JudicialRole::getFirstName, is(judicialRoles.get(0).getFirstName()))
                        .with(JudicialRole::getMiddleName, is(judicialRoles.get(0).getMiddleName()))
                        .with(JudicialRole::getLastName, is(judicialRoles.get(0).getLastName()))
                        .with(JudicialRole::getIsDeputy, is(judicialRoles.get(0).getIsDeputy()))
                        .with(JudicialRole::getIsBenchChairman, is(judicialRoles.get(0).getIsBenchChairman()))))
                .with(HearingDetailChanged::getHearingLanguage, is(hearingDetail.getHearingLanguage()))
                .with(HearingDetailChanged::getJurisdictionType, is(hearingDetail.getJurisdictionType()))
                .with(HearingDetailChanged::getReportingRestrictionReason, is(hearingDetail.getReportingRestrictionReason())));
    }

    @Test
    public void eventHearingDetailChangedShouldBeIgnored() throws Exception {

        CommandHelpers.InitiateHearingCommandHelper hearing = CommandHelpers.h(standardInitiateHearingTemplate());
        final UUID hearingId = hearing.getHearingId();

        CourtCentre courtCentre = createCourtCentre();
        HearingType hearingType = createHearingType();
        List<JudicialRole> judicialRoles = createJudicialRoles();
        List<HearingDay> hearingDays = createHearingDays();

        uk.gov.moj.cpp.hearing.command.hearing.details.Hearing hearingDetail = createHearing(hearingId, courtCentre, hearingType, judicialRoles, hearingDays);

        HearingDetailsUpdateCommand hearingDetailsUpdateCommand = HearingDetailsUpdateCommand.hearingDetailsUpdateCommand()
                .setHearing(hearingDetail);

        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            apply(new HearingInitiated(null));
        }};

        when(eventSource.getStreamById(hearing.getHearingId())).thenReturn(hearingEventStream);
        when(aggregateService.get(hearingEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);

        final JsonEnvelope jsonEnvelope = envelopeFrom(metadataWithRandomUUID(PRIVATE_HEARING_COMMAND_HEARING_DETAIL_CHANGE), objectToJsonObjectConverter.convert(hearingDetailsUpdateCommand));

        handler.changeHearingDetail(jsonEnvelope);

        verify(eventSource).getStreamById(hearing.getHearingId());
        verify(aggregateService).get(hearingEventStream, HearingAggregate.class);

        final List<JsonEnvelope> events = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList());

        assertThat(asPojo(events.get(0), HearingChangeIgnored.class), isBean(HearingChangeIgnored.class)
                .with(HearingChangeIgnored::getHearingId, Matchers.is(hearingId)));
    }

    @Test
    public void eventHearingDetailChangedShouldBeRaisedWhenNoProsecutionCases() throws Exception {

        CommandHelpers.InitiateHearingCommandHelper hearing = CommandHelpers.h(standardInitiateHearingTemplate());
        final UUID hearingId = hearing.getHearingId();

        CourtCentre courtCentre = createCourtCentre();
        HearingType hearingType = createHearingType();
        List<JudicialRole> judicialRoles = createJudicialRoles();
        List<HearingDay> hearingDays = createHearingDays();

        uk.gov.moj.cpp.hearing.command.hearing.details.Hearing hearingDetail = createHearing(hearingId, courtCentre, hearingType, judicialRoles, hearingDays);

        HearingDetailsUpdateCommand hearingDetailsUpdateCommand = HearingDetailsUpdateCommand.hearingDetailsUpdateCommand()
                .setHearing(hearingDetail);

        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearing.getHearing().setProsecutionCases(null)));
        }};

        when(eventSource.getStreamById(hearing.getHearingId())).thenReturn(hearingEventStream);
        when(aggregateService.get(hearingEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);

        final JsonEnvelope jsonEnvelope = envelopeFrom(metadataWithRandomUUID(PRIVATE_HEARING_COMMAND_HEARING_DETAIL_CHANGE), objectToJsonObjectConverter.convert(hearingDetailsUpdateCommand));

        handler.changeHearingDetail(jsonEnvelope);

        verify(eventSource).getStreamById(hearing.getHearingId());
        verify(aggregateService).get(hearingEventStream, HearingAggregate.class);

        final List<JsonEnvelope> events = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList());

        assertThat(asPojo(events.get(0), HearingDetailChanged.class), isBean(HearingDetailChanged.class)
                .with(HearingDetailChanged::getId, Matchers.is(hearingId)));
    }
    @Test
    public void eventHearingAmendedShouldBeRaised() throws Exception {

        CommandHelpers.InitiateHearingCommandHelper hearing = CommandHelpers.h(standardInitiateHearingTemplate());

        HearingAmendCommand hearingAmendCommand = (new HearingAmendCommand(hearing.getHearingId(), HearingState.SHARED_AMEND_LOCKED_ADMIN_ERROR));

        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            apply(new HearingAmendCommand(hearing.getHearing().getId(), HearingState.SHARED_AMEND_LOCKED_ADMIN_ERROR));
        }};

        when(eventSource.getStreamById(hearing.getHearingId())).thenReturn(hearingEventStream);
        when(aggregateService.get(hearingEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);


        final MetadataBuilder metadataBuilder = metadataWithRandomUUID("hearing.command.amend").withUserId(UUID.randomUUID().toString());
        final JsonEnvelope jsonEnvelope = envelopeFrom(metadataBuilder, objectToJsonObjectConverter.convert(hearingAmendCommand));

        handler.amendHearing(jsonEnvelope);

        verify(eventSource).getStreamById(hearing.getHearingId());
        verify(aggregateService).get(hearingEventStream, HearingAggregate.class);

    }

    private List<HearingDay> createHearingDays() {
        return Arrays.asList(HearingDay.hearingDay()
                .withListingSequence(10)
                .withListingSequence(20)
                .withSittingDay(ZonedDateTime.now())
                .build());
    }

    private List<JudicialRole> createJudicialRoles() {
        return Arrays.asList(JudicialRole.judicialRole()
                .withJudicialId(randomUUID())
                .withTitle(STRING.next())
                .withMiddleName(STRING.next())
                .withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withJudicialRoleType(CoreTestTemplates.circuitJudge())
                .withIsDeputy(true)
                .withIsBenchChairman(false)
                .build());
    }

    private HearingType createHearingType() {
        return HearingType.hearingType()
                .withId(randomUUID())
                .withDescription("Trial")
                .build();
    }

    private CourtCentre createCourtCentre() {
        return CourtCentre.courtCentre()
                .withId(randomUUID())
                .withName(STRING.next())
                .withRoomId(randomUUID())
                .withRoomName(STRING.next())
                .withWelshName(STRING.next())
                .withWelshRoomName(STRING.next())
                .build();
    }

    private uk.gov.moj.cpp.hearing.command.hearing.details.Hearing createHearing(UUID hearingId, CourtCentre courtCentre, HearingType hearingType, List<JudicialRole> judicialRoles, List<HearingDay> hearingDays) {
        uk.gov.moj.cpp.hearing.command.hearing.details.Hearing hearing = new uk.gov.moj.cpp.hearing.command.hearing.details.Hearing();
        hearing.setId(hearingId);
        hearing.setType(hearingType);
        hearing.setCourtCentre(courtCentre);
        hearing.setJudiciary(judicialRoles);
        hearing.setHearingDays(hearingDays);
        hearing.setReportingRestrictionReason(STRING.next());
        hearing.setJurisdictionType(JurisdictionType.CROWN);
        hearing.setHearingLanguage(HearingLanguage.ENGLISH);
        hearing.setIsVacated(false);
        hearing.setVacatedTrialReasonId(null);
        return hearing;
    }
}