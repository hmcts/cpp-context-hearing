package uk.gov.moj.cpp.hearing.command.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerClassMatcher.isHandlerClass;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;

import uk.gov.justice.services.core.annotation.Handles;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

public class HearingCommandControllerTest {

    private static final String PATH_TO_RAML = "src/raml/hearing-command-controller.messaging.raml";
    private static final String COMMAND_NAME = "hearing";
    private static final String CONTENT_TYPE_PREFIX = "application/vnd.";
    private Map<String, String> controllerMethodsToHandlerNames;

    @Before
    public void setup() throws IOException {
        controllerMethodsToHandlerNames = getMethodsToHandlerNamesMapFor(HearingCommandController.class);
    }

    @Test
    public void testActionNameAndHandleNameAreSame() throws Exception {
        final List<String> allLines = FileUtils.readLines(new File(PATH_TO_RAML));

        final List<String> ramlActionNames = allLines.stream()
                .filter(action -> !action.isEmpty())
                .filter(line -> line.contains(CONTENT_TYPE_PREFIX) && line.contains(COMMAND_NAME))
                .map(line -> line.replaceAll("(application/vnd\\.)|(\\+json:)","").trim())
                .collect(Collectors.toList());

        assertThat(controllerMethodsToHandlerNames.values(), containsInAnyOrder(ramlActionNames.toArray()));
    }

    @Test
    public void testHandleNamesPassThroughSender() throws Exception {
        for (Map.Entry<String, String> entry : controllerMethodsToHandlerNames.entrySet()) {
            assertThat(HearingCommandController.class, isHandlerClass(COMMAND_CONTROLLER)
                    .with(method(entry.getKey())
                            .thatHandles(entry.getValue())
                            .withSenderPassThrough()));
        }
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