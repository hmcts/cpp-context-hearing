package uk.gov.moj.cpp.hearing.event;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class HearingEventProcessorRamlConfigTest {

    @Test
    public void testThatAllFilesInSchemasAreReferenced() throws IOException {

        List<String> filesThatArePresent =
                Arrays.stream(Objects.requireNonNull(new File("src/raml/json/schema").listFiles()))
                        .map(File::getName)
                        .map(name -> "json/schema/" + name)
                        .sorted()
                        .collect(toList());

        List<String> privateEventSchemas = FileUtils.readLines(new File("src/raml/hearing-public-event.messaging.raml"))
                .stream()
                .filter(line -> line.contains("schema:"))
                .map(line -> line.substring(line.indexOf("include") + "include".length()).trim())
                .collect(toList());

        filesThatArePresent.removeAll(privateEventSchemas);

        assertThat(filesThatArePresent, empty());
    }

}
