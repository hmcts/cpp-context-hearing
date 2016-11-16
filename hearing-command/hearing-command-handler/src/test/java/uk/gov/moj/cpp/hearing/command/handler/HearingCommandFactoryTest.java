package uk.gov.moj.cpp.hearing.command.handler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.NAME;

import java.time.LocalDate;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Test;

import uk.gov.justice.services.messaging.DefaultJsonEnvelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.moj.cpp.hearing.domain.HearingTypeEnum;
import uk.gov.moj.cpp.hearing.domain.command.ListHearing;
import uk.gov.moj.cpp.hearing.domain.command.VacateHearing;

public class HearingCommandFactoryTest {

	private static final String LIST_HEARING_COMMAND = "hearing.command.list-hearing";
	private static final String VACATE_HEARING_COMMAND = "hearing.command.list-hearing";
	private static final UUID HEARING_ID = UUID.randomUUID();
	private static final UUID CASE_ID = UUID.randomUUID();
	private static final String CENTRE_NAME = "Liverpool";
	public static final String HEARING_TYPE = "PTP";
	public static final LocalDate DATE_OF_SENDING = LocalDate.now();
	public static final int DURATION = 2;

	@Test
	public void shouldReturnHearingListedEventForPTP() throws Exception {
		Object object = new HearingCommandFactory()
				.getListHearing(createListHearingCommand(HearingTypeEnum.PTP));

		assertThat(object, instanceOf(ListHearing.class));
		ListHearing event = (ListHearing) object;
		assertThat(event.getCaseId(), equalTo(CASE_ID));
		assertThat(event.getStartDateOfHearing(), equalTo(DATE_OF_SENDING));

	}
	
	@Test
	public void shouldReturnHearingListedEventForTrail() throws Exception {
		Object object = new HearingCommandFactory()
				.getListHearing(createListHearingCommand(HearingTypeEnum.TRIAL));

		assertThat(object, instanceOf(ListHearing.class));
		ListHearing event = (ListHearing) object;
		assertThat(event.getCaseId(), equalTo(CASE_ID));
		assertThat(event.getStartDateOfHearing(), equalTo(DATE_OF_SENDING));

	}
	
	@Test
	public void shouldReturnHearingListedEventForOtherHearingType() throws Exception {
		Object object = new HearingCommandFactory()
				.getListHearing(createListHearingCommand(HearingTypeEnum.SENTENCE));

		assertThat(object, instanceOf(ListHearing.class));
		ListHearing event = (ListHearing) object;
		assertThat(event.getCaseId(), equalTo(CASE_ID));
		assertThat(event.getStartDateOfHearing(), equalTo(DATE_OF_SENDING));

	}

	@Test
	public void shouldReturnHearingVacatedEvent() throws Exception {
		Object object = new HearingCommandFactory().getVacateHearing(createVacateHearingCommand());

		assertThat(object, instanceOf(VacateHearing.class));
		VacateHearing event = (VacateHearing) object;
		assertThat(event.getHearingId(), equalTo(HEARING_ID));

	}

	private JsonEnvelope createListHearingCommand(HearingTypeEnum hearingType) {
		final JsonObject metadataAsJsonObject = Json.createObjectBuilder().add(ID, UUID.randomUUID().toString())
				.add(NAME, LIST_HEARING_COMMAND).build();

		final JsonObject payloadAsJsonObject = Json.createObjectBuilder().add("hearingId", HEARING_ID.toString())
				.add("courtCentreName", CENTRE_NAME).add("hearingType", hearingType.getValue())
				.add("dateOfSending", DATE_OF_SENDING.toString()).add("duration", DURATION)
				.add("caseId", CASE_ID.toString()).build();

		return DefaultJsonEnvelope.envelopeFrom(JsonObjectMetadata.metadataFrom(metadataAsJsonObject),
				payloadAsJsonObject);

	}

	private JsonEnvelope createVacateHearingCommand() {
		final JsonObject metadataAsJsonObject = Json.createObjectBuilder().add(ID, UUID.randomUUID().toString())
				.add(NAME, VACATE_HEARING_COMMAND).build();

		final JsonObject payloadAsJsonObject = Json.createObjectBuilder().add("hearingId", HEARING_ID.toString())
				.build();

		return DefaultJsonEnvelope.envelopeFrom(JsonObjectMetadata.metadataFrom(metadataAsJsonObject),
				payloadAsJsonObject);

	}
}