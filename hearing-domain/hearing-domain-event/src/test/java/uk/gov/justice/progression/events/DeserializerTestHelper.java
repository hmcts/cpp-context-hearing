package uk.gov.justice.progression.events;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matcher;

public class DeserializerTestHelper {

    public static <T> void testJsonSerializationRoundTrip(T pojo, final Matcher jsonMatcher) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

        final String jsonString = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(pojo);
        System.out.println(jsonString);

        assertThat(jsonString, isJson(
                jsonMatcher
        ));

        final T newPojo = (T) objectMapper.readValue(jsonString, pojo.getClass());

        assertThat(newPojo, is(pojo));
    }
}
