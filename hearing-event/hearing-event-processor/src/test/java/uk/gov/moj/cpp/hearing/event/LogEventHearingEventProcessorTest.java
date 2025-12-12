package uk.gov.moj.cpp.hearing.event;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.enveloper.EnvelopeFactory.createEnvelope;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.print;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.utils.TestUtils.convertZonedDate;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Gender;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.Organisation;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.command.updateEvent.HearingEvent;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventsUpdated;
import uk.gov.moj.cpp.hearing.eventlog.Case;
import uk.gov.moj.cpp.hearing.eventlog.PublicHearingEventLogged;
import uk.gov.moj.cpp.hearing.json.schema.event.CourtRoom;
import uk.gov.moj.cpp.hearing.json.schema.event.Hearing;
import uk.gov.moj.cpp.hearing.json.schema.event.LiveStatusPublished;
import uk.gov.moj.cpp.hearing.json.schema.event.Session;
import uk.gov.moj.cpp.hearing.json.schema.event.Sitting;
import uk.gov.moj.cpp.hearing.pi.PIMapper;
import uk.gov.moj.cpp.hearing.pi.ProsecutionCaseRetriever;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.ProsecutionCaseResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

@SuppressWarnings({"unchecked", "unused"})
public class LogEventHearingEventProcessorTest {

    @Spy
    private final Enveloper enveloper = createEnveloper();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    @Spy
    private JsonObjectToObjectConverter jsonObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter = new JsonObjectConvertersFactory().objectToJsonObjectConverter();

    @Spy
    private ObjectToJsonValueConverter objectToJsonValueConverter = new JsonObjectConvertersFactory().objectToJsonValueConverter();

    @InjectMocks
    private LogEventHearingEventProcessor logEventHearingEventProcessor;
    @Mock
    private Sender sender;
    @Mock
    private Requester requester;
    @Mock
    private JsonEnvelope responseEnvelope;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @Mock
    private PIMapper piMapper;

    @Mock
    private ProsecutionCaseRetriever prosecutionCaseRetriever;

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    private LocalDate date(String strDate) {
        return LocalDate.parse(strDate, dateTimeFormatter);
    }

    @Test
    public void shouldPublishHearingEventLoggedPublicEvent() {
        final UUID courtCentreId = randomUUID();
        final HearingEventLogged hearingEventLogged = new HearingEventLogged(randomUUID(), null, randomUUID(), randomUUID(),
                randomUUID(), STRING.next(), convertZonedDate(PAST_ZONED_DATE_TIME.next()), convertZonedDate(PAST_ZONED_DATE_TIME.next()), BOOLEAN.next(),
                new uk.gov.moj.cpp.hearing.domain.CourtCentre(courtCentreId, STRING.next(), randomUUID(), STRING.next(), STRING.next(), STRING.next()),
                new uk.gov.moj.cpp.hearing.domain.HearingType(STRING.next(), randomUUID()), STRING.next(), JurisdictionType.CROWN, STRING.next(), randomUUID());

        Optional<ProsecutionCaseResponse> prosecutionCaseResponse = getProsecutionCaseResponse();

        when(prosecutionCaseRetriever.getProsecutionCaseForHearing(any(), any())).thenReturn(prosecutionCaseResponse);

        LiveStatusPublished liveCaseStatusUpdate = LiveStatusPublished.liveStatusPublished()
                .withVenueId("C22WC00")
                .withListType("CROWN_LCSU")
                .withCourtId(courtCentreId.toString())
                .withCourtCentreName(hearingEventLogged.getCourtCentre().getName())
                .withCourtRooms(buildCourtRooms(hearingEventLogged))
                .build();

        when(piMapper.transformFrom(any(), any())).thenReturn(liveCaseStatusUpdate);

        this.logEventHearingEventProcessor.publishHearingEventLoggedPublicEvent(
                createEnvelope("hearing.hearing-event-logged", this.objectToJsonObjectConverter.convert(hearingEventLogged)));

        verify(this.sender, times(2)).send(this.envelopeArgumentCaptor.capture());

        final JsonEnvelope commandEventEnvelope = envelopeArgumentCaptor.getAllValues().get(0);
        assertThat(commandEventEnvelope, notNullValue());
        assertThat(commandEventEnvelope.metadata().name(), is("public.hearing.event-logged"));

        assertThat(commandEventEnvelope, jsonEnvelope(metadata().withName("public.hearing.event-logged"), payloadIsJson(print())));
        assertThat(uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo(commandEventEnvelope, PublicHearingEventLogged.class), isBean(PublicHearingEventLogged.class)
                .with(PublicHearingEventLogged::getCase, isBean(Case.class)
                        .with(Case::getCaseUrn, Matchers.is(hearingEventLogged.getCaseURN())))
                .with(PublicHearingEventLogged::getHearingEventDefinition, isBean(uk.gov.moj.cpp.hearing.eventlog.HearingEventDefinition.class)
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEventDefinition::getHearingEventDefinitionId, Matchers.is(hearingEventLogged.getHearingEventDefinitionId()))
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEventDefinition::isPriority, Matchers.is(!hearingEventLogged.isAlterable())))
                .with(PublicHearingEventLogged::getHearingEvent, isBean(uk.gov.moj.cpp.hearing.eventlog.HearingEvent.class)
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEvent::getHearingEventId, Matchers.is(hearingEventLogged.getHearingEventId()))
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEvent::getRecordedLabel, Matchers.is(hearingEventLogged.getRecordedLabel()))
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEvent::getEventTime, Matchers.is(hearingEventLogged.getEventTime().toLocalDateTime().atZone(ZoneId.of("UTC"))))
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEvent::getLastModifiedTime, Matchers.is(hearingEventLogged.getLastModifiedTime().toLocalDateTime().atZone(ZoneId.of("UTC")))))
                .with(PublicHearingEventLogged::getHearing, isBean(uk.gov.moj.cpp.hearing.eventlog.Hearing.class)
                        .with(uk.gov.moj.cpp.hearing.eventlog.Hearing::getCourtCentre, isBean(uk.gov.moj.cpp.hearing.eventlog.CourtCentre.class)
                                .with(uk.gov.moj.cpp.hearing.eventlog.CourtCentre::getCourtCentreId, Matchers.is(hearingEventLogged.getCourtCentre().getId()))
                                .with(uk.gov.moj.cpp.hearing.eventlog.CourtCentre::getCourtCentreName, Matchers.is(hearingEventLogged.getCourtCentre().getName()))
                                .with(uk.gov.moj.cpp.hearing.eventlog.CourtCentre::getCourtRoomId, Matchers.is(hearingEventLogged.getCourtCentre().getRoomId()))
                                .with(uk.gov.moj.cpp.hearing.eventlog.CourtCentre::getCourtRoomName, Matchers.is(hearingEventLogged.getCourtCentre().getRoomName())))
                        .with(uk.gov.moj.cpp.hearing.eventlog.Hearing::getHearingType, Matchers.is(hearingEventLogged.getHearingType().getDescription()))));


        final JsonEnvelope publishEventEnvelope = envelopeArgumentCaptor.getAllValues().get(1);
        assertThat(publishEventEnvelope, notNullValue());
        assertThat(publishEventEnvelope.metadata().name(), is("public.hearing.live-status-published"));
        assertThat(publishEventEnvelope, jsonEnvelope(metadata().withName("public.hearing.live-status-published"), payloadIsJson(print())));
        assertThat(uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo(publishEventEnvelope, LiveStatusPublished.class), isBean(LiveStatusPublished.class)
                .with(LiveStatusPublished::getCourtCentreName, is(liveCaseStatusUpdate.getCourtCentreName())));

    }

    private Optional<ProsecutionCaseResponse> getProsecutionCaseResponse() {
        Address address = Address.address().withAddress1("addr1").withAddress2("addr2").withAddress3("addr3").
                withAddress4("addr4").withPostcode("AA1 1AA").build();

        List<ProsecutionCase> prosecutionCases = new ArrayList<ProsecutionCase>();
        final ProsecutionCase prosecutionCase = ProsecutionCase.prosecutionCase()
                .withDefendants(Arrays.asList(Defendant.defendant()
                        .withId(randomUUID())
                        .withPersonDefendant(PersonDefendant.personDefendant()
                                .withArrestSummonsNumber("")
                                .withPersonDetails(Person.person()
                                        .withAddress(address)
                                        .withDateOfBirth(date("12/11/1978"))
                                        .withFirstName("First Name")
                                        .withGender(Gender.MALE)
                                        .withLastName("Last Name").build())
                                .withEmployerOrganisation(Organisation.organisation()
                                        .withName("").build()).build()).build())).build();

        prosecutionCases.add(prosecutionCase);

        Optional<ProsecutionCaseResponse> prosecutionCaseResponse = Optional.of(new ProsecutionCaseResponse());
        prosecutionCaseResponse.get().setProsecutionCases(prosecutionCases);
        return prosecutionCaseResponse;
    }

    @Test
    public void shouldPublishHearingEventTimeStampCorrectedPublicEvent() {

        final HearingEventLogged hearingEventLogged = new HearingEventLogged(randomUUID(), randomUUID(), randomUUID(), randomUUID(),
                null, STRING.next(), convertZonedDate(PAST_ZONED_DATE_TIME.next()), convertZonedDate(PAST_ZONED_DATE_TIME.next()), BOOLEAN.next(),
                new uk.gov.moj.cpp.hearing.domain.CourtCentre(randomUUID(), STRING.next(), randomUUID(), STRING.next(), STRING.next(), STRING.next()),
                new uk.gov.moj.cpp.hearing.domain.HearingType(STRING.next(), randomUUID()), STRING.next(), JurisdictionType.CROWN, STRING.next(), randomUUID());

        Optional<ProsecutionCaseResponse> prosecutionCaseResponse = getProsecutionCaseResponse();

        when(prosecutionCaseRetriever.getProsecutionCaseForHearing(any(), any())).thenReturn(prosecutionCaseResponse);

        this.logEventHearingEventProcessor.publishHearingEventLoggedPublicEvent(
                createEnvelope("hearing.hearing-event-logged", this.objectToJsonObjectConverter.convert(hearingEventLogged)));

        verify(this.sender, times(2)).send(this.envelopeArgumentCaptor.capture());
        final JsonEnvelope commandEventEnvelope = envelopeArgumentCaptor.getAllValues().get(0);
        assertThat(commandEventEnvelope, jsonEnvelope(metadata().withName("public.hearing.event-timestamp-corrected"), payloadIsJson(print())));
        assertThat(uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo(commandEventEnvelope, PublicHearingEventLogged.class), isBean(PublicHearingEventLogged.class)
                .with(PublicHearingEventLogged::getCase, isBean(Case.class)
                        .with(Case::getCaseUrn, Matchers.is(hearingEventLogged.getCaseURN())))
                .with(PublicHearingEventLogged::getHearingEventDefinition, isBean(uk.gov.moj.cpp.hearing.eventlog.HearingEventDefinition.class)
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEventDefinition::getHearingEventDefinitionId, Matchers.is(hearingEventLogged.getHearingEventDefinitionId()))
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEventDefinition::isPriority, Matchers.is(!hearingEventLogged.isAlterable())))
                .with(PublicHearingEventLogged::getHearingEvent, isBean(uk.gov.moj.cpp.hearing.eventlog.HearingEvent.class)
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEvent::getHearingEventId, Matchers.is(hearingEventLogged.getHearingEventId()))
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEvent::getRecordedLabel, Matchers.is(hearingEventLogged.getRecordedLabel()))
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEvent::getEventTime, Matchers.is(hearingEventLogged.getEventTime().toLocalDateTime().atZone(ZoneId.of("UTC"))))
                        .with(uk.gov.moj.cpp.hearing.eventlog.HearingEvent::getLastModifiedTime, Matchers.is(hearingEventLogged.getLastModifiedTime().toLocalDateTime().atZone(ZoneId.of("UTC")))))
                .with(PublicHearingEventLogged::getHearing, isBean(uk.gov.moj.cpp.hearing.eventlog.Hearing.class)
                        .with(uk.gov.moj.cpp.hearing.eventlog.Hearing::getCourtCentre, isBean(uk.gov.moj.cpp.hearing.eventlog.CourtCentre.class)
                                .with(uk.gov.moj.cpp.hearing.eventlog.CourtCentre::getCourtCentreId, Matchers.is(hearingEventLogged.getCourtCentre().getId()))
                                .with(uk.gov.moj.cpp.hearing.eventlog.CourtCentre::getCourtCentreName, Matchers.is(hearingEventLogged.getCourtCentre().getName()))
                                .with(uk.gov.moj.cpp.hearing.eventlog.CourtCentre::getCourtRoomId, Matchers.is(hearingEventLogged.getCourtCentre().getRoomId()))
                                .with(uk.gov.moj.cpp.hearing.eventlog.CourtCentre::getCourtRoomName, Matchers.is(hearingEventLogged.getCourtCentre().getRoomName())))
                        .with(uk.gov.moj.cpp.hearing.eventlog.Hearing::getHearingType, Matchers.is(hearingEventLogged.getHearingType().getDescription()))));
    }

    @Test
    public void publishHearingVerdictUpdatedPublicEvent() throws IOException {

        final HearingEventIgnored hearingEventIgnored =
                new HearingEventIgnored(randomUUID(), randomUUID(), randomUUID(), STRING.next(), PAST_ZONED_DATE_TIME.next(), STRING.next(), BOOLEAN.next(), STRING.next());

        this.logEventHearingEventProcessor.publishHearingEventIgnoredPublicEvent(createEnvelope("hearing.hearing-event-ignored",
                this.objectToJsonObjectConverter.convert(hearingEventIgnored)));

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(this.envelopeArgumentCaptor.getValue(), jsonEnvelope(metadata().withName("public.hearing.event-ignored"), payloadIsJson(print())));
        assertThat(uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo(this.envelopeArgumentCaptor.getValue(), HearingEventIgnored.class), isBean(HearingEventIgnored.class)
                .with(HearingEventIgnored::getHearingId, Matchers.is(hearingEventIgnored.getHearingId())));
    }

    @Test
    public void publishHearingEventsUpdated() throws IOException {

        final HearingEventsUpdated hearingEventsUpdated =
                new HearingEventsUpdated(randomUUID(), Arrays.asList(new HearingEvent(randomUUID(), "RL", STRING.next())));

        this.logEventHearingEventProcessor.publishHearingEventsUpdatedEvent(createEnvelope("hearing.hearing-events-updated",
                this.objectToJsonObjectConverter.convert(hearingEventsUpdated)));

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(this.envelopeArgumentCaptor.getValue(), jsonEnvelope(metadata().withName("public.hearing.events-updated"), payloadIsJson(print())));
        assertThat(uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo(this.envelopeArgumentCaptor.getValue(), HearingEventsUpdated.class), isBean(HearingEventsUpdated.class)
                .with(HearingEventsUpdated::getHearingId, Matchers.is(hearingEventsUpdated.getHearingId())));
    }

    private List<CourtRoom> buildCourtRooms(final HearingEventLogged hearingEventLogged) {
        List<CourtRoom> courtRooms = new ArrayList<>();
        CourtRoom courtRoom = CourtRoom.courtRoom().withCourtRoomName(hearingEventLogged.getCourtCentre().getRoomName())
                .withRoomId(hearingEventLogged.getCourtCentre().getRoomId().toString())
                .withSessions(buildSessions(hearingEventLogged))
                .build();
        courtRooms.add(courtRoom);
        return courtRooms;
    }

    private List<Session> buildSessions(final HearingEventLogged hearingEventLogged) {
        List<Session> sessions = new ArrayList<>();
        Session session = Session.session().withSittings(buildSittings(hearingEventLogged)).build();
        sessions.add(session);
        return sessions;
    }

    private List<Sitting> buildSittings(final HearingEventLogged hearingEventLogged) {
        List<Sitting> sittings = new ArrayList<>();
        Sitting sitting = Sitting.sitting()
                .withHearing(buildHearings(hearingEventLogged))
                .build();
        sittings.add(sitting);
        return sittings;
    }

    private List<Hearing> buildHearings(final HearingEventLogged hearingEventLogged) {
        List<Hearing> hearings = new ArrayList<>();
        Hearing hearing = Hearing.hearing().withHearingType(hearingEventLogged.getHearingType().getDescription())
                .withCaseNumber(buildCaseURNs(hearingEventLogged))
                .withStartTime(hearingEventLogged.getEventTime().toString())
                .withHearingEvents(buildHearingEvents(hearingEventLogged))
                .build();
        hearings.add(hearing);
        return hearings;
    }

    private List<uk.gov.moj.cpp.hearing.json.schema.event.HearingEvent> buildHearingEvents(final HearingEventLogged hearingEventLogged) {
        List<uk.gov.moj.cpp.hearing.json.schema.event.HearingEvent> hearingEvents = new ArrayList<>();
        uk.gov.moj.cpp.hearing.json.schema.event.HearingEvent hearingEvent = uk.gov.moj.cpp.hearing.json.schema.event.HearingEvent.hearingEvent()
                .withHearingEvent(hearingEventLogged.getRecordedLabel())
                .withHearingEventTime(hearingEventLogged.getEventTime())
                .build();
        hearingEvents.add(hearingEvent);
        return hearingEvents;
    }


    private List<String> buildCaseURNs(final HearingEventLogged hearingEventLogged) {
        List<String> caseURNs = new ArrayList<>();
        caseURNs.add(hearingEventLogged.getCaseURN());
        return caseURNs;
    }

}