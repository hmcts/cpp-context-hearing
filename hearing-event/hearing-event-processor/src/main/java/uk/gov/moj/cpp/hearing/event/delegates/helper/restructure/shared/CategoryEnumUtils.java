package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared;

import uk.gov.justice.core.courts.JudicialResultCategory;

import java.util.HashMap;
import java.util.Map;

public class CategoryEnumUtils {

    private static Map<String, JudicialResultCategory> categoryMap;
    static {
        categoryMap = new HashMap<>();
        categoryMap.put("A", JudicialResultCategory.ANCILLARY);
        categoryMap.put("F", JudicialResultCategory.FINAL);
        categoryMap.put("I", JudicialResultCategory.INTERMEDIARY);
    }

    private CategoryEnumUtils() {
    }

    public static JudicialResultCategory getCategory(final String categoryInitial) {
       return categoryMap.getOrDefault(categoryInitial.toUpperCase(), null);
    }
}
