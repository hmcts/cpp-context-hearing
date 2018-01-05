package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import uk.gov.justice.services.core.annotation.Handles;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

public class HearingCommandHandlerRamlConfigTest {

    private static final String PATH_TO_RAML = "src/raml/hearing-command-handler.messaging.raml";
    private static final String COMMAND_NAME = "hearing";
    private static final String CONTENT_TYPE_PREFIX = "application/vnd.";

    private Map<String, String> handlerMethodsToHandlerNames;
    private Map<String, String> eventHandlerMethodsToHandlerNames;

    @Before
    public void setup() {
        handlerMethodsToHandlerNames = getMethodsToHandlerNamesMapFor(HearingCommandHandler.class);
        handlerMethodsToHandlerNames.putAll(getMethodsToHandlerNamesMapFor(ListingCommandHandler.class));
        handlerMethodsToHandlerNames.putAll(getMethodsToHandlerNamesMapFor(ProgressionCommandHandler.class));

        eventHandlerMethodsToHandlerNames = getMethodsToHandlerNamesMapFor(HearingEventCommandHandler.class);
    }

    @Test
    public void testActionNameAndHandleNameAreSame() throws Exception {
        final List<String> allLines = FileUtils.readLines(new File(PATH_TO_RAML));

        final List<String> ramlActionNames = allLines.stream()
                .filter(action -> !action.isEmpty())
                .filter(line -> line.contains(CONTENT_TYPE_PREFIX) && line.contains(COMMAND_NAME))
                .map(line -> line.replaceAll("(application/vnd\\.)|(\\+json:)", "").trim())
                .collect(Collectors.toList());

        final List<String> allHandlerNames = concat(handlerMethodsToHandlerNames.values().stream(), eventHandlerMethodsToHandlerNames.values().stream()).collect(toList());

        assertThat(allHandlerNames, containsInAnyOrder(ramlActionNames.toArray()));
    }

    private <T> Map<String, String> getMethodsToHandlerNamesMapFor(final Class<T> commandApiClass) {
        final Map<String, String> methodToHandlerNamesMap = new HashMap<>();
        for (final Method method : commandApiClass.getMethods()) {
            final Handles handles = method.getAnnotation(Handles.class);
            if (handles != null) {
                methodToHandlerNamesMap.put(method.getName(), handles.value());
            }
        }
        return methodToHandlerNamesMap;
    }

}
