package uk.gov.moj.cpp.hearing.event.delegates;

import static java.util.Locale.ENGLISH;

import uk.gov.moj.cpp.hearing.event.delegates.exception.InvalidDateFormatException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublishResultUtil {

    public static final String OUTGOING_PROMPT_DATE_FORMAT = "dd/MM/yyyy";
    private static final String INCOMING_PROMPT_DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATE_PROMPT_TYPE = "DATE";
    private static final String CURRENCY_PROMPT_TYPE = "CURR";
    private static final Logger LOGGER = LoggerFactory.getLogger(PublishResultUtil.class.getName());
    private static final String POUND_CURRENCY_LABEL = "£";

    private PublishResultUtil() {

    }

    public static String reformatValue(String value, final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt promptRef) {
        if (value == null) {
            return null;
        }
        value = value.trim();
        if (promptRef != null && DATE_PROMPT_TYPE.equals(promptRef.getType()) && value.length() > 0 && onlyIfNotEqualsToOutgoingFormat(OUTGOING_PROMPT_DATE_FORMAT, value, ENGLISH)) {
            try {
                final LocalDate dateValue = LocalDate.parse(value, DateTimeFormatter.ofPattern(INCOMING_PROMPT_DATE_FORMAT));
                value = dateValue.format(DateTimeFormatter.ofPattern(OUTGOING_PROMPT_DATE_FORMAT));
            } catch (DateTimeParseException parseException) {
                throw new InvalidDateFormatException(String.format("invalid format for incoming date prompt id: %s value: %s", promptRef.getId(), value), parseException);
            }
        } else if (promptRef != null && CURRENCY_PROMPT_TYPE.equals(promptRef.getType()) && value.length() > 0 && !value.startsWith(POUND_CURRENCY_LABEL)) {
            value = POUND_CURRENCY_LABEL + value;
        }
        return value;
    }

    private static boolean onlyIfNotEqualsToOutgoingFormat(final String format, final String value, final Locale locale) {
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format, locale);
        boolean isNotEqualToOutgoingFormat = true;
        try {
            final LocalDate localDate = LocalDate.parse(value, dateTimeFormatter);
            final String result = localDate.format(dateTimeFormatter);
            isNotEqualToOutgoingFormat = !result.equals(value);
        } catch (DateTimeParseException e) {
            LOGGER.trace(String.format("Ignore this error (sonar forcing to log error) : Invalid date - %s", value), e);
        }
        return isNotEqualToOutgoingFormat;
    }
}
