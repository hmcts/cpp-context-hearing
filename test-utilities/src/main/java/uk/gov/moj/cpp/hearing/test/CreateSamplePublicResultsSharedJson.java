package uk.gov.moj.cpp.hearing.test;

import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.CreateJsonSampleUtil.HEARING_COMMAND_HEARING_COMMAND_API_SRC_RAML_JSON;
import static uk.gov.moj.cpp.hearing.CreateJsonSampleUtil.HEARING_COMMAND_HEARING_COMMAND_HANDLER_SRC_RAML_JSON;
import static uk.gov.moj.cpp.hearing.CreateJsonSampleUtil.createObjectMapper;

import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CreateSamplePublicResultsSharedJson {
    @SuppressWarnings({"squid:S106", "squid:S2096"})
    public static void main(String[] args) throws IOException {
        final ObjectMapper objectMapper = createObjectMapper();
        final ShareResultsCommand command = ShareResultsCommand.shareResultsCommand()
                .setCourtClerk(DelegatedPowers.delegatedPowers()
                        .withUserId(randomUUID())
                        .withFirstName(STRING.next())
                        .withLastName(STRING.next())
                        .build());

        final List<String> destinations = Arrays.asList(
                HEARING_COMMAND_HEARING_COMMAND_API_SRC_RAML_JSON + "/hearing.share-results.json",
                HEARING_COMMAND_HEARING_COMMAND_HANDLER_SRC_RAML_JSON + "/hearing.command.share-results.json"
        );

        for (final String strFile : destinations) {
            final File file = new File(strFile);
            System.out.println("writing to file " + file.getAbsolutePath());
            objectMapper.writeValue(file, command);
        }

    }

}
