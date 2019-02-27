package uk.gov.moj.cpp.hearing.event;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.progression.events.SendingSheetCompleted;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Defendant;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Hearing;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Offence;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Plea;
import uk.gov.moj.cpp.hearing.domain.event.MagsCourtHearingRecorded;
import uk.gov.moj.cpp.hearing.domain.event.SendingSheetCompletedRecorded;
import uk.gov.moj.cpp.hearing.test.TestTemplates.PleaValueType;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;


public class MagistratesCourtInitiateHearingEventProcessorTest {

    @Spy
    private final Enveloper enveloper = createEnveloper();
    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
    @Spy
    @InjectMocks
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter();
    @Spy
    @InjectMocks
    private final ObjectToJsonValueConverter objectToJsonValueConverter = new ObjectToJsonValueConverter(this.objectMapper);
    @Spy
    @InjectMocks
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();
    @Mock
    private Sender sender;
    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;
    @InjectMocks
    private MagistratesCourtInitiateHearingEventProcessor magistratesCourtInitiateHearingEventProcessor;


    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void recordSendSheetCompleted() {

        SendingSheetCompleted sendingSheetCompleted = SendingSheetCompleted.builder()
                .withHearing(
                        Hearing.builder()
                                .withCaseId(randomUUID())
                                .withCourtCentreId(randomUUID().toString())
                                .build()
                )
                .build();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("public.progression.events.sending-sheet-completed"),
                objectToJsonObjectConverter.convert(sendingSheetCompleted));

        this.magistratesCourtInitiateHearingEventProcessor.recordSendSheetCompleted(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(
                this.envelopeArgumentCaptor.getValue(), jsonEnvelope(
                        metadata().withName("hearing.record-sending-sheet-complete"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearing.caseId", is(sendingSheetCompleted.getHearing().getCaseId().toString()))

                                )
                        )
                )
        );

    }


    @Test
    public void processSendingSheetRecordedRecordMags() {

        SendingSheetCompletedRecorded sendingSheetCompletedRecorded = SendingSheetCompletedRecorded.builder()
                .withHearing(
                        Hearing.builder()
                                .withCaseId(randomUUID())
                                .withCourtCentreId(randomUUID().toString())
                                .build()
                )
                .build();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.sending-sheet-recorded"),
                objectToJsonObjectConverter.convert(sendingSheetCompletedRecorded));

        this.magistratesCourtInitiateHearingEventProcessor.processSendingSheetRecordedRecordMags(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(
                this.envelopeArgumentCaptor.getValue(), jsonEnvelope(
                        metadata().withName("hearing.record-mags-court-hearing"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearing.caseId", is(sendingSheetCompletedRecorded.getHearing().getCaseId().toString()))

                                )
                        )
                )
        );
    }

    @Test
    public void processMagistratesCourtHearing() {

        MagsCourtHearingRecorded magsCourtHearingRecorded = MagsCourtHearingRecorded.builder()
                .withHearing(
                        Hearing.builder()
                                .withCaseId(randomUUID())
                                .withCourtCentreId(randomUUID().toString())
                                .withDefendants(asList(Defendant.builder()
                                        .withId(randomUUID())
                                        .withOffences(asList(
                                                Offence.builder()
                                                        .withId(randomUUID())
                                                        .withCategory(STRING.next())
                                                        .withPlea(Plea.plea()
                                                                .withPleaValue(PleaValueType.GUILTY.name())
                                                                .withPleaDate(PAST_LOCAL_DATE.next())
                                                                .build())
                                                        .build(),
                                                Offence.builder()
                                                        .withId(randomUUID())
                                                        .withCategory(STRING.next())
                                                        .withPlea(Plea.plea()
                                                                .withPleaValue(PleaValueType.NOT_GUILTY.name())
                                                                .withPleaDate(PAST_LOCAL_DATE.next())
                                                                .build())
                                                        .build()
                                        ))
                                        .build()))
                                .build()
                )
                .withHearingId(randomUUID())
                .build();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.mags-court-hearing-recorded"),
                objectToJsonObjectConverter.convert(magsCourtHearingRecorded));

        this.magistratesCourtInitiateHearingEventProcessor.processMagistratesCourtHearing(event);

        verify(this.sender, times(2)).send(this.envelopeArgumentCaptor.capture());

        List<JsonEnvelope> events = this.envelopeArgumentCaptor.getAllValues();

        assertThat(events.get(0), jsonEnvelope(
                metadata().withName("public.mags.hearing.initiated"),
                payloadIsJson(allOf(
                        withJsonPath("$.caseId", is(magsCourtHearingRecorded.getOriginatingHearing().getCaseId().toString()))

                        )
                ))
        );

        Offence offence = magsCourtHearingRecorded.getOriginatingHearing().getDefendants().get(0).getOffences().get(0);
        assertThat(events.get(1), jsonEnvelope(
                metadata().withName("hearing.command.update-plea-against-offence"),
                payloadIsJson(allOf(
                        withJsonPath("$.hearingId", is(magsCourtHearingRecorded.getHearingId().toString())),
                        withJsonPath("$.plea.offenceId", is(offence.getId().toString())),
                        withJsonPath("$.plea.pleaDate", is(offence.getPlea().getPleaDate().toString())),
                        withJsonPath("$.plea.pleaValue", is(offence.getPlea().getValue()))
                ))
                )
        );
    }
}