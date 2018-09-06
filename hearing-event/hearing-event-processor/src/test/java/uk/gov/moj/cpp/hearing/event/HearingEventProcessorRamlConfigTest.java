package uk.gov.moj.cpp.hearing.event;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;

public class HearingEventProcessorRamlConfigTest {

    @Test
    public void testThatAllFilesInSchemasAreReferenced() throws IOException {

        List<String> filesThatArePresent =
                Arrays.stream(Objects.requireNonNull(new File("src/raml/json/schema").listFiles()))
                        .map(File::getName)
                        .map(name -> "json/schema/" + name)
                        .collect(Collectors.toList());

        Collections.sort(filesThatArePresent);

        List<String> commandHandlerSchemas = FileUtils.readLines(new File("src/raml/hearing-event-processor.messaging.raml"))
                .stream()
                .filter(line -> line.contains("schema:"))
                .map(line -> line.substring(line.indexOf("include") + "include".length()).trim())
                .collect(Collectors.toList());

        List<String> privateEventSchemas = FileUtils.readLines(new File("src/raml/hearing-public-event.messaging.raml"))
                .stream()
                .filter(line -> line.contains("schema:"))
                .map(line -> line.substring(line.indexOf("include") + "include".length()).trim())
                .collect(Collectors.toList());

        filesThatArePresent.removeAll(commandHandlerSchemas);
        filesThatArePresent.removeAll(privateEventSchemas);

        assertThat(filesThatArePresent, empty());
    }

    @Test
    public void testThatAllFilesInExamplesAreReferenced() throws IOException {

        List<String> filesThatArePresent =
                Arrays.stream(Objects.requireNonNull(new File("src/raml/json").listFiles()))
                        .map(File::getName)
                        .map(name -> "json/" + name)
                        .filter(name -> !name.equals("json/schema"))
                        .collect(Collectors.toList());

        Collections.sort(filesThatArePresent);

        List<String> commandHandlerSchemas = FileUtils.readLines(new File("src/raml/hearing-event-processor.messaging.raml"))
                .stream()
                .filter(line -> line.contains("example:"))
                .map(line -> line.substring(line.indexOf("include") + "include".length()).trim())
                .collect(Collectors.toList());

        List<String> privateEventSchemas = FileUtils.readLines(new File("src/raml/hearing-public-event.messaging.raml"))
                .stream()
                .filter(line -> line.contains("example:"))
                .map(line -> line.substring(line.indexOf("include") + "include".length()).trim())
                .collect(Collectors.toList());

        filesThatArePresent.removeAll(commandHandlerSchemas);
        filesThatArePresent.removeAll(privateEventSchemas);

        assertThat(filesThatArePresent, empty());
    }
}
