package uk.gov.moj.cpp.hearing.command.handler;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import uk.gov.justice.services.core.annotation.Handles;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;

public class HearingCommandHandlerRamlConfigTest {

    private static final String PATH_TO_RAML = "src/raml/hearing-command-handler.messaging.raml";
    private static final String COMMAND_NAME = "hearing";
    private static final String CONTENT_TYPE_PREFIX = "application/vnd.";

    @Test
    public void testActionNameAndHandleNameAreSame() throws Exception {

        final Object[] ramlActionNames = FileUtils.readLines(new File(PATH_TO_RAML)).stream()
                .filter(action -> !action.isEmpty())
                .filter(line -> line.contains(CONTENT_TYPE_PREFIX) && (line.contains(COMMAND_NAME) || (line.contains("notification"))))
                .map(line -> line.replaceAll("(application/vnd\\.)|(\\+json:)", "").trim())
                .toArray();

        final List<String> allHandlerNames = getHandlerNames(
                ShareResultsCommandHandler.class,
                InitiateHearingCommandHandler.class,
                UpdatePleaCommandHandler.class,
                UpdateVerdictCommandHandler.class,
                AddDefenceCounselCommandHandler.class,
                ProsecutionCounselCommandHandler.class,
                MagistratesCourtInitiateHearingCommandHandler.class,
                HearingEventCommandHandler.class,
                GenerateNowsCommandHandler.class,
                UpdateOffencesForDefendantCommandHandler.class,
                UpdateDefendantCommandHandler.class,
                HearingDetailChangeCommandHandler.class,
                UploadSubscriptionsCommandHandler.class,
                AdjournHearingCommandHandler.class,
                UpdateDefendantAttendanceCommandHandler.class,
                SaveHearingCaseNoteCommandHandler.class,
                SaveApplicationResponseCommandHandler.class,
                RespondentCounselCommandHandler.class,
                ApplicantCounselCommandHandler.class,
                AddDefendantCommandHandler.class,
                ApplicationDetailChangeCommandHandler.class,
                EjectCaseOrApplicationCommandHandler.class,
                CompanyRepresentativeCommandHandler.class,
                InterpreterIntermediaryCommandHandler.class,
                SetTrialTypeCommandHandler.class,
                UpdateOffenceResultsCommandHandler.class,
                CaseMarkersCommandHandler.class,
                UpdateDefendantLegalAidStatusCommandHandler.class,
                UpdateCaseDefendantsHandler.class,
                PublishCourtListStatusHandler.class,
                ComputeOutstandingFinesCommandHandler.class,
                AddRequestForOutstandingFinesCommandHandler.class
        );

        assertThat(allHandlerNames, containsInAnyOrder(ramlActionNames));
    }

    private List<String> getHandlerNames(final Class<?>... handlers) {
        return Stream.of(handlers)
                .flatMap(h -> Arrays.stream(h.getMethods()))
                .filter(m -> m.getAnnotation(Handles.class) != null)
                .map(m -> m.getAnnotation(Handles.class).value())
                .collect(Collectors.toList());
    }

    @Test
    public void testThatAllFilesInSchemasAreReferenced() throws IOException {

        List<String> filesThatArePresent =
                Arrays.stream(Objects.requireNonNull(new File("src/raml/json/schema").listFiles()))
                        .map(File::getName)
                        .filter(filename -> !filename.equals("hearing.command.generate-nows.json"))
                        .map(name -> "json/schema/" + name)
                        .collect(Collectors.toList());

        Collections.sort(filesThatArePresent);

        List<String> commandHandlerSchemas = FileUtils.readLines(new File("src/raml/hearing-command-handler.messaging.raml"))
                .stream()
                .filter(line -> line.contains("schema:"))
                .map(line -> line.substring(line.indexOf("include") + "include".length()).trim())
                .collect(Collectors.toList());

        List<String> privateEventSchemas = FileUtils.readLines(new File("src/raml/hearing-private-event.messaging.raml"))
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

        List<String> commandHandlerSchemas = FileUtils.readLines(new File("src/raml/hearing-command-handler.messaging.raml"))
                .stream()
                .filter(line -> line.contains("example:"))
                .map(line -> line.substring(line.indexOf("include") + "include".length()).trim())
                .collect(Collectors.toList());

        List<String> privateEventSchemas = FileUtils.readLines(new File("src/raml/hearing-private-event.messaging.raml"))
                .stream()
                .filter(line -> line.contains("example:"))
                .map(line -> line.substring(line.indexOf("include") + "include".length()).trim())
                .collect(Collectors.toList());

        filesThatArePresent.removeAll(commandHandlerSchemas);
        filesThatArePresent.removeAll(privateEventSchemas);
        //removing mac "json/.DS_Store" as it's not required
        filesThatArePresent.remove("json/.DS_Store");
        assertThat(filesThatArePresent, empty());
    }

}
