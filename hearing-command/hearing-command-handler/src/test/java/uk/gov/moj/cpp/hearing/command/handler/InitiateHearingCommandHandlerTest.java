package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static uk.gov.justice.core.courts.Defendant.defendant;
import static uk.gov.justice.core.courts.Plea.plea;
import static uk.gov.justice.core.courts.PleaModel.pleaModel;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.JudicialRole;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.hearing.details.UpdateRelatedHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.ExtendHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstCaseCommand;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstDefendantCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.ApplicationAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.CaseAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.DefendantAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;
import uk.gov.moj.cpp.hearing.domain.event.ExistingHearingUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingExtended;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.InheritedPlea;
import uk.gov.moj.cpp.hearing.domain.event.InheritedVerdictAdded;
import uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstApplication;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstCase;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstDefendant;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstOffence;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InitiateHearingCommandHandlerTest {
    private static final String GUILTY = "GUILTY";

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            HearingInitiated.class,
            InheritedPlea.class,
            RegisteredHearingAgainstDefendant.class,
            RegisteredHearingAgainstOffence.class,
            RegisteredHearingAgainstCase.class,
            InheritedVerdictAdded.class,
            RegisteredHearingAgainstApplication.class,
            HearingExtended.class,
            ExistingHearingUpdated.class,
            RegisteredHearingAgainstDefendant.class
    );
    @Mock
    private EventStream hearingEventStream, applicationEventStream;
    @Mock
    private EventStream offenceEventStream;
    @Mock
    private EventStream defendantEventStream;
    @Mock
    private EventStream caseEventStream;
    @Mock
    private EventSource eventSource;
    @Mock
    private AggregateService aggregateService;
    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;
    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;
    @InjectMocks
    private InitiateHearingCommandHandler hearingCommandHandler;
    @Captor
    private ArgumentCaptor<Stream<JsonEnvelope>> argumentCaptor;
    @Captor
    private ArgumentCaptor<Stream<JsonEnvelope>> argumentCaptorApplication;
    @Captor
    private ArgumentCaptor<Stream<JsonEnvelope>> registerHearingAgainstDefendantCaptor;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    /**
     * The test will be refactored part of DD-9620
     *
     * @throws Throwable
     */
    @Test
    public void extendHearing() throws Throwable {
        extendHearing((h) -> {
        });
    }

    /**
     * The test will be refactored part of DD-9620
     *
     * @throws Throwable
     */
    @Test
    public void extendHearingProsecutionCases() throws Throwable {
        extendHearingWithProsecutionCases((h) -> {
        });
    }

    /**
     * The test will be refactored part of DD-9620
     *
     * @throws Throwable
     */
    @Test
    public void updateExistingHearingProsecutionCases() throws Throwable {
        updateExistingHearingWithProsecutionCases((h) -> {
        });
    }

    /**
     * The test will be refactored part of DD-9620
     *
     * @throws Throwable
     */
    @Test
    public void extendHearingNullInitialApplications() throws Throwable {

        extendHearing((h) -> h.setCourtApplications(null));
    }

    private void extendHearing(Consumer<Hearing> hearingModification) throws Throwable {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(standardInitiateHearingTemplate());
        hearingModification.accept(hearingOne.getHearing());

        final JsonEnvelope initcommand = envelopeFrom(metadataWithRandomUUID("hearing.initiate"), objectToJsonObjectConverter.convert(hearingOne.it()));

        setupMockedEventStream(hearingOne.getHearingId(), this.hearingEventStream, new HearingAggregate());
        if (hearingOne.getHearing().getCourtApplications() != null) {
            setupMockedEventStream(hearingOne.getHearing().getCourtApplications().get(0).getId(), this.applicationEventStream, new ApplicationAggregate());
        }

        this.hearingCommandHandler.initiate(initcommand);

        final ExtendHearingCommand command = new ExtendHearingCommand();
        command.setCourtApplication(CourtApplication.courtApplication().withId(UUID.randomUUID()).build());
        command.setHearingId(hearingOne.getHearingId());
        setupMockedEventStream(command.getCourtApplication().getId(), this.applicationEventStream, new ApplicationAggregate());

        final JsonEnvelope commandJson = envelopeFrom(metadataWithRandomUUID("hearing.extend-hearing"), objectToJsonObjectConverter.convert(command));

        this.hearingCommandHandler.extendHearing(commandJson);

        //final JsonEnvelope jsonEnvelope = verifyAppendAndGetArgumentFrom(this.hearingEventStream).findAny().get();

        ArgumentCaptor<Stream> argumentCaptor = ArgumentCaptor.forClass(Stream.class);
        ((EventStream) Mockito.verify(this.hearingEventStream, times(2))).append((Stream) argumentCaptor.capture());
        final JsonEnvelope jsonEnvelope = (JsonEnvelope) argumentCaptor.getAllValues().get(1).findFirst().orElse(null);

        HearingExtended hearingExtended = asPojo(jsonEnvelope, HearingExtended.class);

        assertThat(hearingExtended.getCourtApplication().getId(), is(command.getCourtApplication().getId()));
        assertThat(hearingExtended.getHearingId(), is(command.getHearingId()));

    }


    private void extendHearingWithProsecutionCases(Consumer<Hearing> hearingModification) throws EventStreamException {
        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(standardInitiateHearingTemplate());
        hearingModification.accept(hearingOne.getHearing());

        final JsonEnvelope initCommand = envelopeFrom(metadataWithRandomUUID("hearing.initiate"), objectToJsonObjectConverter.convert(hearingOne.it()));

        setupMockedEventStream(hearingOne.getHearingId(), this.hearingEventStream, new HearingAggregate());
        if (hearingOne.getHearing().getCourtApplications() != null) {
            setupMockedEventStream(hearingOne.getHearing().getCourtApplications().get(0).getId(), this.applicationEventStream, new ApplicationAggregate());
        }

        this.hearingCommandHandler.initiate(initCommand);

        final ExtendHearingCommand command = new ExtendHearingCommand();
        command.setProsecutionCases(singletonList(ProsecutionCase.prosecutionCase()
                .withId(randomUUID())
                .withDefendants(
                        asList(defendant().withId(randomUUID()).build(), defendant().withId(randomUUID()).build())).build()));
        command.setHearingId(hearingOne.getHearingId());

        command.getProsecutionCases().get(0).getDefendants().forEach(defendant ->
                setupMockedEventStream(defendant.getId(), this.defendantEventStream, new DefendantAggregate()));

        final JsonEnvelope commandJson = envelopeFrom(metadataWithRandomUUID("hearing.extend-hearing"), objectToJsonObjectConverter.convert(command));

        this.hearingCommandHandler.extendHearing(commandJson);

        Mockito.verify(this.hearingEventStream, times(2)).append(argumentCaptor.capture());
        Mockito.verify(this.applicationEventStream).append(argumentCaptorApplication.capture());
        Mockito.verify(this.defendantEventStream, times(2)).append(registerHearingAgainstDefendantCaptor.capture());

        final JsonEnvelope jsonEnvelope = argumentCaptor.getAllValues().get(1).findFirst().orElse(null);
        HearingExtended hearingExtended = asPojo(jsonEnvelope, HearingExtended.class);

        assertThat(hearingExtended.getProsecutionCases().get(0).getId(), is(command.getProsecutionCases().get(0).getId()));
        assertThat(hearingExtended.getHearingId(), is(command.getHearingId()));

        final List<Stream<JsonEnvelope>> registerHearingAgainstDefendants = registerHearingAgainstDefendantCaptor.getAllValues();
        assertThat(registerHearingAgainstDefendants.size(), is(2));
        Stream.concat(registerHearingAgainstDefendants.get(0), registerHearingAgainstDefendants.get(1))
                .map(env -> asPojo(env, RegisteredHearingAgainstDefendant.class))
                .forEach(registration -> assertThat(registration.getHearingId(), is(hearingExtended.getHearingId())));
    }

    private void updateExistingHearingWithProsecutionCases(final Consumer<Hearing> hearingModification) throws EventStreamException {
        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(standardInitiateHearingTemplate());
        hearingModification.accept(hearingOne.getHearing());

        final JsonEnvelope initCommand = envelopeFrom(metadataWithRandomUUID("hearing.initiate"), objectToJsonObjectConverter.convert(hearingOne.it()));

        setupMockedEventStream(hearingOne.getHearingId(), this.hearingEventStream, new HearingAggregate());
        if (hearingOne.getHearing().getCourtApplications() != null) {
            setupMockedEventStream(hearingOne.getHearing().getCourtApplications().get(0).getId(), this.applicationEventStream, new ApplicationAggregate());
        }

        this.hearingCommandHandler.initiate(initCommand);

        final UpdateRelatedHearingCommand command = new UpdateRelatedHearingCommand();
        command.setProsecutionCases(asList(ProsecutionCase.prosecutionCase().withId(UUID.randomUUID()).build()));
        command.setHearingId(hearingOne.getHearingId());

        final JsonEnvelope commandJson = envelopeFrom(metadataWithRandomUUID("hearing.command.update-related-hearing"), objectToJsonObjectConverter.convert(command));

        this.hearingCommandHandler.updateRelatedHearing(commandJson);

        ArgumentCaptor<Stream> argumentCaptor = ArgumentCaptor.forClass(Stream.class);
        ((EventStream) Mockito.verify(this.hearingEventStream, times(2))).append((Stream) argumentCaptor.capture());

        final JsonEnvelope jsonEnvelope = (JsonEnvelope) argumentCaptor.getAllValues().get(1).findFirst().orElse(null);

        ExistingHearingUpdated existingHearingUpdated = asPojo(jsonEnvelope, ExistingHearingUpdated.class);

        assertThat(existingHearingUpdated.getProsecutionCases().get(0).getId(), is(command.getProsecutionCases().get(0).getId()));
        assertThat(existingHearingUpdated.getHearingId(), is(command.getHearingId()));
    }

    @Test
    public void initiateHearing() throws Throwable {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(standardInitiateHearingTemplate());

        setupMockedEventStream(hearingOne.getHearingId(), this.hearingEventStream, new HearingAggregate());
        setupMockedEventStream(hearingOne.getHearing().getCourtApplications().get(0).getId(), this.applicationEventStream, new ApplicationAggregate());

        final JsonEnvelope command = envelopeFrom(metadataWithRandomUUID("hearing.initiate"), objectToJsonObjectConverter.convert(hearingOne.it()));

        this.hearingCommandHandler.initiate(command);

        final JsonEnvelope jsonEnvelope = verifyAppendAndGetArgumentFrom(this.hearingEventStream).findAny().get();

        final Hearing hearing = hearingOne.it().getHearing();
        final JudicialRole judicialRole = hearing.getJudiciary().get(0);
        final CourtCentre courtCentre = hearing.getCourtCentre();
        final HearingDay hearingDay = hearing.getHearingDays().get(0);
        final ProsecutionCase prosecutionCase = hearing.getProsecutionCases().get(0);
        final ProsecutionCaseIdentifier prosecutionCaseIdentifier = prosecutionCase.getProsecutionCaseIdentifier();
        final Defendant defendant = prosecutionCase.getDefendants().get(0);

        final Offence offence = defendant.getOffences().get(0);

        assertThat(asPojo(jsonEnvelope, HearingInitiated.class), isBean(HearingInitiated.class)
                .with(HearingInitiated::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getReportingRestrictionReason, is(hearing.getReportingRestrictionReason()))
                        .with(Hearing::getHasSharedResults, is(false))
                        .with(Hearing::getHearingLanguage, is(hearing.getHearingLanguage()))
                        .with(Hearing::getJurisdictionType, is(JurisdictionType.CROWN))
                        .with(Hearing::getType, isBean(HearingType.class)
                                .with(HearingType::getId, is(hearing.getType().getId()))
                                .with(HearingType::getDescription, is(hearing.getType().getDescription())))
                        .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                .with(CourtCentre::getId, is(courtCentre.getId()))
                                .with(CourtCentre::getName, is(courtCentre.getName()))
                                .with(CourtCentre::getRoomId, is(courtCentre.getRoomId()))
                                .with(CourtCentre::getRoomName, is(courtCentre.getRoomName())))
                        .with(Hearing::getHearingDays, first(isBean(HearingDay.class)
                                .with(HearingDay::getSittingDay, is(hearingDay.getSittingDay().withZoneSameLocal(ZoneId.of("UTC"))))
                                .with(HearingDay::getListingSequence, is(hearingDay.getListingSequence()))))
                        .with(Hearing::getJudiciary, first(isBean(JudicialRole.class)
                                .with(JudicialRole::getFirstName, is(judicialRole.getFirstName()))
                                .with(JudicialRole::getIsBenchChairman, is(judicialRole.getIsBenchChairman()))
                                .with(JudicialRole::getIsDeputy, is(judicialRole.getIsDeputy()))
                                .with(JudicialRole::getJudicialId, is(judicialRole.getJudicialId()))
                                .with(JudicialRole::getJudicialRoleType, is(judicialRole.getJudicialRoleType()))
                                .with(JudicialRole::getLastName, is(judicialRole.getLastName()))
                                .with(JudicialRole::getMiddleName, is(judicialRole.getMiddleName()))
                                .with(JudicialRole::getTitle, is(judicialRole.getTitle()))))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getId, is(prosecutionCase.getId()))
                                .with(ProsecutionCase::getCaseStatus, is(prosecutionCase.getCaseStatus()))
                                .with(ProsecutionCase::getInitiationCode, is(prosecutionCase.getInitiationCode()))
                                .with(ProsecutionCase::getClassOfCase, is(prosecutionCase.getClassOfCase()))
                                .with(ProsecutionCase::getOriginatingOrganisation, is(prosecutionCase.getOriginatingOrganisation()))
                                .with(ProsecutionCase::getProsecutionCaseIdentifier, isBean(ProsecutionCaseIdentifier.class)
                                        .with(ProsecutionCaseIdentifier::getCaseURN, is(prosecutionCaseIdentifier.getCaseURN()))
                                        .with(ProsecutionCaseIdentifier::getProsecutionAuthorityCode, is(prosecutionCaseIdentifier.getProsecutionAuthorityCode()))
                                        .with(ProsecutionCaseIdentifier::getProsecutionAuthorityReference, is(prosecutionCaseIdentifier.getProsecutionAuthorityReference()))
                                        .with(ProsecutionCaseIdentifier::getProsecutionAuthorityId, is(prosecutionCaseIdentifier.getProsecutionAuthorityId())))
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getId, is(defendant.getId()))
                                        .with(Defendant::getMasterDefendantId, is(defendant.getMasterDefendantId()))
                                        .with(Defendant::getCourtProceedingsInitiated, is(defendant.getCourtProceedingsInitiated().withZoneSameLocal(ZoneId.of("UTC"))))
                                        .with(Defendant::getProsecutionCaseId, is(defendant.getProsecutionCaseId()))
                                        .with(Defendant::getNumberOfPreviousConvictionsCited, is(defendant.getNumberOfPreviousConvictionsCited()))
                                        .with(Defendant::getProsecutionAuthorityReference, is(defendant.getProsecutionAuthorityReference()))
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(offence.getId()))
                                                .with(Offence::getOffenceDefinitionId, is(offence.getOffenceDefinitionId()))
                                                .with(Offence::getOffenceCode, is(offence.getOffenceCode()))
                                                .with(Offence::getWording, is(offence.getWording()))
                                                .with(Offence::getStartDate, is(offence.getStartDate()))
                                                .with(Offence::getOrderIndex, is(offence.getOrderIndex()))
                                                .with(Offence::getCount, is(offence.getCount()))))
                                ))))));
    }

    @Test
    public void initiateHearingApplicationOnly() throws Throwable {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(standardInitiateHearingTemplate());
        hearingOne.getHearing().setProsecutionCases(null);

        setupMockedEventStream(hearingOne.getHearingId(), this.hearingEventStream, new HearingAggregate());
        setupMockedEventStream(hearingOne.getHearing().getCourtApplications().get(0).getId(), this.applicationEventStream, new ApplicationAggregate());

        final JsonEnvelope command = envelopeFrom(metadataWithRandomUUID("hearing.initiate"), objectToJsonObjectConverter.convert(hearingOne.it()));

        this.hearingCommandHandler.initiate(command);

        final JsonEnvelope jsonEnvelope = verifyAppendAndGetArgumentFrom(this.hearingEventStream).findAny().get();

        final Hearing hearing = hearingOne.it().getHearing();
        final JudicialRole judicialRole = hearing.getJudiciary().get(0);
        final CourtCentre courtCentre = hearing.getCourtCentre();
        final HearingDay hearingDay = hearing.getHearingDays().get(0);

        assertThat(asPojo(jsonEnvelope, HearingInitiated.class), isBean(HearingInitiated.class)
                .with(HearingInitiated::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getReportingRestrictionReason, is(hearing.getReportingRestrictionReason()))
                        .with(Hearing::getHasSharedResults, is(false))
                        .with(Hearing::getHearingLanguage, is(hearing.getHearingLanguage()))
                        .with(Hearing::getJurisdictionType, is(JurisdictionType.CROWN))
                        .with(Hearing::getType, isBean(HearingType.class)
                                .with(HearingType::getId, is(hearing.getType().getId()))
                                .with(HearingType::getDescription, is(hearing.getType().getDescription())))
                        .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                .with(CourtCentre::getId, is(courtCentre.getId()))
                                .with(CourtCentre::getName, is(courtCentre.getName()))
                                .with(CourtCentre::getRoomId, is(courtCentre.getRoomId()))
                                .with(CourtCentre::getRoomName, is(courtCentre.getRoomName())))
                        .with(Hearing::getHearingDays, first(isBean(HearingDay.class)
                                .with(HearingDay::getSittingDay, is(hearingDay.getSittingDay().withZoneSameLocal(ZoneId.of("UTC"))))
                                .with(HearingDay::getListingSequence, is(hearingDay.getListingSequence()))))
                        .with(Hearing::getJudiciary, first(isBean(JudicialRole.class)
                                .with(JudicialRole::getFirstName, is(judicialRole.getFirstName()))
                                .with(JudicialRole::getIsBenchChairman, is(judicialRole.getIsBenchChairman()))
                                .with(JudicialRole::getIsDeputy, is(judicialRole.getIsDeputy()))
                                .with(JudicialRole::getJudicialId, is(judicialRole.getJudicialId()))
                                .with(JudicialRole::getJudicialRoleType, is(judicialRole.getJudicialRoleType()))
                                .with(JudicialRole::getLastName, is(judicialRole.getLastName()))
                                .with(JudicialRole::getMiddleName, is(judicialRole.getMiddleName()))
                                .with(JudicialRole::getTitle, is(judicialRole.getTitle()))))
                ));
    }


    @Test
    public void initiateHearingOffence() throws Throwable {

        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();

        final UUID originHearingId = randomUUID();
        final LocalDate pleaDate = PAST_LOCAL_DATE.next();
        final String value = "GUILTY";

        final JsonEnvelope command = envelopeFrom(metadataWithRandomUUID("hearing.initiate"), createObjectBuilder()
                .add("offenceId", offenceId.toString())
                .add("hearingId", hearingId.toString())
                .build());

        final OffenceAggregate offenceAggregate = new OffenceAggregate();
        offenceAggregate.apply(new OffencePleaUpdated(originHearingId,
                pleaModel().withPlea(plea()
                        .withOffenceId(offenceId)
                        .withPleaDate(pleaDate)
                        .withPleaValue(value)
                        .withOriginatingHearingId(originHearingId)
                        .build()).build()));
        setupMockedEventStream(offenceId, this.offenceEventStream, offenceAggregate);

        this.hearingCommandHandler.initiateHearingOffence(command);

        List<Object> events = verifyAppendAndGetArgumentFrom(this.offenceEventStream).collect(toList());

        assertThat((JsonEnvelope) events.get(0),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName("hearing.events.registered-hearing-against-offence"),
                        payloadIsJson(allOf(
                                withJsonPath("$.offenceId", is(offenceId.toString())),
                                withJsonPath("$.hearingId", is(hearingId.toString()))
                        ))).thatMatchesSchema()
        );
    }


    @Test
    public void recordHearingDefendant() throws EventStreamException {

        RegisterHearingAgainstDefendantCommand command = RegisterHearingAgainstDefendantCommand.builder()
                .withDefendantId(randomUUID())
                .withHearingId(randomUUID())
                .build();

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.command.register-defendant-with-hearing"), objectToJsonObjectConverter.convert(command));

        setupMockedEventStream(command.getDefendantId(), defendantEventStream, new DefendantAggregate());

        hearingCommandHandler.recordHearingDefendant(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(defendantEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(envelope).withName("hearing.events.registered-hearing-against-defendant"),
                        payloadIsJson(allOf(
                                withJsonPath("$.defendantId", is(command.getDefendantId().toString())),
                                withJsonPath("$.hearingId", is(command.getHearingId().toString()))
                        ))).thatMatchesSchema()));
    }

    @Test
    public void registerHearingAgainstCase() throws EventStreamException {

        RegisterHearingAgainstCaseCommand command = RegisterHearingAgainstCaseCommand.builder()
                .withCaseId(randomUUID())
                .withHearingId(randomUUID())
                .build();

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.command.register-hearing-against-case"), objectToJsonObjectConverter.convert(command));

        setupMockedEventStream(command.getCaseId(), caseEventStream, new CaseAggregate());

        hearingCommandHandler.registerHearingAgainstCase(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(caseEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(envelope).withName("hearing.events.registered-hearing-against-case"),
                        payloadIsJson(allOf(
                                withJsonPath("$.caseId", is(command.getCaseId().toString())),
                                withJsonPath("$.hearingId", is(command.getHearingId().toString()))
                        ))).thatMatchesSchema()));
    }

    private <T extends Aggregate> void setupMockedEventStream(final UUID id, final EventStream eventStream, final T aggregate) {
        when(this.eventSource.getStreamById(id)).thenReturn(eventStream);
        final Class<T> clz = (Class<T>) aggregate.getClass();
        when(this.aggregateService.get(eventStream, clz)).thenReturn(aggregate);
    }
}