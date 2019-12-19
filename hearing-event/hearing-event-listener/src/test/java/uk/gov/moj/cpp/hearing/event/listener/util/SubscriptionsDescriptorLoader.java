package uk.gov.moj.cpp.hearing.event.listener.util;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.yaml.YamlFileValidator;
import uk.gov.justice.services.yaml.YamlParser;
import uk.gov.justice.services.yaml.YamlSchemaLoader;
import uk.gov.justice.services.yaml.YamlToJsonObjectConverter;
import uk.gov.justice.subscription.SubscriptionSorter;
import uk.gov.justice.subscription.SubscriptionsDescriptorParser;
import uk.gov.justice.subscription.domain.subscriptiondescriptor.Event;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SubscriptionsDescriptorLoader {

    private final SubscriptionsDescriptorParser subscriptionsDescriptorParser;
    private final Path path;

    public SubscriptionsDescriptorLoader(final Path path) {
        final YamlParser yamlParser = new YamlParser();
        final ObjectMapper yamlObjectMapper = new ObjectMapperProducer().objectMapper();
        final YamlToJsonObjectConverter yamlToJsonObjectConverter = new YamlToJsonObjectConverter(yamlParser, yamlObjectMapper);
        final YamlFileValidator yamlFileValidator = new YamlFileValidator(yamlToJsonObjectConverter, new YamlSchemaLoader());
        this.subscriptionsDescriptorParser = new SubscriptionsDescriptorParser(yamlParser, yamlFileValidator, new SubscriptionSorter());
        this.path = path;
    }

    public List<String> eventNames() throws MalformedURLException {

        final List<URL> urls = singletonList(path.toUri().toURL());

        return subscriptionsDescriptorParser.getSubscriptionDescriptorsFrom(urls)
                .flatMap(subscriptionsDescriptor ->
                        subscriptionsDescriptor.getSubscriptions().stream()
                                .flatMap(subscription -> subscription.getEvents().stream()
                                        .map(Event::getName)))
                .collect(toList());
    }
}
