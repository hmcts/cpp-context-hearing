package uk.gov.moj.cpp.hearing.query.view;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.concat;
import static org.apache.commons.io.FileUtils.readLines;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import uk.gov.justice.services.core.annotation.Handles;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class HearingQueryViewRamlConfigTest {

    private static final String PATH_TO_RAML = "src/raml/hearing-query-view.raml";
    private static final String LABEL_TO_METHOD_NAME = "name:";

    private Map<String, String> viewMethodsToHandlerNames;
    private Map<String, String> eventViewMethodsToHandlerNames;
    private Map<String, String> pleaMethodsToHandlerNames;
    private Map<String,String> verdictMethodsToHandlerNames;

    @Before
    public void setup() throws IOException {
        viewMethodsToHandlerNames = viewMethodsToHandlerNames(HearingQueryView.class);
        eventViewMethodsToHandlerNames = viewMethodsToHandlerNames(HearingEventQueryView.class);
        pleaMethodsToHandlerNames = viewMethodsToHandlerNames(PleaQueryView.class);
        verdictMethodsToHandlerNames = viewMethodsToHandlerNames(VerdictQueryView.class);
    }

    @Test
    public void testActionNameAndHandleNameAreSame() throws Exception {
        final List<String> ramlActionNames = readLines(new File(PATH_TO_RAML)).stream()
                .filter(action -> !action.isEmpty())
                .filter(line -> line.contains(LABEL_TO_METHOD_NAME))
                .map(line -> line.replaceAll(LABEL_TO_METHOD_NAME, "").trim())
                .collect(toList());

        final List<String> allHandlerNames = concat(viewMethodsToHandlerNames.values().stream(), eventViewMethodsToHandlerNames.values().stream()).collect(toList());
        allHandlerNames.addAll(pleaMethodsToHandlerNames.values()) ;
        allHandlerNames.addAll(verdictMethodsToHandlerNames.values()) ;
        assertThat(allHandlerNames, containsInAnyOrder(ramlActionNames.toArray()));
    }

    private <T> Map<String, String> viewMethodsToHandlerNames(final Class<T> clazz) {
        return stream(clazz.getMethods())
                .filter(method -> method.getAnnotation(Handles.class) != null)
                .collect(toMap(Method::getName, method -> method.getAnnotation(Handles.class).value()));
    }
}
