package uk.gov.moj.cpp.hearing.query.view;

import uk.gov.justice.services.common.converter.ListToJsonArrayConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory;

import java.lang.reflect.Field;

/**
 * ToDo: this class needs to be moved to framework as it's a common utility and instead of
 * reflection an implementation similar to {@link EnveloperFactory} needs to be used
 */
public class ListToJsonArrayConverterFactory<T> {

    public static <T> ListToJsonArrayConverter<T> createListToJsonArrayConverter() {
        return new ListToJsonArrayConverterFactory<T>().create();
    }

    private ListToJsonArrayConverter<T> create() throws RuntimeException {
        final ListToJsonArrayConverter<T> listToJsonArrayConverter = new ListToJsonArrayConverter<>();
        final Field stringToJsonObjectConverterField;
        final Field mapperField;

        try {
            stringToJsonObjectConverterField = ListToJsonArrayConverter.class.getDeclaredField("stringToJsonObjectConverter");
            mapperField = ListToJsonArrayConverter.class.getDeclaredField("mapper");
        } catch (final NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        stringToJsonObjectConverterField.setAccessible(true);
        mapperField.setAccessible(true);

        try {
            stringToJsonObjectConverterField.set(listToJsonArrayConverter, new StringToJsonObjectConverter());
            mapperField.set(listToJsonArrayConverter, new ObjectMapperProducer().objectMapper());
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return listToJsonArrayConverter;
    }
}
