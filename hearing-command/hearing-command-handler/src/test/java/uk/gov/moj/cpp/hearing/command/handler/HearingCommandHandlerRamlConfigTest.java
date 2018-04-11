package uk.gov.moj.cpp.hearing.command.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import uk.gov.justice.services.core.annotation.Handles;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class HearingCommandHandlerRamlConfigTest {

    private static final String PATH_TO_RAML = "src/raml/hearing-command-handler.messaging.raml";
    private static final String COMMAND_NAME = "hearing";
    private static final String CONTENT_TYPE_PREFIX = "application/vnd.";

    @Test
    public void testActionNameAndHandleNameAreSame() throws Exception {

        final Object[] ramlActionNames = FileUtils.readLines(new File(PATH_TO_RAML)).stream()
                .filter(action -> !action.isEmpty())
                .filter(line -> line.contains(CONTENT_TYPE_PREFIX) && line.contains(COMMAND_NAME))
                .map(line -> line.replaceAll("(application/vnd\\.)|(\\+json:)", "").trim())
                .toArray();

        final List<String> allHandlerNames = getHandlerNames(
                HearingCommandHandler.class,
                NewModelInitiateHearingCommandHandler.class,
                NewModelUpdatePleaCommandHandler.class,
                NewModelUpdateVerdictCommandHandler.class,
                MagistratesCourtInitiateHearingCommandHandler.class,
                HearingEventCommandHandler.class
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

}
