package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.FUTURE_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.persist.entity.not.Document;
import uk.gov.moj.cpp.hearing.repository.DocumentRepository;
import uk.gov.moj.cpp.hearing.subscription.events.SubscriptionUploaded;
import uk.gov.moj.cpp.hearing.subscription.events.SubscriptionsUploaded;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class SubscriptionsUploadEventListenerTest {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @InjectMocks
    private SubscriptionsUploadEventListener subscriptionsUploadEventListener;
    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;
    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;
    @Mock
    private DocumentRepository documentRepository;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void uploadSubscriptionsWhenExistingDocumentsIsEmpty() {

        final SubscriptionsUploaded subscriptionsUploaded = buildSubscriptions();

        final JsonEnvelope envelope = envelopeFrom(
                metadataWithRandomUUID("hearing.command.upload-subscriptions"),
                objectToJsonObjectConverter.convert(subscriptionsUploaded));

        when(documentRepository.findAllByOrderByStartDateAsc()).thenReturn(new ArrayList<>());

        subscriptionsUploadEventListener.subscriptionsUploaded(envelope);

        final ArgumentCaptor<uk.gov.moj.cpp.hearing.persist.entity.not.Document> argumentCaptor =
                ArgumentCaptor.forClass(uk.gov.moj.cpp.hearing.persist.entity.not.Document.class);

        verify(documentRepository).saveAndFlush(argumentCaptor.capture());

        final uk.gov.moj.cpp.hearing.persist.entity.not.Document document = argumentCaptor.getValue();

        assertThat(subscriptionsUploaded.getSubscriptions().get(0).getId(), is(document.getSubscriptions().get(0).getId()));
    }

    @Test
    public void uploadSubscriptionsWhenExistingDocumentsIsNotEmpty() {

        final SubscriptionsUploaded subscriptionsUploaded = buildSubscriptions();

        final JsonEnvelope envelope = envelopeFrom(
                metadataWithRandomUUID("hearing.command.upload-subscriptions"),
                objectToJsonObjectConverter.convert(subscriptionsUploaded));

        final Document existingDocument = new Document();
        existingDocument.setId(randomUUID());
        existingDocument.setStartDate(PAST_LOCAL_DATE.next());
        existingDocument.setEndDate(null);

        when(documentRepository.findAllByOrderByStartDateAsc()).thenReturn(asList(existingDocument));

        subscriptionsUploadEventListener.subscriptionsUploaded(envelope);

        final ArgumentCaptor<uk.gov.moj.cpp.hearing.persist.entity.not.Document> argumentCaptor =
                ArgumentCaptor.forClass(uk.gov.moj.cpp.hearing.persist.entity.not.Document.class);

        verify(documentRepository).saveAndFlush(argumentCaptor.capture());

        final uk.gov.moj.cpp.hearing.persist.entity.not.Document document = argumentCaptor.getValue();

        assertThat(subscriptionsUploaded.getSubscriptions().get(0).getId(), is(document.getSubscriptions().get(0).getId()));

        assertThat(existingDocument.getEndDate().toString(), is(LocalDate.now().minusDays(1).toString()));
    }

    @Test
    public void uploadSubscriptionsWhenReferenceDateIsBetweenExistingDocuments() {

        final SubscriptionsUploaded subscriptionsUploaded = buildSubscriptions();

        final JsonEnvelope envelope = envelopeFrom(
                metadataWithRandomUUID("hearing.command.upload-subscriptions"),
                objectToJsonObjectConverter.convert(subscriptionsUploaded));

        final Document existingDocument2 = new Document();
        existingDocument2.setId(randomUUID());
        existingDocument2.setStartDate(FUTURE_LOCAL_DATE.next());
        existingDocument2.setEndDate(null);

        final Document existingDocument1 = new Document();
        existingDocument1.setId(randomUUID());
        existingDocument1.setStartDate(PAST_LOCAL_DATE.next());
        existingDocument1.setEndDate(existingDocument2.getStartDate().minusDays(1));

        when(documentRepository.findAllByOrderByStartDateAsc()).thenReturn(asList(
                existingDocument1,
                existingDocument2));

        subscriptionsUploadEventListener.subscriptionsUploaded(envelope);

        final ArgumentCaptor<uk.gov.moj.cpp.hearing.persist.entity.not.Document> argumentCaptor =
                ArgumentCaptor.forClass(uk.gov.moj.cpp.hearing.persist.entity.not.Document.class);

        verify(documentRepository).saveAndFlush(argumentCaptor.capture());

        final uk.gov.moj.cpp.hearing.persist.entity.not.Document document = argumentCaptor.getValue();

        assertThat(subscriptionsUploaded.getSubscriptions().get(0).getId(), is(document.getSubscriptions().get(0).getId()));

        assertThat(existingDocument1.getEndDate().toString(), is(LocalDate.now().minusDays(1).toString()));
    }

    @Test
    public void uploadSubscriptionsWhenReferenceDateIsBeforeExistingDocuments() {

        final SubscriptionsUploaded subscriptionsUploaded = buildSubscriptions();

        final JsonEnvelope envelope = envelopeFrom(
                metadataWithRandomUUID("hearing.command.upload-subscriptions"),
                objectToJsonObjectConverter.convert(subscriptionsUploaded));

        final Document existingDocument = new Document();
        existingDocument.setId(randomUUID());
        existingDocument.setStartDate(FUTURE_LOCAL_DATE.next());
        existingDocument.setEndDate(null);

        when(documentRepository.findAllByOrderByStartDateAsc()).thenReturn(asList(existingDocument));

        subscriptionsUploadEventListener.subscriptionsUploaded(envelope);

        final ArgumentCaptor<uk.gov.moj.cpp.hearing.persist.entity.not.Document> argumentCaptor =
                ArgumentCaptor.forClass(uk.gov.moj.cpp.hearing.persist.entity.not.Document.class);

        verify(documentRepository).saveAndFlush(argumentCaptor.capture());

        final uk.gov.moj.cpp.hearing.persist.entity.not.Document document = argumentCaptor.getValue();

        assertThat(subscriptionsUploaded.getSubscriptions().get(0).getId(), is(document.getSubscriptions().get(0).getId()));

    }

    private SubscriptionsUploaded buildSubscriptions() {

        final Map<String, String> properties = new HashMap<>();
        properties.putIfAbsent(STRING.next(), STRING.next());
        properties.putIfAbsent(STRING.next(), STRING.next());
        properties.putIfAbsent(STRING.next(), STRING.next());

        final List<String> userGroups = asList(STRING.next(), STRING.next());

        final List<UUID> courtCentreIds = asList(randomUUID(), randomUUID());

        final List<UUID> nowTypeIds = asList(randomUUID(), randomUUID());

        final SubscriptionUploaded subscriptionUploaded = new SubscriptionUploaded(
                randomUUID(),
                STRING.next(),
                properties,
                userGroups,
                STRING.next(),
                courtCentreIds,
                nowTypeIds);

        return new SubscriptionsUploaded(
                randomUUID(),
                singletonList(subscriptionUploaded),
                LocalDate.now().format(formatter));

    }
}