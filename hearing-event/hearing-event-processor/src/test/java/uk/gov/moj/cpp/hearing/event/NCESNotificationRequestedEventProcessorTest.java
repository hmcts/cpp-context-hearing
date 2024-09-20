package uk.gov.moj.cpp.hearing.event;

import javax.json.JsonObject;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.core.courts.notification.EmailChannel;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.spi.DefaultEnvelope;
import uk.gov.moj.cpp.hearing.nces.DefendantUpdateWithFinancialOrderDetails;
import uk.gov.moj.cpp.hearing.nces.DocumentContent;
import uk.gov.moj.cpp.hearing.nces.FinancialOrderForDefendant;
import uk.gov.moj.cpp.hearing.nces.NCESNotificationRequested;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"squid:S2699"})
public class NCESNotificationRequestedEventProcessorTest {

    @InjectMocks
    private NCESNotificationRequestedEventProcessor testObj;

    @Mock
    private Sender sender;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Captor
    private ArgumentCaptor<DefaultEnvelope> senderJsonEnvelopeCaptor;

	@Test
	@BeforeEach
	public void setup() {
		setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
		setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
	}

	@Test
    public void publishNcesNotificationRequestedPublicEvent() {

        JsonEnvelope event = mock(JsonEnvelope.class);
        JsonObject jsonObject = mock(JsonObject.class);
        DefendantUpdateWithFinancialOrderDetails defendantNcesNotification = mock(DefendantUpdateWithFinancialOrderDetails.class);
		UUID caseId = UUID.randomUUID();
		UUID defendantId =  UUID.randomUUID();

		UUID hearingId = UUID.randomUUID();
		UUID materialId = UUID.randomUUID();
		List<EmailChannel> emailNotifications = new ArrayList<>();
		UUID templateId = UUID.randomUUID();

		EmailChannel emailChannel = EmailChannel.emailChannel().withTemplateId(templateId).build();

		emailNotifications.add(emailChannel);

		Map<String, Object> additionalProperties = new HashMap<>();
		additionalProperties.put("Ali","ALi");

		FinancialOrderForDefendant publicFinancialOrderForDefendant= FinancialOrderForDefendant.newBuilder()
				.withCaseId(caseId)
				.withDefendantId(defendantId)
				.withDocumentContent(DocumentContent.documentContent().build())
				.withEmailNotifications(emailNotifications)
				.withHearingId(hearingId)
				.withMaterialId(materialId)
				.withAdditionalProperties(additionalProperties).build();
		NCESNotificationRequested ncesNotificationRequested = NCESNotificationRequested.newBuilder().withFinancialOrderForDefendant(publicFinancialOrderForDefendant).build();

		final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.event.nces-notification-requested"),
				objectToJsonObjectConverter.convert(ncesNotificationRequested));
        //when
        testObj.publishNcesNotificationRequestedPublicEvent(envelope);

        // verify
        verify(sender).send(senderJsonEnvelopeCaptor.capture());

		DefaultEnvelope value = senderJsonEnvelopeCaptor.getValue();
		assertThat(value.metadata().name(),is("public.hearing.event.nces-notification-requested"));
		JsonObject convert = objectToJsonObjectConverter.convert(value.payload());
		assertThat(convert.getJsonString("caseId").getString(), is(publicFinancialOrderForDefendant.getCaseId().toString()));
		assertThat(convert.getJsonString("defendantId").getString(), is(publicFinancialOrderForDefendant.getDefendantId().toString()));
		assertThat(convert.getJsonString("hearingId").getString(), is(publicFinancialOrderForDefendant.getHearingId().toString()));
		assertThat(convert.getJsonString("materialId").getString(), is(publicFinancialOrderForDefendant.getMaterialId().toString()));
		String actualTemplate = ((JsonObject) convert.getJsonArray("emailNotifications").get(0)).getJsonString("templateId").getString();
		assertThat(actualTemplate, is(publicFinancialOrderForDefendant.getEmailNotifications().get(0).getTemplateId().toString()));

	}


}
