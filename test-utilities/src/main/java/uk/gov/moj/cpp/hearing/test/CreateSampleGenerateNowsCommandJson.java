package uk.gov.moj.cpp.hearing.test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.GenerateNowsCommand;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * run this to create the sample data for GenerateNowsCommand - this is a replacement for manual editing of this complex file
 */
public class CreateSampleGenerateNowsCommandJson {

    @SuppressWarnings({"squid:S106", "squid:S2096"})
    public static void main(String[] args) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
        final GenerateNowsCommand command = TestTemplates.generateNowsCommandTemplate(UUID.randomUUID(), UUID.randomUUID());

        final List<String> destinations = Arrays.asList(
                "./hearing-command/hearing-command-api/src/raml/json/hearing.generate-nows.json",
                "./hearing-command/hearing-command-handler/src/raml/json/hearing.command.generate-nows.json",
                "./hearing-domain/hearing-domain-event/src/test/resources/hearing.events.nows-requested.json",
                "./hearing-event/hearing-event-listener/src/raml/json/hearing.events.nows-requested.json",
                "./hearing-event/hearing-event-processor/src/raml/json/hearing.events.nows-requested.json",
                "./hearing-event/hearing-event-processor/src/test/resources/data/hearing.events.nows-requested.json"
        );

        for (final String strFile : destinations) {
            final File file = new File(strFile);
            System.out.println("writing to file " + file.getAbsolutePath());
            objectMapper.writeValue(file, command);
        }
    }

}
