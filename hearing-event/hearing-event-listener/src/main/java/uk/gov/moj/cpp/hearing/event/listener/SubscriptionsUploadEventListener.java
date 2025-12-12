package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Objects.isNull;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.persist.entity.not.Document;
import uk.gov.moj.cpp.hearing.repository.DocumentRepository;
import uk.gov.moj.cpp.hearing.subscription.events.SubscriptionUploaded;
import uk.gov.moj.cpp.hearing.subscription.events.SubscriptionsUploaded;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

@SuppressWarnings("squid:S3864")
@ServiceComponent(EVENT_LISTENER)
public class SubscriptionsUploadEventListener {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;
    @Inject
    private DocumentRepository documentRepository;

    @Transactional
    @Handles("hearing.subscriptions-uploaded")
    public void subscriptionsUploaded(final JsonEnvelope envelope) {

        final SubscriptionsUploaded subscriptionsUploaded = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), SubscriptionsUploaded.class);

        final Document document = new Document();

        final LocalDate referenceDate = LocalDate.parse(subscriptionsUploaded.getReferenceDate(), formatter);

        final List<Document> existingDocuments = documentRepository.findAllByOrderByStartDateAsc();

        final Optional<Document> documentToBeUpdatedList = existingDocuments.stream()
                .filter(existingDocument -> equalToOrGreater(referenceDate, existingDocument.getStartDate()))
                .filter(existingDocument -> equalToOrLesser(referenceDate, existingDocument.getEndDate()))
                .peek(documentToBeUpdated -> {
                    document.setEndDate(documentToBeUpdated.getEndDate());
                    documentToBeUpdated.setEndDate(referenceDate.minusDays(1));
                })
                .findFirst();


        if (!documentToBeUpdatedList.isPresent()) {
            document.setEndDate(existingDocuments.stream()
                    .map(Document::getStartDate)
                    .min(Comparator.comparing(Function.identity()))
                    .orElse(null));
        }

        document.setId(subscriptionsUploaded.getId());

        document.setStartDate(referenceDate);

        document.setSubscriptions(
                subscriptionsUploaded.getSubscriptions().stream()
                        .map(this::convert)
                        .collect(Collectors.toList()));

        documentRepository.saveAndFlush(document);

    }

    private boolean equalToOrLesser(LocalDate referenceDate, LocalDate endDate) {
        return isNull(endDate) || referenceDate.isBefore(endDate) || (referenceDate.isEqual(endDate));
    }

    private boolean equalToOrGreater(LocalDate referenceDate, LocalDate startDate) {
        return referenceDate.isAfter(startDate) || referenceDate.isEqual(startDate);
    }

    private uk.gov.moj.cpp.hearing.persist.entity.not.Subscription convert(SubscriptionUploaded s) {

        final uk.gov.moj.cpp.hearing.persist.entity.not.Subscription subscription = new uk.gov.moj.cpp.hearing.persist.entity.not.Subscription();
        subscription.setId(s.getId());
        subscription.setChannel(s.getChannel());
        subscription.setChannelProperties(s.getChannelProperties());
        subscription.setDestination(s.getDestination());
        subscription.setUserGroups(s.getUserGroups());
        subscription.setCourtCentreIds(s.getCourtCentreIds());
        subscription.setNowTypeIds(s.getNowTypeIds());

        return subscription;
    }
}
