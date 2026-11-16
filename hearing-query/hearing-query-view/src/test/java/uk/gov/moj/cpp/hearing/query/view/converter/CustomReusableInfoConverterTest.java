package uk.gov.moj.cpp.hearing.query.view.converter;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.moj.cpp.hearing.query.view.service.ReusableInfoService.NATIONALITY;

import uk.gov.moj.cpp.hearing.query.view.convertor.CustomReusableInfoConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CustomReusableInfoConverterTest {

    private CustomReusableInfoConverter customReusableInfoConverter;

    @BeforeEach
    public void setUp() {
        customReusableInfoConverter = new CustomReusableInfoConverter();
    }

    @Test
    public void shouldMapNationalityCodesWhenCountryCodesMapIsPresent() {
        final Map<String, String> countryCodesMap = new HashMap<>();
        countryCodesMap.put("350", "British");
        countryCodesMap.put("460", "Irish");

        final List<String> result = customReusableInfoConverter.getConvertedValues(
                asList("350", "460"), NATIONALITY, countryCodesMap);

        assertThat(result, is(asList("British", "Irish")));
    }

    @Test
    public void shouldMapNationalityCodesWhenPromptReferenceIsDifferentCase() {
        final Map<String, String> countryCodesMap = new HashMap<>();
        countryCodesMap.put("350", "British");

        final List<String> result = customReusableInfoConverter.getConvertedValues(
                singletonList("350"), "NaTiOnAlItY", countryCodesMap);

        assertThat(result, is(singletonList("British")));
    }

    @Test
    public void shouldReturnOriginalValuesWhenCountryCodesMapIsEmpty() {
        final List<String> promptValues = asList("350", "460");

        final List<String> result = customReusableInfoConverter.getConvertedValues(
                promptValues, NATIONALITY, emptyMap());

        assertThat(result, is(promptValues));
    }

    @Test
    public void shouldReturnOriginalValuesWhenCountryCodesMapIsNull() {
        final List<String> promptValues = asList("350", "460");

        final List<String> result = customReusableInfoConverter.getConvertedValues(
                promptValues, NATIONALITY, null);

        assertThat(result, is(promptValues));
    }

    @Test
    public void shouldReturnOriginalValuesForNonNationalityPromptReference() {
        final Map<String, String> countryCodesMap = new HashMap<>();
        countryCodesMap.put("350", "British");
        final List<String> promptValues = singletonList("350");

        final List<String> result = customReusableInfoConverter.getConvertedValues(
                promptValues, "prosecutionAuthorityReference", countryCodesMap);

        assertThat(result, is(promptValues));
    }
}
