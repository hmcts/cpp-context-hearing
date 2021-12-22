package uk.gov.moj.cpp.hearing.mapping;

import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationCourtRestrictionMapperTest {

    @Spy
    ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Spy
    JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @InjectMocks
    ApplicationCourtListRestrictionMapper applicationCourtListRestrictionMapper;

    @Before
    public void setUp() {
        setField(objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void testNullField() {
        Assert.assertFalse(applicationCourtListRestrictionMapper.getCourtListRestriction(null).isPresent());
    }

    @Test
    public void testEmpty() {
        Assert.assertFalse(applicationCourtListRestrictionMapper.getCourtListRestriction(" ").isPresent());
    }
}
