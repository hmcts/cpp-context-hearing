package uk.gov.moj.cpp.hearing.command.api;

import static com.google.common.collect.Lists.newArrayList;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUIDAndName;

import uk.gov.justice.services.common.util.Clock;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.common.helper.StoppedClock;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class OutstandingFinesCommandApiTest {

    private static final String COMMAND_INITIATE_ACCOUNT_QUERY = "hearing.command.add-request-for-outstanding-fines";

    @Spy
    private final Enveloper enveloper = createEnveloper();
    @Spy
    private final Clock clock = new StoppedClock(ZonedDateTime.now());
    @Mock
    private Sender sender;
    @Captor
    private ArgumentCaptor<JsonEnvelope> senderArgumentCaptor;

    @InjectMocks
    private OutstandingFinesCommandApi outstandingFinesCommandApi;


    @Test
    public void addRequestForOutstandingFinesWithoutHearingDate() {

        final JsonObject requestPayload = createObjectBuilder().build();

        final JsonEnvelope commandJsonEnvelope = envelopeFrom(metadataWithRandomUUIDAndName(), requestPayload);

        outstandingFinesCommandApi.addRequestForOutstandingFines(commandJsonEnvelope);

        verify(sender).send(senderArgumentCaptor.capture());

        final JsonEnvelope jsonEnvelopOut = senderArgumentCaptor.getValue();
        //check that payload was passed through and meta data name was changed
        assertThat(jsonEnvelopOut, is(jsonEnvelope(
                withMetadataEnvelopedFrom(commandJsonEnvelope).withName(COMMAND_INITIATE_ACCOUNT_QUERY),
                payloadIsJson(allOf(
                        withJsonPath("$.hearingDate", equalTo(LocalDate.now().plusDays(1).toString())
                        )))
        )));
    }

    @Test
    public void addRequestForOutstandingFinesWithHearingDate() {

        final JsonObject requestPayload = createObjectBuilder().add("hearingDate", "2020-01-01").build();

        final JsonEnvelope commandJsonEnvelope = envelopeFrom(metadataWithRandomUUIDAndName(), requestPayload);

        outstandingFinesCommandApi.addRequestForOutstandingFines(commandJsonEnvelope);

        verify(sender).send(senderArgumentCaptor.capture());

        final JsonEnvelope jsonEnvelopOut = senderArgumentCaptor.getValue();
        //check that payload was passed through and meta data name was changed
        assertThat(jsonEnvelopOut, is(jsonEnvelope(
                withMetadataEnvelopedFrom(commandJsonEnvelope).withName(COMMAND_INITIATE_ACCOUNT_QUERY),
                payloadIsJson(allOf(
                        withJsonPath("$.hearingDate", equalTo("2020-01-01")
                        )))
        )));
    }

}
