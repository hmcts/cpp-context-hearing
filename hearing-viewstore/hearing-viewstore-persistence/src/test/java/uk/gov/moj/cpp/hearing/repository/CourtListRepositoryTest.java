package uk.gov.moj.cpp.hearing.repository;

import static java.time.ZonedDateTime.*;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishStatus.COURT_LIST_PRODUCED;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishStatus.EXPORT_FAILED;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishStatus.EXPORT_SUCCESSFUL;

import uk.gov.justice.services.test.utils.persistence.BaseTransactionalTest;
import uk.gov.moj.cpp.hearing.publishing.events.PublishStatus;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(CdiTestRunner.class)
public class CourtListRepositoryTest extends BaseTransactionalTest {

    @Inject
    private CourtListRepository courtListRepository;

    @Test
    public void shouldReturnLatestSuccessPublishStatus() {
        final UUID courtCentreId = randomUUID();

        final PublishStatus publishStatus = EXPORT_SUCCESSFUL;
        final UUID courtListFileId1 = randomUUID();
        final String courtListFileName1 = "c1";
        final ZonedDateTime lastUpdated1 = now().minusMinutes(10l);
        final CourtListPublishStatus courtList1 = new CourtListPublishStatus(randomUUID(), courtCentreId, publishStatus, lastUpdated1, courtListFileId1, courtListFileName1, "");

        final UUID courtListFileId2 = randomUUID();
        final String courtListFileName2 = "c2";
        final ZonedDateTime lastUpdated2 = now();
        final CourtListPublishStatus courtList2 = new CourtListPublishStatus(randomUUID(), courtCentreId, publishStatus, lastUpdated2, courtListFileId2, courtListFileName2, "");

        courtListRepository.save(courtList1);
        courtListRepository.save(courtList2);


        final Optional<CourtListPublishStatusResult> courtListPublishStatus =
                courtListRepository.courtListPublishStatuses(courtCentreId);

        assertTrue(courtListPublishStatus.isPresent());
        assertThat(courtListPublishStatus.get().getPublishStatus(), is(publishStatus));
        assertThat(courtListPublishStatus.get().getLastUpdated(), is(lastUpdated2));
    }

    @Test
    public void shouldNotReturnPublishStatus() {
        final UUID courtCentreId = randomUUID();

        final PublishStatus publishStatus = COURT_LIST_PRODUCED;
        final UUID courtListFileId1 = randomUUID();
        final String courtListFileName1 = "c1";
        final ZonedDateTime lastUpdated1 = now().minusMinutes(10l);
        final CourtListPublishStatus courtList1 = new CourtListPublishStatus(randomUUID(), courtCentreId, publishStatus, lastUpdated1, courtListFileId1, courtListFileName1, "");


        courtListRepository.save(courtList1);


        final Optional<CourtListPublishStatusResult> courtListPublishStatus =
                courtListRepository.courtListPublishStatuses(courtCentreId);

        assertFalse(courtListPublishStatus.isPresent());
    }

    @Test
    public void shouldReturnLatestFailedPublishStatus() {
        final UUID courtCentreId = randomUUID();

        final PublishStatus publishStatus = EXPORT_FAILED;
        final UUID courtListFileId = randomUUID();
        final String courtListFileName = "c1";
        final ZonedDateTime lastUpdated1 = now().minusMinutes(10l);
        final CourtListPublishStatus courtList1 = new CourtListPublishStatus(randomUUID(), courtCentreId, publishStatus, lastUpdated1, courtListFileId, courtListFileName, "");

        final UUID courtListFileId2 = randomUUID();
        final String courtListFileName2 = "c2";
        final ZonedDateTime lastUpdated2 = now();
        final CourtListPublishStatus courtList2 = new CourtListPublishStatus(randomUUID(), courtCentreId, publishStatus, lastUpdated2, courtListFileId2, courtListFileName2, "");

        courtListRepository.save(courtList1);
        courtListRepository.save(courtList2);

        final Optional<CourtListPublishStatusResult> courtListPublishStatus =
                courtListRepository.courtListPublishStatuses(courtCentreId);

        assertTrue(courtListPublishStatus.isPresent());
        assertThat(courtListPublishStatus.get().getPublishStatus(), is(publishStatus));
        assertThat(courtListPublishStatus.get().getLastUpdated(), is(lastUpdated2));
    }
}
