package uk.gov.moj.cpp.hearing.query.view.convertor;


import static uk.gov.moj.cpp.hearing.query.view.service.ReusableInfoService.NATIONALITY;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;

public class CustomReusableInfoConverter {

    public List<String> getConvertedValues(final List<String> promptValues,
                                           final String promptReference,
                                           final Map<String, String> countryCodesMap) {
        if (NATIONALITY.equalsIgnoreCase(promptReference)) {
            return MapUtils.isNotEmpty(countryCodesMap) ?
                    promptValues.stream()
                            .map(countryCodesMap::get)
                            .collect(Collectors.toList()) :
                    promptValues;
        }

        return promptValues;
    }
}
