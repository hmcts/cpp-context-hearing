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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.spi.DefaultJsonMetadata.metadataBuilder;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishStatus.EXPORT_SUCCESSFUL;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus.currentCourtStatus;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;
import uk.gov.moj.cpp.hearing.query.view.service.HearingService;
import uk.gov.moj.cpp.hearing.repository.CourtListPublishStatusResult;
import uk.gov.moj.cpp.hearing.repository.CourtListRepository;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingQueryViewTest {

    private static final String COURT_CENTRE_QUERY_PARAMETER = "courtCentreId";
    private static final String COURT_CENTRE_IDS_QUERY_PARAMETER = "courtCentreIds";
    private static final String LAST_MODIFIED_TIME = "dateOfHearing";

    @Spy
    private Enveloper enveloper = createEnveloper();

    @Mock
    private CourtListRepository courtListRepository;

    @Mock
    private HearingService hearingService;

    @InjectMocks
    private HearingQueryView hearingQueryView;

    public static final UUID COURT_CENTRE_ID = randomUUID();


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

        final String courtCentreIdStr = courtCentreId1 + ","+ courtCentreId2;

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

        final String courtCentreIdStr = courtCentreId1 + ","+ courtCentreId2;
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

    private Optional<CourtListPublishStatusResult> publishCourtListStatuses() {
        final UUID courtCentreId = randomUUID();
        final CourtListPublishStatusResult publishCourtListStatus = new CourtListPublishStatusResult(courtCentreId, now(), EXPORT_SUCCESSFUL);
        return of(publishCourtListStatus);
    }

}