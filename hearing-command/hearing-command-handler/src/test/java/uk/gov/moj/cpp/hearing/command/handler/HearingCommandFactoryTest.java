package uk.gov.moj.cpp.hearing.command.handler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.NAME;

import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.messaging.DefaultJsonEnvelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.moj.cpp.hearing.command.handler.converter.JsonToHearingConverter;
import uk.gov.moj.cpp.hearing.domain.command.InitiateHearing;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Test;

public class   HearingCommandFactoryTest {

    private static final String LIST_HEARING_COMMAND = "hearing.command.initiate-hearing";
    private static final UUID HEARING_ID = UUID.randomUUID();
    public static final String START_DATE_TIME = ZonedDateTimes.toString(ZonedDateTime.now());
    public static final int DURATION = 2;

    @Test
    public void shouldReturnCreateHearingObjeect() throws Exception {
        Object object = new JsonToHearingConverter()
                .convertToInitiateHearing(createHearingCommand());

        assertThat(object, instanceOf(InitiateHearing.class));
        InitiateHearing event = (InitiateHearing) object;
        assertThat(event.getStartDateTime().toString(), equalTo(START_DATE_TIME));
        assertThat(event.getDuration(), equalTo(DURATION));

    }


    private JsonEnvelope createHearingCommand() {
        final JsonObject metadataAsJsonObject = Json.createObjectBuilder().add(ID, UUID.randomUUID().toString())
                .add(NAME, LIST_HEARING_COMMAND).build();

        final JsonObject payloadAsJsonObject = Json.createObjectBuilder().add("hearingId", HEARING_ID.toString())
                .add("startDateTime", START_DATE_TIME)
                .add("duration", DURATION).build();

        return DefaultJsonEnvelope.envelopeFrom(JsonObjectMetadata.metadataFrom(metadataAsJsonObject),
                payloadAsJsonObject);

    }


}