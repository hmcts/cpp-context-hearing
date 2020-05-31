package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared;

import uk.gov.justice.core.courts.Category;

import java.util.HashMap;
import java.util.Map;

public class CategoryEnumUtils {

    private static Map<String, Category> categoryMap;
    static {
        categoryMap = new HashMap<>();
        categoryMap.put("A", Category.ANCILLARY);
        categoryMap.put("F", Category.FINAL);
        categoryMap.put("I", Category.INTERMEDIARY);
    }

    private CategoryEnumUtils() {
    }

    public static Category getCategory(final String categoryInitial) {
       return categoryMap.getOrDefault(categoryInitial.toUpperCase(), null);
    }
}
