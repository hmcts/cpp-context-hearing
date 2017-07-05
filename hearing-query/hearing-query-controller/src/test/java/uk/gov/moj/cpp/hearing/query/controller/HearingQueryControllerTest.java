package uk.gov.moj.cpp.hearing.query.controller;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static uk.gov.justice.services.core.annotation.Component.QUERY_CONTROLLER;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerClassMatcher.isHandlerClass;
import static uk.gov.justice.services.test.utils.core.matchers.HandlerMethodMatcher.method;

import uk.gov.justice.services.core.annotation.Handles;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

public class HearingQueryControllerTest {

    private static final String PATH_TO_RAML = "src/raml/hearing-query-controller.raml";

    private Map<String, String> controllerMethodsToHandlerNames;

    @Before
    public void setup() throws Exception {
        controllerMethodsToHandlerNames = getMethodsToHandlerNamesMapFor(HearingQueryController.class);
    }

    @Test
    public void testActionNameAndHandleNameAreSame() throws Exception {
        final List<String> allLines = FileUtils.readLines(new File(PATH_TO_RAML));
        final List<String> ramlActionNames = allLines.stream()
                .filter(action -> !action.isEmpty())
                .filter(line -> line.contains("name:"))
                .map(line -> line.replaceAll("name:", "").trim())
                .collect(toList());

        assertThat(controllerMethodsToHandlerNames.values(), containsInAnyOrder(ramlActionNames.toArray()));
    }

    @Test
    public void testHandleNamesPassThroughRequester() throws Exception {
        for (Map.Entry<String, String> entry : controllerMethodsToHandlerNames.entrySet()) {
            assertThat(HearingQueryController.class, isHandlerClass(QUERY_CONTROLLER)
                    .with(method(entry.getKey())
                            .thatHandles(entry.getValue())
                            .withRequesterPassThrough()));
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
