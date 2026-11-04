package uk.gov.moj.cpp.hearing.query.view.service.ctl;

import static java.time.LocalDate.now;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;
import static uk.gov.justice.services.messaging.JsonObjects.createReader;

import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.query.view.service.ctl.model.PublicHoliday;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ReferenceDataServiceTest {
    private static final String ENGLAND_AND_WALES_DIVISION = "england-and-wales";
    private static final String JUDICIAL_ID = "7e2f843e-d639-40b3-8611-8015f3a13444";

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
    public void shouldReturnJudiciaryWithForenames() {
        final JsonEnvelope value = judiciaryResponseEnvelope("judiciaries.json");
        when(requester.request(any(), any(Class.class))).thenReturn(value);
        final String ids = "7e2f843e-d639-40b3-8611-8015f3a13444," +
                "7e2f843e-d639-40b3-8611-8015f3a13333," +
                "7e2f843e-d639-40b3-8611-8015f3a13334";

        final List<String> judiciaries = referenceDataService.getJudiciaryTitle(value, ids);

        assertEquals(Arrays.asList(
                "Recorder Mark J Ainsworth",
                "Recorder Richard James Adkinson Sf",
                "Mark J Ainsworth"
        ), judiciaries);
    }

    @Test
    public void shouldReturnJudiciaryWithForenamesWhenTitleJudicialPrefixAbsent() {
        final JsonEnvelope value = judiciaryResponseEnvelope("judiciariesv1.json");
        when(requester.request(any(), any(Class.class))).thenReturn(value);
        final String ids = "7e2f843e-d639-40b3-8611-8015f3a13444," +
                "7e2f843e-d639-40b3-8611-8015f3a13333," +
                "7e2f843e-d639-40b3-8611-8015f3a13334";

        final List<String> judiciaries = referenceDataService.getJudiciaryTitle(value, ids);

        assertEquals(Arrays.asList(
                "Mark J Ainsworth",
                "Recorder Richard James Adkinson Sf",
                "Mark J Ainsworth"
        ), judiciaries);
    }

    @Test
    public void shouldNotCallExternalApiWhenInputIsNullOrEmpty() {
        final JsonEnvelope value = judiciaryResponseEnvelope("judiciaries.json");

        final List<String> judiciariesWhenEmpty = referenceDataService.getJudiciaryTitle(value, "");
        final List<String> judiciariesWhenNull = referenceDataService.getJudiciaryTitle(value, null);

        assertEquals(0, judiciariesWhenEmpty.size());
        assertEquals(0, judiciariesWhenNull.size());
    }

    @Test
    public void shouldReturnEmptyWhenNoRecordMatched() {
        final JsonEnvelope value = judiciaryResponseEnvelope("judiciariesEmpty.json");
        when(requester.request(any(), any(Class.class))).thenReturn(value);

        final List<String> judiciaries = referenceDataService.getJudiciaryTitle(value, JUDICIAL_ID);

        assertEquals(0, judiciaries.size());
    }

    @Test
    public void shouldFormatNameWithPrefixForenamesSurnameAndSuffix() {
        final List<String> judiciaries = getJudiciaryTitleFor(judiciaryObject(
                "Recorder", "Richard James", "Adkinson", "Sf"));

        assertEquals(Collections.singletonList("Recorder Richard James Adkinson Sf"), judiciaries);
    }

    @Test
    public void shouldFormatNameWithPrefixForenamesAndSurname() {
        final List<String> judiciaries = getJudiciaryTitleFor(judiciaryObject(
                "HHJ", "Jane", "Smith", null));

        assertEquals(Collections.singletonList("HHJ Jane Smith"), judiciaries);
    }

    @Test
    public void shouldFormatNameWithForenamesSurnameAndSuffix() {
        final List<String> judiciaries = getJudiciaryTitleFor(judiciaryObject(
                null, "John", "Brown", "QC"));

        assertEquals(Collections.singletonList("John Brown QC"), judiciaries);
    }

    @Test
    public void shouldFormatNameWithForenamesAndSurnameOnly() {
        final List<String> judiciaries = getJudiciaryTitleFor(judiciaryObject(
                null, "Mark J", "Ainsworth", null));

        assertEquals(Collections.singletonList("Mark J Ainsworth"), judiciaries);
    }

    @Test
    public void shouldFormatNameWithSurnameOnlyWhenForenamesMissing() {
        final List<String> judiciaries = getJudiciaryTitleFor(judiciaryObject(
                null, null, "Ainsworth", null));

        assertEquals(Collections.singletonList("Ainsworth"), judiciaries);
    }

    @Test
    public void shouldFormatNameWithPrefixAndSurnameWhenForenamesBlank() {
        final List<String> judiciaries = getJudiciaryTitleFor(judiciaryObject(
                "Recorder", "", "Adkinson", "Sf"));

        assertEquals(Collections.singletonList("Recorder Adkinson Sf"), judiciaries);
    }

    @Test
    public void shouldIgnoreBlankPrefixAndSuffixWhenFormattingName() {
        final List<String> judiciaries = getJudiciaryTitleFor(judiciaryObject(
                "", "Mark J", "Ainsworth", ""));

        assertEquals(Collections.singletonList("Mark J Ainsworth"), judiciaries);
    }

    @Test
    public void shouldFormatNameWithPrefixAndForenamesWhenSurnameMissing() {
        final List<String> judiciaries = getJudiciaryTitleFor(judiciaryObject(
                "Recorder", "Richard James", null, null));

        assertEquals(Collections.singletonList("Recorder Richard James"), judiciaries);
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

    @Test
    public void shouldReturnNullIfApplicationTypeIsNotFound() {
        when(requester.requestAsAdmin(any(), any(Class.class))).thenReturn(envelopeFrom(
                metadataBuilder().
                        withName("referencedata.query.application-type").
                        withId(randomUUID()), createObjectBuilder().build()));

        boolean results = referenceDataService.isOffenceActiveOrder(UUID.fromString("72fab61d-e166-4e44-9a4e-046866511993"));

        assertFalse(results);
    }

    private List<String> getJudiciaryTitleFor(final JsonObject judiciary) {
        final JsonEnvelope judiciaryEnvelope = envelopeFrom(
                metadataBuilder()
                        .withName("referencedata.query.judiciaries")
                        .withId(randomUUID()),
                createObjectBuilder()
                        .add("judiciaries", javax.json.Json.createArrayBuilder().add(judiciary).build())
                        .build());

        when(requester.request(any(), any(Class.class))).thenReturn(judiciaryEnvelope);
        return referenceDataService.getJudiciaryTitle(judiciaryEnvelope, JUDICIAL_ID);
    }

    private JsonObject judiciaryObject(final String titleJudicialPrefix,
                                       final String forenames,
                                       final String surname,
                                       final String titleSuffix) {
        final javax.json.JsonObjectBuilder builder = createObjectBuilder().add("id", JUDICIAL_ID);
        if (titleJudicialPrefix != null) {
            builder.add("titleJudicialPrefix", titleJudicialPrefix);
        }
        if (forenames != null) {
            builder.add("forenames", forenames);
        }
        if (surname != null) {
            builder.add("surname", surname);
        }
        if (titleSuffix != null) {
            builder.add("titleSuffix", titleSuffix);
        }
        return builder.build();
    }

    private JsonEnvelope judiciaryResponseEnvelope(final String fileName) {
        return envelopeFrom(
                metadataBuilder().
                        withName("referencedata.query.judiciaries").
                        withId(randomUUID()),
                createReader(getClass().getClassLoader().
                        getResourceAsStream(fileName)).
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
}
