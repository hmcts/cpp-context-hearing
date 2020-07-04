package uk.gov.moj.cpp.hearing.utils;

import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.json.JsonObject;

import com.jayway.restassured.path.json.JsonPath;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQTopic;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueUtil implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueueUtil.class);

    private static final String EVENT_SELECTOR_TEMPLATE = "CPPNAME IN ('%s')";

    private static final String HOST = System.getProperty("INTEGRATION_HOST_KEY", "localhost");
    private static final String QUEUE_URI = "tcp://" + HOST + ":61616";

    private static final long RETRIEVE_TIMEOUT = 20000;
    private final Topic topic;
    private Session session;
    private MessageProducer messageProducer;

    private MessageConsumer messageConsumer;
    private Connection connection;

    private QueueUtil(final String topicName) {
        try {
            LOGGER.info("Artemis URI: {}", QUEUE_URI);
            final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(QUEUE_URI);
            connection = factory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            topic = new ActiveMQTopic(topicName);
        } catch (final JMSException e) {
            LOGGER.error("Fatal error initialising Artemis", e);
            throw new RuntimeException(e);
        }
    }

    public static QueueUtil getPublicTopicInstance() {
        return new QueueUtil("public.event");
    }

    public static QueueUtil getPrivateTopicInstance(final String topicName) {
        return new QueueUtil(topicName);
    }

    public static JsonPath retrieveMessage(final MessageConsumer consumer) {
        return retrieveMessage(consumer, RETRIEVE_TIMEOUT);
    }

    public static void sendMessage(final MessageProducer messageProducer, final String eventName, final JsonObject payload, final Metadata metadata) {

        final JsonEnvelope jsonEnvelope = envelopeFrom(metadata, payload);

        final String json = jsonEnvelope.asJsonObject().toString();

        try {
            final TextMessage message = new ActiveMQTextMessage();

            message.setText(json);
            message.setStringProperty("CPPNAME", eventName);

            messageProducer.send(message);
        } catch (final JMSException e) {
            throw new RuntimeException("Failed to send message. commandName: '" + eventName + "', json: " + json, e);
        }
    }

    public static JsonPath retrieveMessage(final MessageConsumer consumer, final long customTimeOutInMillis) {
        try {
            final TextMessage message = (TextMessage) consumer.receive(customTimeOutInMillis);
            if (message == null) {
                LOGGER.error("No message retrieved using consumer with selector {}", consumer.getMessageSelector());
                return null;
            }
            return new JsonPath(message.getText());
        } catch (final JMSException e) {
            throw new RuntimeException(e);
        }
    }

    public MessageConsumer createConsumer(final String eventSelector) {
        try {
            messageConsumer = session.createConsumer(topic, String.format(EVENT_SELECTOR_TEMPLATE, eventSelector));
            return messageConsumer;
        } catch (final JMSException e) {
            throw new RuntimeException(e);
        }
    }

    public MessageProducer createProducer() {
        try {
            messageProducer = session.createProducer(topic);
            return messageProducer;
        } catch (final JMSException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            session.close();
            if (messageConsumer != null) {
                messageConsumer.close();
            }
            if (messageProducer != null) {
                messageProducer.close();
            }
            connection.close();
        } catch (JMSException ignored) {
        }

        session = null;
        connection = null;
        messageProducer = null;
        messageConsumer = null;

    }
}
