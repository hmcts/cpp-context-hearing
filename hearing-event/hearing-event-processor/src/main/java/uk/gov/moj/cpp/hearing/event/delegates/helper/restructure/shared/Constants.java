package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared;

public class Constants {
    public static final String RESULT_DEFINITION_NOT_FOUND_EXCEPTION_FORMAT = "ResultDefinition not found for resultLineId: %s, resultDefinitionId: %s, hearingId: %s orderedDate: %s";
    public static final String NO_PROMPT_DEFINITION_FOUND_EXCEPTION_FORMAT = "No prompt definition found for prompt id: %s label: %s value: %s resultDefinitionId: %s";
    public static final String REPLACEMENT_COMMA = ",";
    public static final String YES_INITIAL_STR = "Y";
    public static final String NO_INITIAL_STR = "N";
    public static final String ADJOURNMENT_REASONS = "Adjournment Reasons";
    public static final String CROWN_COURT_RESULT_DEFINITION_ID = "fbed768b-ee95-4434-87c8-e81cbc8d24c8";
    public static final String MAGISTRATE_RESULT_DEFINITION_ID = "70c98fa6-804d-11e8-adc0-fa7ae01bbebc";
    public static final String SPACE = " ";
    public static final String DATE_FORMATS = "[dd/MM/yyyy HH:mm][yyyy-MM-dd HH:mm][dd MMM yyyy HH:mm]";
    public static final String EUROPE_LONDON = "Europe/London";
    public static final String COMMA_REGEX = "\\s*,\\s*";
    public static final int DAYS_IN_A_WEEK = 5;
    public static final int HOURS_IN_A_DAY = 6;
    public static final int MINUTES_IN_HOUR = 60;
    public static final int MINUTES_IN_A_DAY = HOURS_IN_A_DAY * MINUTES_IN_HOUR;
    public static final String START_OF_DAY_TIME = "00:00";

    private Constants() {
    }
}
