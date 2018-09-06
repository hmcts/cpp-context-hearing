package uk.gov.moj.cpp.hearing.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import static uk.gov.moj.cpp.hearing.CreateJsonSampleUtil.*;
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
        final ObjectMapper objectMapper = createObjectMapper();

        final GenerateNowsCommand command = TestTemplates.generateNowsCommandTemplate(UUID.randomUUID(), UUID.randomUUID());

        final List<String> destinations = Arrays.asList(
                HEARING_COMMAND_HEARING_COMMAND_API_SRC_RAML_JSON + "/hearing.generate-nows.json",
                HEARING_COMMAND_HEARING_COMMAND_HANDLER_SRC_RAML_JSON + "/hearing.command.generate-nows.json",
                "./hearing-domain/hearing-domain-event/src/test/resources/hearing.events.nows-requested.json",
                HEARING_EVENT_HEARING_EVENT_LISTENER_SRC_RAML_JSON + "/hearing.events.nows-requested.json",
                HEARING_EVENT_HEARING_EVENT_PROCESSOR_SRC_RAML_JSON + "/hearing.events.nows-requested.json",
                "./hearing-event/hearing-event-processor/src/test/resources/data/hearing.events.nows-requested.json"
        );

        for (final String strFile : destinations) {
            final File file = new File(strFile);
            System.out.println("writing to file " + file.getAbsolutePath());
            objectMapper.writeValue(file, command);
        }
    }

}
