package uk.gov.moj.cpp.hearing.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.DefaultJsonEnvelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.CrownCourtHearing;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Defendant;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Hearing;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Offence;
import uk.gov.moj.cpp.hearing.command.RecordMagsCourtHearingCommand;
import uk.gov.moj.cpp.hearing.domain.event.SendingSheetCompletedRecorded;

import javax.json.JsonObject;
import javax.json.JsonValue;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;

public class MagistratesCourtInitiateHearingEventProcessorTest {

    @Mock
    private Sender sender;

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Spy
    @InjectMocks
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter();

    @Spy
    @InjectMocks
    private final ObjectToJsonValueConverter objectToJsonValueConverter = new ObjectToJsonValueConverter(this.objectMapper);

    @InjectMocks
    private MagistratesCourtInitiateHearingEventProcessor hearingEventProcessor;


    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProcessSendingSheetRecordedRecordMags() {
        final UUID caseId = UUID.randomUUID();
        final UUID courtCentreId = UUID.randomUUID();
        final Hearing originatingHearing = (new Hearing.Builder()).withCaseId(caseId).withCourtCentreId(courtCentreId.toString()).build();
        final LocalDate convictionDate = LocalDate.now();
        final UUID followingHearingId = UUID.randomUUID();
        final CrownCourtHearing crownCourtHearing = (new CrownCourtHearing.Builder()).build();
        final List<Offence> offences  = Arrays.asList(new Offence.Builder().build());
        final Defendant defendant = (new Defendant.Builder()).withFirstName("David")
                .withLastName("Bowie")
                .withOffences(offences).build();
        final List<Defendant> defendants = newArrayList(defendant);
        final Hearing hearing = (new Hearing.Builder()).withCaseId(caseId).withDefendants(defendants).build();
        final SendingSheetCompletedRecorded sendingSheetCompletedRecorded = new SendingSheetCompletedRecorded(crownCourtHearing, hearing);

        final RecordMagsCourtHearingCommand command = transactEvent2Command(sendingSheetCompletedRecorded,
                (event) -> this.hearingEventProcessor.processSendingSheetRecordedRecordMags(event), RecordMagsCourtHearingCommand.class, 1);

        assertEquals(caseId, command.getHearing().getCaseId());
        assertEquals(defendant.getLastName(), command.getHearing().getDefendants().get(0).getLastName());
    }

    private <E, C> C transactEvent2Command(final E typedEvent, final Consumer<JsonEnvelope> methodUnderTest, final Class commandClass, int sendCount) {
        final JsonValue payload = this.objectToJsonValueConverter.convert(typedEvent);
        final Metadata metadata = metadataWithDefaults().build();
        final JsonEnvelope event = new DefaultJsonEnvelope(metadata, payload);
        methodUnderTest.accept(event);
        verify(this.sender, times(sendCount)).send(this.envelopeArgumentCaptor.capture());
        List<JsonEnvelope> messages = this.envelopeArgumentCaptor.getAllValues();

        final JsonEnvelope result =  messages.get(0);//this.envelopeArgumentCaptor.getValue();
        final JsonObject resultingPayload = result.payloadAsJsonObject();
        return (C) jsonObjectToObjectConverter.convert(resultingPayload, commandClass);
    }
}