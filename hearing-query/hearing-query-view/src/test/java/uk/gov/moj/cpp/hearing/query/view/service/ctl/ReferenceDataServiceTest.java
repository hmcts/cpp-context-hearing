package uk.gov.moj.cpp.hearing.query.view.service.ctl;

import static java.time.LocalDate.now;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createReader;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;

import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.query.view.service.ctl.model.PublicHoliday;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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

    @Test
    public void shouldReturnJudiciary() {
        final JsonEnvelope value = JudiciaryResponseEnvelope();
        when(requester.request(any(), any(Class.class))).thenReturn(value);
        String ids = "7e2f843e-d639-40b3-8611-8015f3a13444," +
                "7e2f843e-d639-40b3-8611-8015f3a13333," +
                "7e2f843e-d639-40b3-8611-8015f3a13334";

        final List<String> judiciaries = referenceDataService.getJudiciaryTitle(value, ids);

        assertResult(judiciaries);
    }

    @Test
    public void shouldReturnJudiciaryV1() {
        final JsonEnvelope value = JudiciaryResponseEnvelopeV1();
        when(requester.request(any(), any(Class.class))).thenReturn(value);
        String ids = "7e2f843e-d639-40b3-8611-8015f3a13444," +
                "7e2f843e-d639-40b3-8611-8015f3a13333," +
                "7e2f843e-d639-40b3-8611-8015f3a13334";

        final List<String> judiciaries = referenceDataService.getJudiciaryTitle(value, ids);

        assertResultV1(judiciaries);
    }

    @Test
    public void shouldNotCallExternalApiWhenInputIsNullOrEmpty() {
        final JsonEnvelope value = JudiciaryResponseEnvelope();
        when(requester.request(any(JsonEnvelope.class), any(Class.class))).thenReturn(value);

        final List<String> judiciariesWhenEmpty = referenceDataService.getJudiciaryTitle(value, "");
        final List<String> judiciariesWhenNull = referenceDataService.getJudiciaryTitle(value, null);

        assertEquals(0, judiciariesWhenEmpty.size());
        assertEquals(0, judiciariesWhenNull.size());

    }

    @Test
    public void shouldReturnEmptyWhenNoRecordMatched() {
        final JsonEnvelope value = JudiciaryResponseEnvelopeWithEmptyJson();
        when(requester.request(any(), any(Class.class))).thenReturn(value);

        final List<String> judiciaries = referenceDataService.getJudiciaryTitle(value, "7e2f843e-d639-40b3-8611-8015f3a13444");

        assertEquals(0, judiciaries.size());
    }

    @Test
    public void shouldReturnTrueIfTypeOffenceActiveOrderIsOffence() {
        final JsonEnvelope value = getCourtApplicationType("court-application-type.json");
        when(requester.requestAsAdmin(any(), any(Class.class))).thenReturn(value);

        boolean results = referenceDataService.isOffenceActiveOrder(UUID.fromString("62fab61d-e166-4e44-9a4e-046866511993"));

        assertTrue(results);

    }

    @Test
    public void shouldReturnTrueIfTypeOffenceActiveOrderIsNonOffence() {
        final JsonEnvelope value = getCourtApplicationType("court-application-type-non-offence.json");
        when(requester.requestAsAdmin(any(), any(Class.class))).thenReturn(value);

        boolean results = referenceDataService.isOffenceActiveOrder(UUID.fromString("72fab61d-e166-4e44-9a4e-046866511993"));

        assertFalse(results);

    }

    private JsonEnvelope JudiciaryResponseEnvelope() {
        return envelopeFrom(
                metadataBuilder().
                        withName("referencedata.query.judiciaries").
                        withId(randomUUID()),
                createReader(getClass().getClassLoader().
                        getResourceAsStream("judiciaries.json")).
                        readObject()
        );
    }

    private JsonEnvelope JudiciaryResponseEnvelopeV1() {
        return envelopeFrom(
                metadataBuilder().
                        withName("referencedata.query.judiciaries").
                        withId(randomUUID()),
                createReader(getClass().getClassLoader().
                        getResourceAsStream("judiciariesv1.json")).
                        readObject()
        );
    }

    private JsonEnvelope JudiciaryResponseEnvelopeWithEmptyJson() {
        return envelopeFrom(
                metadataBuilder().
                        withName("referencedata.query.judiciaries").
                        withId(randomUUID()),
                createReader(getClass().getClassLoader().
                        getResourceAsStream("judiciariesEmpty.json")).
                        readObject()
        );
    }

    private JsonEnvelope getCourtApplicationType(String fileName) {
        return envelopeFrom(
                metadataBuilder().
                        withName("referencedata.query.application-type").
                        withId(randomUUID()),
                createReader(getClass().getClassLoader().
                        getResourceAsStream(fileName)).
                        readObject()
        );
    }


    private JsonEnvelope publicHolidaysResponseEnvelope() {
        return envelopeFrom(
                metadataBuilder().
                        withName("referencedata.query.public-holidays").
                        withId(randomUUID()),
                createReader(getClass().getClassLoader().
                        getResourceAsStream("public-holidays.json")).
                        readObject()
        );
    }

    private void assertResult(List<String> judiciaries) {
        List<String> expectedJudiciaries = Arrays.asList(
                "Recorder Adkinson Sf",
                "Recorder Ainsworth",
                "Ainsworth"
        );

        assertEquals(expectedJudiciaries.size(), judiciaries.size());
        assertTrue(judiciaries.containsAll(expectedJudiciaries));
    }


    private void assertResultV1(List<String> judiciaries) {
        List<String> expectedJudiciaries = Arrays.asList(
                "Ainsworth",
                "Recorder Adkinson Sf",
                "Ainsworth"
        );

        assertEquals(expectedJudiciaries.size(), judiciaries.size());
        assertTrue(judiciaries.containsAll(expectedJudiciaries));
    }
}
