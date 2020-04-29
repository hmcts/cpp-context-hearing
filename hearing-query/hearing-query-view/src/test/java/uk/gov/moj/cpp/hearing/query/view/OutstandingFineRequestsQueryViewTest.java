package uk.gov.moj.cpp.hearing.query.view;

import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.DefendantOutstandingFineRequests;
import uk.gov.moj.cpp.hearing.domain.DefendantOutstandingFineRequestsResult;
import uk.gov.moj.cpp.hearing.query.view.service.OutstandingFineRequestsService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import javax.inject.Inject;
import javax.persistence.NoResultException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OutstandingFineRequestsQueryViewTest {

    private static final String FIELD_HEARING_DATE = "hearingDate";
    @Mock
    private OutstandingFineRequestsService outstandingFineRequestsService;

    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Spy
    private final ObjectToJsonValueConverter objectToJsonValueConverter = new ObjectToJsonValueConverter(objectMapper);

    @InjectMocks
    private OutstandingFineRequestsQueryView outstandingFineRequestsQueryView;

    @Test
    public void should_send_an_empty_payload_when_no_result() {

        final LocalDate hearingDate = LocalDate.now();

        when(outstandingFineRequestsService.getDefendantOutstandingFineRequestsByHearingDate(hearingDate)).thenThrow(NoResultException.class);

        final JsonEnvelope query = envelopeFrom(metadataWithRandomUUID("hearing.defendant.info"),
                createObjectBuilder()
                        .add(FIELD_HEARING_DATE, hearingDate.toString())
                        .build());

        final JsonEnvelope result = outstandingFineRequestsQueryView.getDefendantOutstandingFineRequests(query);

        assertThat(result.metadata().name(), is("hearing.defendant.info"));
        assertTrue(result.payloadAsJsonObject().isEmpty());
    }

    @Test
    public void should_send_payload_when_defendant_found_with_defendantDetails() {

        final LocalDate hearingDate = LocalDate.now();

        when(outstandingFineRequestsService.getDefendantOutstandingFineRequestsByHearingDate(hearingDate)).thenReturn(createDefendantRequestProfile());

        final JsonEnvelope query = envelopeFrom(metadataWithRandomUUID("hearing.defendant.info"),
                createObjectBuilder()
                        .add(FIELD_HEARING_DATE, hearingDate.toString())
                        .build());

        final JsonEnvelope result = outstandingFineRequestsQueryView.getDefendantOutstandingFineRequests(query);
        assertThat(result.metadata().name(), is("hearing.defendant.info"));
        assertTrue(result.payloadAsJsonObject().getJsonArray("defendantDetails").size() == 3);

    }


    private DefendantOutstandingFineRequestsResult createDefendantRequestProfile() {
        final DefendantOutstandingFineRequestsResult defendantInfoQueryResult = new DefendantOutstandingFineRequestsResult(
                Arrays.asList(
                        DefendantOutstandingFineRequests.newBuilder().withDefendantId(UUID.randomUUID()).withDateOfBirth("1980-06-25 00:00:00").withFirstName("Mr").withLastName("Brown").build(),
                        DefendantOutstandingFineRequests.newBuilder().withDefendantId(UUID.randomUUID()).withFirstName("Mrs").withLastName("Brown").withNationalInsuranceNumber("AB123456Z").build(),
                        DefendantOutstandingFineRequests.newBuilder().withDefendantId(UUID.randomUUID()).withLegalEntityDefendantName("ACME").build()
                )
        );

        return defendantInfoQueryResult;
    }

}