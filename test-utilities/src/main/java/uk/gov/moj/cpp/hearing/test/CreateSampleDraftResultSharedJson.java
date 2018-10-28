package uk.gov.moj.cpp.hearing.test;

import static uk.gov.moj.cpp.hearing.CreateJsonSampleUtil.HEARING_COMMAND_HEARING_COMMAND_API_SRC_RAML_JSON;
import static uk.gov.moj.cpp.hearing.CreateJsonSampleUtil.HEARING_COMMAND_HEARING_COMMAND_HANDLER_SRC_RAML_JSON;
import static uk.gov.moj.cpp.hearing.CreateJsonSampleUtil.HEARING_EVENT_HEARING_EVENT_LISTENER_SRC_RAML_JSON;
import static uk.gov.moj.cpp.hearing.CreateJsonSampleUtil.HEARING_EVENT_HEARING_EVENT_PROCESSOR_SRC_RAML_JSON;
import static uk.gov.moj.cpp.hearing.CreateJsonSampleUtil.createObjectMapper;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.saveDraftResultCommandTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class CreateSampleDraftResultSharedJson {

    public static final String HEARING_DRAFT_RESULT_SAVED_JSON = "/hearing.draft-result-saved.json";

    @SuppressWarnings({"squid:S106", "squid:S2096"})
    public static void main(String[] args) throws IOException {
        final ObjectMapper objectMapper = createObjectMapper();
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final SaveDraftResultCommand command = saveDraftResultCommandTemplate(initiateHearingCommand, LocalDate.now());
        command.setHearingId(command.getHearingId());

        final List<String> destinations = Arrays.asList(
                HEARING_COMMAND_HEARING_COMMAND_HANDLER_SRC_RAML_JSON + "/hearing.command.save-draft-result.json",
                HEARING_COMMAND_HEARING_COMMAND_HANDLER_SRC_RAML_JSON + HEARING_DRAFT_RESULT_SAVED_JSON,
                HEARING_EVENT_HEARING_EVENT_LISTENER_SRC_RAML_JSON + HEARING_DRAFT_RESULT_SAVED_JSON,
                HEARING_EVENT_HEARING_EVENT_PROCESSOR_SRC_RAML_JSON + HEARING_DRAFT_RESULT_SAVED_JSON
        );

        for (final String strFile : destinations) {
            final File file = new File(strFile);
            System.out.println("writing to file " + file.getAbsolutePath());
            objectMapper.writeValue(file, command);
        }

        command.setHearingId(null);
        final File file = new File(HEARING_COMMAND_HEARING_COMMAND_API_SRC_RAML_JSON + "/hearing.save-draft-result.json");
        System.out.println("writing to file " + file.getAbsolutePath());
        objectMapper.writeValue(file, command);

    }

}
