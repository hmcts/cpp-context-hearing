package uk.gov.moj.cpp.hearing.xhibit;

import static java.time.ZonedDateTime.*;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.xhibit.pojo.PublishCourtListRequestParameters;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import javax.json.JsonObject;

import org.junit.jupiter.api.Test;


public class PublishCourtListRequestParametersParserTest {

    @Test
    public void shouldParse() {

        final UUID courtCentreId = randomUUID();
        final ZonedDateTime createdTime = now(ZoneId.of("UTC").normalized());

        final JsonObject payload = createObjectBuilder()
                .add("courtCentreId", courtCentreId.toString())
                .add("createdTime", createdTime.toString())
                .build();

        final Metadata metadata = mock(Metadata.class);
        final JsonEnvelope tEnvelope = envelopeFrom(metadata, payload);

        final PublishCourtListRequestParameters parameters = new PublishCourtListRequestParametersParser().parse(tEnvelope);

        assertThat(parameters.getCourtCentreId(), is(courtCentreId.toString()));
        assertThat(parameters.getCreatedTime(), is(createdTime.toString()));
    }

}