package uk.gov.moj.cpp.hearing.repository;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.moj.cpp.hearing.persist.entity.not.Document;
import uk.gov.moj.cpp.hearing.persist.entity.not.Subscription;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

@RunWith(CdiTestRunner.class)
public class DocumentRepositoryTest {

    @Inject
    private DocumentRepository documentRepository;

    @After
    public void tearDown() {

        final List<Document> documents = documentRepository.findAll();

        documents.forEach(document -> documentRepository.remove(document));
    }

    @Test
    public void testPersistDocument() {

        final Document document = buildDocument();

        documentRepository.save(document);

        final Document expected = documentRepository.findBy(document.getId());

        assertEquals(document.getId(), expected.getId());
    }

    @Test
    public void findAllByOrderByStartDateAsc() {

        final Document document1 = new Document();
        document1.setId(randomUUID());
        document1.setSubscriptions(buildSubscriptions());
        document1.setStartDate(PAST_LOCAL_DATE.next());
        document1.setEndDate(LocalDate.now().minusDays(1));

        documentRepository.save(document1);

        final Document document2 = new Document();
        document2.setId(randomUUID());
        document2.setSubscriptions(buildSubscriptions());
        document2.setStartDate(LocalDate.now());
        document2.setEndDate(LocalDate.now().plusDays(4));

        documentRepository.save(document2);

        final Document document3 = new Document();
        document3.setId(randomUUID());
        document3.setSubscriptions(buildSubscriptions());
        document3.setStartDate(LocalDate.now().plusDays(5));
        document3.setEndDate(null);

        documentRepository.save(document3);

        final List<Document> results = documentRepository.findAllByOrderByStartDateAsc();

        assertEquals(3, results.size());

    }

    private Document buildDocument() {

        final Document document = new Document();
        document.setId(randomUUID());
        document.setSubscriptions(buildSubscriptions());
        document.setStartDate(LocalDate.now());

        return document;
    }

    private List<Subscription> buildSubscriptions() {

        return asList(buildSubscription(), buildSubscription());
    }

    private Subscription buildSubscription() {

        final Map<String, String> properties = new HashMap<>();
        properties.putIfAbsent(STRING.next(), STRING.next());
        properties.putIfAbsent(STRING.next(), STRING.next());
        properties.putIfAbsent(STRING.next(), STRING.next());

        final List<String> userGroups = asList(STRING.next(), STRING.next());

        final List<UUID> courtCentreIds = asList(randomUUID(), randomUUID());

        final List<UUID> nowTypeIds = asList(randomUUID(), randomUUID());

        final Subscription subscription = new Subscription();
        subscription.setId(randomUUID());
        subscription.setChannel(STRING.next());
        subscription.setChannelProperties(properties);
        subscription.setUserGroups(userGroups);
        subscription.setDestination(STRING.next());
        subscription.setCourtCentreIds(courtCentreIds);
        subscription.setNowTypeIds(nowTypeIds);

        return subscription;
    }
}
