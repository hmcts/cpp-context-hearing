package uk.gov.moj.cpp.hearing.command.handler;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.common.converter.LocalDates;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.DefaultJsonEnvelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.domain.HearingTypeEnum;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.command.ListHearing;
import uk.gov.moj.cpp.hearing.domain.command.VacateHearing;
import uk.gov.moj.cpp.hearing.domain.event.HearingListed;
import uk.gov.moj.cpp.hearing.domain.event.HearingVacated;

import javax.json.Json;
import javax.json.JsonObject;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.NAME;
import static uk.gov.justice.services.messaging.JsonObjects.getJsonNumber;
import static uk.gov.justice.services.messaging.JsonObjects.getString;
import static uk.gov.justice.services.messaging.JsonObjects.getUUID;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.UUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.values;

@RunWith(MockitoJUnitRunner.class)
public class HearingCommandHandlerTest {

    private static final UUID HEARING_ID = UUID.next();
    private static final String LIST_HEARING_COMMAND = "hearing.command.list-hearing";
    private static final String HEARING_LISTED_EVENT = "hearing.events.hearing-listed";
    private static final String VACATE_HEARING_COMMAND = "hearing.command.vacate-hearing";
    private static final String HEARING_VACATED_EVENT = "hearing.events.hearing-vacated";

    @Mock
    EventStream eventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    HearingCommandFactory hearingCommandFactory;

    @Mock
    private AggregateService aggregateService;

    @Mock
    private HearingAggregate hearingAggregate;

    @Spy
    private Enveloper enveloper = createEnveloperWithEvents(HearingListed.class, HearingVacated.class);

    @InjectMocks
    private HearingCommandHandler hearingCommandHandler;

    @Test
    public void shouldHandlerListHearingCommand() throws Exception {
        // given
        ListHearing listHearing = createListHearing(HEARING_ID);
        final JsonEnvelope listHearingCommand = createListHearingCommand(listHearing);
        // and
        given(hearingCommandFactory.getListHearing(listHearingCommand)).willReturn(listHearing);
        // and
        given(eventSource.getStreamById(HEARING_ID)).willReturn(eventStream);
        given(aggregateService.get(eventStream, HearingAggregate.class)).willReturn(hearingAggregate);
        // and
        given(hearingAggregate.listHearing(listHearing)).willReturn(Stream.of(createHearingListed(listHearing)));


        // when
        hearingCommandHandler.listHearing(listHearingCommand);

        // then
        final JsonEnvelope resultEvent = verifyAppendAndGetArgumentFrom(eventStream).findFirst().get();
        final Metadata resultMetadata = resultEvent.metadata();
        final JsonObject resultPayload = resultEvent.payloadAsJsonObject();
        // and
        assertThat(resultMetadata.name(), is(HEARING_LISTED_EVENT));
        // and
        assertThat(resultPayload, isFrom(listHearing));

    }

    @Test
    public void shouldHandlerVacateHearingCommand() throws Exception {
        // given
        VacateHearing vacateHearing = createVacateHearing(HEARING_ID);
        final JsonEnvelope vacateHearingCommand = createVacateHearingCommand(vacateHearing);
        // and
        given(hearingCommandFactory.getVacateHearing(vacateHearingCommand)).willReturn(vacateHearing);
        // and
        given(eventSource.getStreamById(HEARING_ID)).willReturn(eventStream);
        given(aggregateService.get(eventStream, HearingAggregate.class)).willReturn(hearingAggregate);
        // and
        given(hearingAggregate.vacateHearing(vacateHearing)).willReturn(Stream.of(createHearingVacated(vacateHearing)));


        // when
        hearingCommandHandler.vacateHearing(vacateHearingCommand);

        // then
        final JsonEnvelope resultEvent = verifyAppendAndGetArgumentFrom(eventStream).findFirst().get();
        final Metadata resultMetadata = resultEvent.metadata();
        final JsonObject resultPayload = resultEvent.payloadAsJsonObject();
        // and
        assertThat(resultMetadata.name(), is(HEARING_VACATED_EVENT));
        // and
        assertThat(resultPayload, isFrom(vacateHearing));

    }

    private HearingVacated createHearingVacated(VacateHearing vacateHearing) {
        return new HearingVacated(vacateHearing.getHearingId());
    }

    private JsonEnvelope createVacateHearingCommand(VacateHearing vacateHearing) {
        final JsonObject metadataAsJsonObject =
                Json.createObjectBuilder()
                        .add(ID, UUID.next().toString())
                        .add(NAME, VACATE_HEARING_COMMAND)
                        .build();

        final JsonObject payloadAsJsonObject = Json.createObjectBuilder()
                .add("hearingId", vacateHearing.getHearingId().toString())
                .build();

        return DefaultJsonEnvelope.envelopeFrom(JsonObjectMetadata.metadataFrom(metadataAsJsonObject), payloadAsJsonObject);
    }

    private VacateHearing createVacateHearing(UUID hearingId) {
        return new VacateHearing(hearingId);
    }

    private JsonEnvelope createListHearingCommand(ListHearing listHearing) {
        final JsonObject metadataAsJsonObject =
                Json.createObjectBuilder()
                        .add(ID, UUID.next().toString())
                        .add(NAME, LIST_HEARING_COMMAND)
                        .build();

        final JsonObject payloadAsJsonObject = Json.createObjectBuilder()
                .add("hearingId", listHearing.getHearingId().toString())
                .add("courtCentreName", listHearing.getCourtCentreName())
                .add("hearingType", listHearing.getHearingType().getValue())
                .add("dateOfSending", LocalDates.to(listHearing.getStartDateOfHearing()))
                .add("duration", listHearing.getDuration())
                .add("caseId", listHearing.getCaseId().toString())
                .build();

        return DefaultJsonEnvelope.envelopeFrom(JsonObjectMetadata.metadataFrom(metadataAsJsonObject), payloadAsJsonObject);

    }

    private HearingListed createHearingListed(ListHearing listHearing) {
        return new HearingListed(listHearing.getHearingId(), listHearing.getCaseId(),
                listHearing.getHearingType(), listHearing.getCourtCentreName(),
                listHearing.getStartDateOfHearing(), listHearing.getDuration());
    }

    private ListHearing createListHearing(UUID hearingId) {
        UUID caseId = randomUUID();
        HearingTypeEnum hearingType = values(HearingTypeEnum.values()).next();
        String courtCentreName = STRING.next();
        LocalDate startDateOfHearing = PAST_LOCAL_DATE.next();
        Integer duration = INTEGER.next();
        return new ListHearing(hearingId, caseId, hearingType, courtCentreName, startDateOfHearing, duration);
    }

    private Matcher<JsonObject> isFrom(final ListHearing listHearing) {
        return new TypeSafeDiagnosingMatcher<JsonObject>() {

            @Override
            public void describeTo(Description description) {
                description.appendText(listHearing.toString());
            }

            @Override
            protected boolean matchesSafely(JsonObject resultPayload, Description description) {
                boolean returnStatus = true;

                if (!Objects.equals(listHearing.getHearingId(), getUUID(resultPayload, "hearingId").get())) {
                    description.appendText(format("HearingId Mismatch:listHearing:%s, hearingListed%s",
                            listHearing.getHearingId(), getUUID(resultPayload, "hearingId").get()));
                    returnStatus = false;
                }

                if (!Objects.equals(listHearing.getCaseId(), getUUID(resultPayload, "caseId").get())) {
                    description.appendText(format("CaseId Mismatch:listHearing:%s, hearingListed%s",
                            listHearing.getCaseId(), getUUID(resultPayload, "caseId").get()));
                    returnStatus = false;
                }

                if (!Objects.equals(listHearing.getCourtCentreName(), getString(resultPayload, "courtCentreName").get())) {
                    description.appendText(format("CourtCentreName Mismatch:listHearing:%s, hearingListed%s",
                            listHearing.getCourtCentreName(), getString(resultPayload, "courtCentreName").get()));
                    returnStatus = false;
                }

                if (!Objects.equals(listHearing.getDuration(), getJsonNumber(resultPayload, "duration").get().intValue())) {
                    description.appendText(format("Duration Mismatch:listHearing:%s, hearingListed%s",
                            listHearing.getDuration(), getJsonNumber(resultPayload, "duration").get().intValue()));
                    returnStatus = false;
                }


                if (!Objects.equals(listHearing.getHearingType().name(), getString(resultPayload, "hearingType").get())) {
                    description.appendText(format("HearingType Mismatch:listHearing:%s, hearingListed%s",
                            listHearing.getHearingType(), getString(resultPayload, "hearingType").get()));
                    returnStatus = false;
                }

                if (!Objects.equals(LocalDates.to(listHearing.getStartDateOfHearing()), getString(resultPayload, "startDateOfHearing").get())) {
                    description.appendText(format("StartDateOfHearing Mismatch:listHearing:%s, hearingListed%s",
                            listHearing.getStartDateOfHearing(), getString(resultPayload, "startDateOfHearing").get()));
                    returnStatus = false;
                }

                return returnStatus;
            }
        };
    }

    private Matcher<JsonObject> isFrom(final VacateHearing vacateHearing) {
        return new TypeSafeDiagnosingMatcher<JsonObject>() {

            @Override
            public void describeTo(Description description) {
                description.appendText(vacateHearing.toString());
            }

            @Override
            protected boolean matchesSafely(JsonObject resultPayload, Description description) {
                if (!Objects.equals(vacateHearing.getHearingId(), getUUID(resultPayload, "hearingId").get())) {
                    description.appendText(format("HearingId Mismatch:vacateHearing:%s, hearingVacated%s",
                            vacateHearing.getHearingId(), getUUID(resultPayload, "hearingId").get()));
                    return false;
                } else {
                    return true;
                }
            }
        };
    }
}
