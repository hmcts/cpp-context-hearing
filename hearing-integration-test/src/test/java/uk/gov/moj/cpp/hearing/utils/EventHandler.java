package uk.gov.moj.cpp.hearing.utils;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static uk.gov.justice.services.messaging.Envelope.envelopeFrom;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.services.messaging.DefaultJsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.messaging.MessageConsumerClient;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import com.google.common.base.Strings;

/**
 * EventListener
 * <p>
 * An instantiated helper which makes it easier listen and receive the events happening in result of
 * an asynchronous request. It exposes a fluent API, in which it expects you to call a few methods
 * in a subsequent fashion.
 * <p>
 * First, it expects you to specify probable events that you are interested in by calling
 * 'subscribe' methods. For each event you subscribed, execution will wait for those to occur (or
 * timeout) synchronously before continuing. Each subscribe call creates a separate event stack for
 * that event type, in the events map. When subscribing, it decides the topic of the event based on
 * the event naming convention.
 * <p>
 * Then, it expects you to call 'run' method with runnables or callables which would possibly
 * execute asynchronous behaviour that may result in the subscribed events. If the passed lambda
 * returns a value then it is interpreted as a callable and runner expects that return value to be
 * correlationId to filter coming events against. If the passed lambda does not return a value it is
 * interpreted as a runnable and runner treats the first incoming subscribed event to be the
 * relevant one.
 * <p>
 * Finally, it expect you to call 'popEvent' with the wanted event type, to access event payloads
 * that has been pushed to several different stacks.
 * <p>
 * Same instance could be used with different jobs by calling 'reset' in between.
 * <p>
 * Example usage; EventListener runner = new EventListener() .subscribe("event")
 * .subscribe("differentEvent") .subscribe("totallyDifferentEvent") .run(() ->
 * runnableThatDoesSomethingAsync()) .run(() -> anotherRunnableThatDoesSomethingAsyncAlso())
 * <p>
 * JsonObject event = runner.popEvent("event"); JsonObject anotherInstanceOfTheSameEvent =
 * runner.popEvent("event"); JsonObject differentEvent = runner.popEvent("differentEvent");
 * JsonObject totallyDifferentEvent = runner.popEvent("totallyDifferentEvent");
 */
public class EventHandler {

    private final Map<String, LinkedList<JsonEnvelope>> eventsByName;
    private Integer maxWaitTime = 15000;

    public static final String PUBLIC_ACTIVE_MQ_TOPIC = "public.event";
    public static final String PRIVATE_ACTIVE_MQ_TOPIC = "hearing.event";

    public EventHandler() {
        this.eventsByName = new HashMap<>();
    }

    public EventHandler withMaxWaitTime(final Integer maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
        return this;
    }

    public EventHandler subscribe(final String... eventNames) {
        for (String eventName : eventNames) {
            this.eventsByName.putIfAbsent(eventName, new LinkedList<>());
        }
        return this;
    }

    public EventHandler unsubscribe(String eventName) {
        this.eventsByName.remove(eventName);
        return this;
    }

    public EventHandler run(final Runnable action) {
        Map<String, MessageConsumerClient> consumers = eventsByName.keySet().parallelStream().collect(toMap(p -> p, this::startConsumer));
        consumers.values().forEach(MessageConsumerClient::cleanQueue);

        action.run();

        consumers.entrySet().parallelStream().forEach(this::receiveAndClose);

        return this;
    }

    public EventHandler run(final Callable action) {
        Map<String, MessageConsumerClient> consumers = eventsByName.keySet().parallelStream().collect(toMap(p -> p, this::startConsumer));

        Optional<UUID> correlationId = runAndCheckForCorrelationId(action);

        if (correlationId.isPresent()) {
            consumers.entrySet().parallelStream().forEach((entry) -> receiveAndClose(entry, correlationId.get()));
        } else {
            consumers.entrySet().parallelStream().forEach(this::receiveAndClose);
        }

        return this;
    }

    public Optional<JsonEnvelope> popEvent(String eventName) {
        return ofNullable(this.eventsByName.get(eventName).poll());
    }

    public Optional<JsonEnvelope> peekEvent(final String eventName) {
        return ofNullable(this.eventsByName.get(eventName).peek());
    }

    public <T> Optional<Envelope<T>> popEvent(final Class<T> eventClass) {

        final Event eventAnnotation = eventClass.getAnnotation(Event.class);

        if (isNull(eventAnnotation)) {
            throw new IllegalArgumentException(String.format("Class %s is not annotated with @Event", eventClass));
        }

        return ofNullable(this.eventsByName.get(eventAnnotation.value()).poll())
                .map(jsonEnvelope -> envelopeFrom(jsonEnvelope.metadata(), JsonHelper.fromJsonObject(jsonEnvelope.payloadAsJsonObject(), eventClass)));
    }

    public <T> T popEventPayload(final Class<T> eventClass) {
        return popEvent(eventClass)
                .map(Envelope::payload)
                .orElseThrow(() -> new RuntimeException(String.format("Event %s not present", eventClass.getAnnotation(Event.class).value())));
    }

    public EventHandler reset() {
        this.eventsByName.clear();
        return this;
    }

    public Set<String> getSubscribedEvents() {
        return eventsByName.keySet();
    }

    private MessageConsumerClient startConsumer(String eventName) {
        MessageConsumerClient messageConsumer = new MessageConsumerClient();
        messageConsumer.startConsumer(eventName, determineTopic(eventName));
        return messageConsumer;
    }

    private Optional<UUID> runAndCheckForCorrelationId(final Callable action) {
        try {
            return Optional.of((UUID) action.call());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private void receiveAndClose(Map.Entry<String, MessageConsumerClient> entry, UUID correlationId) {
        try (MessageConsumerClient consumer = entry.getValue()) {

            while (true) {
                Optional<String> optionalMessage = consumer.retrieveMessage(maxWaitTime);
                if (!optionalMessage.isPresent()) {
                    break;
                }

                JsonEnvelope jsonEnvelope = new DefaultJsonObjectEnvelopeConverter().asEnvelope(optionalMessage.get());
                Optional<String> eventCorrelationId = jsonEnvelope.metadata().clientCorrelationId();
                boolean matched = eventCorrelationId.isPresent() && correlationId.equals(UUID.fromString(eventCorrelationId.get()));

                if (matched) {
                    eventsByName.get(entry.getKey()).add(jsonEnvelope);
                    break;
                }
            }
        }
    }

    private void receiveAndClose(Map.Entry<String, MessageConsumerClient> entry) {
        try (MessageConsumerClient consumer = entry.getValue()) {
            String eventName = entry.getKey();

            Optional<String> optionalMessage = consumer.retrieveMessage(maxWaitTime);
            optionalMessage.ifPresent((message) -> {
                JsonEnvelope jsonEnvelope = new DefaultJsonObjectEnvelopeConverter().asEnvelope(message);
                eventsByName.get(eventName).add(jsonEnvelope);
            });
        }
    }

    private String determineTopic(String eventName) {
        if (Strings.isNullOrEmpty(eventName)) {
            throw new RuntimeException("Event topic could not be determined");
        } else if (eventName.startsWith("public")) {
            return PUBLIC_ACTIVE_MQ_TOPIC;
        } else if (eventName.contains(PRIVATE_ACTIVE_MQ_TOPIC)) {
            return PRIVATE_ACTIVE_MQ_TOPIC;
        } else {
            throw new RuntimeException("Event topic could not be determined");
        }
    }

}
