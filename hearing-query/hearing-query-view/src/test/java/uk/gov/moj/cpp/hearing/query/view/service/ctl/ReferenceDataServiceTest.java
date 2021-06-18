package uk.gov.moj.cpp.hearing.query.view.service.ctl;

import static java.time.LocalDate.now;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createReader;
import static org.junit.Assert.assertEquals;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.query.view.service.ctl.model.PublicHoliday;

import java.time.LocalDate;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReferenceDataServiceTest {
    private static final String ENGLAND_AND_WALES_DIVISION = "england-and-wales";

    @Mock(answer = RETURNS_DEEP_STUBS)
    private Requester requester;

    @InjectMocks
    private ReferenceDataService referenceDataService;

    @Test
    public void shouldRequestCrackedInEffectiveTrialTypes() {
        final JsonEnvelope value = publicHolidaysResponseEnvelope();

        when(requester.requestAsAdmin(any(JsonEnvelope.class), any(Class.class))).thenReturn(value);

        final List<PublicHoliday> publicHolidays = referenceDataService.getPublicHolidays(ENGLAND_AND_WALES_DIVISION, now(), now().plusDays(1));

        assertEquals(5, publicHolidays.size());
    }


    private JsonEnvelope publicHolidaysResponseEnvelope() {
        final JsonEnvelope jsonEnvelope = envelopeFrom(
                metadataBuilder().
                        withName("referencedata.query.public-holidays").
                        withId(randomUUID()),
                createReader(getClass().getClassLoader().
                        getResourceAsStream("public-holidays.json")).
                        readObject()
        );
        return jsonEnvelope;
    }

}
