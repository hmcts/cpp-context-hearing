package uk.gov.moj.cpp.hearing.query.view;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.time.ZonedDateTime.now;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.spi.DefaultJsonMetadata.metadataBuilder;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishStatus.EXPORT_SUCCESSFUL;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingListXhibitResponse;
import uk.gov.moj.cpp.hearing.query.view.service.HearingService;
import uk.gov.moj.cpp.hearing.repository.CourtListPublishStatusResult;
import uk.gov.moj.cpp.hearing.repository.CourtListRepository;

import java.time.ZonedDateTime;
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
    private static final String LAST_MODIFIED_TIME = "lastModifiedTime";

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
    public void shouldGetHearingsByCourtCentre() {
        final ZonedDateTime lastModifiedTime = ZonedDateTime.parse("2016-11-12T09:27:12Z");
        final UUID courtCentreId = randomUUID();
        final HearingListXhibitResponse hearingListXhibitResponse = new HearingListXhibitResponse(courtCentreId);

        when(hearingService.getHearingsBy(COURT_CENTRE_ID, lastModifiedTime)).thenReturn(hearingListXhibitResponse);

        final JsonEnvelope query = envelopeFrom(
                metadataBuilder().withId(randomUUID()).withName("hearing.get-hearings-by-court-centre"),
                createObjectBuilder()
                        .add(COURT_CENTRE_QUERY_PARAMETER, COURT_CENTRE_ID.toString())
                        .add(LAST_MODIFIED_TIME, "2016-11-12T09:27:12Z")
                        .build());


        final JsonEnvelope results = hearingQueryView.getHearingsByCourtCentre(query);

        assertThat(results.metadata().name(), is("hearing.get-hearings-by-court-centre"));
        assertThat(results.payloadAsJsonObject().getString("courtCentreId"), is(courtCentreId.toString()));
    }

    private Optional<CourtListPublishStatusResult> publishCourtListStatuses() {
        final UUID courtCentreId = randomUUID();
        final CourtListPublishStatusResult publishCourtListStatus = new CourtListPublishStatusResult(courtCentreId, now(), EXPORT_SUCCESSFUL);
        return Optional.of(publishCourtListStatus);
    }

}