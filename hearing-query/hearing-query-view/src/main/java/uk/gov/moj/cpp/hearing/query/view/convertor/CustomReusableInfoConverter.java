package uk.gov.moj.cpp.hearing.query.view.convertor;


import static uk.gov.moj.cpp.hearing.query.view.service.ReusableInfoService.NATIONALITY;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;

public class CustomReusableInfoConverter {

    private Map<String, String> countryCodesMap;

    public Map<String, String> getCountryCodesMap() {
        return countryCodesMap;
    }

    public void setCountryCodesMap(final Map<String, String> countryCodesMap) {
        this.countryCodesMap = countryCodesMap;
    }

    public List<String> getConvertedValues(final List<String> promptValues, final String promptReference) {
        if (NATIONALITY.equalsIgnoreCase(promptReference)) {
            return MapUtils.isNotEmpty(getCountryCodesMap()) ?
                    promptValues.stream()
                            .map(val -> getCountryCodesMap()
                                    .get(val))
                            .collect(Collectors.toList()) :
                    promptValues;
        }

        return promptValues;
    }
}
