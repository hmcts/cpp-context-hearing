package uk.gov.moj.cpp.hearing.event.nows;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

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
public class StagingEnforcementEventProcessorTest {

    private static final String COURTS = "courts";
    private static final String ATCM = "atcm";
    private static final String ARBITRARY_ACCOUNT_NUMBER = "201366829";
    private static final String ARBITRARY_REQUEST_ID = UUID.randomUUID().toString();
    private static final String PUBLIC_STAGING_ENFORCEMENT_ENFORCE_FINANCIAL_IMPOSITION_ACKNOWLEDGEMENT = "private.stagingenforcement.enforce-financial-imposition-acknowledgement";
    private static final String PRIVATE_APPLY_ENFORCEMENT_ACKNOWLEDGEMENT = "hearing.command.apply-enforcement-acknowledgement";
    private static final String PRIVATE_ENFORCEMENT_ACKNOWLEDGEMENT_ERROR = "hearing.command.enforcement-acknowledgement-error";

    @Mock
    private Sender sender;

    @Spy
    private Enveloper enveloper = createEnveloper();

    @Captor
    private ArgumentCaptor<JsonEnvelope> senderJsonEnvelopeCaptor;

    @InjectMocks
    private StagingEnforcementEventProcessor testObj;

    @Test
    public void testProcessAcknowledgementSuccess() {

        //Given
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID(PUBLIC_STAGING_ENFORCEMENT_ENFORCE_FINANCIAL_IMPOSITION_ACKNOWLEDGEMENT),
                buildEnforceFinancialImpositionAcknowledgmentEventWithAccount(COURTS));

        //When
        testObj.processAcknowledgement(event);

        //Then
        verify(sender).sendAsAdmin(senderJsonEnvelopeCaptor.capture());

        assertThat(senderJsonEnvelopeCaptor.getValue(), is(jsonEnvelope(withMetadataEnvelopedFrom(event).withName(PRIVATE_APPLY_ENFORCEMENT_ACKNOWLEDGEMENT),
                payloadIsJson(allOf(
                        withJsonPath("$.originator", equalTo(COURTS)),
                        withJsonPath("$.requestId", equalTo(ARBITRARY_REQUEST_ID)),
                        withJsonPath("$.acknowledgement.accountNumber", equalTo(ARBITRARY_ACCOUNT_NUMBER))
                )))
        ));
    }

    @Test
    public void testProcessAcknowledgement_shouldNotProcessEvent_whenMessageOriginatorIsNotCourts() {
        //Given
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID(PUBLIC_STAGING_ENFORCEMENT_ENFORCE_FINANCIAL_IMPOSITION_ACKNOWLEDGEMENT),
                buildEnforceFinancialImpositionAcknowledgmentEventWithAccount(ATCM));
        //When
        testObj.processAcknowledgement(event);

        //Then
        verify(sender, times(0)).sendAsAdmin(anyObject());
    }


    @Test
    public void testProcessAcknowledgement_shouldNotProcessEvent_whenMessageHasError() {
        //Given
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID(PUBLIC_STAGING_ENFORCEMENT_ENFORCE_FINANCIAL_IMPOSITION_ACKNOWLEDGEMENT),
                buildEnforceFinancialImpositionAcknowledgmentEventWithError(COURTS));

        //When
        testObj.processAcknowledgement(event);

        //Then
        verify(sender).sendAsAdmin(senderJsonEnvelopeCaptor.capture());

        System.out.println(senderJsonEnvelopeCaptor.getValue());

        assertThat(senderJsonEnvelopeCaptor.getValue(), is(jsonEnvelope(withMetadataEnvelopedFrom(event).withName(PRIVATE_ENFORCEMENT_ACKNOWLEDGEMENT_ERROR),
                payloadIsJson(allOf(
                        withJsonPath("$.originator", equalTo(COURTS)),
                        withJsonPath("$.requestId", equalTo(ARBITRARY_REQUEST_ID))
                )))));

        assertThat(senderJsonEnvelopeCaptor.getValue(), is(jsonEnvelope(withMetadataEnvelopedFrom(event).withName(PRIVATE_ENFORCEMENT_ACKNOWLEDGEMENT_ERROR),
                payloadIsJson(allOf(
                        withJsonPath("$.originator", equalTo(COURTS)),
                        withJsonPath("$.requestId", equalTo(ARBITRARY_REQUEST_ID))
                )))
        ));
    }


    private JsonObject buildEnforceFinancialImpositionAcknowledgmentEventWithAccount(final String originator) {
        return createObjectBuilder()
                .add("originator", originator)
                .add("requestId", ARBITRARY_REQUEST_ID)
                .add("acknowledgement",
                        createObjectBuilder()
                                .add("accountNumber", ARBITRARY_ACCOUNT_NUMBER)
                                .build())
                .build();
    }


    private JsonObject buildEnforceFinancialImpositionAcknowledgmentEventWithError(final String originator) {
        return createObjectBuilder()
                .add("originator", originator)
                .add("requestId", ARBITRARY_REQUEST_ID)
                .add("acknowledgement",
                        createObjectBuilder()
                                .add("errorCode", STRING.next())
                                .add("errorMessage", STRING.next())
                                .build())
                .build();
    }
}