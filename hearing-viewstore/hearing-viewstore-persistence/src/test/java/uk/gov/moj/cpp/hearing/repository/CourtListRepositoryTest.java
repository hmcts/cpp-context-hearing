package uk.gov.moj.cpp.hearing.repository;

import static java.time.ZonedDateTime.now;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishStatus.COURT_LIST_REQUESTED;
import static uk.gov.moj.cpp.hearing.publishing.events.PublishStatus.EXPORT_SUCCESSFUL;

import uk.gov.justice.services.test.utils.persistence.HibernateTestEntityManagerProvider;
import uk.gov.moj.cpp.hearing.publishing.events.PublishStatus;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;


class CourtListRepositoryTest {

    private static final String PERSISTENCE_UNIT = "hearing-test-persistence-unit";

    @RegisterExtension
    static HibernateTestEntityManagerProvider hibernateTestEntityManagerProvider =
            new HibernateTestEntityManagerProvider(PERSISTENCE_UNIT);

    private CourtListRepository courtListRepository;

    @BeforeEach
    void openEntityManagerAndCreateRepository() {
        courtListRepository = new CourtListRepository();
        hibernateTestEntityManagerProvider.injectEntityManagerInto(courtListRepository);
    }

    @Test
    void shouldReturnLatestSuccessPublishStatus() {
        final UUID courtCentreId = randomUUID();

        final PublishStatus publishStatus = EXPORT_SUCCESSFUL;
        final String courtListFileName1 = "c1";
        final ZonedDateTime lastUpdated1 = now().minusMinutes(10L);
        final CourtListPublishStatus courtList1 = new CourtListPublishStatus(randomUUID(), courtCentreId, publishStatus, lastUpdated1, courtListFileName1, "");

        final String courtListFileName2 = "c2";
        final ZonedDateTime lastUpdated2 = now();
        final CourtListPublishStatus courtList2 = new CourtListPublishStatus(randomUUID(), courtCentreId, publishStatus, lastUpdated2, courtListFileName2, "");

        courtListRepository.save(courtList1);
        courtListRepository.save(courtList2);

        final Optional<CourtListPublishStatusResult> courtListPublishStatus =
                courtListRepository.courtListPublishStatuses(courtCentreId);

        assertThat(courtListPublishStatus.isPresent(), is(true));
        assertThat(courtListPublishStatus.get().getPublishStatus(), is(publishStatus));
        assertThat(courtListPublishStatus.get().getLastUpdated(), is(lastUpdated2));
    }

    @Test
    void shouldNotReturnPublishStatus() {
        final UUID courtCentreId = randomUUID();

        final PublishStatus publishStatus = COURT_LIST_REQUESTED;
        final String courtListFileName1 = "c1";
        final ZonedDateTime lastUpdated1 = now().minusMinutes(10L);
        final CourtListPublishStatus courtList1 = new CourtListPublishStatus(randomUUID(), courtCentreId, publishStatus, lastUpdated1, courtListFileName1, "");

        courtListRepository.save(courtList1);

        final Optional<CourtListPublishStatusResult> courtListPublishStatus =
                courtListRepository.courtListPublishStatuses(courtCentreId);

        assertThat(courtListPublishStatus.isPresent(), is(false));
    }
}
