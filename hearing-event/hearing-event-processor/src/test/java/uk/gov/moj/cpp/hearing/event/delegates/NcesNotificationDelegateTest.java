package uk.gov.moj.cpp.hearing.event.delegates;

import static java.util.UUID.fromString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.core.courts.notification.EmailChannel;
import uk.gov.justice.core.courts.nowdocument.DefendantCaseOffence;
import uk.gov.justice.core.courts.nowdocument.Result;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nows.mapper.FileAsStringReader;
import uk.gov.moj.cpp.hearing.nces.Defendant;
import uk.gov.moj.cpp.hearing.nces.DocumentContent;
import uk.gov.moj.cpp.hearing.nces.FinancialOrderForDefendant;
import uk.gov.moj.cpp.hearing.nces.UpdateDefendantWithFinancialOrderDetails;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class NcesNotificationDelegateTest {

    private static final String COMMAND_NAME = "hearing.command.update-defendant-with-financial-order";
    private static final UUID EMAIL_NOTIF_TEMPLATE_ID = fromString("e1da43dc-da04-4adb-b6ff-47748749932a");

    private static final FileAsStringReader fileAsStringReader = new FileAsStringReader();

    @Spy
    private final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();

    @Spy
    @InjectMocks
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter();

    @Spy
    @InjectMocks
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();

    @InjectMocks
    private StringToJsonObjectConverter stringToJsonObjectConverter;

    @Mock
    Sender sender;

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @InjectMocks
    NcesNotificationDelegate ncesNotificationDelegateUnderTheTest;

    @Captor
    private ArgumentCaptor<JsonEnvelope> updateDefendantWithFinancialOrderDetailsArgumentCaptor;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void should_transform_to_update_defendant_with_financial_order_details() throws IOException {

        //given
        final NowsRequested nowsRequested = getNowsRequestedPayload();
        JsonEnvelope event = generateEvent(nowsRequested);

        //when
        ncesNotificationDelegateUnderTheTest.updateDefendantWithFinancialOrder(sender, event, nowsRequested, nowsRequested.getCreateNowsRequest().getNows().get(0).getId());

        //then
        verify(sender, times(1))
                .sendAsAdmin(updateDefendantWithFinancialOrderDetailsArgumentCaptor.capture());
        UpdateDefendantWithFinancialOrderDetails updateDefendantWithFinancialOrderDetails = getUpdateDefendantWithFinancialOrderDetails();

        assertNotNull(updateDefendantWithFinancialOrderDetails);

        FinancialOrderForDefendant financialOrderForDefendant = updateDefendantWithFinancialOrderDetails.getFinancialOrderForDefendant();
        assertThat(fromString("175cd8b0-2963-4e72-aafd-2e2b9af2f41a"), is(financialOrderForDefendant.getCaseId()));
        assertThat(fromString("5275f609-dadd-4630-9ca0-e54868fd8afa"), is(financialOrderForDefendant.getDefendantId()));
        assertThat(fromString("10316f3a-000d-470f-a67a-2ec8f9598ebf"), is(financialOrderForDefendant.getHearingId()));

        List<UUID> resultDefinitionIds = financialOrderForDefendant.getResultDefinitionIds();
        assertThat(2, is(resultDefinitionIds.size()));
        assertThat(fromString("969f150c-cd05-46b0-9dd9-30891efcc766"), is(resultDefinitionIds.get(0)));
        assertThat(fromString("ae89b99c-e0e3-47b5-b218-24d4fca3ca53"), is(resultDefinitionIds.get(1)));


        EmailChannel emailChannel = financialOrderForDefendant.getEmailNotifications().get(0);
        checkEmailChannel(emailChannel);

        checkDocument(financialOrderForDefendant.getDocumentContent());
    }

    private JsonEnvelope generateEvent(NowsRequested nowsRequested) {
        return envelopeFrom(metadataWithRandomUUID(COMMAND_NAME),
                objectToJsonObjectConverter.convert(nowsRequested));
    }

    private NowsRequested getNowsRequestedPayload() throws IOException {
        String payload = payload("/data/hearing.events.nows-requested_sample.json");

        return mapper.readValue(payload, NowsRequested.class);
    }

    private void checkDocument(DocumentContent documentContent) {
        assertThat("", is(documentContent.getAdjustmentDetails()));
        assertThat("92", is(documentContent.getDivisionCode()));
        assertThat("19000091T", is(documentContent.getGobAccountNumber()));
        assertThat("Portsmouth Magistrates' Court", is(documentContent.getCourtCentreName()));
        assertThat("NCES Notification", is(documentContent.getAmendmentType()));
        assertThat("TFL0551900", is(documentContent.getUrn()));

        checkDefendant(documentContent.getDefendant());

        DefendantCaseOffence defendantCaseOffence = documentContent.getDefendantCaseOffences().get(0);
        checkDefendantCaseOffences(defendantCaseOffence);
        checkResults(defendantCaseOffence.getResults());
    }

    private void checkDefendant(Defendant defendant) {
        assertThat("Lia Humberto Cronin", is(defendant.getName()));
        assertThat(LocalDate.of(1994, 10, 05), is(defendant.getDateOfBirth()));
    }

    private void checkEmailChannel(EmailChannel emailChannel) {
        assertThat("do-pooleenforcement@hmcts.gsi.gov.uk", is(emailChannel.getSendToAddress()));
        assertThat(EMAIL_NOTIF_TEMPLATE_ID, is(emailChannel.getTemplateId()));
    }

    private void checkDefendantCaseOffences(DefendantCaseOffence defendantCaseOffence) {
        assertThat("2019-10-04", is(defendantCaseOffence.getConvictionDate()));
        assertThat("2018-12-05", is(defendantCaseOffence.getStartDate()));
        assertThat("Wound / inflict grievous bodily harm without intent", is(defendantCaseOffence.getWording()));
    }

    private void checkResults(List<Result> results) {
        assertThat("Fine", is(results.get(0).getLabel()));
        assertThat("Amount of fine", is(results.get(0).getPrompts().get(0).getLabel()));
        assertThat("100", is(results.get(0).getPrompts().get(0).getValue()));

        assertThat("Compensation", is(results.get(1).getLabel()));
        assertThat("Amount of compensation", is(results.get(1).getPrompts().get(0).getLabel()));
        assertThat("2", is(results.get(1).getPrompts().get(0).getValue()));
    }

    private UpdateDefendantWithFinancialOrderDetails getUpdateDefendantWithFinancialOrderDetails() {
        return jsonObjectToObjectConverter.convert(updateDefendantWithFinancialOrderDetailsArgumentCaptor.getValue().payloadAsJsonObject(), UpdateDefendantWithFinancialOrderDetails.class);
    }

    private static String payload(final String fileName) {
        return fileAsStringReader.readFile(fileName);
    }
}
