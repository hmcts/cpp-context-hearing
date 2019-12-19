package uk.gov.moj.cpp.hearing.mapping;

import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CourtApplicationsSerializerTest {

    @Spy
    ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Spy
    JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @InjectMocks
    CourtApplicationsSerializer courtApplicationsSerializer;

    @Before
    public void setUp() {
        setField(objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
    }


    @Test
    public void test() {
        final List<CourtApplication> courtApplications = Arrays.asList(
                CourtApplication.courtApplication()
                        .withId(UUID.randomUUID())
                        .build()
        );
        final String json = courtApplicationsSerializer.json(courtApplications);
        final List<CourtApplication> courtApplicationsOut = courtApplicationsSerializer.courtApplications(json);
        Assert.assertEquals(courtApplications.get(0).getId(), courtApplicationsOut.get(0).getId());
    }

    @Test
    public void testNull() {
        final List<CourtApplication> courtApplications = null;
        final String json = courtApplicationsSerializer.json(courtApplications);
        final List<CourtApplication> courtApplicationsOut = courtApplicationsSerializer.courtApplications(json);
        Assert.assertEquals(courtApplications, courtApplicationsOut);
    }

    @Test
    public void testNullField() {
        final String json = null;
        final List<CourtApplication> courtApplicationsOut = courtApplicationsSerializer.courtApplications(json);
        Assert.assertEquals(0, courtApplicationsOut.size());
    }

}
