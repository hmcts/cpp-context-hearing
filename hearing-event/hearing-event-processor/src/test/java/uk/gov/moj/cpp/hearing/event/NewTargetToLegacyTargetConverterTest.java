package uk.gov.moj.cpp.hearing.event;


import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.core.courts.Target;
import uk.gov.justice.core.courts.Target2;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.event.delegates.helper.NextHearingHelperTest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NewTargetToLegacyTargetConverterTest {


    @InjectMocks
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @InjectMocks
    NewTargetToLegacyTargetConverter newTargetToLegacyTargetConverter;

    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new JsonObjectConvertersFactory().objectToJsonObjectConverter();

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }
    @Test
    public void convert() throws IOException {
        final JsonObject jsonObject = givenPayload("/data/hearing.results-shared-v2.json");
        JsonObject targetJson = jsonObject.getJsonArray("targets").getJsonObject(0);
        final Target2 target = jsonObjectToObjectConverter
                .convert(targetJson, Target2.class);
        List<Target2> newTarget = newTargetToLegacyTargetConverter.convert(Arrays.asList(target));
        newTarget.stream().forEach(target1-> System.out.println(objectToJsonObjectConverter.convert(target1).toString()+","));
    }



    private static JsonObject givenPayload(final String filePath) {
        try (InputStream inputStream = NextHearingHelperTest.class.getResourceAsStream(filePath)) {
            final JsonReader jsonReader = Json.createReader(inputStream);
            return jsonReader.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}