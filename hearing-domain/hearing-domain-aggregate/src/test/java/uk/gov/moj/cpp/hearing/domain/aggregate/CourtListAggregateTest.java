package uk.gov.moj.cpp.hearing.domain.aggregate;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import uk.gov.moj.cpp.hearing.publishing.events.PublishCourtListExportFailed;
import uk.gov.moj.cpp.hearing.publishing.events.PublishCourtListExportSuccessful;
import uk.gov.moj.cpp.hearing.publishing.events.PublishCourtListRequested;
import uk.gov.moj.cpp.hearing.publishing.events.PublishStatus;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CourtListAggregateTest {
    private CourtListAggregate courtListAggregate;

    @BeforeEach
    public void setUp() {
        courtListAggregate = new CourtListAggregate();
    }

    @After
    public void teardown() {
        try {
            // ensure aggregate is serializable
            SerializationUtils.serialize(courtListAggregate);
        } catch (SerializationException e) {
            e.printStackTrace();
            fail("Aggregate should be serializable");
        }
    }

    @Test
    public void shouldRaiseEventPublishCourtListRequested(){
        final UUID courtCentreId = UUID.randomUUID();
        final Stream<Object> objectStream = courtListAggregate.recordCourtListRequested(courtCentreId, ZonedDateTime.now());
        final List<Object> events = objectStream.collect(Collectors.toList());
        final PublishCourtListRequested publishCourtListRequested = (PublishCourtListRequested)events.get(0);
        assertThat(publishCourtListRequested.getCourtCentreId(), is(courtCentreId));
        assertThat(publishCourtListRequested.getPublishStatus(), is(PublishStatus.COURT_LIST_REQUESTED));
        assertThat(courtListAggregate.getCourtCentreId(), is(courtCentreId));
    }

    @Test
    public void shouldRaiseEventPublishCourtListExportSuccessful(){
        final UUID courtCentreId = UUID.randomUUID();
        final Stream<Object> objectStream = courtListAggregate.recordCourtListExportSuccessful(courtCentreId, "file_1", ZonedDateTime.now());
        final List<Object> events = objectStream.collect(Collectors.toList());
        final PublishCourtListExportSuccessful publishCourtListExportSuccessful = (PublishCourtListExportSuccessful)events.get(0);
        assertThat(publishCourtListExportSuccessful.getCourtCentreId(), is(courtCentreId));
        assertThat(publishCourtListExportSuccessful.getPublishStatus(), is(PublishStatus.EXPORT_SUCCESSFUL));
    }

    @Test
    public void shouldRaiseEventPublishCourtListExportFailed(){
        final UUID courtCentreId = UUID.randomUUID();
        final Stream<Object> objectStream = courtListAggregate.recordCourtListExportFailed(courtCentreId,
                "file_1", ZonedDateTime.now(), "error_1");
        final List<Object> events = objectStream.collect(Collectors.toList());
        final PublishCourtListExportFailed publishCourtListExportFailed = (PublishCourtListExportFailed)events.get(0);
        assertThat(publishCourtListExportFailed.getCourtCentreId(), is(courtCentreId));
        assertThat(publishCourtListExportFailed.getPublishStatus(), is(PublishStatus.EXPORT_FAILED));
    }
}
