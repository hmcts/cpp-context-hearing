package uk.gov.moj.cpp.hearing.event;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FileUtils.readLines;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

public class HearingEventProcessorYamlConfigTest {

    @Test
    public void testThatAllPrivateAndPublicSchemasForConsumedEventsAreReferenced() throws IOException {

        List<String> contentsOfFilesThatArePresent =
                stream(requireNonNull(new File("src/yaml/json/schema").listFiles()))
                        .map(f -> {
                            try {
                                return readFileToString(f);
                            } catch (IOException e) {
                                return "";
                            }
                        })
                        .collect(toList());

        List<String> privateEventSchemas = readLines(new File("src/yaml/subscriptions-descriptor.yaml"))
                .stream()
                .filter(line -> line.contains("schema_uri: ") && !line.contains("command"))
                .map(line -> line.trim().substring("schema_uri: ".length()))
                .collect(toList());

        for (String privateEventSchema : privateEventSchemas) {
            boolean eventSchemaFound = false;
            for (String fileContent : contentsOfFilesThatArePresent) {
                if (fileContent.contains(privateEventSchema)) {
                    eventSchemaFound = true;
                    break;
                }
            }
            assertThat("Event schema not found for " + privateEventSchema, eventSchemaFound, is(true));
        }
    }

    @Test
    public void testThatAllPublicSchemasForGeneratedEventsAreReferenced() throws IOException {

        List<String> contentsOfFilesThatArePresent =
                stream(requireNonNull(new File("src/yaml/json/schema").listFiles()))
                        .map(f -> {
                            try {
                                return readFileToString(f);
                            } catch (IOException e) {
                                return "";
                            }
                        })
                        .collect(toList());

        List<String> publicEventSchemas = readLines(new File("src/yaml/public-publications-descriptor.yaml"))
                .stream()
                .filter(line -> line.contains("schema_uri: "))
                .map(line -> line.trim().substring("schema_uri: ".length()))
                .collect(toList());

        for (String publicEventSchema : publicEventSchemas) {
            boolean eventSchemaFound = false;
            for (String fileContent : contentsOfFilesThatArePresent) {
                if (fileContent.contains(publicEventSchema)) {
                    eventSchemaFound = true;
                    break;
                }
            }
            assertThat("Event schema not found for " + publicEventSchema, eventSchemaFound, is(true));
        }
    }

}
