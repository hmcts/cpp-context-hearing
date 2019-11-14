package uk.gov.moj.cpp.hearing.repository;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishStatus.COURT_LIST_REQUESTED;

import uk.gov.justice.services.test.utils.persistence.BaseTransactionalTest;
import uk.gov.moj.cpp.hearing.publishing.events.PublishStatus;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

;

@RunWith(CdiTestRunner.class)
public class CourtListRepositoryTest extends BaseTransactionalTest {

    @Inject
    private CourtListRepository courtListRepository;

    @Test
    public void shouldReturnPublishStatus() {
        final UUID courtCentreId = randomUUID();
        final PublishStatus publishStatus = COURT_LIST_REQUESTED;
        final UUID courtListFileId = randomUUID();
        final String courtListFileName = "c1";
        final ZonedDateTime lastUpdated = ZonedDateTime.now();
        final CourtListPK courtListPK = new CourtListPK(courtCentreId, publishStatus);

        final CourtList courtList = new CourtList(courtListPK, courtListFileId, courtListFileName, lastUpdated);

        courtListRepository.save(courtList);

        final List<CourtListPublishStatus> courtListPublishStatuses =
                courtListRepository.courtListPublishStatuses(courtCentreId);

        assertThat(courtListPublishStatuses.size(), is(1));
    }
}