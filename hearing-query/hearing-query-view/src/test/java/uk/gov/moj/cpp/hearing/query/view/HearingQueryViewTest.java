package uk.gov.moj.cpp.hearing.query.view;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.time.ZonedDateTime.now;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.spi.DefaultJsonMetadata.metadataBuilder;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishStatus.EXPORT_SUCCESSFUL;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus.currentCourtStatus;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.CourtRoom;
import uk.gov.moj.cpp.hearing.domain.DefendantDetail;
import uk.gov.moj.cpp.hearing.domain.DefendantInfoQueryResult;
import uk.gov.moj.cpp.hearing.dto.DefendantSearch;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;
import uk.gov.moj.cpp.hearing.query.view.service.HearingService;
import uk.gov.moj.cpp.hearing.repository.CourtListPublishStatusResult;
import uk.gov.moj.cpp.hearing.repository.CourtListRepository;
import uk.gov.moj.cpp.hearing.repository.DefendantRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.NoResultException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingQueryViewTest {

    public static final UUID COURT_CENTRE_ID = randomUUID();
    private static final String FIELD_DEFENDANT_ID = "defendantId";
    private static final String FIELD_COURTCENTRE_ID = "courtCentreId";
    private static final String FIELD_COURTROOM_IDS = "courtRoomIds";
    private static final String FIELD_HEARING_DATE = "hearingDate";
    private static final String COURT_CENTRE_QUERY_PARAMETER = "courtCentreId";
    private static final String COURT_CENTRE_IDS_QUERY_PARAMETER = "courtCentreIds";
    private static final String LAST_MODIFIED_TIME = "dateOfHearing";
    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
    @InjectMocks
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter();
    @Spy
    @InjectMocks
    private final ObjectToJsonValueConverter objectToJsonValueConverter = new ObjectToJsonValueConverter(objectMapper);
    @Spy
    private Enveloper enveloper = createEnveloper();
    @Mock
    private CourtListRepository courtListRepository;
    @Mock
    private DefendantRepository defendantRepository;
    @Mock
    private HearingService hearingService;
    @InjectMocks
    private HearingQueryView hearingQueryView;

    @Test
    public void shouldReturnCorrectPublishCourtListStatus() {

        when(courtListRepository.courtListPublishStatuses(COURT_CENTRE_ID))
                .thenReturn(publishCourtListStatuses());

        final JsonEnvelope query = envelopeFrom(
                metadataBuilder().withId(randomUUID()).withName("hearing.court.list.publish.status"),
                createObjectBuilder()
                        .add(COURT_CENTRE_QUERY_PARAMETER, COURT_CENTRE_ID.toString())
                        .build());

        final JsonEnvelope results = hearingQueryView.getCourtListPublishStatus(query);
        assertThat(results, is(jsonEnvelope(withMetadataEnvelopedFrom(query).withName("hearing.court.list.publish.status"), payloadIsJson(
                allOf(
                        withJsonPath("$.publishCourtListStatus.publishStatus", equalTo(EXPORT_SUCCESSFUL.name()))
                )))));

    }

    @Test
    public void shouldGetLatestHearingsByCourtCentres() {
        final String testPageName = "testPageName";

        final String courtCentreId1 = "ebdaeb99-8952-4c07-99c4-d27c39d3e63a";
        final String courtCentreId2 = "c0a03dfd-f6f2-4590-a026-17f1cf5268e1";

        final String courtCentreIdStr = courtCentreId1 + "," + courtCentreId2;

        final List<UUID> courtCentreIds = new ArrayList();
        courtCentreIds.add(fromString(courtCentreId1));
        courtCentreIds.add(fromString(courtCentreId2));

        final Optional<CurrentCourtStatus> currentCourtStatus = of(currentCourtStatus()
                .withPageName(testPageName)
                .build());

        final LocalDate now = LocalDate.now();
        when(hearingService.getHearingsForWebPage(courtCentreIds, now)).thenReturn(currentCourtStatus);

        final JsonEnvelope query = envelopeFrom(
                metadataBuilder().withId(randomUUID()).withName("hearing.latest-hearings-by-court-centres"),
                createObjectBuilder()
                        .add(COURT_CENTRE_IDS_QUERY_PARAMETER, courtCentreIdStr)
                        .add(LAST_MODIFIED_TIME, now.toString())
                        .build());


        final JsonEnvelope results = hearingQueryView.getLatestHearingsByCourtCentres(query);

        assertThat(results.metadata().name(), is("hearing.get-latest-hearings-by-court-centres"));
        assertThat(results.payloadAsJsonObject().getString("pageName"), is(testPageName));
    }


    @Test
    public void shouldReturnEmptyResult() {
        final String courtCentreId1 = "ebdaeb99-8952-4c07-99c4-d27c39d3e63a";
        final String courtCentreId2 = "c0a03dfd-f6f2-4590-a026-17f1cf5268e1";

        final String courtCentreIdStr = courtCentreId1 + "," + courtCentreId2;
        final Optional<CurrentCourtStatus> currentCourtStatus = empty();

        final List<UUID> courtCentreIds = new ArrayList();
        courtCentreIds.add(fromString(courtCentreId1));
        courtCentreIds.add(fromString(courtCentreId2));

        final LocalDate now = LocalDate.now();
        when(hearingService.getHearingsForWebPage(courtCentreIds, now)).thenReturn(currentCourtStatus);

        final JsonEnvelope query = envelopeFrom(
                metadataBuilder().withId(randomUUID()).withName("hearing.get-latest-hearings-by-court-centres"),
                createObjectBuilder()
                        .add(COURT_CENTRE_IDS_QUERY_PARAMETER, courtCentreIdStr)
                        .add(LAST_MODIFIED_TIME, now.toString())
                        .build());


        final JsonEnvelope results = hearingQueryView.getLatestHearingsByCourtCentres(query);

        assertThat(results.metadata().name(), is("hearing.get-latest-hearings-by-court-centres"));
        assertTrue(results.payloadAsJsonObject().isEmpty());
    }

    @Test
    public void should_send_payload_when_defendant_exists() {
        final Optional<UUID> anExistingDefendantId = Optional.of(UUID.randomUUID());

        when(defendantRepository.getDefendantDetailsForSearching(anExistingDefendantId.get())).thenReturn(createDefendantSearch());
        final JsonEnvelope query = envelopeFrom(
                metadataWithRandomUUID("hearing.defendant.outstanding-fines"),
                createObjectBuilder()
                        .add(FIELD_DEFENDANT_ID, anExistingDefendantId.get().toString())
                        .build());
        final JsonEnvelope outstandingFromDefendantIdEnvelope = hearingQueryView.getOutstandingFromDefendantId(query);

        assertThat(outstandingFromDefendantIdEnvelope.metadata().name(), is("hearing.defendant.outstanding-fines"));

        final DefendantSearch actualDefendantSearch = jsonObjectToObjectConverter.convert(outstandingFromDefendantIdEnvelope.payloadAsJsonObject(), DefendantSearch.class);
        assertThat(actualDefendantSearch.getForename(), is("Tony"));
        assertThat(actualDefendantSearch.getSurname(), is("Stark"));
        assertThat(actualDefendantSearch.getNationalInsuranceNumber(), is("12345"));
        assertThat(actualDefendantSearch.getDateOfBirth(), is(LocalDate.of(1985, 6, 1)));
    }


    @Test
    public void should_send_an_empty_payload_when_defendant_does_not_exists() {
        final Optional<UUID> unknownDefendantId = Optional.of(UUID.randomUUID());

        when(defendantRepository.getDefendantDetailsForSearching(unknownDefendantId.get())).thenThrow(NoResultException.class);
        final JsonEnvelope query = envelopeFrom(
                metadataWithRandomUUID("hearing.defendant.outstanding-fines"),
                createObjectBuilder()
                        .add(FIELD_DEFENDANT_ID, unknownDefendantId.get().toString())
                        .build());
        final JsonEnvelope outstandingFromDefendantIdEnvelope = hearingQueryView.getOutstandingFromDefendantId(query);

        assertThat(outstandingFromDefendantIdEnvelope.metadata().name(), is("hearing.defendant.outstanding-fines"));

        assertTrue(outstandingFromDefendantIdEnvelope.payloadAsJsonObject().isEmpty());
    }

    @Test
    public void should_send_an_empty_payload_when_no_result_from_courtroom() {

        final UUID courtCentreId = UUID.randomUUID();
        final List<UUID> courtRoomIds = Arrays.asList(new UUID[]{UUID.randomUUID(), UUID.randomUUID()});
        final LocalDate hearingDate = LocalDate.now();

        when(hearingService.getHearingsByCourtRoomList(hearingDate, courtCentreId, courtRoomIds)).thenThrow(NoResultException.class);

        final JsonEnvelope query = envelopeFrom(metadataWithRandomUUID("hearing.defendant.info"),
                createObjectBuilder()
                        .add(FIELD_COURTCENTRE_ID, courtCentreId.toString())
                        .add(FIELD_COURTROOM_IDS, courtRoomIds.stream().map(e -> e.toString()).collect(Collectors.joining(",")))
                        .add(FIELD_HEARING_DATE, hearingDate.toString())
                        .build());

        final JsonEnvelope result = hearingQueryView.getDefendantInfoFromCourtHouseId(query);

        assertThat(result.metadata().name(), is("hearing.defendant.info"));
        assertTrue(result.payloadAsJsonObject().isEmpty());
    }

    @Test
    public void should_send_payload_when_defendant_found_with_courtroom() {

        final LocalDate hearingDate = LocalDate.now();
        final UUID courtHouseId = UUID.randomUUID();
        final UUID roomId1 = UUID.randomUUID();
        final UUID roomId2 = UUID.randomUUID();

        when(hearingService.getHearingsByCourtRoomList(hearingDate, courtHouseId, Arrays.asList(roomId1, roomId2))).thenReturn(createDefendantInfo());

        final JsonEnvelope query = envelopeFrom(metadataWithRandomUUID("hearing.defendant.info"),
                createObjectBuilder()
                        .add(FIELD_COURTCENTRE_ID, courtHouseId.toString())
                        .add(FIELD_COURTROOM_IDS, roomId1 + "," + roomId2)
                        .add(FIELD_HEARING_DATE, hearingDate.toString())
                        .build());

        final JsonEnvelope result = hearingQueryView.getDefendantInfoFromCourtHouseId(query);
        assertThat(result.metadata().name(), is("hearing.defendant.info"));
        assertTrue(result.payloadAsJsonObject().getJsonArray("courtRooms").size() == 1);
        assertTrue(result.payloadAsJsonObject().getJsonArray("courtRooms").getJsonObject(0).getString("courtRoomName").equalsIgnoreCase("Room-1"));

    }

    private DefendantSearch createDefendantSearch() {
        final DefendantSearch defendantSearch = new DefendantSearch();
        defendantSearch.setSurname("Stark");
        defendantSearch.setNationalInsuranceNumber("12345");
        defendantSearch.setForename("Tony");
        defendantSearch.setDateOfBirth(LocalDate.of(1985, 6, 1));

        return defendantSearch;
    }

    private DefendantInfoQueryResult createDefendantInfo() {
        final DefendantInfoQueryResult defendantInfoQueryResult = new DefendantInfoQueryResult();
        defendantInfoQueryResult.getCourtRooms().add(
                CourtRoom.courtRoom().withDefendantDetails(
                        Arrays.asList(
                                DefendantDetail.defendantDetail().withDefendantId(UUID.randomUUID()).withDateOfBirth("1980-06-25 00:00:00").withFirstName("Mr").withLastName("Brown").build(),
                                DefendantDetail.defendantDetail().withDefendantId(UUID.randomUUID()).withFirstName("Mrs").withLastName("Brown").withNationalInsuranceNumber("AB123456Z").build(),
                                DefendantDetail.defendantDetail().withDefendantId(UUID.randomUUID()).withLegalEntityOrganizationName("ACME").build()
                        )
                )
                        .withCourtRoomName("Room-1")
                        .build()
        );

        return defendantInfoQueryResult;
    }

    private Optional<CourtListPublishStatusResult> publishCourtListStatuses() {
        final UUID courtCentreId = randomUUID();
        final CourtListPublishStatusResult publishCourtListStatus = new CourtListPublishStatusResult(courtCentreId, now(), EXPORT_SUCCESSFUL);
        return of(publishCourtListStatus);
    }
}